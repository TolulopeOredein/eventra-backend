// src/main/java/com/eventra/controller/GuestImportController.java
package com.eventra.controller;

import com.eventra.service.GuestImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/events/{eventId}/guests")
@RequiredArgsConstructor
public class GuestImportController {

    private final GuestImportService guestImportService;

    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        byte[] template = guestImportService.generateCsvTemplate();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=guest_template.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(template);
    }

    @PostMapping("/import")
    public ResponseEntity<GuestImportService.ImportResult> importGuests(
            @PathVariable UUID eventId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {

        // Validate access
        // (existing validation logic)

        GuestImportService.ImportResult result = guestImportService.importGuests(eventId, file);
        return ResponseEntity.ok(result);
    }
}