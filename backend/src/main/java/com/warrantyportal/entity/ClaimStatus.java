package com.warrantyportal.entity;

/**
 * Authoritative lifecycle status enum for Warranty Claims.
 * Normal Lifecycle: PENDING -> APPROVED -> IN_PROGRESS -> COMPLETED
 * Alternative Paths: PENDING -> REJECTED, PENDING -> CANCELLED
 * Phase 10 — Warranty Claims
 */
public enum ClaimStatus {
    PENDING,
    APPROVED,
    REJECTED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}
