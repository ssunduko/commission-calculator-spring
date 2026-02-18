package com.chapman.edu.commissions.orm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * JPA ENTITY: BonusRule
 * ============================================================
 *
 * ENTITY RELATIONSHIPS DEMONSTRATED:
 * - @ManyToOne to CommissionPlan: Many bonus rules belong to one plan.
 * - @OneToMany to RuleCondition: A bonus rule can have many conditions.
 *   (Conditions are shared between CommissionRule and BonusRule via
 *    a discriminator pattern or separate condition tables.)
 *
 * TEMPORAL DATA PATTERN:
 * The startDate/endDate fields demonstrate how to model time-bound
 * business rules. The isActiveOn() method provides application-level
 * validation, while the repository can use date-range queries.
 */
@Entity
@Table(name = "bonus_rules", indexes = {
        @Index(name = "idx_br_plan_id", columnList = "plan_id"),
        @Index(name = "idx_br_type", columnList = "type"),
        @Index(name = "idx_br_dates", columnList = "start_date, end_date")
})
@Data
@NoArgsConstructor
public class BonusRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "is_percentage", nullable = false)
    private boolean isPercentage = false;

    @Transient
    private List<RuleCondition> conditions = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BonusType type = BonusType.FIXED;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    @JsonIgnore
    private CommissionPlan plan;

    public BonusRule(String name, BigDecimal amount, boolean isPercentage, BonusType type) {
        this.name = name;
        this.amount = amount;
        this.isPercentage = isPercentage;
        this.type = type;
        this.conditions = new ArrayList<>();
    }

    public boolean isActiveOn(LocalDate date) {
        boolean afterStart = startDate == null || !date.isBefore(startDate);
        boolean beforeEnd = endDate == null || !date.isAfter(endDate);
        return afterStart && beforeEnd;
    }

    public BigDecimal calculateBonus(BigDecimal baseAmount) {
        if (isPercentage) {
            return baseAmount.multiply(amount.divide(new BigDecimal("100")));
        } else {
            return amount;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BonusRule that = (BonusRule) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "BonusRule{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", amount=" + amount +
                ", type=" + type +
                '}';
    }
}
