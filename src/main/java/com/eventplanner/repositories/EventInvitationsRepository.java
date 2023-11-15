package com.eventplanner.repositories;

import com.eventplanner.entities.EventInvitations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.UUID;
public interface EventInvitationsRepository extends JpaRepository<EventInvitations, UUID>
{

    Page<EventInvitations> findAllByEvent_EventId(UUID eventId, Pageable pageable);

    Page<EventInvitations> findAllByInvitedUser_UserId(UUID userId, Pageable pageable);
    void deleteAllByEvent_EventId(UUID eventId);

}
