package com.chapman.edu.commissions.orm.controller;

import com.chapman.edu.commissions.orm.entity.CommissionCalculation;
import com.chapman.edu.commissions.orm.entity.CommissionPlan;
import com.chapman.edu.commissions.orm.service.CommissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * ============================================================
 * REST CONTROLLER: CommissionController
 * ============================================================
 *
 * Exposes Commission calculation and plan management endpoints.
 * This is the primary controller for the Commission Calculator application.
 */
@RestController
@RequestMapping("/api/orm/commissions")
public class CommissionController {

    private final CommissionService commissionService;

    public CommissionController(CommissionService commissionService) {
        this.commissionService = commissionService;
    }

    // ============================================================
    // Commission Calculation Endpoints
    // ============================================================

    @GetMapping("/calculations/{id}")
    public ResponseEntity<CommissionCalculation> getCalculation(@PathVariable String id) {
        return commissionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/calculations/sales-rep/{salesRepId}")
    public ResponseEntity<List<CommissionCalculation>> getCalculationsBySalesRep(
            @PathVariable String salesRepId) {
        return ResponseEntity.ok(commissionService.findBySalesRep(salesRepId));
    }

    /**
     * Calculate commission for a deal.
     * This triggers the full commission calculation engine.
     */
    @PostMapping("/calculate")
    public ResponseEntity<CommissionCalculation> calculateCommission(
            @RequestParam String dealId,
            @RequestParam String planId) {
        CommissionCalculation calc = commissionService.calculateCommission(dealId, planId);
        return ResponseEntity.status(HttpStatus.CREATED).body(calc);
    }

    @PutMapping("/calculations/{id}/approve")
    public ResponseEntity<CommissionCalculation> approveCalculation(
            @PathVariable String id,
            @RequestParam String approvedBy) {
        return ResponseEntity.ok(commissionService.approveCalculation(id, approvedBy));
    }

    @PutMapping("/calculations/bulk-approve")
    public ResponseEntity<Map<String, Integer>> bulkApprove(
            @RequestParam String beforeDate) {
        int count = commissionService.bulkApproveCalculations(LocalDate.parse(beforeDate));
        return ResponseEntity.ok(Map.of("updatedCount", count));
    }

    @GetMapping("/sales-rep/{salesRepId}/total")
    public ResponseEntity<BigDecimal> getTotalCommissions(@PathVariable String salesRepId) {
        return ResponseEntity.ok(commissionService.getTotalCommissionsForSalesRep(salesRepId));
    }

    @GetMapping("/summary")
    public ResponseEntity<List<Object[]>> getCommissionSummary() {
        return ResponseEntity.ok(commissionService.getCommissionSummary());
    }

    // ============================================================
    // Commission Plan Endpoints
    // ============================================================

    @GetMapping("/plans/{id}")
    public ResponseEntity<CommissionPlan> getPlan(@PathVariable String id) {
        return commissionService.findPlanById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/plans/active")
    public ResponseEntity<List<CommissionPlan>> getActivePlans() {
        return ResponseEntity.ok(commissionService.findActivePlans());
    }

    @PostMapping("/plans")
    public ResponseEntity<CommissionPlan> createPlan(@RequestBody CommissionPlan plan) {
        CommissionPlan created = commissionService.createPlan(plan);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/plans/{id}/activate")
    public ResponseEntity<CommissionPlan> activatePlan(
            @PathVariable String id,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        CommissionPlan activated = commissionService.activatePlan(
                id, LocalDate.parse(startDate), LocalDate.parse(endDate));
        return ResponseEntity.ok(activated);
    }
}
