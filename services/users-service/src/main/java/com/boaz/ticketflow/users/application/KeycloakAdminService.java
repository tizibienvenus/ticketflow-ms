package com.boaz.ticketflow.users.application;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakAdminService {

    private final Keycloak keycloakAdmin;
    @Value("${keycloak.realm}")
    private String realm;

    // -------------------- RÔLES (SIMPLES ET COMPOSITES) --------------------

    /**
     * Crée un rôle (simple ou composite). Pour un rôle composite, passez {@code composite = true}.
     * Les rôles composites sont créés vides ; utilisez {@link #addCompositesToRole} pour les remplir.
     */
    public void createRole(String roleName, String description, boolean composite) {
        RoleRepresentation role = new RoleRepresentation();
        role.setName(roleName);
        role.setDescription(description);
        role.setComposite(composite);
        try {
            keycloakAdmin.realm(realm).roles().create(role);
            log.info("Role '{}' created (composite: {})", roleName, composite);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create role '" + roleName + "'", e);
        }
    }

    /**
     * Supprime un rôle. Keycloak retire automatiquement ce rôle de tous les composites et utilisateurs.
     */
    public void deleteRole(String roleName) {
        try {
            keycloakAdmin.realm(realm).roles().deleteRole(roleName);
            log.info("Role '{}' deleted", roleName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete role '" + roleName + "'", e);
        }
    }

    /**
     * Ajoute des rôles (simples ou composites) en tant que composites d'un rôle existant.
     * @param roleName le rôle cible (doit être composite)
     * @param compositeRoleNames liste des noms de rôles à ajouter comme composites
     */
    public void addCompositesToRole(String roleName, List<String> compositeRoleNames) {
        RoleResource roleResource = keycloakAdmin.realm(realm).roles().get(roleName);
        List<RoleRepresentation> composites = compositeRoleNames.stream()
                .map(name -> keycloakAdmin.realm(realm).roles().get(name).toRepresentation())
                .collect(Collectors.toList());
        roleResource.addComposites(composites);
        log.info("Added composites {} to role '{}'", compositeRoleNames, roleName);
    }

    /**
     * Retire des rôles composites d'un rôle.
     */
    public void removeCompositesFromRole(String roleName, List<String> compositeRoleNames) {
        RoleResource roleResource = keycloakAdmin.realm(realm).roles().get(roleName);
        List<RoleRepresentation> composites = compositeRoleNames.stream()
                .map(name -> keycloakAdmin.realm(realm).roles().get(name).toRepresentation())
                .collect(Collectors.toList());
        roleResource.deleteComposites(composites);
        log.info("Removed composites {} from role '{}'", compositeRoleNames, roleName);
    }

    /**
     * Récupère la liste des rôles composites d'un rôle.
     */
    public List<String> getRoleComposites(String roleName) {
        return keycloakAdmin.realm(realm).roles().get(roleName).getRoleComposites().stream()
                .map(RoleRepresentation::getName)
                .collect(Collectors.toList());
    }

    // -------------------- ASSIGNATION DIRECTE AUX UTILISATEURS --------------------

    /**
     * Assigne un ou plusieurs rôles à un utilisateur.
     */
    public void assignRolesToUser(String userId, List<String> roleNames) {
        RealmResource realmResource = keycloakAdmin.realm(realm);
        UserResource userResource = realmResource.users().get(userId);
        List<RoleRepresentation> roles = roleNames.stream()
                .map(name -> realmResource.roles().get(name).toRepresentation())
                .collect(Collectors.toList());
        userResource.roles().realmLevel().add(roles);
        log.info("Assigned roles {} to user {}", roleNames, userId);
    }

    /**
     * Retire un ou plusieurs rôles d'un utilisateur.
     */
    public void removeRolesFromUser(String userId, List<String> roleNames) {
        RealmResource realmResource = keycloakAdmin.realm(realm);
        UserResource userResource = realmResource.users().get(userId);
        List<RoleRepresentation> roles = roleNames.stream()
                .map(name -> realmResource.roles().get(name).toRepresentation())
                .collect(Collectors.toList());
        userResource.roles().realmLevel().remove(roles);
        log.info("Removed roles {} from user {}", roleNames, userId);
    }

    /**
     * Liste les rôles d'un utilisateur (directs, sans héritage des groupes).
     */
    public List<String> getUserRoles(String userId) {
        return keycloakAdmin.realm(realm).users().get(userId).roles().realmLevel().listAll().stream()
                .map(RoleRepresentation::getName)
                .collect(Collectors.toList());
    }

    // -------------------- GROUPES --------------------

    /**
     * Crée un groupe.
     * @return l'ID du groupe créé
     */
    public String createGroup(String groupName) {
        GroupRepresentation group = new GroupRepresentation();
        group.setName(groupName);
        try (Response response = keycloakAdmin.realm(realm).groups().add(group)) {
            if (response.getStatus() == 201) {
                String location = response.getHeaderString("Location");
                String groupId = location.substring(location.lastIndexOf('/') + 1);
                log.info("Group '{}' created with id {}", groupName, groupId);
                return groupId;
            } else {
                throw new RuntimeException("Failed to create group, status: " + response.getStatus());
            }
        }
    }

    /**
     * Supprime un groupe.
     */
    public void deleteGroup(String groupId) {
        keycloakAdmin.realm(realm).groups().group(groupId).remove();
        log.info("Group {} deleted", groupId);
    }

    /**
     * Assigne des rôles à un groupe.
     */
    public void assignRolesToGroup(String groupId, List<String> roleNames) {
        RealmResource realmResource = keycloakAdmin.realm(realm);
        GroupResource groupResource = realmResource.groups().group(groupId);
        List<RoleRepresentation> roles = roleNames.stream()
                .map(name -> realmResource.roles().get(name).toRepresentation())
                .collect(Collectors.toList());
        groupResource.roles().realmLevel().add(roles);
        log.info("Assigned roles {} to group {}", roleNames, groupId);
    }

    /**
     * Retire des rôles d'un groupe.
     */
    public void removeRolesFromGroup(String groupId, List<String> roleNames) {
        RealmResource realmResource = keycloakAdmin.realm(realm);
        GroupResource groupResource = realmResource.groups().group(groupId);
        List<RoleRepresentation> roles = roleNames.stream()
                .map(name -> realmResource.roles().get(name).toRepresentation())
                .collect(Collectors.toList());
        groupResource.roles().realmLevel().remove(roles);
        log.info("Removed roles {} from group {}", roleNames, groupId);
    }

    /**
     * Liste les rôles d'un groupe.
     */
    public List<String> getGroupRoles(String groupId) {
        return keycloakAdmin.realm(realm).groups().group(groupId).roles().realmLevel().listAll().stream()
                .map(RoleRepresentation::getName)
                .collect(Collectors.toList());
    }

    /**
     * Ajoute un utilisateur à un groupe.
     */
    public void addUserToGroup(String userId, String groupId) {
        keycloakAdmin.realm(realm).users().get(userId).joinGroup(groupId);
        log.info("User {} added to group {}", userId, groupId);
    }

    /**
     * Retire un utilisateur d'un groupe.
     */
    public void removeUserFromGroup(String userId, String groupId) {
        keycloakAdmin.realm(realm).users().get(userId).leaveGroup(groupId);
        log.info("User {} removed from group {}", userId, groupId);
    }

    /**
     * Liste les groupes d'un utilisateur.
     */
    public List<GroupRepresentation> getUserGroups(String userId) {
        return keycloakAdmin.realm(realm).users().get(userId).groups();
    }

    // -------------------- UTILITAIRES --------------------

    /**
     * Liste tous les rôles du realm (simples et composites).
     */
    public List<String> getAllRoles() {
        return keycloakAdmin.realm(realm).roles().list().stream()
                .map(RoleRepresentation::getName)
                .collect(Collectors.toList());
    }

    /**
     * Vérifie si un rôle existe.
     */
    public boolean roleExists(String roleName) {
        return keycloakAdmin.realm(realm).roles().list().stream()
                .anyMatch(r -> r.getName().equals(roleName));
    }

    /**
     * Récupère les détails d'un rôle.
     */
    public RoleRepresentation getRole(String roleName) {
        return keycloakAdmin.realm(realm).roles().get(roleName).toRepresentation();
    }
}