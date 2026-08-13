package com.warrantyportal.repository;

import com.warrantyportal.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    List<Invoice> findByProductId(UUID productId);
    List<Invoice> findByUserId(UUID userId);
}
