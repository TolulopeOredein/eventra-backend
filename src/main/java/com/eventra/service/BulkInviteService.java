// src/main/java/com/eventra/service/BulkInviteService.java
package com.eventra.service;

import com.eventra.domain.event.Event;
import com.eventra.domain.guest.Guest;
import com.eventra.repository.EventRepository;
import com.eventra.repository.GuestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class BulkInviteService {

    private final GuestRepository guestRepository;
    private final EventRepository eventRepository;
    private final NotificationService notificationService;
    private final QrService qrService;

    private final ConcurrentHashMap<UUID, InviteProgress> progressMap = new ConcurrentHashMap<>();

    /**
     * Send bulk invites asynchronously
     */
    @Async
    @Transactional
    public CompletableFuture<InviteResult> sendBulkInvites(UUID eventId, List<UUID> guestIds, String userEmail) {
        InviteProgress progress = InviteProgress.builder()
                .eventId(eventId)
                .total(guestIds.size())
                .processed(0)
                .successful(0)
                .failed(0)
                .startTime(LocalDateTime.now())
                .build();

        progressMap.put(eventId, progress);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        List<Guest> guests;
        if (guestIds == null || guestIds.isEmpty()) {
            guests = guestRepository.findByEventId(eventId);
            progress.setTotal(guests.size());
        } else {
            guests = guestRepository.findAllById(guestIds);
        }

        AtomicInteger successful = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);

        for (Guest guest : guests) {
            try {
                if (guest.isInviteSent()) {
                    failed.incrementAndGet();
                    progress.setFailed(failed.get());
                    continue;
                }

                // Generate invite link and QR code
                String inviteLink = generateInviteLink(eventId, guest.getId());
                String qrCodeUrl = qrService.generateQrForGuest(guest, event);

                guest.setInviteToken(extractTokenFromLink(inviteLink));
                guest.setQrCodeUrl(qrCodeUrl);
                guestRepository.save(guest);

                // Send invite
                notificationService.sendInvite(guest, event, inviteLink, qrCodeUrl);

                guest.setInviteSent(true);
                guest.setInviteSentAt(LocalDateTime.now());
                guestRepository.save(guest);

                successful.incrementAndGet();
                progress.setSuccessful(successful.get());

            } catch (Exception e) {
                log.error("Failed to send invite to guest: {}", guest.getId(), e);
                failed.incrementAndGet();
                progress.setFailed(failed.get());
            }

            progress.setProcessed(successful.get() + failed.get());
        }

        progress.setEndTime(LocalDateTime.now());

        return CompletableFuture.completedFuture(InviteResult.builder()
                .total(progress.getTotal())
                .successful(successful.get())
                .failed(failed.get())
                .durationMs(java.time.Duration.between(progress.getStartTime(), progress.getEndTime()).toMillis())
                .build());
    }

    /**
     * Get invite progress for an event
     */
    public InviteProgress getInviteProgress(UUID eventId) {
        return progressMap.getOrDefault(eventId, InviteProgress.builder().build());
    }

    private String generateInviteLink(UUID eventId, UUID guestId) {
        return "https://eventra.ng/invite/" + eventId + "/" + guestId;
    }

    private String extractTokenFromLink(String link) {
        return link.substring(link.lastIndexOf("/") + 1);
    }

    @lombok.Data
    @lombok.Builder
    public static class InviteProgress {
        private UUID eventId;
        private int total;
        private int processed;
        private int successful;
        private int failed;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
    }

    @lombok.Data
    @lombok.Builder
    public static class InviteResult {
        private int total;
        private int successful;
        private int failed;
        private long durationMs;
    }
}