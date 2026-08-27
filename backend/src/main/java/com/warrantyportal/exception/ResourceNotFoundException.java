package com.warrantyportal.exception;

/**
 * Thrown when an entity (such as a Product) cannot be found or is not accessible to the current customer.
 * Phase 7 — Product Management
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
