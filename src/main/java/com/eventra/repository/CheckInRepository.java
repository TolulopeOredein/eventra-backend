// src/main/java/com/eventra/repository/CheckInRepository.java
package com.eventra.repository;

import com.eventra.domain.entry.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CheckInRepository extends JpaRepository<CheckIn, UUID> {

    boolean existsByGuestIdAndEventId(UUID guestId, UUID eventId);
    long countByEventId(UUID eventId);
    long countByEventIdAndCheckInTimeAfter(UUID eventId, LocalDateTime after);
    List<CheckIn> findTop10ByEventIdOrderByCheckInTimeDesc(UUID eventId);
}