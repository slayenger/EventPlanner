package com.eventplanner.controllers;

import com.eventplanner.dtos.EventsDTO;
import com.eventplanner.services.api.EventsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/events")
public class EventsController {

    private final EventsService eventsService;

    @GetMapping
    public ResponseEntity<?> getAllEvents()
    {
      return eventsService.getAllEvents();
    }

    @PostMapping
    public ResponseEntity<?> createNewEvent(@RequestBody EventsDTO eventsDTO, Authentication authentication)
    {
        return eventsService.createNewEvent(eventsDTO, authentication);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<?> getEventById(@PathVariable UUID eventId)
    {
        return eventsService.getEventById(eventId);
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<?> updateEvent(@PathVariable UUID eventId, @RequestBody EventsDTO updatedEvent)
    {
        return eventsService.updateEvent(eventId,updatedEvent);
    }

    @GetMapping("by-title/{title}")
    public ResponseEntity<?> getEventByTitle(@PathVariable String title)
    {
        return eventsService.getEventByTitle(title);
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<?> deleteEvent(@PathVariable UUID eventId)
    {
        return eventsService.deleteEvent(eventId);
    }
}
