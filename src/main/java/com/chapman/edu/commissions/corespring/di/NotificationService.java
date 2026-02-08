package com.chapman.edu.commissions.corespring.di;

import com.chapman.edu.commissions.model.CommissionCalculation;

/**
 * Interface for notification services.
 * Demonstrates Dependency Inversion Principle (depend on abstractions).
 */
public interface NotificationService {
    void notifyCommissionCalculated(CommissionCalculation calculation);
    void sendAlert(String message);
}
