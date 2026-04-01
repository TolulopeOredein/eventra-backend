// src/main/java/com/eventra/service/GuestImportService.java
package com.eventra.service;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import com.eventra.domain.guest.Guest;
import com.eventra.repository.GuestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuestImportService {

    private final GuestRepository guestRepository;

    /**
     * Generate CSV template for guest import
     */
    public byte[] generateCsvTemplate() {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             CSVWriter csvWriter = new CSVWriter(writer)) {

            // Headers
            String[] headers = {"Name*", "Phone*", "Email", "Tier", "Table Number"};
            csvWriter.writeNext(headers);

            // Example row
            String[] example = {"John Doe", "08012345678", "john@example.com", "VIP", "12"};
            csvWriter.writeNext(example);

            // Notes row
            String[] notes = {"Notes:", "Phone must be 10-15 digits", "Tier: VIP, Regular, Vendor", "Optional"};
            csvWriter.writeNext(notes);

            csvWriter.flush();
            return out.toByteArray();

        } catch (IOException e) {
            log.error("Failed to generate CSV template", e);
            throw new RuntimeException("Failed to generate template");
        }
    }

    /**
     * Import guests from CSV file
     */
    public ImportResult importGuests(UUID eventId, MultipartFile file) {
        List<Guest> imported = new ArrayList<>();
        List<ImportError> errors = new ArrayList<>();
        int rowNumber = 0;

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            List<String[]> rows = reader.readAll();

            if (rows.isEmpty()) {
                throw new RuntimeException("CSV file is empty");
            }

            // Skip header row
            for (int i = 1; i < rows.size(); i++) {
                rowNumber = i + 1;
                String[] row = rows.get(i);

                if (row.length < 2 || row[0].trim().isEmpty() || row[1].trim().isEmpty()) {
                    errors.add(new ImportError(rowNumber, "Name and phone are required"));
                    continue;
                }

                try {
                    Guest guest = Guest.builder()
                            .eventId(eventId)
                            .name(row[0].trim())
                            .phone(row[1].trim())
                            .email(row.length > 2 ? row[2].trim() : null)
                            .tier(row.length > 3 ? row[3].trim().toLowerCase() : "regular")
                            .build();

                    // Validate phone number
                    if (!isValidPhone(guest.getPhone())) {
                        errors.add(new ImportError(rowNumber, "Invalid phone number: " + guest.getPhone()));
                        continue;
                    }

                    // Validate tier
                    if (!isValidTier(guest.getTier())) {
                        guest.setTier("regular");
                    }

                    imported.add(guestRepository.save(guest));

                } catch (Exception e) {
                    errors.add(new ImportError(rowNumber, e.getMessage()));
                }
            }

        } catch (IOException | CsvException e) {
            log.error("CSV import failed", e);
            throw new RuntimeException("Failed to parse CSV file: " + e.getMessage());
        }

        return ImportResult.builder()
                .importedCount(imported.size())
                .errors(errors)
                .build();
    }

    private boolean isValidPhone(String phone) {
        return phone != null && phone.matches("^[0-9]{10,15}$");
    }

    private boolean isValidTier(String tier) {
        return tier != null && List.of("vip", "regular", "vendor").contains(tier.toLowerCase());
    }

    @lombok.Data
    @lombok.Builder
    public static class ImportResult {
        private int importedCount;
        private List<ImportError> errors;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ImportError {
        private int rowNumber;
        private String message;
    }
}