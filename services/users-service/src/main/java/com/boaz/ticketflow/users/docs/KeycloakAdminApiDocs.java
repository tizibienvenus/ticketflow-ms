package com.boaz.ticketflow.users.docs;

import java.net.URI;
import java.util.List;

import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.boaz.ticketflow.users.application.dtos.request.CreateRoleRequest;
import com.boaz.ticketflow.users.application.dtos.request.GroupNameRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

@Tag(name = "Keycloak Admin", description = "Administrative endpoints for managing roles, groups, and user assignments in Keycloak")
public interface KeycloakAdminApiDocs {

    // -------------------- GESTION DES RÔLES --------------------

    @Operation(
        summary = "Create a new role",
        description = "Creates a realm-level role. The role can be composite (contain other roles)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Role created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid role name or duplicate role"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions (requires 'roles:create')")
    })
    @PostMapping("/roles")
    ResponseEntity<Void> createRole(@Valid @RequestBody CreateRoleRequest request);

    @Operation(
        summary = "Delete a role",
        description = "Permanently removes a realm-level role. Does not affect users/groups that had the role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Role deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Role not found"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions (requires 'roles:delete')")
    })
    @DeleteMapping("/roles/{roleName}")
    ResponseEntity<Void> deleteRole(
        @Parameter(description = "Name of the role to delete", example = "admin", required = true)
        @PathVariable String roleName
    );

    @Operation(
        summary = "Add composite roles",
        description = "Makes a role composite by adding a list of child roles. The parent role will inherit the child roles' permissions."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Composite roles added successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid role names or circular reference"),
        @ApiResponse(responseCode = "404", description = "Parent or child role not found"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions (requires 'roles:manage')")
    })
    @PostMapping("/roles/{roleName}/composites")
    ResponseEntity<Void> addComposites(
        @Parameter(description = "Parent role name", example = "super-admin", required = true)
        @PathVariable String roleName,
        @Parameter(description = "List of child role names to add", required = true)
        @RequestBody List<String> compositeRoleNames
    );

    @Operation(
        summary = "Remove composite roles",
        description = "Removes a list of child roles from a composite role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Composite roles removed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid role names"),
        @ApiResponse(responseCode = "404", description = "Role not found"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions (requires 'roles:manage')")
    })
    @DeleteMapping("/roles/{roleName}/composites")
    ResponseEntity<Void> removeComposites(
        @Parameter(description = "Parent role name", example = "super-admin", required = true)
        @PathVariable String roleName,
        @Parameter(description = "List of child role names to remove", required = true)
        @RequestBody List<String> compositeRoleNames
    );

    @Operation(
        summary = "Get composite roles",
        description = "Retrieves the list of child roles associated with a composite role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Composite roles retrieved",
                     content = @Content(schema = @Schema(implementation = List.class))),
        @ApiResponse(responseCode = "404", description = "Role not found"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions (requires 'roles:read')")
    })
    @GetMapping("/roles/{roleName}/composites")
    ResponseEntity<List<String>> getRoleComposites(
        @Parameter(description = "Role name", example = "super-admin", required = true)
        @PathVariable String roleName
    );

    @Operation(
        summary = "List all realm roles",
        description = "Returns a list of all realm-level role names."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of role names",
                     content = @Content(schema = @Schema(implementation = List.class))),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions (requires 'roles:read')")
    })
    @GetMapping("/roles")
    ResponseEntity<List<String>> getAllRoles();

    @Operation(
        summary = "Get role details",
        description = "Retrieves full representation of a role (including attributes, composite flag, etc.)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Role details",
                     content = @Content(schema = @Schema(implementation = RoleRepresentation.class))),
        @ApiResponse(responseCode = "404", description = "Role not found"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions (requires 'roles:read')")
    })
    @GetMapping("/roles/{roleName}")
    ResponseEntity<RoleRepresentation> getRole(
        @Parameter(description = "Role name", example = "admin", required = true)
        @PathVariable String roleName
    );

    // -------------------- ASSIGNATION AUX UTILISATEURS --------------------

    @Operation(
        summary = "Assign roles to a user",
        description = "Adds a list of realm roles to a specific user."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Roles assigned successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid role names or user ID"),
        @ApiResponse(responseCode = "404", description = "User or role not found"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions (requires 'users:assign-role')")
    })
    @PostMapping("/users/{userId}/roles")
    ResponseEntity<Void> assignRolesToUser(
        @Parameter(description = "User ID (UUID)", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
        @PathVariable String userId,
        @Parameter(description = "List of role names to assign", required = true)
        @RequestBody List<String> roleNames
    );

    @Operation(
        summary = "Remove roles from a user",
        description = "Removes a list of realm roles from a user."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Roles removed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid role names or user ID"),
        @ApiResponse(responseCode = "404", description = "User or role not found"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions (requires 'users:assign-role')")
    })
    @DeleteMapping("/users/{userId}/roles")
    ResponseEntity<Void> removeRolesFromUser(
        @Parameter(description = "User ID (UUID)", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
        @PathVariable String userId,
        @Parameter(description = "List of role names to remove", required = true)
        @RequestBody List<String> roleNames
    );

    @Operation(
        summary = "Get user roles",
        description = "Retrieves all realm roles assigned to a user."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of role names",
                     content = @Content(schema = @Schema(implementation = List.class))),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions (requires 'users:read')")
    })
    @GetMapping("/users/{userId}/roles")
    ResponseEntity<List<String>> getUserRoles(
        @Parameter(description = "User ID (UUID)", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
        @PathVariable String userId
    );

    // -------------------- GESTION DES GROUPES --------------------

    @Operation(
        summary = "Create a new group",
        description = "Creates a new user group in Keycloak."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Group created successfully",
                     headers = @io.swagger.v3.oas.annotations.headers.Header(
                         name = "Location", description = "URI of the newly created group")),
        @ApiResponse(responseCode = "400", description = "Invalid group name or duplicate"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions (requires 'groups:create')")
    })
    @PostMapping("/groups")
    ResponseEntity<Void> createGroup(@Valid @RequestBody GroupNameRequest request);

    @Operation(
        summary = "Delete a group",
        description = "Permanently removes a group. Users in the group are not removed, but they lose the group membership."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Group deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Group not found"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions (requires 'groups:delete')")
    })
    @DeleteMapping("/groups/{groupId}")
    ResponseEntity<Void> deleteGroup(
        @Parameter(description = "Group ID (UUID)", example = "abc12345-1234-1234-1234-123456789012", required = true)
        @PathVariable String groupId
    );

    @Operation(
        summary = "Assign roles to a group",
        description = "Adds realm roles to a group. All members of the group will inherit these roles."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Roles assigned to group successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid role names or group ID"),
        @ApiResponse(responseCode = "404", description = "Group or role not found"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions (requires 'groups:manage')")
    })
    @PostMapping("/groups/{groupId}/roles")
    ResponseEntity<Void> assignRolesToGroup(
        @Parameter(description = "Group ID (UUID)", example = "abc12345-1234-1234-1234-123456789012", required = true)
        @PathVariable String groupId,
        @Parameter(description = "List of role names to assign", required = true)
        @RequestBody List<String> roleNames
    );

    @Operation(
        summary = "Remove roles from a group",
        description = "Removes realm roles from a group."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Roles removed from group successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid role names or group ID"),
        @ApiResponse(responseCode = "404", description = "Group or role not found"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions (requires 'groups:manage')")
    })
    @DeleteMapping("/groups/{groupId}/roles")
    ResponseEntity<Void> removeRolesFromGroup(
        @Parameter(description = "Group ID (UUID)", example = "abc12345-1234-1234-1234-123456789012", required = true)
        @PathVariable String groupId,
        @Parameter(description = "List of role names to remove", required = true)
        @RequestBody List<String> roleNames
    );

    @Operation(
        summary = "Get group roles",
        description = "Retrieves all realm roles assigned to a group."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of role names",
                     content = @Content(schema = @Schema(implementation = List.class))),
        @ApiResponse(responseCode = "404", description = "Group not found"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions (requires 'groups:read')")
    })
    @GetMapping("/groups/{groupId}/roles")
    ResponseEntity<List<String>> getGroupRoles(
        @Parameter(description = "Group ID (UUID)", example = "abc12345-1234-1234-1234-123456789012", required = true)
        @PathVariable String groupId
    );

    @Operation(
        summary = "Add user to group",
        description = "Adds an existing user to a group."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User added to group successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid user or group ID"),
        @ApiResponse(responseCode = "404", description = "User or group not found"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions (requires 'groups:manage')")
    })
    @PostMapping("/groups/{groupId}/members/{userId}")
    ResponseEntity<Void> addUserToGroup(
        @Parameter(description = "User ID (UUID)", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
        @PathVariable String userId,
        @Parameter(description = "Group ID (UUID)", example = "abc12345-1234-1234-1234-123456789012", required = true)
        @PathVariable String groupId
    );

    @Operation(
        summary = "Remove user from group",
        description = "Removes a user from a group."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User removed from group successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid user or group ID"),
        @ApiResponse(responseCode = "404", description = "User or group not found"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions (requires 'groups:manage')")
    })
    @DeleteMapping("/groups/{groupId}/members/{userId}")
    ResponseEntity<Void> removeUserFromGroup(
        @Parameter(description = "User ID (UUID)", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
        @PathVariable String userId,
        @Parameter(description = "Group ID (UUID)", example = "abc12345-1234-1234-1234-123456789012", required = true)
        @PathVariable String groupId
    );

    @Operation(
        summary = "Get user groups",
        description = "Retrieves all groups a user belongs to, including group details."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of groups",
                     content = @Content(schema = @Schema(implementation = List.class))),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions (requires 'users:read')")
    })
    @GetMapping("/users/{userId}/groups")
    ResponseEntity<List<GroupRepresentation>> getUserGroups(
        @Parameter(description = "User ID (UUID)", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
        @PathVariable String userId
    );
}