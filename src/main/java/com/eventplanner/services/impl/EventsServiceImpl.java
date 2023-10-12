package com.eventplanner.services.impl;

import com.eventplanner.dtos.CustomUserDetailsDTO;
import com.eventplanner.dtos.EventsDTO;
import com.eventplanner.dtos.ParticipantsRequestDTO;
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

/**
 * Implementation of the {@link EventsService} interface for managing events in the application.
 */
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

    /**
     * Creates a new event with the provided event data.
     * At the beginning of the method, the main fields from the EventsDTO are set, then the event organizer is set using authentication.
     * After that, the organizer himself is added to the list of participants of this event
     *
     * @param eventsDTO       The data of the event to create.
     * @param authentication  The authentication object of the currently logged-in user.
     * @return ResponseEntity containing the created event if successful, or an error message if it fails.
     */
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

    /**
     * Retrieves a list of all available events.
     *
     * @return ResponseEntity containing a list of events if successful, or an error message if it fails.
     */
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

    /**
     * Retrieves an event by its title.
     *
     * @param title The title of the event to retrieve.
     * @return ResponseEntity containing the event if found, or an error message if it doesn't exist.
     */
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

    /**
     * Retrieves an event by its unique identifier.
     *
     * @param eventId The unique identifier of the event to retrieve.
     * @return ResponseEntity containing the event if found, or an error message if it doesn't exist.
     */
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

    /**
     * Updates an existing event with the provided event data.
     *
     * @param eventId        The unique identifier of the event to update.
     * @param updatedEvent  The updated data of the event.
     * @return ResponseEntity containing the updated event if successful, or an error message if it fails.
     */
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

    /**
     * Deletes an event with the specified unique identifier.
     * At the beginning of the method, the deletion access rights are checked (the user can only delete his account) using authentication.
     * Further, after checking the existence of an event with eventID, all photos of this event, its participants and invitations are deleted
     *
     * @param eventId         The unique identifier of the event to delete.
     * @param authentication  The authentication object of the currently logged-in user.
     * @return ResponseEntity with a success message if the event is deleted, or an error message if it fails.
     */
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

        // Delete all photos from the file system if the folder exists
        if (eventDir.exists())
        {
            try
            {
                FileUtils.deleteDirectory(eventDir); // Deleting folder
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
