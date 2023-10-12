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

/**
 * Implementation of the UsersService interface responsible for managing user-related operations.
 */
@Service
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService, UserDetailsService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final EventParticipantsRepository participantsRepository;
    private final EventsRepository eventsRepository;


    /**
     * Retrieves a list of all users.
     *
     * @return A ResponseEntity containing a list of user objects or a message indicating no users were found.
     */
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

    /**
     * Registers a new user.
     *
     * @param registrationUserDTO The user registration data.
     * @return The registered user object.
     */
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

    /**
     * Retrieves a user by their unique identifier.
     *
     * @param userId The unique identifier of the user.
     * @return A ResponseEntity containing the user object or an error message.
     */
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

    /**
     * Retrieves a user by their email address.
     *
     * @param email The email address of the user.
     * @return A ResponseEntity containing the user object or an error message.
     */
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

    /**
     * Updates user information.
     *
     * @param userId        The unique identifier of the user to update.
     * @param updatedUser   The updated user data.
     * @param authentication The authentication information of the user performing the update.
     * @return A ResponseEntity with a success message or an error message.
     */
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

    /**
     * Retrieves a list of events associated with a user.
     *
     * @param userId The unique identifier of the user.
     * @return A ResponseEntity containing a list of events or a message indicating no events were found.
     */
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

    /**
     * Deletes a user by their unique identifier.
     *
     * @param userId        The unique identifier of the user to delete.
     * @param authentication The authentication information of the user performing the deletion.
     * @return A ResponseEntity with a success message or an error message.
     */
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

    /**
     * Retrieves a user by their username.
     *
     * @param username The username of the user.
     * @return An Optional containing the user object or an empty result if no user with the provided username is found.
     */
    @Override
    public Optional<Users> getUserByUsername(String username) {
        return usersRepository.findByUsername(username);
    }

    /**
     * Loads user details by username for authentication purposes.
     *
     * @param username The username of the user.
     * @return UserDetails containing user information.
     * @throws UsernameNotFoundException if the user is not found.
     */
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
