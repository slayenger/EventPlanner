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

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UsersServiceImpl usersService;
    private final JwtTokenUtils jwtTokenUtil;
    private final AuthenticationManager authenticationManager;





    public ResponseEntity<?> authenticateUser(@RequestBody JwtRequestDTO authRequest) {

        try
        {

            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    authRequest.getUsername(),authRequest.getPassword())
            );
        }
        catch (BadCredentialsException e)
        {
            System.out.println("Error " + authRequest.getUsername() + " " + authRequest.getPassword());
            log.debug("Authentication failed for user: {}", authRequest.getUsername(),e);

            return  new ResponseEntity<>(new BadCredentialsDTO(HttpStatus.UNAUTHORIZED.value(),
                    "Login or password entered incorrectly"), HttpStatus.UNAUTHORIZED) ;
        }

        UserDetails userDetails = usersService.loadUserByUsername(authRequest.getUsername());

        String token = jwtTokenUtil.generateToken(userDetails);

        return ResponseEntity.ok(new JwtResponseDTO(token));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseEntity<?> registerUser(RegistrationUserDTO user)
    {
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
            Users users = usersService.registerUser(user);


            return ResponseEntity.ok(new UserDTO(users.getEmail(), users.getUsername(),
                    users.getFirstname(), users.getLastname()));
        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body("Registration error: " + e.getMessage());
        }
    }
}
