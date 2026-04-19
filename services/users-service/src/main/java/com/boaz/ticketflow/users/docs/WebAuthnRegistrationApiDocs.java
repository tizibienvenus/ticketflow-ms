package com.boaz.ticketflow.users.docs;

import com.yubico.webauthn.data.PublicKeyCredential;
import com.boaz.ticketflow.common.security.AuthenticatedUser;
import com.boaz.ticketflow.common.wrappers.BaseResponse;
import com.yubico.webauthn.data.AuthenticatorAttestationResponse;
import com.yubico.webauthn.data.ClientRegistrationExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "WebAuthn Registration", description = "Endpoints for registering a WebAuthn authenticator (e.g., biometrics, security keys)")
@SecurityRequirement(name = "BearerAuth") // Assumes you have a security scheme defined globally
public interface WebAuthnRegistrationApiDocs {

    @Operation(
        summary = "Start WebAuthn registration",
        description = "Generates a PublicKeyCredentialCreationOptions object for the authenticated user. " +
                      "The client must use this object to create a new credential via the WebAuthn API."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Registration options successfully generated",
                     content = @Content(schema = @Schema(implementation = PublicKeyCredentialCreationOptions.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized – missing or invalid JWT token",
                     content = @Content),
        @ApiResponse(responseCode = "404", description = "User not found",
                     content = @Content)
    })
    @PostMapping("/start")
    ResponseEntity<PublicKeyCredentialCreationOptions> startRegistration(
        @Parameter(description = "Authenticated user details (injected from security context)", hidden = true)
        @AuthenticationPrincipal AuthenticatedUser jwtUser
    );

    @Operation(
        summary = "Complete WebAuthn registration",
        description = "Validates and finalizes the WebAuthn registration using the credential created by the client. " +
                      "On success, the credential is stored and associated with the user."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Registration completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid credential or expired session",
                     content = @Content(schema = @Schema(implementation = BaseResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized – missing or invalid JWT token",
                     content = @Content),
        @ApiResponse(responseCode = "404", description = "User not found or session data missing",
                     content = @Content)
    })
    @PostMapping("/complete")
    ResponseEntity<?> completeRegistration(
        @Parameter(description = "Authenticated user details (injected from security context)", hidden = true)
        @AuthenticationPrincipal AuthenticatedUser jwtUser,

        @Parameter(description = "The PublicKeyCredential object returned by the WebAuthn API during registration",
                   required = true)
        @RequestBody PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> credential
    );
}