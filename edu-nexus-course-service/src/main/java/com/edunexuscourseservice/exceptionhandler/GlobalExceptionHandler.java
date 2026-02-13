package com.edunexuscourseservice.exceptionhandler;

import com.edunexuscourseservice.domain.course.exception.BaseException;
import com.edunexuscourseservice.domain.course.exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler for Course Service
 *
 * Provides centralized exception handling with:
 * - Standardized error response format
 * - Proper logging with MDC context
 * - Appropriate HTTP status codes
 * - Error code mapping
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handle BaseException and all its subclasses
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(
            BaseException ex,
            HttpServletRequest request,
            WebRequest webRequest
    ) {
        log.error(
                "Business exception occurred: code={}, message={}, path={}",
                ex.getErrorCode().getCode(),
                ex.getMessage(),
                request.getRequestURI(),
                ex.getCause()
        );

        Map<String, Object> detailMap = null;
        if (ex.getDetails() != null) {
            detailMap = Map.of("info", ex.getDetails());
        }

        ErrorResponse errorResponse = buildErrorResponse(
                ex.getErrorCode().getHttpStatus(),
                ex.getErrorCode().getCode(),
                ex.getMessage(),
                request.getRequestURI(),
                detailMap
        );

        return ResponseEntity
                .status(ex.getErrorCode().getHttpStatus())
                .body(errorResponse);
    }

    /**
     * Handle legacy NotFoundException (for backward compatibility)
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(
            NotFoundException ex,
            HttpServletRequest request
    ) {
        log.warn(
                "Resource not found: message={}, path={}",
                ex.getMessage(),
                request.getRequestURI()
        );

        ErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.NOT_FOUND,
                ex.getErrorCode() != null ? ex.getErrorCode().getCode() : "CS_NOT_000",
                ex.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    /**
     * Handle generic exceptions (catch-all)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error(
                "Unexpected error occurred: type={}, message={}, path={}",
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                request.getRequestURI(),
                ex
        );

        Map<String, Object> detailMap = null;
        if (isDevProfile()) {
            detailMap = Map.of("errorType", ex.getClass().getSimpleName());
        }

        ErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "CS_SYS_000",
                "An unexpected error occurred. Please try again later.",
                request.getRequestURI(),
                detailMap
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }

    /**
     * Handle IllegalArgumentException (validation errors)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        log.warn(
                "Validation error: message={}, path={}",
                ex.getMessage(),
                request.getRequestURI()
        );

        ErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "CS_VAL_000",
                ex.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * Build standardized error response
     */
    private ErrorResponse buildErrorResponse(
            HttpStatus status,
            String code,
            String message,
            String path,
            Map<String, Object> details
    ) {
        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .code(code)
                .message(message)
                .path(path)
                .traceId(MDC.get("traceId"))
                .correlationId(MDC.get("correlationId"))
                .details(details)
                .build();
    }

    /**
     * Check if running in development profile
     */
    private boolean isDevProfile() {
        // Simplified version - returns true for dev/local profiles
        // In production, this would check active Spring profiles
        return false;
    }
}
