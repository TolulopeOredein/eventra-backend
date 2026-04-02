// src/main/java/com/eventra/service/TrafficService.java
package com.eventra.service;

import com.google.maps.model.DistanceMatrixElement;
import com.eventra.domain.event.Event;
import com.eventra.domain.guest.Guest;
import com.eventra.integration.googlemaps.GoogleMapsClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrafficService {

    private final GoogleMapsClient googleMapsClient;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a");

    /**
     * Get traffic advisory for a single guest
     */
    @Cacheable(value = "traffic", key = "#guest.phone + #event.id")
    public TrafficAdvisory getTrafficAdvisory(Guest guest, Event event) {
        try {
            String origin = getGuestLocation(guest);
            String destination = event.getVenueAddress();

            if (origin == null || destination == null) {
                return getFallbackAdvisory(guest, event);
            }

            DistanceMatrixElement result = googleMapsClient.getDistanceAndDuration(origin, destination);

            if (result == null || result.duration == null) {
                return getFallbackAdvisory(guest, event);
            }

            long travelTimeMinutes = result.duration.inSeconds / 60;
            long travelTimeWithTrafficMinutes = result.durationInTraffic != null ?
                    result.durationInTraffic.inSeconds / 60 : travelTimeMinutes;

            LocalDateTime recommendedDeparture = event.getEventDate()
                    .minusMinutes(travelTimeWithTrafficMinutes + 30);

            String trafficLevel = getTrafficLevel(result);

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
            log.error("Traffic advisory failed for guest: {}", guest.getPhone(), e);
            return getFallbackAdvisory(guest, event);
        }
    }

    /**
     * Get bulk traffic advisory for multiple guests
     */
    public Map<UUID, TrafficAdvisory> getBulkTrafficAdvisory(List<Guest> guests, Event event) {
        Map<UUID, TrafficAdvisory> advisories = new HashMap<>();

        for (Guest guest : guests) {
            try {
                advisories.put(guest.getId(), getTrafficAdvisory(guest, event));
            } catch (Exception e) {
                log.error("Bulk traffic advisory failed for guest: {}", guest.getId(), e);
                advisories.put(guest.getId(), getFallbackAdvisory(guest, event));
            }
        }

        return advisories;
    }

    /**
     * Get guest location (to be enhanced with user address book)
     */
    private String getGuestLocation(Guest guest) {
        // In production, this would come from:
        // 1. User profile saved address
        // 2. Guest's home address from invitation
        // 3. Default to city center if unknown
        return null;
    }

    /**
     * Determine traffic level based on delay ratio
     */
    private String getTrafficLevel(DistanceMatrixElement result) {
        if (result.durationInTraffic == null) return "UNKNOWN";

        double ratio = (double) result.durationInTraffic.inSeconds / result.duration.inSeconds;
        if (ratio > 1.5) return "HEAVY";
        if (ratio > 1.2) return "MODERATE";
        return "LIGHT";
    }

    /**
     * Fallback advisory when API is unavailable
     */
    private TrafficAdvisory getFallbackAdvisory(Guest guest, Event event) {
        long estimatedTravelMinutes = 45; // Default for Lagos

        LocalDateTime recommendedDeparture = event.getEventDate()
                .minusMinutes(estimatedTravelMinutes + 30);

        return TrafficAdvisory.builder()
                .origin(getGuestLocation(guest))
                .destination(event.getVenueAddress())
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