package com.chapman.edu.commissions.architecture.ddd.interfaces.rest;

import com.chapman.edu.commissions.architecture.ddd.application.dispute.DisputeApplicationService;
import com.chapman.edu.commissions.architecture.ddd.application.dto.*;
import com.chapman.edu.commissions.architecture.ddd.domain.dispute.DisputeStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CONCEPT: Interface Layer (DDD)
 *
 * REST controller for Dispute management. Translates HTTP requests into
 * application service calls and HTTP responses, keeping all dispute
 * business logic within the domain and application layers.
 */
@RestController
@RequestMapping("/api/ddd/disputes")
public class DisputeController {

    private final DisputeApplicationService disputeService;

    public DisputeController(DisputeApplicationService disputeService) {
        this.disputeService = disputeService;
    }

    @PostMapping
    public ResponseEntity<DisputeDto> createDispute(@RequestBody CreateDisputeRequest request) {
        return new ResponseEntity<>(disputeService.createDispute(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DisputeDto> getDispute(@PathVariable String id) {
        return ResponseEntity.ok(disputeService.getDispute(id));
    }

    @GetMapping
    public ResponseEntity<List<DisputeDto>> getAllDisputes(
            @RequestParam(required = false) String salesRepId,
            @RequestParam(required = false) DisputeStatus status) {
        if (salesRepId != null) return ResponseEntity.ok(disputeService.getDisputesBySalesRep(salesRepId));
        if (status != null) return ResponseEntity.ok(disputeService.getDisputesByStatus(status));
        return ResponseEntity.ok(disputeService.getAllDisputes());
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<DisputeDto> resolveDispute(
            @PathVariable String id,
            @RequestBody ResolveDisputeRequest request) {
        return ResponseEntity.ok(disputeService.resolveDispute(id, request));
    }

    @PostMapping("/{id}/escalate")
    public ResponseEntity<DisputeDto> escalateDispute(@PathVariable String id) {
        return ResponseEntity.ok(disputeService.escalateDispute(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDispute(@PathVariable String id) {
        disputeService.deleteDispute(id);
        return ResponseEntity.noContent().build();
    }
}
