package com.warrantyportal.exception;

/**
 * Thrown when registration is attempted with an existing email.
 * Phase 4 — Backend Authentication
 */
public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
