// src/main/java/com/eventra/controller/AuditController.java
package com.eventra.controller;

import com.eventra.domain.audit.AuditLog;
import com.eventra.repository.AuditLogRepository;
import com.eventra.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/audit")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;
    private final AuditLogRepository auditLogRepository;  // Add this

    /**
     * Get all audit logs (paginated)
     */
    @GetMapping
    public ResponseEntity<Page<AuditLog>> getAllAuditLogs(Pageable pageable) {
        return ResponseEntity.ok(auditLogRepository.findAll(pageable));
    }

    /**
     * Get audit logs for a specific user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<AuditLog>> getUserAuditLogs(
            @PathVariable UUID userId,
            Pageable pageable) {
        return ResponseEntity.ok(auditService.getUserAuditLogs(userId, pageable));
    }

    /**
     * Get audit logs for a specific resource
     */
    @GetMapping("/resource/{resourceType}/{resourceId}")
    public ResponseEntity<Page<AuditLog>> getResourceAuditLogs(
            @PathVariable String resourceType,
            @PathVariable UUID resourceId,
            Pageable pageable) {
        return ResponseEntity.ok(auditService.getResourceAuditLogs(resourceType, resourceId, pageable));
    }

    /**
     * Get recent audit logs
     */
    @GetMapping("/recent")
    public ResponseEntity<List<AuditLog>> getRecentAuditLogs(@RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(auditService.getRecentAuditLogs(limit));
    }

    /**
     * Get audit logs by date range
     */
    @GetMapping("/date-range")
    public ResponseEntity<Page<AuditLog>> getAuditLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            Pageable pageable) {
        return ResponseEntity.ok(auditService.getAuditLogsByDateRange(start, end, pageable));
    }
}