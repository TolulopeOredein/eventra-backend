// src/main/java/com/eventra/controller/AdminFeeController.java
package com.eventra.controller;

import com.eventra.domain.fee.EventFeeOverride;
import com.eventra.domain.fee.FeeTemplate;
import com.eventra.domain.fee.HostFeeOverride;
import com.eventra.repository.EventFeeOverrideRepository;
import com.eventra.repository.FeeTemplateRepository;
import com.eventra.service.FeeConfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/fees")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminFeeController {

    private final FeeConfigurationService feeService;
    private final FeeTemplateRepository feeTemplateRepository;        // Add this
    private final EventFeeOverrideRepository eventFeeOverrideRepository;  // Add this

    /**
     * Get all fee templates
     */
    @GetMapping("/templates")
    public ResponseEntity<Iterable<FeeTemplate>> getAllTemplates() {
        return ResponseEntity.ok(feeTemplateRepository.findAll());
    }

    /**
     * Get active fee templates
     */
    @GetMapping("/templates/active")
    public ResponseEntity<Iterable<FeeTemplate>> getActiveTemplates() {
        return ResponseEntity.ok(feeTemplateRepository.findAllByIsActiveTrue());
    }

    /**
     * Get fee template by ID
     */
    @GetMapping("/templates/{templateId}")
    public ResponseEntity<FeeTemplate> getTemplate(@PathVariable UUID templateId) {
        return ResponseEntity.ok(feeTemplateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found")));
    }

    /**
     * Create new fee template
     */
    @PostMapping("/templates")
    public ResponseEntity<FeeTemplate> createTemplate(
            @RequestBody FeeTemplate template,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID adminId = getUserId(userDetails);
        return ResponseEntity.ok(feeService.createFeeTemplate(template, adminId));
    }

    /**
     * Update fee template
     */
    @PutMapping("/templates/{templateId}")
    public ResponseEntity<FeeTemplate> updateTemplate(
            @PathVariable UUID templateId,
            @RequestBody FeeTemplate template,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID adminId = getUserId(userDetails);
        return ResponseEntity.ok(feeService.updateFeeTemplate(templateId, template, adminId));
    }

    /**
     * Get host fee override
     */
    @GetMapping("/host/{hostId}")
    public ResponseEntity<HostFeeOverride> getHostOverride(@PathVariable UUID hostId) {
        return ResponseEntity.ok(feeService.getHostFeeOverride(hostId).orElse(null));
    }

    /**
     * Set host fee override
     */
    @PutMapping("/host/{hostId}")
    public ResponseEntity<HostFeeOverride> setHostOverride(
            @PathVariable UUID hostId,
            @RequestBody HostFeeOverride override,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID adminId = getUserId(userDetails);
        return ResponseEntity.ok(feeService.setHostFeeOverride(hostId, override, adminId));
    }

    /**
     * Remove host fee override
     */
    @DeleteMapping("/host/{hostId}")
    public ResponseEntity<Void> removeHostOverride(
            @PathVariable UUID hostId,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID adminId = getUserId(userDetails);
        feeService.removeHostFeeOverride(hostId, adminId);
        return ResponseEntity.ok().build();
    }

    /**
     * Get event fee override
     */
    @GetMapping("/event/{eventId}")
    public ResponseEntity<EventFeeOverride> getEventOverride(@PathVariable UUID eventId) {
        return ResponseEntity.ok(eventFeeOverrideRepository.findByEventId(eventId).orElse(null));
    }

    /**
     * Set event fee override (promotional)
     */
    @PutMapping("/event/{eventId}")
    public ResponseEntity<EventFeeOverride> setEventOverride(
            @PathVariable UUID eventId,
            @RequestBody EventFeeOverride override,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID adminId = getUserId(userDetails);
        return ResponseEntity.ok(feeService.setEventFeeOverride(eventId, override, adminId));
    }

    /**
     * Calculate fee for a transaction (preview)
     */
    @PostMapping("/calculate")
    public ResponseEntity<BigDecimal> calculateFee(
            @RequestParam BigDecimal amount,
            @RequestParam String transactionType,
            @RequestParam UUID eventId,
            @RequestParam UUID hostId) {
        return ResponseEntity.ok(feeService.calculateFee(amount, transactionType, eventId, hostId));
    }

    private UUID getUserId(UserDetails userDetails) {
        // In production, you would fetch the actual user ID from the database
        return UUID.randomUUID();
    }
}