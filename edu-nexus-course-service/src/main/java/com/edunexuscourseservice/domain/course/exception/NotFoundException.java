package com.edunexus.common.exception;

/**
 * Exception thrown when a requested resource is not found
 *
 * Use BaseException with appropriate ErrorCode for new code.
 * This class maintains backward compatibility.
 *
 * @deprecated Use {@link BaseException} with {@link ErrorCode} instead
 */
@Deprecated
public class NotFoundException extends RuntimeException {

    public NotFoundException() {
        super(ErrorCode.COURSE_NOT_FOUND.getMessage());
    }

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
