package com.eventplanner.services.impl;

import com.eventplanner.dtos.EventParticipantsDTO;
import com.eventplanner.dtos.ParticipantRequestDTO;
import com.eventplanner.entities.EventParticipants;
import com.eventplanner.entities.Events;
import com.eventplanner.entities.Users;
import com.eventplanner.exceptions.EmptyListException;
import com.eventplanner.exceptions.InsufficientPermissionException;
import com.eventplanner.exceptions.NotFoundException;
import com.eventplanner.exceptions.participants.NotParticipantException;
import com.eventplanner.exceptions.participants.UserIsParticipantException;
import com.eventplanner.repositories.EventParticipantsRepository;
import com.eventplanner.repositories.EventsRepository;
import com.eventplanner.repositories.UsersRepository;
import com.eventplanner.services.api.ParticipantsService;
import com.eventplanner.util.HashingUtils;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

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
    private final PlatformTransactionManager transactionManager;
    private final InvitationLinkService linkService;
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String DOMAIN = "localhost:8080/";

    @Override
    public void addParticipantToEvent(UUID eventId, UUID userId)
            throws UserIsParticipantException, NotFoundException
    {
        TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        LOGGER.info("Start transaction");

        Optional<Events> event = eventsRepository.findById(eventId);
        if (event.isEmpty())
        {
            throw new NotFoundException("Event with id " + eventId + " not found");
        }

        if (isUserParticipant(eventId, userId))
        {
            throw new UserIsParticipantException("A user with an id " + userId +
                    " is already a participant of an event with an id " + eventId);
        }

        Optional<Users> user = usersRepository.findById(userId);

        EventParticipants participants = new EventParticipants();
        participants.setEvent(event.get());
        participants.setUser(user.get());

        participantsRepository.save(participants);
        transactionManager.commit(transaction);
    }

    @Override
    public Page<EventParticipantsDTO> getEventParticipants(UUID eventId, int page, int size)
            throws NotFoundException, EmptyListException
    {
        if (!eventsRepository.existsById(eventId))
        {
            throw new NotFoundException("Event with id " + eventId + " not found");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<EventParticipants> participantsList = participantsRepository
                .findAllByEvent_EventId(eventId, pageable);

        if (!participantsList.isEmpty())
        {
            return participantsList
                    .map(participant ->
                            {
                                Users user = participant.getUser();
                                EventParticipantsDTO dto = new EventParticipantsDTO();
                                dto.setParticipantId(user.getUserId());
                                dto.setParticipantFirstname(user.getFirstname());
                                dto.setParticipantLastname(user.getLastname());
                                dto.setEventId(participant.getEvent().getEventId());
                                return dto;
                            }
                    );
        }
        else
        {
            throw new EmptyListException("Participants not found");
        }
    }

    @Override
    public void removeParticipantFromEvent(ParticipantRequestDTO participantRequestDTO, UUID authenticatedUserId) throws NotFoundException, InsufficientPermissionException
    {
        TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        LOGGER.info("Start transaction");

        UUID eventId = participantRequestDTO.getEventId();
        UUID participantId = participantRequestDTO.getParticipantId();
        EventParticipants participant = participantsRepository.
                findByEvent_EventIdAndUser_UserId(eventId,participantId)
                .orElseThrow(() ->
                {
                    throw new NotFoundException("Participant not found");
                });
        UUID userId = participant.getUser().getUserId();
        Events event = eventsRepository.getReferenceById(eventId);
        if (!(authenticatedUserId.equals(event.getOrganizer().getUserId())) && !(authenticatedUserId.equals(userId)))
        {
            throw new InsufficientPermissionException("You don't have the rights to delete this participant");
        }

        participantsRepository.delete(participant);
        transactionManager.commit(transaction);
    }

    @Override
    public Boolean isUserParticipant(UUID eventId, UUID userId) {
        return participantsRepository.existsByEvent_EventIdAndUser_UserId(eventId, userId);
    }

    //TODO возвращается нихуя не ссылка XDDDDD
    @Override
    public String generateInvitationLink(UUID eventId, UUID invitedByUserId) throws NotParticipantException
    {
        if (!isUserParticipant(eventId, invitedByUserId))
        {
            throw new NotParticipantException("A user with an id " + invitedByUserId
                    + " is not a participant of an event with an id " + eventId);
        }
        return eventId.toString() + "," + invitedByUserId.toString();
    }
}