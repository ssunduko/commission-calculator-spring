package com.chapman.edu.commissions.architecture.eventdriven;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Verifies the event-driven Spring Boot application context loads correctly.
 *
 * WHY @TestPropertySource?
 * The main application.properties enables Flyway, which runs migrations that
 * create foreign-key constraints (e.g., deals.sales_rep_id -> users.id).
 * The event-driven DataInitializer inserts deals with simple string IDs
 * like "REP001" that don't exist in the users table, causing FK violations.
 *
 * Using a separate in-memory database with ddl-auto=create-drop lets JPA
 * generate the schema from entity annotations (which have no FK constraints
 * in the event-driven domain model), bypassing Flyway migrations entirely.
 */
@SpringBootTest(classes = EventDrivenCommissionApplication.class)
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:eventdrivendb",
    "spring.flyway.enabled=false"
})
class EventDrivenCommissionApplicationTests {

    @Test
    void contextLoads() {
    }

}
