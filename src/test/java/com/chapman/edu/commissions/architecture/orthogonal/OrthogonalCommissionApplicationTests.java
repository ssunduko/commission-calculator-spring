package com.chapman.edu.commissions.architecture.orthogonal;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Verifies the orthogonal architecture Spring Boot application context loads correctly.
 *
 * WHY @TestPropertySource?
 * The main application.properties enables Flyway, which runs migrations that
 * create foreign-key constraints (e.g., deals.sales_rep_id -> users.id).
 * The orthogonal DataInitializer inserts deals with IDs like "orth_rep001"
 * that don't exist in the users table, causing FK violations.
 *
 * Using a separate in-memory database with ddl-auto=create-drop lets JPA
 * generate the schema from entity annotations (which have no FK constraints
 * in the orthogonal domain model), bypassing Flyway migrations entirely.
 */
@SpringBootTest(classes = OrthogonalCommissionApplication.class)
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:orthogonaldb",
    "spring.flyway.enabled=false"
})
class OrthogonalCommissionApplicationTests {

    @Test
    void contextLoads() {
    }

}
