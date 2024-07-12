package com.mskeycloak.error.exception;

import lombok.Getter;
import lombok.ToString;

/**
 * Exception thrown when a request should not be processed.
 * Use this to automatically return a 400 response to the client.
 */
@Getter
@ToString
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }

}
