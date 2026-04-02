// src/main/java/com/eventra/service/AuditService.java
package com.eventra.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.eventra.domain.audit.AuditLog;
import com.eventra.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    /**
     * Generic audit log method
     */
    public void log(String action, String resourceType, UUID resourceId, Object details) {
        try {
            UserDetails userDetails = null;
            try {
                userDetails = (UserDetails) SecurityContextHolder.getContext()
                        .getAuthentication().getPrincipal();
            } catch (Exception e) {
                // No authenticated user
            }

            HttpServletRequest request = null;
            try {
                request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                        .getRequest();
            } catch (Exception e) {
                // No request context
            }

            AuditLog auditLog = AuditLog.builder()
                    .userEmail(userDetails != null ? userDetails.getUsername() : "system")
                    .userRole(userDetails != null ? userDetails.getAuthorities().toString() : "SYSTEM")
                    .action(action)
                    .resourceType(resourceType)
                    .resourceId(resourceId)
                    .details(details != null ? objectMapper.writeValueAsString(details) : null)
                    .ipAddress(request != null ? request.getRemoteAddr() : null)
                    .userAgent(request != null ? request.getHeader("User-Agent") : null)
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit log: {} - {}", action, resourceId);
        } catch (Exception e) {
            log.error("Failed to log audit entry", e);
        }
    }

    /**
     * Log event action
     */
    public void logEventAction(UUID eventId, String action, Map<String, Object> details) {
        log(action, "EVENT", eventId, details);
    }

    /**
     * Log guest action
     */
    public void logGuestAction(UUID guestId, String action, Map<String, Object> details) {
        log(action, "GUEST", guestId, details);
    }

    /**
     * Log payment action
     */
    public void logPaymentAction(UUID transactionId, String action, Map<String, Object> details) {
        log(action, "PAYMENT", transactionId, details);
    }

    /**
     * Log user action
     */
    public void logUserAction(UUID userId, String action, Map<String, Object> details) {
        log(action, "USER", userId, details);
    }

    /**
     * Log login attempt
     */
    public void logLoginAttempt(String email, boolean success, String reason) {
        Map<String, Object> details = new HashMap<>();
        details.put("email", email);
        details.put("success", success);
        details.put("reason", reason);
        log("LOGIN_ATTEMPT", "AUTH", null, details);
    }

    /**
     * Log API access
     */
    public void logApiAccess(String endpoint, String method, int statusCode, long responseTime) {
        Map<String, Object> details = new HashMap<>();
        details.put("endpoint", endpoint);
        details.put("method", method);
        details.put("statusCode", statusCode);
        details.put("responseTimeMs", responseTime);
        log("API_ACCESS", "API", null, details);
    }

    /**
     * Get audit logs for a specific user
     */
    public Page<AuditLog> getUserAuditLogs(UUID userId, Pageable pageable) {
        return auditLogRepository.findByUserId(userId, pageable);
    }

    /**
     * Get audit logs for a specific resource
     */
    public Page<AuditLog> getResourceAuditLogs(String resourceType, UUID resourceId, Pageable pageable) {
        return auditLogRepository.findByResourceTypeAndResourceId(resourceType, resourceId, pageable);
    }

    /**
     * Get recent audit logs
     */
    public List<AuditLog> getRecentAuditLogs(int limit) {
        return auditLogRepository.findTop100ByOrderByCreatedAtDesc()
                .stream()
                .limit(limit)
                .toList();
    }

    /**
     * Get audit logs by date range
     */
    public Page<AuditLog> getAuditLogsByDateRange(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return auditLogRepository.findByCreatedAtBetween(start, end, pageable);
    }
}