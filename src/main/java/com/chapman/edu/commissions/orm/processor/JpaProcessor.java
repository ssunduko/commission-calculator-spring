package com.chapman.edu.commissions.orm.processor;

import com.chapman.edu.commissions.orm.entity.*;
import com.chapman.edu.commissions.orm.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * ============================================================
 * PROCESSOR: Spring Data JPA Repositories & Custom Query Methods
 * ============================================================
 *
 * This processor demonstrates the various query strategies available
 * in Spring Data JPA repositories. It runs on application startup
 * and exercises each query type with explanatory logging.
 *
 * SPRING DATA JPA QUERY STRATEGIES:
 *
 * 1. DERIVED QUERY METHODS (Query Creation from Method Names)
 *    Spring parses the method name and generates a JPQL/SQL query.
 *    Example: findByStatusAndValueGreaterThan -> WHERE status = ? AND value > ?
 *
 * 2. @Query with JPQL
 *    Write queries using Java Persistence Query Language.
 *    JPQL operates on entities and properties, not tables and columns.
 *    Example: @Query("SELECT d FROM Deal d WHERE d.salesRep.id = :repId")
 *
 * 3. @Query with Native SQL
 *    Write raw SQL when JPQL is insufficient.
 *    Useful for database-specific features (window functions, CTEs).
 *    Example: @Query(value = "SELECT * FROM deals WHERE ...", nativeQuery = true)
 *
 * 4. JPA Specifications (Criteria API)
 *    Build dynamic queries programmatically at runtime.
 *    Ideal for search forms with optional filter criteria.
 *    Example: spec.and(hasStatus(WON)).and(valueGreaterThan(10000))
 *
 * 5. Pagination and Sorting
 *    Built-in support via Pageable, Sort, and Page<T>.
 *    Example: findByStatus(OPEN, PageRequest.of(0, 10, Sort.by("value")))
 */
