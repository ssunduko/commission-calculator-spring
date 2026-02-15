package com.chapman.edu.commissions.springboot.controller;

import com.chapman.edu.commissions.model.Dispute;
import com.chapman.edu.commissions.model.DisputeStatus;
import com.chapman.edu.commissions.springboot.dto.request.CreateDisputeRequest;
import com.chapman.edu.commissions.springboot.dto.response.ApiResponse;
import com.chapman.edu.commissions.springboot.dto.response.DisputeResponse;
import com.chapman.edu.commissions.springboot.mapper.DtoMapper;
import com.chapman.edu.commissions.springboot.service.DisputeService;
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
public class DisputeController {

    private final DisputeService disputeService;
    private final DtoMapper mapper;

    public DisputeController(DisputeService disputeService, DtoMapper mapper) {
        this.disputeService = disputeService;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DisputeResponse>>> getAllDisputes(
            @RequestParam(required = false) String salesRepId,
            @RequestParam(required = false) String status) {

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

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DisputeResponse>> getDisputeById(@PathVariable String id) {
        Dispute dispute = disputeService.getDisputeById(id);
        return ResponseEntity.ok(
            ApiResponse.success("Dispute retrieved", mapper.toDisputeResponse(dispute)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DisputeResponse>> createDispute(
            @Valid @RequestBody CreateDisputeRequest request) {
        Dispute dispute = disputeService.createDispute(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Dispute created", mapper.toDisputeResponse(dispute)));
    }

    @PatchMapping("/{id}/resolve")
    public ResponseEntity<ApiResponse<DisputeResponse>> resolveDispute(
            @PathVariable String id,
            @RequestParam String resolution,
            @RequestParam String resolvedBy) {
        Dispute dispute = disputeService.resolveDispute(id, resolution, resolvedBy);
        return ResponseEntity.ok(
            ApiResponse.success("Dispute resolved", mapper.toDisputeResponse(dispute)));
    }

    @PatchMapping("/{id}/escalate")
    public ResponseEntity<ApiResponse<DisputeResponse>> escalateDispute(@PathVariable String id) {
        Dispute dispute = disputeService.escalateDispute(id);
        return ResponseEntity.ok(
            ApiResponse.success("Dispute escalated", mapper.toDisputeResponse(dispute)));
    }
}
