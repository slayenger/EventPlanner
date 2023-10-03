package com.eventplanner.repositories;

import com.eventplanner.entities.EventInvitations;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.UUID;
public interface EventInvitationsRepository extends JpaRepository<EventInvitations, UUID> {
}
