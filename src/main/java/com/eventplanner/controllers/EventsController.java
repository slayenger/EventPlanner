package com.eventplanner.controllers;

import com.eventplanner.dtos.CustomUserDetailsDTO;
import com.eventplanner.dtos.EventsDTO;
import com.eventplanner.entities.Events;
import com.eventplanner.exceptions.EmptyListException;
import com.eventplanner.exceptions.InsufficientPermissionException;
import com.eventplanner.exceptions.NotFoundException;
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
        catch (EmptyListException e)
        {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createNewEvent(@RequestBody EventsDTO eventsDTO,
                                            @AuthenticationPrincipal CustomUserDetailsDTO userDetails)
    {
        try
        {
            eventsService.createNewEvent(eventsDTO, userDetails.getUserId());
            return ResponseEntity.ok("Event created successfully");
        }
        catch (NotFoundException e)
        {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not found");
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
    public ResponseEntity<?> updateEvent(@PathVariable UUID eventId,
                                         @RequestBody EventsDTO updatedEvent,
                                         @AuthenticationPrincipal CustomUserDetailsDTO userDetails)
    {
        try
        {
            UUID authenticatedUserId = userDetails.getUserId();
            return ResponseEntity.status(HttpStatus.OK).body(eventsService.updateEvent(eventId,updatedEvent, authenticatedUserId));
        }
        catch (InsufficientPermissionException err)
        {
            err.printStackTrace();
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(err.getMessage());
        }
        catch (NotFoundException err)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err.getMessage());
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
        catch (NotFoundException err)
        {
            err.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err.getMessage());
        }
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<?> deleteEvent(@PathVariable UUID eventId, @AuthenticationPrincipal CustomUserDetailsDTO userDetails)
    {
        try
        {
            UUID authenticatedUserId = userDetails.getUserId();
            eventsService.deleteEvent(eventId, authenticatedUserId);
            return ResponseEntity.status(HttpStatus.OK).body("Event with id " + eventId + " has been deleted");
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
