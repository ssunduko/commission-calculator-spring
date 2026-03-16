package com.chapman.edu.commissions.architecture.ddd.interfaces.rest;

import com.chapman.edu.commissions.architecture.ddd.application.calculation.CommissionCalculationApplicationService;
import com.chapman.edu.commissions.architecture.ddd.application.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CONCEPT: Interface Layer (DDD)
 *
 * REST controller for Commission Calculation. Delegates calculation
 * orchestration to the CommissionCalculationApplicationService, which
 * in turn uses the domain-level CommissionCalculationService for the
 * actual business logic.
 */
@RestController
@RequestMapping("/api/ddd/calculations")
public class CommissionCalculationController {

    private final CommissionCalculationApplicationService calculationService;

    public CommissionCalculationController(CommissionCalculationApplicationService calculationService) {
        this.calculationService = calculationService;
    }

    @PostMapping
    public ResponseEntity<CommissionCalculationDto> calculateCommission(
            @RequestBody CalculateCommissionRequest request) {
        return new ResponseEntity<>(calculationService.calculateCommission(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommissionCalculationDto> getCalculation(@PathVariable String id) {
        return ResponseEntity.ok(calculationService.getCalculation(id));
    }

    @GetMapping
    public ResponseEntity<List<CommissionCalculationDto>> getAllCalculations(
            @RequestParam(required = false) String dealId,
            @RequestParam(required = false) String salesRepId) {
        if (dealId != null) return ResponseEntity.ok(calculationService.getCalculationsByDeal(dealId));
        if (salesRepId != null) return ResponseEntity.ok(calculationService.getCalculationsBySalesRep(salesRepId));
        return ResponseEntity.ok(calculationService.getAllCalculations());
    }
}
