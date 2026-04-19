package com.boaz.ticketflow.users;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakRoleInitializer {

    private final Keycloak keycloakAdmin;
    @Value("${keycloak.realm}")
    private String realm;

    // Mapping ressource → domaine métier
    private static final Map<String, String> DOMAIN_MAPPING = Map.of(
        "users", "user-management",
        "profile", "user-management",
        "rides", "rides",
        "drivers", "drivers",
        "reports", "reports",
        "availability", "drivers",
        "roles", "system",
        "groups", "system"
    );

    // Mapping ressource → microservice responsable
    private static final Map<String, String> SERVICE_MAPPING = Map.of(
        "rides", "ride-service",
        "drivers", "driver-service",
        "users", "identity-service",
        "profile", "identity-service",
        "reports", "analytics-service",
        "roles", "system",
        "groups", "system"
    );

    @EventListener(ApplicationReadyEvent.class)
    @Transactional // not needed, but ensures it runs after context is ready
    public void initDefaultRoles() {
        try {
            RolesResource rolesResource = keycloakAdmin.realm(realm).roles();

            // 1. Create permission roles (if they don't exist)
            createPermissionRoleIfNotExists(rolesResource, "profile:read"); // tags users to admin
            createPermissionRoleIfNotExists(rolesResource, "profile:update"); // tags users to admin
            createPermissionRoleIfNotExists(rolesResource, "rides:create"); // tags users to admin
            createPermissionRoleIfNotExists(rolesResource, "rides:read"); // tags users to admin
            createPermissionRoleIfNotExists(rolesResource, "rides:cancel"); // tags users to admin
            createPermissionRoleIfNotExists(rolesResource, "rides:accept"); // tags drivers and manager
            createPermissionRoleIfNotExists(rolesResource, "rides:complete"); // tags drivers
            createPermissionRoleIfNotExists(rolesResource, "rides:update"); // // tags users to admins
            createPermissionRoleIfNotExists(rolesResource, "drivers:read");
            createPermissionRoleIfNotExists(rolesResource, "drivers:update");
            createPermissionRoleIfNotExists(rolesResource, "reports:read");
            createPermissionRoleIfNotExists(rolesResource, "reports:generate");
            createPermissionRoleIfNotExists(rolesResource, "availability:update");
            createPermissionRoleIfNotExists(rolesResource, "users:read");
            createPermissionRoleIfNotExists(rolesResource, "users:create");
            createPermissionRoleIfNotExists(rolesResource, "users:update"); // tags rootadmmin
            createPermissionRoleIfNotExists(rolesResource, "users:delete"); // tags users and manager, 
            createPermissionRoleIfNotExists(rolesResource, "system:settings"); // tags system or services
            createPermissionRoleIfNotExists(rolesResource, "*:read"); // tags rootadmmin
            createPermissionRoleIfNotExists(rolesResource, "*:create"); // tags rootadmmin
            createPermissionRoleIfNotExists(rolesResource, "*:update"); // tags rootadmmin
            createPermissionRoleIfNotExists(rolesResource, "*:delete"); // tags rootadmmin

            // 2. Create composite roles (USER, DRIVER, MANAGER, ADMIN, ROOTADMIN)
            createCompositeRoleIfNotExists(rolesResource, "USER",
                Set.of("profile:read", "profile:update", "rides:create", "rides:read", "rides:cancel"));

            createCompositeRoleIfNotExists(rolesResource, "DRIVER",
                Set.of("profile:read", "profile:update", "availability:update",
                    "rides:read", "rides:update", "rides:accept", "rides:complete"
                ));

            createCompositeRoleIfNotExists(rolesResource, "MANAGER",
                Set.of(
                    "users:read", "users:create", "users:update", "users:delete",
                    "rides:read", "rides:create", "rides:update", "rides:delete",
                    "drivers:read", "drivers:update","reports:read", "reports:generate"
                ));

            createCompositeRoleIfNotExists(rolesResource, "ADMIN",
                Set.of("*:read", "*:create", "*:update", "*:delete", "system:settings"));

            createCompositeRoleIfNotExists(rolesResource, "ROOTADMIN",
                Set.of("*:read", "*:create", "*:update", "*:delete", "system:settings"));

            log.info("Default roles and permissions initialized in Keycloak");
        } catch (Exception e) {
            log.error("Failed to initialize Keycloak roles", e);
        }
    }

    private void createPermissionRoleIfNotExists(
        RolesResource rolesResource, 
        String roleName
    ) {
        if (rolesResource.list().stream().noneMatch(r -> r.getName().equals(roleName))) {
            RoleRepresentation role = new RoleRepresentation();
            role.setName(roleName);
            role.setDescription("Permission: " + roleName);
            role.setAttributes(buildPermissionAttributes(roleName));

            rolesResource.create(role);
        }
    }

    private void createCompositeRoleIfNotExists(
        RolesResource rolesResource, 
        String compositeRoleName, 
        Set<String> permissionNames
    ) {
        if (rolesResource.list().stream().noneMatch(r -> r.getName().equals(compositeRoleName))) {
            RoleRepresentation composite = new RoleRepresentation();
            composite.setName(compositeRoleName);
            composite.setDescription("Composite role: " + compositeRoleName);
            composite.setComposite(true); // marks it as composite
            rolesResource.create(composite);

            // Now add composites (permissions) to this role
            RoleResource roleResource = rolesResource.get(compositeRoleName);
            List<RoleRepresentation> permissions = permissionNames.stream()
                .map(rolesResource::get) // get each permission role
                .map(RoleResource::toRepresentation)
                .collect(Collectors.toList());
            roleResource.addComposites(permissions);
        }
    }

    /**
     * Construit les attributs (tags) pour un rôle de permission.
     */
    private Map<String, List<String>> buildPermissionAttributes(String roleName) {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("type", List.of("permission"));

        if (!roleName.contains(":")) {
            return attributes; // pas de découpage possible
        }

        String[] parts = roleName.split(":", 2);
        String resource = parts[0];
        String action = parts[1];

        attributes.put("resource", List.of(resource));
        attributes.put("action", List.of(action));
        attributes.put("domain", List.of(resolveDomain(resource)));
        attributes.put("service", List.of(resolveService(resource)));

        return attributes;
    }

    private String resolveDomain(String resource) {
        return DOMAIN_MAPPING.getOrDefault(resource, "system");
    }

    private String resolveService(String resource) {
        return SERVICE_MAPPING.getOrDefault(resource, "core-service");
    }
}
