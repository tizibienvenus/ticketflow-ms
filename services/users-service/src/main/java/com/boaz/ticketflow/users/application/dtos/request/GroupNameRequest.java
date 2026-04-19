package com.boaz.ticketflow.users.application.dtos.request;

import jakarta.validation.constraints.NotBlank;

public record GroupNameRequest(
    @NotBlank String name
) {}