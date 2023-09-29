package com.eventplanner.services.api;

import com.eventplanner.dtos.UserDTO;
import com.eventplanner.dtos.UsersRegistrationDTO;
import com.eventplanner.entities.Users;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

public interface UsersService {

    ResponseEntity<List<Users>> getAllUsers();

    ResponseEntity<?> registerUser(UsersRegistrationDTO user);

    ResponseEntity<?> authenticateUser(String username, String password);

    ResponseEntity<?> getUserById(UUID userId);

    ResponseEntity<?> getUserByEmail(String email);

    ResponseEntity<?> updateUser(UUID userId, UserDTO updatedUser);

    ResponseEntity<?> getUserEvents(UUID userId);

    ResponseEntity<?> deleteUser(UUID userId);




}
