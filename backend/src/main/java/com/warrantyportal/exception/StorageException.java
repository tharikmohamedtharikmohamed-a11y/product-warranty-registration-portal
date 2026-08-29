package com.warrantyportal.exception;

/**
 * Thrown when an error occurs while communicating with Supabase Storage.
 * Phase 9 — Invoice Management
 */
public class StorageException extends RuntimeException {

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
