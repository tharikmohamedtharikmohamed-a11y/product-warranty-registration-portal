package com.warrantyportal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warrantyportal.dto.LoginRequest;
import com.warrantyportal.dto.ProductRequest;
import com.warrantyportal.dto.RegisterRequest;
import com.warrantyportal.repository.InvoiceRepository;
import com.warrantyportal.repository.ProductRepository;
import com.warrantyportal.repository.UserRepository;
import com.warrantyportal.repository.WarrantyRepository;
import com.warrantyportal.service.SupabaseStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb5;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class InvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private WarrantyRepository warrantyRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SupabaseStorageService storageService;

    private String userAToken;
    private String userBToken;
    private String productAId;
    private String productBId;

    @BeforeEach
    void setUp() throws Exception {
        invoiceRepository.deleteAll();
        warrantyRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        doNothing().when(storageService).uploadFile(anyString(), any(byte[].class), anyString());
        doNothing().when(storageService).deleteFile(anyString());
        when(storageService.createSignedUrl(anyString(), anyInt())).thenReturn("https://supabase.co/signed-url-test");

        // Register & Login User A
        RegisterRequest userA = new RegisterRequest("User A", "usera@example.com", "Password123", "1111111111");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userA)))
                .andExpect(status().isCreated());

        MvcResult loginA = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("usera@example.com", "Password123"))))
                .andExpect(status().isOk())
                .andReturn();
        userAToken = objectMapper.readTree(loginA.getResponse().getContentAsString()).get("token").asText();

        // Create Product for User A
        ProductRequest reqA = new ProductRequest("User A Phone", "Mobile", "Apple", "iPhone 15", "SN-A111", LocalDate.now(), "Apple Store", 80000.0, 12);
        MvcResult prodA = mockMvc.perform(post("/api/products")
                .header("Authorization", "Bearer " + userAToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqA)))
                .andExpect(status().isCreated())
                .andReturn();
        productAId = objectMapper.readTree(prodA.getResponse().getContentAsString()).get("id").asText();

        // Register & Login User B
        RegisterRequest userB = new RegisterRequest("User B", "userb@example.com", "Password123", "2222222222");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userB)))
                .andExpect(status().isCreated());

        MvcResult loginB = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("userb@example.com", "Password123"))))
                .andExpect(status().isOk())
                .andReturn();
        userBToken = objectMapper.readTree(loginB.getResponse().getContentAsString()).get("token").asText();

        // Create Product for User B
        ProductRequest reqB = new ProductRequest("User B Laptop", "Laptop", "Dell", "XPS 13", "SN-B222", LocalDate.now(), "Dell Store", 120000.0, 24);
        MvcResult prodB = mockMvc.perform(post("/api/products")
                .header("Authorization", "Bearer " + userBToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqB)))
                .andExpect(status().isCreated())
                .andReturn();
        productBId = objectMapper.readTree(prodB.getResponse().getContentAsString()).get("id").asText();
    }

    // 1. Upload Valid PDF Test
    @Test
    void testUploadPdfInvoice_Success() throws Exception {
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file", "invoice.pdf", "application/pdf", "Dummy PDF content".getBytes()
        );

        mockMvc.perform(multipart("/api/invoices/upload")
                        .file(pdfFile)
                        .param("productId", productAId)
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.invoiceId").exists())
                .andExpect(jsonPath("$.fileName", is("invoice.pdf")))
                .andExpect(jsonPath("$.fileType", is("application/pdf")))
                .andExpect(jsonPath("$.storagePath", containsString("invoices/")));
    }

    // 2. Upload Valid JPG Test
    @Test
    void testUploadJpgInvoice_Success() throws Exception {
        MockMultipartFile jpgFile = new MockMultipartFile(
                "file", "receipt.jpg", "image/jpeg", "Dummy JPG image bytes".getBytes()
        );

        mockMvc.perform(multipart("/api/invoices/upload")
                        .file(jpgFile)
                        .param("productId", productAId)
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileName", is("receipt.jpg")))
                .andExpect(jsonPath("$.fileType", is("image/jpeg")));
    }

    // 3. Upload Valid PNG Test
    @Test
    void testUploadPngInvoice_Success() throws Exception {
        MockMultipartFile pngFile = new MockMultipartFile(
                "file", "receipt.png", "image/png", "Dummy PNG image bytes".getBytes()
        );

        mockMvc.perform(multipart("/api/invoices/upload")
                        .file(pngFile)
                        .param("productId", productAId)
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileName", is("receipt.png")))
                .andExpect(jsonPath("$.fileType", is("image/png")));
    }

    // 4. Reject Unsupported File Type -> 400 Bad Request
    @Test
    void testUploadUnsupportedFileType_Returns400() throws Exception {
        MockMultipartFile exeFile = new MockMultipartFile(
                "file", "script.exe", "application/x-msdownload", "print('hello')".getBytes()
        );

        mockMvc.perform(multipart("/api/invoices/upload")
                        .file(exeFile)
                        .param("productId", productAId)
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", containsString("Unsupported invoice file type")));
    }

    // 5. Reject File Larger Than 10MB -> 400 Bad Request
    @Test
    void testUploadOversizedFile_Returns400() throws Exception {
        byte[] largeBytes = new byte[11 * 1024 * 1024]; // 11 MB
        MockMultipartFile largeFile = new MockMultipartFile(
                "file", "large-invoice.pdf", "application/pdf", largeBytes
        );

        mockMvc.perform(multipart("/api/invoices/upload")
                        .file(largeFile)
                        .param("productId", productAId)
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", containsString("must not exceed 10 MB")));
    }

    // 6. Reject Unauthenticated Upload -> 401 Unauthorized
    @Test
    void testUploadUnauthenticated_Returns401() throws Exception {
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file", "invoice.pdf", "application/pdf", "Dummy content".getBytes()
        );

        mockMvc.perform(multipart("/api/invoices/upload")
                        .file(pdfFile)
                        .param("productId", productAId))
                .andExpect(status().isUnauthorized());
    }

    // 7. Reject Uploading to Another User's Product -> 403 Forbidden
    @Test
    void testUploadToAnotherUserProduct_Returns403() throws Exception {
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file", "invoice.pdf", "application/pdf", "Dummy content".getBytes()
        );

        // User A attempts to upload invoice to User B's product
        mockMvc.perform(multipart("/api/invoices/upload")
                        .file(pdfFile)
                        .param("productId", productBId)
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }

    // 8. Retrieve User's Invoices Test
    @Test
    void testGetUserInvoices_IsolatesUserData() throws Exception {
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file", "invoiceA.pdf", "application/pdf", "Dummy content".getBytes()
        );
        mockMvc.perform(multipart("/api/invoices/upload")
                        .file(pdfFile)
                        .param("productId", productAId)
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isCreated());

        // User A fetches invoices
        mockMvc.perform(get("/api/invoices")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].fileName", is("invoiceA.pdf")));

        // User B fetches invoices -> empty list
        mockMvc.perform(get("/api/invoices")
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // 9. Get Invoice By ID & Download Test
    @Test
    void testGetInvoiceByIdAndDownload() throws Exception {
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file", "my-receipt.pdf", "application/pdf", "Dummy content".getBytes()
        );
        MvcResult uploadRes = mockMvc.perform(multipart("/api/invoices/upload")
                        .file(pdfFile)
                        .param("productId", productAId)
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isCreated())
                .andReturn();

        String invoiceId = objectMapper.readTree(uploadRes.getResponse().getContentAsString()).get("invoiceId").asText();

        // GET /api/invoices/{id}
        mockMvc.perform(get("/api/invoices/" + invoiceId)
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName", is("my-receipt.pdf")));

        // GET /api/invoices/{id}/download
        mockMvc.perform(get("/api/invoices/" + invoiceId + "/download")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.downloadUrl", containsString("signed-url-test")))
                .andExpect(jsonPath("$.expiresInSeconds", is(3600)));
    }

    // 10. Delete Invoice Test
    @Test
    void testDeleteInvoice_Success() throws Exception {
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file", "to-delete.pdf", "application/pdf", "Dummy content".getBytes()
        );
        MvcResult uploadRes = mockMvc.perform(multipart("/api/invoices/upload")
                        .file(pdfFile)
                        .param("productId", productAId)
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isCreated())
                .andReturn();

        String invoiceIdStr = objectMapper.readTree(uploadRes.getResponse().getContentAsString()).get("invoiceId").asText();
        UUID invoiceId = UUID.fromString(invoiceIdStr);

        assertTrue(invoiceRepository.existsById(invoiceId));

        mockMvc.perform(delete("/api/invoices/" + invoiceIdStr)
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isNoContent());

        assertFalse(invoiceRepository.existsById(invoiceId));
    }
}
