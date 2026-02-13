package com.edunexusgateway.controller;

import com.edunexusgateway.model.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/users")
    public Mono<ErrorResponse> userFallback(ServerWebExchange exchange) {
        log.warn("User service fallback triggered for path: {}", exchange.getRequest().getPath());

        return Mono.just(buildFallbackResponse(
                exchange,
                "edu-nexus-user-service",
                "User authentication service is temporarily unavailable. Please try again later."
        ));
    }

    @GetMapping("/files")
    public Mono<ErrorResponse> fileFallback(ServerWebExchange exchange) {
        log.warn("File service fallback triggered for path: {}", exchange.getRequest().getPath());

        return Mono.just(buildFallbackResponse(
                exchange,
                "edu-nexus-file-manage-service",
                "File management service is temporarily unavailable. Please try again later."
        ));
    }

    @GetMapping("/graphql")
    public Mono<ErrorResponse> graphqlFallback(ServerWebExchange exchange) {
        log.warn("GraphQL service fallback triggered for path: {}", exchange.getRequest().getPath());

        return Mono.just(buildFallbackResponse(
                exchange,
                "edu-nexus-graphql",
                "GraphQL gateway service is temporarily unavailable. Please try again later."
        ));
    }

    private ErrorResponse buildFallbackResponse(ServerWebExchange exchange, String service, String message) {
        String correlationId = exchange.getRequest().getHeaders().getFirst("X-Correlation-ID");
        String traceId = exchange.getRequest().getId();

        Map<String, Object> details = new HashMap<>();
        details.put("service", service);
        details.put("type", "FALLBACK");
        details.put("reason", "Circuit breaker is open or service is unreachable");

        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .path(exchange.getRequest().getPath().value())
                .status(503)
                .error("Service Unavailable")
                .message(message)
                .traceId(traceId)
                .correlationId(correlationId)
                .details(details)
                .build();
    }
}
