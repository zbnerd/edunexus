package com.edunexusenrollmentservice.application.saga.step;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Context object that holds data shared across saga steps.
 * Carries state through the saga execution and compensation.
 */
@Data
@Builder
public class SagaContext {
    private String sagaId;
    private Long userId;
    private Long courseId;
    private Long paymentId;
    private Long enrollmentId;

    @Builder.Default
    private Map<String, Object> data = new HashMap<>();

    /**
     * Store data for use by subsequent steps.
     *
     * @param key The data key
     * @param value The data value
     */
    public void put(String key, Object value) {
        data.put(key, value);
    }

    /**
     * Retrieve data stored by previous steps.
     *
     * @param key The data key
     * @param <T> The expected type
     * @return Optional containing the value if present
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key) {
        return Optional.ofNullable((T) data.get(key));
    }

    /**
     * Retrieve data with a default value if not present.
     *
     * @param key The data key
     * @param defaultValue The default value
     * @param <T> The expected type
     * @return The value or default
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(String key, T defaultValue) {
        return (T) data.getOrDefault(key, defaultValue);
    }
}
