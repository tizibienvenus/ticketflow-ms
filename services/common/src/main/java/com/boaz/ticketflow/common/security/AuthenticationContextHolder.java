package com.boaz.ticketflow.common.security;

import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;

import org.springframework.security.core.Authentication;

@Slf4j
public class AuthenticationContextHolder {

    private AuthenticationContextHolder() {}

    /*public static AuthenticatedUser getCurrentUser() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return extractUserFromJwt(jwt);
    }*/

    public static AuthenticatedUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthenticatedUser user) {
            return user;
        }
        throw new TokenValidationException("No authenticated user found");
    }

    public static AuthenticatedUser extractUserFromJwt(Jwt jwt) {
        Map<String, Object> claims = jwt.getClaims();

        List<String> roles = extractRoles(claims);

        List<String> groups = extractGroups(claims);

        return AuthenticatedUser.builder()
            .id(jwt.getClaimAsString("database_id"))
            .keycloakId(jwt.getSubject())
            .username(jwt.getClaimAsString("preferred_username"))
            .identifier(jwt.getClaimAsString("email"))
            .firstname(jwt.getClaimAsString("given_name"))
            .lastname(jwt.getClaimAsString("family_name"))
            .roles(roles)
            .groups(groups)
            .extras(extractCustomAttributes(claims))
            .build();
    }

    @SuppressWarnings("unchecked")
    public static List<String> extractRoles(Map<String, Object> claims) {
        List<String> roles = new ArrayList<>();

        Map<String, Object> realmAccess = (Map<String, Object>) claims.get("realm_access");
        if (realmAccess != null) {
            List<String> realmRoles = (List<String>) realmAccess.get("roles");
            if (realmRoles != null) {
                roles.addAll(realmRoles);
            }
        }

        return roles.stream()
            .distinct()
            .filter(role -> !role.startsWith("default-roles-") && !role.equals("offline_access") && !role.equals("uma_authorization"))
            .map(role -> "ROLE_" + role.toUpperCase())
            .toList();
    }

    @SuppressWarnings("unchecked")
    private static List<String> extractGroups(Map<String, Object> claims) {
        Object groupsClaim = claims.get("groups");
        if (groupsClaim instanceof List) {
            return (List<String>) groupsClaim;
        }
        return new ArrayList<>();
    }

    private static Map<String, Object> extractCustomAttributes(Map<String, Object> claims) {
        Map<String, Object> customAttributes = new HashMap<>();

        Set<String> standardClaims = Set.of(
            "sid", "email_verified", "name"
        );

        claims.entrySet().stream()
            .filter(entry -> standardClaims.contains(entry.getKey()))
            .forEach(entry -> customAttributes.put(entry.getKey(), entry.getValue()));

        return customAttributes;
    }
}
