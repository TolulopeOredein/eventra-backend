// src/main/java/com/eventra/service/PricingService.java
package com.eventra.service;

import com.eventra.domain.user.User;
import com.eventra.repository.EventRepository;
import com.eventra.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PricingService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    // Pricing constants
    private static final BigDecimal PRO_EVENT_PRICE = new BigDecimal("25000");
    private static final BigDecimal PREMIUM_EVENT_PRICE = new BigDecimal("100000");
    private static final BigDecimal PRO_MONTHLY_PRICE = new BigDecimal("50000");
    private static final BigDecimal PREMIUM_MONTHLY_PRICE = new BigDecimal("150000");
    private static final int FREE_TIER_GUEST_LIMIT = 50;
    private static final int PRO_TIER_GUEST_LIMIT = 500;

    /**
     * Check if user can create an event based on their plan
     */
    public boolean canCreateEvent(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow();

        // Check free events remaining
        if (user.getFreeEventsRemaining() > 0) {
            return true;
        }

        // Check subscription tier
        String tier = user.getSubscriptionTier();
        if ("pro".equals(tier) || "premium".equals(tier)) {
            return true;
        }

        return false;
    }

    /**
     * Get allowed guest count for user's plan
     */
    public int getAllowedGuestCount(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow();
        String tier = user.getSubscriptionTier();

        if ("free".equals(tier)) {
            return FREE_TIER_GUEST_LIMIT;
        } else if ("pro".equals(tier)) {
            return PRO_TIER_GUEST_LIMIT;
        } else if ("premium".equals(tier)) {
            return Integer.MAX_VALUE;
        }

        return FREE_TIER_GUEST_LIMIT;
    }

    /**
     * Calculate cost for creating an event
     */
    public PricingQuote calculateEventCost(UUID userId, int guestCount, boolean includeTrafficAI,
                                           boolean includePremiumReminders, boolean includeAnalytics) {
        User user = userRepository.findById(userId).orElseThrow();

        PricingQuote quote = PricingQuote.builder()
                .basePrice(BigDecimal.ZERO)
                .addOnsTotal(BigDecimal.ZERO)
                .total(BigDecimal.ZERO)
                .build();

        // Free events remaining
        if (user.getFreeEventsRemaining() > 0) {
            quote.setBasePrice(BigDecimal.ZERO);
            quote.setMessage("This is one of your " + user.getFreeEventsRemaining() + " free events!");
            return quote;
        }

        String tier = user.getSubscriptionTier();

        // Determine base price based on plan and guest count
        if ("free".equals(tier) && guestCount > FREE_TIER_GUEST_LIMIT) {
            quote.setBasePrice(PRO_EVENT_PRICE);
        } else if ("pro".equals(tier) && guestCount > PRO_TIER_GUEST_LIMIT) {
            quote.setBasePrice(PREMIUM_EVENT_PRICE);
        }

        // Add add-ons
        if (includeTrafficAI) {
            quote.addAddOn("Traffic AI", new BigDecimal("5000"));
        }
        if (includePremiumReminders) {
            quote.addAddOn("Premium Reminders", new BigDecimal("3000"));
        }
        if (includeAnalytics) {
            quote.addAddOn("Advanced Analytics", new BigDecimal("10000"));
        }

        quote.setTotal(quote.getBasePrice().add(quote.getAddOnsTotal()));

        return quote;
    }

    /**
     * Calculate subscription upgrade cost
     */
    public UpgradeQuote calculateUpgradeCost(String fromTier, String toTier) {
        UpgradeQuote quote = UpgradeQuote.builder()
                .fromTier(fromTier)
                .toTier(toTier)
                .build();

        if ("free".equals(fromTier) && "pro".equals(toTier)) {
            quote.setOneTimeFee(PRO_EVENT_PRICE);
            quote.setMonthlyFee(PRO_MONTHLY_PRICE);
            quote.setBenefits(List.of(
                    "Unlimited events",
                    "Up to 500 guests per event",
                    "CSV guest import",
                    "QR code check-in",
                    "Remove Eventra branding"
            ));
        } else if ("free".equals(fromTier) && "premium".equals(toTier)) {
            quote.setOneTimeFee(PREMIUM_EVENT_PRICE);
            quote.setMonthlyFee(PREMIUM_MONTHLY_PRICE);
            quote.setBenefits(List.of(
                    "Unlimited events",
                    "Unlimited guests",
                    "AI traffic predictions",
                    "Advanced analytics",
                    "Priority check-in",
                    "Dedicated account manager"
            ));
        } else if ("pro".equals(fromTier) && "premium".equals(toTier)) {
            quote.setOneTimeFee(PREMIUM_EVENT_PRICE.subtract(PRO_EVENT_PRICE));
            quote.setMonthlyFee(PREMIUM_MONTHLY_PRICE.subtract(PRO_MONTHLY_PRICE));
            quote.setBenefits(List.of(
                    "Unlimited guests",
                    "AI traffic predictions",
                    "Advanced analytics",
                    "Priority check-in",
                    "Dedicated account manager"
            ));
        }

        return quote;
    }

    /**
     * Use a free event (decrement counter)
     */
    public void useFreeEvent(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow();
        if (user.getFreeEventsRemaining() > 0) {
            user.setFreeEventsRemaining(user.getFreeEventsRemaining() - 1);
            user.setTotalEventsCreated(user.getTotalEventsCreated() + 1);
            userRepository.save(user);
        }
    }

    /**
     * Get remaining free events for user
     */
    public int getRemainingFreeEvents(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return user.getFreeEventsRemaining();
    }

    @lombok.Builder
    @lombok.Data
    public static class PricingQuote {
        private BigDecimal basePrice;
        private BigDecimal addOnsTotal;
        private BigDecimal total;
        private String message;
        private Map<String, BigDecimal> addOns = new HashMap<>();

        public void addAddOn(String name, BigDecimal price) {
            addOns.put(name, price);
            addOnsTotal = addOnsTotal.add(price);
        }
    }

    @lombok.Builder
    @lombok.Data
    public static class UpgradeQuote {
        private String fromTier;
        private String toTier;
        private BigDecimal oneTimeFee;
        private BigDecimal monthlyFee;
        private List<String> benefits;
    }
}