package com.boaz.ticketflow.common.security;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "ticketflow.security")
public class SecurityProperties {
    private Boolean enabled = false;
    private String serverUrl;
    private String realm;
    private String clientId;
    private String clientSecret;
    private String jwkSetUri;
    private String issuer;
    private int tokenValidationCacheTtl = 300;
    // To set the default security filter chain to permitAll
    private boolean disableAutoSecurity = false;
    private List<String> publicPaths = Arrays.asList(
        "/public/**", 
        "/health/**", 
        "/actuator/**", 
        "/error",
        "/v3/api-docs",
        "api/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/springwolf/**",
        "/asyncapi-ui.html",

        "/api/v1/auth/**",

        // to desativate
        "/api/v1/notifications/**",
        "/api/services/**",
        "/api/v1/geo/**",
        "/api/v1/config/**",
        "/api/v1/documents/**"        
    );

    // add path for ip filtering(each ip can have many paths mapping Map and list of allowed ips for each path)
    private Map<String, List<String>> ipFilters = new HashMap<>();
    
}
