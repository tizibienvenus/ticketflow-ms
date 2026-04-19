package com.boaz.ticketflow.common.exceptions;

public class RessourceNotFoundException extends RuntimeException {
    public RessourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public RessourceNotFoundException(String message) {
        super(message);
    }
}
