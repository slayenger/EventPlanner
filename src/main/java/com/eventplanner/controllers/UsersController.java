package com.eventplanner.controllers;

import com.eventplanner.dtos.UserDTO;
import com.eventplanner.dtos.UsersRegistrationDTO;
import com.eventplanner.services.api.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UsersController
{

    public final UsersService usersService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UsersRegistrationDTO user)
    {
        return usersService.registerUser(user);
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers()
    {
        return usersService.getAllUsers();
    }

    @GetMapping("/by-email/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email)
    {
        return usersService.getUserByEmail(email);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable UUID userId, UserDTO userDTO)
    {
        return usersService.updateUser(userId, userDTO);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable UUID userId)
    {
        return usersService.deleteUser(userId);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable UUID userId)
    {
        return usersService.getUserById(userId);
    }

}
