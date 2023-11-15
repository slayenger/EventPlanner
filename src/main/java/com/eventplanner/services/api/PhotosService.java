package com.eventplanner.services.api;

import com.eventplanner.entities.EventPhotos;
import com.eventplanner.exceptions.EmptyListException;
import com.eventplanner.exceptions.InsufficientPermissionException;
import com.eventplanner.exceptions.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * Service interface for managing photos associated with events.
 */
public interface PhotosService {

    /**
     * Uploads a photo to the specified event, ensuring the user has the necessary permissions.
     *
     * @param eventId      The unique identifier of the event.
     * @param file         The MultipartFile representing the photo to be uploaded.
     * @param authenticatedUserId  The unique identifier of the user attempting to upload the photo.
     * @return A message indicating the success of the photo upload, along with the path where the photo is stored.
     * @throws IOException                    If an I/O error occurs during the photo-saving process.
     * @throws InsufficientPermissionException If the user lacks the necessary rights to upload the photo.
     * @throws IllegalArgumentException        If the provided file is empty.
     *
     * @apiNote This method saves the uploaded photo to the event's
     *          directory and updates the associated database records.
     */
    String uploadPhoto(UUID eventId, MultipartFile file, UUID authenticatedUserId) throws IOException;

    /**
     * Retrieves a photo by its unique identifier.
     *
     * @param photoId The unique identifier of the photo.
     * @return The EventPhotos entity representing the photo.
     * @throws NotFoundException If no photo is found with the specified identifier.
     */
    EventPhotos getPhotoById(UUID photoId);

    /**
     * Retrieves a paginated list of photos for a specific event.
     *
     * @param eventId The unique identifier of the event.
     * @param page    The page number (zero-based) to retrieve.
     * @param size    The number of photos per page.
     * @return A Page containing EventPhotos entities for the specified event.
     * @throws EmptyListException If there are no photos for the specified event.
     */
    Page<EventPhotos> getPhotosByEvent(UUID eventId, int page, int size);

    /**
     * Deletes a photo with the specified ID, provided the authenticated user has the necessary permissions.
     *
     * @param photoId             The unique identifier of the photo to be deleted.
     * @param authenticatedUserId The unique identifier of the authenticated user attempting to delete the photo.
     * @throws NotFoundException            If the photo with the specified ID is not found.
     * @throws InsufficientPermissionException If the authenticated user does not have the rights to delete the photo.
     */
    void deletePhoto(UUID photoId, UUID authenticatedUserId);


}
