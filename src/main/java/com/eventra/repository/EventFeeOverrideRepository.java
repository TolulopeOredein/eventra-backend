// src/main/java/com/eventra/repository/EventFeeOverrideRepository.java
package com.eventra.repository;

import com.eventra.domain.fee.EventFeeOverride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventFeeOverrideRepository extends JpaRepository<EventFeeOverride, UUID> {

    Optional<EventFeeOverride> findByEventId(UUID eventId);
    List<EventFeeOverride> findByIsPromotionalTrueAndPromotionalEndDateAfter(LocalDateTime date);
}