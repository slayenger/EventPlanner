package com.eventplanner.services.api;

import com.eventplanner.entities.EventInvitations;
import com.eventplanner.exceptions.EmptyListException;
import com.eventplanner.exceptions.InsufficientPermissionException;
import com.eventplanner.exceptions.NotFoundException;
import com.eventplanner.exceptions.participants.UserIsParticipantException;
import org.springframework.data.domain.Page;

import java.util.UUID;

/**
 * This interface defines the operations related to event invitations in the application.
 */
public interface InvitationsService {

    /**
     * Creates an invitation using the provided link.
     *
     * @param link The invitation link.
     * @return The created EventInvitations object.
     */
    EventInvitations createInvitation(String link);

    /**
     * Retrieves a paginated list of invitations for a specific event.
     *
     * @param eventId The unique identifier of the event.
     * @param page    The page number (zero-based) of the result set to retrieve.
     * @param size    The number of invitations per page.
     * @return A Page object containing the list of EventInvitations for the specified event.
     * @throws EmptyListException If there are no invitations for the specified event.
     */
    Page<EventInvitations> getInvitationsByEvent(UUID eventId, int page, int size);

    /**
     * Retrieves a paginated list of invitations for a specific user.
     *
     * @param userId The unique identifier of the user.
     * @param page   The page number (zero-based) of the result set to retrieve.
     * @param size   The number of invitations per page.
     * @return A Page object containing the list of EventInvitations for the specified user.
     * @throws EmptyListException If there are no invitations for the specified user.
     */
    //Page<EventInvitations> getInvitationsByUser(UUID userId, int page, int size);

    /**
     * Accepts an invitation for a specific user to join an event.
     *
     * @param invitationId The unique identifier of the invitation.
     * @param authenticatedUserId       The unique identifier of the user accepting the invitation.
     * @throws NotFoundException              If the invitation with the specified id is not found.
     * @throws InsufficientPermissionException If the user does not have permission to accept the invitation.
     * @throws UserIsParticipantException      If the user is already a participant of the associated event.
     */
    //void acceptInvitation(UUID invitationId, UUID authenticatedUserId);

    /**
     * Declines an invitation, rejecting the associated event invitation.
     *
     * @param invitationId The unique identifier of the invitation to decline.
     * @throws NotFoundException If the invitation with the specified id is not found.
     */
    //void declineInvitation(UUID invitationId);

    /**
     * Retrieves the status of an invitation based on the associated event's current status.
     *
     * @param invitationId The unique identifier of the invitation to check.
     * @return A string indicating the status of the invitation.
     * @throws NotFoundException If the invitation with the specified id is not found.
     */
    String getInvitationStatus(UUID invitationId);

    /**
     * Deletes an invitation with the specified ID.
     *
     * @param invitationId The unique identifier of the invitation to be deleted.
     * @throws NotFoundException If the invitation with the specified id is not found.
     */
    void deleteInvitation(UUID invitationId);
}
