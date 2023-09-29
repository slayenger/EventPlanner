package com.eventplanner.repositories;

import com.eventplanner.entities.EventPhotos;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EventPhotosRepository extends JpaRepository<EventPhotos, UUID> {
}
