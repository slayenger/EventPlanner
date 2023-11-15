package com.eventplanner.exceptions.participants;

public class UserIsParticipantException extends RuntimeException{
    public UserIsParticipantException(String message) {
        super(message);
    }

    public UserIsParticipantException(String message, Throwable cause) {
        super(message, cause);
    }
}
