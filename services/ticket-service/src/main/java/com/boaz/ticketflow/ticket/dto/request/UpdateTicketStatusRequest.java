package com.boaz.ticketflow.ticket.dto.request;

import com.boaz.ticketflow.ticket.enums.TicketStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Payload for changing a ticket's status")
public record UpdateTicketStatusRequest(

    @NotNull(message = "Status is required")
    @Schema(description = "Target status. Transitions: OPEN → IN_PROGRESS → RESOLVED → CLOSED")
    TicketStatus status

) {}