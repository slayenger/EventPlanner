package com.eventplanner.controllers;


import com.eventplanner.services.api.InvitationsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/invitation")
public class InvitationsController
{
    private final InvitationsService invitationsService;

    @PostMapping("/join_event")
    public ResponseEntity<?> createInvitation(@RequestBody String link)
    {
        return invitationsService.createInvitation(link);
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<?> getInvitationsByEvent(@PathVariable UUID eventId)
    {
        return invitationsService.getInvitationsByEvent(eventId);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getInvitationsByUser(@PathVariable UUID userId)
    {
        return invitationsService.getInvitationsByUser(userId);
    }


    @PostMapping("/accept/{invitationId}")
    public ResponseEntity<?> acceptInvitation(@PathVariable UUID invitationId, Authentication authentication)
    {
        return invitationsService.acceptInvitation(invitationId, authentication);
    }

    @DeleteMapping("/decline/{invitationId}")
    public ResponseEntity<?> declineInvitation(@PathVariable UUID invitationId)
    {
        return invitationsService.declineInvitation(invitationId);
    }

    @GetMapping("/status/{invitationId}")
    public ResponseEntity<?> getInvitationStatus(@PathVariable UUID invitationId)
    {
        return invitationsService.getInvitationStatus(invitationId);
    }

    @DeleteMapping("/{invitationId}")
    public ResponseEntity<?> deleteInvitation(@PathVariable UUID invitationId)
    {
        return invitationsService.deleteInvitation(invitationId);
    }
}
