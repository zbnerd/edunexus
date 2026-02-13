package com.edunexusgraphql.directive;

import com.edunexusgraphql.directive.permission.PermissionDefinitions;
import com.edunexusgraphql.service.EnrollmentService;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Role-based permission service for GraphQL authorization.
 * <p>
 * Refactored to delegate concerns:
 * - Permission definitions delegated to PermissionDefinitions
 * - Validation logic delegated to PermissionValidator
 * - Service focused on permission lookup and execution
 */
@Component
public class RolePermissionService {
    private final Map<String, Set<PermissionAction>> rolePermissions = new HashMap<>();
    private final EnrollmentService enrollmentService;

    public RolePermissionService(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
        initializeRoles();
    }

    /**
     * Initialize role permissions using PermissionDefinitions factory.
     * <p>
     * Refactored from 100+ line method to clean factory calls.
     */
    private void initializeRoles() {
        rolePermissions.put("user", PermissionDefinitions.createUserPermissions(enrollmentService));
        rolePermissions.put("admin", PermissionDefinitions.createAdminPermissions());
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
