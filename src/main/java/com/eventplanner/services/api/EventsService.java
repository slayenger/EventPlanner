package com.eventplanner.services.api;

import com.eventplanner.dtos.EventsRequestDTO;
import com.eventplanner.dtos.EventsResponseDTO;
import com.eventplanner.exceptions.EmptyListException;
import com.eventplanner.exceptions.NotFoundException;
import com.eventplanner.exceptions.ParseException;
import org.springframework.data.domain.Page;

import java.util.UUID;

/**
 * Service interface for managing events in the application.
 */
public interface EventsService {

    /**
     * Creates a new event based on the provided {@link EventsRequestDTO} and organizer ID.
     *
     * @param eventsRequestDTO   The DTO containing event details.
     * @param organizerId The ID of the organizer user.
     * @throws NotFoundException If the organizer user with the given ID is not found.
     */
    void createNewEvent(EventsRequestDTO eventsRequestDTO, UUID organizerId) throws ParseException;

    /**
     * Retrieves a paginated list of all events.
     *
     * @param page The page number (0-indexed) to retrieve.
     * @param size The number of events per page.
     * @return A {@link Page} containing events.
     * @throws EmptyListException If there are no events available.
     */
    Page<EventsResponseDTO> getAllEvents(int page, int size);

    /**
     * Retrieves an event by its title.
     *
     * @param title The title of the event to retrieve.
     * @return The event with the specified title.
     * @throws NotFoundException If no event is found with the given title.
     */
    EventsResponseDTO getEventByTitle(String title);

    /**
     * Retrieves an event by its unique identifier.
     *
     * @param eventId The unique identifier of the event to retrieve.
     * @return The event with the specified identifier.
     * @throws NotFoundException If no event is found with the given identifier.
     */

    EventsResponseDTO getEventById(UUID eventId);

    /**
     * Updates an existing event with the provided data.
     *
     * @param eventId      The unique identifier of the event to update.
     * @param updatedEvent The updated data for the event.
     * @return The updated event.
     * @throws NotFoundException If no event is found with the given identifier.
     */
    void updateEvent(UUID eventId, EventsRequestDTO updatedEvent, UUID authenticatedUserId) throws com.eventplanner.exceptions.ParseException;

    /**
     * Deletes an event along with associated data.
     *
     * @param eventId The unique identifier of the event to delete.
     * @throws NotFoundException If no event is found with the given identifier.
     */
    void deleteEvent(UUID eventId, UUID authenticatedUserId);

}
