package com.eventplanner.services.api;

import com.eventplanner.dtos.RegistrationUserDTO;
import com.eventplanner.dtos.UserDTO;
import com.eventplanner.entities.Users;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsersService {

    ResponseEntity<List<Users>> getAllUsers();

    Users registerUser(RegistrationUserDTO user);

    //ResponseEntity<?> authenticateUser(JwtRequestDTO authRequest);

    ResponseEntity<?> getUserById(UUID userId);

    ResponseEntity<?> getUserByEmail(String email);

    ResponseEntity<?> updateUser(UUID userId, UserDTO updatedUser);

    ResponseEntity<?> getUserEvents(UUID userId);

    ResponseEntity<?> deleteUser(UUID userId);


    Optional<Users> getUserByUsername(String username);
}
