package com.warrantyportal.dto;

import jakarta.validation.constraints.Size;

/**
 * Request DTO for administrative claim decisions (Approve, Reject, Start, Complete).
 * Accepts optional or required admin review notes / rejection reasons.
 * Phase 11 — Admin Management Module
 */
public class AdminClaimDecisionRequest {

    @Size(max = 2000, message = "Admin notes cannot exceed 2000 characters")
    private String adminNotes;

    public AdminClaimDecisionRequest() {
    }

    public AdminClaimDecisionRequest(String adminNotes) {
        this.adminNotes = adminNotes;
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }
}
