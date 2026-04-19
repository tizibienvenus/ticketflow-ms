package com.boaz.ticketflow.common.exceptions;

public class RessourceAlreadyExistsException extends RuntimeException {
    public RessourceAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public RessourceAlreadyExistsException(String message) {
        super(message);
    }
}