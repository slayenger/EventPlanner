package com.eventplanner.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "event_invitations")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventInvitations {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID invitationId;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Events event;

    @ManyToOne
    @JoinColumn(name = "invited_by_user_id")
    private User invitedByUser;

    private String invitationLink;

    private Timestamp createdAt;
}
