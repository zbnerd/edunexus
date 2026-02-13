package com.edunexusgraphql.directive.permission;

import com.edunexusgraphql.directive.PermissionAction;
import com.edunexusgraphql.service.EnrollmentService;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.util.HashSet;
import java.util.Set;

/**
 * Centralized permission definitions for GraphQL authorization.
 * <p>
 * Extracts permission creation logic from RolePermissionService.
 * Uses factory methods to create PermissionAction objects with proper validation.
 */
public class PermissionDefinitions {

    /**
     * Create user permissions set.
     *
     * @param enrollmentService The enrollment service for access checks
     * @return Set of user permissions
     */
    public static Set<PermissionAction> createUserPermissions(EnrollmentService enrollmentService) {
        Set<PermissionAction> permissions = new HashSet<>();

        // User ID match validations
        permissions.add(new PermissionAction("read_user",
                env -> PermissionValidator.validateUserIdMatch(env, "userId")));
        permissions.add(new PermissionAction("update_user",
                env -> PermissionValidator.validateUserIdMatch(env, "userId")));
        permissions.add(new PermissionAction("read_enrollment",
                env -> PermissionValidator.validateUserIdMatch(env, "userId")));
        permissions.add(new PermissionAction("update_purchase",
                env -> PermissionValidator.validateUserIdMatch(env, "userId")));

        // Enrollment access validations
        permissions.add(new PermissionAction("read_purchase", env -> {
            PermissionValidator.validateUserIdMatch(env, "userId");
            validateEnrollmentAccess(env, enrollmentService);
        }));

        permissions.add(new PermissionAction("read_files", env ->
                PermissionValidator.validateEnrollmentAccessFromSource(
                        env,
                        enrollmentService::checkCourseAccess,
                        source -> {
                            com.edunexusgraphql.model.CourseSession session =
                                    (com.edunexusgraphql.model.CourseSession) source;
                            return session.getCourseId();
                        }
                )
        ));

        // Role-based validations
        permissions.add(new PermissionAction("update_course",
                env -> PermissionValidator.validateRole(env, "ADMIN")));
        permissions.add(new PermissionAction("update_record",
                env -> PermissionValidator.validateRole(env, "ADMIN")));

        return permissions;
    }

    /**
     * Create admin permissions set.
     *
     * @return Set of admin permissions
     */
    public static Set<PermissionAction> createAdminPermissions() {
        Set<PermissionAction> permissions = new HashSet<>();

        permissions.add(new PermissionAction("create_course",
                env -> PermissionValidator.validateRole(env, "ADMIN")));
        permissions.add(new PermissionAction("update_course",
                env -> PermissionValidator.validateRole(env, "ADMIN")));
        permissions.add(new PermissionAction("update_user",
                env -> PermissionValidator.validateRole(env, "ADMIN")));

        return permissions;
    }

    private static void validateEnrollmentAccess(DataFetchingEnvironment env,
                                                   EnrollmentService enrollmentService) {
        long argumentUserId = Long.parseLong(env.getArgument("userId"));
        long argumentCourseId = Long.parseLong(env.getArgument("courseId"));

        if (!enrollmentService.checkCourseAccess(argumentCourseId, argumentUserId)) {
            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
    }
}
