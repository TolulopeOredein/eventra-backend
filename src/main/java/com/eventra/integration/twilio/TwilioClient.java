// src/main/java/com/eventra/integration/twilio/TwilioClient.java
package com.eventra.integration.twilio;

import com.eventra.config.TwilioConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TwilioClient {

    private final TwilioConfig twilioConfig;

    public void sendWhatsApp(String to, String message) {
        if (!twilioConfig.isConfigured()) {
            log.warn("Twilio not configured. WhatsApp message not sent to: {}", to);
            return;
        }

        try {
            // Your existing implementation
            log.info("WhatsApp message would be sent to: {}", to);
        } catch (Exception e) {
            log.error("WhatsApp send failed: {}", e.getMessage());
            // Don't throw, just log
        }
    }

    public void sendSms(String to, String message) {
        if (!twilioConfig.isConfigured()) {
            log.warn("Twilio not configured. SMS not sent to: {}", to);
            return;
        }

        try {
            // Your existing implementation
            log.info("SMS would be sent to: {}", to);
        } catch (Exception e) {
            log.error("SMS send failed: {}", e.getMessage());
            // Don't throw, just log
        }
    }
}