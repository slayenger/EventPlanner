package com.eventplanner.controllers;

import com.eventplanner.dtos.JwtRequestDTO;
import com.eventplanner.dtos.RegistrationUserDTO;
import com.eventplanner.services.impl.AuthServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthServiceImpl authenticateUser;

    @PostMapping("/auth")
    public ResponseEntity<?> createAuthToken(@RequestBody JwtRequestDTO authRequest) {
        return authenticateUser.authenticateUser(authRequest);
    }

    @PostMapping("/register")
    public ResponseEntity<?> userRegistration(@RequestBody RegistrationUserDTO registrationUserDTO) {
        return authenticateUser.registerUser(registrationUserDTO);
    }
}
