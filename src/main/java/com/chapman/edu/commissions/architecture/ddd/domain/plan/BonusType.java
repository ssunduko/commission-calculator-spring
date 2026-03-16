package com.chapman.edu.commissions.architecture.ddd.domain.plan;

/**
 * Enum representing the types of bonuses.
 */
public enum BonusType {
    FIXED("Fixed"),
    SPIF("SPIF"),
    ACCELERATOR("Accelerator"),
    QUOTA_ACHIEVEMENT("Quota Achievement"),
    TEAM_PERFORMANCE("Team Performance"),
    SPECIAL_INCENTIVE("Special Incentive");

    private final String displayName;

    BonusType(String displayName) {
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
