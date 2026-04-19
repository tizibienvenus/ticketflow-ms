package com.boaz.ticketflow.ticket.exception;



/**
 * Thrown when the provided assignee ID cannot be validated against user-service.
 */
public class AssigneeValidationException extends RuntimeException {

    public AssigneeValidationException(String assigneeId) {
        super("Assignee with id [%s] could not be validated. User may not exist or user-service is unavailable."
                .formatted(assigneeId));
    }

    public AssigneeValidationException(String assigneeId, Throwable cause) {
        super("Assignee with id [%s] could not be validated.".formatted(assigneeId), cause);
    }
}