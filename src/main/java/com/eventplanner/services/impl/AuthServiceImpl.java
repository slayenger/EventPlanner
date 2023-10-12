package com.eventplanner.services.impl;

import com.eventplanner.dtos.*;
import com.eventplanner.entities.Users;
import com.eventplanner.security.jwt.JwtTokenUtils;
import com.eventplanner.services.api.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Implementation of the {@link AuthService} interface responsible for user authentication
 * and registration.
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsersServiceImpl usersService;
    private final JwtTokenUtils jwtTokenUtil;
    private final AuthenticationManager authenticationManager;

    /**
     * Authenticates a user using the provided authentication request.
     *
     * @param authRequest The authentication request containing the user's username and password.
     * @return ResponseEntity containing a JWT token if authentication is successful, or an error message if it fails.
     */
    @Override
    public ResponseEntity<?> authenticateUser(@RequestBody JwtRequestDTO authRequest) {

        try
        {
            // Attempt to authenticate the user
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    authRequest.getUsername(),authRequest.getPassword())
            );
        }
        catch (BadCredentialsException e)
        {
            // Authentication failed due to incorrect credentials
            System.out.println("Error " + authRequest.getUsername() + " " + authRequest.getPassword());

            return  new ResponseEntity<>(new BadCredentialsDTO(HttpStatus.UNAUTHORIZED.value(),
                    "Login or password entered incorrectly"), HttpStatus.UNAUTHORIZED) ;
        }

        // Authentication failed due to incorrect credentials
        UserDetails userDetails = usersService.loadUserByUsername(authRequest.getUsername());
        String token = jwtTokenUtil.generateToken(userDetails);

        return ResponseEntity.ok(new JwtResponseDTO(token));
    }

    /**
     * Registers a new user with the provided registration data.
     *
     * @param user The registration data of the user.
     * @return ResponseEntity containing the registered user's details if registration is successful, or an error message if it fails.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Override
    public ResponseEntity<?> registerUser(RegistrationUserDTO user)
    {
        // Checking the input data during registration
        if (!user.getPassword().equals(user.getConfirmPassword()))
        {
            return new ResponseEntity<>(new BadCredentialsDTO(
                    HttpStatus.BAD_REQUEST.value(), "Passwords don't match"
            ), HttpStatus.BAD_REQUEST);
        }
        if (usersService.getUserByUsername(user.getUsername()).isPresent())
        {
            return new ResponseEntity<>(new BadCredentialsDTO(HttpStatus.BAD_REQUEST.value(),
                    "The user with the specified name already exists"), HttpStatus.BAD_REQUEST);
        }

        try
        {
            // Register the user
            Users users = usersService.registerUser(user);

            return ResponseEntity.status(HttpStatus.CREATED).body(new UserDTO(users.getEmail(), users.getUsername(),
                    users.getFirstname(), users.getLastname()));
        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body("Registration error: " + e.getMessage());
        }
    }
}
