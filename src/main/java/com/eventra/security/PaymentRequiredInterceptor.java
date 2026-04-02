// src/main/java/com/eventra/security/PaymentRequiredInterceptor.java
package com.eventra.security;

import com.eventra.service.PricingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentRequiredInterceptor implements HandlerInterceptor {

    private final PricingService pricingService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Check if the endpoint requires payment
        if (requiresPayment(request)) {
            String userId = getUserId(request);
            int guestCount = getGuestCount(request);

            if (!pricingService.canCreateEvent(UUID.fromString(userId))) {
                response.setStatus(402); // Payment Required
                response.getWriter().write("{\"error\": \"Upgrade required\", \"message\": \"You've reached your free tier limit. Upgrade to Pro to create more events.\"}");
                return false;
            }

            int allowedGuests = pricingService.getAllowedGuestCount(UUID.fromString(userId));
            if (guestCount > allowedGuests) {
                response.setStatus(402);
                response.getWriter().write("{\"error\": \"Guest limit exceeded\", \"message\": \"Your plan allows up to " + allowedGuests + " guests. Upgrade to add more.\"}");
                return false;
            }
        }

        return true;
    }

    private boolean requiresPayment(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.matches("/api/events.*") && "POST".equals(request.getMethod());
    }

    private String getUserId(HttpServletRequest request) {
        // Extract user ID from JWT
        return (String) request.getAttribute("userId");
    }

    private int getGuestCount(HttpServletRequest request) {
        // Extract guest count from request body
        // Simplified for example
        return 0;
    }
}