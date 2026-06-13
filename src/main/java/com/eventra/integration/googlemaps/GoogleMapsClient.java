// src/main/java/com/eventra/integration/googlemaps/GoogleMapsClient.java
package com.eventra.integration.googlemaps;

import com.google.maps.DistanceMatrixApi;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.DistanceMatrixElement;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.TravelMode;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GoogleMapsClient {

    @Autowired(required = false)
    private GeoApiContext geoApiContext;

    private boolean isConfigured;

    @PostConstruct
    public void init() {
        this.isConfigured = geoApiContext != null;
        if (isConfigured) {
            log.info("GoogleMapsClient initialized with API key");
        } else {
            log.warn("GoogleMapsClient running in MOCK mode. No API key configured. Map operations will return mock data.");
        }
    }

    public DistanceMatrixElement getDistanceAndDuration(String origin, String destination) {
        if (!isConfigured) {
            log.warn("MOCK MODE: Would calculate distance from '{}' to '{}'", origin, destination);
            return null;
        }

        try {
            DistanceMatrix result = DistanceMatrixApi.newRequest(geoApiContext)
                    .origins(origin)
                    .destinations(destination)
                    .mode(TravelMode.DRIVING)
                    .await();

            if (result.rows.length > 0 && result.rows[0].elements.length > 0) {
                return result.rows[0].elements[0];
            }
            return null;
        } catch (Exception e) {
            log.error("Google Maps distance matrix failed: {}", e.getMessage());
            return null;
        }
    }

    public GeocodingResult[] geocodeAddress(String address) {
        if (!isConfigured) {
            log.warn("MOCK MODE: Would geocode address: '{}'", address);
            return new GeocodingResult[0];
        }

        try {
            return GeocodingApi.geocode(geoApiContext, address).await();
        } catch (Exception e) {
            log.error("Google Maps geocoding failed: {}", e.getMessage());
            return new GeocodingResult[0];
        }
    }

    public boolean isConfigured() {
        return isConfigured;
    }
}