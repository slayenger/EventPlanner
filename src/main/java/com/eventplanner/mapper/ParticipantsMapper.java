package com.eventplanner.mapper;

import com.eventplanner.dtos.ParticipantDTO;
import com.eventplanner.entities.EventParticipants;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ParticipantsMapper {

   public ParticipantDTO toDTO(UUID eventId, UUID participantId)
   {
       ParticipantDTO participantDTO = new ParticipantDTO();
       participantDTO.setEventId(eventId);
       participantDTO.setUserId(participantId);

       return participantDTO;
   }

}
