package com.edunexusfilemanageservice.domain.exception;

/**
 * Exception thrown when file storage operations fail.
 *
 * <p>This exception is thrown in the following scenarios:</p>
 * <ul>
 *   <li>File upload fails due to I/O errors</li>
 *   <li>File exceeds maximum size limit</li>
 *   <li>File type is not supported</li>
 *   <li>Storage location is unavailable or lacks permissions</li>
 * </ul>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * try {
 *     fileStorageService.store(file);
 * } catch (FileStorageException e) {
 *     log.error("Failed to store file: {}", e.getMessage());
 *     throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "File upload failed");
 * }
 * }</pre>
 */
public class FileStorageException extends RuntimeException {

    /**
     * Constructs a new FileStorageException with no detail message.
     */
    public FileStorageException() {
    }

    /**
     * Constructs a new FileStorageException with the specified detail message.
     *
     * @param message the detail message explaining the storage failure
     */
    public FileStorageException(String message) {
        super(message);
    }

    /**
     * Constructs a new FileStorageException with the specified detail message and cause.
     *
     * @param message the detail message explaining the storage failure
     * @param cause the underlying cause of the storage failure
     */
    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new FileStorageException with the specified cause.
     *
     * @param cause the underlying cause of the storage failure
     */
    public FileStorageException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new FileStorageException with the specified detail message, cause,
     * suppression enabled or disabled, and writable stack trace enabled or disabled.
     *
     * @param message the detail message
     * @param cause the cause
     * @param enableSuppression whether suppression is enabled
     * @param writableStackTrace whether the stack trace should be writable
     */
    public FileStorageException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
