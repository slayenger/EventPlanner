package com.eventplanner.mappers;

import com.eventplanner.dtos.EventsRequestDTO;
import com.eventplanner.dtos.EventsResponseDTO;
import com.eventplanner.entities.Events;
import com.eventplanner.exceptions.ParseException;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;


@Component
public class EventsMapper {

    public Events toEvent(EventsRequestDTO eventsRequestDTO) throws ParseException {
        Events event = new Events();
        event.setTitle(eventsRequestDTO.getTitle());
        event.setLocation(eventsRequestDTO.getLocation());
        event.setDescription(eventsRequestDTO.getDescription());

        try
        {
            SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            formatter.setLenient(false);
            event.setDateTime(formatter.parse(eventsRequestDTO.getDateTime()));
        }
        catch (java.text.ParseException e)
        {
            throw new ParseException("Error date parsing", 0);
        }


        return event;
    }

    public EventsResponseDTO toDTO(Events event)
    {
        EventsResponseDTO eventDTO = new EventsResponseDTO();
        eventDTO.setEventId(event.getEventId());
        eventDTO.setTitle(event.getTitle());
        eventDTO.setLocation(event.getLocation());

        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        formatter.setLenient(false);
        eventDTO.setDateTime(formatter.format(event.getDateTime()));

        EventsResponseDTO.OrganizerInfo organizerInfo = new EventsResponseDTO.OrganizerInfo();
        organizerInfo.setFirstname(event.getOrganizer().getFirstname());
        organizerInfo.setLastname(event.getOrganizer().getLastname());
        eventDTO.setOrganizerInfo(organizerInfo);

        return eventDTO;
    }

    public Events update(EventsRequestDTO eventsRequestDTO, Events event) throws ParseException {
        event.setTitle(eventsRequestDTO.getTitle());
        event.setLocation(eventsRequestDTO.getLocation());
        event.setDescription(eventsRequestDTO.getDescription());

        try
        {
            SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            formatter.setLenient(false);
            event.setDateTime(formatter.parse(eventsRequestDTO.getDateTime()));
        }
        catch (java.text.ParseException e)
        {
            throw new ParseException("Error date parsing", 0);
        }


        return event;
    }


}
