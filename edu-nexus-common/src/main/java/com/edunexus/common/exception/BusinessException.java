package com.edunexus.common.exception;

/**
 * Exception thrown when business logic validation fails.
 * Replaces service-specific BusinessException classes.
 */
public class BusinessException extends BaseException {

    public BusinessException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BusinessException(ErrorCode errorCode, String details) {
        super(errorCode, details);
    }

    public BusinessException(String details) {
        super(ErrorCode.OPERATION_NOT_ALLOWED, details);
    }

    public BusinessException() {
        super(ErrorCode.OPERATION_NOT_ALLOWED);
    }
}
