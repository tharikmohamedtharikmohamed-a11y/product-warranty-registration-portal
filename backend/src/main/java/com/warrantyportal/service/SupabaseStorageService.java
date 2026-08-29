package com.warrantyportal.service;

import com.warrantyportal.exception.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Service encapsulating direct HTTP interactions with the private Supabase Storage REST API.
 * Uses native Java HttpClient without introducing third-party SDK dependencies.
 * Phase 9 — Invoice Management
 */
@Service
public class SupabaseStorageService {

    private static final Logger logger = LoggerFactory.getLogger(SupabaseStorageService.class);

    private final String supabaseUrl;
    private final String serviceRoleKey;
    private final String bucketName;
    private final HttpClient httpClient;

    public SupabaseStorageService(
            @Value("${supabase.url:https://bfogwrprtsfvsgmbzvdp.supabase.co}") String supabaseUrl,
            @Value("${supabase.service-role-key:}") String serviceRoleKey,
            @Value("${supabase.storage.bucket:invoices}") String bucketName) {
        this.supabaseUrl = supabaseUrl != null ? supabaseUrl.replaceAll("/+$", "") : "";
        this.serviceRoleKey = serviceRoleKey != null ? serviceRoleKey.trim() : "";
        this.bucketName = bucketName != null ? bucketName.trim() : "invoices";
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    /**
     * Uploads binary file content to private Supabase Storage bucket.
     *
     * @param storagePath  the hierarchical object path (e.g. invoices/{userId}/{productId}/{uuid}_{fileName})
     * @param content      the binary file content bytes
     * @param contentType  the MIME content type (e.g. application/pdf, image/jpeg, image/png)
     */
    public void uploadFile(String storagePath, byte[] content, String contentType) {
        validateConfiguration();
        String cleanPath = stripLeadingBucketPrefix(storagePath);
        String targetUri = String.format("%s/storage/v1/object/%s/%s", supabaseUrl, bucketName, cleanPath);

        logger.info("Uploading file to Supabase Storage: bucket={}, path={}", bucketName, cleanPath);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUri))
                    .timeout(Duration.ofSeconds(30))
                    .header("Authorization", "Bearer " + serviceRoleKey)
                    .header("apikey", serviceRoleKey)
                    .header("Content-Type", contentType != null ? contentType : "application/octet-stream")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(content))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                logger.info("Successfully uploaded object: {}", cleanPath);
            } else {
                logger.error("Supabase Storage upload failed with status code {}: {}", response.statusCode(), response.body());
                throw new StorageException("Failed to upload file to storage. Server returned status: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Network error during Supabase Storage upload: {}", e.getMessage());
            throw new StorageException("Storage service communication error during upload", e);
        }
    }

    /**
     * Downloads binary file content from private Supabase Storage bucket.
     *
     * @param storagePath the hierarchical object path
     * @return the raw binary content bytes
     */
    public byte[] downloadFile(String storagePath) {
        validateConfiguration();
        String cleanPath = stripLeadingBucketPrefix(storagePath);
        String targetUri = String.format("%s/storage/v1/object/authenticated/%s/%s", supabaseUrl, bucketName, cleanPath);

        logger.info("Downloading file from Supabase Storage: bucket={}, path={}", bucketName, cleanPath);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUri))
                    .timeout(Duration.ofSeconds(30))
                    .header("Authorization", "Bearer " + serviceRoleKey)
                    .header("apikey", serviceRoleKey)
                    .GET()
                    .build();

            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return response.body();
            } else if (response.statusCode() == 404) {
                throw new StorageException("Requested file was not found in storage.");
            } else {
                logger.error("Supabase Storage download failed with status code: {}", response.statusCode());
                throw new StorageException("Failed to retrieve file from storage. Server returned status: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Network error during Supabase Storage download: {}", e.getMessage());
            throw new StorageException("Storage service communication error during download", e);
        }
    }

    /**
     * Deletes a binary object from private Supabase Storage bucket.
     *
     * @param storagePath the hierarchical object path
     */
    public void deleteFile(String storagePath) {
        validateConfiguration();
        String cleanPath = stripLeadingBucketPrefix(storagePath);
        String targetUri = String.format("%s/storage/v1/object/%s/%s", supabaseUrl, bucketName, cleanPath);

        logger.info("Deleting file from Supabase Storage: bucket={}, path={}", bucketName, cleanPath);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUri))
                    .timeout(Duration.ofSeconds(30))
                    .header("Authorization", "Bearer " + serviceRoleKey)
                    .header("apikey", serviceRoleKey)
                    .DELETE()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                logger.info("Successfully deleted object from storage: {}", cleanPath);
            } else if (response.statusCode() == 404) {
                // If object already does not exist in storage, treat safely to maintain DB consistency
                logger.warn("Object already absent in storage during delete: {}", cleanPath);
            } else {
                logger.error("Supabase Storage delete failed with status code {}: {}", response.statusCode(), response.body());
                throw new StorageException("Failed to delete file from storage. Server returned status: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Network error during Supabase Storage delete: {}", e.getMessage());
            throw new StorageException("Storage service communication error during delete", e);
        }
    }

    private void validateConfiguration() {
        if (supabaseUrl.isEmpty() || serviceRoleKey.isEmpty()) {
            throw new StorageException("Supabase Storage service is not properly configured. Please set SUPABASE_SERVICE_ROLE_KEY.");
        }
    }

    private String stripLeadingBucketPrefix(String path) {
        if (path == null) return "";
        String clean = path.replaceAll("^/+", "");
        if (clean.startsWith(bucketName + "/")) {
            clean = clean.substring(bucketName.length() + 1);
        }
        return clean;
    }

    public String getBucketName() {
        return bucketName;
    }
}
