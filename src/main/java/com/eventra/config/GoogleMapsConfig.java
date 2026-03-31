// src/main/java/com/eventra/config/GoogleMapsConfig.java
package com.eventra.config;

import com.google.maps.GeoApiContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "google.maps")
public class GoogleMapsConfig {

    private String apiKey;
    private int connectTimeout = 10000;
    private int readTimeout = 10000;
    private int maxRetries = 3;

    @Bean
    @ConditionalOnProperty(name = "google.maps.api-key", havingValue = "true", matchIfMissing = false)
    public GeoApiContext geoApiContext() {
        if (!StringUtils.hasText(apiKey)) {
            log.warn("Google Maps API key not configured. Maps features will be disabled.");
            return null;
        }

        return new GeoApiContext.Builder()
                .apiKey(apiKey)
                .connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
                .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
                .maxRetries(maxRetries)
                .build();
    }
}