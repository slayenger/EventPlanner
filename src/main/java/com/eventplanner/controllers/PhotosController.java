package com.eventplanner.controllers;


import com.eventplanner.dtos.CustomUserDetailsDTO;
import com.eventplanner.entities.EventPhotos;
import com.eventplanner.exceptions.EmptyListException;
import com.eventplanner.exceptions.InsufficientPermissionException;
import com.eventplanner.exceptions.NotFoundException;
import com.eventplanner.services.api.PhotosService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/photos")
public class PhotosController
{
    private final PhotosService photosService;

    @PostMapping("/upload/{eventId}")
    public ResponseEntity<?> uploadPhoto(@PathVariable UUID eventId,
                                         @RequestParam("file") MultipartFile file,
                                         @AuthenticationPrincipal CustomUserDetailsDTO userDetails) {
        try
        {
            UUID organizerId = userDetails.getUserId();
            String result = photosService.uploadPhoto(eventId, file, organizerId);
            return ResponseEntity.status(HttpStatus.OK).body(result);
        }
        catch (InsufficientPermissionException err)
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(err.getMessage());
        }
        catch (IllegalArgumentException err)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err.getMessage());
        }
        catch (IOException err)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err.getMessage());
        }

    }

    @GetMapping("/{photoId}")
    public ResponseEntity<?> getPhotoById(@PathVariable UUID photoId)
    {
        try
        {
            EventPhotos photo = photosService.getPhotoById(photoId);
            return ResponseEntity.status(HttpStatus.OK).body(photo);
        }
        catch (NotFoundException err)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err.getMessage());
        }
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<?> getPhotosByEvent(@PathVariable UUID eventId,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size)
    {
        try
        {
            Page<EventPhotos> eventPhotos = photosService.getPhotosByEvent(eventId, page, size);
            return ResponseEntity.status(HttpStatus.OK).body(eventPhotos);
        }
        catch (EmptyListException err)
        {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(err.getMessage());
        }
    }

    @DeleteMapping("/{photoId}")
    public ResponseEntity<?> deletePhoto(@PathVariable UUID photoId,
                                         @AuthenticationPrincipal CustomUserDetailsDTO userDetails)
    {
        try
        {
            UUID authenticatedUserId = userDetails.getUserId();
            photosService.deletePhoto(photoId, authenticatedUserId);
            return ResponseEntity.status(HttpStatus.OK).body("Photo with id " + photoId + " was successfully deleted");
        }
        catch (NotFoundException err)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err.getMessage());
        }
        catch (InsufficientPermissionException err)
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(err.getMessage());
        }

    }
}
