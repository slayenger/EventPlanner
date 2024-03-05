package com.eventplanner.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MailStructure {

    private String subject;
    private String message;
}
