package com.edunexus.common.validation;

import com.edunexus.common.exception.ValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Centralized bean validation utility using Jakarta Validation.
 * Provides consistent validation across all services.
 */
public final class BeanValidator {

    private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = VALIDATOR_FACTORY.getValidator();

    private BeanValidator() {
        // Utility class
    }

    /**
     * Validates an object and throws ValidationException if invalid.
     *
     * @param object the object to validate
     * @param <T> the type of object
     * @throws ValidationException if validation fails
     */
    public static <T> void validate(T object) {
        Set<ConstraintViolation<T>> violations = VALIDATOR.validate(object);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            throw new ValidationException("Validation failed: " + message);
        }
    }

    /**
     * Validates an object and returns violation messages.
     *
     * @param object the object to validate
     * @param <T> the type of object
     * @return set of violation messages, empty if valid
     */
    public static <T> Set<String> getViolations(T object) {
        return VALIDATOR.validate(object).stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
    }

    /**
     * Checks if an object is valid without throwing exception.
     *
     * @param object the object to validate
     * @param <T> the type of object
     * @return true if valid, false otherwise
     */
    public static <T> boolean isValid(T object) {
        return VALIDATOR.validate(object).isEmpty();
    }
}
