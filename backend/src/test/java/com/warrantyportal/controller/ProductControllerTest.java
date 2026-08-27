package com.warrantyportal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warrantyportal.config.SecurityConfig;
import com.warrantyportal.dto.ProductRequest;
import com.warrantyportal.dto.ProductResponse;
import com.warrantyportal.dto.WarrantyResponse;
import com.warrantyportal.entity.Role;
import com.warrantyportal.entity.User;
import com.warrantyportal.entity.WarrantyStatus;
import com.warrantyportal.exception.DuplicateSerialNumberException;
import com.warrantyportal.exception.GlobalExceptionHandler;
import com.warrantyportal.exception.ResourceNotFoundException;
import com.warrantyportal.repository.UserRepository;
import com.warrantyportal.security.JwtAuthenticationFilter;
import com.warrantyportal.security.JwtService;
import com.warrantyportal.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller slice tests for ProductController endpoints, validation, and security scoping.
 * Phase 7 — Product Management
 */
@WebMvcTest(controllers = ProductController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, GlobalExceptionHandler.class})
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    private User testUser;
    private final UUID testUserId = UUID.randomUUID();
    private final String validToken = "valid.test.jwt.token";

    @BeforeEach
    void setUp() {
        testUser = new User("Alice Developer", "alice@example.com", "hashedPassword", Role.CUSTOMER);
        testUser.setId(testUserId);

        when(jwtService.extractUserId(validToken)).thenReturn(testUserId);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(jwtService.isTokenValid(validToken, testUser)).thenReturn(true);
    }

    private ProductRequest createSampleRequest() {
        return new ProductRequest(
                "MacBook Pro 14",
                "Electronics",
                "Apple",
                "MBP-14-M2",
                "SN-APP-12345",
                LocalDate.now().minusDays(30),
                "Apple Fifth Ave",
                new BigDecimal("1999.00"),
                12,
                "Space Gray laptop"
        );
    }

    private ProductResponse createSampleResponse(UUID id) {
        WarrantyResponse warranty = new WarrantyResponse(
                UUID.randomUUID(), id, LocalDate.now().minusDays(30),
                LocalDate.now().plusMonths(11), WarrantyStatus.ACTIVE, 335,
                OffsetDateTime.now(), OffsetDateTime.now()
        );
        return new ProductResponse(
                id, testUserId, "MacBook Pro 14", "Electronics", "Apple",
                "MBP-14-M2", "SN-APP-12345", LocalDate.now().minusDays(30),
                "Apple Fifth Ave", new BigDecimal("1999.00"), 12, "Space Gray laptop",
                warranty, OffsetDateTime.now(), OffsetDateTime.now()
        );
    }

    @Test
    @DisplayName("1. GET /api/products returns 401 Unauthorized without JWT")
    void getProductsWithoutToken_Returns401() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("2. POST /api/products returns 201 Created on valid product registration")
    void createProduct_Returns201() throws Exception {
        ProductRequest request = createSampleRequest();
        UUID productId = UUID.randomUUID();
        ProductResponse response = createSampleResponse(productId);

        when(productService.createProduct(any(ProductRequest.class), eq(testUser))).thenReturn(response);

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.productName").value("MacBook Pro 14"))
                .andExpect(jsonPath("$.serialNumber").value("SN-APP-12345"))
                .andExpect(jsonPath("$.warranty.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("3. POST /api/products returns 400 Bad Request on invalid fields")
    void createProduct_InvalidFields_Returns400() throws Exception {
        ProductRequest invalid = new ProductRequest(); // Missing mandatory fields

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.productName").exists())
                .andExpect(jsonPath("$.errors.serialNumber").exists());
    }

    @Test
    @DisplayName("4. POST /api/products returns 409 Conflict when serial number already registered")
    void createProduct_DuplicateSerial_Returns409() throws Exception {
        ProductRequest request = createSampleRequest();

        when(productService.createProduct(any(ProductRequest.class), eq(testUser)))
                .thenThrow(new DuplicateSerialNumberException("Serial number already exists in your account."));

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Serial number already exists in your account."));
    }

    @Test
    @DisplayName("5. GET /api/products returns 200 OK with customer's products")
    void getProducts_Returns200() throws Exception {
        UUID productId = UUID.randomUUID();
        ProductResponse response = createSampleResponse(productId);

        when(productService.getProductsForUser(testUserId)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/products")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].productName").value("MacBook Pro 14"));
    }

    @Test
    @DisplayName("6. GET /api/products/{id} returns 200 OK when found")
    void getProductById_Returns200() throws Exception {
        UUID productId = UUID.randomUUID();
        ProductResponse response = createSampleResponse(productId);

        when(productService.getProductByIdForUser(productId, testUserId)).thenReturn(response);

        mockMvc.perform(get("/api/products/" + productId)
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.brand").value("Apple"));
    }

    @Test
    @DisplayName("7. GET /api/products/{id} returns 404 Not Found if missing or other customer's product")
    void getProductById_NotFound_Returns404() throws Exception {
        UUID randomId = UUID.randomUUID();

        when(productService.getProductByIdForUser(randomId, testUserId))
                .thenThrow(new ResourceNotFoundException("Product not found with id: " + randomId));

        mockMvc.perform(get("/api/products/" + randomId)
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found with id: " + randomId));
    }

    @Test
    @DisplayName("8. PUT /api/products/{id} returns 200 OK on valid update")
    void updateProduct_Returns200() throws Exception {
        UUID productId = UUID.randomUUID();
        ProductRequest updateRequest = createSampleRequest();
        ProductResponse response = createSampleResponse(productId);

        when(productService.updateProduct(eq(productId), any(ProductRequest.class), eq(testUserId)))
                .thenReturn(response);

        mockMvc.perform(put("/api/products/" + productId)
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId.toString()));
    }

    @Test
    @DisplayName("9. DELETE /api/products/{id} returns 204 No Content")
    void deleteProduct_Returns204() throws Exception {
        UUID productId = UUID.randomUUID();

        doNothing().when(productService).deleteProduct(productId, testUserId);

        mockMvc.perform(delete("/api/products/" + productId)
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNoContent());

        verify(productService, times(1)).deleteProduct(productId, testUserId);
    }
}
