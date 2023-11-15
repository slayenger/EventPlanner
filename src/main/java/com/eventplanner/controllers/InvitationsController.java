package com.eventplanner.controllers;


import com.eventplanner.dtos.CustomUserDetailsDTO;
import com.eventplanner.entities.EventInvitations;
import com.eventplanner.exceptions.*;
import com.eventplanner.exceptions.participants.InvalidLinkException;
import com.eventplanner.exceptions.participants.NotParticipantException;
import com.eventplanner.exceptions.participants.UserIsParticipantException;
import com.eventplanner.services.api.InvitationsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/invitation")
public class InvitationsController
{
    private final InvitationsService invitationsService;

    @PostMapping("/create_invitation/{link}")
    public ResponseEntity<?> createInvitation(@PathVariable String link)
    {
        try
        {
            EventInvitations invitation = invitationsService.createInvitation(link);
            return ResponseEntity.ok(invitation);
        }
        catch (NotParticipantException err)
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(err.getMessage());
        }
        catch (InvalidLinkException err)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err.getMessage());
        }
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<?> getInvitationsByEvent(@PathVariable UUID eventId,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "10") int size)
    {
        try
        {
            Page<EventInvitations> invitations = invitationsService.getInvitationsByEvent(eventId, page, size);
            return ResponseEntity.ok(invitations);
        }
        catch (EmptyListException err)
        {
            System.out.println("Error:" + err.getMessage());
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("There are no invitations for this event");
        }

    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getInvitationsByUser(@PathVariable UUID userId,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "10") int size)
    {
        try
        {
            Page<EventInvitations> invitations = invitationsService.getInvitationsByUser(userId, page, size);
            return ResponseEntity.status(HttpStatus.OK).body(invitations);
        }
        catch (EmptyListException err)
        {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(err.getMessage());
        }
    }


    @PostMapping("/accept/{invitationId}")
    public ResponseEntity<?> acceptInvitation(@PathVariable UUID invitationId,
                                              @AuthenticationPrincipal CustomUserDetailsDTO userDetails)
    {
        try
        {
            UUID userId = userDetails.getUserId();
            invitationsService.acceptInvitation(invitationId, userId);
            return ResponseEntity.status(HttpStatus.OK).body("The invitation with id " + invitationId + " was accepted");
        }
        catch (NotFoundException err)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err.getMessage());
        }
        catch (InsufficientPermissionException | UserIsParticipantException err)
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(err.getMessage());
        }
    }

    @DeleteMapping("/decline/{invitationId}")
    public ResponseEntity<?> declineInvitation(@PathVariable UUID invitationId)
    {
        try
        {
            invitationsService.declineInvitation(invitationId);
            return ResponseEntity.status(HttpStatus.OK).body("Invitation with id " + invitationId + " was declined");
        }
        catch (NotFoundException err)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err.getMessage());
        }
    }

    @GetMapping("/status/{invitationId}")
    public ResponseEntity<?> getInvitationStatus(@PathVariable UUID invitationId)
    {
        try
        {
            String status = invitationsService.getInvitationStatus(invitationId);
            return ResponseEntity.status(HttpStatus.OK).body(status);
        }
        catch (NotFoundException err)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err.getMessage());
        }
    }

    @DeleteMapping("/{invitationId}")
    public ResponseEntity<?> deleteInvitation(@PathVariable UUID invitationId)
    {
        try
        {
            invitationsService.deleteInvitation(invitationId);
            return ResponseEntity.status(HttpStatus.OK).body("Invitation with id " + invitationId + " was successfully deleted");
        }
        catch (NotFoundException err)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err.getMessage());
        }
    }
}
