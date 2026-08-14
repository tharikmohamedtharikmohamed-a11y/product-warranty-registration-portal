package com.warrantyportal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warrantyportal.dto.*;
import com.warrantyportal.entity.Product;
import com.warrantyportal.entity.User;
import com.warrantyportal.entity.Warranty;
import com.warrantyportal.entity.WarrantyClaim;
import com.warrantyportal.entity.enums.ClaimStatus;
import com.warrantyportal.entity.enums.WarrantyStatus;
import com.warrantyportal.repository.InvoiceRepository;
import com.warrantyportal.repository.ProductRepository;
import com.warrantyportal.repository.UserRepository;
import com.warrantyportal.repository.WarrantyClaimRepository;
import com.warrantyportal.repository.WarrantyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb6;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class WarrantyClaimControllerTest {

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
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    private String userAToken;
    private String userBToken;
    private String adminToken;

    private String productAId;
    private String warrantyAId;
    private String productBId;
    private String warrantyBId;

    private User userAEntity;

    @BeforeEach
    void setUp() throws Exception {
        claimRepository.deleteAll();
        invoiceRepository.deleteAll();
        warrantyRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        // Seed Admin User
        User adminUser = new User("System Admin", "admin@warrantyportal.com", passwordEncoder.encode("AdminPassword123"), "9999999999", com.warrantyportal.entity.enums.UserRole.ADMIN);
        userRepository.save(adminUser);

        // 1. Register & Login Customer A
        RegisterRequest userA = new RegisterRequest("User A", "usera@example.com", "Password123", "1111111111");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userA)))
                .andExpect(status().isCreated());

        userAEntity = userRepository.findByEmail("usera@example.com").orElseThrow();

        MvcResult loginA = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("usera@example.com", "Password123"))))
                .andExpect(status().isOk())
                .andReturn();
        userAToken = objectMapper.readTree(loginA.getResponse().getContentAsString()).get("token").asText();

        // Product & Warranty for User A
        ProductRequest reqA = new ProductRequest("User A Phone", "Mobile", "Apple", "iPhone 15", "SN-CLAIM-A1", LocalDate.now(), "Apple Store", 80000.0, 12);
        MvcResult prodA = mockMvc.perform(post("/api/products")
                .header("Authorization", "Bearer " + userAToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqA)))
                .andExpect(status().isCreated())
                .andReturn();
        productAId = objectMapper.readTree(prodA.getResponse().getContentAsString()).get("id").asText();
        warrantyAId = objectMapper.readTree(prodA.getResponse().getContentAsString()).get("warranty").get("warrantyId").asText();

        // 2. Register & Login Customer B
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

        ProductRequest reqB = new ProductRequest("User B Laptop", "Laptop", "Dell", "XPS 13", "SN-CLAIM-B2", LocalDate.now(), "Dell Store", 120000.0, 24);
        MvcResult prodB = mockMvc.perform(post("/api/products")
                .header("Authorization", "Bearer " + userBToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqB)))
                .andExpect(status().isCreated())
                .andReturn();
        productBId = objectMapper.readTree(prodB.getResponse().getContentAsString()).get("id").asText();
        warrantyBId = objectMapper.readTree(prodB.getResponse().getContentAsString()).get("warranty").get("warrantyId").asText();

        // 3. Login Admin
        MvcResult loginAdmin = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("admin@warrantyportal.com", "AdminPassword123"))))
                .andExpect(status().isOk())
                .andReturn();
        adminToken = objectMapper.readTree(loginAdmin.getResponse().getContentAsString()).get("token").asText();
    }

    // 1. Authenticated User Creates Valid Claim (201 Created)
    @Test
    void testCreateClaim_Success() throws Exception {
        CreateClaimRequest request = new CreateClaimRequest(
                UUID.fromString(productAId),
                UUID.fromString(warrantyAId),
                null,
                "Screen flickering issues"
        );

        mockMvc.perform(post("/api/claims")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.claimId").exists())
                .andExpect(jsonPath("$.productId", is(productAId)))
                .andExpect(jsonPath("$.warrantyId", is(warrantyAId)))
                .andExpect(jsonPath("$.issueDescription", is("Screen flickering issues")))
                .andExpect(jsonPath("$.status", is("PENDING")));
    }

    // 2. Unauthenticated User Cannot Create Claim (401 Unauthorized)
    @Test
    void testCreateClaimUnauthenticated_Returns401() throws Exception {
        CreateClaimRequest request = new CreateClaimRequest(
                UUID.fromString(productAId),
                UUID.fromString(warrantyAId),
                null,
                "Camera not focusing"
        );

        mockMvc.perform(post("/api/claims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // 3. User Cannot Create Claim For Another User's Product (403 Forbidden)
    @Test
    void testCreateClaim_AnotherUserProduct_Returns403() throws Exception {
        CreateClaimRequest request = new CreateClaimRequest(
                UUID.fromString(productBId),
                UUID.fromString(warrantyAId),
                null,
                "Attempting to claim User B product"
        );

        mockMvc.perform(post("/api/claims")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // 4. User Cannot Create Claim For Another User's Warranty (403 Forbidden)
    @Test
    void testCreateClaim_AnotherUserWarranty_Returns403() throws Exception {
        CreateClaimRequest request = new CreateClaimRequest(
                UUID.fromString(productAId),
                UUID.fromString(warrantyBId),
                null,
                "Attempting to claim User B warranty"
        );

        mockMvc.perform(post("/api/claims")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // 5. Expired Warranty Claim Rejected (400 Bad Request)
    @Test
    void testCreateClaim_ExpiredWarranty_Returns400() throws Exception {
        // Manually update warranty end date to yesterday
        Warranty warranty = warrantyRepository.findById(UUID.fromString(warrantyAId)).orElseThrow();
        warranty.setWarrantyEndDate(LocalDate.now().minusDays(1));
        warranty.setStatus(WarrantyStatus.EXPIRED);
        warrantyRepository.save(warranty);

        CreateClaimRequest request = new CreateClaimRequest(
                UUID.fromString(productAId),
                UUID.fromString(warrantyAId),
                null,
                "Claiming expired warranty"
        );

        mockMvc.perform(post("/api/claims")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", containsString("Warranty has expired")));
    }

    // 6. Empty Issue Description Rejected (400 Bad Request)
    @Test
    void testCreateClaim_EmptyDescription_Returns400() throws Exception {
        CreateClaimRequest request = new CreateClaimRequest(
                UUID.fromString(productAId),
                UUID.fromString(warrantyAId),
                null,
                "   "
        );

        mockMvc.perform(post("/api/claims")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // 7. Duplicate Active Claim Rejected (409 Conflict)
    @Test
    void testCreateClaim_DuplicateActiveClaim_Returns409() throws Exception {
        CreateClaimRequest request = new CreateClaimRequest(
                UUID.fromString(productAId),
                UUID.fromString(warrantyAId),
                null,
                "First active claim"
        );

        // Create first claim -> 201
        mockMvc.perform(post("/api/claims")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Attempt duplicate active claim -> 409
        mockMvc.perform(post("/api/claims")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.message", containsString("An active warranty claim already exists")));
    }

    // 8. User Can Retrieve Their Claims & Owner Isolation Test
    @Test
    void testGetUserClaims_IsolatesData() throws Exception {
        CreateClaimRequest request = new CreateClaimRequest(
                UUID.fromString(productAId),
                UUID.fromString(warrantyAId),
                null,
                "User A battery issues"
        );

        mockMvc.perform(post("/api/claims")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // User A retrieves claims -> size 1
        mockMvc.perform(get("/api/claims")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].issueDescription", is("User A battery issues")));

        // User B retrieves claims -> size 0
        mockMvc.perform(get("/api/claims")
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // 9. Get Claim By ID Test
    @Test
    void testGetClaimById_SuccessAndForbidden() throws Exception {
        CreateClaimRequest request = new CreateClaimRequest(
                UUID.fromString(productAId),
                UUID.fromString(warrantyAId),
                null,
                "User A speaker glitch"
        );

        MvcResult result = mockMvc.perform(post("/api/claims")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String claimId = objectMapper.readTree(result.getResponse().getContentAsString()).get("claimId").asText();

        // User A GET /api/claims/{id} -> 200 OK
        mockMvc.perform(get("/api/claims/" + claimId)
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.issueDescription", is("User A speaker glitch")));

        // User B GET /api/claims/{id} -> 403 Forbidden
        mockMvc.perform(get("/api/claims/" + claimId)
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isForbidden());
    }

    // 10. Cancel PENDING Claim Test
    @Test
    void testCancelPendingClaim_Success() throws Exception {
        CreateClaimRequest request = new CreateClaimRequest(
                UUID.fromString(productAId),
                UUID.fromString(warrantyAId),
                null,
                "Issue resolved by customer"
        );

        MvcResult result = mockMvc.perform(post("/api/claims")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String claimId = objectMapper.readTree(result.getResponse().getContentAsString()).get("claimId").asText();

        // Customer cancels claim
        mockMvc.perform(put("/api/claims/" + claimId + "/cancel")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));

        WarrantyClaim updated = claimRepository.findById(UUID.fromString(claimId)).orElseThrow();
        assertEquals(ClaimStatus.CANCELLED, updated.getStatus());
    }

    // 11. Cannot Cancel COMPLETED Claim (400 Bad Request)
    @Test
    void testCancelCompletedClaim_Returns400() throws Exception {
        CreateClaimRequest request = new CreateClaimRequest(
                UUID.fromString(productAId),
                UUID.fromString(warrantyAId),
                null,
                "Claim to complete"
        );

        MvcResult result = mockMvc.perform(post("/api/claims")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String claimId = objectMapper.readTree(result.getResponse().getContentAsString()).get("claimId").asText();

        // Mark claim COMPLETED
        WarrantyClaim claim = claimRepository.findById(UUID.fromString(claimId)).orElseThrow();
        claim.setStatus(ClaimStatus.COMPLETED);
        claimRepository.save(claim);

        // Attempt cancel -> 400 Bad Request
        mockMvc.perform(put("/api/claims/" + claimId + "/cancel")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Cannot cancel claim in status: COMPLETED")));
    }

    // 12. Admin Status Update Test
    @Test
    void testAdminUpdateClaimStatus_Success() throws Exception {
        CreateClaimRequest request = new CreateClaimRequest(
                UUID.fromString(productAId),
                UUID.fromString(warrantyAId),
                null,
                "Hardware fault"
        );

        MvcResult result = mockMvc.perform(post("/api/claims")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String claimId = objectMapper.readTree(result.getResponse().getContentAsString()).get("claimId").asText();

        UpdateClaimStatusRequest updateReq = new UpdateClaimStatusRequest(ClaimStatus.APPROVED, "Replacement device approved");

        // Admin updates status -> 200 OK
        mockMvc.perform(put("/api/admin/claims/" + claimId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("APPROVED")))
                .andExpect(jsonPath("$.resolutionNotes", is("Replacement device approved")));

        // Non-admin Customer attempts status update -> 403 Forbidden
        mockMvc.perform(put("/api/admin/claims/" + claimId + "/status")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isForbidden());
    }
}
