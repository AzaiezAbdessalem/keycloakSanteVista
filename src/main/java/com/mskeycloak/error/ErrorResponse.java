package com.mskeycloak.error;

import java.time.Instant;

/**
 * ErrorResponse is the generic error response we can send to the client when catching an exception.
 */
public record ErrorResponse(
        String message,
        String code,
        String statusMessage,
        Instant timestamp
) {
}
