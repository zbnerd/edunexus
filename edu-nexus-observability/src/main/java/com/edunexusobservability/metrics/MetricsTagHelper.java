package com.edunexusobservability.metrics;

import io.micrometer.core.instrument.Tag;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Helper utilities for creating Micrometer tags.
 *
 * Provides convenient methods for:
 * - HTTP request tags (method, status, URI)
 * - Business domain tags (user, course, enrollment)
 * - Conditional tag builders
 *
 * Usage:
 * <pre>
 * {@code
 * List<Tag> tags = MetricsTagHelper.builder()
 *     .add("userId", userId)
 *     .add("courseId", courseId)
 *     .addFromRequest()
 *     .build();
 * }
 * </pre>
 */
public class MetricsTagHelper {

    /**
     * Builder for creating tag lists with convenience methods.
     */
    public static class TagBuilder {
        private final List<Tag> tags = new ArrayList<>();

        /**
         * Add a tag if both key and value are non-null.
         */
        public TagBuilder add(String key, String value) {
            if (key != null && value != null) {
                tags.add(Tag.of(key, value));
            }
            return this;
        }

        /**
         * Add a tag if the value is non-null (empty string used for null).
         */
        public TagBuilder addOrEmpty(String key, String value) {
            tags.add(Tag.of(key, value != null ? value : ""));
            return this;
        }

        /**
         * Add a numeric tag.
         */
        public TagBuilder add(String key, long value) {
            tags.add(Tag.of(key, String.valueOf(value)));
            return this;
        }

        /**
         * Add a numeric tag.
         */
        public TagBuilder add(String key, double value) {
            tags.add(Tag.of(key, String.valueOf(value)));
            return this;
        }

        /**
         * Add a tag if condition is true.
         */
        public TagBuilder addIf(String key, String value, boolean condition) {
            if (condition && key != null && value != null) {
                tags.add(Tag.of(key, value));
            }
            return this;
        }

        /**
         * Add a tag from a supplier (lazy evaluation).
         */
        public TagBuilder add(String key, Supplier<String> valueSupplier) {
            if (key != null && valueSupplier != null) {
                String value = valueSupplier.get();
                if (value != null) {
                    tags.add(Tag.of(key, value));
                }
            }
            return this;
        }

        /**
         * Add HTTP request method tag.
         */
        public TagBuilder addRequestMethod() {
            HttpServletRequest request = getCurrentRequest();
            if (request != null) {
                tags.add(Tag.of("method", request.getMethod()));
            }
            return this;
        }

        /**
         * Add HTTP request URI tag (sanitized).
         */
        public TagBuilder addRequestUri() {
            HttpServletRequest request = getCurrentRequest();
            if (request != null) {
                String uri = sanitizeUri(request.getRequestURI());
                tags.add(Tag.of("uri", uri));
            }
            return this;
        }

        /**
         * Add HTTP status tag.
         */
        public TagBuilder addResponseStatus(int status) {
            tags.add(Tag.of("status", String.valueOf(status)));
            return this;
        }

        /**
         * Add outcome tag based on HTTP status.
         */
        public TagBuilder addOutcome(int status) {
            String outcome = determineOutcome(status);
            tags.add(Tag.of("outcome", outcome));
            return this;
        }

        /**
         * Add exception tag.
         */
        public TagBuilder addException(Exception exception) {
            if (exception != null) {
                tags.add(Tag.of("exception", exception.getClass().getSimpleName()));
            }
            return this;
        }

        /**
         * Add all tags from current HTTP request.
         */
        public TagBuilder addFromRequest() {
            return addRequestMethod().addRequestUri();
        }

        /**
         * Build the tag list.
         */
        public List<Tag> build() {
            return new ArrayList<>(tags);
        }

        /**
         * Build as array.
         */
        public String[] buildAsArray() {
            return tags.stream()
                    .flatMap(tag -> java.util.stream.Stream.of(tag.getKey(), tag.getValue()))
                    .toArray(String[]::new);
        }
    }

    /**
     * Create a new tag builder.
     */
    public static TagBuilder builder() {
        return new TagBuilder();
    }

    /**
     * Create tags from key-value pairs.
     */
    public static List<Tag> of(String... keyValuePairs) {
        if (keyValuePairs.length % 2 != 0) {
            throw new IllegalArgumentException("Key-value pairs must be even length");
        }

        List<Tag> tags = new ArrayList<>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            String key = keyValuePairs[i];
            String value = i + 1 < keyValuePairs.length ? keyValuePairs[i + 1] : "";
            if (key != null && value != null) {
                tags.add(Tag.of(key, value));
            }
        }
        return tags;
    }

    /**
     * Sanitize URI for metric tags by replacing variable segments.
     *
     * Converts: /courses/123/sessions/456 -> /courses/:id/sessions/:id
     */
    public static String sanitizeUri(String uri) {
        if (uri == null || uri.isEmpty()) {
            return "unknown";
        }

        // Replace numeric IDs
        String sanitized = uri.replaceAll("/\\d+(?=/|$)", "/:id");

        // Replace UUIDs
        sanitized = sanitized.replaceAll(
            "/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}",
            "/:uuid"
        );

        return sanitized;
    }

    /**
     * Determine outcome from HTTP status code.
     *
     * Returns: SUCCESS, CLIENT_ERROR, SERVER_ERROR, or REDIRECTION
     */
    public static String determineOutcome(int status) {
        if (status >= 200 && status < 300) {
            return "SUCCESS";
        } else if (status >= 300 && status < 400) {
            return "REDIRECTION";
        } else if (status >= 400 && status < 500) {
            return "CLIENT_ERROR";
        } else if (status >= 500) {
            return "SERVER_ERROR";
        }
        return "UNKNOWN";
    }

    /**
     * Get current HTTP request if available.
     */
    private static HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Common tag keys used across all metrics.
     */
    public static class CommonTags {
        public static final String APPLICATION = "application";
        public static final String ENVIRONMENT = "environment";
        public static final String REGION = "region";
        public static final String METHOD = "method";
        public static final String URI = "uri";
        public static final String STATUS = "status";
        public static final String OUTCOME = "outcome";
        public static final String EXCEPTION = "exception";
        public static final String USER_ID = "userId";
        public static final String COURSE_ID = "courseId";
        public static final String ENROLLMENT_ID = "enrollmentId";
    }
}
