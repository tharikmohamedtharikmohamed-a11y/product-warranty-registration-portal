package com.warrantyportal.service;

import com.warrantyportal.dto.ProductRequest;
import com.warrantyportal.dto.ProductResponse;
import com.warrantyportal.entity.Product;
import com.warrantyportal.entity.User;
import com.warrantyportal.entity.Warranty;
import com.warrantyportal.entity.enums.WarrantyStatus;
import com.warrantyportal.exception.DuplicateSerialNumberException;
import com.warrantyportal.exception.ResourceForbiddenException;
import com.warrantyportal.exception.ResourceNotFoundException;
import com.warrantyportal.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final WarrantyService warrantyService;

    public ProductService(ProductRepository productRepository, WarrantyService warrantyService) {
        this.productRepository = productRepository;
        this.warrantyService = warrantyService;
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request, User currentUser) {
        if (productRepository.existsBySerialNumber(request.getSerialNumber())) {
            throw new DuplicateSerialNumberException("A product with this serial number is already registered");
        }

        Product product = new Product();
        product.setProductName(request.getProductName());
        product.setCategory(request.getCategory());
        product.setBrand(request.getBrand());
        product.setModelNumber(request.getModelNumber());
        product.setSerialNumber(request.getSerialNumber());
        product.setPurchaseDate(request.getPurchaseDate());
        product.setSellerName(request.getSellerName());
        product.setPrice(request.getPrice());
        product.setDescription(request.getDescription());
        product.setUser(currentUser);

        Product savedProduct = productRepository.save(product);

        Warranty warranty = warrantyService.createWarrantyForProduct(
                savedProduct,
                currentUser,
                request.getWarrantyDurationMonths()
        );

        savedProduct.setWarranty(warranty);

        WarrantyStatus computedStatus = warrantyService.calculateWarrantyStatus(warranty.getWarrantyEndDate());
        return ProductResponse.fromEntity(savedProduct, computedStatus);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getUserProducts(User currentUser) {
        List<Product> products = productRepository.findByUserId(currentUser.getId());
        return products.stream()
                .map(p -> {
                    WarrantyStatus status = p.getWarranty() != null ?
                            warrantyService.calculateWarrantyStatus(p.getWarranty().getWarrantyEndDate()) : null;
                    return ProductResponse.fromEntity(p, status);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(UUID productId, User currentUser) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        if (!product.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceForbiddenException("Access denied: You do not own this product");
        }

        WarrantyStatus status = product.getWarranty() != null ?
                warrantyService.calculateWarrantyStatus(product.getWarranty().getWarrantyEndDate()) : null;

        return ProductResponse.fromEntity(product, status);
    }

    @Transactional
    public ProductResponse updateProduct(UUID productId, ProductRequest request, User currentUser) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        if (!product.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceForbiddenException("Access denied: You do not own this product");
        }

        if (productRepository.existsBySerialNumberAndIdNot(request.getSerialNumber(), productId)) {
            throw new DuplicateSerialNumberException("A product with this serial number is already registered");
        }

        product.setProductName(request.getProductName());
        product.setCategory(request.getCategory());
        product.setBrand(request.getBrand());
        product.setModelNumber(request.getModelNumber());
        product.setSerialNumber(request.getSerialNumber());
        product.setPurchaseDate(request.getPurchaseDate());
        product.setSellerName(request.getSellerName());
        product.setPrice(request.getPrice());
        product.setDescription(request.getDescription());

        // Update embedded warranty dates if present
        if (product.getWarranty() != null && request.getWarrantyDurationMonths() != null) {
            Warranty warranty = product.getWarranty();
            warranty.setWarrantyStartDate(request.getPurchaseDate());
            warranty.setWarrantyEndDate(request.getPurchaseDate().plusMonths(request.getWarrantyDurationMonths()));
            warranty.setWarrantyPeriodMonths(request.getWarrantyDurationMonths());
            warranty.setStatus(warrantyService.calculateWarrantyStatus(warranty.getWarrantyEndDate()));
        }

        Product updatedProduct = productRepository.save(product);

        WarrantyStatus computedStatus = updatedProduct.getWarranty() != null ?
                warrantyService.calculateWarrantyStatus(updatedProduct.getWarranty().getWarrantyEndDate()) : null;

        return ProductResponse.fromEntity(updatedProduct, computedStatus);
    }

    @Transactional
    public void deleteProduct(UUID productId, User currentUser) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        if (!product.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceForbiddenException("Access denied: You do not own this product");
        }

        productRepository.delete(product);
    }
}
