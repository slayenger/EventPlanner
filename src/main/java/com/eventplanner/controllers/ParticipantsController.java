package com.eventplanner.controllers;

import com.eventplanner.dtos.CustomUserDetailsDTO;
import com.eventplanner.dtos.EventParticipantsDTO;
import com.eventplanner.dtos.ParticipantRequestDTO;
import com.eventplanner.exceptions.EmptyListException;
import com.eventplanner.exceptions.InsufficientPermissionException;
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

    @PostMapping("/{eventId}")
    public ResponseEntity<?> addParticipantToEvent(@PathVariable UUID eventId, @AuthenticationPrincipal CustomUserDetailsDTO userDetails)
    {
        try
        {
            participantsService.addParticipantToEvent(eventId, userDetails.getUserId());
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
    public ResponseEntity<?> removeParticipantFromEvent(@RequestBody ParticipantRequestDTO participantRequestDTO,
                                                        @AuthenticationPrincipal CustomUserDetailsDTO userDetails)
    {
        try
        {
            participantsService.removeParticipantFromEvent(participantRequestDTO, userDetails.getUserId());
            return ResponseEntity.status(HttpStatus.OK).body("The participant was successfully deleted");
        }
        catch (InsufficientPermissionException err)
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(err.getMessage());
        }
        catch (NotFoundException err)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err.getMessage());
        }
    }

    @GetMapping("/generate_link/{eventId}")
    public ResponseEntity<?> generateInvitationLink(@PathVariable UUID eventId,
                                                    @AuthenticationPrincipal CustomUserDetailsDTO userDetails)
    {
        try
        {
            UUID invitedByUserId = userDetails.getUserId();
            String link = participantsService.generateInvitationLink(eventId, invitedByUserId);
            return ResponseEntity.status(HttpStatus.OK).body(link);
        }
        catch(NotParticipantException err)
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(err.getMessage());
        }
    }
}
