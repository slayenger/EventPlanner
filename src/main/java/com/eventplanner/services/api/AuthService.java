package com.eventplanner.services.api;

import com.eventplanner.dtos.JwtRequestDTO;
import com.eventplanner.dtos.RegistrationUserDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * AuthService provides methods for user authentication and registration.
 */
public interface AuthService {

    /**
     * Attempts to authenticate a user based on the provided credentials.
     *
     * @param authRequest The authentication request containing the user's login and password.
     * @return If authentication is successful, it returns an authentication token and HTTP status OK (200).
     *         In case of unsuccessful authentication, it returns an error message and HTTP status UNAUTHORIZED (401).
     */
    ResponseEntity<?> authenticateUser(@RequestBody JwtRequestDTO authRequest);

    /**
     * Registers a new user in the system.
     *
     * @param user User data for registration.
     * @return If registration is successful, it returns HTTP status CREATED (201).
     *         In case of errors (e.g., the user already exists), it returns an error message and the corresponding status.
     */
    ResponseEntity<?> registerUser(RegistrationUserDTO user);

}
