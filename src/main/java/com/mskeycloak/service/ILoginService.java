package com.mskeycloak.service;

import com.mskeycloak.error.exception.BadRequestException;
import com.mskeycloak.error.exception.NotAuthorizedException;
import com.mskeycloak.error.exception.NotFoundException;
import com.mskeycloak.dto.LoginRequest;
import com.mskeycloak.dto.LoginResponse;

/**
 * Service for Authentication.
 */
public interface ILoginService {

    void checkVerificationCode(String email, String code);

    /**
     * Send a verification code to the user.
     *
     * @param email    the email of the user
     * @param password the password of the user
     * @throws NotFoundException   if the user does not exist.
     * @throws BadRequestException if the user is not valid.
     */
    void resetPassword(String email, String password);

    void changePassword(String username, String currentPassword, String newPassword);

    /**
     * Authenticate the user.
     *
     * @param loginrequest the login request with the username and the password
     * @return the ResponseEntity with status 200 (OK) and the tokens in body,
     * @throws NotAuthorizedException if the credentials are incorrect.
     */
    LoginResponse login(LoginRequest loginrequest);

    /**
     * Logout the user.
     *
     * @param userId the id of the user
     * @throws BadRequestException if the user cannot be disconnected.
     */
    void logout(String userId);

    String findAccount(String email);
}
