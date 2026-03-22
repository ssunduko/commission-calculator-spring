package com.chapman.edu.commissions.architecture.verticalslice.features.currency;

/**
 * Response DTO for exchange rate queries.
 */
public record ExchangeRateResponse(
    String baseCurrency,
    String rates
) {}
