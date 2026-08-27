package com.warrantyportal.entity;

/**
 * Status representation for product warranty coverage.
 * Values: ACTIVE, EXPIRING_SOON, EXPIRED.
 * Matches chk_warranties_status constraint in database/schema.sql.
 * Phase 7 — Product Management
 */
public enum WarrantyStatus {
    ACTIVE,
    EXPIRING_SOON,
    EXPIRED
}
