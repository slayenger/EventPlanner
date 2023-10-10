package com.eventplanner.controllers;

import com.eventplanner.dtos.UserDTO;
import com.eventplanner.services.api.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UsersController {

    public final UsersService usersService;


    @GetMapping()
    public ResponseEntity<?> getAllUsers() {
        return usersService.getAllUsers();
    }

    @GetMapping("/by_email/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        return usersService.getUserByEmail(email);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable UUID userId,
                                        @RequestBody UserDTO userDTO, Authentication authentication) {
        return usersService.updateUser(userId, userDTO, authentication);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable UUID userId, Authentication authentication) {
        return usersService.deleteUser(userId, authentication);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable UUID userId) {
        return usersService.getUserById(userId);
    }

    @GetMapping("/events/{userId}")
    public ResponseEntity<?> getUserEvents(@PathVariable UUID userId)
    {
        return usersService.getUserEvents(userId);
    }

}
