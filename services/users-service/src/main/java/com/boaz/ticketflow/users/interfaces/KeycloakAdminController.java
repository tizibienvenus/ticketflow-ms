package com.boaz.ticketflow.users.interfaces;

import java.net.URI;
import java.util.List;

import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.boaz.ticketflow.users.application.KeycloakAdminService;
import com.boaz.ticketflow.users.application.dtos.request.CreateRoleRequest;
import com.boaz.ticketflow.users.application.dtos.request.GroupNameRequest;
import com.boaz.ticketflow.users.docs.KeycloakAdminApiDocs;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/keycloak")
@RequiredArgsConstructor
public class KeycloakAdminController implements KeycloakAdminApiDocs{

    private final KeycloakAdminService keycloakAdminService;

    // -------------------- GESTION DES RÔLES --------------------

    @Override
    @PostMapping("/roles")
    @PreAuthorize("hasAuthority('roles:create')")
    public ResponseEntity<Void> createRole(@Valid @RequestBody CreateRoleRequest request) {
        keycloakAdminService.createRole(request.name(), request.description(), request.composite());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @DeleteMapping("/roles/{roleName}")
    @PreAuthorize("hasAuthority('roles:delete')")
    public ResponseEntity<Void> deleteRole(@PathVariable String roleName) {
        keycloakAdminService.deleteRole(roleName);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PostMapping("/roles/{roleName}/composites")
    @PreAuthorize("hasAuthority('roles:manage')")
    public ResponseEntity<Void> addComposites(
        @PathVariable String roleName,
        @RequestBody List<String> compositeRoleNames
    ) {
        keycloakAdminService.addCompositesToRole(roleName, compositeRoleNames);
        return ResponseEntity.ok().build();
    }

    @Override
    @DeleteMapping("/roles/{roleName}/composites")
    @PreAuthorize("hasAuthority('roles:manage')")
    public ResponseEntity<Void> removeComposites(
        @PathVariable String roleName,
        @RequestBody List<String> compositeRoleNames
    ) {
        keycloakAdminService.removeCompositesFromRole(roleName, compositeRoleNames);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/roles/{roleName}/composites")
    @PreAuthorize("hasAuthority('roles:read')")
    public ResponseEntity<List<String>> getRoleComposites(
        @PathVariable String roleName
    ) {
        List<String> composites = keycloakAdminService.getRoleComposites(roleName);
        return ResponseEntity.ok(composites);
    }

    @Override
    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('roles:read')")
    public ResponseEntity<List<String>> getAllRoles() {
        List<String> roles = keycloakAdminService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    @Override
    @GetMapping("/roles/{roleName}")
    @PreAuthorize("hasAuthority('roles:read')")
    public ResponseEntity<RoleRepresentation> getRole(@PathVariable String roleName) {
        RoleRepresentation role = keycloakAdminService.getRole(roleName);
        return ResponseEntity.ok(role);
    }

    // -------------------- ASSIGNATION AUX UTILISATEURS --------------------

    @Override
    @PostMapping("/users/{userId}/roles")
    @PreAuthorize("hasAuthority('users:assign-role')")
    public ResponseEntity<Void> assignRolesToUser(
        @PathVariable String userId,
        @RequestBody List<String> roleNames
    ) {
        keycloakAdminService.assignRolesToUser(userId, roleNames);
        return ResponseEntity.ok().build();
    }

    @Override
    @DeleteMapping("/users/{userId}/roles")
    @PreAuthorize("hasAuthority('users:assign-role')")
    public ResponseEntity<Void> removeRolesFromUser(
        @PathVariable String userId,
        @RequestBody List<String> roleNames
    ) {
        keycloakAdminService.removeRolesFromUser(userId, roleNames);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/users/{userId}/roles")
    @PreAuthorize("hasAuthority('users:read')")
    public ResponseEntity<List<String>> getUserRoles(@PathVariable String userId) {
        List<String> roles = keycloakAdminService.getUserRoles(userId);
        return ResponseEntity.ok(roles);
    }

    // -------------------- GESTION DES GROUPES --------------------

    @Override
    @PostMapping("/groups")
    @PreAuthorize("hasAuthority('groups:create')")
    public ResponseEntity<Void> createGroup(@Valid @RequestBody GroupNameRequest request) {
        String groupId = keycloakAdminService.createGroup(request.name());
        return ResponseEntity.created(URI.create("/api/admin/keycloak/groups/" + groupId)).build();
    }

    @Override
    @DeleteMapping("/groups/{groupId}")
    @PreAuthorize("hasAuthority('groups:delete')")
    public ResponseEntity<Void> deleteGroup(@PathVariable String groupId) {
        keycloakAdminService.deleteGroup(groupId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PostMapping("/groups/{groupId}/roles")
    @PreAuthorize("hasAuthority('groups:manage')")
    public ResponseEntity<Void> assignRolesToGroup(
        @PathVariable String groupId,
        @RequestBody List<String> roleNames
    ) {
        keycloakAdminService.assignRolesToGroup(groupId, roleNames);
        return ResponseEntity.ok().build();
    }

    @Override
    @DeleteMapping("/groups/{groupId}/roles")
    @PreAuthorize("hasAuthority('groups:manage')")
    public ResponseEntity<Void> removeRolesFromGroup(
        @PathVariable String groupId,
        @RequestBody List<String> roleNames
    ) {
        keycloakAdminService.removeRolesFromGroup(groupId, roleNames);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/groups/{groupId}/roles")
    @PreAuthorize("hasAuthority('groups:read')")
    public ResponseEntity<List<String>> getGroupRoles(@PathVariable String groupId) {
        List<String> roles = keycloakAdminService.getGroupRoles(groupId);
        return ResponseEntity.ok(roles);
    }

    @Override
    @PostMapping("/groups/{groupId}/members/{userId}")
    @PreAuthorize("hasAuthority('groups:manage')")
    public ResponseEntity<Void> addUserToGroup(
        @PathVariable String userId,
        @PathVariable String groupId
    ) {
        keycloakAdminService.addUserToGroup(userId, groupId);
        return ResponseEntity.ok().build();
    }

    @Override
    @DeleteMapping("/groups/{groupId}/members/{userId}")
    @PreAuthorize("hasAuthority('groups:manage')")
    public ResponseEntity<Void> removeUserFromGroup(
        @PathVariable String userId,
        @PathVariable String groupId
    ) {
        keycloakAdminService.removeUserFromGroup(userId, groupId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/users/{userId}/groups")
    @PreAuthorize("hasAuthority('users:read')")
    public ResponseEntity<List<GroupRepresentation>> getUserGroups(@PathVariable String userId) {
        List<GroupRepresentation> groups = keycloakAdminService.getUserGroups(userId);
        return ResponseEntity.ok(groups);
    }
    
}