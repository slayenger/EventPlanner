package com.eventplanner.services.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * Service interface for managing photos associated with events.
 */
public interface PhotosService {

    /**
     * Uploads a photo for a specific event.
     *
     * @param eventId         The unique identifier of the event.
     * @param file            The photo file to upload.
     * @param authentication  The authentication information of the user performing the upload.
     * @return A ResponseEntity with a success message or an error message.
     * @throws IOException If an I/O error occurs during photo upload.
     */
    ResponseEntity<?> uploadPhoto(UUID eventId, MultipartFile file, Authentication authentication) throws IOException;

    /**
     * Retrieves a photo by its unique identifier.
     *
     * @param photoId The unique identifier of the photo to retrieve.
     * @return A ResponseEntity containing the photo data or an error message.
     */
    ResponseEntity<?> getPhotoById(UUID photoId);

    /**
     * Retrieves all photos associated with a specific event.
     *
     * @param eventId The unique identifier of the event.
     * @return A ResponseEntity containing a list of photos or a message indicating no photos were found.
     */
    ResponseEntity<?> getPhotosByEvent(UUID eventId);

    /**
     * Deletes a photo by its unique identifier.
     *
     * @param photoId        The unique identifier of the photo to delete.
     * @param authentication  The authentication information of the user performing the deletion.
     * @return A ResponseEntity with a success message or an error message.
     */
    ResponseEntity<?> deletePhoto(UUID photoId, Authentication authentication);


}
