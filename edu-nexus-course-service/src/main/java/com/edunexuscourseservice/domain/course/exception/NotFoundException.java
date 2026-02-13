package com.edunexuscourseservice.domain.course.exception;

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

    private final ErrorCode errorCode;

    public NotFoundException() {
        super(ErrorCode.COURSE_NOT_FOUND.getMessage());
        this.errorCode = ErrorCode.COURSE_NOT_FOUND;
    }

    public NotFoundException(String message) {
        super(message);
        this.errorCode = ErrorCode.COURSE_NOT_FOUND;
    }

    public NotFoundException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.COURSE_NOT_FOUND;
    }

    public NotFoundException(ErrorCode errorCode, String details) {
        super(errorCode.getMessage() + (details != null ? ": " + details : ""));
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
