package com.edunexuscourseservice.domain.course.exception;

/**
 * Exception thrown for system-level errors (internal, external service, cache, etc.)
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

    public SystemException(ErrorCode errorCode, String details, Throwable cause) {
        super(errorCode, details, cause);
    }
}
