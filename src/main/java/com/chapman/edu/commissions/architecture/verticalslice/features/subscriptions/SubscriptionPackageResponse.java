package com.chapman.edu.commissions.architecture.verticalslice.features.subscriptions;

import com.chapman.edu.commissions.architecture.verticalslice.domain.PackageTier;
import com.chapman.edu.commissions.architecture.verticalslice.domain.SubscriptionPackage;

import java.math.BigDecimal;

public record SubscriptionPackageResponse(
    String id,
    String code,
    String name,
    String description,
    BigDecimal monthlyPrice,
    int maxUsers,
    int maxDealsPerMonth,
    PackageTier tier,
    boolean active
) {
    public static SubscriptionPackageResponse from(SubscriptionPackage pkg) {
        return new SubscriptionPackageResponse(
            pkg.getId(),
            pkg.getCode(),
            pkg.getName(),
            pkg.getDescription(),
            pkg.getMonthlyPrice(),
            pkg.getMaxUsers(),
            pkg.getMaxDealsPerMonth(),
            pkg.getTier(),
            pkg.isActive()
        );
    }
}
