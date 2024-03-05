package com.eventplanner.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "invitation_link")
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer"})
public class InvitationLink {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID linkId;

    @Column(unique = true)
    private String shortIdentifier;

    @OneToOne
    private EventInvitations invitation;

    //TODO добавить время жизни ссылки)
}
