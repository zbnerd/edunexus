package com.edunexusgraphql.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtValidatorImpl
 *
 * Test coverage:
 * - Happy path: Valid token validation
 * - Error cases: Invalid tokens, expired tokens, malformed tokens
 * - Edge cases: Null tokens, empty tokens, blank tokens
 * - Configuration: Default key vs custom key
 */
@DisplayName("JwtValidatorImpl Tests")
class JwtValidatorImplTest {

    private static final String CUSTOM_SECRET = "my-custom-secret-key-for-testing-purposes-at-least-256-bits-long";
    private static final String DEFAULT_SECRET = "default-secret-key-for-development-only-change-in-production-minimum-256-bits";

    private JwtValidatorImpl validatorWithDefaultKey;
    private JwtValidatorImpl validatorWithCustomKey;
    private SecretKey customSigningKey;

    @BeforeEach
    void setUp() {
        validatorWithDefaultKey = new JwtValidatorImpl("");
        validatorWithCustomKey = new JwtValidatorImpl(CUSTOM_SECRET);
        customSigningKey = Keys.hmacShaKeyFor(CUSTOM_SECRET.getBytes(StandardCharsets.UTF_8));
    }

    @Nested
    @DisplayName("Happy Path Tests")
    class HappyPathTests {

        @Test
        @DisplayName("Valid token with default key should return claims")
        void validateToken_ValidTokenWithDefaultKey_ReturnsClaims() {
            // Given
            String token = createTestToken("123", "admin", DEFAULT_SECRET);

            // When
            Claims result = validatorWithDefaultKey.validateToken(token);

            // Then
            assertNotNull(result);
            assertEquals("123", result.getSubject());
            assertEquals("admin", result.get("role", String.class));
        }

        @Test
        @DisplayName("Valid token with custom key should return claims")
        void validateToken_ValidTokenWithCustomKey_ReturnsClaims() {
            // Given
            String token = createTestToken("456", "user", CUSTOM_SECRET);

            // When
            Claims result = validatorWithCustomKey.validateToken(token);

            // Then
            assertNotNull(result);
            assertEquals("456", result.getSubject());
            assertEquals("user", result.get("role", String.class));
        }

