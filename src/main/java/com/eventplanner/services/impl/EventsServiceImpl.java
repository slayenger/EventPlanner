package com.eventplanner.services.impl;

import com.eventplanner.dtos.CustomUserDetailsDTO;
import com.eventplanner.dtos.EventsDTO;
import com.eventplanner.dtos.ParticipantsRequestDTO;
import com.eventplanner.entities.Events;
import com.eventplanner.entities.Users;
import com.eventplanner.repositories.EventInvitationsRepository;
import com.eventplanner.repositories.EventParticipantsRepository;
import com.eventplanner.repositories.EventsRepository;
import com.eventplanner.repositories.UsersRepository;
import com.eventplanner.services.api.EventsService;
import com.eventplanner.services.api.ParticipantsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventsServiceImpl implements EventsService {

    private final EventsRepository eventsRepository;
    private final UsersRepository usersRepository;
    private final ParticipantsService participantsService;
    private final EventParticipantsRepository participantsRepository;
    private final EventInvitationsRepository invitationsRepository;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseEntity<?> createNewEvent(EventsDTO eventsDTO, Authentication authentication)
    {
        try
        {
            if (!authentication.isAuthenticated())
            {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }
            Events newEvent = new Events();
            newEvent.setTitle(eventsDTO.getTitle());
            newEvent.setDescription(eventsDTO.getDescription());
            newEvent.setLocation(eventsDTO.getLocation());
            newEvent.setDateTime(eventsDTO.getDateTime());

            CustomUserDetailsDTO userDetails = (CustomUserDetailsDTO) authentication.getPrincipal();

            if (userDetails == null)
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Organizer not found");
            }

            Users organizer = usersRepository.getReferenceById(userDetails.getUserId());
            newEvent.setOrganizer(organizer);

            ParticipantsRequestDTO participantsRequestDTO = new ParticipantsRequestDTO();

            eventsRepository.save(newEvent);
            participantsRequestDTO.setEventId(newEvent.getEventId());
            participantsRequestDTO.setUserId(organizer.getUserId());
            participantsService.addParticipantToEvent(participantsRequestDTO);
            return ResponseEntity.ok(eventsDTO);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating event");
        }
    }

    @Override
    public ResponseEntity<?> getAllEvents()
    {
        List<Events> events = eventsRepository.findAll();
        if (events.isEmpty())
        {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        else
        {
            return ResponseEntity.ok().body(events);
        }
    }

    @Override
    public ResponseEntity<?> getEventByTitle(String title)
    {
        Events event = eventsRepository.findByTitle(title).orElse(null);

        if (event == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event with title " + title + " not found");
        }
        else
        {
            return ResponseEntity.ok(event);
        }
    }

    @Override
    public ResponseEntity<?> getEventById(UUID eventId)
    {
        try
        {
            Events event = eventsRepository.getReferenceById(eventId);
            return ResponseEntity.ok(event);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event with id " + eventId + " not found");
        }
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseEntity<?> updateEvent(UUID eventId, EventsDTO updatedEvent)
    {
        if (eventsRepository.existsById(eventId))
        {
            Events event = eventsRepository.getReferenceById(eventId);
            event.setTitle(updatedEvent.getTitle());
            event.setDescription(updatedEvent.getDescription());
            event.setLocation(updatedEvent.getLocation());
            event.setDateTime(updatedEvent.getDateTime());

            eventsRepository.save(event);
            return ResponseEntity.ok(event);
        }
        else
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event with id " + eventId + " not found");
        }
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseEntity<?> deleteEvent(UUID eventId)
    {
        if (eventsRepository.existsById(eventId))
        {
            eventsRepository.deleteById(eventId);
            participantsRepository.deleteAllByEvent_EventId(eventId);
            invitationsRepository.deleteAllByEvent_EventId(eventId);
            return ResponseEntity.ok().body("Event with id " + eventId + " has been deleted");
        }
        else
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event with id " + eventId + " not found");
        }
    }

}
