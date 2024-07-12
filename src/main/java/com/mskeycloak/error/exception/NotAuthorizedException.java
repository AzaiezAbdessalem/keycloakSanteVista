package com.mskeycloak.error.exception;

import lombok.Getter;
import lombok.ToString;

/**
 * Exception thrown when a user is not authorized to perform an action.
 */
@Getter
@ToString
public class NotAuthorizedException extends RuntimeException {

    public NotAuthorizedException(String message) {
        super(message);
    }

}
