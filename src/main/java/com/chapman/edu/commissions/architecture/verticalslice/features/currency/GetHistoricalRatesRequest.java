package com.chapman.edu.commissions.architecture.verticalslice.features.currency;

/**
 * Request DTO for fetching historical exchange rates.
 */
public record GetHistoricalRatesRequest(
    String date,
    String base,
    String symbols
) {}
