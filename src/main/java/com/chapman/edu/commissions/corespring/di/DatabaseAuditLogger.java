package com.chapman.edu.commissions.corespring.di;

import org.springframework.stereotype.Component;

/**
 * Database implementation of AuditLogger.
 */
@Component("databaseAuditLogger")
public class DatabaseAuditLogger implements AuditLogger {

    @Override
    public void log(String message) {
        System.out.println("[DB AUDIT] " + message);
    }

    @Override
    public void logWithMetadata(String message, Object... metadata) {
        System.out.println("[DB AUDIT] " + message + " | Metadata: " + java.util.Arrays.toString(metadata));
    }
}
