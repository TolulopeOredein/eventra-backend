// src/main/java/com/eventra/domain/user/User.java
package com.eventra.domain.user;

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
@Table(name = "users", schema = "user_management")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String phone;

    @Column(nullable = false)
    private String phoneNormalized;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    private String role = "host";
    private String status = "active";

    private boolean phoneVerified;
    private boolean emailVerified;
    private int verificationLevel;

    // Subscription fields
    private String subscriptionTier = "free";
    private String subscriptionStatus = "active";
    private LocalDateTime subscriptionEndDate;
    private boolean autoRenew = false;

    // Free event tracking
    private int totalEventsCreated = 0;
    private int freeEventsRemaining = 50;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime lastLogin;
    private int failedLoginAttempts;
    private LocalDateTime lockedUntil;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}