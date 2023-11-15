package com.eventplanner.repositories;

import com.eventplanner.entities.EventPhotos;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventPhotosRepository extends JpaRepository<EventPhotos, UUID> {

    Page<EventPhotos> findAllByEvent_EventId(UUID eventId, Pageable pageable);

    void deleteAllByEvent_EventId(UUID eventId);

}
