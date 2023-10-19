package com.eventplanner.services.impl;

import com.eventplanner.dtos.CustomUserDetailsDTO;
import com.eventplanner.dtos.EventsDTO;
import com.eventplanner.entities.Events;
import com.eventplanner.entities.Users;
import com.eventplanner.repositories.*;
import com.eventplanner.services.api.ParticipantsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class EventsServiceImplTest {

    @Mock
    private EventsRepository eventsRepository;

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private EventPhotosRepository photosRepository;

    @Mock
    private EventParticipantsRepository participantsRepository;

    @Mock
    private EventInvitationsRepository invitationsRepository;
    @Mock
    private ParticipantsService participantsService;

    @InjectMocks
    private EventsServiceImpl eventsService;

    private Authentication authentication;

    @BeforeEach
    void setUp()
    {
        authentication = Mockito.mock(Authentication.class);
        CustomUserDetailsDTO userDetailsDTO = new CustomUserDetailsDTO();
        userDetailsDTO.setUserId(UUID.randomUUID());
        lenient().when(authentication.getPrincipal()).thenReturn(userDetailsDTO);
    }


    @Test
    public void createNewEvent_Success() {
        EventsDTO eventsDTO = new EventsDTO();
        eventsDTO.setTitle("Test Event");
        eventsDTO.setDescription("Test Description");
        eventsDTO.setLocation("Test Location");
        eventsDTO.setDateTime(new Date());


        CustomUserDetailsDTO userDetails = (CustomUserDetailsDTO) authentication.getPrincipal();
        Users organizer = new Users();
        organizer.setUserId(userDetails.getUserId());
        when(usersRepository.getReferenceById(any())).thenReturn(organizer);

        when(eventsRepository.save(any())).thenReturn(new Events());
        when(participantsService.addParticipantToEvent(any())).thenReturn(ResponseEntity.ok().build());

        ResponseEntity<?> response = eventsService.createNewEvent(eventsDTO, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void createNewEvent_InternalServerError()
    {
        EventsDTO eventsDTO = new EventsDTO();
        eventsDTO.setTitle("Test Event");
        eventsDTO.setDescription("Test Description");
        eventsDTO.setLocation("Test Location");
        eventsDTO.setDateTime(new Date());

        when(usersRepository.getReferenceById(any())).thenReturn(new Users());
        when(eventsRepository.save(any())).thenThrow(new RuntimeException());



        ResponseEntity<?> response = eventsService.createNewEvent(eventsDTO, authentication);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
    @Test
    void getAllEvents_NoContent() {
        when(eventsRepository.findAll()).thenReturn(new ArrayList<>());
        ResponseEntity<?> response = eventsService.getAllEvents();
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void getAllEvents_Success()
    {
        List<Events> mockEvents = new ArrayList<>();
        UUID id1 = UUID.randomUUID();
        Users user = new Users();
        UUID id2 = UUID.randomUUID();
        mockEvents.add(new Events(id1, "title","Description 1", "Location 1", new Date(), user));
        mockEvents.add(new Events(id2, "title","Description 2", "Location 2", new Date(),user));

        when(eventsRepository.findAll()).thenReturn(mockEvents);
        ResponseEntity<?> response = eventsService.getAllEvents();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assert response.getBody() instanceof List<?>;
        List<Events> events = (List<Events>) response.getBody();
        assertEquals(events, response.getBody());
    }

    @Test
    void getEventByTitle_Success()
    {
        String title = "TITLE";
        Events event = new Events();
        event.setTitle(title);
        when(eventsRepository.findByTitle(title)).thenReturn(Optional.of(event));

        ResponseEntity<?> response = eventsService.getEventByTitle(title);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(event, response.getBody());

    }

    @Test
    void getEventByTitle_NotFound()
    {
        String title = "TITLE";

        when(eventsRepository.findByTitle(title)).thenReturn(Optional.empty());
        ResponseEntity<?> response = eventsService.getEventByTitle(title);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Event with title " + title + " not found", response.getBody());
    }

    @Test
    void getEventById_Success()
    {
        UUID eventId = UUID.randomUUID();
        Events event = new Events();
        event.setEventId(eventId);

        when (eventsRepository.getReferenceById(eventId)).thenReturn(event);
        ResponseEntity<?> response = eventsService.getEventById(eventId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(event, response.getBody());
    }

    @Test
    void getEventById_NotFound()
    {
        UUID eventId = UUID.randomUUID();

        when(eventsRepository.getReferenceById(eventId)).thenThrow(new RuntimeException());
        ResponseEntity<?> response = eventsService.getEventById(eventId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Event with id " + eventId + " not found", response.getBody());
    }

    @Test
    void updateEvent_Success()
    {
        UUID eventId = UUID.randomUUID();
        Events event = new Events();
        EventsDTO updatedEvent = new EventsDTO("TITLE",
                "DESCRIPTION","LOCATION",new Date());

        when(eventsRepository.existsById(eventId)).thenReturn(true);
        when(eventsRepository.getReferenceById(eventId)).thenReturn(event);
        when(eventsRepository.save(any())).thenReturn(event);
        ResponseEntity<?> response = eventsService.updateEvent(eventId,updatedEvent);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(event, response.getBody());
    }

    @Test
    void updateEvent_NotFound()
    {
        UUID eventId = UUID.randomUUID();
        EventsDTO updatedEvent = new EventsDTO();

        when(eventsRepository.existsById(eventId)).thenReturn(false);
        ResponseEntity<?> response = eventsService.updateEvent(eventId, updatedEvent);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Event with id " + eventId + " not found", response.getBody());
    }

    @Test
    void deleteEvent_Success()
    {
        UUID eventId = UUID.randomUUID();
        Events event = new Events();
        event.setEventId(eventId);
        CustomUserDetailsDTO userDetails = (CustomUserDetailsDTO) authentication.getPrincipal();
        Users organizer = new Users();
        organizer.setUserId(userDetails.getUserId());
        event.setOrganizer(organizer);

        when(eventsRepository.getReferenceById(eventId)).thenReturn(event);
        when(eventsRepository.existsById(eventId)).thenReturn(true);
        ResponseEntity<?> response = eventsService.deleteEvent(eventId, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Event with id " + eventId + " has been deleted", response.getBody());
    }

    @Test
    void deleteEvent_Unauthorized()
    {
        UUID eventId = UUID.randomUUID();
        Events event = new Events();
        event.setEventId(eventId);
        Users organizer = new Users();
        UUID userId = UUID.randomUUID(); // Random user
        organizer.setUserId(userId);
        event.setOrganizer(organizer);

        when(eventsRepository.getReferenceById(eventId)).thenReturn(event);
        when(eventsRepository.existsById(eventId)).thenReturn(true);
        ResponseEntity<?> response = eventsService.deleteEvent(eventId, authentication);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("You don't have the rights to upload this event", response.getBody());
    }

    @Test
    void deleteEvent_NotFound()
    {
        UUID eventId = UUID.randomUUID();
        Events event = new Events();
        CustomUserDetailsDTO userDetailsDTO = (CustomUserDetailsDTO) authentication.getPrincipal();
        Users organizer = new Users();
        organizer.setUserId(userDetailsDTO.getUserId());
        event.setOrganizer(organizer);

        when(eventsRepository.existsById(eventId)).thenReturn(false);
        ResponseEntity<?> response = eventsService.deleteEvent(eventId, authentication);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Event with id " + eventId + " not found", response.getBody());
    }
}