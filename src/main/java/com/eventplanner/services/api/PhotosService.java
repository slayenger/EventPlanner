package com.eventplanner.services.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

public interface PhotosService {

    ResponseEntity<?> uploadPhoto(UUID eventId, MultipartFile file, Authentication authentication) throws IOException;

    ResponseEntity<?> getPhotoById(UUID photoId);

    ResponseEntity<?> getPhotosByEvent(UUID eventId);

    ResponseEntity<?> deletePhoto(UUID photoId, Authentication authentication);


}
