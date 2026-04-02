// src/main/java/com/eventra/domain/fee/FeeTemplate.java
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
@Table(name = "fee_templates", schema = "fee_management")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeeTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String name;

    private String description;
    private boolean isActive = true;

    // Platform fees
    private BigDecimal platformFeePercentage;
    private BigDecimal platformFeeFixed;

    // Module-specific fees
    private BigDecimal asobiFeePercentage;
    private BigDecimal vendorEscrowFeePercentage;
    private BigDecimal ticketFeePercentage;
    private BigDecimal virtualSprayFeePercentage;
    private BigDecimal fxMarkupPercentage;

    // Subscription fees
    private BigDecimal monthlySubscriptionFee;
    private BigDecimal annualSubscriptionDiscount;

    // Limits
    private BigDecimal minTransactionFee;
    private BigDecimal maxTransactionFee;

    @Column(columnDefinition = "text[]")
    private String[] applicableTiers;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}