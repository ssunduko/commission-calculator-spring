package com.chapman.edu.commissions.architecture.verticalslice.features.currency;

import com.chapman.edu.commissions.architecture.verticalslice.infrastructure.config.FeatureFlagMetrics;
import com.chapman.edu.commissions.architecture.verticalslice.infrastructure.config.Features;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.togglz.core.manager.FeatureManager;

/**
 * REST Controller for Currency Conversion.
 *
 * Exposes the external Currency MCP server's capabilities
 * through local REST endpoints. Gated by the CURRENCY_CONVERSION feature flag.
 */
@RestController
@RequestMapping("/api/currency")
public class CurrencyController {

    private final CurrencyConversionService currencyService;
    private final FeatureManager featureManager;
    private final FeatureFlagMetrics metrics;

    public CurrencyController(CurrencyConversionService currencyService,
                               FeatureManager featureManager,
                               FeatureFlagMetrics metrics) {
        this.currencyService = currencyService;
        this.featureManager = featureManager;
        this.metrics = metrics;
    }

    /**
     * Convert an amount between two currencies.
     * Example: POST /api/currency/convert { "from": "USD", "to": "EUR", "amount": 100 }
     */
    @PostMapping("/convert")
    public ResponseEntity<CurrencyConversionResponse> convert(@RequestBody ConvertCurrencyRequest request) {
        metrics.recordCheck(Features.CURRENCY_CONVERSION);
        if (!featureManager.isActive(Features.CURRENCY_CONVERSION)) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        return ResponseEntity.ok(currencyService.convertCurrency(request));
    }

    /**
     * Get latest exchange rates.
     * Example: GET /api/currency/rates?base=USD&symbols=EUR,GBP,JPY
     */
    @GetMapping("/rates")
    public ResponseEntity<ExchangeRateResponse> getLatestRates(
            @RequestParam(required = false) String base,
            @RequestParam(required = false) String symbols) {
        metrics.recordCheck(Features.CURRENCY_CONVERSION);
        if (!featureManager.isActive(Features.CURRENCY_CONVERSION)) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        return ResponseEntity.ok(currencyService.getLatestRates(
                new GetLatestRatesRequest(base, symbols)));
    }

    /**
     * List all supported currencies.
     * Example: GET /api/currency/supported
     */
    @GetMapping("/supported")
    public ResponseEntity<SupportedCurrenciesResponse> getSupportedCurrencies() {
        metrics.recordCheck(Features.CURRENCY_CONVERSION);
        if (!featureManager.isActive(Features.CURRENCY_CONVERSION)) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        return ResponseEntity.ok(currencyService.listSupportedCurrencies());
    }

    /**
     * Get historical exchange rates for a specific date.
     * Example: GET /api/currency/historical?date=2025-01-15&base=USD&symbols=EUR,GBP
     */
    @GetMapping("/historical")
    public ResponseEntity<ExchangeRateResponse> getHistoricalRates(
            @RequestParam String date,
            @RequestParam(required = false) String base,
            @RequestParam(required = false) String symbols) {
        metrics.recordCheck(Features.CURRENCY_CONVERSION);
        if (!featureManager.isActive(Features.CURRENCY_CONVERSION)) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        return ResponseEntity.ok(currencyService.getHistoricalRates(
                new GetHistoricalRatesRequest(date, base, symbols)));
    }
}

