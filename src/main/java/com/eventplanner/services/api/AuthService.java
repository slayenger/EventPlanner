package com.eventplanner.services.api;

import com.eventplanner.dtos.JwtRequestDTO;
import com.eventplanner.dtos.RegistrationUserDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

public interface AuthService {

    ResponseEntity<?> authenticateUser(@RequestBody JwtRequestDTO authRequest);

    ResponseEntity<?> registerUser(RegistrationUserDTO user);

}
