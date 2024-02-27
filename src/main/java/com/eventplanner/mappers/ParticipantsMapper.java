package com.eventplanner.mappers;

import com.eventplanner.dtos.ParticipantRequestDTO;
import org.springframework.stereotype.Component;

import java.util.UUID;

//TODO не делать этот класс бином(?)
@Component
public class ParticipantsMapper {

   public ParticipantRequestDTO toDTO(UUID eventId, UUID participantId)
   {
       ParticipantRequestDTO participantRequestDTO = new ParticipantRequestDTO();
       participantRequestDTO.setEventId(eventId);
       participantRequestDTO.setParticipantId(participantId);

       return participantRequestDTO;
   }

}
