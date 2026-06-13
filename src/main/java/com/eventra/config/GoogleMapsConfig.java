// src/main/java/com/eventra/config/GoogleMapsConfig.java
package com.eventra.config;

import com.google.maps.GeoApiContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogleMapsConfig {

    @Value("${google.maps.api-key:}")
    private String apiKey;

    @Bean
    public GeoApiContext geoApiContext() {
        if (apiKey == null || apiKey.isEmpty()) {
            // Return a mock context when no API key is provided
            return null;
        }

        return new GeoApiContext.Builder()
                .apiKey(apiKey)
                .build();
    }
}