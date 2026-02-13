package com.edunexus.common.exception;

/**
 * Exception thrown when a requested resource is not found.
 * Replaces service-specific NotFoundException classes.
 */
public class NotFoundException extends BaseException {

    public NotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public NotFoundException(ErrorCode errorCode, String details) {
        super(errorCode, details);
    }

    public NotFoundException(String details) {
        super(ErrorCode.ENTITY_NOT_FOUND, details);
    }

    public NotFoundException() {
        super(ErrorCode.ENTITY_NOT_FOUND);
    }
}
