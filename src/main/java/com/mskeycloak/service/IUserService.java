package com.mskeycloak.service;

import com.mskeycloak.error.exception.BadRequestException;
import com.mskeycloak.error.exception.NotFoundException;
import com.mskeycloak.model.User;

import java.util.List;

public interface IUserService {

    /**
     * Delete a user.
     * @param id the id of the user to delete.
     * @throws NotFoundException if the user couldn't be found.
     * @throws BadRequestException if the user couldn't be deleted.
     */
    void deleteUser(String id);

    /**
     * Enable or disable a user.
     * @param userId the id of the user to enable or disable.
     * @throws NotFoundException if the user couldn't be found.
     */
    void toggleUserEnabled(String userId);

    /**
     * Create a new user.
     * @param user the user to create.
     * @return the created user.
     * @throws BadRequestException if the user already exists.
     */
    User create(User user);

    /**
     * Find a user by username.
     * @param username the username of the user to find.
     * @return the user.
     * @throws NotFoundException if the user couldn't be found.
     */
    User findByUsername(String username);

    /**
     * Find a user by id.
     * @param id the id of the user to find.
     * @return the user.
     * @throws NotFoundException if the user couldn't be found.
     */
    User findById(String id);

    List<User> findAll();

    User updateUser(User user);
}
