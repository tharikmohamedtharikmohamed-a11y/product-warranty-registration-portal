package com.warrantyportal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warrantyportal.dto.LoginRequest;
import com.warrantyportal.dto.ProductRequest;
import com.warrantyportal.dto.RegisterRequest;
import com.warrantyportal.entity.Product;
import com.warrantyportal.entity.User;
import com.warrantyportal.entity.Warranty;
import com.warrantyportal.entity.enums.WarrantyStatus;
import com.warrantyportal.repository.ProductRepository;
import com.warrantyportal.repository.UserRepository;
import com.warrantyportal.repository.WarrantyRepository;
import com.warrantyportal.service.WarrantyService;
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
        "spring.datasource.url=jdbc:h2:mem:testdb4;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class ProductAndWarrantyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private WarrantyRepository warrantyRepository;

    @Autowired
    private WarrantyService warrantyService;

    @Autowired
    private ObjectMapper objectMapper;

    private String userAToken;
    private String userBToken;

    @BeforeEach
    void setUp() throws Exception {
        warrantyRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

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
    }

    // 1. Create Product & Auto Warranty Test
    @Test
    void testCreateProductAndAutoWarranty() throws Exception {
        ProductRequest req = new ProductRequest(
                "Samsung Refrigerator", "Home Appliance", "Samsung", "RT28T3922S8",
                "SN-REF-001", LocalDate.now(), "ABC Electronics", 45000.0, 24
        );

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.productName", is("Samsung Refrigerator")))
                .andExpect(jsonPath("$.serialNumber", is("SN-REF-001")))
                .andExpect(jsonPath("$.sellerName", is("ABC Electronics")))
                .andExpect(jsonPath("$.price", is(45000.0)))
                .andExpect(jsonPath("$.warranty.warrantyId").exists())
                .andExpect(jsonPath("$.warranty.warrantyDurationMonths", is(24)))
                .andExpect(jsonPath("$.warranty.status", is("ACTIVE")));
    }

    // 2. Duplicate Serial Number Test (409 Conflict)
    @Test
    void testDuplicateSerialNumber_Returns409Conflict() throws Exception {
        ProductRequest req1 = new ProductRequest(
                "Samsung Refrigerator", "Home Appliance", "Samsung", "RT28T3922S8",
                "SN-DUPLICATE", LocalDate.now(), "ABC Electronics", 45000.0, 24
        );

        mockMvc.perform(post("/api/products")
                .header("Authorization", "Bearer " + userAToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated());

        ProductRequest req2 = new ProductRequest(
                "LG Washing Machine", "Home Appliance", "LG", "WM100",
                "SN-DUPLICATE", LocalDate.now(), "XYZ Stores", 30000.0, 12
        );

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + userBToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.message", containsString("already registered")));
    }

    // 3. Get User Products Isolation Test (User A cannot see User B's products)
    @Test
    void testGetUserProducts_IsolatesUserData() throws Exception {
        ProductRequest reqA = new ProductRequest(
                "Product User A", "Category A", "Brand A", "M-A",
                "SN-USER-A", LocalDate.now(), "Seller A", 1000.0, 12
        );
        mockMvc.perform(post("/api/products")
                .header("Authorization", "Bearer " + userAToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqA)))
                .andExpect(status().isCreated());

        ProductRequest reqB = new ProductRequest(
                "Product User B", "Category B", "Brand B", "M-B",
                "SN-USER-B", LocalDate.now(), "Seller B", 2000.0, 12
        );
        mockMvc.perform(post("/api/products")
                .header("Authorization", "Bearer " + userBToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqB)))
                .andExpect(status().isCreated());

        // User A fetches products
        mockMvc.perform(get("/api/products")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].productName", is("Product User A")));

        // User B fetches products
        mockMvc.perform(get("/api/products")
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].productName", is("Product User B")));
    }

    // 4. Access Product By ID - 403 Forbidden for Cross-Customer Access
    @Test
    void testGetProductById_Returns403ForAnotherUserProduct() throws Exception {
        ProductRequest reqA = new ProductRequest(
                "Product User A", "Category A", "Brand A", "M-A",
                "SN-USER-A", LocalDate.now(), "Seller A", 1000.0, 12
        );
        MvcResult result = mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqA)))
                .andExpect(status().isCreated())
                .andReturn();

        String productId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();

        // User B attempts to access User A's product -> 403 Forbidden
        mockMvc.perform(get("/api/products/" + productId)
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }

    // 5. Get Product By ID - 404 Not Found for non-existent product
    @Test
    void testGetProductById_Returns404ForNonExistentProduct() throws Exception {
        mockMvc.perform(get("/api/products/" + UUID.randomUUID())
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    // 6. Update Product Test
    @Test
    void testUpdateProduct_Success() throws Exception {
        ProductRequest reqA = new ProductRequest(
                "Original Name", "Category A", "Brand A", "M-A",
                "SN-ORIGINAL", LocalDate.now(), "Seller A", 1000.0, 12
        );
        MvcResult result = mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqA)))
                .andExpect(status().isCreated())
                .andReturn();

        String productId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();

        ProductRequest updateReq = new ProductRequest(
                "Updated Name", "Category A", "Brand A", "M-A",
                "SN-ORIGINAL", LocalDate.now(), "Updated Seller", 1500.0, 24
        );

        mockMvc.perform(put("/api/products/" + productId)
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName", is("Updated Name")))
                .andExpect(jsonPath("$.sellerName", is("Updated Seller")))
                .andExpect(jsonPath("$.warranty.warrantyDurationMonths", is(24)));
    }

    // 7. Delete Product Test
    @Test
    void testDeleteProduct_Success() throws Exception {
        ProductRequest reqA = new ProductRequest(
                "To Delete", "Category A", "Brand A", "M-A",
                "SN-DELETE", LocalDate.now(), "Seller A", 1000.0, 12
        );
        MvcResult result = mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqA)))
                .andExpect(status().isCreated())
                .andReturn();

        String productId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(delete("/api/products/" + productId)
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isNoContent());

        // Verify product deleted
        mockMvc.perform(get("/api/products/" + productId)
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isNotFound());
    }

    // 8. Warranty Calculation Status Test (ACTIVE, EXPIRING_SOON, EXPIRED)
    @Test
    void testWarrantyStatusCalculation() {
        // Active (> 30 days remaining)
        assertEquals(WarrantyStatus.ACTIVE, warrantyService.calculateWarrantyStatus(LocalDate.now().plusDays(60)));

        // Expiring Soon (<= 30 days remaining)
        assertEquals(WarrantyStatus.EXPIRING_SOON, warrantyService.calculateWarrantyStatus(LocalDate.now().plusDays(15)));
        assertEquals(WarrantyStatus.EXPIRING_SOON, warrantyService.calculateWarrantyStatus(LocalDate.now()));

        // Expired (Past date)
        assertEquals(WarrantyStatus.EXPIRED, warrantyService.calculateWarrantyStatus(LocalDate.now().minusDays(1)));
    }

    // 9. Warranty API Endpoints Test
    @Test
    void testGetWarrantiesAndProductWarranty() throws Exception {
        ProductRequest reqA = new ProductRequest(
                "Smart TV", "Electronics", "Sony", "Bravia-55",
                "SN-TV-55", LocalDate.now(), "Sony Center", 65000.0, 12
        );
        MvcResult result = mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqA)))
                .andExpect(status().isCreated())
                .andReturn();

        String productId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
        String warrantyId = objectMapper.readTree(result.getResponse().getContentAsString()).get("warranty").get("warrantyId").asText();

        // GET /api/warranties
        mockMvc.perform(get("/api/warranties")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].warrantyId", is(warrantyId)));

        // GET /api/warranties/{id}
        mockMvc.perform(get("/api/warranties/" + warrantyId)
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName", is("Smart TV")));

        // GET /api/products/{productId}/warranty
        mockMvc.perform(get("/api/products/" + productId + "/warranty")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.warrantyId", is(warrantyId)))
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }
}
