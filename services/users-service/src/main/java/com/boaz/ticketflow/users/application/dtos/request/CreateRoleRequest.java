package com.boaz.ticketflow.users.application.dtos.request;

import jakarta.validation.constraints.NotBlank;

public record CreateRoleRequest(
    @NotBlank String name,
    String description,
    boolean composite
) {}
