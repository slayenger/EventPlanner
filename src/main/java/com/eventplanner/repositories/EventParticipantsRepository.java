package com.eventplanner.repositories;

import com.eventplanner.entities.EventParticipants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventParticipantsRepository extends JpaRepository<EventParticipants, UUID> {

    Optional<EventParticipants> findByEvent_EventId(UUID eventId);

    Optional<EventParticipants> findByEvent_EventIdAndUser_UserId(UUID eventId, UUID userId);

    boolean existsByEvent_EventIdAndUser_UserId(UUID eventId, UUID userId);

    Page<EventParticipants> findAllByEvent_EventId(UUID eventId, Pageable pageable);

    List<EventParticipants> findAllByEvent_EventId(UUID eventId);

    void deleteAllByEvent_EventId(UUID eventId);

    List<EventParticipants> findAllByUser_UserId(UUID userId);

    Page<EventParticipants> findAllByUser_UserId(UUID userId, Pageable pageable);
}
