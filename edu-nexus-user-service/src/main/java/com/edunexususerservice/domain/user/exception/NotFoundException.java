package com.edunexususerservice.domain.user.exception;

/**
 * Exception thrown when a requested resource cannot be found.
 *
 * <p>This exception is thrown when an operation requires a resource that doesn't exist,
 * such as:</p>
 * <ul>
 *   <li>User ID not found in the database</li>
 *   <li>Email address doesn't match any user account</li>
 *   <li>Related entity (course, enrollment, etc.) not found</li>
 * </ul>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * User user = userRepository.findById(userId)
 *     .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
 * }</pre>
 *
 * <p>This exception typically results in an HTTP 404 Not Found response when
 * propagated to the API layer.</p>
 */
public class NotFoundException extends RuntimeException {

    /**
     * Constructs a new NotFoundException with no detail message.
     */
    public NotFoundException() {
    }

    /**
     * Constructs a new NotFoundException with the specified detail message.
     *
     * @param message the detail message identifying the missing resource
     *                (e.g., "User not found with id: 123")
     */
    public NotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new NotFoundException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the underlying cause of the exception
     */
    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new NotFoundException with the specified cause.
     *
     * @param cause the underlying cause of the exception
     */
    public NotFoundException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new NotFoundException with the specified detail message, cause,
     * suppression enabled or disabled, and writable stack trace enabled or disabled.
     *
     * @param message the detail message
     * @param cause the cause
     * @param enableSuppression whether suppression is enabled
     * @param writableStackTrace whether the stack trace should be writable
     */
    public NotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
