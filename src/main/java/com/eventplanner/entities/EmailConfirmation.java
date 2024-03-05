package com.eventplanner.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "email_confirmation")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailConfirmation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;


    @Column(name = "confirmation_code")
    private String confirmationCode;

    @Column(name = "code_created_at")
    private Date codeCreatedAt;

    @Column(name = "is_email_confirmed")
    private boolean isEmailConfirmed;
}
