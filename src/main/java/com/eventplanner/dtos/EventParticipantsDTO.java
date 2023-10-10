package com.eventplanner.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventParticipantsDTO {

    private UUID participantId;
    private UUID eventId;
    private String participantName;


}
