package com.boaz.ticketflow.users.docs;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.boaz.ticketflow.common.wrappers.BaseResponse;
import com.boaz.ticketflow.users.application.dtos.TokenResponse;
import com.boaz.ticketflow.users.application.dtos.request.RefreshTokenRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Authentication", description = "Endpoints for authentication and token management")
public interface AuthApiDocs {

    @Operation(
        summary = "Login with OTP",
        description = "Authenticates a user using an identifier (email/phone) and a one-time password (OTP). " +
            "Returns access and refresh tokens upon success."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Login successful",
            content = @Content(schema = @Schema(implementation = TokenResponse.class))),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid identifier or OTP",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "User not found",
            content = @Content
        )
    })
    @PostMapping("/login")
    ResponseEntity<TokenResponse> login(
        @Parameter(description = "User identifier (email or phone number)", example = "user@example.com", required = true)
        @RequestParam String identifier,
        @Parameter(description = "One-time password received via email or SMS", example = "123456", required = true)
        @RequestParam String code
    );

    @Operation(
        summary = "Request OTP",
        description = "Sends a one-time password (OTP) to the given identifier (email or phone). " +
            "The OTP can then be used to log in."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "OTP sent successfully",
            content = @Content(schema = @Schema(implementation = BaseResponse.class))),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid identifier or unsupported delivery method",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "429", 
            description = "Too many OTP requests, please wait",
            content = @Content
        )
    })
    @PostMapping("/otp/request")
    ResponseEntity<BaseResponse<String>> requestOtp(
        @Parameter(description = "Identifier (email or phone number) to send OTP to", example = "+1234567890", required = true)
        @RequestParam String identifier
    );

    @Operation(
        summary = "Refresh access token",
        description = "Obtains a new access token using a valid refresh token."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "New tokens generated",
            content = @Content(schema = @Schema(implementation = TokenResponse.class))),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid refresh token or request body",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Refresh token expired or revoked",
            content = @Content
        )
    })
    @PostMapping("/refresh")
    ResponseEntity<TokenResponse> refresh(
        @Parameter(description = "Refresh token request payload", required = true)
        @Valid @RequestBody RefreshTokenRequest request
    );
}