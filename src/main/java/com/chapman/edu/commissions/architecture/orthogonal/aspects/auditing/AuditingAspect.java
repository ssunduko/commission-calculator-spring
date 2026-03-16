package com.chapman.edu.commissions.architecture.orthogonal.aspects.auditing;

import com.chapman.edu.commissions.architecture.orthogonal.pipeline.Command;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * CONCEPT: Auditing as an Orthogonal Concern
 *
 * Auditing records every command execution (state-changing operation)
 * to the audit_log table. This is distinct from logging:
 * - Logging is for developers (debug, troubleshoot)
 * - Auditing is for compliance (who changed what, when)
 *
 * This aspect only audits COMMANDS (writes), not QUERIES (reads),
 * because auditing read-only operations would be noisy and wasteful.
 *
 * @Order(3) means this runs AFTER logging and validation,
 * so only valid commands are audited.
 */
@Aspect
@Component
@Order(3)
public class AuditingAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditingAspect.class);

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditingAspect(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    @Around("execution(* com.chapman.edu.commissions.architecture.orthogonal.features..*.handle(..)) && args(command,..)")
    public Object auditCommand(ProceedingJoinPoint joinPoint, Object command) throws Throwable {
        // Only audit commands (state-changing operations), not queries
        if (!(command instanceof Command)) {
            return joinPoint.proceed();
        }

        String operation = command.getClass().getSimpleName();
        String handlerName = joinPoint.getTarget().getClass().getSimpleName();
        String inputData = serializeCommand(command);

        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            AuditLog entry = new AuditLog(operation, handlerName, inputData,
                    "SUCCESS", null, duration);
            auditLogRepository.save(entry);

            log.debug("[AUDIT] Recorded: {} via {} ({}ms)", operation, handlerName, duration);
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;

            AuditLog entry = new AuditLog(operation, handlerName, inputData,
                    "FAILURE", e.getMessage(), duration);
            auditLogRepository.save(entry);

            log.debug("[AUDIT] Recorded failure: {} via {} — {}", operation, handlerName, e.getMessage());
            throw e;
        }
    }

    private String serializeCommand(Object command) {
        try {
            return objectMapper.writeValueAsString(command);
        } catch (JsonProcessingException e) {
            return command.toString();
        }
    }
}
