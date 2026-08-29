package com.warrantyportal.controller;

import com.warrantyportal.config.SecurityConfig;
import com.warrantyportal.dto.InvoiceDownload;
import com.warrantyportal.dto.InvoiceResponse;
import com.warrantyportal.entity.Role;
import com.warrantyportal.entity.User;
import com.warrantyportal.exception.GlobalExceptionHandler;
import com.warrantyportal.exception.ResourceNotFoundException;
import com.warrantyportal.repository.UserRepository;
import com.warrantyportal.security.JwtAuthenticationFilter;
import com.warrantyportal.security.JwtService;
import com.warrantyportal.service.InvoiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller slice tests for InvoiceController endpoints, security, and customer scoping.
 * Phase 9 — Invoice Management
 */
@WebMvcTest(controllers = InvoiceController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, GlobalExceptionHandler.class})
class InvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InvoiceService invoiceService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    private User testUser;
    private final UUID testUserId = UUID.randomUUID();
    private final String validToken = "valid.test.jwt.token";
    private final UUID invoiceId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        testUser = new User("Alice Developer", "alice@example.com", "hashedPassword", Role.CUSTOMER);
        testUser.setId(testUserId);

        when(jwtService.extractUserId(validToken)).thenReturn(testUserId);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(jwtService.isTokenValid(validToken, testUser)).thenReturn(true);
    }

    private InvoiceResponse createSampleInvoiceResponse() {
        return new InvoiceResponse(
                invoiceId,
                productId,
                "MacBook Pro 16",
                "receipt.pdf",
                "application/pdf",
                2048L,
                OffsetDateTime.now()
        );
    }

    @Test
    @DisplayName("1. POST /api/invoices/upload returns 401 Unauthorized without JWT")
    void testUploadUnauthorizedWithoutToken() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "receipt.pdf", "application/pdf", "data".getBytes());

        mockMvc.perform(multipart("/api/invoices/upload")
                        .file(file)
                        .param("productId", productId.toString()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("2. POST /api/invoices/upload returns 201 Created with valid token")
    void testUploadWithValidToken() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "receipt.pdf", "application/pdf", "data".getBytes());
        InvoiceResponse response = createSampleInvoiceResponse();

        when(invoiceService.uploadInvoice(eq(productId), any(), eq(testUser))).thenReturn(response);

        mockMvc.perform(multipart("/api/invoices/upload")
                        .file(file)
                        .param("productId", productId.toString())
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(invoiceId.toString()))
                .andExpect(jsonPath("$.fileName").value("receipt.pdf"))
                .andExpect(jsonPath("$.fileType").value("application/pdf"));
    }

    @Test
    @DisplayName("3. GET /api/invoices returns 200 with list of user invoices")
    void testGetInvoices() throws Exception {
        InvoiceResponse response = createSampleInvoiceResponse();
        when(invoiceService.getInvoicesForUser(testUserId)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/invoices")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].fileName").value("receipt.pdf"));
    }

    @Test
    @DisplayName("4. GET /api/invoices/{id} returns 200 for owned invoice")
    void testGetInvoiceById() throws Exception {
        InvoiceResponse response = createSampleInvoiceResponse();
        when(invoiceService.getInvoiceByIdForUser(invoiceId, testUserId)).thenReturn(response);

        mockMvc.perform(get("/api/invoices/" + invoiceId)
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(invoiceId.toString()))
                .andExpect(jsonPath("$.fileName").value("receipt.pdf"));
    }

    @Test
    @DisplayName("5. GET /api/invoices/{id} returns 404 for unowned or nonexistent invoice")
    void testGetInvoiceByIdNotFound() throws Exception {
        when(invoiceService.getInvoiceByIdForUser(invoiceId, testUserId))
                .thenThrow(new ResourceNotFoundException("Invoice not found with id: " + invoiceId));

        mockMvc.perform(get("/api/invoices/" + invoiceId)
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Invoice not found with id: " + invoiceId));
    }

    @Test
    @DisplayName("6. GET /api/invoices/{id}/download returns 200 with attachment header")
    void testDownloadInvoice() throws Exception {
        byte[] content = "dummy pdf content".getBytes();
        InvoiceDownload download = new InvoiceDownload(content, "receipt.pdf", "application/pdf");
        when(invoiceService.downloadInvoice(invoiceId, testUserId)).thenReturn(download);

        mockMvc.perform(get("/api/invoices/" + invoiceId + "/download")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"receipt.pdf\""))
                .andExpect(content().contentType("application/pdf"))
                .andExpect(content().bytes(content));
    }

    @Test
    @DisplayName("7. GET /api/invoices/{id}/view returns 200 with inline header")
    void testViewInvoice() throws Exception {
        byte[] content = "dummy pdf content".getBytes();
        InvoiceDownload download = new InvoiceDownload(content, "receipt.pdf", "application/pdf");
        when(invoiceService.downloadInvoice(invoiceId, testUserId)).thenReturn(download);

        mockMvc.perform(get("/api/invoices/" + invoiceId + "/view")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "inline; filename=\"receipt.pdf\""))
                .andExpect(content().contentType("application/pdf"))
                .andExpect(content().bytes(content));
    }

    @Test
    @DisplayName("8. DELETE /api/invoices/{id} returns 204 No Content for owned invoice")
    void testDeleteInvoice() throws Exception {
        doNothing().when(invoiceService).deleteInvoice(invoiceId, testUserId);

        mockMvc.perform(delete("/api/invoices/" + invoiceId)
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNoContent());

        verify(invoiceService, times(1)).deleteInvoice(invoiceId, testUserId);
    }

    @Test
    @DisplayName("9. DELETE /api/invoices/{id} returns 404 for unowned invoice")
    void testDeleteInvoiceNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Invoice not found with id: " + invoiceId))
                .when(invoiceService).deleteInvoice(invoiceId, testUserId);

        mockMvc.perform(delete("/api/invoices/" + invoiceId)
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("10. GET /api/products/{productId}/invoice returns 200 for product invoice")
    void testGetProductInvoice() throws Exception {
        InvoiceResponse response = createSampleInvoiceResponse();
        when(invoiceService.getInvoiceByProductIdForUser(productId, testUserId)).thenReturn(response);

        mockMvc.perform(get("/api/products/" + productId + "/invoice")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(productId.toString()))
                .andExpect(jsonPath("$.fileName").value("receipt.pdf"));
    }

    @Test
    @DisplayName("11. GET /api/products/{productId}/invoice returns 404 if no invoice exists")
    void testGetProductInvoiceNotFound() throws Exception {
        when(invoiceService.getInvoiceByProductIdForUser(productId, testUserId))
                .thenThrow(new ResourceNotFoundException("No invoice found for product id: " + productId));

        mockMvc.perform(get("/api/products/" + productId + "/invoice")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNotFound());
    }
}
