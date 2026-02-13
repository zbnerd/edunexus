package com.edunexusgraphql.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Implementation of JWT token validator.
 * Follows Single Responsibility Principle - only handles JWT validation.
 */
@Service
@Slf4j
public class JwtValidatorImpl implements JwtValidator {

    private final SecretKey signingKey;
    private static final String DEFAULT_SECRET = "default-secret-key-for-development-only-change-in-production-minimum-256-bits";

    public JwtValidatorImpl(@Value("${jwt.secret:}") String secretKey) {
        String keyToUse = (secretKey == null || secretKey.isBlank()) ? DEFAULT_SECRET : secretKey;
        this.signingKey = Keys.hmacShaKeyFor(keyToUse.getBytes(StandardCharsets.UTF_8));
        if (secretKey == null || secretKey.isBlank()) {
            log.warn("JWT secret not configured, using default key. This should NOT be used in production!");
        } else {
            log.info("JwtValidator initialized");
        }
    }

    @Override
    public Claims validateToken(String token) throws InvalidTokenException {
        try {
            return Jwts.parser()
                    .setSigningKey(signingKey)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            throw new InvalidTokenException("Invalid or expired JWT token", e);
        }
    }
}