@Component
@Order(1)
public class JpaProcessor implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(JpaProcessor.class);

    private final UserRepository userRepository;
    private final DealRepository dealRepository;
    private final CommissionPlanRepository planRepository;
    private final CommissionCalculationRepository calculationRepository;

    public JpaProcessor(UserRepository userRepository,
                        DealRepository dealRepository,
                        CommissionPlanRepository planRepository,
                        CommissionCalculationRepository calculationRepository) {
        this.userRepository = userRepository;
        this.dealRepository = dealRepository;
        this.planRepository = planRepository;
        this.calculationRepository = calculationRepository;
    }

    @Override
    public void run(String... args) {
        log.info("============================================================");
        log.info("JPA PROCESSOR: Demonstrating Spring Data JPA Query Methods");
        log.info("============================================================");

        demonstrateDerivedQueries();
        demonstrateJpqlQueries();
        demonstratePagination();
        demonstrateSpecifications();
        demonstrateRelationshipQueries();

        log.info("============================================================");
        log.info("JPA PROCESSOR: Complete");
        log.info("============================================================");
    }

    private void demonstrateDerivedQueries() {
        log.info("");
        log.info("--- STRATEGY 1: Derived Query Methods ---");
        log.info("Spring generates SQL from method name keywords.");

        // findByUsername - simple property match
        userRepository.findByUsername("jsmith").ifPresent(user ->
                log.info("findByUsername('jsmith'): Found {}", user.getFullName()));

        // findByStatus - enum property match
        List<Deal> wonDeals = dealRepository.findByStatus(DealStatus.WON);
        log.info("findByStatus(WON): Found {} won deals", wonDeals.size());

        // findByValueGreaterThan - comparison
        List<Deal> bigDeals = dealRepository.findByValueGreaterThan(new BigDecimal("50000"));
        log.info("findByValueGreaterThan(50000): Found {} deals over $50K", bigDeals.size());

        // findByTerritoryAndActive - multiple conditions
        List<User> westCoastUsers = userRepository.findByTerritoryAndActive("West Coast", true);
        log.info("findByTerritoryAndActive('West Coast', true): Found {} users", westCoastUsers.size());

        // existsByUsername - existence check
        boolean exists = userRepository.existsByUsername("jsmith");
        log.info("existsByUsername('jsmith'): {}", exists);

        // findByStatusOrderByValueDesc - with ordering
        List<Deal> orderedDeals = dealRepository.findByStatusOrderByValueDesc(DealStatus.WON);
        log.info("findByStatusOrderByValueDesc(WON): {} deals sorted by value", orderedDeals.size());
    }

    private void demonstrateJpqlQueries() {
        log.info("");
        log.info("--- STRATEGY 2: JPQL Queries (@Query annotation) ---");
        log.info("JPQL operates on entity classes and properties, not tables/columns.");

        // Find users by role
        List<User> salesReps = userRepository.findActiveUsersByRole(UserRole.SALES_REP);
        log.info("findActiveUsersByRole(SALES_REP): Found {} sales reps", salesReps.size());

        // Find direct reports
        List<User> reports = userRepository.findDirectReportsByManagerId("usr-004");
        log.info("findDirectReportsByManagerId('usr-004'): Found {} direct reports", reports.size());

        // Search by name
        List<User> searchResults = userRepository.searchByName("john");
        log.info("searchByName('john'): Found {} results", searchResults.size());

        // Aggregate query - total deal value
        BigDecimal totalValue = dealRepository.calculateTotalValueBySalesRepAndStatus("usr-001", DealStatus.WON);
        log.info("calculateTotalValueBySalesRepAndStatus('usr-001', WON): ${}", totalValue);

        // Count by department
        List<Object[]> deptCounts = userRepository.countUsersByDepartment();
        for (Object[] row : deptCounts) {
            log.info("Department '{}': {} users", row[0], row[1]);
        }
    }

    private void demonstratePagination() {
        log.info("");
        log.info("--- STRATEGY 3: Pagination and Sorting ---");
        log.info("PageRequest.of(page, size, Sort) enables database-level pagination.");

        // Page 0, size 2, sorted by value descending
        Page<Deal> page1 = dealRepository.findBySalesRepId("usr-001",
                PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "value")));
        log.info("Page 1: {} deals (total: {}, pages: {})",
                page1.getContent().size(), page1.getTotalElements(), page1.getTotalPages());
        page1.getContent().forEach(d ->
                log.info("  - {} (${} )", d.getTitle(), d.getValue()));

        // Paginated active users
        Page<User> activeUsers = userRepository.findByActive(true, PageRequest.of(0, 10));
        log.info("Active users page: {} of {} total",
                activeUsers.getContent().size(), activeUsers.getTotalElements());
    }

    private void demonstrateSpecifications() {
        log.info("");
        log.info("--- STRATEGY 4: JPA Specifications (Dynamic Queries) ---");
        log.info("Specifications compose reusable predicates for flexible search.");

        // Single specification
        Specification<Deal> wonSpec = DealSpecifications.hasStatus(DealStatus.WON);
        List<Deal> wonDeals = dealRepository.findAll(wonSpec);
        log.info("hasStatus(WON): {} deals", wonDeals.size());

        // Combined specifications
        Specification<Deal> combinedSpec = DealSpecifications.hasStatus(DealStatus.WON)
                .and(DealSpecifications.valueGreaterThan(new BigDecimal("30000")));
        List<Deal> bigWonDeals = dealRepository.findAll(combinedSpec);
        log.info("hasStatus(WON) AND valueGreaterThan(30000): {} deals", bigWonDeals.size());

        // Specification with pagination
        Page<Deal> pagedResults = dealRepository.findAll(combinedSpec, PageRequest.of(0, 5));
        log.info("Combined spec with pagination: {} results", pagedResults.getTotalElements());
    }

    private void demonstrateRelationshipQueries() {
        log.info("");
        log.info("--- STRATEGY 5: Relationship & Entity Graph Queries ---");
        log.info("JOIN FETCH and @EntityGraph solve the N+1 problem.");

        // EntityGraph - loads products and salesRep in one query
        dealRepository.findWithProductsAndSalesRepById("deal-001").ifPresent(deal -> {
            log.info("EntityGraph loaded deal '{}' with {} products",
                    deal.getTitle(), deal.getProducts().size());
        });

        // JOIN FETCH in JPQL
        List<Deal> dealsWithProducts = dealRepository.findDealsWithProductsBySalesRepAndStatus(
                "usr-001", DealStatus.WON);
        log.info("JOIN FETCH query: {} deals with products loaded", dealsWithProducts.size());

        // Anti-join: won deals without calculations
        List<Deal> pending = dealRepository.findWonDealsWithoutCalculations();
        log.info("Won deals without calculations: {}", pending.size());

        // Commission summary by sales rep
        List<Object[]> summary = calculationRepository.getCommissionSummaryBySalesRep();
        for (Object[] row : summary) {
            log.info("Sales Rep '{}': {} calculations, total ${}",
                    row[1], row[2], row[3]);
        }
    }
}
