package com.eventplanner.services.impl;

import com.eventplanner.dtos.MailStructure;
import com.eventplanner.dtos.RegistrationUserDTO;
import com.eventplanner.entities.EmailConfirmation;
import com.eventplanner.entities.User;
import com.eventplanner.exceptions.EmailConfirmedException;
import com.eventplanner.exceptions.EmailNotConfirmedException;
import com.eventplanner.exceptions.NotFoundException;
import com.eventplanner.repositories.EmailConfirmationRepository;
import com.eventplanner.repositories.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl {

   private final EmailConfirmationRepository emailConfirmationRepository;
   private final UsersRepository usersRepository;
   private final JavaMailSender mailSender;

   @Value("${spring.mail.username}")
   private String fromMail;


   @Async
   public void generateConfirmationCode(RegistrationUserDTO registrationUserDTO)
   {
       String confirmationCode = generateCode();

       User user = usersRepository.findByEmail(registrationUserDTO.getEmail())
               .orElseThrow(() ->
                       new NotFoundException("User with email " + registrationUserDTO.getEmail() + " not found."));

       EmailConfirmation emailConfirmation = new EmailConfirmation();
       emailConfirmation.setUser(user);
       emailConfirmation.setConfirmationCode(confirmationCode);
       emailConfirmation.setCodeCreatedAt(new Date());
       emailConfirmation.setEmailConfirmed(false);
       emailConfirmationRepository.save(emailConfirmation);

       MailStructure mailStructure = createConfirmationMail(confirmationCode);
       sendMail(registrationUserDTO.getEmail(), mailStructure);
   }

   public void confirmEmailAddress(String inputCode, UUID userId)
   {
       EmailConfirmation emailConfirmation = emailConfirmationRepository.findByUser_UserId(userId);
       String realCode = emailConfirmation.getConfirmationCode();
       if (emailConfirmation.isEmailConfirmed())
       {
           throw new EmailConfirmedException("Your email is already confirmed.");
       }
       if (inputCode.equals(realCode))
       {
           emailConfirmation.setEmailConfirmed(true);
           emailConfirmationRepository.save(emailConfirmation);
       }
       else
       {
           throw new EmailNotConfirmedException("The entered code is incorrect. Please try again or" +
                   " send the new code again");
       }
   }

   @Async
   public void sendCodeAgain(UUID userId)
   {
       User user = usersRepository.getReferenceById(userId);
       EmailConfirmation emailConfirmation = emailConfirmationRepository.findByUser(user);
       if (emailConfirmation.isEmailConfirmed())
       {
           throw new EmailConfirmedException("Your email is already confirmed.");
       }
       String confirmationCode = generateCode();
       emailConfirmation.setConfirmationCode(confirmationCode);
       emailConfirmation.setCodeCreatedAt(new Date());
       emailConfirmationRepository.save(emailConfirmation);

       MailStructure mailStructure = createConfirmationMail(confirmationCode);
       sendMail(user.getEmail(), mailStructure);
   }

   private void sendMail(String mail, MailStructure mailStructure)
   {
       SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
       simpleMailMessage.setFrom(fromMail);
       simpleMailMessage.setSubject(mailStructure.getSubject());
       simpleMailMessage.setText(mailStructure.getMessage());
       simpleMailMessage.setTo(mail);

       mailSender.send(simpleMailMessage);
   }
    private MailStructure createConfirmationMail(String confirmationCode) {
        String subject = "Подтверждение почты";
        String message = "Привет!\n\nДля подключения осталось только ввести код: " + confirmationCode + "\n\nСпасибо, что вы с нами!";

        return new MailStructure(subject, message);
    }

    private String generateCode() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(1111, 9999));
    }
}
