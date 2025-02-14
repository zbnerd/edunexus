package com.edunexusgraphql.directive;

import com.edunexusgraphql.model.CourseSession;
import com.edunexusgraphql.service.EnrollmentService;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class RolePermissionService {
    private final Map<String, Set<PermissionAction>> rolePermissions = new HashMap<>();
    private final EnrollmentService enrollmentService;

    public RolePermissionService(EnrollmentService enrollmentService) {
        initializeRoles();
        this.enrollmentService = enrollmentService;
    }

    private void initializeRoles() {

        Set<PermissionAction> userPermissions = new HashSet<>();

        userPermissions.add(new PermissionAction("read_user", env -> {
            Long headerUserId = Long.valueOf(env.getGraphQlContext().get("X-USER-ID"));
            Long argumentUserId = Long.valueOf(env.getArgument("userId"));

            if (!headerUserId.equals(argumentUserId)) {
                throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Unauthorized");
            }
        }));

        userPermissions.add(new PermissionAction("update_user", env -> {
            Long headerUserId = Long.valueOf(env.getGraphQlContext().get("X-USER-ID"));
            Long argumentUserId = Long.valueOf(env.getArgument("userId"));

            if (!headerUserId.equals(argumentUserId)) {
                throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Unauthorized");
            }
        }));

        userPermissions.add(new PermissionAction("read_purchase", env -> {

            Long headerUserId = Long.valueOf(env.getGraphQlContext().get("X-USER-ID"));
            long argumentUserId = Long.parseLong(env.getArgument("userId"));
            long argumentCourseId = Long.parseLong(env.getArgument("courseId"));

            if (!headerUserId.equals(argumentUserId)) {
                throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Unauthorized");
            }

            if (!enrollmentService.checkCourseAccess(argumentCourseId, argumentUserId)) {
                throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Unauthorized");
            }
        }));
        userPermissions.add(new PermissionAction("read_files", env -> {
            long userId = Long.parseLong(env.getGraphQlContext().get("X-USER-ID"));
            CourseSession session = env.getSource();
            long courseId = session.getCourseId();

            if (!enrollmentService.checkCourseAccess(courseId, userId)) {
                throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Unauthorized");
            }
        }));
        userPermissions.add(new PermissionAction("read_enrollment", env -> {
            long headerUserId = Long.parseLong(env.getGraphQlContext().get("X-USER-ID"));
            long argumentUserId = Long.parseLong(env.getArgument("userId"));

            if (headerUserId != argumentUserId) {
                throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Unauthorized");
            }
        }));
        userPermissions.add(new PermissionAction("update_course", env -> {
            String headerUserRole = env.getGraphQlContext().get("X-USER-ROLE");

            if (!headerUserRole.equals("ADMIN")) {
                throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Unauthorized");
            }
        }));
        userPermissions.add(new PermissionAction("update_record", env -> {
            String headerUserRole = env.getGraphQlContext().get("X-USER-ROLE");

            if (!headerUserRole.equals("ADMIN")) {
                throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Unauthorized");
            }
        }));
        userPermissions.add(new PermissionAction("update_purchase", env -> {
            long headerUserId = Long.parseLong(env.getGraphQlContext().get("X-USER-ID"));
            long argumentUserId = Long.parseLong(env.getArgument("userId"));

            if (headerUserId != argumentUserId) {
                throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Unauthorized");
            }
        }));

        Set<PermissionAction> adminPermissions = new HashSet<>();
        adminPermissions.add(new PermissionAction("create_course", env -> {
            String headerUserRole = env.getGraphQlContext().get("X-USER-ROLE");

            if (!headerUserRole.equals("ADMIN")) {
                throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Unauthorized");
            }
        }));
        adminPermissions.add(new PermissionAction("update_course", env -> {
            String headerUserRole = env.getGraphQlContext().get("X-USER-ROLE");

            if (!headerUserRole.equals("ADMIN")) {
                throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Unauthorized");
            }
        }));
        adminPermissions.add(new PermissionAction("update_user", env -> {
            String headerUserRole = env.getGraphQlContext().get("X-USER-ROLE");

            if (!headerUserRole.equals("ADMIN")) {
                throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Unauthorized");
            }
        }));

        rolePermissions.put("user", userPermissions);
        rolePermissions.put("admin", adminPermissions);
    }

    public boolean checkPermission(String role, String permission, DataFetchingEnvironment env) {
        Set<PermissionAction> actions = rolePermissions.get(role);
        if (actions != null) {
            for (PermissionAction action : actions) {
                if (action.getPermission().equals(permission)) {
                    action.executeAction(env);
                    return true;
                }
            }
        }
        return false;
    }
}
