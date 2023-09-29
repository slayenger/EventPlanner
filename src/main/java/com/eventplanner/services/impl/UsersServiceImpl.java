package com.eventplanner.services.impl;

import com.eventplanner.dtos.UserDTO;
import com.eventplanner.dtos.UsersRegistrationDTO;
import com.eventplanner.entities.Users;
import com.eventplanner.repositories.UsersRepository;
import com.eventplanner.services.api.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService {

    private final UsersRepository repository;

    @Override
    public ResponseEntity<List<Users>> getAllUsers()
    {
        List<Users> users = repository.findAll();
        if (users.isEmpty())
        {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        else
        {
            return ResponseEntity.ok(users);
        }
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseEntity<?> registerUser(UsersRegistrationDTO user)
    {
        try
        {
            Users users = new Users();
            users.setEmail(user.getEmail());
            users.setPassword(user.getPassword());
            repository.save(users);
            return ResponseEntity.ok("Success");
        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body("Registration error" + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> authenticateUser(String username, String password) {
        return null;
    }

    @Override
    public ResponseEntity<?> getUserById(UUID userId)
    {
        try
        {
            Users user = repository.getReferenceById(userId);
            return ResponseEntity.ok(user);
        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body("User with id: " + userId + " not found.");
        }
    }

    @Override
    public ResponseEntity<?> getUserByEmail(String email)
    {
        Optional<Users> user = repository.findByEmail(email);

        if (user.isPresent())
        {
            return ResponseEntity.ok(user);
        }
        else
        {
            return ResponseEntity.badRequest().body("User with email: " + email + " not found.");
        }
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseEntity<?> updateUser(UUID userId, UserDTO updatedUser)
    {
        if (repository.existsById(userId))
        {
            Users user = repository.getReferenceById(userId);
            user.setEmail(updatedUser.getEmail());
            user.setFirstname(updatedUser.getFirstname());
            user.setLastname(updatedUser.getLastname());
            repository.save(user);
            return ResponseEntity.ok().body("Success");
        }
        else
        {
            return ResponseEntity.badRequest().body("Something went wrong");
        }
    }

    @Override
    public ResponseEntity<?> getUserEvents(UUID userId)
    {
        return null;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseEntity<?> deleteUser(UUID userId)
    {
        if (repository.existsById(userId))
        {
            repository.deleteById(userId);
            return ResponseEntity.ok().body("Success");
        }
        else
        {
            return ResponseEntity.badRequest().body("User not found");
        }
    }
}
