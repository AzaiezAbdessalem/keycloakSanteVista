package com.mskeycloak.service;

import com.mskeycloak.error.exception.NotFoundException;
import com.mskeycloak.model.Role;

import java.util.List;

/**
 * Interface Service for {@link Role} entity.
 */
public interface IRoleService {

    /**
     * Create a new {@link Role}
     *
     * @param role the {@link Role} to create
     * @return the created {@link Role} with id
     */
    Role create(Role role);

    /**
     * Get all {@link Role}
     *
     * @return the list of {@link Role}
     */
    List<Role> getRoles();

    /**
     * Get all {@link Role} with status false
     *
     * @return the list of {@link Role}
     */
    List<Role> getAllRolesWithStatusFalse();

    /**
     * Find a {@link Role} by name
     *
     * @param name the name of the {@link Role} to find
     * @return the {@link Role} found
     * @throws NotFoundException if the {@link Role} is not found
     */
    Role findByName(String name);

    void delete(Long id);

    /**
     * Find a {@link Role} by id
     *
     * @param id the id of the {@link Role} to find
     * @return the {@link Role} found
     * @throws NotFoundException if the {@link Role} is not found
     */
    Role findById(Long id);

    /**
     * Update a {@link Role}
     *
     * @param id   the id of the {@link Role} to update
     * @param role the {@link Role} to update
     * @return the updated {@link Role}
     * @throws NotFoundException if the {@link Role} is not found
     */
    Role update(Long id, Role role);

}
