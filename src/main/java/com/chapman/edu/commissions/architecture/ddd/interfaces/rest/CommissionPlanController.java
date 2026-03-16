package com.chapman.edu.commissions.architecture.ddd.interfaces.rest;

import com.chapman.edu.commissions.architecture.ddd.application.dto.*;
import com.chapman.edu.commissions.architecture.ddd.application.plan.CommissionPlanApplicationService;
import com.chapman.edu.commissions.architecture.ddd.domain.plan.PlanStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CONCEPT: Interface Layer (DDD)
 *
 * REST controller for Commission Plan management. This thin controller
 * delegates all business logic to the CommissionPlanApplicationService,
 * keeping the interface layer free of domain concerns.
 */
@RestController
@RequestMapping("/api/ddd/plans")
public class CommissionPlanController {

    private final CommissionPlanApplicationService planService;

    public CommissionPlanController(CommissionPlanApplicationService planService) {
        this.planService = planService;
    }

    @PostMapping
    public ResponseEntity<CommissionPlanDto> createPlan(@RequestBody CreatePlanRequest request) {
        return new ResponseEntity<>(planService.createPlan(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommissionPlanDto> getPlan(@PathVariable String id) {
        return ResponseEntity.ok(planService.getPlan(id));
    }

    @GetMapping
    public ResponseEntity<List<CommissionPlanDto>> getAllPlans(
            @RequestParam(required = false) PlanStatus status) {
        if (status != null) return ResponseEntity.ok(planService.getPlansByStatus(status));
        return ResponseEntity.ok(planService.getAllPlans());
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<CommissionPlanDto> activatePlan(@PathVariable String id) {
        return ResponseEntity.ok(planService.activatePlan(id));
    }

    @PostMapping("/{id}/rules")
    public ResponseEntity<CommissionPlanDto> addRuleToPlan(
            @PathVariable String id,
            @RequestBody AddRuleRequest request) {
        return ResponseEntity.ok(planService.addRuleToPlan(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlan(@PathVariable String id) {
        planService.deletePlan(id);
        return ResponseEntity.noContent().build();
    }
}
