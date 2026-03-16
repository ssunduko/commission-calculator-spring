package com.chapman.edu.commissions.architecture.orthogonal.aspects.auditing;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for querying the audit log.
 * Provides visibility into all command executions and their outcomes.
 */
@RestController
@RequestMapping("/api/orthogonal/audit-log")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    public AuditLogController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    public ResponseEntity<List<AuditLog>> getAllAuditEntries() {
        return ResponseEntity.ok(auditLogRepository.findAllByOrderByOccurredAtDesc());
    }

    @GetMapping("/operation/{operation}")
    public ResponseEntity<List<AuditLog>> getByOperation(@PathVariable String operation) {
        return ResponseEntity.ok(auditLogRepository.findByOperationOrderByOccurredAtDesc(operation));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<AuditLog>> getByStatus(@PathVariable String status) {
        return ResponseEntity.ok(auditLogRepository.findByStatusOrderByOccurredAtDesc(status));
    }
}
