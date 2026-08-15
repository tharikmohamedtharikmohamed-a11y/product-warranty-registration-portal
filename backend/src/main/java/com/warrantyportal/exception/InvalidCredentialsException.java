package com.warrantyportal.exception;

/**
 * Thrown when login fails due to incorrect email or password.
 * Phase 4 — Backend Authentication
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
