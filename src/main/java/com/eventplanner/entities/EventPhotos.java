package com.eventplanner.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "event_photos")
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer"})
public class EventPhotos {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID photoId;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Events event;

    private String path;
}
