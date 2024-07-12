package com.mskeycloak.service.impl;

import com.mskeycloak.error.exception.BadRequestException;
import com.mskeycloak.error.exception.NotFoundException;
import com.mskeycloak.model.Role;
import com.mskeycloak.service.IRoleService;
import com.mskeycloak.config.KeycloakConfig;
import com.mskeycloak.repository.IRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service Implementation for {@link Role} entity.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RoleService implements IRoleService {

    private final IRoleRepository roleRepository;
    private final Keycloak keycloak;
    private final KeycloakConfig keycloakConfig;

    /**
     * {@inheritDoc}
     */
    @Override
    public Role create(Role role) {
        log.debug("Request to create Role : {}", role);
        role.setId(null);
        try {
            keycloak.realm(keycloakConfig.getRealm()).roles().get(role.getName()).toRepresentation();
        } catch (Exception e) {
            if (!e.getMessage().equals("HTTP 404 Not Found")) {
                throw new BadRequestException("Role existe  dans keycloak");
            }
            Optional<Role> existingRole = roleRepository.findByName(role.getName());

            if (existingRole.isPresent() && !existingRole.get().isStatus()) {
                throw new BadRequestException("Role existe  dans la base locale avec le status false");
            }
            RoleRepresentation newRole = new RoleRepresentation();
            newRole.setName(role.getName());
            newRole.setDescription(role.getDescription());
            keycloak.realm(keycloakConfig.getRealm()).roles().create(newRole);
            return roleRepository.save(role);
        }
        throw new BadRequestException("Role existe ");
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Role> getRoles() {
        log.debug("Request to get all Roles");
        return roleRepository.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Role> getAllRolesWithStatusFalse() {
        log.debug("Request to get all Roles with status false");
        return roleRepository.getAllRolesByStatusFalse();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Role findByName(String name) {
        log.debug("Request to get Role : {}", name);
        return roleRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException("Role non trouvé avec le nom : " + name));
    }

    @Override
    public void delete(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Role " + id + " non trouvé avec l'ID"));
        role.setStatus(true);
        try {
            RoleResource roleResource = keycloak.realm(keycloakConfig.getRealm()).roles().get(role.getName());
            roleResource.remove();
            roleRepository.save(role);
        } catch (Exception e) {
            log.error("Erreur lors de la suppression du rôle : {}", e.getMessage());
            throw new BadRequestException("Erreur lors de la suppression du rôle : " + e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Role findById(Long id) {
        log.debug("Request to get Role : {}", id);
        return roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Role non trouvé avec l'ID : " + id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Role update(Long id, Role role) {
        log.debug("Request to update Role : {}", role);
        Role existingRole = roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Role non trouvé avec l'ID : " + id));

        RoleResource roleResource = keycloak.realm(keycloakConfig.getRealm()).roles().get(role.getName());
        RoleRepresentation roleToUpdate = roleResource.toRepresentation();
        if (roleToUpdate == null) {
            throw new NotFoundException("Role non trouvé avec le nom : " + role.getName());
        }

        roleToUpdate.setDescription(role.getDescription());
        roleResource.update(roleToUpdate);
        existingRole.setDescription(role.getDescription());
        return roleRepository.save(existingRole);
    }

}
