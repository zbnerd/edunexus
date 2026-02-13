package com.edunexusgraphql.directive.permission;

import graphql.schema.DataFetchingEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Utility class for common permission validations.
 * <p>
 * Extracts repeated validation logic for:
 * - User ID comparison
 * - Role-based authorization
 * - Enrollment access checks
 */
public class PermissionValidator {

    /**
     * Validates that the header user ID matches the argument user ID.
     *
     * @param env The GraphQL data fetching environment
     * @param argumentKey The argument key for the user ID
     * @throws HttpClientErrorException if validation fails
     */
    public static void validateUserIdMatch(DataFetchingEnvironment env, String argumentKey) {
        Long headerUserId = getHeaderUserId(env);
        Long argumentUserId = getArgumentUserId(env, argumentKey);

        if (!headerUserId.equals(argumentUserId)) {
            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
    }

    /**
     * Validates that the user has the required role.
     *
     * @param env The GraphQL data fetching environment
     * @param requiredRole The required role
     * @throws HttpClientErrorException if validation fails
     */
    public static void validateRole(DataFetchingEnvironment env, String requiredRole) {
        String headerUserRole = getHeaderUserRole(env);

        if (!headerUserRole.equals(requiredRole)) {
            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
    }

    /**
     * Validates enrollment access for a user to a course.
     *
     * @param env The GraphQL data fetching environment
     * @param enrollmentChecker The enrollment checker service
     * @param courseIdKey The argument key for the course ID
     * @throws HttpClientErrorException if validation fails
     */
    public static void validateEnrollmentAccess(DataFetchingEnvironment env,
                                                  EnrollmentChecker enrollmentChecker,
                                                  String courseIdKey) {
        long userId = getHeaderUserId(env);
        long courseId = getArgumentCourseId(env, courseIdKey);

        if (!enrollmentChecker.checkCourseAccess(courseId, userId)) {
            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
    }

    /**
     * Validates enrollment access using source object (for nested fields).
     *
     * @param env The GraphQL data fetching environment
     * @param enrollmentChecker The enrollment checker service
     * @param sourceCourseIdGetter Function to extract course ID from source
     * @throws HttpClientErrorException if validation fails
     */
    public static void validateEnrollmentAccessFromSource(DataFetchingEnvironment env,
                                                           EnrollmentChecker enrollmentChecker,
                                                           SourceCourseIdGetter sourceCourseIdGetter) {
        long userId = getHeaderUserId(env);
        long courseId = sourceCourseIdGetter.getCourseId(env.getSource());

        if (!enrollmentChecker.checkCourseAccess(courseId, userId)) {
            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
    }

    private static Long getHeaderUserId(DataFetchingEnvironment env) {
        return Long.valueOf(env.getGraphQlContext().get("X-USER-ID"));
    }

    private static Long getArgumentUserId(DataFetchingEnvironment env, String key) {
        return Long.valueOf(env.getArgument(key));
    }

    private static long getArgumentCourseId(DataFetchingEnvironment env, String key) {
        return Long.parseLong(env.getArgument(key));
    }

    private static String getHeaderUserRole(DataFetchingEnvironment env) {
        return env.getGraphQlContext().get("X-USER-ROLE");
    }

    /**
     * Functional interface for extracting course ID from source object.
     */
    @FunctionalInterface
    public interface SourceCourseIdGetter {
        long getCourseId(Object source);
    }

    /**
     * Functional interface for enrollment checking.
     */
    @FunctionalInterface
    public interface EnrollmentChecker {
        boolean checkCourseAccess(long courseId, long userId);
    }
}
