package com.eventplanner.services.impl;

import com.eventplanner.dtos.CustomUserDetailsDTO;
import com.eventplanner.entities.EventPhotos;
import com.eventplanner.entities.Events;
import com.eventplanner.repositories.EventPhotosRepository;
import com.eventplanner.repositories.EventsRepository;
import com.eventplanner.services.api.PhotosService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PhotosServiceImpl implements PhotosService
{
    private static final String UPLOAD_DIR = "C:/Users/sinya/IdeaProjects/EventPlannerApp/src/main/resources/static/event_images";
    private final EventPhotosRepository photosRepository;
    private final EventsRepository eventsRepository;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseEntity<?> uploadPhoto(UUID eventId, MultipartFile file, Authentication authentication)
    {
        CustomUserDetailsDTO userDetailsDTO = (CustomUserDetailsDTO) authentication.getPrincipal();
        Events event = eventsRepository.getReferenceById(eventId);
        if (!event.getOrganizer().getUserId().equals(userDetailsDTO.getUserId()))
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You don't have the rights to upload this photo");
        }

        try
        {
            if (file.isEmpty())
            {
                return ResponseEntity.badRequest().body("Please select the file to download");
            }

            String eventDirPath = UPLOAD_DIR + File.separator + eventId.toString();
            File eventDir = new File(eventDirPath);
            if (!eventDir.exists())
            {
                boolean dirCreated = eventDir.mkdirs(); // Creating an event folder if it does not exist
                if (!dirCreated)
                {
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

            return ResponseEntity.ok()
                    .body("Photo was uploaded to event with id " + eventId + "\n Path: " + filePath);
        }
        catch (IOException e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File upload error");
        }
    }

    @Override
    public ResponseEntity<?> getPhotoById(UUID photoId)
    {

        if (!photosRepository.existsById(photoId))
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Photo with id " + photoId + " not found");
        }

        EventPhotos photo = photosRepository.getReferenceById(photoId);

        return ResponseEntity.ok(photo);

    }

    @Override
    public ResponseEntity<?> getPhotosByEvent(UUID eventId)
    {
        List<EventPhotos> eventPhotosList = photosRepository.findAllByEvent_EventId(eventId);

        if (eventPhotosList.isEmpty())
        {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("There are no photos for this event");
        }
        else
        {
            return ResponseEntity.ok(eventPhotosList);
        }
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseEntity<?> deletePhoto(UUID photoId, Authentication authentication)
    {
        if (!photosRepository.existsById(photoId))
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Photo with id " + photoId + " not found");
        }

        CustomUserDetailsDTO userDetailsDTO = (CustomUserDetailsDTO) authentication.getPrincipal();
        UUID eventId = photosRepository.getReferenceById(photoId).getEvent().getEventId();
        Events event = eventsRepository.getReferenceById(eventId);

        if (!event.getOrganizer().getUserId().equals(userDetailsDTO.getUserId()))
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You don't have the rights to delete this photo");
        }

        try
        {
            Path imagePath = Paths.get(photosRepository.getReferenceById(photoId).getPath());
            Files.delete(imagePath);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting photo file");
        }

        photosRepository.deleteById(photoId);
        return ResponseEntity.ok().body("Photo with id " + photoId + " was successfully deleted");
    }
}
