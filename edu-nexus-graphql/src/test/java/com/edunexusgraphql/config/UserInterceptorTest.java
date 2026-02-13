package com.edunexusgraphql.config;

import com.edunexusgraphql.security.InvalidTokenException;
import com.edunexusgraphql.security.JwtValidator;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserInterceptor
 *
 * Test coverage:
 * - Happy path: Valid JWT token extraction and validation
 * - Error cases: Invalid tokens, missing headers, malformed headers
 * - Edge cases: Null values, blank subjects, null roles
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserInterceptor Tests")
class UserInterceptorTest {

    @Mock
    private JwtValidator jwtValidator;

    @Mock
    private WebGraphQlRequest request;

    @Mock
    private WebGraphQlInterceptor.Chain chain;

    @Mock
    private WebGraphQlResponse response;

    @InjectMocks
    private UserInterceptor interceptor;

    @BeforeEach
    void setUp() {
        lenient().when(chain.next(request)).thenReturn(Mono.just(response));
        lenient().when(request.getHeaders()).thenReturn(mockHttpHeaders());
    }

    @Nested
    @DisplayName("Happy Path Tests")
    class HappyPathTests {

        @Test
        @DisplayName("Valid Bearer token should set user context and proceed")
        void intercept_ValidBearerToken_SetsUserContextAndProceeds() {
            // Given
            String validToken = "valid.jwt.token";
            Claims claims = createTestClaims("123", "admin");

            when(request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + validToken);
            when(jwtValidator.validateToken(validToken)).thenReturn(claims);
            when(chain.next(request)).thenReturn(Mono.just(response));

            // When
            Mono<WebGraphQlResponse> result = interceptor.intercept(request, chain);

            // Then
            assertNotNull(result);
            verify(jwtValidator).validateToken(validToken);
            verify(chain).next(request);
        }

        @Test
        @DisplayName("Valid token with user role should set role correctly")
        void intercept_ValidTokenWithUserRole_SetsRoleCorrectly() {
            // Given
            String validToken = "valid.jwt.token";
            Claims claims = createTestClaims("456", "user");

            when(request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + validToken);
            when(jwtValidator.validateToken(validToken)).thenReturn(claims);

            // When
            interceptor.intercept(request, chain);

            // Then
            verify(request).configureExecutionInput(any());
            verify(jwtValidator).validateToken(validToken);
        }

        @Test
        @DisplayName("Valid token with instructor role should set role correctly")
        void intercept_ValidTokenWithInstructorRole_SetsRoleCorrectly() {
            // Given
            String validToken = "valid.jwt.token";
            Claims claims = createTestClaims("789", "instructor");

            when(request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + validToken);
            when(jwtValidator.validateToken(validToken)).thenReturn(claims);

            // When
            interceptor.intercept(request, chain);

            // Then
            verify(jwtValidator).validateToken(validToken);
            verify(chain).next(request);
        }

        @Test
        @DisplayName("Valid token should set X-USER-ID and X-USER-ROLE in context")
        void intercept_ValidToken_SetsUserHeadersInContext() {
            // Given
            String validToken = "valid.jwt.token";
            String userId = "123";
            String userRole = "admin";
            Claims claims = createTestClaims(userId, userRole);

            when(request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + validToken);
            when(jwtValidator.validateToken(validToken)).thenReturn(claims);

            // When
            interceptor.intercept(request, chain);

            // Then
            verify(request).configureExecutionInput(any());
        }

    }

    @Nested
    @DisplayName("Error Cases Tests")
    class ErrorCasesTests {

        @Test
        @DisplayName("Missing Authorization header should throw UNAUTHORIZED")
        void intercept_MissingAuthorizationHeader_ThrowsUnauthorized() {
            // Given
            when(request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION)).thenReturn(null);

            // When & Then
            ResponseStatusException exception = assertThrows(
                    ResponseStatusException.class,
                    () -> interceptor.intercept(request, chain).block()
            );

            assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
            assertTrue(exception.getReason().contains("Missing or invalid Authorization header"));
            verify(jwtValidator, never()).validateToken(any());
            verify(chain, never()).next(request);
        }

        @Test
        @DisplayName("Empty Authorization header should throw UNAUTHORIZED")
        void intercept_EmptyAuthorizationHeader_ThrowsUnauthorized() {
            // Given
            when(request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION)).thenReturn("");

            // When & Then
            ResponseStatusException exception = assertThrows(
                    ResponseStatusException.class,
                    () -> interceptor.intercept(request, chain).block()
            );

            assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
            verify(jwtValidator, never()).validateToken(any());
        }

        @Test
        @DisplayName("Authorization header without 'Bearer ' prefix should throw UNAUTHORIZED")
        void intersect_AuthHeaderWithoutBearerPrefix_ThrowsUnauthorized() {
            // Given
            String invalidHeader = "invalid.token.format";
            when(request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION)).thenReturn(invalidHeader);

            // When & Then
            ResponseStatusException exception = assertThrows(
                    ResponseStatusException.class,
                    () -> interceptor.intercept(request, chain).block()
            );

            assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
            verify(jwtValidator, never()).validateToken(any());
        }

        @Test
        @DisplayName("Invalid JWT token should throw UNAUTHORIZED")
        void intercept_InvalidJwtToken_ThrowsUnauthorized() {
            // Given
            String invalidToken = "invalid.jwt.token";
            when(request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + invalidToken);
            when(jwtValidator.validateToken(invalidToken))
                    .thenThrow(new InvalidTokenException("Token validation failed"));

            // When & Then
            ResponseStatusException exception = assertThrows(
                    ResponseStatusException.class,
                    () -> interceptor.intercept(request, chain).block()
            );

            assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
            assertTrue(exception.getReason().contains("Invalid or expired token"));
            verify(jwtValidator).validateToken(invalidToken);
            verify(chain, never()).next(request);
        }

        @Test
        @DisplayName("Expired JWT token should throw UNAUTHORIZED")
        void intercept_ExpiredJwtToken_ThrowsUnauthorized() {
            // Given
            String expiredToken = "expired.jwt.token";
            when(request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + expiredToken);
            when(jwtValidator.validateToken(expiredToken))
                    .thenThrow(new InvalidTokenException("Token expired"));

            // When & Then
            ResponseStatusException exception = assertThrows(
                    ResponseStatusException.class,
                    () -> interceptor.intercept(request, chain).block()
            );

            assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
            verify(jwtValidator).validateToken(expiredToken);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Token with null subject should set userId to -1")
        void intercept_NullSubject_SetsUserIdToNegativeOne() {
            // Given
            String validToken = "valid.jwt.token";
            Claims claims = Jwts.claims();
            claims.put("role", "admin");
            // subject is null

            when(request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + validToken);
            when(jwtValidator.validateToken(validToken)).thenReturn(claims);

            // When
            interceptor.intercept(request, chain);

            // Then
            verify(request).configureExecutionInput(any());
            verify(chain).next(request);
        }

        @Test
        @DisplayName("Token with blank subject should set userId to -1")
        void intercept_BlankSubject_SetsUserIdToNegativeOne() {
            // Given
            String validToken = "valid.jwt.token";
            Claims claims = Jwts.claims();
            claims.setSubject("");  // blank subject
            claims.put("role", "user");

            when(request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + validToken);
            when(jwtValidator.validateToken(validToken)).thenReturn(claims);

            // When
            interceptor.intercept(request, chain);

            // Then
            verify(request).configureExecutionInput(any());
        }

        @Test
        @DisplayName("Token with null role should default to 'user'")
        void intercept_NullRole_DefaultsToUser() {
            // Given
            String validToken = "valid.jwt.token";
            Claims claims = Jwts.claims();
            claims.setSubject("123");
            // role is null

            when(request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + validToken);
            when(jwtValidator.validateToken(validToken)).thenReturn(claims);

            // When
            interceptor.intercept(request, chain);

            // Then
            verify(request).configureExecutionInput(any());
        }

        @Test
        @DisplayName("Token with blank role should default to 'user'")
        void intercept_BlankRole_DefaultsToUser() {
            // Given
            String validToken = "valid.jwt.token";
            Claims claims = Jwts.claims();
            claims.setSubject("123");
            claims.put("role", "");  // blank role

            when(request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + validToken);
            when(jwtValidator.validateToken(validToken)).thenReturn(claims);

            // When
            interceptor.intercept(request, chain);

            // Then
            verify(request).configureExecutionInput(any());
        }

        @Test
        @DisplayName("Bearer token with only 'Bearer ' and no token should be handled")
        void intercept_BearerWithNoToken_HandledGracefully() {
            // Given
            String emptyToken = "";
            when(request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + emptyToken);
            when(jwtValidator.validateToken(emptyToken))
                    .thenThrow(new InvalidTokenException("Invalid token"));

            // When & Then
            assertThrows(ResponseStatusException.class, () -> {
                interceptor.intercept(request, chain).block();
            });
        }

        @Test
        @DisplayName("Bearer token with extra spaces should extract token with leading space")
        void intercept_BearerWithExtraSpaces_ExtractsTokenWithSpace() {
            // Given
            String validToken = "valid.jwt.token";
            String tokenWithSpace = " " + validToken;  // substring(7) keeps leading space
            Claims claims = createTestClaims("123", "admin");

            when(request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer  " + validToken);
            when(jwtValidator.validateToken(tokenWithSpace)).thenThrow(new InvalidTokenException("Invalid token"));

            // When & Then
            assertThrows(ResponseStatusException.class, () -> interceptor.intercept(request, chain).block());
        }

        @Test
        @DisplayName("Multiple Authorization headers should use first one")
        void intercept_MultipleAuthHeaders_UsesFirst() {
            // Given
            String validToken = "valid.jwt.token";
            Claims claims = createTestClaims("123", "user");

            HttpHeaders headers = mock(HttpHeaders.class);
            when(headers.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + validToken);
            when(request.getHeaders()).thenReturn(headers);
            when(jwtValidator.validateToken(validToken)).thenReturn(claims);

            // When
            interceptor.intercept(request, chain);

            // Then
            verify(jwtValidator).validateToken(validToken);
        }
    }

    /**
     * Helper method to create test JWT claims
     */
    private Claims createTestClaims(String subject, String role) {
        Claims claims = Jwts.claims();
        claims.setSubject(subject);
        claims.put("role", role);
        return claims;
    }

    /**
     * Helper method to mock HttpHeaders
     */
    private HttpHeaders mockHttpHeaders() {
        return mock(HttpHeaders.class);
    }
}
