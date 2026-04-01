// src/main/java/com/eventra/domain/guest/Guest.java
package com.eventra.domain.guest;

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
@Table(name = "guests", schema = "guest_management")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Guest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID eventId;

    @Column(nullable = false)
    private String name;

    private String email;

    @Column(nullable = false)
    private String phone;

    private String phoneNormalized;

    private String tier = "regular";

    private String rsvpStatus = "pending";

    private LocalDateTime rsvpAt;

    private String mealPreference;
    private String dietaryRestrictions;

    private boolean asobiPaid;
    private String asobiSize;

    private boolean checkInStatus;
    private LocalDateTime checkInTime;
    private String checkInGate;

    private String qrCodeUrl;
    private String inviteToken;
    private boolean inviteSent;
    private LocalDateTime inviteSentAt;
    private LocalDateTime inviteOpenedAt;
    private int inviteClickCount;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}