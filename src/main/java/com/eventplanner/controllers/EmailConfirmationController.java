package com.eventplanner.controllers;


import com.eventplanner.dtos.CustomUserDetailsDTO;
import com.eventplanner.exceptions.EmailConfirmedException;
import com.eventplanner.exceptions.EmailNotConfirmedException;
import com.eventplanner.services.impl.EmailServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/email")
public class EmailConfirmationController {

    private final EmailServiceImpl confirmationService;

    @PatchMapping("/confirm")
    public ResponseEntity<?> confirmEmailAddress(@RequestParam String inputCode,
                                                 @AuthenticationPrincipal CustomUserDetailsDTO userDetailsDTO)
    {
        try
        {
            confirmationService.confirmEmailAddress(inputCode, userDetailsDTO.getUserId());
            return ResponseEntity.status(HttpStatus.OK).body("You have successfully verified your email");
        }
        catch (EmailConfirmedException | EmailNotConfirmedException err)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err.getMessage());
        }
    }

    @PatchMapping("/send-code-again")
    public ResponseEntity<?> sendCodeAgain(@AuthenticationPrincipal CustomUserDetailsDTO userDetailsDTO)
    {
        try
        {
            confirmationService.sendCodeAgain(userDetailsDTO.getUserId());
            return ResponseEntity.status(HttpStatus.OK).body("The confirmation code has been sent to your email");
        }
        catch (EmailConfirmedException err)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err.getMessage());
        }
    }

}
