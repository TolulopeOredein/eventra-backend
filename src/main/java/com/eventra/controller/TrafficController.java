// src/main/java/com/eventra/controller/TrafficController.java
package com.eventra.controller;

import com.eventra.domain.event.Event;
import com.eventra.domain.guest.Guest;
import com.eventra.repository.EventRepository;
import com.eventra.repository.GuestRepository;
import com.eventra.service.TrafficService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/events/{eventId}/traffic")
@RequiredArgsConstructor
public class TrafficController {

    private final TrafficService trafficService;
    private final EventRepository eventRepository;
    private final GuestRepository guestRepository;

    /**
     * Get traffic advisory for a specific guest
     */
    @GetMapping("/advisory/{guestId}")
    public ResponseEntity<TrafficService.TrafficAdvisory> getTrafficAdvisory(
            @PathVariable UUID eventId,
            @PathVariable UUID guestId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new RuntimeException("Guest not found"));

        // Verify access
        if (!event.getCreatedBy().toString().equals(userDetails.getUsername()) &&
                !guest.getPhone().equals(userDetails.getUsername())) {
            throw new RuntimeException("Access denied");
        }

        return ResponseEntity.ok(trafficService.getTrafficAdvisory(guest, event));
    }

    /**
     * Get traffic advisory for all guests (host only)
     */
    @GetMapping("/all")
    public ResponseEntity<Map<UUID, TrafficService.TrafficAdvisory>> getAllTrafficAdvisories(
            @PathVariable UUID eventId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // Verify host access
        if (!event.getCreatedBy().toString().equals(userDetails.getUsername())) {
            throw new RuntimeException("Access denied");
        }

        List<Guest> guests = guestRepository.findByEventId(eventId);
        return ResponseEntity.ok(trafficService.getBulkTrafficAdvisory(guests, event));
    }
}