package com.eventplanner.services.impl;

import com.eventplanner.dtos.CustomUserDetailsDTO;
import com.eventplanner.dtos.EventsDTO;
import com.eventplanner.dtos.ParticipantsRequestDTO;
import com.eventplanner.entities.EventPhotos;
import com.eventplanner.entities.Events;
import com.eventplanner.entities.Users;
import com.eventplanner.repositories.*;
import com.eventplanner.services.api.EventsService;
import com.eventplanner.services.api.ParticipantsService;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
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
    private final EventPhotosRepository photosRepository;
    private static final String UPLOAD_DIR = "C:/Users/sinya/IdeaProjects/EventPlannerApp/src/main/resources/static/event_images";

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseEntity<?> createNewEvent(EventsDTO eventsDTO, Authentication authentication)
    {
        try
        {
            Events newEvent = new Events();
            newEvent.setTitle(eventsDTO.getTitle());
            newEvent.setDescription(eventsDTO.getDescription());
            newEvent.setLocation(eventsDTO.getLocation());
            newEvent.setDateTime(eventsDTO.getDateTime());

            CustomUserDetailsDTO userDetails = (CustomUserDetailsDTO) authentication.getPrincipal();

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
    public ResponseEntity<?> deleteEvent(UUID eventId, Authentication authentication)
    {
        CustomUserDetailsDTO userDetailsDTO = (CustomUserDetailsDTO) authentication.getPrincipal();
        Events event = eventsRepository.getReferenceById(eventId);

        if (!event.getOrganizer().getUserId().equals(userDetailsDTO.getUserId()))
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("You don't have the rights to upload this event");
        }

        if (!eventsRepository.existsById(eventId))
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Event with id " + eventId + " not found");
        }

        String eventDirPath = UPLOAD_DIR + File.separator + eventId.toString();
        File eventDir = new File(eventDirPath);

        // Удалите все фотографии из файловой системы, если папка существует
        if (eventDir.exists())
        {
            try
            {
                FileUtils.deleteDirectory(eventDir); // Используйте Apache Commons IO для удаления папки
            }
            catch (IOException e)
            {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting event folder");
            }
        }

        photosRepository.deleteAllByEvent_EventId(eventId);
        eventsRepository.deleteById(eventId);
        participantsRepository.deleteAllByEvent_EventId(eventId);
        invitationsRepository.deleteAllByEvent_EventId(eventId);
        return ResponseEntity.ok().body("Event with id " + eventId + " has been deleted");
    }

}
