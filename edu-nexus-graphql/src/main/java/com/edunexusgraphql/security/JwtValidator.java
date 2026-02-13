package com.edunexusgraphql.security;

import io.jsonwebtoken.Claims;

/**
 * Interface for JWT token validation.
 * Follows Dependency Inversion Principle - high-level modules depend on this abstraction.
 */
public interface JwtValidator {
    /**
     * Validates a JWT token and returns its claims.
     *
     * @param token the JWT token to validate
     * @return Claims containing userId, role, and other token data
     * @throws InvalidTokenException if the token is invalid, expired, or malformed
     */
    Claims validateToken(String token) throws InvalidTokenException;
}
