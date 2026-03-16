package com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain entity representing a commission calculation result.
 */
@Entity
@Table(name = "commission_calculations")
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
    private CommissionStatus status;

    @Column(name = "calculation_date", nullable = false)
    private LocalDate calculationDate;

    @Column(name = "payout_date")
    private LocalDate payoutDate;

    @Column(name = "plan_id")
    private String planId;

    @Column(name = "calculated_by")
    private String calculatedBy;

    public CommissionCalculation() {
        this.status = CommissionStatus.CALCULATED;
        this.calculationDate = LocalDate.now();
        this.baseCommission = BigDecimal.ZERO;
        this.grossCommission = BigDecimal.ZERO;
        this.netCommission = BigDecimal.ZERO;
        this.bonuses = new ArrayList<>();
        this.accelerators = new ArrayList<>();
    }

    public CommissionCalculation(String dealId, String salesRepId, BigDecimal baseCommission) {
        this();
        this.dealId = dealId;
        this.salesRepId = salesRepId;
        this.baseCommission = baseCommission;
        this.grossCommission = baseCommission;
        this.netCommission = baseCommission;
    }

    public void addBonus(BonusCalculation bonus) {
        this.bonuses.add(bonus);
    }

    public void addAccelerator(AcceleratorCalculation accelerator) {
        this.accelerators.add(accelerator);
    }

    /**
     * Calculate the total commission amount including base, bonuses, and accelerators.
     */
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

    /**
     * Recalculates gross and net commission from base commission, bonuses, and accelerators.
     */
    public void recalculate() {
        this.grossCommission = calculateTotalCommission();
        this.netCommission = this.grossCommission;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDealId() { return dealId; }
    public void setDealId(String dealId) { this.dealId = dealId; }
    public String getSalesRepId() { return salesRepId; }
    public void setSalesRepId(String salesRepId) { this.salesRepId = salesRepId; }
    public BigDecimal getBaseCommission() { return baseCommission; }
    public void setBaseCommission(BigDecimal baseCommission) { this.baseCommission = baseCommission; }
    public List<BonusCalculation> getBonuses() { return bonuses; }
    public void setBonuses(List<BonusCalculation> bonuses) { this.bonuses = bonuses; }
    public List<AcceleratorCalculation> getAccelerators() { return accelerators; }
    public void setAccelerators(List<AcceleratorCalculation> accelerators) { this.accelerators = accelerators; }
    public BigDecimal getGrossCommission() { return grossCommission; }
    public void setGrossCommission(BigDecimal grossCommission) { this.grossCommission = grossCommission; }
    public BigDecimal getNetCommission() { return netCommission; }
    public void setNetCommission(BigDecimal netCommission) { this.netCommission = netCommission; }
    public CommissionStatus getStatus() { return status; }
    public void setStatus(CommissionStatus status) { this.status = status; }
    public LocalDate getCalculationDate() { return calculationDate; }
    public void setCalculationDate(LocalDate calculationDate) { this.calculationDate = calculationDate; }
    public LocalDate getPayoutDate() { return payoutDate; }
    public void setPayoutDate(LocalDate payoutDate) { this.payoutDate = payoutDate; }
    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }
    public String getCalculatedBy() { return calculatedBy; }
    public void setCalculatedBy(String calculatedBy) { this.calculatedBy = calculatedBy; }
}
