package com.edunexusattendanceservice.exceptionhandler;

import com.edunexus.common.exception.ErrorCode;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Standard error response structure
 */
@Getter
@Builder
public class ErrorResponse {
    private String code;
    private String message;
    private LocalDateTime timestamp;
}
