// src/main/java/com/eventra/integration/r2/R2StorageService.java
package com.eventra.integration.r2;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.Optional;

@Slf4j
@Service
public class R2StorageService {

    @Autowired(required = false)
    private S3Client s3Client;

    @Value("${r2.bucket-name:eventra-storage}")
    private String bucketName;

    @Value("${r2.public-url:https://eventra-storage.r2.cloudflarestorage.com}")
    private String publicUrl;

    @Value("${r2.enabled:false}")
    private boolean r2Enabled;

    private boolean isConfigured;

    @PostConstruct
    public void init() {
        this.isConfigured = r2Enabled && s3Client != null;
        if (isConfigured) {
            log.info("R2StorageService initialized with bucket: {}", bucketName);
        } else {
            log.warn("R2StorageService running in MOCK mode. S3Client not configured or R2 disabled. File operations will be simulated.");
        }
    }

    public String uploadFile(byte[] file, String fileName, String contentType) {
        if (!isConfigured) {
            String mockUrl = publicUrl + "/" + fileName;
            log.info("MOCK MODE: File would be uploaded to: {}", mockUrl);
            return mockUrl;
        }

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file));

            String fileUrl = publicUrl + "/" + fileName;
            log.info("File uploaded successfully: {}", fileUrl);
            return fileUrl;
        } catch (S3Exception e) {
            log.error("Failed to upload file {}: {}", fileName, e.getMessage());
            throw new RuntimeException("File upload failed: " + e.getMessage(), e);
        }
    }

    public String uploadFile(byte[] file, String fileName, String contentType, String directory) {
        String fullPath = directory + "/" + fileName;
        return uploadFile(file, fullPath, contentType);
    }

    public byte[] downloadFile(String fileUrl) {
        if (!isConfigured) {
            log.info("MOCK MODE: Would download file from: {}", fileUrl);
            return new byte[0];
        }

        try {
            String key = extractKeyFromUrl(fileUrl);

            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            return s3Client.getObjectAsBytes(request).asByteArray();
        } catch (S3Exception e) {
            log.error("Failed to download file from {}: {}", fileUrl, e.getMessage());
            throw new RuntimeException("File download failed: " + e.getMessage(), e);
        }
    }

    public void deleteFile(String fileUrl) {
        if (!isConfigured) {
            log.info("MOCK MODE: Would delete file from: {}", fileUrl);
            return;
        }

        try {
            String key = extractKeyFromUrl(fileUrl);

            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(request);
            log.info("File deleted successfully: {}", key);
        } catch (S3Exception e) {
            log.error("Failed to delete file from {}: {}", fileUrl, e.getMessage());
            throw new RuntimeException("File deletion failed: " + e.getMessage(), e);
        }
    }

    public boolean fileExists(String fileUrl) {
        if (!isConfigured) {
            log.info("MOCK MODE: Would check existence of: {}", fileUrl);
            return true;
        }

        try {
            String key = extractKeyFromUrl(fileUrl);

            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.headObject(request);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            log.error("Failed to check file existence: {}", e.getMessage());
            return false;
        }
    }

    public boolean isConfigured() {
        return isConfigured;
    }

    private String extractKeyFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            throw new IllegalArgumentException("File URL cannot be null or empty");
        }

        String baseUrl = publicUrl;
        if (fileUrl.startsWith(baseUrl)) {
            return fileUrl.substring(baseUrl.length() + 1);
        }

        return fileUrl;
    }
}