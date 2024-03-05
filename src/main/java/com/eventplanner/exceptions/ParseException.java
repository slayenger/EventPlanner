package com.eventplanner.exceptions;

public class ParseException extends java.text.ParseException {

    public ParseException(String s, int errorOffset) {
        super(s, errorOffset);
    }
}
