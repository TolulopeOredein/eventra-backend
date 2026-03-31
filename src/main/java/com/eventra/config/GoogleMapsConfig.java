// src/main/java/com/eventra/config/GoogleMapsConfig.java
package com.eventra.config;

import com.google.maps.GeoApiContext;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Data
@Configuration
@ConfigurationProperties(prefix = "google.maps")
public class GoogleMapsConfig {

    private String apiKey;
    private int connectTimeout = 10000;
    private int readTimeout = 10000;
    private int maxRetries = 3;

    @Bean
    public GeoApiContext geoApiContext() {
        return new GeoApiContext.Builder()
                .apiKey(apiKey)
                .connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
                .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
                .maxRetries(maxRetries)
                .build();
    }
}