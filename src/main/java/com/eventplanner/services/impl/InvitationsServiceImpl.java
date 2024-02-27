package com.eventplanner.services.impl;

import com.eventplanner.dtos.CustomUserDetailsDTO;
import com.eventplanner.dtos.ParticipantRequestDTO;
import com.eventplanner.entities.EventInvitations;
import com.eventplanner.entities.Events;
import com.eventplanner.exceptions.EmptyListException;
import com.eventplanner.exceptions.InsufficientPermissionException;
import com.eventplanner.exceptions.NotFoundException;
import com.eventplanner.exceptions.participants.InvalidLinkException;
import com.eventplanner.exceptions.participants.NotParticipantException;
import com.eventplanner.exceptions.participants.UserIsParticipantException;
import com.eventplanner.repositories.EventInvitationsRepository;
import com.eventplanner.repositories.EventsRepository;
import com.eventplanner.repositories.UsersRepository;
import com.eventplanner.services.api.InvitationsService;
import com.eventplanner.services.api.ParticipantsService;
import com.eventplanner.util.HashingUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This class provides the implementation of the {@link InvitationsService} interface,
 * allowing for the management of event invitations in the application.
 */
@Service
@RequiredArgsConstructor
public class InvitationsServiceImpl implements InvitationsService {
    private final EventInvitationsRepository invitationsRepository;
    private final EventsRepository eventsRepository;
    private final UsersRepository usersRepository;
    private final ParticipantsService participantsService;
    private final InvitationLinkService linkService;
    private final PlatformTransactionManager transactionManager;
    private static final Logger LOGGER = LogManager.getLogger();


    //TODO должна возвращаться дто
    //TODO если юзер генерирует ссылку заново, то старая должна удаляться
    @Override
    public EventInvitations createInvitation(String link) throws NotParticipantException, InvalidLinkException {
        TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        LOGGER.info("Start transaction");

        Map<String, UUID> linkData = parseInvitationLink(link);
        UUID eventId = linkData.get("eventId");
        UUID invitedByUserId = linkData.get("invitedByUserId");
        if (!participantsService.isUserParticipant(eventId, invitedByUserId)) {
            throw new NotParticipantException("The invited user with id " + invitedByUserId
                    + " is not participant in this event with id " + eventId);
        }

            EventInvitations eventInvitations = setInvitationData(eventId, invitedByUserId, link);

            invitationsRepository.save(eventInvitations);
            linkService.generateUniqueShortIdentifier(eventInvitations);
            transactionManager.commit(transaction);
            return eventInvitations;
    }

    //TODO нужно сделать проверку, кто выполняет этот запрос (тот, кто аутентифицирован,может посмотреть только список своих приглашений)
    @Override
    public Page<EventInvitations> getInvitationsByEvent(UUID eventId, int page, int size) throws EmptyListException {

        Pageable pageable = PageRequest.of(page, size);
        Page<EventInvitations> invitations = invitationsRepository.findAllByEvent_EventId(eventId, pageable);
        if (invitations.isEmpty()) {
            throw new EmptyListException("There are no invitations for this event: " + eventId);
        } else {
            return invitations;
        }
    }

    @Override
    public String getInvitationStatus(UUID invitationId) throws NotFoundException {
        if (!invitationsRepository.existsById(invitationId)) {
            throw new NotFoundException("Invitation with id " + invitationId + " not found");
        }

        EventInvitations invitation = invitationsRepository.getReferenceById(invitationId);
        UUID eventId = invitation.getEvent().getEventId();
        Events event = eventsRepository.getReferenceById(eventId);
        if (event.getDateTime().after(new Date())) {
            eventsRepository.deleteById(eventId);
            return "The event has already ended";
        }

        return "The invitation with id " + invitationId + " was neither accepted nor declined";
    }

    @Override
    public void deleteInvitation(UUID invitationId) {
        if (!invitationsRepository.existsById(invitationId)) {
            throw new NotFoundException("Invitation with id " + invitationId + " not found");
        }
        invitationsRepository.deleteById(invitationId);
    }


    /**
     * Sets all invitation fields
     *
     * @param eventId         The unique identifier of the event.
     * @param invitedByUserId The unique identifier of the user who invited.
     * @param link            The link where the data is encrypted.
     * @return an object of the EventInvitations class.
     */

    private EventInvitations setInvitationData(UUID eventId, UUID invitedByUserId, String link) {
        EventInvitations eventInvitations = new EventInvitations();
        eventInvitations.setEvent(eventsRepository.getReferenceById(eventId));
        eventInvitations.setInvitedByUser(usersRepository.getReferenceById(invitedByUserId));
        eventInvitations.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        eventInvitations.setInvitationLink(link);
        return eventInvitations;
    }

    /**
     * The link is decoded and divided into several parts by the symbol ',', and then the decoded data from the link is installed in linkData
     *
     * @param link The link where the data is encrypted.
     * @return Map<String, UUID> linkData, which contains the necessary data
     */
    private Map<String, UUID> parseInvitationLink(String link) {
        Map<String, UUID> linkData = new HashMap<>();
        String[] parts = link.split(",");

        if (parts.length >= 2) {
            try {
                UUID eventId = UUID.fromString(parts[0]);
                UUID invitedByUserId = UUID.fromString(parts[1]);
                linkData.put("eventId", eventId);
                linkData.put("invitedByUserId", invitedByUserId);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Incorrect link");
            }
        }
        return linkData;
    }
}
