package com.warrantyportal.exception;

/**
 * Thrown when a warranty claim operation fails validation rules
 * (e.g., attempting to submit a claim for an expired warranty, or cancelling a non-pending claim).
 * Phase 10 — Warranty Claims
 */
public class InvalidClaimException extends RuntimeException {

    public InvalidClaimException(String message) {
        super(message);
    }
}
