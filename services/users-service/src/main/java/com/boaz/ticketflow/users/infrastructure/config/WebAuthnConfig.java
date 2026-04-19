package com.boaz.ticketflow.users.infrastructure.config;

import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.boaz.ticketflow.users.infrastructure.persistence.CredentialStorage;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.RelyingPartyIdentity;

@Configuration
public class WebAuthnConfig {

    @Value("${webauthn.relying-party.id:localhost}")   // ex: "example.com"
    private String rpId;

    @Value("${webauthn.relying-party.name:camergo}") // ex: "Mon Application"
    private String rpName;

    @Value("${webauthn.origin:http://localhost:8080}")              // ex: "https://api.example.com"
    private String origin;

    @Bean
    public RelyingParty relyingParty(CredentialStorage credentialStorage) {
        return RelyingParty.builder()
            .identity(RelyingPartyIdentity.builder()
                .id(rpId)
                .name(rpName)
                .build())
            .credentialRepository(credentialStorage)
            .origins(Set.of(origin))
            .build();
    }
}