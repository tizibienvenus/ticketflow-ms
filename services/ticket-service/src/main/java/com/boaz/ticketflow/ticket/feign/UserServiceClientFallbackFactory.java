package com.boaz.ticketflow.ticket.feign;

import com.boaz.ticketflow.ticket.feign.dto.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Fallback factory for {@link UserServiceClient}.
 * Returns a degraded response instead of propagating upstream failures.
 * The caller (TicketService) decides how to handle a null/degraded user response.
 */
/* @Component
@Slf4j
public class UserServiceClientFallbackFactory implements FallbackFactory<UserServiceClient> {

    @Override
    public UserServiceClient create(Throwable cause) {
        return new UserServiceClient() {
            @Override
            public UserResponse getUserById(String userId) {
                log.warn("[FALLBACK] user-service unavailable for userId={}, cause: {}",
                    userId, cause.getMessage());
                return null;
            }
        };
    }
} */