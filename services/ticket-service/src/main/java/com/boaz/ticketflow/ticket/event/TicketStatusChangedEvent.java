package com.boaz.ticketflow.ticket.event;

import com.boaz.ticketflow.ticket.enums.TicketStatus;

import java.time.OffsetDateTime;

/**
 * Event published to Kafka topic 'ticket.status.changed' on every status transition.
 */
public record TicketStatusChangedEvent(
    String ticketId,
    String ticketTitle,
    TicketStatus previousStatus,
    TicketStatus newStatus,
    String changedBy,
    String creatorId,
    String assigneeId,
    OffsetDateTime changedAt
) {}