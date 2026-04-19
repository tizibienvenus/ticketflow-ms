package com.boaz.ticketflow.users.interfaces;

import java.util.Optional;

import org.keycloak.representations.AccessTokenResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boaz.ticketflow.users.application.CredentialService;
import com.boaz.ticketflow.users.application.KeycloakTokenService;
import com.boaz.ticketflow.users.application.UserService;
import com.boaz.ticketflow.users.application.WebAuthnSessionService;
import com.boaz.ticketflow.users.application.dtos.TokenResponse;
import com.boaz.ticketflow.users.docs.WebAuthnLoginApiDocs;
import com.boaz.ticketflow.users.domain.model.UserEntity;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.FinishAssertionOptions;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartAssertionOptions;
import com.yubico.webauthn.data.AuthenticatorAssertionResponse;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.ClientAssertionExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.PublicKeyCredentialRequestOptions;
import com.yubico.webauthn.exception.AssertionFailedException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth/webauthn/login")
@RequiredArgsConstructor
public class WebAuthnLoginController implements WebAuthnLoginApiDocs{

    private final RelyingParty relyingParty;
    private final UserService userService;
    private final WebAuthnSessionService sessionService;
    private final CredentialService credentialService;
    private final KeycloakTokenService keycloakTokenService;

    @Override
    @PostMapping("/start")
    public ResponseEntity<PublicKeyCredentialRequestOptions> startLogin(
        @RequestParam(required = false) String identifier
    ) {

        StartAssertionOptions.StartAssertionOptionsBuilder builder = StartAssertionOptions.builder();

        if (identifier != null && !identifier.isEmpty()) {
            // Restreindre aux credentials de cet utilisateur
            Optional<ByteArray> userHandle = userService.getUserHandleForUsername(identifier);
            builder.username(identifier).userHandle(userHandle.orElse(null));
        }

        AssertionRequest assertionRequest = relyingParty.startAssertion(builder.build());

        // Stocker la requête complète en session avec le challenge comme clé
        ByteArray challenge = assertionRequest.getPublicKeyCredentialRequestOptions().getChallenge();
        sessionService.storeLoginRequest(challenge, assertionRequest);

        return ResponseEntity.ok(assertionRequest.getPublicKeyCredentialRequestOptions());
    }

    @Override
    @PostMapping("/complete")
    public ResponseEntity<?> completeLogin(
        @RequestBody PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> credential
    ) {
        // Extraire le challenge depuis la réponse (le client renvoie le challenge dans la réponse)
        ByteArray challenge = credential.getResponse().getClientData().getChallenge();

        // Récupérer la requête initiale stockée
        AssertionRequest originalRequest = sessionService.getLoginRequest(challenge)
            .orElseThrow(() -> new IllegalArgumentException("Challenge expiré ou introuvable"));

        FinishAssertionOptions options = FinishAssertionOptions.builder()
            .request(originalRequest)
            .response(credential)
            .build();

        try {
            AssertionResult result = relyingParty.finishAssertion(options);

            if (!result.isSuccess()) {
                return ResponseEntity.badRequest().body("Authentification échouée");
            }

            // Mise à jour du compteur de signature
            credentialService.updateSignatureCount(result.getCredential().getCredentialId(), result.getSignatureCount());

            // Récupérer l'utilisateur
            String userId = new String(result.getCredential().getUserHandle().getBytes());
            UserEntity user = userService.getUserById(userId);

            // Générer un nouveau JWT applicatif
            AccessTokenResponse jwt = keycloakTokenService.authenticateWithWebAuthn(user.getIdentifier());

            // Nettoyer la session
            sessionService.removeLoginRequest(challenge);

            return ResponseEntity.ok(TokenResponse.fromAccessTokenResponse(jwt));

        } catch (AssertionFailedException e) {
            log.error("Login failed", e);
            return ResponseEntity.badRequest().body("Authentification échouée : " + e.getMessage());
        }
    }

}
