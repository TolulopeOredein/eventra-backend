// src/main/java/com/eventra/service/QrService.java
package com.eventra.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.eventra.domain.entry.QrCode;
import com.eventra.repository.QrCodeRepository;
import com.eventra.domain.event.Event;
import com.eventra.domain.guest.Guest;
import com.eventra.integration.r2.R2StorageService;
import com.eventra.repository.QrCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class QrService {

    private static final String SECRET_KEY = "EventraSecretKey2024";
    private static final int QR_SIZE = 300;

    private final QrCodeRepository qrCodeRepository;
    private final R2StorageService r2StorageService;
    private final ObjectMapper objectMapper;

    public String generateQrForGuest(Guest guest, Event event) {
        try {
            // Create payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("guestId", guest.getId().toString());
            payload.put("eventId", event.getId().toString());
            payload.put("guestName", guest.getName());
            payload.put("tier", guest.getTier());
            payload.put("issuedAt", System.currentTimeMillis());
            payload.put("expiresAt", event.getEventDate().minusHours(2).toEpochSecond(java.time.ZoneOffset.UTC));

            // Encrypt payload
            String json = objectMapper.writeValueAsString(payload);
            String encrypted = encrypt(json);

            // Generate QR code image
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(encrypted, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            byte[] qrImage = outputStream.toByteArray();

            // Upload to R2
            String key = String.format("events/%s/qr/%s.png", event.getId(), guest.getId());
            String qrUrl = r2StorageService.uploadBytes(qrImage, key, "image/png");

            // Store QR code record
            String codeHash = hashPayload(encrypted);

            QrCode qrCode = QrCode.builder()
                    .guestId(guest.getId())
                    .eventId(event.getId())
                    .codeHash(codeHash)
                    .encryptedPayload(encrypted)
                    .expiresAt(event.getEventDate().minusHours(2))
                    .build();

            qrCodeRepository.save(qrCode);

            return qrUrl;

        } catch (Exception e) {
            log.error("QR generation failed", e);
            throw new RuntimeException("QR generation failed: " + e.getMessage());
        }
    }

    public Map<String, Object> verifyQr(String qrData, UUID eventId) {
        try {
            String decrypted = decrypt(qrData);
            Map<String, Object> payload = objectMapper.readValue(decrypted, Map.class);

            // Verify event matches
            if (!payload.get("eventId").toString().equals(eventId.toString())) {
                throw new RuntimeException("QR code is for a different event");
            }

            // Check expiry
            long expiresAt = ((Number) payload.get("expiresAt")).longValue();
            if (System.currentTimeMillis() / 1000 > expiresAt) {
                throw new RuntimeException("QR code has expired");
            }

            // Check if already used
            UUID guestId = UUID.fromString(payload.get("guestId").toString());
            String codeHash = hashPayload(qrData);

            QrCode qrCode = qrCodeRepository.findByCodeHash(codeHash)
                    .orElseThrow(() -> new RuntimeException("QR code not found"));

            if (qrCode.isRevoked()) {
                throw new RuntimeException("QR code has been revoked");
            }

            if (qrCode.getUsedAt() != null) {
                throw new RuntimeException("QR code has already been used");
            }

            // Mark as used
            qrCode.setUsedAt(LocalDateTime.now());
            qrCodeRepository.save(qrCode);

            return payload;

        } catch (Exception e) {
            log.error("QR verification failed", e);
            throw new RuntimeException("Invalid QR code: " + e.getMessage());
        }
    }

    private String encrypt(String data) throws Exception {
        SecretKey key = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    private String decrypt(String encryptedData) throws Exception {
        SecretKey key = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decoded = Base64.getDecoder().decode(encryptedData);
        byte[] decrypted = cipher.doFinal(decoded);
        return new String(decrypted);
    }

    private String hashPayload(String payload) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(payload.getBytes());
        return Base64.getEncoder().encodeToString(hash);
    }
}