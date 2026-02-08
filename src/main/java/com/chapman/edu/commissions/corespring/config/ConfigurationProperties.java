package com.chapman.edu.commissions.corespring.config;

import java.math.BigDecimal;

/**
 * POJO for holding configuration properties.
 * Demonstrates type-safe configuration management.
 */
public class ConfigurationProperties {
    private BigDecimal defaultCommissionRate;
    private BigDecimal bonusThreshold;
    private String applicationName;

    public BigDecimal getDefaultCommissionRate() {
        return defaultCommissionRate;
    }

    public void setDefaultCommissionRate(BigDecimal defaultCommissionRate) {
        this.defaultCommissionRate = defaultCommissionRate;
    }

    public BigDecimal getBonusThreshold() {
        return bonusThreshold;
    }

    public void setBonusThreshold(BigDecimal bonusThreshold) {
        this.bonusThreshold = bonusThreshold;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }
}
