package com.eventplanner.repositories;

import com.eventplanner.dtos.CustomUserDetailsDTO;
import com.eventplanner.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UsersRepository extends JpaRepository<Users, UUID> {

    Optional<Users> findByEmail(String email);
    Optional<Users> findByUsername(String username);



}
