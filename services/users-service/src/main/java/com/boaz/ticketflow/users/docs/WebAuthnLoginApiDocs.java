package com.boaz.ticketflow.users.docs;

import com.boaz.ticketflow.users.application.dtos.TokenResponse;
import com.yubico.webauthn.data.AuthenticatorAssertionResponse;
import com.yubico.webauthn.data.ClientAssertionExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.PublicKeyCredentialRequestOptions;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "WebAuthn Login", description = "Endpoints for authenticating with WebAuthn (biometrics, security keys)")
public interface WebAuthnLoginApiDocs {

    @Operation(
        summary = "Start WebAuthn login",
        description = "Generates a PublicKeyCredentialRequestOptions object. " +
                      "If an identifier (email/phone) is provided, the challenge is restricted to credentials belonging to that user. " +
                      "Otherwise, a general challenge is generated and the user will be identified by the authenticator during the assertion."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login options successfully generated",
                     content = @Content(schema = @Schema(implementation = PublicKeyCredentialRequestOptions.class))),
        @ApiResponse(responseCode = "400", description = "Invalid identifier provided",
                     content = @Content),
        @ApiResponse(responseCode = "404", description = "User not found for the given identifier",
                     content = @Content)
    })
    @PostMapping("/start")
    ResponseEntity<PublicKeyCredentialRequestOptions> startLogin(
        @Parameter(description = "Optional user identifier (email or phone). If provided, the login challenge will be tailored to that user's registered credentials.", example = "user@example.com")
        @RequestParam(required = false) String identifier
    );

    @Operation(
        summary = "Complete WebAuthn login",
        description = "Validates the authenticator's response against the previously generated challenge. " +
                      "On success, returns a JWT access token. On failure, returns an error message."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful, returns a JWT token",
                     content = @Content(schema = @Schema(implementation = TokenResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid authenticator response or expired challenge",
                     content = @Content(schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "404", description = "User or credential not found",
                     content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error during authentication",
                     content = @Content)
    })
    @PostMapping("/complete")
    ResponseEntity<?> completeLogin(
        @Parameter(description = "The PublicKeyCredential object returned by the WebAuthn API during authentication", required = true)
        @RequestBody PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> credential
    );
}