package com.boaz.ticketflow.users.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.boaz.ticketflow.users.domain.model.WebAuthnCredential;
import com.boaz.ticketflow.users.domain.port.outbound.WebAuthnCredentialRepository;

public interface JpaWebAuthnCredentialRepository extends JpaRepository<WebAuthnCredential, String>, WebAuthnCredentialRepository {
    @Override
    List<WebAuthnCredential> findByUserIdentifier(String identifier);
    
    @Override
    Optional<WebAuthnCredential> findByCredentialId(String credentialId);
    
    @Override
    List<WebAuthnCredential> findByUserId(String userId);
}
