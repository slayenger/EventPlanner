package com.eventplanner.services.impl;

import com.eventplanner.dtos.CustomUserDetailsDTO;
import com.eventplanner.dtos.EventsDTO;
import com.eventplanner.dtos.EventsResponseDTO;
import com.eventplanner.dtos.ParticipantDTO;
import com.eventplanner.entities.Events;
import com.eventplanner.entities.Users;
import com.eventplanner.mapper.EventsMapper;
import com.eventplanner.mapper.ParticipantsMapper;
import com.eventplanner.repositories.*;
import com.eventplanner.services.api.EventsService;
import com.eventplanner.services.api.ParticipantsService;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import java.util.stream.Collectors;

/**
 * Implementation of the {@link EventsService} interface for managing events in the application.
 */
// TODO refactor all service
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
    private final EventsMapper eventsMapper;
    private final ParticipantsMapper participantsMapper;

    /**
     * Creates a new event with the provided event data.
     * At the beginning of the method, the main fields from the EventsDTO are set, then the event organizer is set using authentication.
     * After that, the organizer himself is added to the list of participants of this event
     *
     * @param eventsDTO       The data of the event to create.
     *
     * @return ResponseEntity containing the created event if successful, or an error message if it fails.
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    // TODO create custom exceptions
    // TODO transaction handling
    public void createNewEvent(EventsDTO eventsDTO, UUID organizerId)
    {
        try
        {
                Events newEvent = eventsMapper.toEvent(eventsDTO);
                Users organizer = usersRepository.getReferenceById(organizerId);
                newEvent.setOrganizer(organizer);
                eventsRepository.save(newEvent);

                ParticipantDTO participantDTO = participantsMapper.toDTO(newEvent.getEventId(),organizerId);
                participantsService.addParticipantToEvent(participantDTO);
        }
        catch (RuntimeException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Error creating event");
        }
    }

    /**
     * Retrieves a list of all available events.
     *
     * @return ResponseEntity containing a list of events if successful, or an error message if it fails.
     */
    @Override
    public Page<Events> getAllEvents(int page, int size)
    {
        Pageable pageable = PageRequest.of(page, size);
        Page<Events> events = eventsRepository.findAll(pageable);
        if (events.isEmpty())
        {
            // TODO create custom exception
            throw new RuntimeException("empty list");
        }
        else
        {
            return events;
        }
    }

    /**
     * Retrieves an event by its title.
     *
     * @param title The title of the event to retrieve.
     * @return ResponseEntity containing the event if found, or an error message if it doesn't exist.
     */
    @Override
    public Events getEventByTitle(String title)
    {
        Events event = eventsRepository.findByTitle(title).orElse(null);

        if (event == null)
        {
            throw new RuntimeException("Event with title " + title + " not found");
        }
        else
        {
            return event;
        }
    }

    /**
     * Retrieves an event by its unique identifier.
     *
     * @param eventId The unique identifier of the event to retrieve.
     * @return ResponseEntity containing the event if found, or an error message if it doesn't exist.
     */
    @Override
    public Events getEventById(UUID eventId)
    {
        try
        {
            return eventsRepository.getReferenceById(eventId);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException("Event with id " + eventId + " not found");
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
    public Events updateEvent(UUID eventId, EventsDTO updatedEvent)
    {
        if (eventsRepository.existsById(eventId))
        {
            Events event = eventsRepository.getReferenceById(eventId);
            eventsMapper.update(updatedEvent, event);
            eventsRepository.save(event);
            return event;
        }
        else
        {
           throw new RuntimeException("Event with id " + eventId + " not found");
        }
    }

    /**
     * Deletes an event with the specified unique identifier.
     * At the beginning of the method, the deletion access rights are checked (the user can only delete his account) using authentication.
     * Further, after checking the existence of an event with eventID, all photos of this event, its participants and invitations are deleted
     *
     * @param eventId         The unique identifier of the event to delete.
     *
     * @return ResponseEntity with a success message if the event is deleted, or an error message if it fails.
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteEvent(UUID eventId)
    {
        if (!eventsRepository.existsById(eventId))
        {
            throw new RuntimeException("Event with id " + eventId + " not found");
        }

        String eventDirPath = UPLOAD_DIR + File.separator + eventId;
        File eventDir = new File(eventDirPath);

        // Delete all photos from the file system if the folder exists
        if (eventDir.exists())
        {
            try
            {
                FileUtils.deleteDirectory(eventDir); // Deleting folder
            }
            // TODO add catch with NotFoundPath exception
            catch (IOException e)
            {
                e.printStackTrace();
                throw new RuntimeException("Error deleting event folder");
            }
        }

        photosRepository.deleteAllByEvent_EventId(eventId);
        participantsRepository.deleteAllByEvent_EventId(eventId);
        invitationsRepository.deleteAllByEvent_EventId(eventId);
        eventsRepository.deleteById(eventId);
    }

}
