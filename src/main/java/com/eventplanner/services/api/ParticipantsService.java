package com.eventplanner.services.api;

import com.eventplanner.dtos.ParticipantsRequestDTO;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public interface ParticipantsService {

    ResponseEntity<?> addParticipantToEvent(ParticipantsRequestDTO participantsRequestDTO);

    ResponseEntity<?> getEventParticipants(UUID eventId);

    ResponseEntity<?> removeParticipantFromEvent(ParticipantsRequestDTO participantsRequestDTO);

    Boolean isUserParticipant(UUID eventId, UUID userId);

    ResponseEntity<?> removeAllParticipantsFromEvent(UUID eventId);
}
