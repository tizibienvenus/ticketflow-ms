package com.boaz.ticketflow.ticket.exception;



/**
 * Thrown when a requested comment does not exist.
 */
public class CommentNotFoundException extends RuntimeException {

    public CommentNotFoundException(String commentId) {
        super("Comment not found with id: " + commentId);
    }
}