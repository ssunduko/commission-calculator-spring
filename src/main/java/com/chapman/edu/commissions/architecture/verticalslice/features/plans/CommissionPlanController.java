package com.chapman.edu.commissions.verticalslice.features.plans;

import com.chapman.edu.commissions.verticalslice.domain.PlanStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Commission Plan Management.
 * Handles all HTTP requests related to commission plans.
 */
@RestController
@RequestMapping("/api/plans")
public class CommissionPlanController {
    private final CommissionPlanService planService;

    public CommissionPlanController(CommissionPlanService planService) {
        this.planService = planService;
    }

    @PostMapping
    public ResponseEntity<CommissionPlanResponse> createPlan(@RequestBody CreateCommissionPlanRequest request) {
        CommissionPlanResponse response = planService.createPlan(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommissionPlanResponse> getPlan(@PathVariable String id) {
        CommissionPlanResponse response = planService.getPlan(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<CommissionPlanResponse>> getAllPlans(
        @RequestParam(required = false) PlanStatus status
    ) {
        List<CommissionPlanResponse> plans;

        if (status != null) {
            plans = planService.getPlansByStatus(status);
        } else {
            plans = planService.getAllPlans();
        }

        return ResponseEntity.ok(plans);
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<CommissionPlanResponse> activatePlan(@PathVariable String id) {
        CommissionPlanResponse response = planService.activatePlan(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/rules")
    public ResponseEntity<CommissionPlanResponse> addRuleToPlan(
        @PathVariable String id,
        @RequestBody AddRuleToPlanRequest request
    ) {
        CommissionPlanResponse response = planService.addRuleToPlan(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlan(@PathVariable String id) {
        planService.deletePlan(id);
        return ResponseEntity.noContent().build();
    }
}
