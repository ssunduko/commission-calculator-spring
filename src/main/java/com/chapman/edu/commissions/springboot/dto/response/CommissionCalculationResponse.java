package com.chapman.edu.commissions.springboot.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response DTO for Commission Calculation data.
 */
public class CommissionCalculationResponse {
    private String id;
    private String dealId;
    private String salesRepId;
    private BigDecimal baseCommission;
    private BigDecimal grossCommission;
    private BigDecimal netCommission;
    private String status;
    private LocalDate calculationDate;
    private LocalDate payoutDate;
    private String planId;
    private String calculatedBy;
    private int bonusCount;
    private int acceleratorCount;

    // --- Getters and Setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDealId() { return dealId; }
    public void setDealId(String dealId) { this.dealId = dealId; }
    public String getSalesRepId() { return salesRepId; }
    public void setSalesRepId(String salesRepId) { this.salesRepId = salesRepId; }
    public BigDecimal getBaseCommission() { return baseCommission; }
    public void setBaseCommission(BigDecimal baseCommission) { this.baseCommission = baseCommission; }
    public BigDecimal getGrossCommission() { return grossCommission; }
    public void setGrossCommission(BigDecimal grossCommission) { this.grossCommission = grossCommission; }
    public BigDecimal getNetCommission() { return netCommission; }
    public void setNetCommission(BigDecimal netCommission) { this.netCommission = netCommission; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getCalculationDate() { return calculationDate; }
    public void setCalculationDate(LocalDate calculationDate) { this.calculationDate = calculationDate; }
    public LocalDate getPayoutDate() { return payoutDate; }
    public void setPayoutDate(LocalDate payoutDate) { this.payoutDate = payoutDate; }
    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }
    public String getCalculatedBy() { return calculatedBy; }
    public void setCalculatedBy(String calculatedBy) { this.calculatedBy = calculatedBy; }
    public int getBonusCount() { return bonusCount; }
    public void setBonusCount(int bonusCount) { this.bonusCount = bonusCount; }
    public int getAcceleratorCount() { return acceleratorCount; }
    public void setAcceleratorCount(int acceleratorCount) { this.acceleratorCount = acceleratorCount; }
}
