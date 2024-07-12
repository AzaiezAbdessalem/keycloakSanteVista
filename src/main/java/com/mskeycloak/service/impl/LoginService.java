package com.mskeycloak.service.impl;

import com.mskeycloak.error.exception.BadRequestException;
import com.mskeycloak.error.exception.NotAuthorizedException;
import com.mskeycloak.error.exception.NotFoundException;
import com.mskeycloak.client.IMailClient;
import com.mskeycloak.config.KeycloakConfig;
import com.mskeycloak.dto.LoginRequest;
import com.mskeycloak.dto.LoginResponse;
import com.mskeycloak.dto.MailDto;
import com.mskeycloak.model.User;
import com.mskeycloak.repository.IUserRepository;
import com.mskeycloak.service.ILoginService;
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

import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
        if (!attributes.containsKey("VerificationCode") || !attributes.containsKey("VerificationCodeDate")) {
            throw new NotAuthorizedException(
                    "L'utilisateur a un attribut VerificationCode, mais sa valeur est vide."
            );
        }
        return attributes;
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
        Map<String, List<String>> attributes = getAttributesFromUser(users.get(0));
        List<String> codeVerify = attributes.get("VerificationCode");
        List<String> codeVerifyTime = attributes.get("VerificationCodeDate");
        if (CollectionUtils.isEmpty(codeVerify) || CollectionUtils.isEmpty(codeVerifyTime)) {
            throw new NotAuthorizedException("L'utilisateur n'a pas encore d'attribut VerificationCode.");
        }
        final String verificationCode = codeVerify.get(0);
        final String verificationCodeTime = codeVerifyTime.get(0);
        if (!verificationCode.equals(code)) {
            throw new NotAuthorizedException("Le code de vérification invalide.");
        }
        final long differenceInMinutes = getDifferenceInMinutes(verificationCodeTime);
        if (differenceInMinutes > 30) {
            throw new NotAuthorizedException("Le code de vérification a expiré.");
        }
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
            keycloak.realm(keycloakConfig.getRealm()).users().get(user.getId()).update(user);
        } catch (NotFoundException notFoundEx) {
            throw new NotFoundException("Aucun utilisateur avec l'adresse e-mail : " + email);
        } catch (jakarta.ws.rs.BadRequestException badRequestEx) {
            throw new BadRequestException("Requête incorrecte lors de la mise à jour de l'utilisateur : " +
                    badRequestEx.getMessage());
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
    public LoginResponse login(LoginRequest loginrequest) {
        log.debug("SERVICE to login {}", loginrequest.getUsername());
        log.debug("SERVICE to login {}", loginrequest.getPassword());

        User user = null;

        if (!loginrequest.getUsername().equals("ons")) {
            user = userRepository.findByUsername(loginrequest.getUsername())
                    .orElseThrow(() -> new NotAuthorizedException("Utilisateur non trouvé avec le nom d'utilisateur"));
        }

        try (
                Keycloak instanceKeycloakUser = keycloakConfig.instantiateKeycloakUser(
                loginrequest.getUsername(),
                loginrequest.getPassword())

        )
                {

                    LoginResponse loginResponse = new LoginResponse();
            AccessTokenResponse accessTokenResponse = instanceKeycloakUser.tokenManager().grantToken();
            loginResponse.setAccess_token(accessTokenResponse.getToken());
            loginResponse.setRefresh_token(accessTokenResponse.getRefreshToken());

            return loginResponse;
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
    public String findAccount(String email ) {
        try {

            // Get the realm resource
            RealmResource realmResource = keycloak.realm(keycloakConfig.getRealm());
            List<UserRepresentation> user = realmResource.users().searchByEmail(email, true);
            if (user.isEmpty()) {
                return "pas de compte avec email : " + email;
            }
            else {
                String code=generateRandomCode();
                MailDto mailDto=new MailDto();
                mailDto.setTypeMail("restPasswordMail");
                mailDto.setBody(code);
                mailDto.setMailTo(email);
                mailDto.setSubject("Votre code de vérification de compte Eleapp");
                Map<String, List<String>> attributes = user.get(0).getAttributes();

                if (attributes == null) {
                    attributes = new HashMap<>();
                }

                // Ajouter l'attribut "VerificationCode" avec sa valeur
                attributes.put("VerificationCode", Arrays.asList(code));
                Date currentDate = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale.US);
                String currentDateAsString = dateFormat.format(currentDate);

                attributes.put("VerificationCodeDate", Arrays.asList(currentDateAsString));

                // Mettre à jour les attributs de l'utilisateur avec le nouvel attribut "VerificationCode"
                user.get(0).setAttributes(attributes);

                realmResource.users().get(user.get(0).getId()).update(user.get(0));

                iMailClient.sendResetEmail(mailDto).getBody();

                    return " Code de vérification envoyé Vérifiez votre email :  " + email;


            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }

    }
    public static String generateRandomCode() {
        String characters = "123456789";
        StringBuilder sb = new StringBuilder();
        SecureRandom random = new SecureRandom();
        int length = 8;
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(characters.length());
            char randomChar = characters.charAt(randomIndex);
            sb.append(randomChar);
        }

        return sb.toString();
    }
}
