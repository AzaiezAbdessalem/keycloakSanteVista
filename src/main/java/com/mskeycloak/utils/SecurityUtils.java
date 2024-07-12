package com.mskeycloak.utils;

import com.mskeycloak.error.exception.NotAuthorizedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.security.Principal;
import java.util.Optional;

/**
 * Utility class for Spring Security.
 */
public class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * Get the keycloak id of the current user connected.
     * Should be used to get the id and make sure the current user can only access his own data.
     *
     * @return the keycloak id of the current user.
     * @throws NotAuthorizedException if the user is not connected.
     */
    public static String getCurrentUserId() {
        try {
            Principal principal = SecurityContextHolder.getContext().getAuthentication();
            Optional<String> userId = Optional.ofNullable(principal.getName());
            return userId
                    .orElseThrow(() -> new NotAuthorizedException(""));
        } catch (Exception e) {
            throw new NotAuthorizedException("Vous n'êtes pas autorisé à accéder à cette ressource");
        }
    }

    /**
     * Get the email of the current user connected.
     *
     * @return the email of the current user.
     * @throws NotAuthorizedException if the user is not connected.
     */
    public static String getCurrentUserEmail() {
        try {
            JwtAuthenticationToken principal =
                    (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
            Optional<String> userEmail = Optional.ofNullable(principal.getToken().getClaimAsString("email"));
            return userEmail.orElseThrow(() -> new NotAuthorizedException(""));
        } catch (Exception e) {
            throw new NotAuthorizedException("Vous n'êtes pas autorisé à accéder à cette ressource");
        }
    }

}
