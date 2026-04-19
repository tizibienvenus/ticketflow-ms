package com.boaz.ticketflow.users.domain.port.outbound;

import java.util.List;
import java.util.Optional;

import com.boaz.ticketflow.users.domain.model.WebAuthnCredential;

public interface WebAuthnCredentialRepository {
    WebAuthnCredential save(WebAuthnCredential auth);
    Optional<WebAuthnCredential> findById(String credentialId);
    List<WebAuthnCredential> findByUserIdentifier(String identifier);
    Optional<WebAuthnCredential> findByCredentialId(String credentialId);
    List<WebAuthnCredential> findByUserId(String userId);

}
