package com.chapman.edu.commissions.architecture.ddd.domain.plan;

/**
 * Enum representing the types of commission rules.
 */
public enum RuleType {
    STANDARD("Standard"),
    ACCELERATOR("Accelerator"),
    DECELERATOR("Decelerator"),
    BONUS("Bonus"),
    SPECIAL("Special");

    private final String displayName;

    RuleType(String displayName) {
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
