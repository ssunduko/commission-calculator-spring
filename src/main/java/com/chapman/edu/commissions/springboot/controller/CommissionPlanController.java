package com.chapman.edu.commissions.springboot.controller;

import com.chapman.edu.commissions.model.CommissionPlan;
import com.chapman.edu.commissions.springboot.dto.request.CreatePlanRequest;
import com.chapman.edu.commissions.springboot.dto.response.ApiResponse;
import com.chapman.edu.commissions.springboot.dto.response.CommissionPlanResponse;
import com.chapman.edu.commissions.springboot.mapper.DtoMapper;
import com.chapman.edu.commissions.springboot.service.CommissionPlanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for Commission Plan management.
 *
 * Endpoints:
 *   GET    /api/plans          — List all plans
 *   GET    /api/plans/{id}     — Get plan by ID
 *   GET    /api/plans/active   — List active plans
 *   POST   /api/plans          — Create a new plan
 *   PATCH  /api/plans/{id}/activate — Activate a plan
 *   PATCH  /api/plans/{id}/archive  — Archive a plan
 *   DELETE /api/plans/{id}     — Delete a plan
 */
@RestController
@RequestMapping("/api/plans")
public class CommissionPlanController {

    private final CommissionPlanService planService;
    private final DtoMapper mapper;

    public CommissionPlanController(CommissionPlanService planService, DtoMapper mapper) {
        this.planService = planService;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CommissionPlanResponse>>> getAllPlans() {
        List<CommissionPlanResponse> responses = planService.getAllPlans().stream()
                .map(mapper::toCommissionPlanResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Plans retrieved successfully", responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CommissionPlanResponse>> getPlanById(@PathVariable String id) {
        CommissionPlan plan = planService.getPlanById(id);
        return ResponseEntity.ok(
            ApiResponse.success("Plan retrieved successfully", mapper.toCommissionPlanResponse(plan)));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<CommissionPlanResponse>>> getActivePlans() {
        List<CommissionPlanResponse> responses = planService.getActivePlans().stream()
                .map(mapper::toCommissionPlanResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Active plans retrieved", responses));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CommissionPlanResponse>> createPlan(
            @Valid @RequestBody CreatePlanRequest request) {
        CommissionPlan plan = planService.createPlan(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Plan created successfully", mapper.toCommissionPlanResponse(plan)));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<CommissionPlanResponse>> activatePlan(@PathVariable String id) {
        CommissionPlan plan = planService.activatePlan(id);
        return ResponseEntity.ok(
            ApiResponse.success("Plan activated successfully", mapper.toCommissionPlanResponse(plan)));
    }

    @PatchMapping("/{id}/archive")
    public ResponseEntity<ApiResponse<CommissionPlanResponse>> archivePlan(@PathVariable String id) {
        CommissionPlan plan = planService.archivePlan(id);
        return ResponseEntity.ok(
            ApiResponse.success("Plan archived successfully", mapper.toCommissionPlanResponse(plan)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlan(@PathVariable String id) {
        planService.deletePlan(id);
        return ResponseEntity.noContent().build();
    }
}
