package com.chapman.edu.commissions.architecture.verticalslice.features.calculations;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Commission Calculation.
 * Handles all HTTP requests related to commission calculations.
 */
@RestController
@RequestMapping("/api/calculations")
public class CommissionCalculationController {
    private final CommissionCalculationService calculationService;

    public CommissionCalculationController(CommissionCalculationService calculationService) {
        this.calculationService = calculationService;
    }

    @PostMapping
    public ResponseEntity<CommissionCalculationResponse> calculateCommission(
        @RequestBody CalculateCommissionRequest request
    ) {
        CommissionCalculationResponse response = calculationService.calculateCommission(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommissionCalculationResponse> getCalculation(@PathVariable String id) {
        CommissionCalculationResponse response = calculationService.getCalculation(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<CommissionCalculationResponse>> getAllCalculations(
        @RequestParam(required = false) String dealId,
        @RequestParam(required = false) String salesRepId
    ) {
        List<CommissionCalculationResponse> calculations;

        if (dealId != null) {
            calculations = calculationService.getCalculationsByDeal(dealId);
        } else if (salesRepId != null) {
            calculations = calculationService.getCalculationsBySalesRep(salesRepId);
        } else {
            calculations = calculationService.getAllCalculations();
        }

        return ResponseEntity.ok(calculations);
    }
}
