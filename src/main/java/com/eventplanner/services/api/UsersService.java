package com.eventplanner.services.api;

import com.eventplanner.dtos.RegistrationUserDTO;
import com.eventplanner.dtos.UserDTO;
import com.eventplanner.entities.Users;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsersService {

    ResponseEntity<List<Users>> getAllUsers();

    Users registerUser(RegistrationUserDTO user);


    ResponseEntity<?> getUserById(UUID userId);

    ResponseEntity<?> getUserByEmail(String email);

    ResponseEntity<?> updateUser(UUID userId, UserDTO updatedUser, Authentication authentication);

    ResponseEntity<?> getUserEvents(UUID userId);

    ResponseEntity<?> deleteUser(UUID userId, Authentication authentication);

    Optional<Users> getUserByUsername(String username);
}
