package com.chapman.edu.commissions.architecture.verticalslice.features.currency;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Currency Conversion.
 *
 * Exposes the external Currency MCP server's capabilities
 * through local REST endpoints.
 */
@RestController
@RequestMapping("/api/currency")
public class CurrencyController {

    private final CurrencyConversionService currencyService;

    public CurrencyController(CurrencyConversionService currencyService) {
        this.currencyService = currencyService;
    }

    /**
     * Convert an amount between two currencies.
     * Example: POST /api/currency/convert { "from": "USD", "to": "EUR", "amount": 100 }
     */
    @PostMapping("/convert")
    public ResponseEntity<CurrencyConversionResponse> convert(@RequestBody ConvertCurrencyRequest request) {
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
        return ResponseEntity.ok(currencyService.getLatestRates(
                new GetLatestRatesRequest(base, symbols)));
    }

    /**
     * List all supported currencies.
     * Example: GET /api/currency/supported
     */
    @GetMapping("/supported")
    public ResponseEntity<SupportedCurrenciesResponse> getSupportedCurrencies() {
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
        return ResponseEntity.ok(currencyService.getHistoricalRates(
                new GetHistoricalRatesRequest(date, base, symbols)));
    }
}
