package com.chapman.edu.commissions.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a commission rule in the system.
 * Commission rules define the conditions and rates for calculating commissions.
 */
public class CommissionRule {
    private String id;
    private String name;
    private String description;
    private List<RuleCondition> conditions;
    private BigDecimal rate;
    private RuleType type;
    private int priority;
    private String planId;
    
    /**
     * Default constructor
     */
    public CommissionRule() {
        this.conditions = new ArrayList<>();
        this.rate = BigDecimal.ZERO;
        this.priority = 0;
        this.type = RuleType.STANDARD;
    }
    
    /**
     * Constructor with essential fields
     */
    public CommissionRule(String name, BigDecimal rate, RuleType type) {
        this();
        this.name = name;
        this.rate = rate;
        this.type = type;
    }
    
    // Getters and Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public List<RuleCondition> getConditions() {
        return conditions;
    }
    
    public void setConditions(List<RuleCondition> conditions) {
        this.conditions = conditions;
    }
    
    public void addCondition(RuleCondition condition) {
        this.conditions.add(condition);
    }
    
    public BigDecimal getRate() {
        return rate;
    }
    
    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }
    
    public RuleType getType() {
        return type;
    }
    
    public void setType(RuleType type) {
        this.type = type;
    }
    
    public int getPriority() {
        return priority;
    }
    
    public void setPriority(int priority) {
        this.priority = priority;
    }
    
    public String getPlanId() {
        return planId;
    }
    
    public void setPlanId(String planId) {
        this.planId = planId;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommissionRule that = (CommissionRule) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "CommissionRule{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", rate=" + rate +
                ", type=" + type +
                ", priority=" + priority +
                '}';
    }
    
    /**
     * Enum representing the types of commission rules.
     */
    public enum RuleType {
        STANDARD("Standard"),
        ACCELERATOR("Accelerator"),
        DECELERATOR("Decelerator"),
        BONUS("Bonus"),
        SPECIAL("Special");
        
        private final String displayName;
        
        RuleType(String displayName) {
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