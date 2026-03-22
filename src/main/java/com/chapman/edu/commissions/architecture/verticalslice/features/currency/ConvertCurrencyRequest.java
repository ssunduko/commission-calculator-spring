package com.chapman.edu.commissions.architecture.verticalslice.features.currency;

import com.chapman.edu.commissions.architecture.verticalslice.infrastructure.exceptions.ValidationException;

/**
 * Request DTO for currency conversion.
 */
public record ConvertCurrencyRequest(
    String from,
    String to,
    double amount
) {
    public void validate() {
        if (from == null || from.isBlank() || from.length() != 3) {
            throw new ValidationException("'from' must be a 3-letter currency code (e.g. USD)");
        }
        if (to == null || to.isBlank() || to.length() != 3) {
            throw new ValidationException("'to' must be a 3-letter currency code (e.g. EUR)");
        }
        if (amount <= 0) {
            throw new ValidationException("'amount' must be a positive number");
        }
    }
}
