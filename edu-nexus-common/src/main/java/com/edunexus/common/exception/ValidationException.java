package com.edunexus.common.exception;

/**
 * Exception thrown when validation fails.
 * Replaces service-specific ValidationException classes.
 */
public class ValidationException extends BaseException {

    public ValidationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ValidationException(ErrorCode errorCode, String details) {
        super(errorCode, details);
    }

    public ValidationException(String details) {
        super(ErrorCode.VALIDATION_ERROR, details);
    }

    public ValidationException() {
        super(ErrorCode.VALIDATION_ERROR);
    }
}
