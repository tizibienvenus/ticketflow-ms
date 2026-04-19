package com.boaz.ticketflow.common.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CustomPrincipal {
    private String id;
    private String keycloakId;
    private String username;
    private String identifier;
    private String firstname;
    private String lastname;
}
