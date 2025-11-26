package com.chapman.edu.commissions.verticalslice.features.disputes;

import com.chapman.edu.commissions.verticalslice.domain.DisputeStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Dispute Management.
 * Handles all HTTP requests related to disputes.
 */
@RestController
@RequestMapping("/api/disputes")
public class DisputeController {
    private final DisputeService disputeService;

    public DisputeController(DisputeService disputeService) {
        this.disputeService = disputeService;
    }

    @PostMapping
    public ResponseEntity<DisputeResponse> createDispute(@RequestBody CreateDisputeRequest request) {
        DisputeResponse response = disputeService.createDispute(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DisputeResponse> getDispute(@PathVariable String id) {
        DisputeResponse response = disputeService.getDispute(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<DisputeResponse>> getAllDisputes(
        @RequestParam(required = false) String salesRepId,
        @RequestParam(required = false) DisputeStatus status
    ) {
        List<DisputeResponse> disputes;

        if (salesRepId != null) {
            disputes = disputeService.getDisputesBySalesRep(salesRepId);
        } else if (status != null) {
            disputes = disputeService.getDisputesByStatus(status);
        } else {
            disputes = disputeService.getAllDisputes();
        }

        return ResponseEntity.ok(disputes);
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<DisputeResponse> resolveDispute(
        @PathVariable String id,
        @RequestBody ResolveDisputeRequest request
    ) {
        DisputeResponse response = disputeService.resolveDispute(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/escalate")
    public ResponseEntity<DisputeResponse> escalateDispute(@PathVariable String id) {
        DisputeResponse response = disputeService.escalateDispute(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDispute(@PathVariable String id) {
        disputeService.deleteDispute(id);
        return ResponseEntity.noContent().build();
    }
}
