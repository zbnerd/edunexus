package com.edunexus.common.exception;

/**
 * Exception thrown when a system error occurs.
 * Replaces service-specific SystemException classes.
 */
public class SystemException extends BaseException {

    public SystemException(ErrorCode errorCode) {
        super(errorCode);
    }

    public SystemException(ErrorCode errorCode, String details) {
        super(errorCode, details);
    }

    public SystemException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public SystemException(String details) {
        super(ErrorCode.INTERNAL_ERROR, details);
    }

    public SystemException(String details, Throwable cause) {
        super(ErrorCode.INTERNAL_ERROR, details, cause);
    }

    public SystemException() {
        super(ErrorCode.INTERNAL_ERROR);
    }
}
