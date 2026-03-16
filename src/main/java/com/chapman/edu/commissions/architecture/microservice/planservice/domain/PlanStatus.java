package com.chapman.edu.commissions.architecture.microservice.planservice.domain;

/**
 * Enum representing the possible statuses of a commission plan.
 */
public enum PlanStatus {
    DRAFT("Draft"),
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    ARCHIVED("Archived");

    private final String displayName;

    PlanStatus(String displayName) {
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
