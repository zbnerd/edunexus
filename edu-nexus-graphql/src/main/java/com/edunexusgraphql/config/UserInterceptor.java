package com.edunexusgraphql.config;

import com.edunexusgraphql.security.InvalidTokenException;
import com.edunexusgraphql.security.JwtValidator;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

/**
 * Interceptor that validates JWT tokens and extracts user information.
 * Replaces insecure header-based authentication with proper JWT validation.
 */
@Configuration
@Slf4j
public class UserInterceptor implements WebGraphQlInterceptor {

    private final JwtValidator jwtValidator;

    public UserInterceptor(@Lazy JwtValidator jwtValidator) {
        this.jwtValidator = jwtValidator;
    }

    @Override
    public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {
        log.info("GraphQL Request: {}", request.getDocument());

        String authorizationHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        try {
            // Extract and validate JWT token
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7);
                Claims claims = jwtValidator.validateToken(token);

                // Extract user information from JWT claims
                final String userId;
                final String userRole;

                String subject = claims.getSubject();
                String role = claims.get("role", String.class);

                userId = (subject == null || subject.isBlank()) ? "-1" : subject;
                userRole = (role == null || role.isBlank()) ? "user" : role;

                log.info("Authenticated user: {}, role: {}", userId, userRole);

                // Set validated user information in GraphQL context
                request.configureExecutionInput((executionInput, executionInputBuilder) -> {
                    executionInput.getGraphQLContext().put("X-USER-ID", userId);
                    executionInput.getGraphQLContext().put("X-USER-ROLE", userRole);
                    return executionInput;
                });

                return chain.next(request);
            } else {
                log.warn("Missing or invalid Authorization header");
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
            }
        } catch (InvalidTokenException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }
    }
}
