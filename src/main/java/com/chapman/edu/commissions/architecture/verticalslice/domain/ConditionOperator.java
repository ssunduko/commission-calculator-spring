package com.chapman.edu.commissions.architecture.verticalslice.domain;

/**
 * Enum representing the operators for rule conditions.
 */
public enum ConditionOperator {
    EQUALS("Equals"),
    NOT_EQUALS("Not Equals"),
    GREATER_THAN("Greater Than"),
    LESS_THAN("Less Than"),
    GREATER_THAN_OR_EQUALS("Greater Than or Equals"),
    LESS_THAN_OR_EQUALS("Less Than or Equals"),
    CONTAINS("Contains"),
    STARTS_WITH("Starts With"),
    ENDS_WITH("Ends With"),
    IN("In"),
    NOT_IN("Not In");

    private final String displayName;

    ConditionOperator(String displayName) {
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