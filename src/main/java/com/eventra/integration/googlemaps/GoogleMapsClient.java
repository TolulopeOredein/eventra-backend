// src/main/java/com/eventra/integration/googlemaps/GoogleMapsClient.java
package com.eventra.integration.googlemaps;

import com.google.maps.DistanceMatrixApi;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.DistanceMatrixElement;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.TravelMode;
import com.eventra.config.GoogleMapsConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleMapsClient {

    private final GeoApiContext geoApiContext;

    public DistanceMatrixElement getDistanceAndDuration(String origin, String destination) {
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
        try {
            return GeocodingApi.geocode(geoApiContext, address).await();
        } catch (Exception e) {
            log.error("Google Maps geocoding failed: {}", e.getMessage());
            return new GeocodingResult[0];
        }
    }
}