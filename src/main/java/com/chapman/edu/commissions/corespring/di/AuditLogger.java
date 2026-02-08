package com.chapman.edu.commissions.corespring.di;

/**
 * Interface for audit logging.
 * Demonstrates Dependency Inversion Principle.
 */
public interface AuditLogger {
    void log(String message);
    void logWithMetadata(String message, Object... metadata);
}
