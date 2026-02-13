package com.edunexuscourseservice.exceptionhandler;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Standardized Error Response DTO
 *
 * Provides consistent error response format across all services with:
 * - Timestamp for when the error occurred
 * - HTTP status code
 * - Error type (e.g., "Not Found", "Bad Request")
 * - User-friendly error message
 * - Request path for debugging
 * - Trace ID for distributed tracing
 * - Correlation ID for request tracking
 * - Optional details for additional context
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /**
     * ISO 8601 timestamp when the error occurred
     */
    private Instant timestamp;

    /**
     * HTTP status code (e.g., 404, 500)
     */
    private int status;

    /**
     * HTTP error reason phrase (e.g., "Not Found", "Internal Server Error")
     */
    private String error;

    /**
     * Application-specific error code (e.g., "CS_NOT_001")
     */
    private String code;

    /**
     * User-friendly error message
     */
    private String message;

    /**
     * Request path that caused the error
     */
    private String path;

    /**
     * Distributed tracing ID for tracking across services
     */
    private String traceId;

    /**
     * Correlation ID for grouping related requests
     */
    private String correlationId;

    /**
     * Additional error details (optional)
     */
    private Map<String, Object> details;
}
