// src/main/java/com/eventra/integration/twilio/TwilioClient.java
package com.eventra.integration.twilio;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
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
        try {
            Message.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(twilioConfig.getWhatsappNumber()),
                    message
            ).create();
            log.info("WhatsApp message sent to: {}", to);
        } catch (Exception e) {
            log.error("WhatsApp send failed: {}", e.getMessage());
            throw new RuntimeException("WhatsApp send failed: " + e.getMessage());
        }
    }

    public void sendSms(String to, String message) {
        try {
            Message.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(twilioConfig.getWhatsappNumber()),
                    message
            ).create();
            log.info("SMS sent to: {}", to);
        } catch (Exception e) {
            log.error("SMS send failed: {}", e.getMessage());
            throw new RuntimeException("SMS send failed: " + e.getMessage());
        }
    }
}