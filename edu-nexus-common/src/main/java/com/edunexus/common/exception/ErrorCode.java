package com.edunexus.common.exception;

import lombok.Getter;

/**
 * Standardized error codes for all services.
 * Provides consistent error classification and messaging.
 */
@Getter
public enum ErrorCode {

    // Not Found Errors (4xx)
    ENTITY_NOT_FOUND("E001", "Requested entity not found"),
    USER_NOT_FOUND("E002", "User not found"),
    COURSE_NOT_FOUND("E003", "Course not found"),
    SESSION_NOT_FOUND("E004", "Session not found"),
    ENROLLMENT_NOT_FOUND("E005", "Enrollment not found"),
    SUBSCRIPTION_NOT_FOUND("E006", "Subscription not found"),
    FILE_NOT_FOUND("E007", "File not found"),

    // Validation Errors (4xx)
    VALIDATION_ERROR("V001", "Validation failed"),
    INVALID_INPUT("V002", "Invalid input provided"),
    INVALID_PASSWORD("V003", "Invalid password"),
    INVALID_TOKEN("V004", "Invalid token"),
    EXPIRED_TOKEN("V005", "Token has expired"),

    // Business Logic Errors (4xx)
    DUPLICATE_ENTITY("B001", "Entity already exists"),
    DUPLICATE_USER("B002", "User already exists"),
    ACCESS_DENIED("B003", "Access denied"),
    INSUFFICIENT_PERMISSIONS("B004", "Insufficient permissions"),
    OPERATION_NOT_ALLOWED("B005", "Operation not allowed"),

    // System Errors (5xx)
    INTERNAL_ERROR("S001", "Internal server error"),
    EXTERNAL_SERVICE_ERROR("S002", "External service error"),
    DATABASE_ERROR("S003", "Database operation failed"),
    CACHE_ERROR("S004", "Cache operation failed");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
