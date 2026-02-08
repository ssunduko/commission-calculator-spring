package com.chapman.edu.commissions.corespring.di;

import com.chapman.edu.commissions.model.CommissionCalculation;
import com.chapman.edu.commissions.model.Deal;
import com.chapman.edu.commissions.corespring.annotations.Auditable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Service demonstrating Dependency Injection concepts in Spring.
 * Shows constructor injection (recommended), setter injection, and field injection.
 *
 * KEY CONCEPTS DEMONSTRATED:
 * 1. Constructor Injection (preferred method - immutable dependencies)
 * 2. @Autowired annotation (can be omitted on single constructor since Spring 4.3)
 * 3. @Qualifier for resolving multiple beans of same type
 * 4. @Service stereotype annotation for component scanning
 */
@Service
public class CommissionCalculationService {

    // Constructor injection - recommended for required dependencies
    private final CommissionRuleEngine ruleEngine;
    private final NotificationService notificationService;

    // Field injection - not recommended but shown for educational purposes
    @Autowired
    @Qualifier("emailAuditLogger")
    private AuditLogger auditLogger;

    // Setter injection - useful for optional dependencies
    private ValidationService validationService;

    /**
     * Constructor injection (RECOMMENDED)
     * - Makes dependencies explicit and required
     * - Enables immutability (final fields)
     * - Easier to test (can pass mocks in tests)
     * - @Autowired is optional on single constructor since Spring 4.3+
     */
    @Autowired  // Optional since Spring 4.3 when only one constructor exists
    public CommissionCalculationService(
            CommissionRuleEngine ruleEngine,
            @Qualifier("defaultNotificationService") NotificationService notificationService) {
        this.ruleEngine = ruleEngine;
        this.notificationService = notificationService;
    }

    /**
     * Setter injection (for optional dependencies)
     * - Allows optional dependencies with defaults
     * - Enables reconfiguration after construction
     * - Can lead to partially constructed objects (anti-pattern)
     */
    @Autowired(required = false)  // Optional dependency
    public void setValidationService(ValidationService validationService) {
        this.validationService = validationService;
    }

    /**
     * Calculate commission for a deal
     * Demonstrates custom annotation for AOP
     */
    @Auditable(action = "CALCULATE_COMMISSION", logParams = true)
    public CommissionCalculation calculateCommission(Deal deal, String planId) {
        // Validation using optional dependency
        if (validationService != null) {
            validationService.validateDeal(deal);
        }

        // Core business logic using required dependencies
        BigDecimal baseCommission = ruleEngine.calculateBaseCommission(deal, planId);

        CommissionCalculation calculation = new CommissionCalculation(
            deal.getId(),
            deal.getSalesRepId(),
            baseCommission
        );

        // Apply bonus rules
        BigDecimal bonusAmount = ruleEngine.calculateBonuses(deal, planId);
        calculation.setGrossCommission(baseCommission.add(bonusAmount));
        calculation.setNetCommission(calculation.getGrossCommission());

        // Audit the calculation
        auditLogger.log("Commission calculated for deal: " + deal.getId());

        // Notify stakeholders
        notificationService.notifyCommissionCalculated(calculation);

        return calculation;
    }

    /**
     * Demonstrates that field injection happens after construction
     */
    public String getDependencyStatus() {
        return String.format(
            "RuleEngine: %s, NotificationService: %s, AuditLogger: %s, ValidationService: %s",
            ruleEngine != null ? "injected" : "null",
            notificationService != null ? "injected" : "null",
            auditLogger != null ? "injected" : "null",
            validationService != null ? "injected" : "null"
        );
    }
}
