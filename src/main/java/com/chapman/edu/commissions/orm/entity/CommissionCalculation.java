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
 * JPA ENTITY: CommissionCalculation
 * ============================================================
 *
 * ENTITY RELATIONSHIPS DEMONSTRATED:
 * - @ManyToOne to Deal: Each calculation is for one deal
 * - @ManyToOne to User: Each calculation belongs to one sales rep
 * - @ManyToOne to CommissionPlan: Each calculation uses one plan
 * - @OneToMany to BonusCalculation: A calculation can have many bonus results
 * - @OneToMany to AcceleratorCalculation: A calculation can have many accelerator results
 *
 * This entity demonstrates a complex entity with MULTIPLE relationships
 * to different parent entities (Deal, User, CommissionPlan).
 *
 * BIDIRECTIONAL RELATIONSHIPS:
 * The @ManyToOne fields (deal, salesRep, plan) are the owning sides.
 * They control the foreign key columns in the commission_calculations table.
 */
@Entity
@Table(name = "commission_calculations", indexes = {
        @Index(name = "idx_calc_deal_id", columnList = "deal_id"),
        @Index(name = "idx_calc_sales_rep_id", columnList = "sales_rep_id"),
        @Index(name = "idx_calc_plan_id", columnList = "plan_id"),
        @Index(name = "idx_calc_status", columnList = "status"),
        @Index(name = "idx_calc_date", columnList = "calculation_date"),
        @Index(name = "idx_calc_payout_date", columnList = "payout_date")
})
@Data
@NoArgsConstructor
public class CommissionCalculation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /**
     * @ManyToOne: Each calculation is for one deal.
     * A deal can have multiple calculations (e.g., recalculations).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deal_id", nullable = false)
    @JsonIgnore
    private Deal deal;

    /**
     * @ManyToOne: Each calculation belongs to one sales representative.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_rep_id", nullable = false)
    @JsonIgnore
    private User salesRep;

    @Column(name = "base_commission", nullable = false, precision = 19, scale = 2)
    private BigDecimal baseCommission = BigDecimal.ZERO;

    /**
     * @OneToMany: Bonus calculations are owned by this commission calculation.
     * CascadeType.ALL: Saving/deleting the calculation cascades to bonuses.
     */
    @OneToMany(mappedBy = "commissionCalculation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<BonusCalculation> bonuses = new ArrayList<>();

    @OneToMany(mappedBy = "commissionCalculation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<AcceleratorCalculation> accelerators = new ArrayList<>();

    @Column(name = "gross_commission", nullable = false, precision = 19, scale = 2)
    private BigDecimal grossCommission = BigDecimal.ZERO;

    @Column(name = "net_commission", nullable = false, precision = 19, scale = 2)
    private BigDecimal netCommission = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommissionStatus status = CommissionStatus.CALCULATED;

    @Column(name = "calculation_date", nullable = false)
    private LocalDate calculationDate = LocalDate.now();

    @Column(name = "payout_date")
    private LocalDate payoutDate;

    /**
     * @ManyToOne: Each calculation was computed under one plan.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    @JsonIgnore
    private CommissionPlan plan;

    @Column(name = "calculated_by")
    private String calculatedBy;

    public CommissionCalculation(Deal deal, User salesRep, BigDecimal baseCommission) {
        this.deal = deal;
        this.salesRep = salesRep;
        this.baseCommission = baseCommission;
        this.grossCommission = baseCommission;
        this.netCommission = baseCommission;
        this.bonuses = new ArrayList<>();
        this.accelerators = new ArrayList<>();
        this.calculationDate = LocalDate.now();
        this.status = CommissionStatus.CALCULATED;
    }

    public void addBonus(BonusCalculation bonus) {
        bonuses.add(bonus);
        bonus.setCommissionCalculation(this);
    }

    public void addAccelerator(AcceleratorCalculation accelerator) {
        accelerators.add(accelerator);
        accelerator.setCommissionCalculation(this);
    }

    public BigDecimal calculateTotalCommission() {
        BigDecimal total = baseCommission;
        for (BonusCalculation bonus : bonuses) {
            total = total.add(bonus.getAmount());
        }
        for (AcceleratorCalculation accelerator : accelerators) {
            total = total.multiply(accelerator.getMultiplier());
        }
        return total;
    }

    public void recalculate() {
        this.grossCommission = calculateTotalCommission();
        this.netCommission = this.grossCommission;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommissionCalculation that = (CommissionCalculation) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "CommissionCalculation{" +
                "id='" + id + '\'' +
                ", baseCommission=" + baseCommission +
                ", grossCommission=" + grossCommission +
                ", netCommission=" + netCommission +
                ", status=" + status +
                '}';
    }
}
