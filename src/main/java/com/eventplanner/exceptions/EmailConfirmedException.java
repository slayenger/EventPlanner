package com.eventplanner.exceptions;

public class EmailConfirmedException extends RuntimeException{
    public EmailConfirmedException(String message) {
        super(message);
    }

    public EmailConfirmedException(String message, Throwable cause) {
        super(message, cause);
    }
}
