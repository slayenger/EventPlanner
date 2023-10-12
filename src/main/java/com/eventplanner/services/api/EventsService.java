package com.eventplanner.services.api;

import com.eventplanner.dtos.EventsDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.UUID;

/**
 * Service interface for managing events in the application.
 */
public interface EventsService {

    /**
     * Creates a new event with the provided event data.
     *
     * @param eventsDTO       The data of the event to create.
     * @param authentication  The authentication object of the currently logged-in user.
     * @return ResponseEntity containing the created event if successful, or an error message if it fails.
     */
    ResponseEntity<?> createNewEvent(EventsDTO eventsDTO, Authentication authentication);

    /**
     * Retrieves a list of all available events.
     *
     * @return ResponseEntity containing a list of events if successful, or an error message if it fails.
     */
    ResponseEntity<?> getAllEvents();

    /**
     * Retrieves an event by its title.
     *
     * @param title The title of the event to retrieve.
     * @return ResponseEntity containing the event if found, or an error message if it doesn't exist.
     */
    ResponseEntity<?> getEventByTitle(String title);

    /**
     * Retrieves an event by its unique identifier.
     *
     * @param eventId The unique identifier of the event to retrieve.
     * @return ResponseEntity containing the event if found, or an error message if it doesn't exist.
     */
    ResponseEntity<?> getEventById(UUID eventId);

    /**
     * Updates an existing event with the provided event data.
     *
     * @param eventId       The unique identifier of the event to update.
     * @param updatedEvent  The updated data of the event.
     * @return ResponseEntity containing the updated event if successful, or an error message if it fails.
     */
    ResponseEntity<?> updateEvent(UUID eventId, EventsDTO updatedEvent);

    /**
     * Deletes an event with the specified unique identifier.
     *
     * @param eventId         The unique identifier of the event to delete.
     * @param authentication  The authentication object of the currently logged-in user.
     * @return ResponseEntity with a success message if the event is deleted, or an error message if it fails.
     */
    ResponseEntity<?> deleteEvent(UUID eventId, Authentication authentication);

}
