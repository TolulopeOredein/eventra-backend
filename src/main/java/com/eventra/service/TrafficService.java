// src/main/java/com/eventra/service/TrafficService.java
package com.eventra.service;

import com.eventra.domain.event.Event;
import com.eventra.domain.guest.Guest;
import com.eventra.integration.googlemaps.GoogleMapsClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class TrafficService {

    private final Optional<GoogleMapsClient> googleMapsClient;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a");

    /**
     * Constructor using @Lazy to break circular dependencies with GoogleMapsClient
     */
    public TrafficService(@Lazy Optional<GoogleMapsClient> googleMapsClient) {
        this.googleMapsClient = googleMapsClient;
    }

    /**
     * Get traffic advisory for a single guest
     */
    public TrafficAdvisory getTrafficAdvisory(Guest guest, Event event) {
        // Check if Google Maps client bean exists and is configured
        if (googleMapsClient.isEmpty()) {
            log.warn("GoogleMapsClient bean not found in context - using fallback advisory");
            return getFallbackAdvisory(guest, event);
        }

        try {
            String origin = getGuestLocation(guest);
            String destination = event.getVenueAddress();

            // Validate addresses before calling external API
            if (origin == null || origin.isBlank() || destination == null || destination.isBlank()) {
                log.debug("Missing origin or destination for guest {}. Using fallback.", guest.getId());
                return getFallbackAdvisory(guest, event);
            }

            // Call the Google Maps client
            var result = googleMapsClient.get().getDistanceAndDuration(origin, destination);

            if (result == null || result.duration == null) {
                log.warn("Google Maps API returned empty results for origin: {}", origin);
                return getFallbackAdvisory(guest, event);
            }

            long travelTimeMinutes = result.duration.inSeconds / 60;
            long travelTimeWithTrafficMinutes = (result.durationInTraffic != null) ?
                    result.durationInTraffic.inSeconds / 60 : travelTimeMinutes;

            // Buffer time of 30 minutes added to estimated traffic time
            LocalDateTime recommendedDeparture = event.getEventDate()
                    .minusMinutes(travelTimeWithTrafficMinutes + 30);

            String trafficLevel = calculateTrafficLevel(result);

            return TrafficAdvisory.builder()
                    .origin(origin)
                    .destination(destination)
                    .distanceText(result.distance != null ? result.distance.humanReadable : "Unknown")
                    .durationText(result.duration.humanReadable)
                    .durationInTrafficText(result.durationInTraffic != null ?
                            result.durationInTraffic.humanReadable : result.duration.humanReadable)
                    .trafficLevel(trafficLevel)
                    .recommendedDepartureTime(recommendedDeparture)
                    .recommendedDepartureText(recommendedDeparture.format(TIME_FORMATTER))
                    .build();

        } catch (Exception e) {
            log.error("Traffic advisory calculation failed for guest: {}", guest.getId(), e);
            return getFallbackAdvisory(guest, event);
        }
    }

    /**
     * Get bulk traffic advisory for multiple guests
     */
    public Map<UUID, TrafficAdvisory> getBulkTrafficAdvisory(java.util.List<Guest> guests, Event event) {
        Map<UUID, TrafficAdvisory> advisories = new HashMap<>();

        if (guests == null) return advisories;

        for (Guest guest : guests) {
            advisories.put(guest.getId(), getTrafficAdvisory(guest, event));
        }

        return advisories;
    }

    private String getGuestLocation(Guest guest) {
        // Placeholder: logic to extract guest address (e.g. guest.getAddress())
        // Currently returns null to trigger fallback as requested in original snippet
        return null;
    }

    private String calculateTrafficLevel(com.google.maps.model.DistanceMatrixElement result) {
        if (result.durationInTraffic == null || result.duration == null) {
            return "NORMAL";
        }

        double ratio = (double) result.durationInTraffic.inSeconds / result.duration.inSeconds;

        if (ratio > 1.5) return "HEAVY";
        if (ratio > 1.2) return "MODERATE";
        return "LIGHT";
    }

    private TrafficAdvisory getFallbackAdvisory(Guest guest, Event event) {
        long estimatedTravelMinutes = 45; // Default estimate
        LocalDateTime recommendedDeparture = event.getEventDate()
                .minusMinutes(estimatedTravelMinutes + 30);

        return TrafficAdvisory.builder()
                .origin("Unknown")
                .destination(event != null ? event.getVenueAddress() : "Unknown")
                .distanceText("~15 km")
                .durationText("45 min")
                .durationInTrafficText("45-60 min")
                .trafficLevel("MODERATE")
                .recommendedDepartureTime(recommendedDeparture)
                .recommendedDepartureText(recommendedDeparture.format(TIME_FORMATTER))
                .build();
    }

    @lombok.Builder
    @lombok.Data
    public static class TrafficAdvisory {
        private String origin;
        private String destination;
        private String distanceText;
        private String durationText;
        private String durationInTrafficText;
        private String trafficLevel;
        private LocalDateTime recommendedDepartureTime;
        private String recommendedDepartureText;
    }
}