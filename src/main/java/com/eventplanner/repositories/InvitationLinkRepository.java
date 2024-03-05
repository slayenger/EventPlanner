package com.eventplanner.repositories;

import com.eventplanner.entities.Events;
import com.eventplanner.entities.InvitationLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvitationLinkRepository extends JpaRepository<InvitationLink, UUID> {

    boolean existsByShortIdentifier (String shortIdentifier);

    Optional<InvitationLink> findByShortIdentifier (String shortIdentifier);

}
