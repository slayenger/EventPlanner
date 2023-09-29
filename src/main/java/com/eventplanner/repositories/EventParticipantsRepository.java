package com.eventplanner.repositories;

import com.eventplanner.entities.EventParticipants;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EventParticipantsRepository extends JpaRepository<EventParticipants, UUID> {
}
