package com.boaz.ticketflow.ticket.exception;



/**
 * Thrown when a requested ticket does not exist in the database.
 */
public class TicketNotFoundException extends RuntimeException {

    public TicketNotFoundException(String ticketId) {
        super("Ticket not found with id: " + ticketId);
    }
}