// src/main/java/com/eventra/controller/GuestController.java
package com.eventra.controller;

import com.eventra.dto.guest.GuestResponse;
import com.eventra.service.GuestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/events/{eventId}/guests")
@RequiredArgsConstructor
public class GuestController {

    private final GuestService guestService;

    @PostMapping("/import")
    public ResponseEntity<Integer> importGuests(
            @PathVariable UUID eventId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        int count = guestService.importGuests(eventId, file, userDetails.getUsername());
        return ResponseEntity.ok(count);
    }

    @GetMapping
    public ResponseEntity<Page<GuestResponse>> getGuests(
            @PathVariable UUID eventId,
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        return ResponseEntity.ok(guestService.getGuests(eventId, userDetails.getUsername(), pageable));
    }

    @GetMapping("/{guestId}")
    public ResponseEntity<GuestResponse> getGuest(
            @PathVariable UUID eventId,
            @PathVariable UUID guestId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(guestService.getGuest(eventId, guestId, userDetails.getUsername()));
    }

    @PostMapping
    public ResponseEntity<GuestResponse> addGuest(
            @PathVariable UUID eventId,
            @RequestBody GuestResponse request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(guestService.addGuest(eventId, request, userDetails.getUsername()));
    }

    @PostMapping("/invite")
    public ResponseEntity<Integer> sendInvites(
            @PathVariable UUID eventId,
            @RequestParam(required = false) List<UUID> guestIds,
            @AuthenticationPrincipal UserDetails userDetails) {
        int count = guestService.sendInvites(eventId, guestIds, userDetails.getUsername());
        return ResponseEntity.ok(count);
    }

    @DeleteMapping("/{guestId}")
    public ResponseEntity<Void> deleteGuest(
            @PathVariable UUID eventId,
            @PathVariable UUID guestId,
            @AuthenticationPrincipal UserDetails userDetails) {
        guestService.deleteGuest(eventId, guestId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}