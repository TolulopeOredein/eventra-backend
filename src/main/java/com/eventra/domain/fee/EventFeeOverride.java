// src/main/java/com/eventra/domain/fee/EventFeeOverride.java
package com.eventra.domain.fee;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "event_fee_overrides", schema = "fee_management")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventFeeOverride {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private UUID eventId;

    private BigDecimal platformFeePercentage;
    private BigDecimal platformFeeFixed;

    private boolean isPromotional;
    private LocalDateTime promotionalEndDate;
    private String promotionalNote;

    private UUID approvedBy;
    private LocalDateTime approvedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;
}