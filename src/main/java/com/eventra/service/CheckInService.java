// src/main/java/com/eventra/service/CheckInService.java
package com.eventra.service;

import com.eventra.domain.entry.CheckIn;
import com.eventra.repository.CheckInRepository;
import com.eventra.domain.event.Event;
import com.eventra.repository.EventRepository;
import com.eventra.domain.guest.Guest;
import com.eventra.repository.GuestRepository;
import com.eventra.repository.CheckInRepository;
import com.eventra.repository.EventRepository;
import com.eventra.repository.GuestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckInService {

    private final CheckInRepository checkInRepository;
    private final GuestRepository guestRepository;
    private final EventRepository eventRepository;
    private final QrService qrService;
    private final NotificationService notificationService;

    @Transactional
    public Map<String, Object> verifyAndCheckIn(String qrData, UUID eventId, String deviceId, String gate) {
        // Verify QR code
        Map<String, Object> payload = qrService.verifyQr(qrData, eventId);
        UUID guestId = UUID.fromString(payload.get("guestId").toString());

        // Check if already checked in
        if (checkInRepository.existsByGuestIdAndEventId(guestId, eventId)) {
            throw new RuntimeException("Guest already checked in");
        }

        // Get guest
        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new RuntimeException("Guest not found"));

        // Update guest check-in status
        guest.setCheckInStatus(true);
        guest.setCheckInTime(LocalDateTime.now());
        guest.setCheckInGate(gate);
        guestRepository.save(guest);

        // Create check-in record
        CheckIn checkIn = CheckIn.builder()
                .guestId(guestId)
                .eventId(eventId)
                .deviceId(deviceId)
                .gate(gate)
                .checkInTime(LocalDateTime.now())
                .synced(true)
                .build();

        checkInRepository.save(checkIn);

        // Send welcome message
        Event event = eventRepository.findById(eventId).orElseThrow();
        notificationService.sendWelcomeMessage(guest, event);

        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("guestName", guest.getName());
        result.put("tier", guest.getTier());
        result.put("checkInTime", LocalDateTime.now().toString());
        result.put("gate", gate);

        return result;
    }

    /**
     * Get live check-in statistics for an event
     */
    public Map<String, Object> getLiveStats(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        long checkedInCount = checkInRepository.countByEventId(eventId);
        long totalGuests = guestRepository.countByEventId(eventId);
        double gateRate = getCurrentGateRate(eventId, 30);

        // Get recent check-ins - use List<Map<String, Object>> instead of List<Map<String, String>>
        List<Map<String, Object>> recentCheckins = new ArrayList<>();
        List<CheckIn> recent = checkInRepository.findTop10ByEventIdOrderByCheckInTimeDesc(eventId);

        for (CheckIn checkIn : recent) {
            Guest guest = guestRepository.findById(checkIn.getGuestId()).orElse(null);
            Map<String, Object> checkInData = new HashMap<>();
            checkInData.put("time", checkIn.getCheckInTime().toString());
            checkInData.put("name", guest != null ? guest.getName() : "Unknown");
            checkInData.put("tier", guest != null ? guest.getTier() : "regular");
            checkInData.put("gate", checkIn.getGate());
            recentCheckins.add(checkInData);
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("checkedIn", checkedInCount);
        stats.put("totalGuests", totalGuests);
        stats.put("gateRate", gateRate);
        stats.put("completionPercentage", totalGuests > 0 ? (double) checkedInCount / totalGuests * 100 : 0);
        stats.put("recentCheckins", recentCheckins);
        stats.put("isLive", "live".equals(event.getStatus()));

        return stats;
    }

    public List<CheckIn> getRecentCheckins(UUID eventId, int limit) {
        return checkInRepository.findTop10ByEventIdOrderByCheckInTimeDesc(eventId);
    }

    public long getCheckedInCount(UUID eventId) {
        return checkInRepository.countByEventId(eventId);
    }

    public double getCurrentGateRate(UUID eventId, int minutes) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(minutes);
        long count = checkInRepository.countByEventIdAndCheckInTimeAfter(eventId, since);
        return (double) count / minutes;
    }

    @Transactional
    public void syncOfflineCheckins(List<Map<String, Object>> offlineCheckins, String deviceId) {
        for (Map<String, Object> checkin : offlineCheckins) {
            try {
                String qrData = (String) checkin.get("qrData");
                UUID eventId = UUID.fromString((String) checkin.get("eventId"));
                String gate = (String) checkin.get("gate");
                verifyAndCheckIn(qrData, eventId, deviceId, gate);
            } catch (Exception e) {
                log.error("Failed to sync offline check-in", e);
                // Store in dead letter queue for manual review
            }
        }
    }
}