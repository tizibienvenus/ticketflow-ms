package com.boaz.ticketflow.users.infrastructure.persistence;

import java.util.Base64;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.boaz.ticketflow.users.domain.model.UserEntity;
import com.boaz.ticketflow.users.domain.port.outbound.UserRepository;
import com.boaz.ticketflow.users.domain.port.outbound.WebAuthnCredentialRepository;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CredentialStorage implements CredentialRepository {

    private WebAuthnCredentialRepository credentialRepo;
    private UserRepository userRepo;

    private ByteArray toByteArray(String base64Url) {
        return new ByteArray(Base64.getUrlDecoder().decode(base64Url));
    }

    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
        return credentialRepo.findByUserIdentifier(username).stream()
                .map(cred -> PublicKeyCredentialDescriptor.builder()
                        .id(toByteArray(cred.getCredentialId()))
                        .build())
                .collect(Collectors.toSet());
    }

    @Override
    public Optional<ByteArray> getUserHandleForUsername(String username) {
        return userRepo.findByIdentifier(username)
            .map(user -> new ByteArray(user.getId().getBytes()));
    }

    @Override
    public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
        String userId = new String(userHandle.getBytes());
        return userRepo.findById(userId).map(UserEntity::getIdentifier);
    }

    @Override
    public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
        return credentialRepo.findByCredentialId(credentialId.getBase64Url())
                .map(cred -> RegisteredCredential.builder()
                        .credentialId(credentialId)
                        .userHandle(userHandle)
                        .publicKeyCose(toByteArray(cred.getPublicKeyCose()))
                        .signatureCount(cred.getSignatureCount())
                        .build());
    }

    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
        return credentialRepo.findByCredentialId(credentialId.getBase64Url())
                .map(cred -> Set.of(RegisteredCredential.builder()
                        .credentialId(credentialId)
                        .userHandle(new ByteArray(cred.getUser().getId().toString().getBytes()))
                        .publicKeyCose(toByteArray(cred.getPublicKeyCose()))
                        .signatureCount(cred.getSignatureCount())
                        .build()))
                .orElse(Set.of());
    }
}