package com.edunexusgateway.exception;

import com.edunexusgateway.model.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Order(-1)
@Component
public class GlobalGatewayExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        response.setStatusCode(determineStatusCode(ex));
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ErrorResponse errorResponse = buildErrorResponse(exchange, ex);

        DataBuffer buffer = response.bufferFactory().wrap(serializeError(errorResponse));
        return response.writeWith(Mono.just(buffer));
    }

    private HttpStatus determineStatusCode(Throwable ex) {
        if (ex instanceof CallNotPermittedException) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        } else if (ex instanceof ResponseStatusException rse) {
            return (HttpStatus) rse.getStatusCode();
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private ErrorResponse buildErrorResponse(ServerWebExchange exchange, Throwable ex) {
        String correlationId = exchange.getRequest().getHeaders().getFirst("X-Correlation-ID");
        if (correlationId == null) {
            correlationId = exchange.getAttribute("correlationId") != null
                    ? exchange.getAttribute("correlationId").toString()
                    : generateCorrelationId();
        }

        String traceId = exchange.getRequest().getId();

        ErrorResponse.ErrorResponseBuilder builder = ErrorResponse.builder()
                .timestamp(Instant.now())
                .path(exchange.getRequest().getPath().value())
                .status(determineStatusCode(ex).value())
                .error(determineStatusCode(ex).getReasonPhrase())
                .message(getUserMessage(ex))
                .traceId(traceId)
                .correlationId(correlationId);

        if (ex instanceof CallNotPermittedException) {
            Map<String, Object> details = new HashMap<>();
            details.put("type", "CIRCUIT_BREAKER_OPEN");
            details.put("message", "Service temporarily unavailable. Please try again later.");
            builder.details(details);
        }

        return builder.build();
    }

    private String getUserMessage(Throwable ex) {
        if (ex instanceof CallNotPermittedException) {
            return "Service circuit breaker is open. The service is temporarily unavailable.";
        } else if (ex instanceof ResponseStatusException rse) {
            return rse.getReason();
        }
        return "An unexpected error occurred. Please try again later.";
    }

    private String generateCorrelationId() {
        return java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private byte[] serializeError(ErrorResponse errorResponse) {
        try {
            return objectMapper.writeValueAsBytes(errorResponse);
        } catch (Exception e) {
            log.error("Failed to serialize error response", e);
            return "{}".getBytes();
        }
    }
}
