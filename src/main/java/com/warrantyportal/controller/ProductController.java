package com.warrantyportal.controller;

import com.warrantyportal.dto.ProductRequest;
import com.warrantyportal.dto.ProductResponse;
import com.warrantyportal.dto.WarrantyResponse;
import com.warrantyportal.entity.User;
import com.warrantyportal.service.AuthService;
import com.warrantyportal.service.ProductService;
import com.warrantyportal.service.WarrantyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final WarrantyService warrantyService;
    private final AuthService authService;

    public ProductController(ProductService productService, WarrantyService warrantyService, AuthService authService) {
        this.productService = productService;
        this.warrantyService = warrantyService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest productRequest) {
        User currentUser = authService.getCurrentUser();
        ProductResponse response = productService.createProduct(productRequest, currentUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getUserProducts() {
        User currentUser = authService.getCurrentUser();
        List<ProductResponse> products = productService.getUserProducts(currentUser);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable("id") UUID id) {
        User currentUser = authService.getCurrentUser();
        ProductResponse response = productService.getProductById(id, currentUser);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable("id") UUID id,
                                                          @Valid @RequestBody ProductRequest productRequest) {
        User currentUser = authService.getCurrentUser();
        ProductResponse response = productService.updateProduct(id, productRequest, currentUser);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable("id") UUID id) {
        User currentUser = authService.getCurrentUser();
        productService.deleteProduct(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{productId}/warranty")
    public ResponseEntity<WarrantyResponse> getProductWarranty(@PathVariable("productId") UUID productId) {
        User currentUser = authService.getCurrentUser();
        WarrantyResponse response = warrantyService.getWarrantyByProductId(productId, currentUser);
        return ResponseEntity.ok(response);
    }
}
