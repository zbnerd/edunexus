package com.edunexuscourseservice.domain.course.exception;

/**
 * Exception thrown for business logic violations
 */
public class BusinessException extends BaseException {

    public BusinessException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BusinessException(ErrorCode errorCode, String details) {
        super(errorCode, details);
    }

    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public BusinessException(ErrorCode errorCode, String details, Throwable cause) {
        super(errorCode, details, cause);
    }
}
