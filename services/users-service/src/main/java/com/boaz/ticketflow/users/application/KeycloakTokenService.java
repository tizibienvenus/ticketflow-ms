package com.boaz.ticketflow.users.application;

import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.boaz.ticketflow.common.exceptions.AuthenticationException;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakTokenService {

    private static final String GRANT_TYPE_PASSWORD = "password";
    private static final String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";
    private static final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";
    private static final String GRANT_TYPE_TOKEN_EXCHANGE = "urn:ietf:params:oauth:grant-type:token-exchange";
    private static final String PARAM_CLIENT_ID = "client_id";
    private static final String PARAM_CLIENT_SECRET = "client_secret";
    private static final String PARAM_USERNAME = "username";
    private static final String PARAM_PASSWORD = "password";
    private static final String PARAM_REFRESH_TOKEN = "refresh_token";
    private static final String PARAM_OTP = "otp";
    private static final String PARAM_SCOPE = "scope";
    private static final String PARAM_SUBJECT_TOKEN = "subject_token";
    private static final String PARAM_SUBJECT_TOKEN_TYPE = "subject_token_type";
    private static final String PARAM_REQUESTED_TOKEN_TYPE = "requested_token_type";
    private static final String PARAM_REQUESTED_SUBJECT = "requested_subject";

    @Value("${keycloak.token-uri}")
    private String tokenUri;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @NonNull
    private final RestClient restClient;

    /**
     * Authentifie un utilisateur via WebAuthn / Passkey (passwordless) en utilisant
     * le Token Exchange de Keycloak. Nécessite que le client soit autorisé à
     * échanger son token client contre un token utilisateur.
     *
     * @param username le nom d'utilisateur Keycloak pour lequel générer le token
     * @return AccessTokenResponse contenant le token d'accès et de rafraîchissement
     * @throws AuthenticationException si la requête échoue
     */
    public AccessTokenResponse authenticateWithWebAuthn(String username) {
        String clientToken = getClientToken();
        return exchangeToken(clientToken, username);
    }

    /**
     * Authentifie un utilisateur avec son nom d'utilisateur et mot de passe.
     *
     * @param username le nom d'utilisateur
     * @param password le mot de passe
     * @return la réponse contenant les tokens d'accès et de rafraîchissement
     * @throws AuthenticationException si les identifiants sont invalides
     */
    public AccessTokenResponse authenticateWithPassword(
        String username, 
        String password
    ) {
        MultiValueMap<String, String> form = createBasePasswordForm(username, password);
        return executeGrant(form);
    }

    /**
     * Authentifie un utilisateur avec mot de passe et code OTP.
     *
     * @param username le nom d'utilisateur
     * @param password le mot de passe
     * @param otp      le code OTP
     * @return la réponse contenant les tokens
     * @throws AuthenticationException si les identifiants sont invalides
     */
    public AccessTokenResponse authenticateWithPasswordAndOtp(
        String username, 
        String password, 
        String otp
    ) {
        MultiValueMap<String, String> form = createBasePasswordForm(username, password);
        form.add(PARAM_OTP, otp);
        return executeGrant(form);
    }

    /**
     * Rafraîchit un token d'accès à l'aide d'un refresh token valide.
     *
     * @param refreshToken le refresh token
     * @return une nouvelle réponse contenant les tokens rafraîchis
     * @throws AuthenticationException si le refresh token est invalide
     */
    public AccessTokenResponse refreshToken(String refreshToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add(PARAM_CLIENT_ID, clientId);
        form.add(PARAM_CLIENT_SECRET, clientSecret);
        form.add("grant_type", GRANT_TYPE_REFRESH_TOKEN);
        form.add(PARAM_REFRESH_TOKEN, refreshToken);
        return executeGrant(form);
    }

    /**
     * Obtient un token pour le client (machine-to-machine) via client_credentials.
     */
    private String getClientToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add(PARAM_CLIENT_ID, clientId);
        form.add(PARAM_CLIENT_SECRET, clientSecret);
        form.add("grant_type", GRANT_TYPE_CLIENT_CREDENTIALS);
        form.add(PARAM_SCOPE, "openid"); // optionnel

        AccessTokenResponse response = executeGrant(form);
        if (response == null || response.getToken() == null) {
            throw new AuthenticationException("Failed to obtain client token");
        }
        return response.getToken();
    }

    /**
     * Échange un token client contre un token pour un utilisateur spécifique.
     */
    private AccessTokenResponse exchangeToken(String clientToken, String username) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add(PARAM_CLIENT_ID, clientId);
        form.add(PARAM_CLIENT_SECRET, clientSecret);
        form.add("grant_type", GRANT_TYPE_TOKEN_EXCHANGE);
        form.add(PARAM_SUBJECT_TOKEN, clientToken);
        form.add(PARAM_SUBJECT_TOKEN_TYPE, "urn:ietf:params:oauth:token-type:access_token");
        form.add(PARAM_REQUESTED_TOKEN_TYPE, "urn:ietf:params:oauth:token-type:refresh_token");
        form.add(PARAM_REQUESTED_SUBJECT, username);
        form.add(PARAM_SCOPE, "offline_access");

        return executeGrant(form);
    }

    /**
     * Crée un formulaire de base pour le grant type password.
     */
    private MultiValueMap<String, String> createBasePasswordForm(String username, String password) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add(PARAM_CLIENT_ID, clientId);
        form.add(PARAM_CLIENT_SECRET, clientSecret);
        form.add("grant_type", GRANT_TYPE_PASSWORD);
        form.add(PARAM_USERNAME, username);
        form.add(PARAM_PASSWORD, password);
        return form;
    }

    /**
     * Exécute une requête de grant vers le endpoint token de Keycloak.
     *
     * @param form les paramètres du formulaire
     * @return la réponse de Keycloak
     * @throws AuthenticationException si la requête échoue (identifiants invalides, etc.)
     */
    private AccessTokenResponse executeGrant(MultiValueMap<String, String> form) {
        try {
            AccessTokenResponse response = restClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(AccessTokenResponse.class);

            if (response == null || response.getToken() == null) {
                throw new AuthenticationException("Empty token response from Keycloak");
            }

            return response;

        } catch (HttpClientErrorException e) {
            log.error("Keycloak error [{}]: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AuthenticationException("Authentication failed", e);

        } catch (AuthenticationException e) {
            log.error("Unexpected error during token request", e);
            throw new AuthenticationException("Unexpected authentication error", e);
        }
    }
}