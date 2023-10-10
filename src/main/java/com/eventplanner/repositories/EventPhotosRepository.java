package com.eventplanner.repositories;

import com.eventplanner.entities.EventPhotos;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventPhotosRepository extends JpaRepository<EventPhotos, UUID> {

    List<EventPhotos> findAllByEvent_EventId(UUID eventId);

    void deleteAllByEvent_EventId(UUID eventId);

}
