package com.chapman.edu.commissions.architecture.microservice.calculationservice.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a commission calculation in the system.
 * Commission calculations store the results of applying commission rules to deals.
 */
@Entity
@Table(name = "commission_calculations")
@Data
@NoArgsConstructor
public class CommissionCalculation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "deal_id", nullable = false)
    private String dealId;

    @Column(name = "sales_rep_id", nullable = false)
    private String salesRepId;

    @Column(name = "base_commission", nullable = false, precision = 19, scale = 2)
    private BigDecimal baseCommission = BigDecimal.ZERO;

    @Transient
    private List<BonusCalculation> bonuses = new ArrayList<>();

    @Transient
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

    @Column(name = "plan_id")
    private String planId;

    @Column(name = "calculated_by")
    private String calculatedBy;

    /**
     * Constructor with essential fields
     */
    public CommissionCalculation(String dealId, String salesRepId, BigDecimal baseCommission) {
        this.dealId = dealId;
        this.salesRepId = salesRepId;
        this.baseCommission = baseCommission;
        this.grossCommission = baseCommission;
        this.netCommission = baseCommission;
        this.bonuses = new ArrayList<>();
        this.accelerators = new ArrayList<>();
        this.calculationDate = LocalDate.now();
        this.status = CommissionStatus.CALCULATED;
    }

    public void addBonus(BonusCalculation bonus) {
        this.bonuses.add(bonus);
    }

    public void addAccelerator(AcceleratorCalculation accelerator) {
        this.accelerators.add(accelerator);
    }

    /**
     * Calculate the total commission amount including base, bonuses, and accelerators
     * @return the total commission amount
     */
    public BigDecimal calculateTotalCommission() {
        BigDecimal total = baseCommission;

        // Add bonuses
        for (BonusCalculation bonus : bonuses) {
            total = total.add(bonus.getAmount());
        }

        // Apply accelerators
        for (AcceleratorCalculation accelerator : accelerators) {
            total = total.multiply(accelerator.getMultiplier());
        }

        return total;
    }

    /**
     * Recalculate the gross and net commission amounts
     */
    public void recalculate() {
        this.grossCommission = calculateTotalCommission();
        this.netCommission = this.grossCommission; // In a real system, taxes and deductions would be applied here
    }

    @Override
    public String toString() {
        return "CommissionCalculation{" +
                "id='" + id + '\'' +
                ", dealId='" + dealId + '\'' +
                ", salesRepId='" + salesRepId + '\'' +
                ", baseCommission=" + baseCommission +
                ", grossCommission=" + grossCommission +
                ", netCommission=" + netCommission +
                ", status=" + status +
                '}';
    }
}
