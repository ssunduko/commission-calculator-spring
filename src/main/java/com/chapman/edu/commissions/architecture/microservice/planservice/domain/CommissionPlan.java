package com.chapman.edu.commissions.architecture.microservice.planservice.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

/**
 * Represents a commission plan in the system.
 * Commission plans define the rules and tiers for calculating commissions.
 */
@Entity
@Table(name = "commission_plans")
@Data
@NoArgsConstructor
public class CommissionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Transient
    private List<CommissionRule> rules = new ArrayList<>();

    @Transient
    private List<CommissionTier> tiers = new ArrayList<>();

    @Transient
    private List<BonusRule> bonuses = new ArrayList<>();

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanStatus status = PlanStatus.DRAFT;

    @Column(name = "effective_start_date")
    private LocalDate effectiveStartDate;

    @Column(name = "effective_end_date")
    private LocalDate effectiveEndDate;

    @Column(name = "created_date", nullable = false)
    private LocalDate createdDate = LocalDate.now();

    @Column(name = "last_modified_date")
    private LocalDate lastModifiedDate = LocalDate.now();

    @Column(name = "created_by")
    private String createdBy;

    /**
     * Constructor with essential fields
     */
    public CommissionPlan(String name, Currency currency) {
        this.name = name;
        this.currency = currency.getCurrencyCode();
        this.rules = new ArrayList<>();
        this.tiers = new ArrayList<>();
        this.bonuses = new ArrayList<>();
        this.createdDate = LocalDate.now();
        this.lastModifiedDate = LocalDate.now();
        this.status = PlanStatus.DRAFT;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency.getCurrencyCode();
    }

    public Currency getCurrency() {
        return Currency.getInstance(this.currency);
    }

    public void addRule(CommissionRule rule) {
        this.rules.add(rule);
    }

    public void addTier(CommissionTier tier) {
        this.tiers.add(tier);
    }

    public void addBonus(BonusRule bonus) {
        this.bonuses.add(bonus);
    }

    public void setStatus(PlanStatus status) {
        this.status = status;
        this.lastModifiedDate = LocalDate.now();
    }

    /**
     * Check if the plan is active on a given date
     * @param date the date to check
     * @return true if the plan is active on the given date
     */
    public boolean isActiveOn(LocalDate date) {
        if (status != PlanStatus.ACTIVE) {
            return false;
        }

        boolean afterStart = effectiveStartDate == null || !date.isBefore(effectiveStartDate);
        boolean beforeEnd = effectiveEndDate == null || !date.isAfter(effectiveEndDate);

        return afterStart && beforeEnd;
    }

    @Override
    public String toString() {
        return "CommissionPlan{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", effectiveStartDate=" + effectiveStartDate +
                ", effectiveEndDate=" + effectiveEndDate +
                '}';
    }
}
