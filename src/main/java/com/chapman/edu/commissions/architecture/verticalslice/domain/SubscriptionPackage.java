package com.chapman.edu.commissions.architecture.verticalslice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "subscription_packages")
@Data
@NoArgsConstructor
public class SubscriptionPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(name = "monthly_price", nullable = false)
    private BigDecimal monthlyPrice;

    @Column(name = "max_users", nullable = false)
    private int maxUsers;

    @Column(name = "max_deals_per_month", nullable = false)
    private int maxDealsPerMonth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PackageTier tier = PackageTier.BASIC;

    @Column(nullable = false)
    private boolean active = true;

    public SubscriptionPackage(String code, String name, String description,
                               BigDecimal monthlyPrice, int maxUsers,
                               int maxDealsPerMonth, PackageTier tier) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.monthlyPrice = monthlyPrice;
        this.maxUsers = maxUsers;
        this.maxDealsPerMonth = maxDealsPerMonth;
        this.tier = tier;
        this.active = true;
    }
}
