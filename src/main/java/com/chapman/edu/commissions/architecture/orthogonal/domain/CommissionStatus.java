package com.chapman.edu.commissions.architecture.orthogonal.domain;

/**
 * Enum representing the status of a commission calculation.
 */
public enum CommissionStatus {
    CALCULATED("Calculated"),
    APPROVED("Approved"),
    PAID("Paid"),
    DISPUTED("Disputed"),
    ADJUSTED("Adjusted"),
    CANCELLED("Cancelled");

    private final String displayName;

    CommissionStatus(String displayName) {
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
