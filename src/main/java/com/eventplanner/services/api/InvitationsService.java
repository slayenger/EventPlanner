package com.eventplanner.services.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.UUID;

public interface InvitationsService {

    ResponseEntity<?> createInvitation(String link);

    ResponseEntity<?> getInvitationsByEvent(UUID eventId);

    ResponseEntity<?> getInvitationsByUser(UUID userId);

    ResponseEntity<?> acceptInvitation(UUID invitationId, Authentication authentication);

    ResponseEntity<?> declineInvitation(UUID invitationId);

    ResponseEntity<?> getInvitationStatus(UUID invitationId);

    ResponseEntity<?> deleteInvitation(UUID invitationId);


}
