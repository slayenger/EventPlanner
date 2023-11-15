package com.eventplanner.services.api;

import com.eventplanner.dtos.RegistrationUserDTO;
import com.eventplanner.dtos.UserDTO;
import com.eventplanner.entities.Events;
import com.eventplanner.entities.Users;
import com.eventplanner.exceptions.EmptyListException;
import com.eventplanner.exceptions.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.Optional;
import java.util.UUID;

/**
 * Interface for managing user-related operations.
 */
public interface UsersService {

    /**
     * Retrieves a paginated list of all users.
     *
     * @param page The page number to retrieve (0-indexed).
     * @param size The number of users to include on each page.
     * @return A {@link Page} of {@link Users} containing the users for the specified page.
     * @throws EmptyListException if there are no users available.
     */
    Page<Users> getAllUsers(int page, int size);

    /**
     * Registers a new user.
     *
     * @param user The user registration data.
     * @return The registered user object.
     */
    Users registerUser(RegistrationUserDTO user);

    /**
     * Retrieves a user by their unique identifier.
     *
     * @param userId The unique identifier of the user to retrieve.
     * @return A {@link UserDTO} representing the user with the specified ID.
     * @throws NotFoundException if no user is found with the given ID.
     */
    UserDTO getUserById(UUID userId);

    /**
     * Retrieves a user by their email address.
     *
     * @param email The email address of the user to retrieve.
     * @return A {@link UserDTO} representing the user with the specified email.
     * @throws NotFoundException if no user is found with the given email address.
     */
    UserDTO getUserByEmail(String email);

    /**
     * Updates user information.
     *
     * @param userId      The ID of the user to be updated.
     * @param updatedUser A {@link UserDTO} containing the updated information.
     * @return A {@link UserDTO} representing the updated user.
     * @throws NotFoundException if no user is found with the given ID.
     */
    UserDTO updateUser(UUID userId, UserDTO updatedUser);

    /**
     * Retrieves a paginated list of events in which the user participates.
     *
     * @param userId The ID of the user for whom to retrieve events.
     * @param page   The page number (zero-based).
     * @param size   The size of the page to be retrieved.
     * @return A {@link PageImpl} containing the paginated list of events.
     * @throws NotFoundException   if no user is found with the given ID.
     * @throws EmptyListException  if the user does not participate in any events.
     */

    PageImpl<Events> getUserEvents(UUID userId, int page, int size);

    /**
     * Deletes a user and removes them from all events they are participating in.
     *
     * @param userId The ID of the user to be deleted.
     * @throws NotFoundException if no user is found with the given ID.
     */
    void deleteUser(UUID userId);

    /**
     * Retrieves a user by their username.
     *
     * @param username The username of the user.
     * @return An Optional containing the user object or an empty result if no user with the provided username is found.
     */
    Optional<Users> getUserByUsername(String username);
}
