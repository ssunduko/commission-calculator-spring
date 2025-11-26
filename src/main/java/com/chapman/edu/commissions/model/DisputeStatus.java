package com.chapman.edu.commissions.model;

/**
 * Enum representing the possible statuses of a dispute.
 */
public enum DisputeStatus {
    INITIATED("Initiated"),
    UNDER_REVIEW("Under Review"),
    ADDITIONAL_INFO_REQUESTED("Additional Info Requested"),
    ESCALATED("Escalated"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    RESOLVED("Resolved"),
    CANCELLED("Cancelled");
    
    private final String displayName;
    
    DisputeStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}