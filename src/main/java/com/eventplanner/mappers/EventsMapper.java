package com.eventplanner.mappers;

import com.eventplanner.dtos.EventsDTO;
import com.eventplanner.dtos.EventsResponseDTO;
import com.eventplanner.entities.Events;
import org.springframework.stereotype.Component;


@Component
public class EventsMapper {

    public Events toEvent(EventsDTO eventsDTO)
    {
        Events event = new Events();
        event.setTitle(eventsDTO.getTitle());
        event.setLocation(eventsDTO.getLocation());
        event.setDescription(eventsDTO.getDescription());
        event.setDateTime(eventsDTO.getDateTime());

        return event;
    }

    public EventsResponseDTO toDTO(Events event)
    {
        EventsResponseDTO eventDTO = new EventsResponseDTO();
        eventDTO.setTitle(event.getTitle());
        eventDTO.setLocation(event.getLocation());
        eventDTO.setDateTime(event.getDateTime());
        return eventDTO;
    }

    public Events update(EventsDTO eventsDTO, Events event)
    {
        event.setTitle(eventsDTO.getTitle());
        event.setLocation(eventsDTO.getLocation());
        event.setDescription(eventsDTO.getDescription());
        event.setDateTime(eventsDTO.getDateTime());
        return event;
    }

}
