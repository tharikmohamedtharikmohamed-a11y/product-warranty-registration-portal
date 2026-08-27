package com.warrantyportal.service;

import com.warrantyportal.dto.ProductRequest;
import com.warrantyportal.dto.ProductResponse;
import com.warrantyportal.entity.Product;
import com.warrantyportal.entity.Role;
import com.warrantyportal.entity.User;
import com.warrantyportal.entity.Warranty;
import com.warrantyportal.entity.WarrantyStatus;
import com.warrantyportal.exception.DuplicateSerialNumberException;
import com.warrantyportal.exception.ResourceNotFoundException;
import com.warrantyportal.repository.ProductRepository;
import com.warrantyportal.repository.WarrantyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductService verifying business logic, warranty generation, and customer isolation.
 * Phase 7 — Product Management
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WarrantyRepository warrantyRepository;

    private ProductService productService;

    private User testUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository, warrantyRepository);
        userId = UUID.randomUUID();
        testUser = new User("Alice Developer", "alice@example.com", "hashPass", Role.CUSTOMER);
        testUser.setId(userId);
    }

    @Test
    @DisplayName("1. Product creation automatically provisions active warranty")
    void createProduct_Success() {
        LocalDate purchaseDate = LocalDate.now().minusDays(10);
        ProductRequest request = new ProductRequest(
                "Dell XPS 15",
                "Electronics",
                "Dell",
                "XPS-9520",
                "SN-DELL-12345",
                purchaseDate,
                "Best Buy",
                new BigDecimal("1899.99"),
                24,
                "Primary work laptop"
        );

        when(productRepository.existsBySerialNumberIgnoreCaseAndUserId("SN-DELL-12345", userId)).thenReturn(false);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        when(productRepository.save(captor.capture())).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(UUID.randomUUID());
            if (p.getWarranty() != null) {
                p.getWarranty().setId(UUID.randomUUID());
            }
            return p;
        });

        ProductResponse response = productService.createProduct(request, testUser);

        assertNotNull(response);
        assertEquals("Dell XPS 15", response.getProductName());
        assertEquals("SN-DELL-12345", response.getSerialNumber());
        assertNotNull(response.getWarranty());
        assertEquals(purchaseDate, response.getWarranty().getStartDate());
        assertEquals(purchaseDate.plusMonths(24), response.getWarranty().getExpiryDate());
        assertEquals(WarrantyStatus.ACTIVE, response.getWarranty().getStatus());
    }

    @Test
    @DisplayName("2. Product creation throws DuplicateSerialNumberException if serial already exists for user")
    void createProduct_DuplicateSerial_ThrowsConflict() {
        ProductRequest request = new ProductRequest(
                "Dell XPS 15", "Electronics", "Dell", "XPS-9520",
                "SN-DUPLICATE", LocalDate.now(), "Best Buy", new BigDecimal("1200.00"), 12, null
        );

        when(productRepository.existsBySerialNumberIgnoreCaseAndUserId("SN-DUPLICATE", userId)).thenReturn(true);

        assertThrows(DuplicateSerialNumberException.class, () -> productService.createProduct(request, testUser));
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("3. Get products for user returns mapped list")
    void getProductsForUser_Success() {
        Product product = new Product(
                testUser, "MacBook Pro", "Electronics", "Apple", "MBP-14",
                "SN-APPLE-99", LocalDate.now().minusDays(5), "Apple Store",
                new BigDecimal("2199.00"), 12, "Developer machine"
        );
        product.setId(UUID.randomUUID());
        Warranty warranty = new Warranty(product, LocalDate.now().minusDays(5), LocalDate.now().plusMonths(12), WarrantyStatus.ACTIVE);
        product.setWarranty(warranty);

        when(productRepository.findAllByUserIdWithWarrantyOrderByCreatedAtDesc(userId)).thenReturn(List.of(product));

        List<ProductResponse> result = productService.getProductsForUser(userId);

        assertEquals(1, result.size());
        assertEquals("MacBook Pro", result.get(0).getProductName());
        assertEquals("Apple", result.get(0).getBrand());
        assertNotNull(result.get(0).getWarranty());
    }

    @Test
    @DisplayName("4. Get product by ID returns product when owned by customer")
    void getProductByIdForUser_Found() {
        UUID productId = UUID.randomUUID();
        Product product = new Product(
                testUser, "Sony Headphones", "Audio", "Sony", "WH-1000XM5",
                "SN-SONY-77", LocalDate.now(), "Amazon", new BigDecimal("399.99"), 12, null
        );
        product.setId(productId);
        product.setWarranty(new Warranty(product, LocalDate.now(), LocalDate.now().plusMonths(12), WarrantyStatus.ACTIVE));

        when(productRepository.findByIdAndUserIdWithWarranty(productId, userId)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProductByIdForUser(productId, userId);

        assertNotNull(response);
        assertEquals(productId, response.getId());
        assertEquals("Sony Headphones", response.getProductName());
    }

    @Test
    @DisplayName("5. Get product by ID throws ResourceNotFoundException when not found or belongs to another user")
    void getProductByIdForUser_NotFound() {
        UUID randomId = UUID.randomUUID();
        when(productRepository.findByIdAndUserIdWithWarranty(randomId, userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getProductByIdForUser(randomId, userId));
    }

    @Test
    @DisplayName("6. Update product recalculates warranty dates when duration or purchase date changes")
    void updateProduct_RecalculatesWarranty() {
        UUID productId = UUID.randomUUID();
        LocalDate oldPurchase = LocalDate.now().minusMonths(2);
        Product product = new Product(
                testUser, "Keyboard", "Peripherals", "Keychron", "K2",
                "SN-KEY-11", oldPurchase, "Keychron Direct", new BigDecimal("89.00"), 12, null
        );
        product.setId(productId);
        Warranty warranty = new Warranty(product, oldPurchase, oldPurchase.plusMonths(12), WarrantyStatus.ACTIVE);
        product.setWarranty(warranty);

        when(productRepository.findByIdAndUserIdWithWarranty(productId, userId)).thenReturn(Optional.of(product));
        when(productRepository.existsBySerialNumberIgnoreCaseAndUserIdAndIdNot("SN-KEY-11", userId, productId)).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        LocalDate newPurchase = LocalDate.now().minusMonths(1);
        ProductRequest updateRequest = new ProductRequest(
                "Keychron K2 Pro", "Peripherals", "Keychron", "K2-PRO",
                "SN-KEY-11", newPurchase, "Keychron Direct", new BigDecimal("99.00"), 24, "Updated switch version"
        );

        ProductResponse updated = productService.updateProduct(productId, updateRequest, userId);

        assertEquals("Keychron K2 Pro", updated.getProductName());
        assertEquals(24, updated.getWarrantyDurationMonths());
        assertNotNull(updated.getWarranty());
        assertEquals(newPurchase, updated.getWarranty().getStartDate());
        assertEquals(newPurchase.plusMonths(24), updated.getWarranty().getExpiryDate());
    }

    @Test
    @DisplayName("7. Update product throws DuplicateSerialNumberException if serial belongs to another product of user")
    void updateProduct_DuplicateSerial_ThrowsConflict() {
        UUID productId = UUID.randomUUID();
        Product product = new Product(
                testUser, "Monitor", "Displays", "LG", "27UP850",
                "SN-MON-01", LocalDate.now(), "Amazon", new BigDecimal("450.00"), 36, null
        );
        product.setId(productId);

        when(productRepository.findByIdAndUserIdWithWarranty(productId, userId)).thenReturn(Optional.of(product));
        when(productRepository.existsBySerialNumberIgnoreCaseAndUserIdAndIdNot("SN-MON-TAKEN", userId, productId)).thenReturn(true);

        ProductRequest updateRequest = new ProductRequest(
                "Monitor", "Displays", "LG", "27UP850",
                "SN-MON-TAKEN", LocalDate.now(), "Amazon", new BigDecimal("450.00"), 36, null
        );

        assertThrows(DuplicateSerialNumberException.class, () -> productService.updateProduct(productId, updateRequest, userId));
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("8. Delete product succeeds when owned by customer")
    void deleteProduct_Success() {
        UUID productId = UUID.randomUUID();
        Product product = new Product();
        product.setId(productId);
        product.setUser(testUser);

        when(productRepository.findByIdAndUserId(productId, userId)).thenReturn(Optional.of(product));

        productService.deleteProduct(productId, userId);

        verify(productRepository, times(1)).delete(product);
    }

    @Test
    @DisplayName("9. Delete product throws ResourceNotFoundException when product does not exist")
    void deleteProduct_NotFound() {
        UUID productId = UUID.randomUUID();
        when(productRepository.findByIdAndUserId(productId, userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.deleteProduct(productId, userId));
        verify(productRepository, never()).delete(any());
    }
}
