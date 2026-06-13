// src/main/java/com/eventra/service/CheckInService.java
package com.eventra.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckInService {

    private final QrService qrService;

    @Transactional
    public boolean checkInGuest(String guestId, String eventId, String gate) {
        try {
            // Implementation for check-in logic
            log.info("Guest {} checked in at gate {} for event {}", guestId, gate, eventId);
            return true;
        } catch (Exception e) {
            log.error("Check-in failed: {}", e.getMessage());
            return false;
        }
    }

    public byte[] generateGuestQrCode(String guestId, String eventId) {
        return qrService.generateQrCodeForGuest(guestId, eventId).getBytes();
    }
}