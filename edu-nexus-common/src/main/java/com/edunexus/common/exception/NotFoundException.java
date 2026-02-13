package com.edunexus.common.exception;

/**
 * Exception thrown when a requested resource cannot be found.
 *
 * <p>This exception replaces service-specific NotFoundException classes across the codebase.
 * It is thrown when an operation requires a resource that doesn't exist.</p>
 *
 * <p>Common scenarios:</p>
 * <ul>
 *   <li>Entity ID not found in database</li>
 *   <li>Lookup by unique identifier returns no results</li>
 *   <li>Related entity (parent/child) doesn't exist</li>
 * </ul>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * Course course = courseRepository.findById(courseId)
 *     .orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
 * }</pre>
 *
 * <p>This exception typically results in HTTP 404 Not Found when propagated to REST controllers.</p>
 *
 * @see ErrorCode#ENTITY_NOT_FOUND
 */
public class NotFoundException extends BaseException {

    /**
     * Constructs a new NotFoundException with the specified error code.
     *
     * @param errorCode the error code describing the type of not found error
     */
    public NotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * Constructs a new NotFoundException with the specified error code and details.
     *
     * @param errorCode the error code describing the type of not found error
     * @param details additional details about what was not found
     */
    public NotFoundException(ErrorCode errorCode, String details) {
        super(errorCode, details);
    }

    /**
     * Constructs a new NotFoundException with a details message.
     * Uses {@link ErrorCode#ENTITY_NOT_FOUND} as the default error code.
     *
     * @param details additional details about what was not found
     */
    public NotFoundException(String details) {
        super(ErrorCode.ENTITY_NOT_FOUND, details);
    }

    /**
     * Constructs a new NotFoundException with default error code and no details.
     * Uses {@link ErrorCode#ENTITY_NOT_FOUND} as the error code.
     */
    public NotFoundException() {
        super(ErrorCode.ENTITY_NOT_FOUND);
    }
}
