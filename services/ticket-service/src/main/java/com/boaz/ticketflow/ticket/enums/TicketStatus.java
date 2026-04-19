package com.boaz.ticketflow.ticket.enums;


/**
 * Lifecycle states of a support ticket.
 * Transitions are strictly enforced: OPEN → IN_PROGRESS → RESOLVED → CLOSED.
 */
public enum TicketStatus {
    OPEN,
    IN_PROGRESS,
    RESOLVED,
    CLOSED;

    /**
     * Validates whether a transition from the current state to {@code next} is allowed.
     *
     * @param next the target status
     * @return true if the transition is valid
     */
    public boolean canTransitionTo(TicketStatus next) {
        return switch (this) {
            case OPEN        -> next == IN_PROGRESS;
            case IN_PROGRESS -> next == RESOLVED;
            case RESOLVED    -> next == CLOSED;
            case CLOSED      -> false;
        };
    }
}