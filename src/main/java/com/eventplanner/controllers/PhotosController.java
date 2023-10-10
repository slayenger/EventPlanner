package com.eventplanner.controllers;


import com.eventplanner.services.api.PhotosService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
                                         Authentication authentication) throws IOException
    {
        return photosService.uploadPhoto(eventId, file, authentication);
    }

    @GetMapping("/{photoId}")
    public ResponseEntity<?> getPhotoById(@PathVariable UUID photoId)
    {
        return photosService.getPhotoById(photoId);
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<?> getPhotosByEvent(@PathVariable UUID eventId)
    {
        return photosService.getPhotosByEvent(eventId);
    }

    @DeleteMapping("/{photoId}")
    public ResponseEntity<?> deletePhoto(@PathVariable UUID photoId, Authentication authentication)
    {
        return photosService.deletePhoto(photoId, authentication);
    }
}
