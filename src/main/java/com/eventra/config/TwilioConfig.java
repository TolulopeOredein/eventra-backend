// src/main/java/com/eventra/config/TwilioConfig.java
package com.eventra.config;

import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "twilio")
public class TwilioConfig {

    private String accountSid;
    private String authToken;
    private String whatsappNumber;

    @PostConstruct
    public void init() {
        if (StringUtils.hasText(accountSid) && StringUtils.hasText(authToken)) {
            Twilio.init(accountSid, authToken);
            log.info("Twilio initialized successfully");
        } else {
            log.warn("Twilio credentials not configured. SMS/WhatsApp features will be disabled.");
        }
    }

    public boolean isConfigured() {
        return StringUtils.hasText(accountSid) && StringUtils.hasText(authToken);
    }
}