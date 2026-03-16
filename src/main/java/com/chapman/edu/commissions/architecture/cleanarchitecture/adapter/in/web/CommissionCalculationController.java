package com.chapman.edu.commissions.architecture.cleanarchitecture.adapter.in.web;

import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.CalculateCommissionCommand;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.CalculationResult;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.port.in.CommissionCalculationUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/clean/calculations")
public class CommissionCalculationController {

    private final CommissionCalculationUseCase commissionCalculationUseCase;

    public CommissionCalculationController(CommissionCalculationUseCase commissionCalculationUseCase) {
        this.commissionCalculationUseCase = commissionCalculationUseCase;
    }

    @PostMapping
    public ResponseEntity<CalculationResult> calculateCommission(@RequestBody CalculateCommissionCommand command) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commissionCalculationUseCase.calculateCommission(command));
    }

    @GetMapping
    public ResponseEntity<List<CalculationResult>> getAllCalculations(
            @RequestParam(required = false) String dealId,
            @RequestParam(required = false) String salesRepId) {
        if (dealId != null) {
            return ResponseEntity.ok(commissionCalculationUseCase.getCalculationsByDeal(dealId));
        }
        if (salesRepId != null) {
            return ResponseEntity.ok(commissionCalculationUseCase.getCalculationsBySalesRep(salesRepId));
        }
        return ResponseEntity.ok(commissionCalculationUseCase.getAllCalculations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CalculationResult> getCalculation(@PathVariable String id) {
        return ResponseEntity.ok(commissionCalculationUseCase.getCalculation(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCalculation(@PathVariable String id) {
        commissionCalculationUseCase.deleteCalculation(id);
        return ResponseEntity.noContent().build();
    }
}
