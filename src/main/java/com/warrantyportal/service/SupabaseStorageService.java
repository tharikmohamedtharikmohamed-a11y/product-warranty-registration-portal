package com.warrantyportal.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.warrantyportal.exception.StorageOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class SupabaseStorageService {

    private static final Logger logger = LoggerFactory.getLogger(SupabaseStorageService.class);

    @Value("${app.supabase.url:https://cyzgjkjjhwqssobvovfj.supabase.co}")
    private String supabaseUrl;

    @Value("${app.supabase.key:sb_publishable_fvARdV2oKBJej4dMmkdLEA_bFfhx4Ds}")
    private String supabaseKey;

    @Value("${app.supabase.bucket:product-invoices}")
    private String storageBucket;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void uploadFile(String storagePath, byte[] fileBytes, String contentType) {
        try {
            String uploadUrl = String.format("%s/storage/v1/object/%s/%s", supabaseUrl, storageBucket, storagePath);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + supabaseKey);
            headers.set("apikey", supabaseKey);
            headers.set("x-upsert", "true");
            headers.setContentType(MediaType.parseMediaType(contentType));

            HttpEntity<byte[]> requestEntity = new HttpEntity<>(fileBytes, headers);
            ResponseEntity<String> response = restTemplate.exchange(uploadUrl, HttpMethod.POST, requestEntity, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new StorageOperationException("Failed to upload file to Supabase Storage: " + response.getBody());
            }
            logger.info("Successfully uploaded file to Supabase Storage at path: {}", storagePath);
        } catch (Exception ex) {
            logger.error("Error uploading file to Supabase Storage", ex);
            throw new StorageOperationException("Failed to upload file to storage: " + ex.getMessage(), ex);
        }
    }

    public void deleteFile(String storagePath) {
        try {
            String deleteUrl = String.format("%s/storage/v1/object/%s/%s", supabaseUrl, storageBucket, storagePath);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + supabaseKey);
            headers.set("apikey", supabaseKey);

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(deleteUrl, HttpMethod.DELETE, requestEntity, String.class);

            if (!response.getStatusCode().is2xxSuccessful() && response.getStatusCode() != HttpStatus.NOT_FOUND) {
                logger.warn("Supabase file deletion response status: {}", response.getStatusCode());
            }
            logger.info("Successfully deleted file from Supabase Storage at path: {}", storagePath);
        } catch (Exception ex) {
            logger.error("Error deleting file from Supabase Storage", ex);
            // Log warning but proceed if cleanup fails
        }
    }

    public String createSignedUrl(String storagePath, int expiresInSeconds) {
        try {
            String signUrl = String.format("%s/storage/v1/object/sign/%s/%s", supabaseUrl, storageBucket, storagePath);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + supabaseKey);
            headers.set("apikey", supabaseKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("expiresIn", expiresInSeconds);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(signUrl, HttpMethod.POST, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                if (jsonNode.has("signedURL")) {
                    String relativeSignedUrl = jsonNode.get("signedURL").asText();
                    return supabaseUrl + "/storage/v1" + relativeSignedUrl;
                }
            }
            return String.format("%s/storage/v1/object/public/%s/%s", supabaseUrl, storageBucket, storagePath);
        } catch (Exception ex) {
            logger.warn("Could not generate Supabase signed URL, falling back to storage path: {}", ex.getMessage());
            return String.format("%s/storage/v1/object/public/%s/%s", supabaseUrl, storageBucket, storagePath);
        }
    }

    public byte[] downloadFileBytes(String storagePath) {
        try {
            String downloadUrl = String.format("%s/storage/v1/object/%s/%s", supabaseUrl, storageBucket, storagePath);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + supabaseKey);
            headers.set("apikey", supabaseKey);

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            ResponseEntity<byte[]> response = restTemplate.exchange(downloadUrl, HttpMethod.GET, requestEntity, byte[].class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            throw new StorageOperationException("Failed to download file from Supabase Storage");
        } catch (Exception ex) {
            logger.error("Error downloading file from Supabase Storage", ex);
            throw new StorageOperationException("Failed to download file bytes: " + ex.getMessage(), ex);
        }
    }
}
