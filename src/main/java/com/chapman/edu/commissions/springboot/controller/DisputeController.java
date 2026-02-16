package com.chapman.edu.commissions.springboot.controller;

import com.chapman.edu.commissions.model.Dispute;
import com.chapman.edu.commissions.model.DisputeStatus;
import com.chapman.edu.commissions.springboot.dto.request.CreateDisputeRequest;
import com.chapman.edu.commissions.springboot.dto.response.ApiResponse;
import com.chapman.edu.commissions.springboot.dto.response.DisputeResponse;
import com.chapman.edu.commissions.springboot.mapper.DtoMapper;
import com.chapman.edu.commissions.springboot.service.DisputeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for Dispute management.
 */
@RestController
@RequestMapping("/api/disputes")
@Tag(name = "Disputes", description = "Dispute management — create, resolve, and escalate commission disputes")
public class DisputeController {

    private final DisputeService disputeService;
    private final DtoMapper mapper;

    public DisputeController(DisputeService disputeService, DtoMapper mapper) {
        this.disputeService = disputeService;
        this.mapper = mapper;
    }

    @Operation(summary = "List all disputes", description = "Retrieve all disputes, optionally filtered by sales rep ID or status")
    @GetMapping
    public ResponseEntity<ApiResponse<List<DisputeResponse>>> getAllDisputes(
            @Parameter(description = "Filter by sales representative ID") @RequestParam(required = false) String salesRepId,
            @Parameter(description = "Filter by dispute status (OPEN, UNDER_REVIEW, RESOLVED, ESCALATED)") @RequestParam(required = false) String status) {

        List<Dispute> disputes;
        if (salesRepId != null) {
            disputes = disputeService.getDisputesBySalesRep(salesRepId);
        } else if (status != null) {
            disputes = disputeService.getDisputesByStatus(DisputeStatus.valueOf(status));
        } else {
            disputes = disputeService.getAllDisputes();
        }

        List<DisputeResponse> responses = disputes.stream()
                .map(mapper::toDisputeResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Disputes retrieved", responses));
    }

    @Operation(summary = "Get dispute by ID", description = "Retrieve a specific dispute by its unique identifier")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DisputeResponse>> getDisputeById(
            @Parameter(description = "Dispute ID", example = "dispute-001") @PathVariable String id) {
        Dispute dispute = disputeService.getDisputeById(id);
        return ResponseEntity.ok(
            ApiResponse.success("Dispute retrieved", mapper.toDisputeResponse(dispute)));
    }

    @Operation(summary = "Create a new dispute", description = "File a new commission dispute against a calculation")
    @PostMapping
    public ResponseEntity<ApiResponse<DisputeResponse>> createDispute(
            @Valid @RequestBody CreateDisputeRequest request) {
        Dispute dispute = disputeService.createDispute(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Dispute created", mapper.toDisputeResponse(dispute)));
    }

    @Operation(summary = "Resolve a dispute", description = "Resolve an open dispute with a resolution message")
    @PatchMapping("/{id}/resolve")
    public ResponseEntity<ApiResponse<DisputeResponse>> resolveDispute(
            @Parameter(description = "Dispute ID", example = "dispute-001") @PathVariable String id,
            @Parameter(description = "Resolution description") @RequestParam String resolution,
            @Parameter(description = "User ID of the person resolving the dispute") @RequestParam String resolvedBy) {
        Dispute dispute = disputeService.resolveDispute(id, resolution, resolvedBy);
        return ResponseEntity.ok(
            ApiResponse.success("Dispute resolved", mapper.toDisputeResponse(dispute)));
    }

    @Operation(summary = "Escalate a dispute", description = "Escalate an open dispute for higher-level review")
    @PatchMapping("/{id}/escalate")
    public ResponseEntity<ApiResponse<DisputeResponse>> escalateDispute(
            @Parameter(description = "Dispute ID", example = "dispute-001") @PathVariable String id) {
        Dispute dispute = disputeService.escalateDispute(id);
        return ResponseEntity.ok(
            ApiResponse.success("Dispute escalated", mapper.toDisputeResponse(dispute)));
    }
}
