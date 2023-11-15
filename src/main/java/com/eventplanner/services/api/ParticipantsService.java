package com.eventplanner.services.api;

import com.eventplanner.dtos.EventParticipantsDTO;
import com.eventplanner.dtos.ParticipantDTO;
import com.eventplanner.exceptions.EmptyListException;
import com.eventplanner.exceptions.NotFoundException;
import com.eventplanner.exceptions.participants.NotParticipantException;
import com.eventplanner.exceptions.participants.UserIsParticipantException;
import org.springframework.data.domain.Page;

import java.util.UUID;

/**
 * Service interface for managing participants in events.
 */
public interface ParticipantsService {

    /**
     * Adds a participant to the specified event.
     *
     * @param participantDTO The participant data transfer object containing event and user IDs.
     * @throws UserIsParticipantException If the specified user is already a participant in the event.
     * @throws NotFoundException         If the specified event or user is not found.
     *
     * @apiNote This method checks if the user is already a participant in the event and throws an exception if so.
     *          It also ensures that both the event and user exist before creating a new participant entry.
     *          If the specified event or user is not found, a NotFoundException is thrown.
     *          If the user is already a participant, a UserIsParticipantException is thrown.
     */
    void addParticipantToEvent(ParticipantDTO participantDTO);

    /**
     * Retrieves a paginated list of participants for the specified event.
     *
     * @param eventId The ID of the event for which participants are to be retrieved.
     * @param page    The page number (0-indexed).
     * @param size    The size of each page.
     * @return A paginated list of EventParticipantsDTO representing participants in the event.
     * @throws NotFoundException  If the specified event is not found.
     * @throws EmptyListException If no participants are found for the specified event.
     *
     * @apiNote This method retrieves participants for the specified event in a paginated manner.
     *          It checks if the event exists and throws a NotFoundException if not.
     *          If participants are found, it returns a paginated list of EventParticipantsDTO.
     *          If no participants are found, an EmptyListException is thrown.
     */
    Page<EventParticipantsDTO> getEventParticipants(UUID eventId, int page, int size);

    /**
     * Removes a participant from the specified event.
     *
     * @param participantDTO The ParticipantDTO containing the event and user IDs.
     * @throws NotFoundException If the participant is not found in the specified event.
     *
     * @apiNote This method removes a participant from the specified event based on the provided
     *          ParticipantDTO containing the event and user IDs. If the participant is not found,
     *          a NotFoundException is thrown. Otherwise, the participant is deleted from the event.
     */
    void removeParticipantFromEvent(ParticipantDTO participantDTO);

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
     * @param eventId The ID of the event from which participants will be removed.
     * @throws NotFoundException If the specified event is not found.
     *
     * @apiNote This method removes all participants from the specified event. If the event is not found,
     *          a NotFoundException is thrown. Otherwise, all participants associated with the event are
     *          deleted.
     */
    void removeAllParticipantsFromEvent(UUID eventId);

    /**
     * Generates an invitation link for the specified participant in the event.
     *
     * @param requestDTO        The ParticipantDTO containing event and user information.
     * @param invitedByUserId   The ID of the user who is sending the invitation.
     * @return                  The generated invitation link.
     * @throws NotParticipantException If the user sending the invitation is not a participant in the specified event.
     *
     * @apiNote This method generates an invitation link for the specified participant in the event. It checks if
     *          the user sending the invitation is a participant in the event. If not, a NotParticipantException
     *          is thrown. Otherwise, the invitation link is generated using the HashingUtils class.
     */
    String generateInvitationLink(ParticipantDTO requestDTO, UUID invitedByUserId);
}
