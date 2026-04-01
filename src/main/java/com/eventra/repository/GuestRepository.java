// src/main/java/com/eventra/repository/GuestRepository.java
package com.eventra.repository;

import com.eventra.domain.guest.Guest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GuestRepository extends JpaRepository<Guest, UUID> {

    long countByEventId(UUID eventId);

    List<Guest> findByEventId(UUID eventId);
    Page<Guest> findByEventId(UUID eventId, Pageable pageable);
    Optional<Guest> findByEventIdAndId(UUID eventId, UUID guestId);
    Optional<Guest> findByEventIdAndPhone(UUID eventId, String phone);
    Optional<Guest> findByInviteToken(String token);

    List<Guest> findByEventIdAndRsvpStatus(UUID eventId, String status);
    List<Guest> findByEventIdAndCheckInStatusFalse(UUID eventId);

    long countByEventIdAndRsvpStatus(UUID eventId, String status);
    long countByEventIdAndCheckInStatusTrue(UUID eventId);

    @Modifying
    @Query("UPDATE Guest g SET g.checkInStatus = true, g.checkInTime = :checkInTime, g.checkInGate = :gate WHERE g.id = :guestId")
    void markCheckedIn(@Param("guestId") UUID guestId, @Param("checkInTime") LocalDateTime checkInTime, @Param("gate") String gate);

    @Modifying
    @Query("UPDATE Guest g SET g.inviteSent = true, g.inviteSentAt = :sentAt WHERE g.id = :guestId")
    void markInviteSent(@Param("guestId") UUID guestId, @Param("sentAt") LocalDateTime sentAt);
}