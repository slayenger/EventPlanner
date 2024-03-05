package com.eventplanner.repositories;

import com.eventplanner.entities.EmailConfirmation;
import com.eventplanner.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EmailConfirmationRepository extends JpaRepository<EmailConfirmation, UUID> {

    EmailConfirmation findByUser_UserId(UUID userId);
    EmailConfirmation findByUser(User user);
}
