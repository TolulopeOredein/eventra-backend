// src/main/java/com/eventra/service/FeeConfigurationService.java
package com.eventra.service;

import com.eventra.domain.fee.EventFeeOverride;
import com.eventra.domain.fee.FeeTemplate;
import com.eventra.domain.fee.HostFeeOverride;
import com.eventra.repository.EventFeeOverrideRepository;
import com.eventra.repository.FeeTemplateRepository;
import com.eventra.repository.HostFeeOverrideRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeeConfigurationService {

    private final FeeTemplateRepository feeTemplateRepository;
    private final HostFeeOverrideRepository hostFeeOverrideRepository;
    private final EventFeeOverrideRepository eventFeeOverrideRepository;
    private final AuditService auditService;

    private static final String DEFAULT_TEMPLATE = "Standard";

    /**
     * Get effective fee for a host (template + host override)
     */
    public EffectiveFees getEffectiveFees(UUID hostId) {
        FeeTemplate defaultTemplate = feeTemplateRepository.findByName(DEFAULT_TEMPLATE)
                .orElseThrow(() -> new RuntimeException("Default fee template not found"));

        Optional<HostFeeOverride> hostOverride = hostFeeOverrideRepository.findByHostId(hostId);

        return EffectiveFees.builder()
                .platformFeePercentage(getValue(hostOverride.map(HostFeeOverride::getPlatformFeePercentage),
                        defaultTemplate.getPlatformFeePercentage()))
                .platformFeeFixed(getValue(hostOverride.map(HostFeeOverride::getPlatformFeeFixed),
                        defaultTemplate.getPlatformFeeFixed()))
                .asobiFeePercentage(getValue(hostOverride.map(HostFeeOverride::getAsobiFeePercentage),
                        defaultTemplate.getAsobiFeePercentage()))
                .vendorEscrowFeePercentage(getValue(hostOverride.map(HostFeeOverride::getVendorEscrowFeePercentage),
                        defaultTemplate.getVendorEscrowFeePercentage()))
                .ticketFeePercentage(getValue(hostOverride.map(HostFeeOverride::getTicketFeePercentage),
                        defaultTemplate.getTicketFeePercentage()))
                .virtualSprayFeePercentage(getValue(hostOverride.map(HostFeeOverride::getVirtualSprayFeePercentage),
                        defaultTemplate.getVirtualSprayFeePercentage()))
                .fxMarkupPercentage(getValue(hostOverride.map(HostFeeOverride::getFxMarkupPercentage),
                        defaultTemplate.getFxMarkupPercentage()))
                .build();
    }

    /**
     * Get effective fee for an event (template + host override + event override)
     */
    public EffectiveFees getEffectiveFeesForEvent(UUID eventId, UUID hostId) {
        EffectiveFees hostFees = getEffectiveFees(hostId);

        Optional<EventFeeOverride> eventOverride = eventFeeOverrideRepository.findByEventId(eventId);

        if (eventOverride.isPresent()) {
            EventFeeOverride override = eventOverride.get();
            return EffectiveFees.builder()
                    .platformFeePercentage(override.getPlatformFeePercentage() != null ?
                            override.getPlatformFeePercentage() : hostFees.getPlatformFeePercentage())
                    .platformFeeFixed(override.getPlatformFeeFixed() != null ?
                            override.getPlatformFeeFixed() : hostFees.getPlatformFeeFixed())
                    .asobiFeePercentage(hostFees.getAsobiFeePercentage())
                    .vendorEscrowFeePercentage(hostFees.getVendorEscrowFeePercentage())
                    .ticketFeePercentage(hostFees.getTicketFeePercentage())
                    .virtualSprayFeePercentage(hostFees.getVirtualSprayFeePercentage())
                    .fxMarkupPercentage(hostFees.getFxMarkupPercentage())
                    .build();
        }

        return hostFees;
    }

    /**
     * Calculate fee for a transaction
     */
    public BigDecimal calculateFee(BigDecimal amount, String transactionType, UUID eventId, UUID hostId) {
        EffectiveFees fees = getEffectiveFeesForEvent(eventId, hostId);

        BigDecimal percentage = switch (transactionType) {
            case "ASOBI" -> fees.getAsobiFeePercentage();
            case "VENDOR_ESCROW" -> fees.getVendorEscrowFeePercentage();
            case "TICKET" -> fees.getTicketFeePercentage();
            case "VIRTUAL_SPRAY" -> fees.getVirtualSprayFeePercentage();
            default -> fees.getPlatformFeePercentage();
        };

        BigDecimal fee = amount.multiply(percentage.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));

        // Add fixed fee if applicable
        if ("PLATFORM".equals(transactionType) && fees.getPlatformFeeFixed() != null) {
            fee = fee.add(fees.getPlatformFeeFixed());
        }

        // Apply min/max limits
        if (fees.getMinTransactionFee() != null && fee.compareTo(fees.getMinTransactionFee()) < 0) {
            fee = fees.getMinTransactionFee();
        }
        if (fees.getMaxTransactionFee() != null && fee.compareTo(fees.getMaxTransactionFee()) > 0) {
            fee = fees.getMaxTransactionFee();
        }

        return fee.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Update fee template (admin only)
     */
    @Transactional
    public FeeTemplate updateFeeTemplate(UUID templateId, FeeTemplate updatedTemplate, UUID adminId) {
        FeeTemplate template = feeTemplateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Fee template not found"));

        template.setPlatformFeePercentage(updatedTemplate.getPlatformFeePercentage());
        template.setPlatformFeeFixed(updatedTemplate.getPlatformFeeFixed());
        template.setAsobiFeePercentage(updatedTemplate.getAsobiFeePercentage());
        template.setVendorEscrowFeePercentage(updatedTemplate.getVendorEscrowFeePercentage());
        template.setTicketFeePercentage(updatedTemplate.getTicketFeePercentage());
        template.setVirtualSprayFeePercentage(updatedTemplate.getVirtualSprayFeePercentage());
        template.setFxMarkupPercentage(updatedTemplate.getFxMarkupPercentage());
        template.setMonthlySubscriptionFee(updatedTemplate.getMonthlySubscriptionFee());
        template.setAnnualSubscriptionDiscount(updatedTemplate.getAnnualSubscriptionDiscount());

        template = feeTemplateRepository.save(template);

        Map<String, Object> details = new HashMap<>();
        details.put("template_name", template.getName());
        details.put("updated_by", adminId);
        auditService.log("FEE_TEMPLATE_UPDATED", "FEE_TEMPLATE", templateId, details);

        log.info("Fee template updated: {}", template.getName());
        return template;
    }

    /**
     * Create new fee template
     */
    @Transactional
    public FeeTemplate createFeeTemplate(FeeTemplate template, UUID adminId) {
        template = feeTemplateRepository.save(template);

        Map<String, Object> details = new HashMap<>();
        details.put("template_name", template.getName());
        details.put("created_by", adminId);
        auditService.log("FEE_TEMPLATE_CREATED", "FEE_TEMPLATE", template.getId(), details);

        log.info("Fee template created: {}", template.getName());
        return template;
    }

    /**
     * Set host-specific fee override
     */
    @Transactional
    public HostFeeOverride setHostFeeOverride(UUID hostId, HostFeeOverride override, UUID adminId) {
        Optional<HostFeeOverride> existing = hostFeeOverrideRepository.findByHostId(hostId);

        HostFeeOverride result;
        if (existing.isPresent()) {
            result = existing.get();
            result.setPlatformFeePercentage(override.getPlatformFeePercentage());
            result.setPlatformFeeFixed(override.getPlatformFeeFixed());
            result.setAsobiFeePercentage(override.getAsobiFeePercentage());
            result.setVendorEscrowFeePercentage(override.getVendorEscrowFeePercentage());
            result.setTicketFeePercentage(override.getTicketFeePercentage());
            result.setVirtualSprayFeePercentage(override.getVirtualSprayFeePercentage());
            result.setFxMarkupPercentage(override.getFxMarkupPercentage());
            result.setNotes(override.getNotes());
            result.setConfiguredBy(adminId);
        } else {
            override.setConfiguredBy(adminId);
            result = hostFeeOverrideRepository.save(override);
        }

        Map<String, Object> details = new HashMap<>();
        details.put("host_id", hostId);
        details.put("configured_by", adminId);
        auditService.log("HOST_FEE_OVERRIDE", "HOST", hostId, details);

        log.info("Host fee override set for: {}", hostId);
        return result;
    }

    /**
     * Set event-specific fee override (promotional)
     */
    @Transactional
    public EventFeeOverride setEventFeeOverride(UUID eventId, EventFeeOverride override, UUID adminId) {
        Optional<EventFeeOverride> existing = eventFeeOverrideRepository.findByEventId(eventId);

        EventFeeOverride result;
        if (existing.isPresent()) {
            result = existing.get();
            result.setPlatformFeePercentage(override.getPlatformFeePercentage());
            result.setPlatformFeeFixed(override.getPlatformFeeFixed());
            result.setPromotional(override.isPromotional());
            result.setPromotionalEndDate(override.getPromotionalEndDate());
            result.setPromotionalNote(override.getPromotionalNote());
            result.setApprovedBy(adminId);
            result.setApprovedAt(LocalDateTime.now());
        } else {
            override.setApprovedBy(adminId);
            override.setApprovedAt(LocalDateTime.now());
            result = eventFeeOverrideRepository.save(override);
        }

        Map<String, Object> details = new HashMap<>();
        details.put("event_id", eventId);
        details.put("promotional", result.isPromotional());
        auditService.log("EVENT_FEE_OVERRIDE", "EVENT", eventId, details);

        log.info("Event fee override set for: {}", eventId);
        return result;
    }

    /**
     * Remove host fee override (revert to template)
     */
    @Transactional
    public void removeHostFeeOverride(UUID hostId, UUID adminId) {
        hostFeeOverrideRepository.findByHostId(hostId).ifPresent(override -> {
            hostFeeOverrideRepository.delete(override);
            auditService.log("HOST_FEE_OVERRIDE_REMOVED", "HOST", hostId,
                    Map.of("removed_by", adminId));
            log.info("Host fee override removed for: {}", hostId);
        });
    }

    private BigDecimal getValue(Optional<BigDecimal> override, BigDecimal defaultValue) {
        return override.orElse(defaultValue);
    }

    // Add this method to FeeConfigurationService.java

    /**
     * Get host fee override (returns Optional)
     */
    public Optional<HostFeeOverride> getHostFeeOverride(UUID hostId) {
        return hostFeeOverrideRepository.findByHostId(hostId);
    }

    @lombok.Builder
    @lombok.Data
    public static class EffectiveFees {
        private BigDecimal platformFeePercentage;
        private BigDecimal platformFeeFixed;
        private BigDecimal asobiFeePercentage;
        private BigDecimal vendorEscrowFeePercentage;
        private BigDecimal ticketFeePercentage;
        private BigDecimal virtualSprayFeePercentage;
        private BigDecimal fxMarkupPercentage;
        private BigDecimal minTransactionFee;
        private BigDecimal maxTransactionFee;
    }
}