package com.eventplanner.services.impl;

import com.eventplanner.dtos.EventParticipantsDTO;
import com.eventplanner.dtos.ParticipantDTO;
import com.eventplanner.entities.EventParticipants;
import com.eventplanner.entities.Events;
import com.eventplanner.entities.Users;
import com.eventplanner.exceptions.EmptyListException;
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
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
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
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void addParticipantToEvent(ParticipantDTO participantDTO)
            throws UserIsParticipantException, NotFoundException
    {
        TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        LOGGER.info("Start transaction");

        UUID eventId = participantDTO.getEventId();
        UUID userId = participantDTO.getUserId();
        if (isUserParticipant(eventId, userId))
        {
            throw new UserIsParticipantException("A user with an id " + userId +
                    " is already a participant of an event with an id " + eventId);
        }
        Optional<Events> event = eventsRepository.findById(eventId);
        if (event.isEmpty())
        {
            throw new NotFoundException("Event with id " + eventId + " not found");
        }

        Optional<Users> user = usersRepository.findById(userId);
        if (user.isEmpty())
        {
            transactionManager.rollback(transaction);
            throw new NotFoundException("User with id " + userId + " not found");
        }

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
                                EventParticipantsDTO dto = new EventParticipantsDTO();
                                dto.setParticipantId(participant.getUser().getUserId());
                                dto.setParticipantName(participant.getUser().getUsername());
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
    public void removeParticipantFromEvent(ParticipantDTO participantDTO) throws NotFoundException
    {
        TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        LOGGER.info("Start transaction");

        UUID eventId = participantDTO.getEventId();
        UUID userId = participantDTO.getUserId();
        EventParticipants participant = participantsRepository.
                findByEvent_EventIdAndUser_UserId(eventId,userId).orElse(null);
        if (participant == null)
        {
            transactionManager.rollback(transaction);
            throw new NotFoundException("Participant not found in event");
        }
        else
        {
            participantsRepository.delete(participant);
            transactionManager.commit(transaction);
        }
    }

    @Override
    public Boolean isUserParticipant(UUID eventId, UUID userId) {
        return participantsRepository.existsByEvent_EventIdAndUser_UserId(eventId, userId);
    }

    @Override
    public void removeAllParticipantsFromEvent(UUID eventId)
    {
        TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        LOGGER.info("Start transaction");

        if (!eventsRepository.existsById(eventId))
        {
            throw new NotFoundException("Event with id " + eventId + " not found");
        }
        else
        {
            List<EventParticipants> participants = participantsRepository
                    .findAllByEvent_EventId(eventId);
            participantsRepository.deleteAll(participants);
            transactionManager.commit(transaction);
        }
    }

    @Override
    public String generateInvitationLink(ParticipantDTO requestDTO, UUID invitedByUserId) throws NotParticipantException
    {
        UUID eventId = requestDTO.getEventId();
        UUID invitedUserId = requestDTO.getUserId();

        if (!isUserParticipant(eventId, invitedByUserId))
        {
            throw new NotParticipantException("A user with an id " + invitedByUserId
                    + " is not a participant of an event with an id " + eventId);
        }
        return HashingUtils.generateInvitationLink(eventId,invitedUserId, invitedByUserId);
    }
}