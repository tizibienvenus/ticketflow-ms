package com.boaz.ticketflow.users.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.boaz.ticketflow.users.domain.model.UserEntity;
import com.boaz.ticketflow.users.domain.port.outbound.UserRepository;

public interface JpaUserRepository extends JpaRepository<UserEntity, String>, UserRepository {

    @Override
    Optional<UserEntity> findByKeycloakId(String keycloakId);

    @Override
    Optional<UserEntity> findByIdentifier(String identifier);
}