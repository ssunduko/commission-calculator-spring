package com.chapman.edu.commissions.model;

/**
 * Enum representing the possible roles of users in the system.
 */
public enum UserRole {
    SALES_REP("Sales Representative"),
    SALES_MANAGER("Sales Manager"),
    FINANCE_ADMIN("Finance Administrator"),
    SYSTEM_ADMIN("System Administrator");
    
    private final String displayName;
    
    UserRole(String displayName) {
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