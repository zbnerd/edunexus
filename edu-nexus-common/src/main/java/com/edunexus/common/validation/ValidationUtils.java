package com.edunexus.common.validation;

import com.edunexus.common.exception.ValidationException;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Common validation utilities for all services.
 * Provides consistent validation logic and error messages.
 */
public final class ValidationUtils {

    private ValidationUtils() {
        // Utility class
    }

    /**
     * Validates that the specified object is not null.
     *
     * @param value   the value to validate
     * @param fieldName the field name for error message
     * @throws ValidationException if value is null
     */
    public static void requireNonNull(Object value, String fieldName) {
        if (value == null) {
            throw new ValidationException(fieldName + " cannot be null");
        }
    }

    /**
     * Validates that the specified string is not blank.
     *
     * @param value   the value to validate
     * @param fieldName the field name for error message
     * @throws ValidationException if value is null or blank
     */
    public static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " cannot be blank");
        }
    }

    /**
     * Validates that the specified number is positive.
     *
     * @param value   the value to validate
     * @param fieldName the field name for error message
     * @throws ValidationException if value is not positive
     */
    public static void requirePositive(Number value, String fieldName) {
        if (value == null || value.longValue() <= 0) {
            throw new ValidationException(fieldName + " must be positive");
        }
    }

    /**
     * Validates that the specified number is non-negative.
     *
     * @param value   the value to validate
     * @param fieldName the field name for error message
     * @throws ValidationException if value is negative
     */
    public static void requireNonNegative(Number value, String fieldName) {
        if (value == null || value.longValue() < 0) {
            throw new ValidationException(fieldName + " must be non-negative");
        }
    }

    /**
     * Validates that the collection is not empty.
     *
     * @param collection the collection to validate
     * @param fieldName the field name for error message
     * @throws ValidationException if collection is null or empty
     */
    public static void requireNonEmpty(Collection<?> collection, String fieldName) {
        if (collection == null || collection.isEmpty()) {
            throw new ValidationException(fieldName + " cannot be empty");
        }
    }

    /**
     * Validates that the condition is true.
     *
     * @param condition the condition to validate
     * @param errorMessage the error message
     * @throws ValidationException if condition is false
     */
    public static void requireTrue(boolean condition, String errorMessage) {
        if (!condition) {
            throw new ValidationException(errorMessage);
        }
    }

    /**
     * Validates that the condition is false.
     *
     * @param condition the condition to validate
     * @param errorMessage the error message
     * @throws ValidationException if condition is true
     */
    public static void requireFalse(boolean condition, String errorMessage) {
        if (condition) {
            throw new ValidationException(errorMessage);
        }
    }

    /**
     * Validates that the value is within the specified range (inclusive).
     *
     * @param value the value to validate
     * @param min the minimum value (inclusive)
     * @param max the maximum value (inclusive)
     * @param fieldName the field name for error message
     * @throws ValidationException if value is outside range
     */
    public static void requireInRange(long value, long min, long max, String fieldName) {
        if (value < min || value > max) {
            throw new ValidationException(fieldName + " must be between " + min + " and " + max);
        }
    }

    /**
     * Validates that the value is within the specified range (inclusive).
     *
     * @param value the value to validate
     * @param min the minimum value (inclusive)
     * @param max the maximum value (inclusive)
     * @param fieldName the field name for error message
     * @throws ValidationException if value is outside range
     */
    public static void requireInRange(double value, double min, double max, String fieldName) {
        if (value < min || value > max) {
            throw new ValidationException(fieldName + " must be between " + min + " and " + max);
        }
    }

    /**
     * Validates email format using basic validation.
     *
     * @param email the email to validate
     * @throws ValidationException if email is invalid
     */
    public static void requireValidEmail(String email) {
        requireNonBlank(email, "Email");
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new ValidationException("Invalid email format");
        }
    }

    /**
     * Throws exception if the supplier returns null.
     *
     * @param supplier the supplier to get value from
     * @param errorMessage the error message
     * @param <T> the type of value
     * @return the value if not null
     * @throws ValidationException if value is null
     */
    public static <T> T requireNonNull(Supplier<T> supplier, String errorMessage) {
        T value = supplier.get();
        if (value == null) {
            throw new ValidationException(errorMessage);
        }
        return value;
    }

    /**
     * Validates that two objects are equal.
     *
     * @param actual the actual value
     * @param expected the expected value
     * @param fieldName the field name for error message
     * @throws ValidationException if values are not equal
     */
    public static void requireEquals(Object actual, Object expected, String fieldName) {
        if (!Objects.equals(actual, expected)) {
            throw new ValidationException(fieldName + " does not match expected value");
        }
    }
}
