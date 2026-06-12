package com.eventra.integration.googlemaps;

import com.google.maps.DistanceMatrixApi;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.DistanceMatrixElement;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.TravelMode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Slf4j
@Service  // ← Only one annotation: @Service
@RequiredArgsConstructor
public class GoogleMapsClient {

    @Value("${google.maps.api-key}")
    private String apiKey;

    private GeoApiContext geoApiContext;

    // Initialize the GeoApiContext
    private GeoApiContext getGeoApiContext() {
        if (geoApiContext == null) {
            geoApiContext = new GeoApiContext.Builder()
                    .apiKey(apiKey)
                    .build();
        }
        return geoApiContext;
    }

    public DistanceMatrixElement getDistanceAndDuration(String origin, String destination) {
        try {
            DistanceMatrix result = DistanceMatrixApi.newRequest(getGeoApiContext())
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
            return GeocodingApi.geocode(getGeoApiContext(), address).await();
        } catch (Exception e) {
            log.error("Google Maps geocoding failed: {}", e.getMessage());
            return new GeocodingResult[0];
        }
    }
}