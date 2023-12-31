package com.eventplanner.services.impl;

import com.eventplanner.dtos.EventsDTO;
import com.eventplanner.dtos.ParticipantDTO;
import com.eventplanner.entities.Events;
import com.eventplanner.entities.Users;
import com.eventplanner.exceptions.EmptyListException;
import com.eventplanner.exceptions.InsufficientPermissionException;
import com.eventplanner.exceptions.NotFoundException;
import com.eventplanner.mappers.EventsMapper;
import com.eventplanner.mappers.ParticipantsMapper;
import com.eventplanner.repositories.*;
import com.eventplanner.services.api.EventsService;
import com.eventplanner.services.api.ParticipantsService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
    private final EventsMapper eventsMapper;
    private final ParticipantsMapper participantsMapper;
    private final PlatformTransactionManager transactionManager;
    private static final Logger LOGGER = LogManager.getLogger();


    @Override
    public void createNewEvent(EventsDTO eventsDTO, UUID organizerId) throws NotFoundException
    {
        TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        LOGGER.info("Start transaction");

        if (!usersRepository.existsById(organizerId))
        {
            throw new NotFoundException("User with id " + organizerId + " not found");
        }
        Events newEvent = eventsMapper.toEvent(eventsDTO);
        Users organizer = usersRepository.getReferenceById(organizerId);
        newEvent.setOrganizer(organizer);
        eventsRepository.save(newEvent);

        ParticipantDTO participantDTO = participantsMapper.toDTO(newEvent.getEventId(),organizerId);
        participantsService.addParticipantToEvent(participantDTO);

        transactionManager.commit(transaction);
    }

    @Override
    public Page<Events> getAllEvents(int page, int size) throws EmptyListException
    {
        Pageable pageable = PageRequest.of(page, size);
        Page<Events> events = eventsRepository.findAll(pageable);
        if (events.isEmpty())
        {
            throw new EmptyListException("At the moment there is no event");
        }
        else
        {
            return events;
        }
    }

    @Override
    public Events getEventByTitle(String title) throws NotFoundException
    {
        Events event = eventsRepository.findByTitle(title).orElse(null);

        if (event == null)
        {
            throw new NotFoundException("Event with title " + title + " not found");
        }
        else
        {
            return event;
        }
    }

    @Override
    public Events getEventById(UUID eventId) throws NotFoundException
    {
        if (eventsRepository.existsById(eventId))
        {
            return eventsRepository.getReferenceById(eventId);
        }
        else
        {
            throw new NotFoundException("Event with id " + eventId + " not found");
        }
    }

    @Override
    public Events updateEvent(UUID eventId, EventsDTO updatedEvent, UUID authenticatedUserId)
            throws NotFoundException, InsufficientPermissionException
    {
        TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        LOGGER.info("Start transaction");

        Events event = eventsRepository.getReferenceById(eventId);
        if (!event.getOrganizer().getUserId().equals(authenticatedUserId)) {
            throw new InsufficientPermissionException("You don't have the rights to delete this event");
        }
        if (eventsRepository.existsById(eventId))
        {
            eventsMapper.update(updatedEvent, event);
            eventsRepository.save(event);
            transactionManager.commit(transaction);
            return event;
        }
        else
        {
            transactionManager.rollback(transaction);
            throw new NotFoundException("Event with id " + eventId + " not found");
        }
    }

    @Override
    public void deleteEvent(UUID eventId, UUID authenticatedUserId)
            throws NotFoundException, InsufficientPermissionException {
        TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        LOGGER.info("Start transaction");

        if (!eventsRepository.existsById(eventId))
        {
            transactionManager.rollback(transaction);
            throw new NotFoundException("Event with id " + eventId + " not found");
        }

        Events event = eventsRepository.getReferenceById(eventId);
        if (!event.getOrganizer().getUserId().equals(authenticatedUserId)) {
            transactionManager.rollback(transaction);
            throw new InsufficientPermissionException("You don't have the rights to delete this event");
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
            catch (FileNotFoundException err)
            {
                transactionManager.rollback(transaction);
                throw new NotFoundException("File not found, ", err);
            }
            catch (IOException e)
            {
                transactionManager.rollback(transaction);
                e.printStackTrace();
                throw new RuntimeException("Error deleting event folder");
            }
        }

        photosRepository.deleteAllByEvent_EventId(eventId);
        participantsRepository.deleteAllByEvent_EventId(eventId);
        invitationsRepository.deleteAllByEvent_EventId(eventId);
        eventsRepository.deleteById(eventId);

        transactionManager.commit(transaction);
    }
}
