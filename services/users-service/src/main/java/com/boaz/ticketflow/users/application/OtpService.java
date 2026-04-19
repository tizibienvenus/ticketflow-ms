package com.boaz.ticketflow.users.application;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boaz.ticketflow.users.domain.model.OtpEntity;
import com.boaz.ticketflow.users.domain.port.outbound.OtpRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private static final SecureRandom OTP_RANDOM = new SecureRandom();
    
    private final OtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public boolean validateOtp(String identifier, String code) {
        Optional<OtpEntity> otpOpt = otpRepository.findByIdentifier(identifier);
        
        if (otpOpt.isEmpty()) {
            return false;
        }
        
        OtpEntity otp = otpOpt.get();
        
        // Vérifier si expiré
        if (otp.isExpired()) {
            otpRepository.delete(otp); // Supprimer s'il est expiré
            return false;
        }

        // Vérifier le code
        if (!passwordEncoder.matches(code, otp.getCode()) || otp.isExpired()) {
            return false;
        }
        
        // Code valide, supprimer l'OTP
        otpRepository.delete(otp);
        return true;
    }


    @Scheduled(cron = "0 0 * * * *") // Toutes les heures
    @Transactional
    public void cleanupExpiredOtps() {
        LocalDateTime now = LocalDateTime.now();
        int deletedCount = otpRepository.deleteByExpiresAtBefore(now);
        log.info("Deleted {} expired OTPs", deletedCount);
    }

    @Transactional
    public String generateAndSaveOTP(
        String identifier,
        int length,
        Long expirationTime
    ) {
        String code = randomCode(length);
        OtpEntity otp = OtpEntity.builder()
            .identifier(identifier)
            .code(passwordEncoder.encode(code))
            .expirationTime(expirationTime)
            .createdAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusMinutes(expirationTime + 1))
            .build();

        otpRepository.deleteByIdentifier(identifier); // Supprime l'ancien OTP
        otpRepository.save(otp);
        return code;
    }

    public static String randomCode(int length) {
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int randomDigit = OTP_RANDOM.nextInt(10);
            sb.append(randomDigit);
        }
        return sb.toString();
    }

}
