package com.eventplanner.controllers;


import com.eventplanner.dtos.CustomUserDetailsDTO;
import com.eventplanner.dtos.EventParticipantsDTO;
import com.eventplanner.dtos.ParticipantDTO;
import com.eventplanner.exceptions.EmptyListException;
import com.eventplanner.exceptions.NotFoundException;
import com.eventplanner.exceptions.participants.NotParticipantException;
import com.eventplanner.exceptions.participants.UserIsParticipantException;
import com.eventplanner.services.api.ParticipantsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/participants")
public class ParticipantsController {

    private final ParticipantsService participantsService;

    @GetMapping("/event/{eventId}")
    public ResponseEntity<?> getEventParticipants(@PathVariable UUID eventId,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "10") int size)
    {
        try
        {
            Page<EventParticipantsDTO> participantsDTO =
                    participantsService.getEventParticipants(eventId, page, size);
            return ResponseEntity.status(HttpStatus.OK).body(participantsDTO);
        }
        catch (NotFoundException err)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err.getMessage());
        }
        catch (EmptyListException err)
        {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(err.getMessage());
        }
    }


    @PostMapping
    public ResponseEntity<?> addParticipantToEvent(@RequestBody ParticipantDTO participantDTO)
    {
        try
        {
            participantsService.addParticipantToEvent(participantDTO);
            return ResponseEntity.status(HttpStatus.OK).body("The participant was successfully added");
        }
        catch (UserIsParticipantException err)
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(err.getMessage());
        }
        catch (NotFoundException err)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err.getMessage());
        }
    }

    @DeleteMapping
    public ResponseEntity<?> removeParticipantFromEvent(@RequestBody ParticipantDTO participantDTO)
    {
        try
        {
            participantsService.removeParticipantFromEvent(participantDTO);
            return ResponseEntity.status(HttpStatus.OK).body("The participant was successfully deleted");
        }
        catch (NotFoundException err)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err.getMessage());
        }
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<?> removeAllParticipantsFromEvent(@PathVariable UUID eventId)
    {
        try
        {
            participantsService.removeAllParticipantsFromEvent(eventId);
            return ResponseEntity.status(HttpStatus.OK)
                    .body("All participants of the event with an id have been successfully deleted");
        }
        catch (NotFoundException err)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err.getMessage());
        }
    }

    @GetMapping("/generate_link")
    public ResponseEntity<?> generateInvitationLink(@RequestBody ParticipantDTO requestDTO,
                                                    @AuthenticationPrincipal CustomUserDetailsDTO userDetails)
    {
        try
        {
            UUID invitedByUserId = userDetails.getUserId();
            String link = participantsService.generateInvitationLink(requestDTO, invitedByUserId);
            return ResponseEntity.status(HttpStatus.OK).body(link);
        }
        catch(NotParticipantException err)
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(err.getMessage());
        }
    }

}
