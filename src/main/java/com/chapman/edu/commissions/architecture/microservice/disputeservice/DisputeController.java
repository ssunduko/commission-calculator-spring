package com.chapman.edu.commissions.architecture.microservice.disputeservice;

import com.chapman.edu.commissions.architecture.microservice.common.dto.CreateDisputeRequest;
import com.chapman.edu.commissions.architecture.microservice.common.dto.DisputeDto;
import com.chapman.edu.commissions.architecture.microservice.common.dto.ResolveDisputeRequest;
import com.chapman.edu.commissions.architecture.microservice.disputeservice.domain.DisputeStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/disputes")
public class DisputeController {

    private final DisputeService disputeService;

    public DisputeController(DisputeService disputeService) {
        this.disputeService = disputeService;
    }

    @PostMapping
    public ResponseEntity<DisputeDto> createDispute(@RequestBody CreateDisputeRequest request) {
        return ResponseEntity.ok(disputeService.createDispute(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DisputeDto> getDispute(@PathVariable String id) {
        return ResponseEntity.ok(disputeService.getDispute(id));
    }

    @GetMapping
    public ResponseEntity<List<DisputeDto>> getAllDisputes(
            @RequestParam(required = false) String salesRepId,
            @RequestParam(required = false) String status) {
        if (salesRepId != null) {
            return ResponseEntity.ok(disputeService.getDisputesBySalesRep(salesRepId));
        }
        if (status != null) {
            return ResponseEntity.ok(disputeService.getDisputesByStatus(DisputeStatus.valueOf(status)));
        }
        return ResponseEntity.ok(disputeService.getAllDisputes());
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<DisputeDto> resolveDispute(@PathVariable String id, @RequestBody ResolveDisputeRequest request) {
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
