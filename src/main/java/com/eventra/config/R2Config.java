// src/main/java/com/eventra/config/R2Config.java
package com.eventra.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class R2Config {

    @Value("${r2.endpoint:}")
    private String endpoint;

    @Value("${r2.access-key:}")
    private String accessKey;

    @Value("${r2.secret-key:}")
    private String secretKey;

    @Value("${r2.enabled:false}")
    private boolean r2Enabled;

    @Bean
    public S3Client r2Client() {
        if (!r2Enabled) {
            // Return a mock/no-op client instead of null
            return null;
        }

        if (endpoint == null || endpoint.isEmpty() ||
                accessKey == null || accessKey.isEmpty() ||
                secretKey == null || secretKey.isEmpty()) {
            return null;
        }

        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.EU_WEST_1)
                .build();
    }
}