package com.boaz.ticketflow.ticket.event;

import com.boaz.ticketflow.ticket.enums.TicketPriority;
import com.boaz.ticketflow.ticket.enums.TicketStatus;

import java.time.OffsetDateTime;


/**
 * Event published to Kafka topic 'ticket.created' after a new ticket is persisted.
 */
public record TicketCreatedEvent(
    String ticketId,
    String title,
    TicketStatus status,
    TicketPriority priority,
    String creatorId,
    String assigneeId,
    OffsetDateTime createdAt
) {}