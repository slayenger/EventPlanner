package com.eventplanner.exceptions.participants;

public class InvalidLinkException extends RuntimeException{
    public InvalidLinkException(String message) {
        super(message);
    }

    public InvalidLinkException(String message, Throwable cause) {
        super(message, cause);
    }
}
