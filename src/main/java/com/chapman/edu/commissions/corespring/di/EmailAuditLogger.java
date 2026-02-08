package com.chapman.edu.commissions.corespring.di;

import org.springframework.stereotype.Component;

/**
 * Email implementation of AuditLogger.
 * Used with @Qualifier to demonstrate resolving multiple bean candidates.
 */
@Component("emailAuditLogger")
public class EmailAuditLogger implements AuditLogger {

    @Override
    public void log(String message) {
        System.out.println("[EMAIL AUDIT] " + message);
    }

    @Override
    public void logWithMetadata(String message, Object... metadata) {
        System.out.println("[EMAIL AUDIT] " + message + " | Metadata: " + java.util.Arrays.toString(metadata));
    }
}
