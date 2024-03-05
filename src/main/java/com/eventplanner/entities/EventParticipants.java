package com.eventplanner.entities;

import jakarta.persistence.*;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "event_participants")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventParticipants {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID participantId;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Events event;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
