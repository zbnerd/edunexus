package com.edunexuscourseservice.domain.course.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base exception class for all domain exceptions
 *
 * Provides consistent error handling with:
 * - Standard error codes
 * - HTTP status mapping
 * - Detailed error context
 */
@Getter
public class BaseException extends RuntimeException {

    private final ErrorCode errorCode;
    private final HttpStatus httpStatus;
    private final String details;

    public BaseException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
        this.details = null;
    }

    public BaseException(ErrorCode errorCode, String details) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
        this.details = details;
    }

    public BaseException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
        this.details = null;
    }

    public BaseException(ErrorCode errorCode, String details, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
        this.details = details;
    }
}
