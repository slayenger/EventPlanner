package com.eventplanner.services.impl;

import com.eventplanner.dtos.JwtRequestDTO;
import com.eventplanner.dtos.JwtResponseDTO;
import com.eventplanner.dtos.RegistrationUserDTO;
import com.eventplanner.dtos.UserDTO;
import com.eventplanner.entities.Users;
import com.eventplanner.security.jwt.JwtTokenUtils;
import com.eventplanner.services.api.AuthService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
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
    private final PlatformTransactionManager transactionManager;
    private final EmailConfirmationServiceImpl confirmationService;
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public JwtResponseDTO authenticateUser(@RequestBody JwtRequestDTO authRequest) {

        try
        {
            // Attempt to authenticate the user
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    authRequest.getUsername(),authRequest.getPassword())
            );

            // Authentication failed due to incorrect credentials
            UserDetails userDetails = usersService.loadUserByUsername(authRequest.getUsername());
            String token = jwtTokenUtil.generateToken(userDetails);

            return new JwtResponseDTO(token);
        }
        catch (BadCredentialsException e)
        {
            // Authentication failed due to incorrect credentials
            throw new BadCredentialsException("Error "
                    + authRequest.getUsername() + " " + authRequest.getPassword());
        }
    }

    //TODO добавить свое исключение если ник занят
    //@Transactional(isolation = Isolation.READ_COMMITTED)
    @Override
    public UserDTO registerUser(RegistrationUserDTO user)
    {
        // Checking the input data during registration
        if (!user.getPassword().equals(user.getConfirmPassword()))
        {
            throw new BadCredentialsException("Passwords don't matches");

        }
        if (usersService.getUserByUsername(user.getUsername()).isPresent())
        {
            throw new BadCredentialsException("The user with the specified name already exists");
        }

        /*TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        LOGGER.info("Start transaction");*/

        try
        {
            // Register the user
            Users users = usersService.registerUser(user);
            //transactionManager.commit(transaction);
            confirmationService.generateConfirmationCode(user);

            return new UserDTO(users.getEmail(), users.getUsername(),
                    users.getFirstname(), users.getLastname());
        }
        catch (Exception e)
        {
            //transactionManager.rollback(transaction);
            throw new RuntimeException("Error occurred while authenticate user");
        }
    }
}
