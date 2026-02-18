package com.chapman.edu.commissions.orm.repository;

import com.chapman.edu.commissions.orm.entity.CommissionPlan;
import com.chapman.edu.commissions.orm.entity.PlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * ============================================================
 * SPRING DATA JPA REPOSITORY: CommissionPlanRepository
 * ============================================================
 *
 * QUERY METHOD FEATURES DEMONSTRATED:
 * - Date range queries
 * - JOIN FETCH for aggregate loading
 * - Derived methods with multiple conditions
 * - Optional return types for single results
 */
@Repository
public interface CommissionPlanRepository extends JpaRepository<CommissionPlan, String> {

    /**
     * Find plans by status.
     */
    List<CommissionPlan> findByStatus(PlanStatus status);

    /**
     * Find the active plan for a given date.
     * A plan is active when:
     *   - status = ACTIVE
     *   - effectiveStartDate <= date
     *   - effectiveEndDate >= date OR effectiveEndDate IS NULL
     */
    @Query("SELECT p FROM CommissionPlan p WHERE p.status = 'ACTIVE' " +
           "AND p.effectiveStartDate <= :date " +
           "AND (p.effectiveEndDate >= :date OR p.effectiveEndDate IS NULL)")
    List<CommissionPlan> findActivePlansForDate(@Param("date") LocalDate date);

    /**
     * Find a plan with its rules eagerly loaded.
     *
     * IMPORTANT - MultipleBagFetchException:
     * Hibernate CANNOT simultaneously JOIN FETCH multiple List (bag) collections
     * in a single query. For example, fetching both p.rules AND r.conditions
     * in one query throws MultipleBagFetchException.
     *
     * SOLUTIONS:
     * 1. Split into separate queries (used here) - fetch rules first, then conditions
     * 2. Change List to Set (eliminates the "bag" semantics)
     * 3. Use @BatchSize annotation for batch lazy loading
     *
     * We split the fetch: this method loads rules, and conditions are loaded
     * lazily or via a separate query when needed.
     */
    @Query("SELECT DISTINCT p FROM CommissionPlan p " +
           "LEFT JOIN FETCH p.rules " +
           "WHERE p.id = :planId")
    Optional<CommissionPlan> findByIdWithRules(@Param("planId") String planId);

    /**
     * Find a plan with its rules AND each rule's conditions.
     * To avoid MultipleBagFetchException, we fetch conditions separately
     * after rules are already loaded in the persistence context.
     */
    @Query("SELECT DISTINCT r FROM CommissionRule r " +
           "LEFT JOIN FETCH r.conditions " +
           "WHERE r.plan.id = :planId")
    List<com.chapman.edu.commissions.orm.entity.CommissionRule> findRulesWithConditionsByPlanId(@Param("planId") String planId);

    @Query("SELECT DISTINCT p FROM CommissionPlan p " +
           "LEFT JOIN FETCH p.tiers " +
           "WHERE p.id = :planId")
    Optional<CommissionPlan> findByIdWithTiers(@Param("planId") String planId);

    @Query("SELECT DISTINCT p FROM CommissionPlan p " +
           "LEFT JOIN FETCH p.bonuses " +
           "WHERE p.id = :planId")
    Optional<CommissionPlan> findByIdWithBonuses(@Param("planId") String planId);

    /**
     * Find plans created by a specific user.
     */
    List<CommissionPlan> findByCreatedBy(String createdBy);

    /**
     * Find plans by name containing a search term (case-insensitive).
     */
    List<CommissionPlan> findByNameContainingIgnoreCase(String name);
}
