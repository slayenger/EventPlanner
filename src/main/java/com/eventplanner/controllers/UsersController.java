package com.eventplanner.controllers;

import com.eventplanner.dtos.RegistrationUserDTO;
import com.eventplanner.dtos.UserDTO;
import com.eventplanner.services.api.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UsersController
{

    public final UsersService usersService;



    @GetMapping()
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
    //@PreAuthorize("#userId.equals(authentication.principal.userId)")
    public ResponseEntity<?> updateUser(@PathVariable UUID userId, UserDTO userDTO)
    {
        return usersService.updateUser(userId, userDTO);
    }

    @DeleteMapping("/{userId}")
    //@PreAuthorize("#userId == authentication.principal.userId")
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
