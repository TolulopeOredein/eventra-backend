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
    private String passwordHash;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Builder.Default
    private String role = "host";

    @Builder.Default
    private String status = "active";

    @Builder.Default
    private boolean phoneVerified = false;

    @Builder.Default
    private boolean emailVerified = false;

    @Builder.Default
    private int verificationLevel = 0;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime lastLogin;

    @Builder.Default
    private int failedLoginAttempts = 0;

    private LocalDateTime lockedUntil;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}