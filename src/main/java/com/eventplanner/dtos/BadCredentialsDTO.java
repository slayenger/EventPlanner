package com.eventplanner.dtos;

import lombok.Data;

import java.util.Date;

@Data
public class BadCredentialsDTO {

    private int status;
    private String message;
    private Date timestamp;

    public BadCredentialsDTO(int status, String message)
    {
        this.status = status;
        this.message = message;
        this.timestamp = new Date();
    }

}
