package com.eventplanner.services.impl;

import com.eventplanner.dtos.CustomUserDetailsDTO;
import com.eventplanner.dtos.EventParticipantsDTO;
import com.eventplanner.dtos.ParticipantsRequestDTO;
import com.eventplanner.entities.EventParticipants;
import com.eventplanner.entities.Events;
import com.eventplanner.entities.Users;
import com.eventplanner.repositories.EventParticipantsRepository;
import com.eventplanner.repositories.EventsRepository;
import com.eventplanner.repositories.UsersRepository;
import com.eventplanner.services.api.InvitationsService;
import com.eventplanner.services.api.ParticipantsService;
import com.eventplanner.util.HashingUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * This class provides the implementation of the {@link ParticipantsService} interface.
 * Service class for managing participants in events.
 */
@Service
@RequiredArgsConstructor
public class ParticipantsServiceImpl implements ParticipantsService
{
    private final EventParticipantsRepository participantsRepository;
    private final UsersRepository usersRepository;
    private final EventsRepository eventsRepository;


    /**
     * Adds a participant to the specified event.
     *
     * @param participantsRequestDTO Information about the participant and the event.
     * @return A response indicating the success or failure of the operation.
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseEntity<?> addParticipantToEvent(ParticipantsRequestDTO participantsRequestDTO)
    {
        UUID eventId = participantsRequestDTO.getEventId();
        UUID userId = participantsRequestDTO.getUserId();
        if (isUserParticipant(eventId, userId))
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("A user with an id " + userId +
                            " is already a participant of an event with an id " + eventId);
        }
        Optional<Events> event = eventsRepository.findById(eventId);
        if (event.isEmpty())
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    "Event with id " + eventId + " not found"
            );
        }

        Optional<Users> user = usersRepository.findById(userId);
        if (user.isEmpty())
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    "User with id " + userId + " not found"
            );
        }

        EventParticipants participants = new EventParticipants();
        participants.setEvent(event.get());
        participants.setUser(user.get());

        participantsRepository.save(participants);
        return ResponseEntity.status(HttpStatus.OK)
                .body("User with ID " + userId + " added to event with ID " + eventId);

    }

    /**
     * Retrieves the list of participants for a specific event.
     *
     * @param eventId The unique identifier of the event.
     * @return A response containing the list of participants or an appropriate message.
     */
    @Override
    public ResponseEntity<?> getEventParticipants(UUID eventId)
    {
        if (!eventsRepository.existsById(eventId))
        {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body("Event with id " + eventId + " not found");
        }
        List<EventParticipants> participantsList = participantsRepository
                .findAllByEvent_EventId(eventId);

        if (!participantsList.isEmpty())
        {
            List<EventParticipantsDTO> participantsDTOs = participantsList.stream()
                    .map(participant ->
                            {
                                EventParticipantsDTO dto = new EventParticipantsDTO();
                                dto.setParticipantId(participant.getUser().getUserId());
                                dto.setParticipantName(participant.getUser().getUsername());
                                dto.setEventId(participant.getEvent().getEventId());
                                return dto;
                            }
                    ).toList();
            return ResponseEntity.ok(participantsDTOs);
        }
        else
        {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Participants not found");
        }

    }

    /**
     * Removes a participant from the specified event.
     *
     * @param participantsRequestDTO Information about the participant and the event.
     * @return A response indicating the success or failure of the removal.
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseEntity<?> removeParticipantFromEvent(ParticipantsRequestDTO participantsRequestDTO)
    {
        UUID eventId = participantsRequestDTO.getEventId();
        UUID userId = participantsRequestDTO.getUserId();
        EventParticipants participant = participantsRepository.
                findByEvent_EventIdAndUser_UserId(eventId,userId).orElse(null);
        if (participant == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Participant not found in event");
        }
        else
        {
            participantsRepository.delete(participant);
            return ResponseEntity.ok("Participant removed from event");
        }
    }

    /**
     * Checks if a user is a participant in a particular event.
     *
     * @param eventId The unique identifier of the event.
     * @param userId  The unique identifier of the user.
     * @return True if the user is a participant; false otherwise.
     */
    @Override
    public Boolean isUserParticipant(UUID eventId, UUID userId) {
        return participantsRepository.existsByEvent_EventIdAndUser_UserId(eventId, userId);
    }

    /**
     * Removes all participants from the specified event.
     *
     * @param eventId The unique identifier of the event.
     * @return A response indicating the success or failure of the removal.
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseEntity<?> removeAllParticipantsFromEvent(UUID eventId)
    {
        if (!eventsRepository.existsById(eventId))
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Event with id " + eventId + " not found");
        }
        else
        {
            List<EventParticipants> participants = participantsRepository
                    .findAllByEvent_EventId(eventId);

            participantsRepository.deleteAll(participants);
            return ResponseEntity.ok("Success");
        }
    }

    /**
     * Generates an invitation link for adding a participant to the event.
     *
     * @param requestDTO      Information about the participant and the event.
     * @param authentication   The authentication context.
     * @return A response containing the generated invitation link.
     */
    @Override
    public ResponseEntity<?> generateInvitationLink(ParticipantsRequestDTO requestDTO, Authentication authentication)
    {
        UUID eventId = requestDTO.getEventId();
        UUID invitedUserId = requestDTO.getUserId();
        CustomUserDetailsDTO userDetailsDTO = (CustomUserDetailsDTO) authentication.getPrincipal();
        UUID invitedByUserId = userDetailsDTO.getUserId();
        if (!isUserParticipant(eventId, invitedByUserId))
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("A user with an id " + invitedByUserId + " is not a participant of an event with an id " + eventId);
        }
        String link = HashingUtils.generateInvitationLink(eventId,invitedUserId, invitedByUserId);
        return ResponseEntity.ok().body(link);
    }
}
