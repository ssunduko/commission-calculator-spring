package com.chapman.edu.commissions.architecture.verticalslice.features.currency;

/**
 * Response DTO for currency conversion results.
 */
public record CurrencyConversionResponse(
    String from,
    String to,
    double amount,
    String result
) {}
