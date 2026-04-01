// src/main/java/com/eventra/repository/QrCodeRepository.java
package com.eventra.repository;

import com.eventra.domain.entry.QrCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface QrCodeRepository extends JpaRepository<QrCode, UUID> {

    Optional<QrCode> findByCodeHash(String codeHash);
    Optional<QrCode> findByGuestIdAndEventId(UUID guestId, UUID eventId);
    boolean existsByGuestIdAndEventId(UUID guestId, UUID eventId);
}