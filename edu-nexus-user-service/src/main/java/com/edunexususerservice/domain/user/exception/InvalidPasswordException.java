package com.edunexususerservice.domain.user.exception;

/**
 * Exception thrown when password validation fails.
 *
 * <p>This exception is thrown in the following scenarios:</p>
 * <ul>
 *   <li>Old password doesn't match during password update</li>
 *   <li>New password doesn't meet complexity requirements</li>
 *   <li>Password confirmation doesn't match</li>
 * </ul>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * try {
 *     userService.updatePassword(userId, passwordChangeDto);
 * } catch (InvalidPasswordException e) {
 *     model.addAttribute("error", "Current password is incorrect");
 *     return "change-password";
 * }
 * }</pre>
 *
 * @see UserService#updatePassword(Long, PasswordChangeDto)
 */
public class InvalidPasswordException extends RuntimeException {

    /**
     * Constructs a new InvalidPasswordException with no detail message.
     */
    public InvalidPasswordException() {
    }

    /**
     * Constructs a new InvalidPasswordException with the specified detail message.
     *
     * @param message the detail message explaining why the password is invalid
     */
    public InvalidPasswordException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidPasswordException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the underlying cause of the exception
     */
    public InvalidPasswordException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new InvalidPasswordException with the specified cause.
     *
     * @param cause the underlying cause of the exception
     */
    public InvalidPasswordException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new InvalidPasswordException with the specified detail message, cause,
     * suppression enabled or disabled, and writable stack trace enabled or disabled.
     *
     * @param message the detail message
     * @param cause the cause
     * @param enableSuppression whether suppression is enabled
     * @param writableStackTrace whether the stack trace should be writable
     */
    public InvalidPasswordException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
