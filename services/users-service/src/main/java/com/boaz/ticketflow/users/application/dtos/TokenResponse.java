package com.boaz.ticketflow.users.application.dtos;

import java.time.ZonedDateTime;

import org.keycloak.representations.AccessTokenResponse;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {

    @JsonProperty("access_token")
    private String accessToken;
    
    @JsonProperty("refresh_token")
    private String refreshToken;
    
    @JsonProperty("refresh_expires_in")
    private Long refreshExpiresIn;
    
    @JsonProperty("token_type")
    @Builder.Default
    private String tokenType = "Bearer";
    
    @JsonProperty("expires_in")
    private Long expiresIn;
    
    @JsonProperty("user_id")
    private String userId;        // ou un objet UserResponse si vous avez un DTO utilisateur
    
    @JsonProperty("issued_at")
    private ZonedDateTime issuedAt;
    
    @JsonProperty("expires_at")
    private ZonedDateTime expiresAt;

    /**
     * Construit un TokenResponse à partir de la réponse Keycloak, sans les infos utilisateur.
     */
    public static TokenResponse fromAccessTokenResponse(AccessTokenResponse response) {
        ZonedDateTime now = ZonedDateTime.now();
        return TokenResponse.builder()
            .accessToken(response.getToken())
            .refreshToken(response.getRefreshToken())
            .refreshExpiresIn(response.getRefreshExpiresIn())
            .tokenType("Bearer")
            .expiresIn(response.getExpiresIn())
            .issuedAt(now)
            .expiresAt(now.plusSeconds(response.getExpiresIn()))
            .build();
    }

    /**
     * Version avec userId (à appeler après avoir récupéré l'utilisateur).
     */
    public TokenResponse withUserId(String userId) {
        this.userId = userId;
        return this;
    }
}