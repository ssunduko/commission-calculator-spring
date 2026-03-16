package com.chapman.edu.commissions.architecture.orthogonal.aspects.auditing;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * CONCEPT: Audit Log Entity
 *
 * Records every command (state-changing operation) that passes through
 * the pipeline. This provides a complete audit trail of WHO did WHAT
 * and WHEN, independent of the business logic.
 *
 * Unlike the event store in event-driven architecture (which stores
 * domain events), the audit log stores OPERATIONS — it records the
 * intent (command) rather than the outcome (event).
 */
@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "operation", nullable = false)
    private String operation;

    @Column(name = "handler_name", nullable = false)
    private String handlerName;

    @Column(name = "input_data", columnDefinition = "TEXT")
    private String inputData;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "duration_ms")
    private long durationMs;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected AuditLog() {}

    public AuditLog(String operation, String handlerName, String inputData,
                    String status, String errorMessage, long durationMs) {
        this.operation = operation;
        this.handlerName = handlerName;
        this.inputData = inputData;
        this.status = status;
        this.errorMessage = errorMessage;
        this.durationMs = durationMs;
        this.occurredAt = Instant.now();
    }

    public String getId() { return id; }
    public String getOperation() { return operation; }
    public String getHandlerName() { return handlerName; }
    public String getInputData() { return inputData; }
    public String getStatus() { return status; }
    public String getErrorMessage() { return errorMessage; }
    public long getDurationMs() { return durationMs; }
    public Instant getOccurredAt() { return occurredAt; }
}
