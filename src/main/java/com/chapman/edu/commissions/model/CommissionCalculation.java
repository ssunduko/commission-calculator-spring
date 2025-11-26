package com.chapman.edu.commissions.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a commission calculation in the system.
 * Commission calculations store the results of applying commission rules to deals.
 */
public class CommissionCalculation {
    private String id;
    private String dealId;
    private String salesRepId;
    private BigDecimal baseCommission;
    private List<BonusCalculation> bonuses;
    private List<AcceleratorCalculation> accelerators;
    private BigDecimal grossCommission;
    private BigDecimal netCommission;
    private CommissionStatus status;
    private LocalDate calculationDate;
    private LocalDate payoutDate;
    private String planId;
    private String calculatedBy;
    
    /**
     * Default constructor
     */
    public CommissionCalculation() {
        this.bonuses = new ArrayList<>();
        this.accelerators = new ArrayList<>();
        this.baseCommission = BigDecimal.ZERO;
        this.grossCommission = BigDecimal.ZERO;
        this.netCommission = BigDecimal.ZERO;
        this.calculationDate = LocalDate.now();
        this.status = CommissionStatus.CALCULATED;
    }
    
    /**
     * Constructor with essential fields
     */
    public CommissionCalculation(String dealId, String salesRepId, BigDecimal baseCommission) {
        this();
        this.dealId = dealId;
        this.salesRepId = salesRepId;
        this.baseCommission = baseCommission;
        this.grossCommission = baseCommission;
        this.netCommission = baseCommission;
    }
    
    // Getters and Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getDealId() {
        return dealId;
    }
    
    public void setDealId(String dealId) {
        this.dealId = dealId;
    }
    
    public String getSalesRepId() {
        return salesRepId;
    }
    
    public void setSalesRepId(String salesRepId) {
        this.salesRepId = salesRepId;
    }
    
    public BigDecimal getBaseCommission() {
        return baseCommission;
    }
    
    public void setBaseCommission(BigDecimal baseCommission) {
        this.baseCommission = baseCommission;
    }
    
    public List<BonusCalculation> getBonuses() {
        return bonuses;
    }
    
    public void setBonuses(List<BonusCalculation> bonuses) {
        this.bonuses = bonuses;
    }
    
    public void addBonus(BonusCalculation bonus) {
        this.bonuses.add(bonus);
    }
    
    public List<AcceleratorCalculation> getAccelerators() {
        return accelerators;
    }
    
    public void setAccelerators(List<AcceleratorCalculation> accelerators) {
        this.accelerators = accelerators;
    }
    
    public void addAccelerator(AcceleratorCalculation accelerator) {
        this.accelerators.add(accelerator);
    }
    
    public BigDecimal getGrossCommission() {
        return grossCommission;
    }
    
    public void setGrossCommission(BigDecimal grossCommission) {
        this.grossCommission = grossCommission;
    }
    
    public BigDecimal getNetCommission() {
        return netCommission;
    }
    
    public void setNetCommission(BigDecimal netCommission) {
        this.netCommission = netCommission;
    }
    
    public CommissionStatus getStatus() {
        return status;
    }
    
    public void setStatus(CommissionStatus status) {
        this.status = status;
    }
    
    public LocalDate getCalculationDate() {
        return calculationDate;
    }
    
    public void setCalculationDate(LocalDate calculationDate) {
        this.calculationDate = calculationDate;
    }
    
    public LocalDate getPayoutDate() {
        return payoutDate;
    }
    
    public void setPayoutDate(LocalDate payoutDate) {
        this.payoutDate = payoutDate;
    }
    
    public String getPlanId() {
        return planId;
    }
    
    public void setPlanId(String planId) {
        this.planId = planId;
    }
    
    public String getCalculatedBy() {
        return calculatedBy;
    }
    
    public void setCalculatedBy(String calculatedBy) {
        this.calculatedBy = calculatedBy;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommissionCalculation that = (CommissionCalculation) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
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
    
    /**
     * Enum representing the status of a commission calculation.
     */
    public enum CommissionStatus {
        CALCULATED("Calculated"),
        APPROVED("Approved"),
        PAID("Paid"),
        DISPUTED("Disputed"),
        ADJUSTED("Adjusted"),
        CANCELLED("Cancelled");
        
        private final String displayName;
        
        CommissionStatus(String displayName) {
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
}