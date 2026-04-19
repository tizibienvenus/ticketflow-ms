package com.boaz.ticketflow.ticket.feign.dto;

import java.util.List;
import java.util.UUID;

/**
 * Minimal representation of a user returned by user-service.
 * Only the fields needed by ticket-service are mapped here.
 */
public record UserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        List<String> roles
) {}