// src/main/java/com/eventra/domain/audit/AuditLog.java
package com.eventra.domain.audit;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs", schema = "audit")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID userId;
    private String userEmail;
    private String userRole;

    @Column(nullable = false)
    private String action;

    private String resourceType;
    private UUID resourceId;

    @Column(columnDefinition = "jsonb")
    private String details;

    private String ipAddress;
    private String userAgent;

    @CreationTimestamp
    private LocalDateTime createdAt;
}