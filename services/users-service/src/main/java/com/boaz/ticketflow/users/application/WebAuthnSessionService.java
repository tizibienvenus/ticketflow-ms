package com.boaz.ticketflow.users.application;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class WebAuthnSessionService {

    private static final long CHALLENGE_TTL_SECONDS = 300;

    // Map pour les challenges d'enregistrement (clé = userHandle, valeur = challenge)
    private final Map<ByteArray, ChallengeEntry> registrationChallenges = new ConcurrentHashMap<>();

    // Map pour les challenges d'authentification (clé = challenge, valeur = la requête complète + timestamp)
    private final Map<ByteArray, AssertionRequestEntry> loginRequests = new ConcurrentHashMap<>();

    private final Map<ByteArray, RegistrationOptionsEntry> registrationOptions = new ConcurrentHashMap<>();

    // --- Enregistrement ---
    public void storeRegistrationChallenge(ByteArray userHandle, ByteArray challenge) {
        registrationChallenges.put(userHandle, new ChallengeEntry(challenge, Instant.now()));
        log.debug("Registration challenge stored for userHandle: {}", userHandle);
    }

    public Optional<ByteArray> getRegistrationChallenge(ByteArray userHandle) {
        ChallengeEntry entry = registrationChallenges.get(userHandle);
        if (entry == null) return Optional.empty();
        if (isExpired(entry.getTimestamp())) {
            registrationChallenges.remove(userHandle);
            return Optional.empty();
        }
        return Optional.of(entry.getChallenge());
    }

    public void removeRegistrationChallenge(ByteArray userHandle) {
        registrationChallenges.remove(userHandle);
    }

    // --- Authentification ---
    public void storeLoginRequest(ByteArray challenge, AssertionRequest request) {
        loginRequests.put(challenge, new AssertionRequestEntry(request, Instant.now()));
        log.debug("Login request stored for challenge: {}", challenge);
    }

    public Optional<AssertionRequest> getLoginRequest(ByteArray challenge) {
        AssertionRequestEntry entry = loginRequests.get(challenge);
        if (entry == null) return Optional.empty();
        if (isExpired(entry.getTimestamp())) {
            loginRequests.remove(challenge);
            return Optional.empty();
        }
        return Optional.of(entry.getRequest());
    }

    public void removeLoginRequest(ByteArray challenge) {
        loginRequests.remove(challenge);
    }

    // ==================== Enregistrement (options complètes) ====================
    public void storeRegistrationOptions(ByteArray userHandle, PublicKeyCredentialCreationOptions options) {
        registrationOptions.put(userHandle, new RegistrationOptionsEntry(options, Instant.now()));
        log.debug("Registration options stored for userHandle: {}", userHandle);
    }

    public Optional<PublicKeyCredentialCreationOptions> getRegistrationOptions(ByteArray userHandle) {
        RegistrationOptionsEntry entry = registrationOptions.get(userHandle);
        if (entry == null) return Optional.empty();
        if (isExpired(entry.getTimestamp())) {
            registrationOptions.remove(userHandle);
            return Optional.empty();
        }
        return Optional.of(entry.getOptions());
    }

    public void removeRegistrationOptions(ByteArray userHandle) {
        registrationOptions.remove(userHandle);
    }

    // --- Expiration ---
    private boolean isExpired(Instant timestamp) {
        return timestamp.plusSeconds(CHALLENGE_TTL_SECONDS).isBefore(Instant.now());
    }

    @Scheduled(fixedDelay = 60_000)
    public void cleanExpired() {
        registrationChallenges.entrySet().removeIf(e -> isExpired(e.getValue().getTimestamp()));
        loginRequests.entrySet().removeIf(e -> isExpired(e.getValue().getTimestamp()));
    }

    @Value
    private static class ChallengeEntry {
        ByteArray challenge;
        Instant timestamp;
    }

    @Value
    private static class AssertionRequestEntry {
        AssertionRequest request;
        Instant timestamp;
    }

    @Value
    private static class RegistrationOptionsEntry {
        PublicKeyCredentialCreationOptions options;
        Instant timestamp;
    }
}