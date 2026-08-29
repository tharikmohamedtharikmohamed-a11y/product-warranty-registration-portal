package com.warrantyportal.exception;

/**
 * Thrown when an uploaded invoice fails file validation checks:
 * empty payload, unsupported MIME type/extension, or exceeding 10 MB limit.
 * Phase 9 — Invoice Management
 */
public class InvalidFileException extends RuntimeException {

    public InvalidFileException(String message) {
        super(message);
    }
}
