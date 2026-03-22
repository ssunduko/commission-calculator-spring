package com.chapman.edu.commissions.architecture.verticalslice.features.currency;

/**
 * Request DTO for fetching latest exchange rates.
 */
public record GetLatestRatesRequest(
    String base,
    String symbols
) {}
