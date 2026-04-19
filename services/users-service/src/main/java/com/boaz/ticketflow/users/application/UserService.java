package com.boaz.ticketflow.users.application;


import com.boaz.ticketflow.common.exceptions.RessourceNotFoundException;
import com.boaz.ticketflow.users.domain.model.UserEntity;
import com.boaz.ticketflow.users.domain.port.outbound.UserRepository;
//import com.boaz.ticketflow.users.infrastructure.keycloak.KeycloakService;
import com.yubico.webauthn.data.ByteArray;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
/**
 * Service de gestion des utilisateurs de l'application.
 * Fait le lien entre l'utilisateur Keycloak et l'utilisateur local (stocké en base).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final KeycloakService keycloakService;

    /**
     * Récupère un utilisateur par son ID interne.
     *
     * @param userId l'ID de l'utilisateur
     * @return l'utilisateur
     * @throws RessourceNotFoundException si l'utilisateur n'existe pas
     */
    @Transactional(readOnly = true)
    public UserEntity getUserById(String userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new RessourceNotFoundException("User not found with id: " + userId));
    }

    /**
     * Récupère un utilisateur par son nom d'utilisateur (username).
     * Dans notre cas, le username est généralement le numéro de téléphone.
     *
     * @param username le nom d'utilisateur
     * @return l'utilisateur, s'il existe
     */
    @Transactional(readOnly = true)
    public Optional<UserEntity> findByUsername(String username) {
        return userRepository.findByIdentifier(username);
    }

    /**
     * Récupère un utilisateur par son numéro de téléphone.
     *
     * @param phoneNumber le numéro de téléphone
     * @return l'utilisateur, s'il existe
     */
    @Transactional(readOnly = true)
    public Optional<UserEntity> findByPhone(String phoneNumber) {
        return userRepository.findByPhone(phoneNumber);
    }

    /**
     * Récupère le user handle (sous forme de ByteArray) pour un nom d'utilisateur donné.
     * Utilisé par WebAuthn pour identifier l'utilisateur lors des opérations d'authentification.
     *
     * @param username le nom d'utilisateur
     * @return un Optional contenant le ByteArray représentant l'id de l'utilisateur
     */
    @Transactional(readOnly = true)
    public Optional<ByteArray> getUserHandleForUsername(String username) {
        return userRepository.findByIdentifier(username)
                .map(user -> new ByteArray(user.getId().getBytes()));
    }

    /**
     * Crée ou récupère un utilisateur local à partir de l'ID Keycloak et du numéro de téléphone.
     * Si l'utilisateur n'existe pas en base, on le crée.
     * Cette méthode est appelée après une validation OTP réussie.
     *
     * @param keycloakId l'ID de l'utilisateur dans Keycloak
     * @param phoneNumber le numéro de téléphone
     * @return l'utilisateur (existant ou nouvellement créé)
     */
    @Transactional
    public UserEntity findOrCreateUser(String keycloakId, String phoneNumber) {
        return userRepository.findByKeycloakId(keycloakId)
                .orElseGet(() -> {
                    // Vérifier qu'aucun utilisateur n'existe déjà avec ce téléphone
                    Optional<UserEntity> existingByPhone = userRepository.findByPhone(phoneNumber);
                    if (existingByPhone.isPresent()) {
                        // Cas rare : l'utilisateur a changé de téléphone dans Keycloak ?
                        // On peut lier l'ancien compte au nouveau keycloakId ou lever une exception.
                        // Pour simplifier, on met à jour le keycloakId.
                        UserEntity user = existingByPhone.get();
                        user.setKeycloakId(keycloakId);
                        log.warn("User with phone {} already exists, updating keycloakId", phoneNumber);
                        return userRepository.save(user);
                    }

                    // Création d'un nouvel utilisateur
                    UserEntity newUser = UserEntity.builder()
                            .keycloakId(keycloakId)
                            .phone(phoneNumber)
                            .identifier(phoneNumber)  // on utilise le téléphone comme username par défaut
                            .build();
                    log.info("Creating new local user with keycloakId: {}", keycloakId);
                    return userRepository.save(newUser);
                });
    }

    /**
     * Met à jour le username d'un utilisateur.
     * Utile si l'utilisateur souhaite un identifiant plus lisible (optionnel).
     *
     * @param userId l'ID de l'utilisateur
     * @param newUsername le nouveau nom d'utilisateur
     * @return l'utilisateur mis à jour
     */
    @Transactional
    public UserEntity updateUsername(String userId, String newUsername) {
        UserEntity user = getUserById(userId);
        user.setIdentifier(newUsername);
        return userRepository.save(user);
    }

    /**
     * Supprime un utilisateur local (et probablement aussi dans Keycloak).
     * À utiliser avec précaution.
     *
     * @param userId l'ID de l'utilisateur
     */
    @Transactional
    public void deleteUser(String userId) {
        UserEntity user = getUserById(userId);
        // Optionnel : supprimer aussi dans Keycloak via KeycloakService
        // keycloakService.deleteUser(user.getKeycloakId());
        userRepository.delete(user);
        log.info("Deleted user with id: {}", userId);
    }
}