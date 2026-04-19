package com.boaz.ticketflow.ticket.dto.request;

import com.boaz.ticketflow.ticket.enums.TicketPriority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload for creating a new support ticket")
public record CreateTicketRequest(

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
    @Schema(description = "Short title describing the issue", example = "Login page broken on Safari")
    String title,

    @NotBlank(message = "Description is required")
    @Size(min = 10, message = "Description must be at least 10 characters")
    @Schema(description = "Detailed description of the issue")
    String description,

    @Schema(description = "Priority level of the ticket", defaultValue = "MEDIUM")
    TicketPriority priority,

    @Schema(description = "String of the user to assign this ticket to")
    String assigneeId
) {
    public CreateTicketRequest {
        if (priority == null) priority = TicketPriority.MEDIUM;
    }
}