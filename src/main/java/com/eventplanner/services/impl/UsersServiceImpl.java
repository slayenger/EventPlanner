package com.eventplanner.services.impl;

import com.eventplanner.dtos.CustomUserDetailsDTO;
import com.eventplanner.dtos.RegistrationUserDTO;
import com.eventplanner.dtos.UserDTO;
import com.eventplanner.entities.EventParticipants;
import com.eventplanner.entities.Events;
import com.eventplanner.entities.Users;
import com.eventplanner.repositories.EventParticipantsRepository;
import com.eventplanner.repositories.EventsRepository;
import com.eventplanner.repositories.UsersRepository;
import com.eventplanner.services.api.UsersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService, UserDetailsService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final EventParticipantsRepository participantsRepository;
    private final EventsRepository eventsRepository;


    @Override
    public ResponseEntity<List<Users>> getAllUsers()
    {
        List<Users> users = usersRepository.findAll();
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
        usersRepository.save(user);
        return usersRepository.save(user);
    }

    @Override
    public ResponseEntity<?> getUserById(UUID userId)
    {
        try
        {
            Users user = usersRepository.getReferenceById(userId);
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
        Optional<Users> user = usersRepository.findByEmail(email);

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
    public ResponseEntity<?> updateUser(UUID userId, UserDTO updatedUser, Authentication authentication)
    {
        CustomUserDetailsDTO userDetailsDTO = (CustomUserDetailsDTO) authentication.getPrincipal();
        if (!userId.equals(userDetailsDTO.getUserId()))
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You don't have the rights to update this user");
        }

        if (usersRepository.existsById(userId))
        {
            Users user = usersRepository.getReferenceById(userId);
            user.setEmail(updatedUser.getEmail());
            user.setUsername(updatedUser.getUsername());
            user.setFirstname(updatedUser.getFirstname());
            user.setLastname(updatedUser.getLastname());
            usersRepository.save(user);
            return ResponseEntity.ok().body("Success");
        }
        else
        {
            return ResponseEntity.badRequest().body("User with id " + userId + " not found");
        }
    }

    @Override
    public ResponseEntity<?> getUserEvents(UUID userId)
    {
        if (!usersRepository.existsById(userId))
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with id " + userId + " not found");
        }

        List<EventParticipants> participants = participantsRepository.findAllByUser_UserId(userId);
        List<Events> events = new ArrayList<>();
        if (participants.size() > 0)
        {
            for (EventParticipants participant: participants)
            {
                UUID eventId = participant.getEvent().getEventId();

                events.add(eventsRepository.getReferenceById(eventId));
            }
            return ResponseEntity.ok(events);
        }
        else
        {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body("The user does not participate in any events");
        }
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseEntity<?> deleteUser(UUID userId, Authentication authentication)
    {
        CustomUserDetailsDTO userDetailsDTO = (CustomUserDetailsDTO) authentication.getPrincipal();
        if (!userId.equals(userDetailsDTO.getUserId()))
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You don't have the rights to delete this user");
        }

        if (!usersRepository.existsById(userId))
        {
            return ResponseEntity.badRequest().body("User with id " + userId + " not found");
        }

        List<EventParticipants> participants = participantsRepository.findAllByUser_UserId(userId);
        participantsRepository.deleteAll(participants);

        usersRepository.deleteById(userId);
        return ResponseEntity.ok().body("Success");

    }

    @Override
    public Optional<Users> getUserByUsername(String username) {
        return usersRepository.findByUsername(username);
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        Users users  = usersRepository.findByUsername(username).orElseThrow(
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
