package com.eventplanner.services.api;

import com.eventplanner.dtos.ParticipantsRequestDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.UUID;

/**
 * Service interface for managing participants in events.
 */
public interface ParticipantsService {

    /**
     * Adds a participant to the specified event.
     *
     * @param participantsRequestDTO Information about the participant and the event.
     * @return A response indicating the success or failure of the operation.
     */
    ResponseEntity<?> addParticipantToEvent(ParticipantsRequestDTO participantsRequestDTO);

    /**
     * Retrieves the list of participants for a specific event.
     *
     * @param eventId The unique identifier of the event.
     * @return A response containing the list of participants or an appropriate message.
     */
    ResponseEntity<?> getEventParticipants(UUID eventId);

    /**
     * Removes a participant from the specified event.
     *
     * @param participantsRequestDTO Information about the participant and the event.
     * @return A response indicating the success or failure of the removal.
     */
    ResponseEntity<?> removeParticipantFromEvent(ParticipantsRequestDTO participantsRequestDTO);

    /**
     * Checks if a user is a participant in a particular event.
     *
     * @param eventId The unique identifier of the event.
     * @param userId  The unique identifier of the user.
     * @return True if the user is a participant; false otherwise.
     */
    Boolean isUserParticipant(UUID eventId, UUID userId);

    /**
     * Removes all participants from the specified event.
     *
     * @param eventId The unique identifier of the event.
     * @return A response indicating the success or failure of the removal.
     */
    ResponseEntity<?> removeAllParticipantsFromEvent(UUID eventId);

    /**
     * Generates an invitation link for adding a participant to the event.
     *
     * @param requestDTO      Information about the participant and the event.
     * @param authentication   The authentication context.
     * @return A response containing the generated invitation link.
     */
    ResponseEntity<?> generateInvitationLink(ParticipantsRequestDTO requestDTO, Authentication authentication);
}
