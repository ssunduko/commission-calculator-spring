package com.chapman.edu.commissions.architecture.microservice.calculationservice;

import com.chapman.edu.commissions.architecture.microservice.common.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/calculations")
public class CalculationController {

    private final CalculationService calculationService;

    public CalculationController(CalculationService calculationService) {
        this.calculationService = calculationService;
    }

    @PostMapping
    public ResponseEntity<CalculationDto> calculate(@RequestBody CalculateCommissionRequest request) {
        return new ResponseEntity<>(calculationService.calculateCommission(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CalculationDto> getCalculation(@PathVariable String id) {
        return ResponseEntity.ok(calculationService.getCalculation(id));
    }

    @GetMapping
    public ResponseEntity<List<CalculationDto>> getAll(
            @RequestParam(required = false) String dealId,
            @RequestParam(required = false) String salesRepId) {
        if (dealId != null) return ResponseEntity.ok(calculationService.getCalculationsByDeal(dealId));
        if (salesRepId != null) return ResponseEntity.ok(calculationService.getCalculationsBySalesRep(salesRepId));
        return ResponseEntity.ok(calculationService.getAllCalculations());
    }
}
