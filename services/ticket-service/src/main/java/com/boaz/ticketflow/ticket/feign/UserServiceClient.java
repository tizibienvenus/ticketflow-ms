package com.boaz.ticketflow.ticket.feign;

import com.boaz.ticketflow.ticket.feign.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;



/**
 * Declarative Feign client for communication with user-service.
 * JWT is propagated automatically via {@link FeignJwtRequestInterceptor}.
 *
 * Circuit breaker and retry are configured in application.yml under
 * resilience4j.circuitbreaker.instances.user-service.
 */
/* @FeignClient(
    name = "user-service",
    fallbackFactory = UserServiceClientFallbackFactory.class
)
public interface UserServiceClient {

    @GetMapping("/api/users/{id}")
    UserResponse getUserById(@PathVariable("id") String userId);
} */