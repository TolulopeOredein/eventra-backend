// src/main/java/com/eventra/integration/resend/ResendClient.java
package com.eventra.integration.resend;

import com.eventra.config.ResendConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResendClient {

    private final RestTemplate restTemplate;
    private final ResendConfig resendConfig;

    public void sendEmail(String to, String subject, String text, String html) {
        if (!resendConfig.isConfigured()) {
            log.warn("Resend API key not configured. Email not sent to: {}", to);
            return;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + resendConfig.getApiKey());

            java.util.Map<String, Object> request = new java.util.HashMap<>();
            request.put("from", resendConfig.getFromName() + " <" + resendConfig.getFromEmail() + ">");
            request.put("to", to);
            request.put("subject", subject);
            request.put("text", text);
            request.put("html", html);

            HttpEntity<java.util.Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    resendConfig.getBaseUrl() + "/emails",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            log.info("Email sent to: {}", to);
        } catch (Exception e) {
            log.error("Email send failed: {}", e.getMessage());
            // Don't throw exception, just log error
        }
    }
}