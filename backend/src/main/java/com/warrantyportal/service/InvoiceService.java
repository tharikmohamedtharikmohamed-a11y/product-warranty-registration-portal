package com.warrantyportal.service;

import com.warrantyportal.dto.InvoiceDownload;
import com.warrantyportal.dto.InvoiceResponse;
import com.warrantyportal.entity.Invoice;
import com.warrantyportal.entity.Product;
import com.warrantyportal.entity.User;
import com.warrantyportal.exception.InvalidFileException;
import com.warrantyportal.exception.ResourceNotFoundException;
import com.warrantyportal.repository.InvoiceRepository;
import com.warrantyportal.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

/**
 * Service managing invoice upload validation, secure storage coordination,
 * customer ownership isolation, and metadata persistence.
 * Phase 9 — Invoice Management
 */
@Service
public class InvoiceService {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceService.class);

    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10 MB = 10,485,760 bytes

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/jpg",
            "image/png"
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf",
            "jpg",
            "jpeg",
            "png"
    );

    private final InvoiceRepository invoiceRepository;
    private final ProductRepository productRepository;
    private final SupabaseStorageService supabaseStorageService;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          ProductRepository productRepository,
                          SupabaseStorageService supabaseStorageService) {
        this.invoiceRepository = invoiceRepository;
        this.productRepository = productRepository;
        this.supabaseStorageService = supabaseStorageService;
    }

    /**
     * Validates and uploads a proof-of-purchase invoice for a customer's product.
     * Enforces file constraints, generates a secure unguessable storage path,
     * stores binary content in Supabase Storage, and saves metadata in PostgreSQL.
     */
    public InvoiceResponse uploadInvoice(UUID productId, MultipartFile file, User currentUser) {
        if (currentUser == null || currentUser.getId() == null) {
            throw new ResourceNotFoundException("User authentication required");
        }

        // 1. Verify product ownership (404 if product does not exist or belongs to another user)
        Product product = productRepository.findByIdAndUserId(productId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        // 2. Validate uploaded file
        validateFile(file);

        // 3. Sanitize and prepare metadata
        String originalFilename = sanitizeFilename(file.getOriginalFilename());
        String contentType = determineContentType(file);
        long fileSize = file.getSize();

        // 4. Construct secure hierarchical storage path:
        // invoices/{userId}/{productId}/{uuid}_{sanitizedFileName}
        String fileUuid = UUID.randomUUID().toString();
        String storedFileName = fileUuid + "_" + originalFilename;
        String storagePath = String.format("invoices/%s/%s/%s",
                currentUser.getId(), product.getId(), storedFileName);

        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (IOException e) {
            logger.error("Failed to read bytes from uploaded invoice file: {}", e.getMessage());
            throw new InvalidFileException("Could not read uploaded invoice file.");
        }

        // 5. Upload binary payload to Supabase Storage
        supabaseStorageService.uploadFile(storagePath, fileBytes, contentType);

        // 6. Persist metadata in PostgreSQL with compensating rollback on failure
        Invoice invoice = new Invoice(
                currentUser,
                product,
                originalFilename,
                storagePath,
                contentType,
                fileSize
        );

        try {
            Invoice savedInvoice = invoiceRepository.save(invoice);
            logger.info("Invoice metadata saved successfully for product={}, invoiceId={}", product.getId(), savedInvoice.getId());
            return InvoiceResponse.fromInvoice(savedInvoice);
        } catch (Exception ex) {
            // Compensating transaction: Clean up uploaded Storage file to avoid orphaned binary
            logger.error("Database persistence failed after storage upload. Executing compensating delete for path: {}", storagePath);
            try {
                supabaseStorageService.deleteFile(storagePath);
            } catch (Exception storageCleanupEx) {
                logger.error("Failed to clean up orphaned storage object after database insert error: {}", storageCleanupEx.getMessage());
            }
            throw ex;
        }
    }

    /**
     * Retrieves all invoices belonging to the authenticated customer.
     */
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getInvoicesForUser(UUID userId) {
        return invoiceRepository.findByUserIdOrderByUploadedAtDesc(userId)
                .stream()
                .map(InvoiceResponse::fromInvoice)
                .toList();
    }

    /**
     * Retrieves an invoice by ID, verifying customer ownership.
     * Returns 404 if not found or unowned to prevent IDOR leaks.
     */
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceByIdForUser(UUID invoiceId, UUID userId) {
        Invoice invoice = invoiceRepository.findByIdAndUserId(invoiceId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + invoiceId));
        return InvoiceResponse.fromInvoice(invoice);
    }

    /**
     * Retrieves the latest invoice for a product, verifying customer ownership.
     * Returns 404 if product not found or unowned, or if no invoice is present.
     */
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceByProductIdForUser(UUID productId, UUID userId) {
        // First verify product ownership
        productRepository.findByIdAndUserId(productId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        Invoice invoice = invoiceRepository.findFirstByProductIdAndUserIdOrderByUploadedAtDesc(productId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("No invoice found for product id: " + productId));

        return InvoiceResponse.fromInvoice(invoice);
    }

    /**
     * Retrieves the binary content of an invoice for download or viewing.
     * Enforces customer ownership.
     */
    @Transactional(readOnly = true)
    public InvoiceDownload downloadInvoice(UUID invoiceId, UUID userId) {
        Invoice invoice = invoiceRepository.findByIdAndUserId(invoiceId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + invoiceId));

        byte[] binaryData = supabaseStorageService.downloadFile(invoice.getStoragePath());
        return new InvoiceDownload(binaryData, invoice.getFileName(), invoice.getFileType());
    }

    /**
     * Deletes an invoice from Supabase Storage and PostgreSQL metadata.
     * Enforces customer ownership.
     */
    @Transactional
    public void deleteInvoice(UUID invoiceId, UUID userId) {
        Invoice invoice = invoiceRepository.findByIdAndUserId(invoiceId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + invoiceId));

        String storagePath = invoice.getStoragePath();

        // 1. Delete binary from Supabase Storage
        supabaseStorageService.deleteFile(storagePath);

        // 2. Delete metadata from PostgreSQL
        invoiceRepository.delete(invoice);
        logger.info("Invoice metadata deleted successfully: invoiceId={}", invoiceId);
    }

    /**
     * Validates file presence, size limits, and allowed types.
     */
    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getSize() == 0) {
            throw new InvalidFileException("Invoice file is empty.");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new InvalidFileException("Maximum file size is 10 MB.");
        }

        String rawFilename = file.getOriginalFilename();
        if (rawFilename == null || rawFilename.isBlank()) {
            throw new InvalidFileException("Invoice file must have a valid filename.");
        }

        String extension = getFileExtension(rawFilename).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new InvalidFileException("Only PDF, JPG, JPEG, and PNG files are allowed.");
        }

        String contentType = file.getContentType();
        if (contentType != null && !contentType.isBlank()) {
            String normalizedType = contentType.toLowerCase(Locale.ROOT).trim();
            if (!ALLOWED_MIME_TYPES.contains(normalizedType) && !normalizedType.equals("application/octet-stream")) {
                throw new InvalidFileException("Only PDF, JPG, JPEG, and PNG files are allowed.");
            }
        }
    }

    /**
     * Sanitizes original filename to eliminate path traversal characters and non-safe symbols.
     */
    public String sanitizeFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "invoice.pdf";
        }

        // Strip path traversal sequences and path prefixes
        String nameOnly = Paths.get(originalFilename).getFileName().toString();
        String sanitized = nameOnly.replaceAll("[^a-zA-Z0-9._-]", "_");

        // Prevent hidden files or double dots
        sanitized = sanitized.replaceAll("^\\.+", "invoice_");

        if (sanitized.length() > 200) {
            String ext = getFileExtension(sanitized);
            String base = sanitized.substring(0, 190);
            sanitized = base + (ext.isEmpty() ? "" : "." + ext);
        }

        return sanitized;
    }

    private String getFileExtension(String filename) {
        if (filename == null) return "";
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1);
        }
        return "";
    }

    private String determineContentType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null && !contentType.isBlank() && ALLOWED_MIME_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            return contentType.toLowerCase(Locale.ROOT);
        }

        String ext = getFileExtension(file.getOriginalFilename()).toLowerCase(Locale.ROOT);
        return switch (ext) {
            case "pdf" -> "application/pdf";
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            default -> "application/pdf";
        };
    }
}
