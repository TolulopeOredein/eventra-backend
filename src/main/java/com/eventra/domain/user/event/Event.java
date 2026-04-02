// src/main/java/com/eventra/domain/event/Event.java
package com.eventra.domain.event;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "events", schema = "event_management")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;
    private String description;
    private String venue;
    private String venueAddress;
    private String city;
    private String state;
    private String country;
    private LocalDateTime eventDate;
    private String dressCode;
    private Integer expectedGuests;
    private String eventType;
    private String eventStyle;

    private UUID createdBy;
    private UUID beneficiaryId;
    private boolean beneficiaryVerified;
    private LocalDateTime beneficiaryVerifiedAt;

    private String status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}