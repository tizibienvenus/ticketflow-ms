package com.boaz.ticketflow.users.application;

import com.boaz.ticketflow.users.domain.model.WebAuthnCredential;
import com.boaz.ticketflow.users.domain.port.outbound.WebAuthnCredentialRepository;
import com.yubico.webauthn.data.ByteArray;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;

import com.boaz.ticketflow.users.domain.model.UserEntity;

@Service
@RequiredArgsConstructor
public class CredentialService {

    private final WebAuthnCredentialRepository credentialRepository;

    @Transactional
    public void saveCredential(
        ByteArray credentialId, 
        UserEntity user, 
        ByteArray publicKeyCose,
        long signatureCount, 
        Set<String> transports
    ) {
        WebAuthnCredential cred = new WebAuthnCredential();
        cred.setCredentialId(credentialId.getBase64Url());
        cred.setUser(user);
        cred.setPublicKeyCose(publicKeyCose.getBase64Url());
        cred.setSignatureCount(signatureCount);
        cred.setTransports(transports);
        cred.setCreated(Instant.now());
        credentialRepository.save(cred);
    }

    @Transactional
    public void updateSignatureCount(ByteArray credentialId, long newCount) {
        credentialRepository.findByCredentialId(credentialId.getBase64Url())
            .ifPresent(cred -> {
                cred.setSignatureCount(newCount);
                cred.setLastUsed(Instant.now());
                credentialRepository.save(cred);
            });
    }
}