package com.warrantyportal.dto;

import com.warrantyportal.entity.enums.ClaimStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateClaimStatusRequest {

    @NotNull(message = "Claim status is required")
    private ClaimStatus status;

    private String resolutionNotes;

    public UpdateClaimStatusRequest() {
    }

    public UpdateClaimStatusRequest(ClaimStatus status, String resolutionNotes) {
        this.status = status;
        this.resolutionNotes = resolutionNotes;
    }

    public ClaimStatus getStatus() {
        return status;
    }

    public void setStatus(ClaimStatus status) {
        this.status = status;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }
}
