package com.boaz.ticketflow.common.security;

import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtToCustomPrincipalConverter implements Converter<Jwt, UsernamePasswordAuthenticationToken> {

    @Override
    public UsernamePasswordAuthenticationToken convert(@NonNull Jwt jwt) {
        AuthenticatedUser principal = AuthenticationContextHolder.extractUserFromJwt(jwt);

        if (principal == null) {
            String errorMessage = "User with ID " + jwt.getSubject() + " not found";
            log.error(errorMessage);
            throw new UsernameNotFoundException(errorMessage);
        }

        Collection<? extends GrantedAuthority> authorities = AuthenticationContextHolder
            .extractRoles(jwt.getClaims())
            .stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());

        log.info("The user {} is authenticated with roles {}", principal.getId(), authorities);

        return new UsernamePasswordAuthenticationToken(principal, jwt, authorities);
    }
}