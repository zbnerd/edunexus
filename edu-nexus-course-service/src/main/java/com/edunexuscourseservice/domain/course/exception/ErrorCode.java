package com.edunexuscourseservice.domain.course.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Standardized Error Codes for Course Service
 *
 * Error Code Format: SERVICE_TYPE_SPECIFIC_ID
 * - SERVICE: Service identifier (CS = Course Service)
 * - TYPE: Error type (VAL = Validation, NOT = Not Found, BUS = Business, SYS = System)
 * - ID: Unique error identifier
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Validation Errors (4xx)
    INVALID_INPUT("CS_VAL_001", "Invalid input provided", HttpStatus.BAD_REQUEST),
    INVALID_COURSE_ID("CS_VAL_002", "Invalid course ID format", HttpStatus.BAD_REQUEST),
    INVALID_RATING_VALUE("CS_VAL_003", "Rating must be between 1 and 5", HttpStatus.BAD_REQUEST),

    // Not Found Errors (404)
    COURSE_NOT_FOUND("CS_NOT_001", "Course not found", HttpStatus.NOT_FOUND),
    COURSE_SESSION_NOT_FOUND("CS_NOT_002", "Course session not found", HttpStatus.NOT_FOUND),
    COURSE_RATING_NOT_FOUND("CS_NOT_003", "Course rating not found", HttpStatus.NOT_FOUND),

    // Business Logic Errors (4xx)
    DUPLICATE_COURSE("CS_BUS_001", "Course already exists", HttpStatus.CONFLICT),
    COURSE_HAS_ENROLLMENTS("CS_BUS_002", "Cannot delete course with active enrollments", HttpStatus.CONFLICT),
    INVALID_COURSE_STATE("CS_BUS_003", "Invalid course state for this operation", HttpStatus.BAD_REQUEST),

    // System Errors (5xx)
    CACHE_OPERATION_FAILED("CS_SYS_001", "Cache operation failed", HttpStatus.INTERNAL_SERVER_ERROR),
    DATABASE_ERROR("CS_SYS_002", "Database operation failed", HttpStatus.INTERNAL_SERVER_ERROR),
    EXTERNAL_SERVICE_ERROR("CS_SYS_003", "External service call failed", HttpStatus.INTERNAL_SERVER_ERROR),
    KAFKA_ERROR("CS_SYS_004", "Message queue operation failed", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
