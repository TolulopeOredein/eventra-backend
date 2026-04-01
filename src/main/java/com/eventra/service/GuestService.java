// src/main/java/com/eventra/service/GuestService.java
package com.eventra.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.eventra.domain.event.Event;
import com.eventra.repository.EventRepository;
import com.eventra.domain.guest.Guest;
import com.eventra.domain.user.User;
import com.eventra.dto.guest.GuestResponse;
import com.eventra.repository.EventRepository;
import com.eventra.repository.GuestRepository;
import com.eventra.repository.UserRepository;
import com.eventra.util.PhoneNumberUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuestService {

    private final GuestRepository guestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final PhoneNumberUtil phoneNumberUtil;
    private final QrService qrService;
    private final NotificationService notificationService;

    @Transactional
    public int importGuests(UUID eventId, MultipartFile file, String userEmail) {
        validateAccess(eventId, userEmail);

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> rows = reader.readAll();
            if (rows.size() < 2) return 0;

            // Skip header row
            List<String[]> dataRows = rows.subList(1, rows.size());
            int imported = 0;

            for (String[] row : dataRows) {
                if (row.length < 2) continue;

                String name = row[0];
                String phone = row[1];
                String email = row.length > 2 ? row[2] : null;
                String tier = row.length > 3 ? row[3] : "regular";

                String normalizedPhone = phoneNumberUtil.normalize(phone);

                Guest guest = Guest.builder()
                        .eventId(eventId)
                        .name(name)
                        .phone(phone)
                        .phoneNormalized(normalizedPhone)
                        .email(email)
                        .tier(tier)
                        .rsvpStatus("pending")
                        .build();

                guestRepository.save(guest);
                imported++;
            }

            log.info("Imported {} guests for event: {}", imported, eventId);
            return imported;

        } catch (Exception e) {
            log.error("Import failed", e);
            throw new RuntimeException("Import failed: " + e.getMessage());
        }
    }

    public Page<GuestResponse> getGuests(UUID eventId, String userEmail, Pageable pageable) {
        validateAccess(eventId, userEmail);
        return guestRepository.findByEventId(eventId, pageable).map(this::mapToResponse);
    }

    public GuestResponse getGuest(UUID eventId, UUID guestId, String userEmail) {
        validateAccess(eventId, userEmail);
        Guest guest = guestRepository.findByEventIdAndId(eventId, guestId)
                .orElseThrow(() -> new RuntimeException("Guest not found"));
        return mapToResponse(guest);
    }

    @Transactional
    public GuestResponse addGuest(UUID eventId, GuestResponse request, String userEmail) {
        validateAccess(eventId, userEmail);

        String normalizedPhone = phoneNumberUtil.normalize(request.getPhone());

        Guest guest = Guest.builder()
                .eventId(eventId)
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .phoneNormalized(normalizedPhone)
                .tier(request.getTier())
                .rsvpStatus("pending")
                .build();

        guest = guestRepository.save(guest);
        return mapToResponse(guest);
    }

    @Transactional
    public int sendInvites(UUID eventId, List<UUID> guestIds, String userEmail) {
        validateAccess(eventId, userEmail);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        List<Guest> guests;
        if (guestIds == null || guestIds.isEmpty()) {
            guests = guestRepository.findByEventId(eventId);
        } else {
            guests = guestRepository.findAllById(guestIds);
        }

        int sent = 0;
        for (Guest guest : guests) {
            if (guest.isInviteSent()) continue;

            String inviteLink = generateInviteLink(eventId, guest.getId());
            String qrCodeUrl = qrService.generateQrForGuest(guest, event);

            guest.setInviteToken(extractTokenFromLink(inviteLink));
            guest.setQrCodeUrl(qrCodeUrl);
            guestRepository.save(guest);

            notificationService.sendInvite(guest, event, inviteLink, qrCodeUrl);

            guest.setInviteSent(true);
            guest.setInviteSentAt(LocalDateTime.now());
            guestRepository.save(guest);

            sent++;
        }

        return sent;
    }

    @Transactional
    public void deleteGuest(UUID eventId, UUID guestId, String userEmail) {
        validateAccess(eventId, userEmail);
        Guest guest = guestRepository.findByEventIdAndId(eventId, guestId)
                .orElseThrow(() -> new RuntimeException("Guest not found"));
        guestRepository.delete(guest);
    }

    private void validateAccess(UUID eventId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (!event.getCreatedBy().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }
    }

    private String generateInviteLink(UUID eventId, UUID guestId) {
        return "https://eventra.ng/invite/" + eventId + "/" + guestId;
    }

    private String extractTokenFromLink(String link) {
        return link.substring(link.lastIndexOf("/") + 1);
    }

    private GuestResponse mapToResponse(Guest guest) {
        return GuestResponse.builder()
                .id(guest.getId())
                .name(guest.getName())
                .email(guest.getEmail())
                .phone(guest.getPhone())
                .tier(guest.getTier())
                .rsvpStatus(guest.getRsvpStatus())
                .rsvpAt(guest.getRsvpAt())
                .mealPreference(guest.getMealPreference())
                .dietaryRestrictions(guest.getDietaryRestrictions())
                .asobiPaid(guest.isAsobiPaid())
                .asobiSize(guest.getAsobiSize())
                .checkInStatus(guest.isCheckInStatus())
                .checkInTime(guest.getCheckInTime())
                .qrCodeUrl(guest.getQrCodeUrl())
                .build();
    }
}