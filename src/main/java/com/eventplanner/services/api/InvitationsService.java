package com.eventplanner.services.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.UUID;

/**
 * This interface defines the operations related to event invitations in the application.
 */
public interface InvitationsService {

    /**
     * Creates an invitation with the provided link.
     *
     * @param link The link for the invitation.
     * @return ResponseEntity with the created invitation if successful, or an error message if it fails.
     */
    ResponseEntity<?> createInvitation(String link);

    /**
     * Retrieves invitations associated with a specific event.
     *
     * @param eventId The unique identifier of the event.
     * @return ResponseEntity containing a list of invitations for the event if successful, or an error message if there are no invitations.
     */
    ResponseEntity<?> getInvitationsByEvent(UUID eventId);

    /**
     * Retrieves invitations for a specific user.
     *
     * @param userId The unique identifier of the user.
     * @return ResponseEntity containing a list of invitations for the user if successful, or an error message if there are no invitations.
     */
    ResponseEntity<?> getInvitationsByUser(UUID userId);

    /**
     * Accepts an invitation with the specified identifier.
     *
     * @param invitationId    The unique identifier of the invitation to accept.
     * @param authentication   The authentication object of the currently logged-in user.
     * @return ResponseEntity with a success message if the invitation is accepted, or an error message if it fails.
     */
    ResponseEntity<?> acceptInvitation(UUID invitationId, Authentication authentication);

    /**
     * Declines an invitation with the specified identifier.
     *
     * @param invitationId The unique identifier of the invitation to decline.
     * @return ResponseEntity with a success message if the invitation is declined, or an error message if it fails.
     */
    ResponseEntity<?> declineInvitation(UUID invitationId);

    /**
     * Retrieves the status of an invitation by its identifier.
     *
     * @param invitationId The unique identifier of the invitation.
     * @return ResponseEntity containing the invitation status if found, or an error message if it doesn't exist.
     */
    ResponseEntity<?> getInvitationStatus(UUID invitationId);

    /**
     * Deletes an invitation with the specified identifier.
     *
     * @param invitationId The unique identifier of the invitation to delete.
     * @return ResponseEntity with a success message if the invitation is deleted, or an error message if it fails.
     */
    ResponseEntity<?> deleteInvitation(UUID invitationId);


}
