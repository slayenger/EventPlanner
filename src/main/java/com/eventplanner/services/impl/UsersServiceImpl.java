package com.eventplanner.services.impl;

import com.eventplanner.dtos.CustomUserDetailsDTO;
import com.eventplanner.dtos.RegistrationUserDTO;
import com.eventplanner.dtos.UserDTO;
import com.eventplanner.entities.EventParticipants;
import com.eventplanner.entities.Events;
import com.eventplanner.entities.Users;
import com.eventplanner.exceptions.EmptyListException;
import com.eventplanner.exceptions.NotFoundException;
import com.eventplanner.mappers.UsersMapper;
import com.eventplanner.repositories.EventParticipantsRepository;
import com.eventplanner.repositories.EventsRepository;
import com.eventplanner.repositories.UsersRepository;
import com.eventplanner.services.api.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of the {@link  UsersService} interface responsible for managing user-related operations.
 */
@Service
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService, UserDetailsService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final EventParticipantsRepository participantsRepository;
    private final EventsRepository eventsRepository;
    private final UsersMapper usersMapper;

    @Override
    public Page<Users> getAllUsers(int page, int size) throws EmptyListException
    {
        Pageable pageable = PageRequest.of(page, size);
        Page<Users> users = usersRepository.findAll(pageable);

        if (users.isEmpty())
        {
            throw new EmptyListException("At the moment there is no users");
        }
        else
        {
            return users;
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
    public UserDTO getUserById(UUID userId) throws NotFoundException
    {
       if (usersRepository.existsById(userId))
       {
           Users user = usersRepository.getReferenceById(userId);
           return usersMapper.toDTO(user);
       }
       else
       {
           throw new NotFoundException("User with id: " + userId + " not found");
       }
    }

    @Override
    public UserDTO getUserByEmail(String email) throws NotFoundException
    {

        if(usersRepository.existsByEmail(email))
        {
            Optional<Users> user = usersRepository.findByEmail(email);
            return usersMapper.toDTO(user.get());
        }
        else
        {
            throw new NotFoundException("User with email: " + email + " not found");
        }
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public UserDTO updateUser(UUID userId, UserDTO updatedUser) throws NotFoundException
    {

        if (usersRepository.existsById(userId))
        {
            Users user = usersRepository.getReferenceById(userId);
            usersRepository.save(usersMapper.update(updatedUser, user));
            return usersMapper.toDTO(user);
        }
        else
        {
            throw new NotFoundException("User with id " + userId + " not found");
        }
    }

    @Override
    public PageImpl<Events> getUserEvents(UUID userId, int page, int size)
    {
        if (!usersRepository.existsById(userId)) {
            throw new NotFoundException("User with id " + userId + " not found");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<EventParticipants> participantsPage = participantsRepository.findAllByUser_UserId(userId, pageable);

        List<Events> events = participantsPage
                .stream()
                .map(participant -> eventsRepository.getReferenceById(participant.getEvent().getEventId()))
                .toList();

        return new PageImpl<>(events, pageable, participantsPage.getTotalElements());
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteUser(UUID userId)
    {

        if (!usersRepository.existsById(userId))
        {
            throw new NotFoundException("User with id " + userId + " not found");
        }

        List<EventParticipants> participants = participantsRepository.findAllByUser_UserId(userId);
        participantsRepository.deleteAll(participants);

        usersRepository.deleteById(userId);
    }

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
