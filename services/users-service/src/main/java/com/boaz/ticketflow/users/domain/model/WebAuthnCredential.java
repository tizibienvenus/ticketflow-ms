package com.boaz.ticketflow.users.domain.model;

import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import com.boaz.ticketflow.users.converter.StringSetConverter;

import jakarta.persistence.Convert;
import jakarta.persistence.*;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "webauthn_credential")
public class WebAuthnCredential {
    @Id
    private String credentialId;    // base64url

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
    
    private String publicKeyCose;   // stockage en base64
    private long signatureCount;
    @Convert(converter = StringSetConverter.class)
    private Set<String> transports;
    private boolean isBackupEligible;
    private boolean isBackupState;
    private Instant created;
    private Instant lastUsed;
}