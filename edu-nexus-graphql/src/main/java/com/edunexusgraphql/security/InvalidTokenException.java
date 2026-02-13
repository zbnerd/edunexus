package com.edunexusgraphql.security;

/**
 * Exception thrown when JWT token validation fails.
 * Used for invalid, expired, or malformed tokens.
 */
public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
