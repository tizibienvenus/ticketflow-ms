package com.boaz.ticketflow.common.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableMethodSecurity
@ConditionalOnProperty(
        prefix = "ticketflow.security", 
        name = "enabled", 
        havingValue = "true", 
        matchIfMissing = false
)
public class SecurityConfig {
        private final SecurityProperties properties;
        private final JwtToCustomPrincipalConverter jwtAuthenticationConverter;

        public SecurityConfig(SecurityProperties properties, JwtToCustomPrincipalConverter jwtAuthenticationConverter) {
                this.properties = properties;
                this.jwtAuthenticationConverter = jwtAuthenticationConverter;
        }

        @PostConstruct
        public void init() {
                System.out.println("SecurityConfig is loaded!");
        }

        @Bean
        @ConditionalOnMissingBean
        public JwtDecoder jwtDecoder() {

                if (properties.getJwkSetUri() == null || properties.getJwkSetUri().isBlank()) {
                        throw new IllegalStateException("ticketflow.security.jwk-set-uri must be defined");
                }

                if (properties.getIssuer() == null || properties.getIssuer().isBlank()) {
                        throw new IllegalStateException("ticketflow.security.issuer must be defined");
                }

                NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder
                        .withJwkSetUri(properties.getJwkSetUri())
                        .build();

                OAuth2TokenValidator<Jwt> jwtValidator = new DelegatingOAuth2TokenValidator<>(
                        new JwtTimestampValidator(),
                        new JwtIssuerValidator(properties.getIssuer())
                );

                jwtDecoder.setJwtValidator(jwtValidator);

                return jwtDecoder;
        }

        @Bean
        @ConditionalOnMissingBean(name = "securityFilterChain")
        public SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityProperties securityProperties) throws Exception {
                http
                        .csrf(AbstractHttpConfigurer::disable)
                        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                        .formLogin(AbstractHttpConfigurer::disable)
                        .httpBasic(AbstractHttpConfigurer::disable)
                        .authorizeHttpRequests(authz -> authz
                                .requestMatchers(securityProperties.getPublicPaths().toArray(new String[0])).permitAll()
                                .anyRequest().authenticated()
                        )
                        .oauth2ResourceServer(oauth2 -> oauth2
                                .jwt(jwt -> jwt
                                        .decoder(jwtDecoder())
                                        .jwtAuthenticationConverter(jwtAuthenticationConverter)
                                )
                                .authenticationEntryPoint((request, response, authException) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"))
                        );

                return http.build();
        }

        @Bean
        @ConditionalOnProperty(prefix = "camergo.security", name = "disable-auto-security", havingValue = "true")
        public SecurityFilterChain permissiveSecurityFilterChain(HttpSecurity http) throws Exception {
                http
                        .csrf(AbstractHttpConfigurer::disable)
                        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                        .authorizeHttpRequests(authz -> authz
                                .anyRequest().permitAll()
                        );

                return http.build();
        }
}
