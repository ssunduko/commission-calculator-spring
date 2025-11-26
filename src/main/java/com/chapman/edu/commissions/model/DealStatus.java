package com.chapman.edu.commissions.model;

/**
 * Enum representing the possible statuses of a deal.
 */
public enum DealStatus {
    OPEN("Open"),
    WON("Won"),
    LOST("Lost"),
    CANCELLED("Cancelled");
    
    private final String displayName;
    
    DealStatus(String displayName) {
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