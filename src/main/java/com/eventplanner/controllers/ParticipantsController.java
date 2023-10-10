package com.eventplanner.controllers;


import com.eventplanner.dtos.ParticipantsRequestDTO;
import com.eventplanner.services.api.ParticipantsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/participants")
public class ParticipantsController {

    private final ParticipantsService participantsService;

    @GetMapping("/event/{eventId}")
    public ResponseEntity<?> getEventParticipants(@PathVariable UUID eventId)
    {
        return participantsService.getEventParticipants(eventId);
    }


    @PostMapping
    public ResponseEntity<?> addParticipantToEvent(@RequestBody ParticipantsRequestDTO participantsRequestDTO)
    {
        return participantsService.addParticipantToEvent(participantsRequestDTO);
    }

    @DeleteMapping
    public ResponseEntity<?> removeParticipantFromEvent(@RequestBody ParticipantsRequestDTO participantsRequestDTO)
    {
        return participantsService.removeParticipantFromEvent(participantsRequestDTO);
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<?> removeAllParticipantsFromEvent(@PathVariable UUID eventId)
    {
        return participantsService.removeAllParticipantsFromEvent(eventId);
    }

    @GetMapping("/generate_link")
    public ResponseEntity<?> generateInvitationLink(@RequestBody ParticipantsRequestDTO requestDTO, Authentication authentication)
    {
        return participantsService.generateInvitationLink(requestDTO, authentication);
    }

}
