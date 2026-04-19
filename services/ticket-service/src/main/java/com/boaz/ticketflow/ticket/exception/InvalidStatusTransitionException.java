package com.boaz.ticketflow.ticket.exception;

import com.boaz.ticketflow.ticket.enums.TicketStatus;

/**
 * Thrown when an illegal ticket status transition is attempted.
 */
public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(TicketStatus current, TicketStatus target) {
        super("Cannot transition ticket from %s to %s".formatted(current, target));
    }
}