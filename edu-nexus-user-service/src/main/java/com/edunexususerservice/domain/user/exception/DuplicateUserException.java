package com.edunexususerservice.domain.user.exception;

/**
 * Exception thrown when attempting to create a user with an email that already exists.
 *
 * <p>This exception indicates a violation of the unique email constraint on user accounts.
 * It is typically thrown during user registration when the provided email address is
 * already associated with an existing account.</p>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * try {
 *     userService.signUp(name, email, password);
 * } catch (DuplicateUserException e) {
 *     return ResponseEntity.status(HttpStatus.CONFLICT)
 *         .body("User with this email already exists");
 * }
 * }</pre>
 *
 * @see UserService#signUp(String, String, String)
 */
public class DuplicateUserException extends RuntimeException {

    /**
     * Constructs a new DuplicateUserException with no detail message.
     */
    public DuplicateUserException() {
    }

    /**
     * Constructs a new DuplicateUserException with the specified detail message.
     *
     * @param message the detail message (e.g., "User already exists: user@example.com")
     */
    public DuplicateUserException(String message) {
        super(message);
    }

    /**
     * Constructs a new DuplicateUserException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the underlying cause of the exception
     */
    public DuplicateUserException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new DuplicateUserException with the specified cause.
     *
     * @param cause the underlying cause of the exception
     */
    public DuplicateUserException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new DuplicateUserException with the specified detail message, cause,
     * suppression enabled or disabled, and writable stack trace enabled or disabled.
     *
     * @param message the detail message
     * @param cause the cause
     * @param enableSuppression whether suppression is enabled
     * @param writableStackTrace whether the stack trace should be writable
     */
    public DuplicateUserException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
