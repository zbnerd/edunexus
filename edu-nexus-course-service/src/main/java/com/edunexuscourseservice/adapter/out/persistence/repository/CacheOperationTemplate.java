package com.edunexuscourseservice.adapter.out.persistence.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Template for cache operations with consistent error handling.
 * <p>
 * Wraps cache operations with try-catch blocks that log warnings
 * but don't throw exceptions, following the cache-aside pattern
 * where cache failures should not block business operations.
 */
@Slf4j
@Component
public class CacheOperationTemplate {

    /**
     * Execute a cache operation with error handling.
     * <p>
     * Catches exceptions, logs warnings, and continues without throwing.
     *
     * @param operation The cache operation to execute
     * @param errorMessage The error message format string
     * @param args Arguments for the error message
     */
    public void executeWithErrorHandling(CacheOperation operation, String errorMessage, Object... args) {
        try {
            operation.execute();
        } catch (Exception e) {
            log.warn(errorMessage, args);
        }
    }

    /**
     * Execute a cache operation with error handling and return a value.
     * <p>
     * Returns the default value on exception.
     *
     * @param operation The cache operation to execute
     * @param defaultValue The default value to return on error
     * @param errorMessage The error message format string
     * @param args Arguments for the error message
     * @param <T> The return type
     * @return The operation result or default value
     */
    public <T> T executeWithErrorHandling(CacheOperationWithResult<T> operation, T defaultValue,
                                          String errorMessage, Object... args) {
        try {
            return operation.execute();
        } catch (Exception e) {
            log.warn(errorMessage + " Error: {}", args.length > 0 ? new Object[]{args[0], e.getMessage()} : e.getMessage());
            return defaultValue;
        }
    }

    /**
     * Functional interface for cache operations without return value.
     */
    @FunctionalInterface
    public interface CacheOperation {
        void execute() throws Exception;
    }

    /**
     * Functional interface for cache operations with return value.
     */
    @FunctionalInterface
    public interface CacheOperationWithResult<T> {
        T execute() throws Exception;
    }
}
