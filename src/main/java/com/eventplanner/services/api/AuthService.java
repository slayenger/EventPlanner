package com.eventplanner.services.api;

import com.eventplanner.dtos.JwtRequestDTO;
import com.eventplanner.dtos.JwtResponseDTO;
import com.eventplanner.dtos.RegistrationUserDTO;
import com.eventplanner.dtos.UserDTO;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * AuthService provides methods for user authentication and registration.
 */
public interface AuthService {

    /**
     * Authenticates a user based on the provided credentials and generates a JWT token upon successful authentication.
     *
     * @param authRequest The authentication request containing the username and password.
     * @return JwtResponseDTO containing the generated JWT token.
     * @throws BadCredentialsException If authentication fails due to incorrect credentials.
     */
    JwtResponseDTO authenticateUser(@RequestBody JwtRequestDTO authRequest);

    /**
     * Registers a new user based on the provided registration details.
     *
     * @param user The registration details for the new user.
     * @return UserDTO containing information about the registered user.
     * @throws BadCredentialsException If the provided passwords do not match or if a user with the same username already exists.
     * @throws RuntimeException       If an error occurs during user registration.
     */
    UserDTO registerUser(RegistrationUserDTO user);

}
