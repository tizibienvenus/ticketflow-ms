package com.boaz.ticketflow.users.domain.model;


import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "otp", indexes = {
    @Index(name = "idx_otp_identifier", columnList = "identifier"),
    @Index(name = "idx_otp_expires_at", columnList = "expires_at")
})
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OtpEntity extends JpaBaseEntity {
    
    @Column(name = "identifier", nullable = false)
    private String identifier;
    
    @Column(name = "code", nullable = false, length = 100)
    private String code;

    @Column(name = "expiration_time")
    private Long expirationTime;
    
    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (expiresAt == null) {
            expiresAt = createdAt.plusMinutes(5);
        }
    }
    
    @Transient
    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }

    @Transient
    @Override
    protected String getPrefix() {
        return "OTP";
    }
}