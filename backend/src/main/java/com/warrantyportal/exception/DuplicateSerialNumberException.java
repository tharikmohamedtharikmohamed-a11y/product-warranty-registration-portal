package com.warrantyportal.exception;

/**
 * Thrown when a product registration or update conflicts with an existing serial number owned by the customer.
 * Phase 7 — Product Management
 */
public class DuplicateSerialNumberException extends RuntimeException {

    public DuplicateSerialNumberException(String message) {
        super(message);
    }
}
