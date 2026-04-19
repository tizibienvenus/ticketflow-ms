package com.boaz.ticketflow.users.infrastructure.config;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
@Slf4j
public class KeycloakHealthChecker implements CommandLineRunner {

    @Value("${keycloak.auth-server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Override
    public void run(String... args) {
        checkKeycloakConnection();
    }

    private void checkKeycloakConnection() {
        try {
            HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

            String healthUrl = keycloakServerUrl + "/realms/" + realm;
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(healthUrl))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                log.info("✅ Keycloak is accessible at: {}", healthUrl);
                log.info("✅ Realm '{}' is available", realm);
            } else {
                log.warn("⚠️ Keycloak responded with status code: {} for realm: {}", response.statusCode(), realm);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logConnectionFailure(e);
        } catch (IOException e) {
            logConnectionFailure(e);
        }
    }

    private void logConnectionFailure(Exception e) {
        log.error("❌ Failed to connect to Keycloak at: {}/realms/{}", keycloakServerUrl, realm);
        log.error("Error details: {}", e.getMessage());
        log.warn("🔧 Please ensure Keycloak is running and the realm '{}' exists", realm);
        log.warn("🔧 You can start Keycloak using: docker-compose up -d keycloak");
    }
}
