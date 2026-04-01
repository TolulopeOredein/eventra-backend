// src/main/java/com/eventra/integration/r2/R2StorageService.java
package com.eventra.integration.r2;

import com.eventra.config.R2Config;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class R2StorageService {

    private final S3Client r2Client;
    private final R2Config r2Config;

    /**
     * Upload bytes to R2
     */
    public String uploadBytes(byte[] data, String key, String contentType) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(r2Config.getBucketName())
                    .key(key)
                    .contentType(contentType)
                    .build();

            r2Client.putObject(request, RequestBody.fromBytes(data));
            log.info("Uploaded to R2: key={}", key);

            return getPublicUrl(key);
        } catch (Exception e) {
            log.error("R2 upload failed: {}", e.getMessage());
            throw new RuntimeException("Storage upload failed: " + e.getMessage());
        }
    }

    /**
     * Upload MultipartFile to R2
     */
    public String uploadFile(MultipartFile file, String key) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(r2Config.getBucketName())
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            r2Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
            log.info("File uploaded to R2: key={}, size={}", key, file.getSize());

            return getPublicUrl(key);
        } catch (IOException e) {
            log.error("File upload failed", e);
            throw new RuntimeException("File upload failed: " + e.getMessage());
        }
    }

    /**
     * Get public URL (for QR codes - not sensitive)
     */
    public String getPublicUrl(String key) {
        // Format: https://{bucket}.{account}.r2.cloudflarestorage.com/{key}
        String endpoint = r2Config.getEndpoint().replace("https://", "");
        return String.format("https://%s.%s/%s",
                r2Config.getBucketName(),
                endpoint,
                key);
    }

    /**
     * Delete file from R2
     */
    public void deleteFile(String key) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(r2Config.getBucketName())
                    .key(key)
                    .build();

            r2Client.deleteObject(deleteRequest);
            log.info("File deleted from R2: key={}", key);
        } catch (Exception e) {
            log.error("File deletion failed for key: {}", key, e);
            throw new RuntimeException("File deletion failed: " + e.getMessage());
        }
    }

    /**
     * Upload QR code
     */
    public String uploadQrCode(UUID eventId, UUID guestId, byte[] qrImage) {
        String key = String.format("events/%s/qr/%s.png", eventId, guestId);
        return uploadBytes(qrImage, key, "image/png");
    }

    /**
     * Upload event photo
     */
    public String uploadEventPhoto(UUID eventId, MultipartFile photo, String photoType) {
        String key = String.format("events/%s/photos/%s/%s_%s",
                eventId,
                photoType,
                UUID.randomUUID(),
                photo.getOriginalFilename());
        return uploadFile(photo, key);
    }
}