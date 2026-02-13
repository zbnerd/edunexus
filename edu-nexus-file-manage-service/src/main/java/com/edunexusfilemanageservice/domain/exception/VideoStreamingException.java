package com.edunexusfilemanageservice.domain.exception;

/**
 * Exception thrown when video streaming operations fail.
 */
public class VideoStreamingException extends RuntimeException {

    public VideoStreamingException(String message) {
        super(message);
    }

    public VideoStreamingException(String message, Throwable cause) {
        super(message, cause);
    }
}
