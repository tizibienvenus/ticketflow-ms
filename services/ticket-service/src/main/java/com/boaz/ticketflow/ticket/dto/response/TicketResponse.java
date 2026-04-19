package com.boaz.ticketflow.ticket.dto.response;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import com.boaz.ticketflow.ticket.enums.TicketPriority;
import com.boaz.ticketflow.ticket.enums.TicketStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Full representation of a support ticket")
public record TicketResponse(

        @Schema(description = "Unique ticket identifier")
        String id,

        @Schema(description = "Ticket title")
        String title,

        @Schema(description = "Ticket description")
        String description,

        @Schema(description = "Current lifecycle status")
        TicketStatus status,

        @Schema(description = "Priority level")
        TicketPriority priority,

        @Schema(description = "Id of the user who created this ticket")
        String creatorId,

        @Schema(description = "Id of the assigned support agent, if any")
        String assigneeId,

        @Schema(description = "Timestamp of creation")
        OffsetDateTime createdAt,

        @Schema(description = "Timestamp of last update")
        OffsetDateTime updatedAt,

        @Schema(description = "Timestamp when the ticket was resolved, if applicable")
        OffsetDateTime resolvedAt,

        @Schema(description = "Timestamp when the ticket was closed, if applicable")
        OffsetDateTime closedAt,

        @Schema(description = "Comments on this ticket")
        List<CommentResponse> comments,

        @Schema(description = "IDs of documents attached to this ticket")
        Set<String> documentIds
) {}