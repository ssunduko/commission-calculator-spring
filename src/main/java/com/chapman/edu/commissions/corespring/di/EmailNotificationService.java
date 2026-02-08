package com.chapman.edu.commissions.corespring.di;

import com.chapman.edu.commissions.model.CommissionCalculation;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Email implementation of NotificationService.
 * Demonstrates @Primary annotation to resolve ambiguity when multiple beans of same type exist.
 */
@Service("defaultNotificationService")
@Primary  // This bean will be preferred when multiple NotificationService beans exist
public class EmailNotificationService implements NotificationService {

    @Override
    public void notifyCommissionCalculated(CommissionCalculation calculation) {
        System.out.println("EMAIL: Commission calculated - ID: " + calculation.getId() +
                         ", Amount: $" + calculation.getNetCommission());
    }

    @Override
    public void sendAlert(String message) {
        System.out.println("EMAIL ALERT: " + message);
    }
}
