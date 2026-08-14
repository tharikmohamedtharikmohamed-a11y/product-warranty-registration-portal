package com.warrantyportal.service;

import com.warrantyportal.dto.InvoiceDownloadResponse;
import com.warrantyportal.dto.InvoiceResponse;
import com.warrantyportal.entity.Invoice;
import com.warrantyportal.entity.Product;
import com.warrantyportal.entity.User;
import com.warrantyportal.exception.InvalidFileTypeException;
import com.warrantyportal.exception.MaxFileSizeExceededException;
import com.warrantyportal.exception.ResourceForbiddenException;
import com.warrantyportal.exception.ResourceNotFoundException;
import com.warrantyportal.exception.StorageOperationException;
import com.warrantyportal.repository.InvoiceRepository;
import com.warrantyportal.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class InvoiceService {

    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10 MB
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "application/pdf",
            "image/jpeg",
            "image/jpg",
            "image/png"
    );

    private final InvoiceRepository invoiceRepository;
    private final ProductRepository productRepository;
    private final SupabaseStorageService storageService;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          ProductRepository productRepository,
                          SupabaseStorageService storageService) {
        this.invoiceRepository = invoiceRepository;
        this.productRepository = productRepository;
        this.storageService = storageService;
    }

    @Transactional
    public InvoiceResponse uploadInvoice(UUID productId, MultipartFile file, User currentUser) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileTypeException("Invoice file is required");
        }

        // 1. Validate File Size (<= 10MB)
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new MaxFileSizeExceededException("Invoice file size must not exceed 10 MB");
        }

        // 2. Validate File Type
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        if (!isValidFileType(contentType, originalFilename)) {
            throw new InvalidFileTypeException("Unsupported invoice file type");
        }

        // 3. Verify Product Existence and Ownership
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        if (!product.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceForbiddenException("Access denied: You do not own this product");
        }

        // 4. Generate Unique Storage Path: invoices/{userId}/{productId}/{uuid}_{fileName}
        String cleanFileName = originalFilename != null ? originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_") : "invoice.pdf";
        String uniqueFileName = UUID.randomUUID() + "_" + cleanFileName;
        String storagePath = String.format("invoices/%s/%s/%s", currentUser.getId(), productId, uniqueFileName);

        // 5. Upload File to Supabase Storage
        try {
            storageService.uploadFile(storagePath, file.getBytes(), contentType != null ? contentType : "application/pdf");
        } catch (Exception ex) {
            throw new StorageOperationException("Failed to upload invoice to Supabase Storage: " + ex.getMessage(), ex);
        }

        // 6. Save Invoice Metadata to PostgreSQL
        Invoice invoice = new Invoice();
        invoice.setProduct(product);
        invoice.setUser(currentUser);
        invoice.setFileName(originalFilename != null ? originalFilename : uniqueFileName);
        invoice.setFileType(contentType != null ? contentType : "application/pdf");
        invoice.setFileSize(file.getSize());
        invoice.setStoragePath(storagePath);

        try {
            Invoice savedInvoice = invoiceRepository.save(invoice);
            return InvoiceResponse.fromEntity(savedInvoice);
        } catch (Exception ex) {
            // Cleanup storage file if DB save fails to keep consistency
            storageService.deleteFile(storagePath);
            throw new StorageOperationException("Failed to save invoice metadata: " + ex.getMessage(), ex);
        }
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> getUserInvoices(User currentUser) {
        List<Invoice> invoices = invoiceRepository.findByUserId(currentUser.getId());
        return invoices.stream()
                .map(InvoiceResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceById(UUID invoiceId, User currentUser) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " + invoiceId));

        if (!invoice.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceForbiddenException("Access denied: You do not own this invoice");
        }

        return InvoiceResponse.fromEntity(invoice);
    }

    @Transactional(readOnly = true)
    public InvoiceDownloadResponse getInvoiceDownload(UUID invoiceId, User currentUser) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " + invoiceId));

        if (!invoice.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceForbiddenException("Access denied: You do not own this invoice");
        }

        int expiresInSeconds = 3600; // 1 hour signed URL
        String signedUrl = storageService.createSignedUrl(invoice.getStoragePath(), expiresInSeconds);

        return new InvoiceDownloadResponse(
                invoice.getId(),
                invoice.getFileName(),
                invoice.getFileType(),
                signedUrl,
                expiresInSeconds
        );
    }

    @Transactional
    public void deleteInvoice(UUID invoiceId, User currentUser) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " + invoiceId));

        if (!invoice.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceForbiddenException("Access denied: You do not own this invoice");
        }

        // 1. Delete from Supabase Storage
        storageService.deleteFile(invoice.getStoragePath());

        // 2. Delete metadata from PostgreSQL
        invoiceRepository.delete(invoice);
    }

    private boolean isValidFileType(String contentType, String fileName) {
        if (contentType != null && ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            return true;
        }
        if (fileName != null) {
            String lowerName = fileName.toLowerCase();
            return lowerName.endsWith(".pdf") || lowerName.endsWith(".jpg") ||
                   lowerName.endsWith(".jpeg") || lowerName.endsWith(".png");
        }
        return false;
    }
}
