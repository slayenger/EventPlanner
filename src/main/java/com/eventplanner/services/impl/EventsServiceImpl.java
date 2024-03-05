package com.eventplanner.services.impl;

import com.eventplanner.dtos.EventsRequestDTO;
import com.eventplanner.dtos.EventsResponseDTO;
import com.eventplanner.entities.EmailConfirmation;
import com.eventplanner.entities.Events;
import com.eventplanner.entities.User;
import com.eventplanner.exceptions.*;
import com.eventplanner.mappers.EventsMapper;
import com.eventplanner.repositories.*;
import com.eventplanner.services.api.EventsService;
import com.eventplanner.services.api.ParticipantsService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of the {@link EventsService} interface for managing events in the application.
 */
@Service
@RequiredArgsConstructor
//TODO добавить валидацию на недопустимые символы в названии, описании и тд...
public class EventsServiceImpl implements EventsService {

    private final EventsRepository eventsRepository;
    private final UsersRepository usersRepository;
    private final ParticipantsService participantsService;
    private final EventParticipantsRepository participantsRepository;
    private final EventInvitationsRepository invitationsRepository;
    private final EventPhotosRepository photosRepository;
    private final EmailConfirmationRepository emailConfirmationRepository;
    private static final String UPLOAD_DIR = "C:/User/sinya/IdeaProjects/EventPlannerApp/src/main/resources/static/event_images";
    private final EventsMapper eventsMapper;
    private final PlatformTransactionManager transactionManager;
    private static final Logger LOGGER = LogManager.getLogger();


    @Override
    public void createNewEvent(EventsRequestDTO eventsRequestDTO, UUID organizerId) throws NotFoundException, ParseException {
        TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        LOGGER.info("Start transaction");

        EmailConfirmation emailConfirmation = emailConfirmationRepository.findByUser_UserId(organizerId);
        if (!emailConfirmation.isEmailConfirmed())
        {
            throw new EmailNotConfirmedException("To create an event, you need to confirm your email address.");
        }
        Events newEvent = eventsMapper.toEvent(eventsRequestDTO);
        User organizer = usersRepository.getReferenceById(organizerId);
        newEvent.setOrganizer(organizer);
        eventsRepository.save(newEvent);

        participantsService.addParticipantToEvent(newEvent.getEventId(),organizerId);

        transactionManager.commit(transaction);
    }

    @Override
    public Page<EventsResponseDTO> getAllEvents(int page, int size) throws EmptyListException
    {
        Pageable pageable = PageRequest.of(page, size);
        List<Events> events = eventsRepository.findAll();
        List<EventsResponseDTO> eventsDTO = new ArrayList<>();
        for (Events event: events)
        {
            eventsDTO.add(eventsMapper.toDTO(event));
        }
        Page<EventsResponseDTO> eventsPage = new PageImpl<>(eventsDTO,pageable,size);

        if (events.isEmpty())
        {
            throw new EmptyListException("At the moment there is no event");
        }
        else
        {
            return eventsPage;
        }
    }

    @Override
    public EventsResponseDTO getEventByTitle(String title) throws NotFoundException
    {
        Events event = eventsRepository.findByTitle(title).orElse(null);
        if (event == null)
        {
            throw new NotFoundException("Event with title " + title + " not found");
        }
        else
        {
            return eventsMapper.toDTO(event);
        }
    }

    @Override
    public EventsResponseDTO getEventById(UUID eventId) throws NotFoundException
    {
        if (eventsRepository.existsById(eventId))
        {
            Events event = eventsRepository.getReferenceById(eventId);
            return eventsMapper.toDTO(event);
        }
        else
        {
            throw new NotFoundException("Event with id " + eventId + " not found");
        }
    }

    @Override
    public void updateEvent(UUID eventId, EventsRequestDTO updatedEvent, UUID authenticatedUserId)
            throws NotFoundException, InsufficientPermissionException, ParseException {
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
