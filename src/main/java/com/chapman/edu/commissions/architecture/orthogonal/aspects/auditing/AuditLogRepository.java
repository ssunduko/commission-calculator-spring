package com.chapman.edu.commissions.architecture.orthogonal.aspects.auditing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repository for querying the audit log.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, String> {

    List<AuditLog> findByOperationOrderByOccurredAtDesc(String operation);

    List<AuditLog> findByStatusOrderByOccurredAtDesc(String status);

    List<AuditLog> findByOccurredAtBetweenOrderByOccurredAtDesc(Instant start, Instant end);

    List<AuditLog> findAllByOrderByOccurredAtDesc();
}
