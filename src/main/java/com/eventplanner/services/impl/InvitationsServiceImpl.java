package com.eventplanner.services.impl;

import com.eventplanner.dtos.CustomUserDetailsDTO;
import com.eventplanner.dtos.ParticipantDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

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
public class InvitationsServiceImpl implements InvitationsService
{
    private final EventInvitationsRepository invitationsRepository;
    private final EventsRepository eventsRepository;
    private final UsersRepository usersRepository;
    private final ParticipantsService participantsService;


    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public EventInvitations createInvitation(String link) throws NotParticipantException, InvalidLinkException
    {
            Map<String, UUID> linkData = parseInvitationLink(link);
            UUID eventId = linkData.get("eventId");
            UUID invitedUserId = linkData.get("invitedUserId");
            UUID invitedByUserId = linkData.get("invitedByUserId");

            if (!participantsService.isUserParticipant(eventId,invitedByUserId))
            {
                throw new NotParticipantException("The invited user with id " + invitedByUserId
                        + " is not participant in this event with id " + eventId);
            }

            if (validateInvitationLink(eventId, invitedUserId, invitedByUserId))
            {
                EventInvitations eventInvitations = setInvitationData(eventId, invitedByUserId, invitedUserId, link);
                invitationsRepository.save(eventInvitations);
                return eventInvitations;
            }
            else
            {
                throw new InvalidLinkException("Invalid link: " + link);
            }
    }

    @Override
    public Page<EventInvitations> getInvitationsByEvent(UUID eventId, int page, int size) throws EmptyListException
    {

        Pageable pageable = PageRequest.of(page, size);
        Page<EventInvitations> invitations = invitationsRepository.findAllByEvent_EventId(eventId,pageable);
        if (invitations.isEmpty())
        {
            throw new EmptyListException("There are no invitations for this event: " + eventId);
        }
        else
        {
            return invitations;
        }
    }

    @Override
    public Page<EventInvitations> getInvitationsByUser(UUID userId, int page, int size) throws EmptyListException
    {
        Pageable pageable = PageRequest.of(page, size);
        Page<EventInvitations> invitations = invitationsRepository.findAllByInvitedUser_UserId(userId, pageable);
        if (invitations.isEmpty())
        {
            throw new EmptyListException("There are no invitations for this user: " + userId);
        }
        else
        {
            return invitations;
        }
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void acceptInvitation(UUID invitationId, UUID authenticatedUserId)
            throws NotFoundException, InsufficientPermissionException, UserIsParticipantException
    {
        if (!invitationsRepository.existsById(invitationId))
        {
            throw new NotFoundException("Invitation with id " + invitationId + " not found");
        }
        EventInvitations invitation = invitationsRepository.getReferenceById(invitationId);

        if (!authenticatedUserId.equals(invitation.getInvitedUser().getUserId()))
        {
            throw new InsufficientPermissionException("You do not have permission to perform this action");
        }
        if (participantsService.isUserParticipant(invitation.getEvent().getEventId(), authenticatedUserId))
        {
            throw new UserIsParticipantException("User with id " + authenticatedUserId
                    + " is already a participant of the event with id " + invitation.getEvent().getEventId());
        }
        UUID eventId = invitation.getEvent().getEventId();
        ParticipantDTO requestDTO = new ParticipantDTO(eventId, authenticatedUserId);

        participantsService.addParticipantToEvent(requestDTO);
        invitationsRepository.delete(invitation);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void declineInvitation(UUID invitationId) throws NotFoundException
    {
        if (!invitationsRepository.existsById(invitationId))
        {
            throw new NotFoundException("Invitation with id " + invitationId + " not found");
        }
        invitationsRepository.deleteById(invitationId);
    }

    @Override
    public String getInvitationStatus(UUID invitationId) throws NotFoundException
    {
        if (!invitationsRepository.existsById(invitationId))
        {
            throw new NotFoundException("Invitation with id " + invitationId + " not found");
        }

        EventInvitations invitation = invitationsRepository.getReferenceById(invitationId);
        UUID eventId = invitation.getEvent().getEventId();
        Events event = eventsRepository.getReferenceById(eventId);
        if (event.getDateTime().after(new Date()))
        {
            eventsRepository.deleteById(eventId);
            return "The event has already ended";
        }

        return  "The invitation with id " + invitationId + " was neither accepted nor declined";
    }

    @Override
    public void deleteInvitation(UUID invitationId)
    {
        if (!invitationsRepository.existsById(invitationId))
        {
            throw new NotFoundException("Invitation with id " + invitationId + " not found");
        }
        invitationsRepository.deleteById(invitationId);
    }

    /**
     * Checks whether this user and events exist
     *
     * @param eventId The unique identifier of the event.
     * @param userId  The unique identifier of the user.
     */
    private void checkOfExistence(UUID eventId, UUID userId)
    {
        if (!eventsRepository.existsById(eventId))
        {
            throw new EntityNotFoundException("Event with id " + eventId + " not found");
        }

        if (!usersRepository.existsById(userId))
        {
            throw new EntityNotFoundException("User with id " + userId + " not found");
        }
    }

    /**
     * Sets all invitation fields
     *
     * @param eventId The unique identifier of the event.
     * @param invitedByUserId The unique identifier of the user who invited.
     * @param invitedUserId   The unique identifier of the user who was invited.
     * @param link The link where the data is encrypted.
     * @return an object of the EventInvitations class.
     */

    private EventInvitations setInvitationData(UUID eventId, UUID invitedByUserId,UUID invitedUserId, String link)
    {
        EventInvitations eventInvitations = new EventInvitations();
        eventInvitations.setEvent(eventsRepository.getReferenceById(eventId));
        eventInvitations.setInvitedByUser(usersRepository.getReferenceById(invitedByUserId));
        eventInvitations.setInvitedUser(usersRepository.getReferenceById(invitedUserId));
        eventInvitations.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        eventInvitations.setInvitationLink(link);
        return eventInvitations;
    }

    /**
     * The link is decoded and divided into several parts by the symbol ';', and then the decoded data from the link is installed in linkData
     *
     * @param link The link where the data is encrypted.
     * @return Map<String, UUID> linkData, which contains the necessary data
     */
    private Map<String, UUID> parseInvitationLink(String link) {
        Map<String, UUID> linkData = new HashMap<>();
        String decryptedLink = HashingUtils.decryptData(link);
        String[] parts = decryptedLink.split(";");

        if (parts.length >= 2) {
            try {
                UUID eventId = UUID.fromString(parts[0]);
                UUID invitedUserId = UUID.fromString(parts[1]);
                UUID invitedByUserId = UUID.fromString(parts[2]);
                linkData.put("eventId", eventId);
                linkData.put("invitedUserId", invitedUserId);
                linkData.put("invitedByUserId", invitedByUserId);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Incorrect link");
            }
        }
        return linkData;
    }

    /**
     * Validates an invitation link based on the provided event, invited user, and inviter user IDs.
     *
     * @param eventId         The unique identifier of the event.
     * @param invitedUserId   The unique identifier of the user who was invited.
     * @param invitedByUserId The unique identifier of the user who invited.
     * @return True if the link is valid; false otherwise.
     */
    private boolean validateInvitationLink(UUID eventId, UUID invitedUserId, UUID invitedByUserId)
    {
        checkOfExistence(eventId, invitedUserId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetailsDTO userDetailsDTO = (CustomUserDetailsDTO) authentication.getPrincipal();
        return invitedByUserId.equals(userDetailsDTO.getUserId());
    }
}
