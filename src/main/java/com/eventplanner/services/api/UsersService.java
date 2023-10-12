package com.eventplanner.services.api;

import com.eventplanner.dtos.RegistrationUserDTO;
import com.eventplanner.dtos.UserDTO;
import com.eventplanner.entities.Users;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface for managing user-related operations.
 */
public interface UsersService {

    /**
     * Retrieves a list of all users.
     *
     * @return A ResponseEntity containing a list of user objects or a message indicating no users were found.
     */
    ResponseEntity<List<Users>> getAllUsers();

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
     * @param userId The unique identifier of the user.
     * @return A ResponseEntity containing the user object or an error message.
     */
    ResponseEntity<?> getUserById(UUID userId);

    /**
     * Retrieves a user by their email address.
     *
     * @param email The email address of the user.
     * @return A ResponseEntity containing the user object or an error message.
     */
    ResponseEntity<?> getUserByEmail(String email);

    /**
     * Updates user information.
     *
     * @param userId        The unique identifier of the user to update.
     * @param updatedUser   The updated user data.
     * @param authentication The authentication information of the user performing the update.
     * @return A ResponseEntity with a success message or an error message.
     */
    ResponseEntity<?> updateUser(UUID userId, UserDTO updatedUser, Authentication authentication);

    /**
     * Retrieves a list of events associated with a user.
     *
     * @param userId The unique identifier of the user.
     * @return A ResponseEntity containing a list of events or a message indicating no events were found.
     */
    ResponseEntity<?> getUserEvents(UUID userId);

    /**
     * Deletes a user by their unique identifier.
     *
     * @param userId        The unique identifier of the user to delete.
     * @param authentication The authentication information of the user performing the deletion.
     * @return A ResponseEntity with a success message or an error message.
     */
    ResponseEntity<?> deleteUser(UUID userId, Authentication authentication);

    /**
     * Retrieves a user by their username.
     *
     * @param username The username of the user.
     * @return An Optional containing the user object or an empty result if no user with the provided username is found.
     */
    Optional<Users> getUserByUsername(String username);
}
