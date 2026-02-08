package com.chapman.edu.commissions.corespring.di;

import com.chapman.edu.commissions.model.Deal;
import org.springframework.stereotype.Service;

/**
 * Service for validating business objects.
 * Demonstrates optional dependency injection.
 */
@Service
public class ValidationService {

    public void validateDeal(Deal deal) {
        if (deal == null) {
            throw new IllegalArgumentException("Deal cannot be null");
        }
        if (deal.getValue() == null || deal.getValue().signum() <= 0) {
            throw new IllegalArgumentException("Deal value must be positive");
        }
        if (deal.getSalesRepId() == null || deal.getSalesRepId().isEmpty()) {
            throw new IllegalArgumentException("Sales rep ID is required");
        }
    }
}
