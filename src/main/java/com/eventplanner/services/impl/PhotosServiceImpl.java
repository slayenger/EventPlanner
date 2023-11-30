package com.eventplanner.services.impl;

import com.eventplanner.entities.EventPhotos;
import com.eventplanner.entities.Events;
import com.eventplanner.exceptions.EmptyListException;
import com.eventplanner.exceptions.InsufficientPermissionException;
import com.eventplanner.exceptions.NotFoundException;
import com.eventplanner.repositories.EventPhotosRepository;
import com.eventplanner.repositories.EventsRepository;
import com.eventplanner.services.api.PhotosService;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Implementation of the {@link PhotosService} interface for managing photos associated with events.
 */
@Service
@RequiredArgsConstructor
public class PhotosServiceImpl implements PhotosService
{
    private static final String UPLOAD_DIR = "C:/Users/sinya/IdeaProjects/EventPlannerApp/src/main/resources/static/event_images";
    private final EventPhotosRepository photosRepository;
    private final EventsRepository eventsRepository;
    private final PlatformTransactionManager transactionManager;
    private static final Logger LOGGER = LogManager.getLogger();


    @Override
    public String uploadPhoto(UUID eventId, MultipartFile file, UUID authenticatedUserId) throws IOException
    {
        TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        LOGGER.info("Start transaction");

        Events event = eventsRepository.getReferenceById(eventId);
        if (!event.getOrganizer().getUserId().equals(authenticatedUserId))
        {
            transactionManager.rollback(transaction);
            throw new InsufficientPermissionException("You don't have the rights to upload this photo");
        }

        if (file.isEmpty())
        {
            transactionManager.rollback(transaction);
            throw new IllegalArgumentException("Please select the file to download");
        }

        String eventDirPath = UPLOAD_DIR + File.separator + eventId;
        File eventDir = new File(eventDirPath);
        if (!eventDir.exists())
        {
            boolean dirCreated = eventDir.mkdirs(); // Creating an event folder if it does not exist
            if (!dirCreated)
            {
                transactionManager.rollback(transaction);
                throw new IOException("Failed to create event directory");
            }
        }

        String fileName = file.getOriginalFilename();
        String filePath = eventDirPath + File.separator + fileName;
        File dest = new File(filePath);
        file.transferTo(dest); //save photo in event folder

        EventPhotos eventPhoto = new EventPhotos();
        eventPhoto.setEvent(eventsRepository.getReferenceById(eventId));
        eventPhoto.setPath(filePath);
        photosRepository.save(eventPhoto);

        transactionManager.commit(transaction);
        return "Photo was uploaded to event with id " + eventId + "\n Path: " + filePath;
    }

    @Override
    public EventPhotos getPhotoById(UUID photoId) throws NotFoundException
    {

        if (!photosRepository.existsById(photoId))
        {
            throw new NotFoundException("Photo with id " + photoId + " not found");
        }

        return photosRepository.getReferenceById(photoId);
    }

    @Override
    public Page<EventPhotos> getPhotosByEvent(UUID eventId, int page, int size)
    {
        Pageable pageable = PageRequest.of(page, size);
        Page<EventPhotos> eventPhotosList = photosRepository.findAllByEvent_EventId(eventId, pageable);
        if (eventPhotosList.isEmpty())
        {
            throw new EmptyListException("There are no photos for event with id " + eventId);
        }
        else
        {
            return eventPhotosList;
        }
    }

    @Override
    public void deletePhoto(UUID photoId, UUID authenticatedUserId)
    {
        TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        LOGGER.info("Start transaction");

        if (!photosRepository.existsById(photoId))
        {
            transactionManager.rollback(transaction);
            throw new NotFoundException("Photo with id " + photoId + " not found");
        }

        UUID eventId = photosRepository.getReferenceById(photoId).getEvent().getEventId();
        Events event = eventsRepository.getReferenceById(eventId);

        if (!event.getOrganizer().getUserId().equals(authenticatedUserId))
        {
            transactionManager.rollback(transaction);
            throw new InsufficientPermissionException("You don't have the rights to delete this photo");
        }

        try
        {
            Path imagePath = Paths.get(photosRepository.getReferenceById(photoId).getPath());
            Files.delete(imagePath);
        }
        catch (IOException e)
        {
            transactionManager.rollback(transaction);
            e.printStackTrace();
        }

        photosRepository.deleteById(photoId);
        transactionManager.commit(transaction);
    }
}
