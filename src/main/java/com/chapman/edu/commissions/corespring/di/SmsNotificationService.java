package com.chapman.edu.commissions.corespring.di;

import com.chapman.edu.commissions.model.CommissionCalculation;
import org.springframework.stereotype.Service;

/**
 * SMS implementation of NotificationService.
 * Demonstrates multiple implementations of the same interface.
 * Requires @Qualifier when injecting to specify which implementation to use.
 */
@Service("smsNotificationService")
public class SmsNotificationService implements NotificationService {

    @Override
    public void notifyCommissionCalculated(CommissionCalculation calculation) {
        System.out.println("SMS: Commission $" + calculation.getNetCommission() +
                         " calculated for deal " + calculation.getDealId());
    }

    @Override
    public void sendAlert(String message) {
        System.out.println("SMS ALERT: " + message);
    }
}
