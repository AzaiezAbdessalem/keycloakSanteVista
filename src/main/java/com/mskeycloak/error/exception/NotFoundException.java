package com.mskeycloak.error.exception;

import lombok.Getter;
import lombok.ToString;

/**
 * Exception thrown when an entity is not found.
 * Use this to automatically return a 404 response to the client.
 */
@Getter
@ToString
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

}
