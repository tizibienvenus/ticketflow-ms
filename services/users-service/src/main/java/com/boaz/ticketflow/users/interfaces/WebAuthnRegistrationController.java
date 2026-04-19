package com.boaz.ticketflow.users.interfaces;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.boaz.ticketflow.common.security.AuthenticatedUser;
import com.boaz.ticketflow.common.wrappers.BaseResponse;
import com.boaz.ticketflow.users.application.CredentialService;
import com.boaz.ticketflow.users.application.UserService;
import com.boaz.ticketflow.users.application.WebAuthnSessionService;
import com.boaz.ticketflow.users.docs.WebAuthnRegistrationApiDocs;
import com.boaz.ticketflow.users.domain.model.UserEntity;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.AuthenticatorAttestationResponse;
import com.yubico.webauthn.data.AuthenticatorSelectionCriteria;
import com.yubico.webauthn.data.AuthenticatorTransport;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.ClientRegistrationExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.data.ResidentKeyRequirement;
import com.yubico.webauthn.data.UserIdentity;
import com.yubico.webauthn.data.UserVerificationRequirement;
import com.yubico.webauthn.exception.RegistrationFailedException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth/webauthn/register")
@RequiredArgsConstructor
public class WebAuthnRegistrationController implements WebAuthnRegistrationApiDocs{

        private final RelyingParty relyingParty;
        private final UserService userService;
        private final WebAuthnSessionService sessionService;
        private final CredentialService credentialService;

        @Override
        @PostMapping("/start")
        public ResponseEntity<PublicKeyCredentialCreationOptions> startRegistration(
                @AuthenticationPrincipal AuthenticatedUser jwtUser
        ) {

                UserEntity appUser = userService.getUserById(jwtUser.getId());

                // Générer un user handle (l'UUID de l'utilisateur)
                ByteArray userHandle = new ByteArray(appUser.getId().getBytes());

                // Options d'enregistrement
                StartRegistrationOptions options = StartRegistrationOptions.builder()
                        .user(UserIdentity.builder()
                                .name(appUser.getIdentifier())
                                .displayName(appUser.getPhone())
                                .id(userHandle)
                                .build())
                        .authenticatorSelection(AuthenticatorSelectionCriteria.builder()
                                .residentKey(ResidentKeyRequirement.PREFERRED)
                                .userVerification(UserVerificationRequirement.PREFERRED)
                                .build())
                        .build();

                PublicKeyCredentialCreationOptions registrationOptions = relyingParty.startRegistration(options);

                // Stocker le challenge
                sessionService.storeRegistrationChallenge(userHandle, registrationOptions.getChallenge());

                return ResponseEntity.ok(registrationOptions);
        }

        @Override
        @PostMapping("/complete")
        public ResponseEntity<?> completeRegistration(
                @AuthenticationPrincipal AuthenticatedUser jwtUser,
                @RequestBody PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> credential
        ) {

                UserEntity appUser = userService.getUserById(jwtUser.getId());
                ByteArray userHandle = new ByteArray(appUser.getId().getBytes());

                // Récupérer les options de création stockées
                PublicKeyCredentialCreationOptions requestOptions = sessionService.getRegistrationOptions(userHandle)
                        .orElseThrow(() -> new IllegalArgumentException("Session expirée ou introuvable"));

                FinishRegistrationOptions options = FinishRegistrationOptions.builder()
                        .request(requestOptions)
                        .response(credential)
                        .build();

                try {
                        
                        RegistrationResult result = relyingParty.finishRegistration(options);

                        SortedSet<AuthenticatorTransport> transportsEnum = result.getKeyId()
                                .getTransports()
                                .orElse(new TreeSet<>());
                        
                        Set<String> transports = transportsEnum.stream()
                                .map(AuthenticatorTransport::getId)
                                .collect(Collectors.toSet());

                        // Sauvegarde du credential
                        credentialService.saveCredential(
                                result.getKeyId().getId(),
                                appUser,
                                result.getPublicKeyCose(),
                                result.getSignatureCount(),
                                transports
                        );

                        // Nettoyer la session
                        sessionService.removeRegistrationChallenge(userHandle);
                        sessionService.removeRegistrationOptions(userHandle);

                        return ResponseEntity.ok().build();

                } catch (RegistrationFailedException e) {
                        log.error("Registration failed", e);
                        return ResponseEntity.badRequest().body(BaseResponse.error("Échec de l'enregistrement : " + e.getMessage()));
                }
        }
}