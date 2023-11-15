package com.eventplanner.controllers;

import com.eventplanner.dtos.BadCredentialsDTO;
import com.eventplanner.dtos.JwtRequestDTO;
import com.eventplanner.dtos.RegistrationUserDTO;
import com.eventplanner.dtos.UserDTO;
import com.eventplanner.services.impl.AuthServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthServiceImpl authenticateUser;

    @PostMapping("/auth")
    public ResponseEntity<?> createAuthToken(@RequestBody JwtRequestDTO authRequest)
    {
        try
        {
            return ResponseEntity.ok(authenticateUser.authenticateUser(authRequest));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return  new ResponseEntity<>(new BadCredentialsDTO(HttpStatus.UNAUTHORIZED.value(),
                    "Login or password entered incorrectly"), HttpStatus.UNAUTHORIZED) ;
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> userRegistration(@RequestBody RegistrationUserDTO registrationUserDTO)
    {
        try
        {
            UserDTO userDTO = authenticateUser.registerUser(registrationUserDTO);
            return ResponseEntity.ok(userDTO);
        }
        catch (RuntimeException e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating new user");
        }

    }
}
