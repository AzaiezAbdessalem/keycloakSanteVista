package com.mskeycloak.service.impl;

import com.mskeycloak.client.IMailClient;
import com.mskeycloak.dto.MailDto;
import com.mskeycloak.error.exception.BadRequestException;
import com.mskeycloak.error.exception.NotFoundException;
import com.mskeycloak.model.Role;
import com.mskeycloak.repository.IUserRepository;
import com.mskeycloak.service.IUserService;
import com.mskeycloak.config.KeycloakConfig;
import com.mskeycloak.model.User;
import com.mskeycloak.repository.IRoleRepository;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * Service class for managing {@link User}.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final IUserRepository userRepository;
    private final IRoleRepository roleRepository;
    private final Keycloak keycloak;
    private final KeycloakConfig keycloakConfig;

    private final IMailClient mailClient;

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteUser(String id) {
        log.debug("SERVICE : deleteUser : {}", id);
        UsersResource usersResource = keycloak.realm(keycloakConfig.getRealm()).users();
        try {
            UserRepresentation user = usersResource.get(id).toRepresentation();

            try (Response response = usersResource.delete(id)) {
                if (user!=null && response.getStatus() != 204) {
                    throw new BadRequestException("Erreur lors de la suppression de l'utilisateur");
                }
            }
        }catch (Exception e){
            log.error("Utilisateur introuvable dans serveur Keycloak : {}", id);
        }
        userRepository.deleteById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User toggleUserEnabled(String userId) {
        log.debug("SERVICE : toggleUserEnabled : {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User " + userId + " not found"));
        UserRepresentation userRepresentation = keycloak
                .realm(keycloakConfig.getRealm())
                .users()
                .get(userId)
                .toRepresentation();
        final boolean previousStatus = userRepresentation.isEnabled();
        user.setEnabled(!previousStatus);
        userRepository.save(user);
        userRepresentation.setEnabled(!previousStatus);
        keycloak.realm(keycloakConfig.getRealm()).users().get(userId).update(userRepresentation);
        if (!previousStatus) {
            sendActivationEmail(user);
        }

        return user;
    }

    private void sendActivationEmail(User user) {
        System.out.println(user.isEnabled());
        MailDto mail=new MailDto();
        String subject = "Votre compte a été activé";
        String message = "";
        mail.setSubject(subject);
        mail.setMailTo(user.getEmail());
        mailClient.activatedAccount(mail);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public User create(User user) {
        log.info("SERVICE : createUser : {}", user);

        // Créer la représentation de l'utilisateur
        UserRepresentation newUser = new UserRepresentation();
        newUser.setUsername(user.getUsername());
        newUser.setEmail(user.getEmail());
        newUser.setFirstName(user.getFirstname());
        newUser.setLastName(user.getLastname());
        newUser.setEnabled(false);

        // Ajouter les credentials (mot de passe)
        CredentialRepresentation credentials = new CredentialRepresentation();
        credentials.setTemporary(false);
        credentials.setType(CredentialRepresentation.PASSWORD);
        credentials.setValue(user.getPassword());
        newUser.setCredentials(List.of(credentials));

        // Vérifier si l'email existe déjà
        if (userRepository.findByEmailIgnoreCase(user.getEmail()).isPresent()) {
            throw new BadRequestException("Email déjà existant");
        }

        // Ajouter l'attribut firstLogin
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("firstLogin", Collections.singletonList("true"));
        newUser.setAttributes(attributes);

        // Créer l'utilisateur dans Keycloak
        Response response = keycloak.realm(keycloakConfig.getRealm()).users().create(newUser);
        log.info("SERVICE : createUser : {}", response.getStatus());

        if (response.getStatus() != 201) {
            throw new BadRequestException("Erreur lors de la création de l'utilisateur");
        }

        // Récupérer l'ID de l'utilisateur créé
        String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
        UserRepresentation createdUser = keycloak
                .realm(keycloakConfig.getRealm())
                .users()
                .get(userId)
                .toRepresentation();

        // Assigner les rôles à l'utilisateur
        Collection<Role> roles = user.getRoles();
        if (!roles.isEmpty()) {
            for (Role r : roles) {
                RoleRepresentation roleRepresentation = keycloak
                        .realm(keycloakConfig.getRealm())
                        .roles()
                        .get(r.getName())
                        .toRepresentation();
                log.info("SERVICE : roleRepresentation : {}", roleRepresentation);
                keycloak.realm(keycloakConfig.getRealm()).users()
                        .get(userId)
                        .roles()
                        .realmLevel()
                        .add(Collections.singletonList(roleRepresentation));
            }
        }

        // Créer l'utilisateur dans la base de données locale
        User userToCreateInLocalDb = User.builder()
                .username(user.getUsername())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .gender(user.getGender())
                .id(createdUser.getId())
                .email(user.getEmail())
                .roles(roles)
                .build();
        userToCreateInLocalDb = userRepository.save(userToCreateInLocalDb);

        return userToCreateInLocalDb;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        log.debug("SERVICE : getUser : {}", username);
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public User findById(String id) {
        log.debug("SERVICE : getUserById : {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        log.debug("SERVICE : getUsers");
        return userRepository.findAll();
    }

    @Override
    public User updateUser(User user) {
        final String userId = user.getId();
        UserResource existingUserResource = keycloak.realm(keycloakConfig.getRealm()).users().get(userId);
        UserRepresentation existingUser = existingUserResource.toRepresentation();
        if (existingUser == null) {
            throw new NotFoundException("Utilisateur non trouvé");
        }
        existingUser.setFirstName(user.getFirstname());
        existingUser.setLastName(user.getLastname());
        existingUser.setEmail(user.getEmail());
        existingUserResource.update(existingUser);
        if(CollectionUtils.isEmpty(user.getRoles())){
            return userRepository.save(user);
        }
        List<RoleRepresentation> existingUserRoles = existingUserResource
                .roles()
                .realmLevel()
                .listAll();
        existingUserResource
                .roles()
                .realmLevel()
                .remove(existingUserRoles);

        List<RoleRepresentation> rolesToAdd = user.getRoles().stream()
                .map(role -> keycloak
                        .realm(keycloakConfig.getRealm())
                        .roles()
                        .get(role.getName())
                        .toRepresentation())
                .toList();

        existingUserResource
                .roles()
                .realmLevel()
                .add(rolesToAdd);

        return userRepository.save(user);
    }

}