        @Test
        @DisplayName("Valid token with multiple claims should return all claims")
        void validateToken_ValidTokenWithMultipleClaims_ReturnsAllClaims() {
            // Given
            String token = Jwts.builder()
                    .setSubject("789")
                    .claim("role", "instructor")
                    .claim("email", "test@example.com")
                    .claim("name", "Test User")
                    .signWith(Keys.hmacShaKeyFor(DEFAULT_SECRET.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                    .compact();

            // When
            Claims result = validatorWithDefaultKey.validateToken(token);

            // Then
            assertNotNull(result);
            assertEquals("789", result.getSubject());
            assertEquals("instructor", result.get("role", String.class));
            assertEquals("test@example.com", result.get("email", String.class));
            assertEquals("Test User", result.get("name", String.class));
        }

        @Test
        @DisplayName("Valid token with null secret should use default key")
        void validateToken_NullSecret_UsesDefaultKey() {
            // Given
            JwtValidatorImpl validator = new JwtValidatorImpl(null);
            String token = createTestToken("999", "student", DEFAULT_SECRET);

            // When
            Claims result = validator.validateToken(token);

            // Then
            assertNotNull(result);
            assertEquals("999", result.getSubject());
        }
    }

    @Nested
    @DisplayName("Error Cases Tests")
    class ErrorCasesTests {

        @Test
        @DisplayName("Token signed with wrong key should throw InvalidTokenException")
        void validateToken_WrongSigningKey_ThrowsInvalidTokenException() {
            // Given
            String token = createTestToken("123", "admin", CUSTOM_SECRET);

            // When & Then
            InvalidTokenException exception = assertThrows(
                    InvalidTokenException.class,
                    () -> validatorWithDefaultKey.validateToken(token)
            );

            assertTrue(exception.getMessage().contains("Invalid or expired JWT token"));
        }

        @Test
        @DisplayName("Malformed token should throw InvalidTokenException")
        void validateToken_MalformedToken_ThrowsInvalidTokenException() {
            // Given
            String malformedToken = "not.a.valid.jwt.token";

            // When & Then
            InvalidTokenException exception = assertThrows(
                    InvalidTokenException.class,
                    () -> validatorWithDefaultKey.validateToken(malformedToken)
            );

            assertTrue(exception.getMessage().contains("Invalid or expired JWT token"));
        }

        @Test
        @DisplayName("Expired token should throw InvalidTokenException")
        void validateToken_ExpiredToken_ThrowsInvalidTokenException() {
            // Given
            Date expiration = new Date(System.currentTimeMillis() - 1000); // 1 second ago
            String token = Jwts.builder()
                    .setSubject("123")
                    .setExpiration(expiration)
                    .signWith(Keys.hmacShaKeyFor(DEFAULT_SECRET.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                    .compact();

            // When & Then
            InvalidTokenException exception = assertThrows(
                    InvalidTokenException.class,
                    () -> validatorWithDefaultKey.validateToken(token)
            );

            assertTrue(exception.getMessage().contains("Invalid or expired JWT token"));
        }

        @Test
        @DisplayName("Token without signature should throw InvalidTokenException")
        void validateToken_NoSignature_ThrowsInvalidTokenException() {
            // Given - unsigned token
            String token = Jwts.builder()
                    .setSubject("123")
                    .claim("role", "admin")
                    .compact();

            // When & Then
            InvalidTokenException exception = assertThrows(
                    InvalidTokenException.class,
                    () -> validatorWithDefaultKey.validateToken(token)
            );

            assertTrue(exception.getMessage().contains("Invalid or expired JWT token"));
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Null token should throw InvalidTokenException")
        void validateToken_NullToken_ThrowsInvalidTokenException() {
            // When & Then
            InvalidTokenException exception = assertThrows(
                    InvalidTokenException.class,
                    () -> validatorWithDefaultKey.validateToken(null)
            );

            assertTrue(exception.getMessage().contains("Invalid or expired JWT token"));
        }

        @Test
        @DisplayName("Empty token should throw InvalidTokenException")
        void validateToken_EmptyToken_ThrowsInvalidTokenException() {
            // Given
            String emptyToken = "";

            // When & Then
            InvalidTokenException exception = assertThrows(
                    InvalidTokenException.class,
                    () -> validatorWithDefaultKey.validateToken(emptyToken)
            );

            assertTrue(exception.getMessage().contains("Invalid or expired JWT token"));
        }

        @Test
        @DisplayName("Blank token should throw InvalidTokenException")
        void validateToken_BlankToken_ThrowsInvalidTokenException() {
            // Given
            String blankToken = "   ";

            // When & Then
            InvalidTokenException exception = assertThrows(
                    InvalidTokenException.class,
                    () -> validatorWithDefaultKey.validateToken(blankToken)
            );

            assertTrue(exception.getMessage().contains("Invalid or expired JWT token"));
        }

        @Test
        @DisplayName("Token with only header should throw InvalidTokenException")
        void validateToken_OnlyHeader_ThrowsInvalidTokenException() {
            // Given
            String partialToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";

            // When & Then
            InvalidTokenException exception = assertThrows(
                    InvalidTokenException.class,
                    () -> validatorWithDefaultKey.validateToken(partialToken)
            );

            assertTrue(exception.getMessage().contains("Invalid or expired JWT token"));
        }

        @Test
        @DisplayName("Token without subject should still return claims")
        void validateToken_NoSubject_ReturnsClaimsWithNullSubject() {
            // Given
            String token = Jwts.builder()
                    .claim("role", "admin")
                    .signWith(Keys.hmacShaKeyFor(DEFAULT_SECRET.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                    .compact();

            // When
            Claims result = validatorWithDefaultKey.validateToken(token);

            // Then
            assertNotNull(result);
            assertNull(result.getSubject());
            assertEquals("admin", result.get("role", String.class));
        }

        @Test
        @DisplayName("Token with empty subject should return empty string subject")
        void validateToken_EmptySubject_ReturnsEmptySubject() {
            // Given
            String token = Jwts.builder()
                    .setSubject("")
                    .claim("role", "user")
                    .signWith(Keys.hmacShaKeyFor(DEFAULT_SECRET.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                    .compact();

            // When
            Claims result = validatorWithDefaultKey.validateToken(token);

            // Then
            assertNotNull(result);
            // JJWT may treat empty subject as null
            assertTrue(result.getSubject() == null || "".equals(result.getSubject()));
        }
    }

    /**
     * Helper method to create a test JWT token
     */
    private String createTestToken(String userId, String role, String secret) {
        return Jwts.builder()
                .setSubject(userId)
                .claim("role", role)
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }
}
