package com.chapman.edu.commissions.orm.controller;

import com.chapman.edu.commissions.orm.entity.Dispute;
import com.chapman.edu.commissions.orm.entity.DisputeStatus;
import com.chapman.edu.commissions.orm.service.DisputeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ============================================================
 * REST CONTROLLER: DisputeController
 * ============================================================
 *
 * Exposes Dispute management endpoints.
 */
@RestController
@RequestMapping("/api/orm/disputes")
public class DisputeController {

    private final DisputeService disputeService;

    public DisputeController(DisputeService disputeService) {
        this.disputeService = disputeService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Dispute> getDispute(@PathVariable String id) {
        return disputeService.findByIdWithComments(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<Dispute>> getDisputesByStatus(
            @PathVariable DisputeStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                disputeService.findByStatus(status, PageRequest.of(page, size)));
    }

    @GetMapping("/sales-rep/{salesRepId}")
    public ResponseEntity<List<Dispute>> getDisputesBySalesRep(@PathVariable String salesRepId) {
        return ResponseEntity.ok(disputeService.findBySalesRep(salesRepId));
    }

    @GetMapping("/manager/{managerId}/open")
    public ResponseEntity<List<Dispute>> getOpenDisputesForManager(@PathVariable String managerId) {
        return ResponseEntity.ok(disputeService.findOpenDisputesForManager(managerId));
    }

    @PostMapping
    public ResponseEntity<Dispute> fileDispute(
            @RequestParam String calculationId,
            @RequestParam String salesRepId,
            @RequestParam String title,
            @RequestParam String description) {
        Dispute dispute = disputeService.fileDispute(calculationId, salesRepId, title, description);
        return ResponseEntity.status(HttpStatus.CREATED).body(dispute);
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<Dispute> resolveDispute(
            @PathVariable String id,
            @RequestParam String resolution,
            @RequestParam String resolvedBy,
            @RequestParam boolean approved) {
        return ResponseEntity.ok(
                disputeService.resolveDispute(id, resolution, resolvedBy, approved));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<Dispute> addComment(
            @PathVariable String id,
            @RequestParam String userId,
            @RequestParam String userName,
            @RequestParam String text) {
        return ResponseEntity.ok(
                disputeService.addComment(id, userId, userName, text));
    }

    @GetMapping("/status-counts")
    public ResponseEntity<List<Object[]>> getStatusCounts() {
        return ResponseEntity.ok(disputeService.getDisputeStatusCounts());
    }
}
