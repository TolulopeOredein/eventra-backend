// src/main/java/com/eventra/domain/fee/HostFeeOverride.java
package com.eventra.domain.fee;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "host_fee_overrides", schema = "fee_management")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HostFeeOverride {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private UUID hostId;

    // Override values (null means use template default)
    private BigDecimal platformFeePercentage;
    private BigDecimal platformFeeFixed;
    private BigDecimal asobiFeePercentage;
    private BigDecimal vendorEscrowFeePercentage;
    private BigDecimal ticketFeePercentage;
    private BigDecimal virtualSprayFeePercentage;
    private BigDecimal fxMarkupPercentage;

    private BigDecimal customMonthlyFee;

    private String notes;
    private UUID configuredBy;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
