package com.chapman.edu.commissions.architecture.microservice.planservice;

import com.chapman.edu.commissions.architecture.microservice.common.dto.AddRuleRequest;
import com.chapman.edu.commissions.architecture.microservice.common.dto.CreatePlanRequest;
import com.chapman.edu.commissions.architecture.microservice.common.dto.PlanDto;
import com.chapman.edu.commissions.architecture.microservice.planservice.domain.PlanStatus;
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
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @PostMapping
    public ResponseEntity<PlanDto> createPlan(@RequestBody CreatePlanRequest request) {
        return new ResponseEntity<>(planService.createPlan(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlanDto> getPlan(@PathVariable String id) {
        return ResponseEntity.ok(planService.getPlan(id));
    }

    @GetMapping
    public ResponseEntity<List<PlanDto>> getAllPlans(
            @RequestParam(required = false) PlanStatus status) {
        List<PlanDto> plans;
        if (status != null) {
            plans = planService.getPlansByStatus(status);
        } else {
            plans = planService.getAllPlans();
        }
        return ResponseEntity.ok(plans);
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<PlanDto> activatePlan(@PathVariable String id) {
        return ResponseEntity.ok(planService.activatePlan(id));
    }

    @PostMapping("/{id}/rules")
    public ResponseEntity<PlanDto> addRuleToPlan(
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
