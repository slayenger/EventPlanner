package com.eventplanner.controllers;

import com.eventplanner.dtos.CustomUserDetailsDTO;
import com.eventplanner.dtos.UserDTO;
import com.eventplanner.entities.Events;
import com.eventplanner.exceptions.EmptyListException;
import com.eventplanner.exceptions.NotFoundException;
import com.eventplanner.services.api.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UsersController {

    public final UsersService usersService;

    @GetMapping()
    public ResponseEntity<?> getAllUsers(@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int size) {
        try
        {
            return ResponseEntity.status(HttpStatus.OK).body(usersService.getAllUsers(page, size));
        }
        catch (EmptyListException err)
        {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(err.getMessage());
        }
    }

    @GetMapping("/by_email/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email)
    {
        try
        {
            UserDTO userDTO = usersService.getUserByEmail(email);
            return ResponseEntity.status(HttpStatus.OK).body(userDTO);
        }
        catch (NotFoundException err)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err.getMessage());
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable UUID userId,
                                        @RequestBody UserDTO updatedUser,
                                        @AuthenticationPrincipal CustomUserDetailsDTO userDetails)
    {
        try
        {
            if (!userId.equals(userDetails.getUserId()))
            {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You don't have the rights to update this user");
            }
            return ResponseEntity.status(HttpStatus.OK)
                    .body(usersService.updateUser(userId,updatedUser));
        }
        catch (NotFoundException err)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err.getMessage());
        }

    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable UUID userId,
                                        @AuthenticationPrincipal CustomUserDetailsDTO userDetails)
    {
        try
        {
            if (!userId.equals(userDetails.getUserId()))
            {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You don't have the rights to delete this user");
            }
            usersService.deleteUser(userId);
            return ResponseEntity.status(HttpStatus.OK).body("User with id " + userId + " was deleted");
        }
        catch (NotFoundException err)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err.getMessage());
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable UUID userId)
    {
        try
        {
            UserDTO userDTO = usersService.getUserById(userId);
            return ResponseEntity.status(HttpStatus.OK).body(userDTO);
        }
        catch (NotFoundException err)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err.getMessage());
        }
    }

    @GetMapping("/events/{userId}")
    public ResponseEntity<?> getUserEvents(@PathVariable UUID userId,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size)
    {
        try
        {
            PageImpl<Events> eventsPage = usersService.getUserEvents(userId, page, size);
            return ResponseEntity.status(HttpStatus.OK).body(eventsPage);
        }
        catch (NotFoundException err)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err.getMessage());
        }
    }

}
