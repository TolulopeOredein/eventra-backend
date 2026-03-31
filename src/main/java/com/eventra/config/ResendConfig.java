// src/main/java/com/eventra/config/ResendConfig.java
package com.eventra.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "resend")
public class ResendConfig {
    private String apiKey;
    private String fromEmail = "noreply@eventra.com";
    private String fromName = "Eventra";
    private String baseUrl = "https://api.resend.com";

    public boolean isConfigured() {
        return StringUtils.hasText(apiKey);
    }
}