// src/main/java/com/eventra/domain/entry/CheckIn.java
package com.eventra.domain.entry;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "check_ins", schema = "entry_management")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID guestId;

    @Column(nullable = false)
    private UUID eventId;

    private String deviceId;
    private String gate;

    @CreationTimestamp
    private LocalDateTime checkInTime;

    private boolean synced = true;
}