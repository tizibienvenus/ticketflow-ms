package com.boaz.ticketflow.common.security;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AuthenticatedUser extends CustomPrincipal {
    private List<String> roles;
    private List<String> groups;
    private Map<String, Object> extras;
}