// src/main/java/com/eventra/integration/twilio/TwilioClient.java
package com.eventra.integration.twilio;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import com.eventra.config.TwilioConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class TwilioClient {

    private final TwilioConfig twilioConfig;

    /**
     * Send WhatsApp message (text only)
     */
    public void sendWhatsApp(String to, String message) {
        sendWhatsApp(to, message, null);
    }

    /**
     * Send WhatsApp message with media
     */
    public void sendWhatsApp(String to, String message, String mediaUrl) {
        try {
            Message.creator(
                            new PhoneNumber(to),
                            new PhoneNumber(twilioConfig.getWhatsappNumber()),
                            message
                    ).setMediaUrl(mediaUrl != null ? Arrays.asList(URI.create(mediaUrl)) : null)
                    .create();

            log.info("WhatsApp message sent to: {}", to);
        } catch (Exception e) {
            log.error("WhatsApp send failed to {}: {}", to, e.getMessage());
            throw new RuntimeException("WhatsApp send failed: " + e.getMessage());
        }
    }

    /**
     * Send SMS fallback
     */
    public void sendSms(String to, String message) {
        try {
            Message.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(twilioConfig.getWhatsappNumber()),
                    message
            ).create();

            log.info("SMS sent to: {}", to);
        } catch (Exception e) {
            log.error("SMS send failed to {}: {}", to, e.getMessage());
            throw new RuntimeException("SMS send failed: " + e.getMessage());
        }
    }
}