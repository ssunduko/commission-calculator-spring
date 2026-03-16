package com.chapman.edu.commissions.architecture.cleanarchitecture.adapter.in.web;

import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.CreateDisputeCommand;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.DisputeResult;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.ResolveDisputeCommand;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.port.in.DisputeUseCase;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.DisputeStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/clean/disputes")
public class DisputeController {

    private final DisputeUseCase disputeUseCase;

    public DisputeController(DisputeUseCase disputeUseCase) {
        this.disputeUseCase = disputeUseCase;
    }

    @PostMapping
    public ResponseEntity<DisputeResult> createDispute(@RequestBody CreateDisputeCommand command) {
        return ResponseEntity.status(HttpStatus.CREATED).body(disputeUseCase.createDispute(command));
    }

    @GetMapping
    public ResponseEntity<List<DisputeResult>> getAllDisputes(
            @RequestParam(required = false) String salesRepId,
            @RequestParam(required = false) DisputeStatus status) {
        if (salesRepId != null) {
            return ResponseEntity.ok(disputeUseCase.getDisputesBySalesRep(salesRepId));
        }
        if (status != null) {
            return ResponseEntity.ok(disputeUseCase.getDisputesByStatus(status));
        }
        return ResponseEntity.ok(disputeUseCase.getAllDisputes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DisputeResult> getDispute(@PathVariable String id) {
        return ResponseEntity.ok(disputeUseCase.getDispute(id));
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<DisputeResult> resolveDispute(@PathVariable String id, @RequestBody ResolveDisputeCommand command) {
        return ResponseEntity.ok(disputeUseCase.resolveDispute(id, command));
    }

    @PostMapping("/{id}/escalate")
    public ResponseEntity<DisputeResult> escalateDispute(@PathVariable String id) {
        return ResponseEntity.ok(disputeUseCase.escalateDispute(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDispute(@PathVariable String id) {
        disputeUseCase.deleteDispute(id);
        return ResponseEntity.noContent().build();
    }
}
