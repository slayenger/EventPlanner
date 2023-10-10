package com.eventplanner.repositories;

import com.eventplanner.entities.EventInvitations;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface EventInvitationsRepository extends JpaRepository<EventInvitations, UUID>
{

    List<EventInvitations> findAllByEvent_EventId(UUID eventId);

    List<EventInvitations> findAllByInvitedUser_UserId(UUID userId);
    Optional<EventInvitations> deleteAllByEvent_EventId(UUID eventId);

}
