package com.warrantyportal.controller;

import com.warrantyportal.dto.InvoiceDownloadResponse;
import com.warrantyportal.dto.InvoiceResponse;
import com.warrantyportal.entity.User;
import com.warrantyportal.service.AuthService;
import com.warrantyportal.service.InvoiceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final AuthService authService;

    public InvoiceController(InvoiceService invoiceService, AuthService authService) {
        this.invoiceService = invoiceService;
        this.authService = authService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<InvoiceResponse> uploadInvoice(@RequestParam("productId") UUID productId,
                                                          @RequestParam("file") MultipartFile file) {
        User currentUser = authService.getCurrentUser();
        InvoiceResponse response = invoiceService.uploadInvoice(productId, file, currentUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<InvoiceResponse>> getUserInvoices() {
        User currentUser = authService.getCurrentUser();
        List<InvoiceResponse> invoices = invoiceService.getUserInvoices(currentUser);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponse> getInvoiceById(@PathVariable("id") UUID id) {
        User currentUser = authService.getCurrentUser();
        InvoiceResponse response = invoiceService.getInvoiceById(id, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<InvoiceDownloadResponse> downloadInvoice(@PathVariable("id") UUID id) {
        User currentUser = authService.getCurrentUser();
        InvoiceDownloadResponse response = invoiceService.getInvoiceDownload(id, currentUser);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable("id") UUID id) {
        User currentUser = authService.getCurrentUser();
        invoiceService.deleteInvoice(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
