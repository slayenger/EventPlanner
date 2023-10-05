package com.eventplanner.services.api;

import com.eventplanner.dtos.EventsDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.UUID;

public interface EventsService {

    ResponseEntity<?> createNewEvent(EventsDTO eventsDTO, Authentication authentication);

    ResponseEntity<?> getAllEvents();

    ResponseEntity<?> getEventByTitle(String title);
    ResponseEntity<?> getEventById(UUID eventId);

    ResponseEntity<?> updateEvent(UUID eventId, EventsDTO updatedEvent);

    ResponseEntity<?> deleteEvent(UUID eventId);

}
