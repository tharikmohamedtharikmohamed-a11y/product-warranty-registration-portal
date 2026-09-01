package com.warrantyportal.dto;

/**
 * Data Transfer Object for unread notification count.
 * Phase 12 — Dashboard & Notifications
 */
public class UnreadCountResponse {

    private long count;

    public UnreadCountResponse() {
    }

    public UnreadCountResponse(long count) {
        this.count = count;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
