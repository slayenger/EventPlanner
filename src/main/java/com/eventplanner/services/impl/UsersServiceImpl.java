package com.eventplanner.services.impl;

import com.eventplanner.dtos.CustomUserDetailsDTO;
import com.eventplanner.dtos.RegistrationUserDTO;
import com.eventplanner.dtos.UserDTO;
import com.eventplanner.entities.EmailConfirmation;
import com.eventplanner.entities.EventParticipants;
import com.eventplanner.entities.Events;
import com.eventplanner.entities.Users;
import com.eventplanner.exceptions.EmptyListException;
import com.eventplanner.exceptions.NotFoundException;
import com.eventplanner.exceptions.PasswordMismatchException;
import com.eventplanner.exceptions.SamePasswordException;
import com.eventplanner.mappers.UsersMapper;
import com.eventplanner.repositories.EmailConfirmationRepository;
import com.eventplanner.repositories.EventParticipantsRepository;
import com.eventplanner.repositories.EventsRepository;
import com.eventplanner.repositories.UsersRepository;
import com.eventplanner.services.api.UsersService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of the {@link  UsersService} interface responsible for managing user-related operations.
 */
@Service
@RequiredArgsConstructor
//TODO добавить метод смены пароля
public class UsersServiceImpl implements UsersService, UserDetailsService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final EventParticipantsRepository participantsRepository;
    private final EventsRepository eventsRepository;
    private final EmailConfirmationRepository emailConfirmationRepository;
    private final UsersMapper usersMapper;
    private final PlatformTransactionManager transactionManager;
    private static final Logger LOGGER = LogManager.getLogger();

    //TODO должны возвращаться дто а не сущность
    @Override
    public Page<Users> getAllUsers(int page, int size) throws EmptyListException {
        Pageable pageable = PageRequest.of(page, size);
        Page<Users> users = usersRepository.findAll(pageable);

        if (users.isEmpty()) {
            throw new EmptyListException("At the moment there is no users");
        } else {
            return users;
        }
    }

    @Override
    public Users registerUser(RegistrationUserDTO registrationUserDTO) {
        /*TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        LOGGER.info("Start transaction");*/
        Users user = new Users();
        user.setUsername(registrationUserDTO.getUsername());
        user.setEmail(registrationUserDTO.getEmail());
        user.setPassword(passwordEncoder.encode(registrationUserDTO.getPassword()));
        usersRepository.save(user);
        //transactionManager.commit(transaction);
        return user;
    }

    @Override
    public void changePassword (UUID userId, String currentPassword, String newPassword)
    {
        Users user = usersRepository.getReferenceById(userId);

        if (!passwordEncoder.matches(currentPassword, user.getPassword()))
        {
            throw new PasswordMismatchException("Current password is incorrect");
        }
        if (passwordEncoder.matches(currentPassword,newPassword))
        {
            throw new SamePasswordException("The new password must be different from the current password. Please choose a different password");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        usersRepository.save(user);
    }
    @Override
    public UserDTO getUserById(UUID userId) throws NotFoundException {
        if (usersRepository.existsById(userId)) {
            Users user = usersRepository.getReferenceById(userId);
            return usersMapper.toDTO(user);
        } else {
            throw new NotFoundException("User with id: " + userId + " not found");
        }
    }

    @Override
    public UserDTO getUserByEmail(String email) throws NotFoundException {

        if (usersRepository.existsByEmail(email)) {
            Optional<Users> user = usersRepository.findByEmail(email);
            return usersMapper.toDTO(user.get());
        } else {
            throw new NotFoundException("User with email: " + email + " not found");
        }
    }

    @Override
    public UserDTO updateUser(UUID userId, UserDTO updatedUser) throws NotFoundException {
        TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        Users user = usersRepository.getReferenceById(userId);
        usersRepository.save(usersMapper.update(updatedUser, user));
        transactionManager.commit(transaction);
        return usersMapper.toDTO(user);

    }

    @Override
    public PageImpl<Events> getUserEvents(UUID userId, int page, int size) {
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

    //TODO при удалении пользователя токен должен удаляться
    //TODO пользователя может удалить кто угодно
    @Override
    public void deleteUser(UUID userId) {
        TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        LOGGER.info("Start transaction");
        if (!usersRepository.existsById(userId)) {
            throw new NotFoundException("User with id " + userId + " not found");
        }

        List<EventParticipants> participants = participantsRepository.findAllByUser_UserId(userId);
        participantsRepository.deleteAll(participants);

        EmailConfirmation emailConfirmation = emailConfirmationRepository.findByUser_UserId(userId);
        if (emailConfirmation != null)
        emailConfirmationRepository.delete(emailConfirmation);

        usersRepository.deleteById(userId);
        transactionManager.commit(transaction);
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
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users users = usersRepository.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException(
                        String.format("User  '%s' not found", username)
                )
                //TODO redirect to registerUser
        );

        return new CustomUserDetailsDTO(
                users.getUserId(),
                users.getUsername(),
                users.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
