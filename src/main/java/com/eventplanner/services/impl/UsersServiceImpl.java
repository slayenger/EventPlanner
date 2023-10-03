package com.eventplanner.services.impl;

import com.eventplanner.dtos.*;

import com.eventplanner.entities.Users;
import com.eventplanner.repositories.UsersRepository;
import com.eventplanner.services.api.UsersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.*;

@Service
@Slf4j
public class UsersServiceImpl implements UsersService, UserDetailsService {

    private UsersRepository repository;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public void setRepository(UsersRepository usersRepository)
    {
        this.repository = usersRepository;
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder)
    {
        this.passwordEncoder = passwordEncoder;
    }


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
    public Users registerUser(RegistrationUserDTO registrationUserDTO)
    {
        Users user = new Users();
        user.setUsername(registrationUserDTO.getUsername());
        user.setEmail(registrationUserDTO.getEmail());
        user.setPassword(passwordEncoder.encode(registrationUserDTO.getPassword()));
        repository.save(user);
        return repository.save(user);
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
            user.setUsername(updatedUser.getUsername());
            user.setFirstname(updatedUser.getFirstname());
            user.setLastname(updatedUser.getLastname());
            repository.save(user);
            System.out.println(user);
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

    @Override
    public Optional<Users> getUserByUsername(String username) {
        return repository.findByUsername(username);
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        Users users  = repository.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException(
                        String.format("User  '%s' not found", username)
                )
        );

        return new CustomUserDetailsDTO(
                users.getUserId(),
                users.getUsername(),
                users.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))
                );
    }
}
