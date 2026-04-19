package com.boaz.ticketflow.users.application;


import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.*;

import com.boaz.ticketflow.common.exceptions.RessourceAlreadyExistsException;
import com.boaz.ticketflow.users.domain.model.UserEntity;
import com.boaz.ticketflow.users.domain.port.outbound.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakService {

    private final UserRepository userRepository;
    
    @Value("${keycloak.auth-server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.admin.username}")
    private String adminUsername;

    @Value("${keycloak.admin.password}")
    private String adminPassword;

    @Value("${keycloak.client-id:camergo-identity-service}")
    private String clientId;

    @Value("${keycloak.client-secret:Jo4s3SZYlzuNyTA0IKUc4lWtq6TEYVHC}")
    private String clientSecret;

    private Keycloak getKeycloakAdminInstance() {
        return KeycloakBuilder.builder()
            .serverUrl(keycloakServerUrl)
            .realm("master")
            .username(adminUsername)
            .password(adminPassword)
            .clientId(clientId)
            .build();
    }

    private KeycloakBuilder getKeycloakInstance() {
        return KeycloakBuilder.builder()
            .serverUrl(keycloakServerUrl)
            .realm(realm)
            .clientId(clientId)
            .clientSecret(clientSecret);
    }

    /**
     * Crée un utilisateur dans Keycloak avec seulement le nom d'utilisateur.
     * Aucun mot de passe ni email n'est défini initialement.
     */
    public String createUser(String username) {
        Keycloak keycloak = getKeycloakAdminInstance();
        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();

        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEnabled(true);
        // Pas d'email, pas de mot de passe

        Response response = usersResource.create(user);

        switch (response.getStatus()) {
            case 201 -> {
                String userId = extractUserIdFromLocationHeader(response.getLocation().toString());
                log.info("Utilisateur créé dans Keycloak avec username: {}", username);
                return userId;
            }
            case 409 -> throw new RessourceAlreadyExistsException("User already exists");
            default -> {
                log.error("Erreur création Keycloak. Status: {}", response.getStatus());
                throw new RuntimeException("Erreur création utilisateur Keycloak");
            }
        }
    }


    /**
     * Met à jour le profil utilisateur (email, prénom, nom) dans Keycloak.
     */
    public void updateUserProfile(String keycloakUserId, String email, String firstName, String lastName) {
        try {
            Keycloak keycloak = getKeycloakAdminInstance();
            RealmResource realmResource = keycloak.realm(realm);
            UserResource userResource = realmResource.users().get(keycloakUserId);

            UserRepresentation user = userResource.toRepresentation();
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            // Optionnel : marquer l'email comme vérifié selon votre politique
            user.setEmailVerified(false);

            userResource.update(user);
            log.info("Profil utilisateur mis à jour dans Keycloak: {}", keycloakUserId);
        } catch (Exception e) {
            log.error("Erreur mise à jour profil Keycloak: {}", e.getMessage());
            throw new RuntimeException("Erreur mise à jour profil Keycloak", e);
        }
    }

    public void updateUser(String keycloakUserId, UserEntity user) {
        try {
            Keycloak keycloak = getKeycloakAdminInstance();
            RealmResource realmResource = keycloak.realm(realm);
            UserResource userResource = realmResource.users().get(keycloakUserId);

            UserRepresentation userRepresentation = userResource.toRepresentation();
            userRepresentation.setFirstName(user.getFirstname());
            userRepresentation.setLastName(user.getLastname());
            userRepresentation.setEmail(user.getEmail());
            userRepresentation.setEnabled(user.getEnabled());

            Map<String, List<String>> attributes = userRepresentation.getAttributes();
            if (attributes == null) {
                attributes = new HashMap<>();
            }
            if (!attributes.containsKey("databaseId")) {
                attributes.put("databaseId", Collections.singletonList(user.getId().toString()));
            }
            userRepresentation.setAttributes(attributes);

            userResource.update(userRepresentation);
            log.info("Utilisateur mis à jour dans Keycloak: {}", user.getIdentifier());
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour de l'utilisateur dans Keycloak: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la mise à jour de l'utilisateur dans Keycloak", e);
        }
    }

    public void deleteUser(String keycloakUserId) {
        try {
            Keycloak keycloak = getKeycloakInstance().build();
            RealmResource realmResource = keycloak.realm(realm);
            UserResource userResource = realmResource.users().get(keycloakUserId);
            userResource.remove();
            log.info("Utilisateur supprimé de Keycloak: {}", keycloakUserId);
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de l'utilisateur dans Keycloak: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la suppression de l'utilisateur dans Keycloak", e);
        }
    }

    public void enableUser(String keycloakUserId, boolean enabled) {
        try {
            Keycloak keycloak = getKeycloakInstance().build();
            RealmResource realmResource = keycloak.realm(realm);
            UserResource userResource = realmResource.users().get(keycloakUserId);

            UserRepresentation userRepresentation = userResource.toRepresentation();
            userRepresentation.setEnabled(enabled);
            userResource.update(userRepresentation);

            log.info("Statut utilisateur modifié dans Keycloak: {} - Enabled: {}", keycloakUserId, enabled);
        } catch (Exception e) {
            log.error("Erreur lors de la modification du statut utilisateur dans Keycloak: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la modification du statut utilisateur dans Keycloak", e);
        }
    }

    private void setPassword(UsersResource usersResource, String userId, String password) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);

        UserResource userResource = usersResource.get(userId);
        userResource.resetPassword(credential);
    }

    private String extractUserIdFromLocationHeader(String locationHeader) {
        return locationHeader.substring(locationHeader.lastIndexOf("/") + 1);
    }
}