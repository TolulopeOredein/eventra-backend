// src/main/java/com/eventra/config/ResendConfig.java
package com.eventra.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "resend")
public class ResendConfig {
    private String apiKey;
    private String fromEmail = "noreply@eventra.com";
    private String fromName = "Eventra";
    private String baseUrl = "https://api.resend.com";
}