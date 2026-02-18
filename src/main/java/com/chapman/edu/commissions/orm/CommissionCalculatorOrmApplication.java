package com.chapman.edu.commissions.orm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * ============================================================
 * SPRING BOOT APPLICATION: Commission Calculator ORM Module
 * ============================================================
 *
 * This module demonstrates Spring Boot ORM concepts:
 * 1. Spring Data JPA repositories and custom query methods
 * 2. Entity relationships, mapping strategies, and database design
 * 3. Database migration with Flyway
 * 4. Transaction management and isolation levels
 * 5. Caching strategies with Spring Cache
 *
 * @SpringBootApplication combines:
 * - @Configuration: Marks this as a configuration class
 * - @EnableAutoConfiguration: Auto-configures based on classpath
 * - @ComponentScan: Scans this package and sub-packages for Spring beans
 *
 * @EnableTransactionManagement: Enables Spring's annotation-driven
 * transaction management (@Transactional support).
 * Note: Spring Boot auto-configures this, but we include it explicitly
 * for educational clarity.
 */
@SpringBootApplication(
    scanBasePackages = "com.chapman.edu.commissions.orm"
)
@EnableTransactionManagement
public class CommissionCalculatorOrmApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommissionCalculatorOrmApplication.class, args);
    }
}
