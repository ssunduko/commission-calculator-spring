package com.chapman.edu.commissions.model;

import java.util.Objects;

/**
 * Represents a condition for a commission rule.
 * Rule conditions define when a commission rule should be applied.
 */
public class RuleCondition {
    private String id;
    private String field;
    private ConditionOperator operator;
    private String value;
    private LogicalOperator logicalOperator;
    private String ruleId;
    
    /**
     * Default constructor
     */
    public RuleCondition() {
        this.logicalOperator = LogicalOperator.AND;
    }
    
    /**
     * Constructor with essential fields
     */
    public RuleCondition(String field, ConditionOperator operator, String value) {
        this();
        this.field = field;
        this.operator = operator;
        this.value = value;
    }
    
    // Getters and Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getField() {
        return field;
    }
    
    public void setField(String field) {
        this.field = field;
    }
    
    public ConditionOperator getOperator() {
        return operator;
    }
    
    public void setOperator(ConditionOperator operator) {
        this.operator = operator;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
    public LogicalOperator getLogicalOperator() {
        return logicalOperator;
    }
    
    public void setLogicalOperator(LogicalOperator logicalOperator) {
        this.logicalOperator = logicalOperator;
    }
    
    public String getRuleId() {
        return ruleId;
    }
    
    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RuleCondition that = (RuleCondition) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "RuleCondition{" +
                "field='" + field + '\'' +
                ", operator=" + operator +
                ", value='" + value + '\'' +
                ", logicalOperator=" + logicalOperator +
                '}';
    }
    
    /**
     * Enum representing the operators for rule conditions.
     */
    public enum ConditionOperator {
        EQUALS("Equals"),
        NOT_EQUALS("Not Equals"),
        GREATER_THAN("Greater Than"),
        LESS_THAN("Less Than"),
        GREATER_THAN_OR_EQUALS("Greater Than or Equals"),
        LESS_THAN_OR_EQUALS("Less Than or Equals"),
        CONTAINS("Contains"),
        STARTS_WITH("Starts With"),
        ENDS_WITH("Ends With"),
        IN("In"),
        NOT_IN("Not In");
        
        private final String displayName;
        
        ConditionOperator(String displayName) {
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
    
    /**
     * Enum representing the logical operators for combining rule conditions.
     */
    public enum LogicalOperator {
        AND("And"),
        OR("Or");
        
        private final String displayName;
        
        LogicalOperator(String displayName) {
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