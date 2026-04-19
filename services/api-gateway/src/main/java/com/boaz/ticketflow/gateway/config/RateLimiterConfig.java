package com.boaz.ticketflow.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.Principal;

@Configuration
public class RateLimiterConfig {

    @Bean
    public KeyResolver hybridKeyResolver() {
        return exchange -> {
            String deviceId = exchange.getRequest()
                .getHeaders()
                .getFirst("X-Device-Id");

            return exchange.getPrincipal()
                .map(Principal::getName)
                .map(user -> user + ":" + deviceId)
                .defaultIfEmpty("anonymous:" + deviceId);
        };
    }
}