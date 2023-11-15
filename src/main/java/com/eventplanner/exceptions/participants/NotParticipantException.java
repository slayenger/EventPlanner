package com.eventplanner.exceptions.participants;

import org.aspectj.weaver.ast.Not;

public class NotParticipantException extends RuntimeException{

    public NotParticipantException(String errorMessage, Throwable err)
    {
        super(errorMessage, err);
    }

    public NotParticipantException(String errorMessage)
    {
        super(errorMessage);
    }

}
