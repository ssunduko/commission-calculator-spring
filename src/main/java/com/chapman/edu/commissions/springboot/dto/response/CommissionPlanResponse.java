package com.chapman.edu.commissions.springboot.dto.response;

import java.time.LocalDate;

/**
 * Response DTO for Commission Plan data.
 */
public class CommissionPlanResponse {
    private String id;
    private String name;
    private String currency;
    private String status;
    private LocalDate effectiveStartDate;
    private LocalDate effectiveEndDate;
    private LocalDate createdDate;
    private String createdBy;
    private int ruleCount;
    private int tierCount;
    private int bonusCount;

    // --- Getters and Setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getEffectiveStartDate() { return effectiveStartDate; }
    public void setEffectiveStartDate(LocalDate effectiveStartDate) { this.effectiveStartDate = effectiveStartDate; }
    public LocalDate getEffectiveEndDate() { return effectiveEndDate; }
    public void setEffectiveEndDate(LocalDate effectiveEndDate) { this.effectiveEndDate = effectiveEndDate; }
    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public int getRuleCount() { return ruleCount; }
    public void setRuleCount(int ruleCount) { this.ruleCount = ruleCount; }
    public int getTierCount() { return tierCount; }
    public void setTierCount(int tierCount) { this.tierCount = tierCount; }
    public int getBonusCount() { return bonusCount; }
    public void setBonusCount(int bonusCount) { this.bonusCount = bonusCount; }
}
