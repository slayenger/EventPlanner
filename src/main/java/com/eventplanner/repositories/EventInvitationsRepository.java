package com.eventplanner.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EventInvitationsRepository extends JpaRepository<EventInvitationsRepository, UUID> {
}
