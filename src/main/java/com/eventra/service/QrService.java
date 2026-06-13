// src/main/java/com/eventra/service/QrService.java
package com.eventra.service;

import com.eventra.integration.r2.R2StorageService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class QrService {

    private final R2StorageService r2StorageService;

    public String generateQrCodeForGuest(String guestId, String eventId) {
        try {
            String qrData = String.format("{\"guestId\":\"%s\",\"eventId\":\"%s\",\"timestamp\":%d}",
                    guestId, eventId, System.currentTimeMillis());

            byte[] qrImage = generateQrCodeImage(qrData, 300, 300);
            String fileName = String.format("qr/%s/%s.png", eventId, guestId);

            return r2StorageService.uploadFile(qrImage, fileName, "image/png");
        } catch (Exception e) {
            log.error("Failed to generate QR code: {}", e.getMessage());
            return null;
        }
    }

    public byte[] generateQrCodeImage(String text, int width, int height) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

        return outputStream.toByteArray();
    }
}