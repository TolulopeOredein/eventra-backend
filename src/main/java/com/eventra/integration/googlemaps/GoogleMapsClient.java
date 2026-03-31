// src/main/java/com/eventra/integration/googlemaps/GoogleMapsClient.java
package com.eventra.integration.googlemaps;

import com.google.maps.GeoApiContext;
import com.google.maps.model.DistanceMatrixElement;
import com.google.maps.model.GeocodingResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnBean(GeoApiContext.class)
@RequiredArgsConstructor
public class GoogleMapsClient {

    private final GeoApiContext geoApiContext;

    public DistanceMatrixElement getDistanceAndDuration(String origin, String destination) {
        if (geoApiContext == null) {
            log.warn("Google Maps not configured. Skipping distance calculation.");
            return null;
        }

        try {
            // Your existing implementation
            return null; // Placeholder
        } catch (Exception e) {
            log.error("Google Maps distance matrix failed: {}", e.getMessage());
            return null;
        }
    }

    public GeocodingResult[] geocodeAddress(String address) {
        if (geoApiContext == null) {
            log.warn("Google Maps not configured. Skipping geocoding.");
            return new GeocodingResult[0];
        }

        try {
            // Your existing implementation
            return new GeocodingResult[0];
        } catch (Exception e) {
            log.error("Google Maps geocoding failed: {}", e.getMessage());
            return new GeocodingResult[0];
        }
    }
}