package com.chapman.edu.commissions.orm.processor;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * ============================================================
 * PROCESSOR: Database Migration with Flyway
 * ============================================================
 *
 * This processor demonstrates Flyway database migration concepts
 * by inspecting the migration history after Flyway has run.
 *
 * ============================================================
 * WHAT IS DATABASE MIGRATION?
 * ============================================================
 * Database migration is the process of managing incremental,
 * reversible changes to a database schema. Instead of manually
 * executing SQL scripts, migration tools:
 *
 * 1. Track which migrations have been applied
 * 2. Apply pending migrations in the correct order
 * 3. Prevent re-application of already-applied migrations
 * 4. Detect tampering with applied migration scripts (checksums)
 *
 * ============================================================
 * FLYWAY vs. LIQUIBASE
 * ============================================================
 *
 * FLYWAY:
 * - SQL-based migrations (write raw SQL)
 * - Simple naming convention: V1__description.sql
 * - Lightweight and easy to learn
 * - Best for: Teams comfortable with SQL
 * - Limitation: Migrations are forward-only (no built-in rollback for free tier)
 *
 * LIQUIBASE:
 * - XML/YAML/JSON-based changelogs (database-agnostic)
 * - Automatic rollback generation
 * - Diff tool to compare schemas
 * - Best for: Multi-database support, complex enterprise environments
 * - Limitation: More verbose, steeper learning curve
 *
 * ============================================================
 * FLYWAY MIGRATION LIFECYCLE
 * ============================================================
 *
 * 1. Application starts
 * 2. Flyway checks flyway_schema_history table
 * 3. Scans classpath:db/migration for migration files
 * 4. Compares applied vs. available migrations
 * 5. Applies new migrations in version order
 * 6. Records each migration in flyway_schema_history:
 *    - Version, description, type, script, checksum, installed_by,
 *      installed_on, execution_time, success
 *
 * ============================================================
 * NAMING CONVENTIONS
 * ============================================================
 *
 * Versioned: V{version}__{description}.sql
 *   V1__create_tables.sql       (applied once, never modified)
 *   V2__seed_data.sql           (applied once)
 *   V1.1__add_column.sql        (sub-versions supported)
 *
 * Repeatable: R__{description}.sql
 *   R__create_views.sql         (re-applied when checksum changes)
 *   R__stored_procedures.sql    (useful for views, functions)
 *
 * ============================================================
 * BEST PRACTICES
 * ============================================================
 *
 * 1. NEVER modify an already-applied migration
 *    - Flyway checksums will detect the change and fail
 *    - Create a new migration instead (e.g., V3__fix_column_type.sql)
 *
 * 2. Keep migrations small and focused
 *    - One logical change per migration
 *    - Easier to understand, test, and troubleshoot
 *
 * 3. Use meaningful descriptions in filenames
 *    - V4__add_email_index.sql (good)
 *    - V4__changes.sql (bad)
 *
 * 4. Test against production-like data
 *    - Migrations that work on empty tables may fail on tables with data
 *    - ALTER TABLE with NOT NULL needs a DEFAULT for existing rows
 *
 * 5. Separate DDL and DML migrations
 *    - Schema changes (CREATE, ALTER) in one migration
 *    - Data changes (INSERT, UPDATE) in another
 *
 * 6. In production, ALWAYS use Flyway (not hibernate.ddl-auto)
 *    - ddl-auto=create-drop: Destroys data on restart!
 *    - ddl-auto=update: May not handle all schema changes correctly
 *    - ddl-auto=validate: Use with Flyway to verify schema matches entities
 */
@Component
@Order(3)
public class MigrationProcessor implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(MigrationProcessor.class);

    private final Flyway flyway;

    public MigrationProcessor(Flyway flyway) {
        this.flyway = flyway;
    }

    @Override
    public void run(String... args) {
        log.info("============================================================");
        log.info("MIGRATION PROCESSOR: Flyway Database Migration Status");
        log.info("============================================================");

        demonstrateMigrationInfo();

        log.info("============================================================");
        log.info("MIGRATION PROCESSOR: Complete");
        log.info("============================================================");
    }

    private void demonstrateMigrationInfo() {
        log.info("");
        log.info("--- Flyway Migration History ---");
        log.info("Flyway tracks all applied migrations in 'flyway_schema_history'.");

        MigrationInfoService infoService = flyway.info();

        // Show all migrations (applied + pending)
        MigrationInfo[] allMigrations = infoService.all();
        log.info("Total migrations found: {}", allMigrations.length);

        for (MigrationInfo info : allMigrations) {
            log.info("  Migration: {} | Description: '{}' | State: {} | Type: {}",
                    info.getVersion(),
                    info.getDescription(),
                    info.getState(),
                    info.getType());
            if (info.getInstalledOn() != null) {
                log.info("    Applied on: {} | Execution time: {}ms",
                        info.getInstalledOn(), info.getExecutionTime());
            }
        }

        // Show pending migrations
        MigrationInfo[] pending = infoService.pending();
        if (pending.length > 0) {
            log.info("Pending migrations: {}", pending.length);
            for (MigrationInfo p : pending) {
                log.info("  PENDING: {} - {}", p.getVersion(), p.getDescription());
            }
        } else {
            log.info("No pending migrations - database schema is up to date!");
        }

        // Show current version
        MigrationInfo current = infoService.current();
        if (current != null) {
            log.info("Current schema version: {} ({})", current.getVersion(), current.getDescription());
        }

        log.info("");
        log.info("--- Configuration ---");
        log.info("Migration locations: {}", (Object) flyway.getConfiguration().getLocations());
        log.info("Schema history table: {}", flyway.getConfiguration().getTable());
        log.info("Baseline on migrate: {}", flyway.getConfiguration().isBaselineOnMigrate());
    }
}
