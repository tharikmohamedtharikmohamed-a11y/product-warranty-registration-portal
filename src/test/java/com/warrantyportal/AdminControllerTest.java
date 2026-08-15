package com.warrantyportal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warrantyportal.dto.*;
import com.warrantyportal.entity.User;
import com.warrantyportal.entity.enums.ClaimStatus;
import com.warrantyportal.entity.enums.UserRole;
import com.warrantyportal.repository.*;
import com.warrantyportal.service.SupabaseStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
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
        "spring.datasource.url=jdbc:h2:mem:testdb7;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class AdminControllerTest {

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
    private WarrantyClaimRepository claimRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private SupabaseStorageService storageService;

    private String customerToken;
    private String adminToken;
    private String customerUserId;
    private String productId;
    private String warrantyId;
    private String claimId;
    private String invoiceId;

    @BeforeEach
    void setUp() throws Exception {
        claimRepository.deleteAll();
        invoiceRepository.deleteAll();
        warrantyRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        doNothing().when(storageService).uploadFile(anyString(), any(byte[].class), anyString());
        doNothing().when(storageService).deleteFile(anyString());
        when(storageService.createSignedUrl(anyString(), anyInt())).thenReturn("https://supabase.co/signed-admin-download");

        // Seed Admin User
        User adminUser = new User("System Admin", "admin@warrantyportal.com", passwordEncoder.encode("AdminPassword123"), "9999999999", UserRole.ADMIN);
        userRepository.save(adminUser);

        // 1. Login Admin
        MvcResult loginAdmin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("admin@warrantyportal.com", "AdminPassword123"))))
                .andExpect(status().isOk())
                .andReturn();
        adminToken = objectMapper.readTree(loginAdmin.getResponse().getContentAsString()).get("token").asText();

        // 2. Register & Login Customer
        RegisterRequest customerReq = new RegisterRequest("Customer One", "customer1@example.com", "Password123", "9876543210");
        MvcResult regCustomer = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerReq)))
                .andExpect(status().isCreated())
                .andReturn();
        customerUserId = objectMapper.readTree(regCustomer.getResponse().getContentAsString()).get("id").asText();

        MvcResult loginCustomer = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("customer1@example.com", "Password123"))))
                .andExpect(status().isOk())
                .andReturn();
        customerToken = objectMapper.readTree(loginCustomer.getResponse().getContentAsString()).get("token").asText();

        // 3. Create Product for Customer
        ProductRequest prodReq = new ProductRequest("Smart TV", "Electronics", "LG", "OLED55", "SN-TV-777", LocalDate.now(), "BestBuy", 95000.0, 12);
        MvcResult prodRes = mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(prodReq)))
                .andExpect(status().isCreated())
                .andReturn();
        productId = objectMapper.readTree(prodRes.getResponse().getContentAsString()).get("id").asText();
        warrantyId = objectMapper.readTree(prodRes.getResponse().getContentAsString()).get("warranty").get("warrantyId").asText();

        // 4. Upload Invoice for Customer
        MockMultipartFile pdfFile = new MockMultipartFile("file", "tv-receipt.pdf", "application/pdf", "Dummy PDF content".getBytes());
        MvcResult invRes = mockMvc.perform(multipart("/api/invoices/upload")
                        .file(pdfFile)
                        .param("productId", productId)
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isCreated())
                .andReturn();
        invoiceId = objectMapper.readTree(invRes.getResponse().getContentAsString()).get("invoiceId").asText();

        // 5. Submit Warranty Claim for Customer
        CreateClaimRequest claimReq = new CreateClaimRequest(UUID.fromString(productId), UUID.fromString(warrantyId), UUID.fromString(invoiceId), "Display line issue");
        MvcResult claimRes = mockMvc.perform(post("/api/claims")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(claimReq)))
                .andExpect(status().isCreated())
                .andReturn();
        claimId = objectMapper.readTree(claimRes.getResponse().getContentAsString()).get("claimId").asText();
    }

    // 1. Normal Customer Cannot Access /api/admin/users (403 Forbidden)
    @Test
    void testCustomerAccessAdminUsers_Returns403() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    // 2. Normal Customer Cannot Access /api/admin/claims (403 Forbidden)
    @Test
    void testCustomerAccessAdminClaims_Returns403() throws Exception {
        mockMvc.perform(get("/api/admin/claims")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    // 3. Normal Customer Cannot Update Claim Status via Admin Endpoint (403 Forbidden)
    @Test
    void testCustomerUpdateClaimStatus_Returns403() throws Exception {
        UpdateClaimStatusRequest updateReq = new UpdateClaimStatusRequest(ClaimStatus.APPROVED, "Unauthorized update");
        mockMvc.perform(put("/api/admin/claims/" + claimId + "/status")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isForbidden());
    }

    // 4. Unauthenticated Access Rejected (401 Unauthorized)
    @Test
    void testUnauthenticatedAdminAccess_Returns401() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard/stats"))
                .andExpect(status().isUnauthorized());
    }

    // 5. Admin Can View Users (200 OK)
    @Test
    void testAdminGetAllUsers_Success() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2))) // Admin + Customer
                .andExpect(jsonPath("$[*].email", hasItems("admin@warrantyportal.com", "customer1@example.com")));
    }

    // 6. Admin Can View User By ID (200 OK)
    @Test
    void testAdminGetUserById_Success() throws Exception {
        mockMvc.perform(get("/api/admin/users/" + customerUserId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("customer1@example.com")))
                .andExpect(jsonPath("$.role", is("CUSTOMER")));
    }

    // 7. Admin Can View All Products (200 OK)
    @Test
    void testAdminGetAllProducts_Success() throws Exception {
        mockMvc.perform(get("/api/admin/products")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].productName", is("Smart TV")));
    }

    // 8. Admin Can View All Warranties (200 OK)
    @Test
    void testAdminGetAllWarranties_Success() throws Exception {
        mockMvc.perform(get("/api/admin/warranties")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].warrantyStatus", is("ACTIVE")));
    }

    // 9. Admin Can View All Claims & Claim By ID (200 OK)
    @Test
    void testAdminGetAllClaimsAndById_Success() throws Exception {
        mockMvc.perform(get("/api/admin/claims")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].issueDescription", is("Display line issue")));

        mockMvc.perform(get("/api/admin/claims/" + claimId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.claimId", is(claimId)));
    }

    // 10. Admin Can Update Claim Status (200 OK)
    @Test
    void testAdminUpdateClaimStatus_Success() throws Exception {
        UpdateClaimStatusRequest updateReq = new UpdateClaimStatusRequest(ClaimStatus.APPROVED, "Authorized for screen panel replacement");

        mockMvc.perform(put("/api/admin/claims/" + claimId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("APPROVED")))
                .andExpect(jsonPath("$.resolutionNotes", is("Authorized for screen panel replacement")));
    }

    // 11. Invalid Claim Status Transition Rejected (400 Bad Request)
    @Test
    void testAdminInvalidClaimStatusTransition_Returns400() throws Exception {
        // Step 1: Update status to COMPLETED
        UpdateClaimStatusRequest req1 = new UpdateClaimStatusRequest(ClaimStatus.APPROVED, "Approved");
        mockMvc.perform(put("/api/admin/claims/" + claimId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isOk());

        UpdateClaimStatusRequest req2 = new UpdateClaimStatusRequest(ClaimStatus.COMPLETED, "Completed repair");
        mockMvc.perform(put("/api/admin/claims/" + claimId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isOk());

        // Step 2: Attempt nonsensical transition COMPLETED -> PENDING (400)
        UpdateClaimStatusRequest req3 = new UpdateClaimStatusRequest(ClaimStatus.PENDING, "Reopen");
        mockMvc.perform(put("/api/admin/claims/" + claimId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req3)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", containsString("Invalid claim status transition")));
    }

    // 12. Admin View Invoices & Signed Download Link (200 OK)
    @Test
    void testAdminGetInvoicesAndDownload_Success() throws Exception {
        mockMvc.perform(get("/api/admin/invoices")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].fileName", is("tv-receipt.pdf")));

        mockMvc.perform(get("/api/admin/invoices/" + invoiceId + "/download")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.downloadUrl", containsString("signed-admin-download")));
    }

    // 13. Dashboard Statistics Calculation Test (200 OK)
    @Test
    void testAdminGetDashboardStats_Success() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard/stats")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers", is(2)))
                .andExpect(jsonPath("$.totalProducts", is(1)))
                .andExpect(jsonPath("$.totalWarranties", is(1)))
                .andExpect(jsonPath("$.activeWarranties", is(1)))
                .andExpect(jsonPath("$.expiredWarranties", is(0)))
                .andExpect(jsonPath("$.totalInvoices", is(1)))
                .andExpect(jsonPath("$.totalClaims", is(1)))
                .andExpect(jsonPath("$.pendingClaims", is(1)));
    }
}
