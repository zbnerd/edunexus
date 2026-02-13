package com.edunexuscourseservice.domain.course.exception;

/**
 * Exception thrown for validation errors
 */
public class ValidationException extends BaseException {

    public ValidationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ValidationException(ErrorCode errorCode, String details) {
        super(errorCode, details);
    }

    public ValidationException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public ValidationException(ErrorCode errorCode, String details, Throwable cause) {
        super(errorCode, details, cause);
    }
}
