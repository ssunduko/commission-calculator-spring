package com.chapman.edu.commissions.orm.entity;

/**
 * Enum representing the logical operators for combining rule conditions.
 */
public enum LogicalOperator {
    AND("And"),
    OR("Or");

    private final String displayName;

    LogicalOperator(String displayName) {
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