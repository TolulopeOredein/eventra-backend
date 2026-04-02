// src/main/java/com/eventra/service/GuestService.java
package com.eventra.service;

import com.opencsv.CSVReader;
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
import java.util.ArrayList;
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
    public ImportResult importGuests(UUID eventId, MultipartFile file, String userEmail) {
        validateAccess(eventId, userEmail);

        List<ImportError> errors = new ArrayList<>();
        int imported = 0;

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> rows = reader.readAll();
            if (rows.size() < 2) {
                return ImportResult.failure("CSV file has no data rows");
            }

            // Skip header row
            List<String[]> dataRows = rows.subList(1, rows.size());

            for (int i = 0; i < dataRows.size(); i++) {
                String[] row = dataRows.get(i);
                int rowNumber = i + 2; // +2 because header is row 1

                if (row.length < 2) {
                    errors.add(new ImportError(rowNumber, "Missing name or phone"));
                    continue;
                }

                String name = row[0].trim();
                String phone = row[1].trim();
                String email = row.length > 2 ? row[2].trim() : null;
                String tier = row.length > 3 ? row[3].trim() : "regular";

                if (name.isEmpty()) {
                    errors.add(new ImportError(rowNumber, "Name is required"));
                    continue;
                }

                if (phone.isEmpty()) {
                    errors.add(new ImportError(rowNumber, "Phone is required"));
                    continue;
                }

                try {
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
                } catch (Exception e) {
                    errors.add(new ImportError(rowNumber, "Invalid phone number: " + phone));
                }
            }

            log.info("Imported {} guests for event: {} ({} errors)", imported, eventId, errors.size());

        } catch (Exception e) {
            log.error("Import failed", e);
            throw new RuntimeException("Import failed: " + e.getMessage());
        }

        return ImportResult.success(imported, errors);
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
        log.info("Guest added: {} for event {}", guest.getName(), eventId);

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

        log.info("Sent {} invites for event {}", sent, eventId);
        return sent;
    }

    @Transactional
    public void deleteGuest(UUID eventId, UUID guestId, String userEmail) {
        validateAccess(eventId, userEmail);
        Guest guest = guestRepository.findByEventIdAndId(eventId, guestId)
                .orElseThrow(() -> new RuntimeException("Guest not found"));
        guestRepository.delete(guest);
        log.info("Guest deleted: {} from event {}", guestId, eventId);
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

    // Inner class for import result
    public static class ImportResult {
        private final boolean success;
        private final int importedCount;
        private final List<ImportError> errors;
        private final String errorMessage;

        private ImportResult(boolean success, int importedCount, List<ImportError> errors, String errorMessage) {
            this.success = success;
            this.importedCount = importedCount;
            this.errors = errors;
            this.errorMessage = errorMessage;
        }

        public static ImportResult success(int importedCount, List<ImportError> errors) {
            return new ImportResult(true, importedCount, errors, null);
        }

        public static ImportResult failure(String errorMessage) {
            return new ImportResult(false, 0, new ArrayList<>(), errorMessage);
        }

        public boolean isSuccess() { return success; }
        public int getImportedCount() { return importedCount; }
        public List<ImportError> getErrors() { return errors; }
        public String getErrorMessage() { return errorMessage; }
        public boolean hasErrors() { return !errors.isEmpty(); }
    }

    public static class ImportError {
        private final int row;
        private final String message;

        public ImportError(int row, String message) {
            this.row = row;
            this.message = message;
        }

        public int getRow() { return row; }
        public String getMessage() { return message; }
    }
}