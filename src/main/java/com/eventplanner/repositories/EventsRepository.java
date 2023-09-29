package com.eventplanner.repositories;

import com.eventplanner.entities.Events;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EventsRepository extends JpaRepository<Events, UUID> {

    Optional<Events> findByTitle (String title);

}
