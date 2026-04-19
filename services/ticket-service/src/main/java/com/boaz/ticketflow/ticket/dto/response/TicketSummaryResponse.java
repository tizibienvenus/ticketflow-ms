package com.boaz.ticketflow.ticket.dto.response;

import com.boaz.ticketflow.ticket.enums.TicketPriority;
import com.boaz.ticketflow.ticket.enums.TicketStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "Summary of a ticket for list views")
public record TicketSummaryResponse(

        String id,
        String title,
        TicketStatus status,
        TicketPriority priority,
        String creatorId,
        String assigneeId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        int commentCount
) {}