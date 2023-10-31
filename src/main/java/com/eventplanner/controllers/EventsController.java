package com.eventplanner.controllers;

import com.eventplanner.dtos.CustomUserDetailsDTO;
import com.eventplanner.dtos.EventsDTO;
import com.eventplanner.entities.Events;
import com.eventplanner.services.api.EventsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/events")
public class EventsController {

    private final EventsService eventsService;

    @GetMapping
    public ResponseEntity<?> getAllEvents(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size)
    {
        try
        {
            Page<Events> events = eventsService.getAllEvents(page, size);
            return ResponseEntity.ok().body(events);
        }
        catch (RuntimeException e)
        {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createNewEvent(@RequestBody EventsDTO eventsDTO, @AuthenticationPrincipal CustomUserDetailsDTO userDetails)
    {
        try
        {
            eventsService.createNewEvent(eventsDTO, userDetails.getUserId());
            return ResponseEntity.ok("Event created successfully");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating event");
        }
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<?> getEventById(@PathVariable UUID eventId)
    {
        try
        {
            Events event = eventsService.getEventById(eventId);
            return ResponseEntity.status(HttpStatus.OK).body(event);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Event with id " + eventId + " not found");
        }

    }

    @PutMapping("/{eventId}")
    public ResponseEntity<?> updateEvent(@PathVariable UUID eventId, @RequestBody EventsDTO updatedEvent,
                                         @AuthenticationPrincipal CustomUserDetailsDTO userDetails)
    {
        try
        {
            Events event = eventsService.getEventById(eventId);
            if (!event.getOrganizer().getUserId().equals(userDetails.getUserId()))
            {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("You don't have the rights to update this event");
            }
            return ResponseEntity.status(HttpStatus.OK).body(eventsService.updateEvent(eventId,updatedEvent));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Event with id " + eventId + " not found");
        }

    }

    @GetMapping("by-title/{title}")
    public ResponseEntity<?> getEventByTitle(@PathVariable String title)
    {
        try
        {
            Events event = eventsService.getEventByTitle(title);
            return ResponseEntity.status(HttpStatus.OK).body(event);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event with title " + title + " not found");
        }
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<?> deleteEvent(@PathVariable UUID eventId, @AuthenticationPrincipal CustomUserDetailsDTO userDetails)
    {
        try
        {
            Events event = eventsService.getEventById(eventId);
            if (!event.getOrganizer().getUserId().equals(userDetails.getUserId())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("You don't have the rights to delete this event");
            }
            eventsService.deleteEvent(eventId);
            return ResponseEntity.status(HttpStatus.OK).body("Event with id " + eventId + " has been deleted");
        }
        catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error:(");
        }
    }
}
