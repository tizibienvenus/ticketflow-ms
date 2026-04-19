package com.boaz.ticketflow.users.application;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.boaz.ticketflow.common.domain.EmailTemplates;
import com.boaz.ticketflow.common.domain.NotificationType;
import com.boaz.ticketflow.common.dtos.EmailRequest;
import com.boaz.ticketflow.common.dtos.NotificationRequest;
import com.boaz.ticketflow.common.dtos.SmsRequest;
import com.boaz.ticketflow.users.application.dtos.TokenResponse;
import com.boaz.ticketflow.users.infrastructure.kafka.NotificationKafkaProducer;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final OtpService otpService;
    private final KeycloakService keycloakService;
    private final NotificationKafkaProducer kafkaProducer; 
    private final KeycloakTokenService keycloakTokenService;

    public TokenResponse loginWithCredentials(String username, String password) {
        return TokenResponse.fromAccessTokenResponse(keycloakTokenService.authenticateWithWebAuthn(username));
    }

    public TokenResponse refreshToken(String refreshToken) {
        return TokenResponse.fromAccessTokenResponse(keycloakTokenService.refreshToken(refreshToken));
    }

    public void requestOTP(String identifier) {
        // 1. Générer et sauvegarder l'OTP
        String otpCode = otpService.generateAndSaveOTP(identifier, 6, 5L);

        // 2. Construire la requête de notification
        NotificationRequest request = buildOtpRequest(identifier, otpCode);

        // 3. Envoyer à Kafka via le producer existant
        kafkaProducer.sendNotifications(request);
    }

    public TokenResponse loginWithOtp(String identifier, String code) {
        boolean valided = otpService.validateOtp(identifier, code);
        
        if (!valided) {
            throw new SecurityException("Invalid or expired OTP");
        }

        keycloakService.createUser(identifier);

        return TokenResponse.fromAccessTokenResponse(keycloakTokenService.authenticateWithWebAuthn(identifier));

    }

    private NotificationRequest buildOtpRequest(String identifier, String otpCode) {
        // Détection simple du type (email si @, sinon SMS)
        if (identifier.contains("@")) {
            return EmailRequest.builder()
                .recipient(identifier)
                .type(NotificationType.EMAIL) // enum partagée
                .subject("Votre code de vérification")
                .templateName(EmailTemplates.OTP_EMAIL.getTemplate())
                .templateData(Map.of("otpCode", otpCode))
                .build();
        } else {
            return SmsRequest.builder()
                .recipient(identifier)
                .type(NotificationType.SMS)
                .message("Your verification code is: " + otpCode + ". It will expire in 5 minutes.")
                .build();
        }
    }
}
