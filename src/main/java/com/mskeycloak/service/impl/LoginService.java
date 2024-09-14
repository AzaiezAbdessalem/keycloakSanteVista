package com.mskeycloak.service.impl;

import com.mskeycloak.client.IMailClient;
import com.mskeycloak.config.KeycloakConfig;
import com.mskeycloak.dto.LoginRequest;
import com.mskeycloak.dto.LoginResponse;
import com.mskeycloak.dto.MailDto;
import com.mskeycloak.error.exception.BadRequestException;
import com.mskeycloak.error.exception.NotAuthorizedException;
import com.mskeycloak.error.exception.NotFoundException;
import com.mskeycloak.model.User;
import com.mskeycloak.repository.IUserRepository;
import com.mskeycloak.service.ILoginService;
import com.mskeycloak.utils.RandomUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.security.auth.login.AccountLockedException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Service Implementation for Authentication.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class LoginService implements ILoginService {

    private final IMailClient iMailClient;
    private final IUserRepository userRepository;
    private final Keycloak keycloak;
    private final KeycloakConfig keycloakConfig;

    private static final String VERIF_CODE = "VerificationCode";
    private static final String VERIF_CODE_DATE = "VerificationCodeDate";


    private static long getDifferenceInMinutes(String codeVerifyTime) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale.US);
            Date verificationCodeDate = dateFormat.parse(codeVerifyTime);
            Date currentDate = new Date();
            long differenceInMillis = currentDate.getTime() - verificationCodeDate.getTime();
            return differenceInMillis / (60 * 1000);
        } catch (ParseException e) {
            throw new BadRequestException("Erreur lors de la conversion de la date.");
        }
    }

    private static Map<String, List<String>> getAttributesFromUser(UserRepresentation user) {
        Map<String, List<String>> attributes = user.getAttributes();
        if (!attributes.containsKey(VERIF_CODE) || !attributes.containsKey(VERIF_CODE_DATE)) {
            throw new NotAuthorizedException(
                    "L'utilisateur a un attribut VerificationCode, mais sa valeur est vide."
            );
        }
        return attributes;
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public void resetPassword(String email, String password) {
        log.debug("SERVICE to reset password of {}", email);
        try {
            List<UserRepresentation> users = keycloak
                    .realm(keycloakConfig.getRealm())
                    .users()
                    .searchByEmail(email, true);
            if (users.isEmpty()) {
                throw new NotFoundException("");
            }
            UserRepresentation user = users.get(0);

            CredentialRepresentation newCredential = toCredentialRepresentation(password);
            user.setCredentials(Collections.singletonList(newCredential));

            Map<String, List<String>> attributes = user.getAttributes();
            if (attributes != null) {
                attributes.remove(VERIF_CODE);
                attributes.remove(VERIF_CODE_DATE);
                user.setAttributes(attributes);
            }

            keycloak.realm(keycloakConfig.getRealm()).users().get(user.getId()).update(user);
        } catch (NotFoundException notFoundEx) {
            throw new NotFoundException("Aucun utilisateur avec l'adresse e-mail : " + email);
        } catch (Exception e) {
            throw new BadRequestException("Erreur lors de la mise à jour de l'utilisateur : " + e.getMessage());
        }
    }

    private CredentialRepresentation toCredentialRepresentation(String password) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);
        return credential;
    }

    @Override
    public void changePassword(String username, String currentPassword, String newPassword) {
        try {
            List<UserRepresentation> usersByUsername = keycloak
                    .realm(keycloakConfig.getRealm())
                    .users()
                    .search(username);
            if (usersByUsername.isEmpty()) {
                throw new NotFoundException("");
            }
            LoginRequest loginRequest = new LoginRequest(username, currentPassword);
            login(loginRequest);
            UserResource userToUpdate = keycloak
                    .realm(keycloakConfig.getRealm())
                    .users()
                    .get(usersByUsername.get(0).getId());
            CredentialRepresentation credentials = new CredentialRepresentation();
            credentials.setType(CredentialRepresentation.PASSWORD);
            credentials.setValue(newPassword);
            userToUpdate.resetPassword(credentials);
        } catch (NotFoundException notFoundEx) {
            throw new NotFoundException("Aucun utilisateur avec le nom d'utilisateur : " + username);
        } catch (Exception e) {
            throw new BadRequestException("Erreur lors de la modification du mot de passe : " + e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        log.debug("SERVICE to login {}", loginRequest.getUsername());
        User user = null;

        if (!loginRequest.getUsername().equals("hela")) {
            user = userRepository.findByUsernameIgnoreCase(loginRequest.getUsername())
                    .orElseThrow(() -> new NotAuthorizedException("Utilisateur non trouvé avec le nom d'utilisateur"));
        }

        if (user != null && user.getAccountLocked() != null && user.getAccountLocked().booleanValue() &&
                LocalDateTime.now().isBefore(user.getUnlockTime())) {
        } else if (user != null && user.getAccountLocked() != null && user.getAccountLocked().booleanValue()) {
            user.setAccountLocked(false);
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
        }

        try (Keycloak instanceKeycloakUser = keycloakConfig.instantiateKeycloakUser(
                loginRequest.getUsername(),
                loginRequest.getPassword()
        )) {
            LoginResponse loginResponse = new LoginResponse();
            AccessTokenResponse accessTokenResponse = instanceKeycloakUser.tokenManager().grantToken();
            loginResponse.setAccess_token(accessTokenResponse.getToken());
            loginResponse.setRefresh_token(accessTokenResponse.getRefreshToken());

            return loginResponse;

        } catch (Exception e) {
            throw new NotAuthorizedException("Login failed");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logout(String userId) {
        log.debug("SERVICE to logout {}", userId);
        try {
            keycloak.realm(keycloakConfig.getRealm()).users().get(userId).logout();
        } catch (Exception e) {
            log.error("Erreur lors de la déconnexion : " + e.getMessage());
            throw new BadRequestException("Échec de la déconnexion");
        }
    }

    public String findAccount(String email) {
        try {
            RealmResource realmResource = keycloak.realm(keycloakConfig.getRealm());
            List<UserRepresentation> user = realmResource.users().searchByEmail(email, true);
            if (user.isEmpty()) {
                return "pas de compte avec email : " + email;
            }
            else {
                String code = RandomUtils.generateRandomCode();
                MailDto mailDto = new MailDto();
                mailDto.setTypeMail("restPasswordMail");
                mailDto.setBody(code);
                mailDto.setMailTo(email);
                mailDto.setSubject("Votre code de vérification de compte santeVista");
                Date currentDate = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale.US);
                String currentDateAsString = dateFormat.format(currentDate);
                 User userFound=userRepository.findByEmailIgnoreCase(email).get();
                userFound.setVERIF_CODE(code);
                userFound.setVERIF_CODE_DATE(currentDateAsString);
                userRepository.save(userFound);
                iMailClient.sendResetEmail(mailDto);
                return " Code de vérification envoyé Vérifiez votre email :  " + email;
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }

    }
    @Override
    public void checkVerificationCode(String email, String code) {
        log.debug("SERVICE to check verification code {} of {}", code, email);
        List<UserRepresentation> users = keycloak
                .realm(keycloakConfig.getRealm())
                .users()
                .searchByEmail(email, true);
        if (users.isEmpty()) {
            throw new NotAuthorizedException("Aucun utilisateur avec l'adresse e-mail");
        }
        User userFound=userRepository.findByEmailIgnoreCase(email).get();
        String codeVerify = userFound.getVERIF_CODE();
        String codeVerifyTime = userFound.getVERIF_CODE_DATE();
        if ( codeVerify.equals(null) ||  codeVerifyTime.equals(null) ) {
            throw new NotAuthorizedException("L'utilisateur n'a pas encore d'attribut VerificationCode.");
        }
        final String verificationCode = codeVerify;
        final String verificationCodeTime = codeVerifyTime;
        if (!verificationCode.equals(code)) {
            throw new NotAuthorizedException("Le code de vérification invalide.");
        }
        final long differenceInMinutes = getDifferenceInMinutes(verificationCodeTime);
        if (differenceInMinutes > 30) {
            throw new NotAuthorizedException("Le code de vérification a expiré.");
        }
        userFound.setVERIF_CODE(null);
        userFound.setVERIF_CODE_DATE(null);
        userRepository.save(userFound);
    }

}
