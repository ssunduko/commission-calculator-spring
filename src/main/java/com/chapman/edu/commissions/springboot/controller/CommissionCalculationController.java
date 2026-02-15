package com.chapman.edu.commissions.springboot.controller;

import com.chapman.edu.commissions.model.CommissionCalculation;
import com.chapman.edu.commissions.springboot.dto.request.CalculateCommissionRequest;
import com.chapman.edu.commissions.springboot.dto.response.ApiResponse;
import com.chapman.edu.commissions.springboot.dto.response.CommissionCalculationResponse;
import com.chapman.edu.commissions.springboot.mapper.DtoMapper;
import com.chapman.edu.commissions.springboot.service.CommissionCalculationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for Commission Calculations.
 *
 * CONCEPT: Method-Level Security with @PreAuthorize
 * ---------------------------------------------------
 * @PreAuthorize enables fine-grained access control on individual methods.
 * It uses Spring Expression Language (SpEL) to evaluate access rules:
 *
 *   @PreAuthorize("hasRole('ADMIN')")           — User must have ADMIN role
 *   @PreAuthorize("hasAnyRole('ADMIN','MGR')")  — User must have ADMIN or MGR
 *   @PreAuthorize("#id == authentication.name")  — User can only access own data
 *   @PreAuthorize("isAuthenticated()")           — Any authenticated user
 *
 * This is enabled by @EnableMethodSecurity in SecurityConfig.
 */
@RestController
@RequestMapping("/api/calculations")
public class CommissionCalculationController {

    private final CommissionCalculationService calculationService;
    private final DtoMapper mapper;

    public CommissionCalculationController(CommissionCalculationService calculationService,
                                           DtoMapper mapper) {
        this.calculationService = calculationService;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CommissionCalculationResponse>>> getAllCalculations(
            @RequestParam(required = false) String salesRepId,
            @RequestParam(required = false) String dealId) {

        List<CommissionCalculation> calculations;
        if (salesRepId != null) {
            calculations = calculationService.getCalculationsBySalesRep(salesRepId);
        } else if (dealId != null) {
            calculations = calculationService.getCalculationsByDeal(dealId);
        } else {
            calculations = calculationService.getAllCalculations();
        }

        List<CommissionCalculationResponse> responses = calculations.stream()
                .map(mapper::toCommissionCalculationResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Calculations retrieved", responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CommissionCalculationResponse>> getCalculationById(
            @PathVariable String id) {
        CommissionCalculation calc = calculationService.getCalculationById(id);
        return ResponseEntity.ok(
            ApiResponse.success("Calculation retrieved", mapper.toCommissionCalculationResponse(calc)));
    }

    /**
     * POST /api/calculations — Calculate commission for a deal
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CommissionCalculationResponse>> calculateCommission(
            @Valid @RequestBody CalculateCommissionRequest request) {
        CommissionCalculation calc = calculationService.calculateCommission(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Commission calculated", mapper.toCommissionCalculationResponse(calc)));
    }

    /**
     * PATCH /api/calculations/{id}/approve — Approve a calculation
     *
     * @PreAuthorize restricts this to SALES_MANAGER and FINANCE_ADMIN roles only.
     */
    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'FINANCE_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<CommissionCalculationResponse>> approveCalculation(
            @PathVariable String id) {
        CommissionCalculation calc = calculationService.approveCalculation(id);
        return ResponseEntity.ok(
            ApiResponse.success("Calculation approved", mapper.toCommissionCalculationResponse(calc)));
    }

    /**
     * PATCH /api/calculations/{id}/pay — Mark a calculation as paid
     *
     * Only FINANCE_ADMIN and SYSTEM_ADMIN can process payments.
     */
    @PatchMapping("/{id}/pay")
    @PreAuthorize("hasAnyRole('FINANCE_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<CommissionCalculationResponse>> markAsPaid(
            @PathVariable String id) {
        CommissionCalculation calc = calculationService.markAsPaid(id);
        return ResponseEntity.ok(
            ApiResponse.success("Calculation marked as paid", mapper.toCommissionCalculationResponse(calc)));
    }
}
