package com.warrantyportal.service;

import com.warrantyportal.dto.InvoiceDownload;
import com.warrantyportal.dto.InvoiceResponse;
import com.warrantyportal.entity.Invoice;
import com.warrantyportal.entity.Product;
import com.warrantyportal.entity.Role;
import com.warrantyportal.entity.User;
import com.warrantyportal.exception.InvalidFileException;
import com.warrantyportal.exception.ResourceNotFoundException;
import com.warrantyportal.exception.StorageException;
import com.warrantyportal.repository.InvoiceRepository;
import com.warrantyportal.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for InvoiceService.
 * External Supabase Storage is mocked to ensure independent and reproducible unit testing.
 * Phase 9 — Invoice Management
 */
@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SupabaseStorageService supabaseStorageService;

    private InvoiceService invoiceService;

    private User userA;
    private User userB;
    private Product productA;
    private UUID userAId;
    private UUID userBId;
    private UUID productAId;
    private UUID invoiceId;

    @BeforeEach
    void setUp() {
        invoiceService = new InvoiceService(invoiceRepository, productRepository, supabaseStorageService);

        userAId = UUID.randomUUID();
        userA = new User("Alice Developer", "alice@example.com", "hashedPass", Role.CUSTOMER);
        userA.setId(userAId);

        userBId = UUID.randomUUID();
        userB = new User("Bob Smith", "bob@example.com", "hashedPass", Role.CUSTOMER);
        userB.setId(userBId);

        productAId = UUID.randomUUID();
        productA = new Product(userA, "MacBook Pro 16", "Electronics", "Apple", "A2485",
                "MBP-2026-XYZ", LocalDate.of(2026, 1, 15), "Apple Store",
                BigDecimal.valueOf(2499.00), 12, "Work laptop");
        productA.setId(productAId);

        invoiceId = UUID.randomUUID();
    }

    @Test
    @DisplayName("1. Upload valid PDF succeeds")
    void testUploadValidPdf() {
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file", "receipt.pdf", "application/pdf", "%PDF-1.4 dummy receipt bytes".getBytes()
        );

        when(productRepository.findByIdAndUserId(productAId, userAId)).thenReturn(Optional.of(productA));
        doNothing().when(supabaseStorageService).uploadFile(anyString(), any(byte[].class), eq("application/pdf"));

        Invoice saved = new Invoice(userA, productA, "receipt.pdf", "invoices/" + userAId + "/" + productAId + "/uuid_receipt.pdf", "application/pdf", pdfFile.getSize());
        saved.setId(invoiceId);
        saved.setUploadedAt(OffsetDateTime.now());

        when(invoiceRepository.save(any(Invoice.class))).thenReturn(saved);

        InvoiceResponse response = invoiceService.uploadInvoice(productAId, pdfFile, userA);

        assertNotNull(response);
        assertEquals(invoiceId, response.getId());
        assertEquals(productAId, response.getProductId());
        assertEquals("receipt.pdf", response.getFileName());
        assertEquals("application/pdf", response.getFileType());
        verify(supabaseStorageService, times(1)).uploadFile(anyString(), any(byte[].class), eq("application/pdf"));
        verify(invoiceRepository, times(1)).save(any(Invoice.class));
    }

    @Test
    @DisplayName("2. Upload valid JPG succeeds")
    void testUploadValidJpg() {
        MockMultipartFile jpgFile = new MockMultipartFile(
                "file", "bill.jpg", "image/jpeg", new byte[]{ (byte) 0xFF, (byte) 0xD8, 0x01, 0x02 }
        );

        when(productRepository.findByIdAndUserId(productAId, userAId)).thenReturn(Optional.of(productA));
        doNothing().when(supabaseStorageService).uploadFile(anyString(), any(byte[].class), eq("image/jpeg"));

        Invoice saved = new Invoice(userA, productA, "bill.jpg", "path", "image/jpeg", 4L);
        saved.setId(invoiceId);
        saved.setUploadedAt(OffsetDateTime.now());
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(saved);

        InvoiceResponse response = invoiceService.uploadInvoice(productAId, jpgFile, userA);

        assertNotNull(response);
        assertEquals("image/jpeg", response.getFileType());
        verify(supabaseStorageService, times(1)).uploadFile(anyString(), any(byte[].class), eq("image/jpeg"));
    }

    @Test
    @DisplayName("3. Upload valid JPEG succeeds")
    void testUploadValidJpeg() {
        MockMultipartFile jpegFile = new MockMultipartFile(
                "file", "invoice.jpeg", "image/jpeg", new byte[]{ (byte) 0xFF, (byte) 0xD8, 0x01, 0x02 }
        );

        when(productRepository.findByIdAndUserId(productAId, userAId)).thenReturn(Optional.of(productA));
        doNothing().when(supabaseStorageService).uploadFile(anyString(), any(byte[].class), eq("image/jpeg"));

        Invoice saved = new Invoice(userA, productA, "invoice.jpeg", "path", "image/jpeg", 4L);
        saved.setId(invoiceId);
        saved.setUploadedAt(OffsetDateTime.now());
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(saved);

        InvoiceResponse response = invoiceService.uploadInvoice(productAId, jpegFile, userA);

        assertNotNull(response);
        assertEquals("image/jpeg", response.getFileType());
        verify(supabaseStorageService, times(1)).uploadFile(anyString(), any(byte[].class), eq("image/jpeg"));
    }

    @Test
    @DisplayName("4. Upload valid PNG succeeds")
    void testUploadValidPng() {
        MockMultipartFile pngFile = new MockMultipartFile(
                "file", "receipt.png", "image/png", new byte[]{ (byte) 0x89, 0x50, 0x4E, 0x47 }
        );

        when(productRepository.findByIdAndUserId(productAId, userAId)).thenReturn(Optional.of(productA));
        doNothing().when(supabaseStorageService).uploadFile(anyString(), any(byte[].class), eq("image/png"));

        Invoice saved = new Invoice(userA, productA, "receipt.png", "path", "image/png", 4L);
        saved.setId(invoiceId);
        saved.setUploadedAt(OffsetDateTime.now());
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(saved);

        InvoiceResponse response = invoiceService.uploadInvoice(productAId, pngFile, userA);

        assertNotNull(response);
        assertEquals("image/png", response.getFileType());
        verify(supabaseStorageService, times(1)).uploadFile(anyString(), any(byte[].class), eq("image/png"));
    }

    @Test
    @DisplayName("5. Reject unsupported file type (.exe, .zip, .html)")
    void testRejectUnsupportedType() {
        MockMultipartFile exeFile = new MockMultipartFile(
                "file", "malware.exe", "application/x-msdownload", "fake executable binary".getBytes()
        );
        when(productRepository.findByIdAndUserId(productAId, userAId)).thenReturn(Optional.of(productA));

        InvalidFileException ex = assertThrows(InvalidFileException.class, () ->
                invoiceService.uploadInvoice(productAId, exeFile, userA)
        );
        assertTrue(ex.getMessage().contains("Only PDF, JPG, JPEG, and PNG files are allowed"));
        verifyNoInteractions(supabaseStorageService);
    }

    @Test
    @DisplayName("6. Reject file over 10 MB limit")
    void testRejectFileOver10Mb() {
        byte[] oversizedData = new byte[10 * 1024 * 1024 + 1]; // 10MB + 1 byte
        MockMultipartFile bigFile = new MockMultipartFile(
                "file", "large.pdf", "application/pdf", oversizedData
        );
        when(productRepository.findByIdAndUserId(productAId, userAId)).thenReturn(Optional.of(productA));

        InvalidFileException ex = assertThrows(InvalidFileException.class, () ->
                invoiceService.uploadInvoice(productAId, bigFile, userA)
        );
        assertTrue(ex.getMessage().contains("Maximum file size is 10 MB"));
        verifyNoInteractions(supabaseStorageService);
    }

    @Test
    @DisplayName("7. Reject empty file")
    void testRejectEmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.pdf", "application/pdf", new byte[0]
        );
        when(productRepository.findByIdAndUserId(productAId, userAId)).thenReturn(Optional.of(productA));

        InvalidFileException ex = assertThrows(InvalidFileException.class, () ->
                invoiceService.uploadInvoice(productAId, emptyFile, userA)
        );
        assertEquals("Invoice file is empty.", ex.getMessage());
        verifyNoInteractions(supabaseStorageService);
    }

    @Test
    @DisplayName("8. Upload to own product succeeds")
    void testUploadToOwnProduct() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "store_receipt.pdf", "application/pdf", "%PDF test".getBytes()
        );
        when(productRepository.findByIdAndUserId(productAId, userAId)).thenReturn(Optional.of(productA));
        doNothing().when(supabaseStorageService).uploadFile(anyString(), any(), anyString());

        Invoice saved = new Invoice(userA, productA, "store_receipt.pdf", "path", "application/pdf", 9L);
        saved.setId(invoiceId);
        saved.setUploadedAt(OffsetDateTime.now());
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(saved);

        InvoiceResponse res = invoiceService.uploadInvoice(productAId, file, userA);
        assertNotNull(res);
        assertEquals("store_receipt.pdf", res.getFileName());
    }

    @Test
    @DisplayName("9. Reject upload to another user's product (throws 404)")
    void testRejectUploadToAnotherUsersProduct() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "receipt.pdf", "application/pdf", "%PDF".getBytes()
        );
        // Product belongs to userA; userB attempts to upload against productAId
        when(productRepository.findByIdAndUserId(productAId, userBId)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () ->
                invoiceService.uploadInvoice(productAId, file, userB)
        );
        assertTrue(ex.getMessage().contains("Product not found with id: " + productAId));
        verifyNoInteractions(supabaseStorageService);
    }

    @Test
    @DisplayName("10. List own invoices returns only user's invoices")
    void testListOwnInvoices() {
        Invoice inv1 = new Invoice(userA, productA, "rec1.pdf", "path1", "application/pdf", 100L);
        inv1.setId(UUID.randomUUID());
        inv1.setUploadedAt(OffsetDateTime.now());

        Invoice inv2 = new Invoice(userA, productA, "rec2.png", "path2", "image/png", 200L);
        inv2.setId(UUID.randomUUID());
        inv2.setUploadedAt(OffsetDateTime.now().minusDays(1));

        when(invoiceRepository.findByUserIdOrderByUploadedAtDesc(userAId)).thenReturn(List.of(inv1, inv2));

        List<InvoiceResponse> list = invoiceService.getInvoicesForUser(userAId);

        assertEquals(2, list.size());
        assertEquals("rec1.pdf", list.get(0).getFileName());
        assertEquals("rec2.png", list.get(1).getFileName());
    }

    @Test
    @DisplayName("11. Get own invoice returns invoice metadata")
    void testGetOwnInvoice() {
        Invoice inv = new Invoice(userA, productA, "rec.pdf", "path", "application/pdf", 100L);
        inv.setId(invoiceId);
        inv.setUploadedAt(OffsetDateTime.now());

        when(invoiceRepository.findByIdAndUserId(invoiceId, userAId)).thenReturn(Optional.of(inv));

        InvoiceResponse response = invoiceService.getInvoiceByIdForUser(invoiceId, userAId);
        assertNotNull(response);
        assertEquals(invoiceId, response.getId());
        assertEquals("rec.pdf", response.getFileName());
    }

    @Test
    @DisplayName("12. Reject unowned invoice query (throws 404)")
    void testRejectUnownedInvoice() {
        when(invoiceRepository.findByIdAndUserId(invoiceId, userBId)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () ->
                invoiceService.getInvoiceByIdForUser(invoiceId, userBId)
        );
        assertTrue(ex.getMessage().contains("Invoice not found with id: " + invoiceId));
    }

    @Test
    @DisplayName("13. Download own invoice returns binary content")
    void testDownloadOwnInvoice() {
        Invoice inv = new Invoice(userA, productA, "invoice.pdf", "invoices/" + userAId + "/" + productAId + "/uuid_invoice.pdf", "application/pdf", 150L);
        inv.setId(invoiceId);

        byte[] expectedContent = "%PDF-1.4 file content".getBytes();
        when(invoiceRepository.findByIdAndUserId(invoiceId, userAId)).thenReturn(Optional.of(inv));
        when(supabaseStorageService.downloadFile(inv.getStoragePath())).thenReturn(expectedContent);

        InvoiceDownload download = invoiceService.downloadInvoice(invoiceId, userAId);

        assertNotNull(download);
        assertArrayEquals(expectedContent, download.getData());
        assertEquals("invoice.pdf", download.getFileName());
        assertEquals("application/pdf", download.getContentType());
    }

    @Test
    @DisplayName("14. Reject unowned download request (throws 404)")
    void testRejectUnownedDownload() {
        when(invoiceRepository.findByIdAndUserId(invoiceId, userBId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                invoiceService.downloadInvoice(invoiceId, userBId)
        );
        verifyNoInteractions(supabaseStorageService);
    }

    @Test
    @DisplayName("15. Delete own invoice removes file from storage and database")
    void testDeleteOwnInvoice() {
        Invoice inv = new Invoice(userA, productA, "invoice.pdf", "invoices/path/uuid_invoice.pdf", "application/pdf", 100L);
        inv.setId(invoiceId);

        when(invoiceRepository.findByIdAndUserId(invoiceId, userAId)).thenReturn(Optional.of(inv));
        doNothing().when(supabaseStorageService).deleteFile(inv.getStoragePath());
        doNothing().when(invoiceRepository).delete(inv);

        invoiceService.deleteInvoice(invoiceId, userAId);

        verify(supabaseStorageService, times(1)).deleteFile(inv.getStoragePath());
        verify(invoiceRepository, times(1)).delete(inv);
    }

    @Test
    @DisplayName("16. Reject unowned delete request (throws 404)")
    void testRejectUnownedDelete() {
        when(invoiceRepository.findByIdAndUserId(invoiceId, userBId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                invoiceService.deleteInvoice(invoiceId, userBId)
        );
        verifyNoInteractions(supabaseStorageService);
        verify(invoiceRepository, never()).delete(any());
    }

    @Test
    @DisplayName("17. Storage failure handling during upload aborts DB save")
    void testStorageFailureHandlingDuringUpload() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "receipt.pdf", "application/pdf", "%PDF-1.4 sample".getBytes()
        );
        when(productRepository.findByIdAndUserId(productAId, userAId)).thenReturn(Optional.of(productA));
        doThrow(new StorageException("Supabase storage timed out"))
                .when(supabaseStorageService).uploadFile(anyString(), any(byte[].class), anyString());

        StorageException ex = assertThrows(StorageException.class, () ->
                invoiceService.uploadInvoice(productAId, file, userA)
        );
        assertTrue(ex.getMessage().contains("Supabase storage timed out"));
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    @DisplayName("18. Database failure triggers compensating delete in storage")
    void testDatabaseFailureTriggersCompensatingCleanup() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "receipt.pdf", "application/pdf", "%PDF-1.4 sample".getBytes()
        );
        when(productRepository.findByIdAndUserId(productAId, userAId)).thenReturn(Optional.of(productA));
        doNothing().when(supabaseStorageService).uploadFile(anyString(), any(byte[].class), anyString());

        when(invoiceRepository.save(any(Invoice.class))).thenThrow(new RuntimeException("Database constraint violation"));

        assertThrows(RuntimeException.class, () ->
                invoiceService.uploadInvoice(productAId, file, userA)
        );

        // Verify that compensating delete was invoked to clean up orphaned storage object
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(supabaseStorageService, times(1)).deleteFile(pathCaptor.capture());
        assertTrue(pathCaptor.getValue().startsWith("invoices/" + userAId + "/" + productAId));
    }

    @Test
    @DisplayName("Path traversal sanitization prevents directory climbing")
    void testPathTraversalSanitization() {
        String dangerousName = "../../../etc/passwd.pdf";
        String sanitized = invoiceService.sanitizeFilename(dangerousName);
        assertFalse(sanitized.contains(".."));
        assertFalse(sanitized.contains("/"));
        assertFalse(sanitized.contains("\\"));
        assertTrue(sanitized.endsWith(".pdf"));
    }

    @Test
    @DisplayName("Get invoice for product returns invoice")
    void testGetInvoiceByProductIdForUser() {
        Invoice inv = new Invoice(userA, productA, "bill.pdf", "path", "application/pdf", 100L);
        inv.setId(invoiceId);
        inv.setUploadedAt(OffsetDateTime.now());

        when(productRepository.findByIdAndUserId(productAId, userAId)).thenReturn(Optional.of(productA));
        when(invoiceRepository.findFirstByProductIdAndUserIdOrderByUploadedAtDesc(productAId, userAId))
                .thenReturn(Optional.of(inv));

        InvoiceResponse response = invoiceService.getInvoiceByProductIdForUser(productAId, userAId);
        assertNotNull(response);
        assertEquals(invoiceId, response.getId());
    }
}
