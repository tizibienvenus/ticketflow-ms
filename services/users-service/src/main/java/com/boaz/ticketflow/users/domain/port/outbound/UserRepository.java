package com.boaz.ticketflow.users.domain.port.outbound;

import java.util.Optional;

import com.boaz.ticketflow.users.domain.model.UserEntity;

public interface UserRepository {
    UserEntity save(UserEntity user);
    void delete(UserEntity user);
    Optional<UserEntity> findByPhone(String phone);
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findById(String id);
    Optional<UserEntity> findByIdentifier(String identifier);
    Optional<UserEntity> findByKeycloakId(String keycloakId);
}
