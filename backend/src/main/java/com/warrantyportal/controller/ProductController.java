package com.warrantyportal.controller;

import com.warrantyportal.dto.ProductRequest;
import com.warrantyportal.dto.ProductResponse;
import com.warrantyportal.entity.User;
import com.warrantyportal.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller managing product registration, queries, updates, and removals.
 * All operations are strictly scoped to the authenticated customer.
 * Phase 7 — Product Management
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Registers a new product and automatically generates its warranty.
     */
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductRequest request,
            @AuthenticationPrincipal User currentUser) {
        ProductResponse response = productService.createProduct(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all registered products belonging to the authenticated customer.
     */
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getProducts(
            @AuthenticationPrincipal User currentUser) {
        List<ProductResponse> products = productService.getProductsForUser(currentUser.getId());
        return ResponseEntity.ok(products);
    }

    /**
     * Retrieves a single product by ID scoped to the authenticated customer.
     * Returns 404 if not found or if owned by another customer.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        ProductResponse product = productService.getProductByIdForUser(id, currentUser.getId());
        return ResponseEntity.ok(product);
    }

    /**
     * Updates an existing registered product and recalculates warranty dates if applicable.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody ProductRequest request,
            @AuthenticationPrincipal User currentUser) {
        ProductResponse response = productService.updateProduct(id, request, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a registered product and cascades deletion to its associated warranty.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        productService.deleteProduct(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}
