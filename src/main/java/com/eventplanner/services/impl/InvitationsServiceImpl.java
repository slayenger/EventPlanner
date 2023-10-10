package com.eventplanner.services.impl;

import com.eventplanner.dtos.CustomUserDetailsDTO;
import com.eventplanner.dtos.ParticipantsRequestDTO;
import com.eventplanner.entities.EventInvitations;
import com.eventplanner.entities.Events;
import com.eventplanner.repositories.EventInvitationsRepository;
import com.eventplanner.repositories.EventsRepository;
import com.eventplanner.repositories.UsersRepository;
import com.eventplanner.services.api.InvitationsService;
import com.eventplanner.services.api.ParticipantsService;
import com.eventplanner.util.HashingUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;


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
    public ResponseEntity<?> createInvitation(String link)
    {
        try
        {
            Map<String, UUID> linkData = parseInvitationLink(link);
            UUID eventId = linkData.get("eventId");
            UUID invitedUserId = linkData.get("invitedUserId");
            UUID invitedByUserId = linkData.get("invitedByUserId");

            if (!participantsService.isUserParticipant(eventId,invitedByUserId))
            {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("The invited user with id " + invitedByUserId
                                + " is not participant in this event with id " + eventId);
            }

            if (validateInvitationLink(eventId, invitedUserId, invitedByUserId))
            {
                EventInvitations eventInvitations = setInvitationData(eventId, invitedByUserId, invitedUserId, link);
                invitationsRepository.save(eventInvitations);
            }
            else
            {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid link " + link);
            }

            return ResponseEntity.ok("Success");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new IllegalArgumentException("error decrypting");
        }
    }

    @Override
    public ResponseEntity<?> getInvitationsByEvent(UUID eventId)
    {
        List<EventInvitations> invitations = invitationsRepository.findAllByEvent_EventId(eventId);
        if (invitations.isEmpty())
        {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("There are no invitations for this event");
        }
        else
        {
            return ResponseEntity.ok(invitations);
        }
    }


    @Override
    public ResponseEntity<?> getInvitationsByUser(UUID userId)
    {
        List<EventInvitations> invitations = invitationsRepository.findAllByInvitedUser_UserId(userId);
        if (invitations.isEmpty())
        {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("There are no invitations for this user");
        }
        else
        {
            return ResponseEntity.ok(invitations);
        }

    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseEntity<?> acceptInvitation(UUID invitationId, Authentication authentication)
    {
        if (!invitationsRepository.existsById(invitationId))
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Invitation with id " + invitationId + " not found");
        }
        EventInvitations invitation = invitationsRepository.getReferenceById(invitationId);

        CustomUserDetailsDTO userDetailsDTO = (CustomUserDetailsDTO) authentication.getPrincipal();
        UUID userId = userDetailsDTO.getUserId();
        if (!userId.equals(invitation.getInvitedUser().getUserId()))
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("WTFMAN");
        }
        if (participantsService.isUserParticipant(invitation.getEvent().getEventId(),userId))
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("User with id " + userId
                            + " is already a participant of the event with id " + invitation.getEvent().getEventId());
        }
        UUID eventId = invitation.getEvent().getEventId();
        ParticipantsRequestDTO requestDTO = new ParticipantsRequestDTO(eventId, userId);

        ResponseEntity<?> response = ResponseEntity.ok(participantsService
                .addParticipantToEvent(requestDTO)).getBody();
        invitationsRepository.delete(invitation);

        return response;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseEntity<?> declineInvitation(UUID invitationId)
    {
        if (!invitationsRepository.existsById(invitationId))
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Invitation with id " + invitationId + " not found");
        }
        invitationsRepository.deleteById(invitationId);
        return ResponseEntity.ok().body("Invitation with id " + invitationId + " was declined");
    }


    @Override
    public ResponseEntity<?> getInvitationStatus(UUID invitationId)
    {
        EventInvitations invitation = invitationsRepository.getReferenceById(invitationId);
        UUID eventId = invitation.getEvent().getEventId();
        Events event = eventsRepository.getReferenceById(eventId);
        if (event.getDateTime().after(new Date()))
        {
            eventsRepository.deleteById(eventId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The event has already ended");
        }

        if (!invitationsRepository.existsById(invitationId))
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Invitation with id " + invitationId + " not found");
        }
        return ResponseEntity.ok()
                .body("The invitation with id " + invitationId + " was neither accepted nor declined");
    }

    @Override
    public ResponseEntity<?> deleteInvitation(UUID invitationId)
    {
        if (!invitationsRepository.existsById(invitationId))
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Invitation with id " + invitationId + " not found");
        }
        invitationsRepository.deleteById(invitationId);
        return ResponseEntity.ok().body("Invitation with id " + invitationId + " was successfully deleted");
    }

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

    private boolean validateInvitationLink(UUID eventId, UUID invitedUserId, UUID invitedByUserId)
    {
        checkOfExistence(eventId, invitedUserId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetailsDTO userDetailsDTO = (CustomUserDetailsDTO) authentication.getPrincipal();
        return invitedByUserId.equals(userDetailsDTO.getUserId());
    }

}
