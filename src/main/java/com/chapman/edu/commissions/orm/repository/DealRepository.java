package com.chapman.edu.commissions.orm.repository;

import com.chapman.edu.commissions.orm.entity.Deal;
import com.chapman.edu.commissions.orm.entity.DealStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * ============================================================
 * SPRING DATA JPA REPOSITORY: DealRepository
 * ============================================================
 *
 * ADVANCED FEATURES DEMONSTRATED:
 *
 * 1. JpaSpecificationExecutor<Deal>:
 *    Adds support for dynamic queries using the Specification pattern.
 *    Specifications allow building queries programmatically at runtime,
 *    ideal for search/filter screens with optional criteria.
 *
 * 2. @EntityGraph:
 *    Overrides the default fetch strategy for a specific query.
 *    Solves the N+1 problem by eagerly fetching related entities
 *    in a single JOIN query.
 *
 * 3. Pageable and Page:
 *    Built-in pagination and sorting support.
 *    Page<Deal> contains: content (list), totalElements, totalPages,
 *    current page number, and navigation info.
 *
 * THE N+1 PROBLEM:
 * When you load 100 Deals with LAZY salesRep, accessing each salesRep
 * triggers a separate SQL query (1 for deals + 100 for sales reps = 101 queries).
 * Solutions:
 *   1. @EntityGraph (used below) - SELECT with JOIN
 *   2. JOIN FETCH in JPQL - Explicit fetch join
 *   3. @BatchSize - Batch lazy loading (IN clause with multiple IDs)
 */
@Repository
public interface DealRepository extends JpaRepository<Deal, String>, JpaSpecificationExecutor<Deal> {

    // ============================================================
    // DERIVED QUERY METHODS
    // ============================================================

    /**
     * Find deals by status.
     */
    List<Deal> findByStatus(DealStatus status);

    /**
     * Find deals by sales rep with pagination.
     */
    Page<Deal> findBySalesRepId(String salesRepId, Pageable pageable);

    /**
     * Find deals closed within a date range.
     * 'Between' includes both endpoints.
     *
     * Generated: WHERE close_date BETWEEN :start AND :end
     */
    List<Deal> findByCloseDateBetween(LocalDate start, LocalDate end);

    /**
     * Find deals with value greater than a threshold.
     *
     * Generated: WHERE deal_value > :minValue
     */
    List<Deal> findByValueGreaterThan(BigDecimal minValue);

    /**
     * Find deals by status ordered by value descending.
     * OrderBy keyword in method name controls sorting.
     *
     * Generated: WHERE status = :status ORDER BY deal_value DESC
     */
    List<Deal> findByStatusOrderByValueDesc(DealStatus status);

    // ============================================================
    // @EntityGraph - Solving the N+1 Problem
    // ============================================================

    /**
     * Find a deal by ID with its products eagerly loaded.
     *
     * @EntityGraph tells JPA to fetch the specified associations
     * in the same query using a LEFT JOIN FETCH.
     *
     * WITHOUT @EntityGraph:
     *   SELECT * FROM deals WHERE id = ?          -- 1 query
     *   SELECT * FROM deal_products WHERE deal_id = ?  -- 1 query (lazy load)
     *
     * WITH @EntityGraph:
     *   SELECT * FROM deals d
     *   LEFT JOIN deal_products dp ON dp.deal_id = d.id
     *   WHERE d.id = ?                           -- 1 query total!
     *
     * attributePaths: The relationship properties to fetch eagerly.
     */
    @EntityGraph(attributePaths = {"products", "salesRep"})
    Optional<Deal> findWithProductsAndSalesRepById(String id);

    // ============================================================
    // JPQL QUERIES with JOIN FETCH
    // ============================================================

    /**
     * Find all deals for a sales rep with products loaded.
     * JOIN FETCH is the JPQL equivalent of @EntityGraph.
     *
     * JOIN FETCH d.products: Performs a LEFT JOIN and initializes
     * the products collection in the same query.
     *
     * DISTINCT: Required to avoid duplicate Deal objects when
     * a deal has multiple products (due to the JOIN producing
     * multiple rows per deal).
     */
    @Query("SELECT DISTINCT d FROM Deal d " +
           "LEFT JOIN FETCH d.products " +
           "WHERE d.salesRep.id = :salesRepId AND d.status = :status")
    List<Deal> findDealsWithProductsBySalesRepAndStatus(
            @Param("salesRepId") String salesRepId,
            @Param("status") DealStatus status);

    /**
     * Calculate total deal value for a sales rep.
     * Demonstrates JPQL aggregate functions.
     */
    @Query("SELECT COALESCE(SUM(d.value), 0) FROM Deal d " +
           "WHERE d.salesRep.id = :salesRepId AND d.status = :status")
    BigDecimal calculateTotalValueBySalesRepAndStatus(
            @Param("salesRepId") String salesRepId,
            @Param("status") DealStatus status);

    /**
     * Monthly deal summary - demonstrates JPQL with date functions.
     * Returns: [month, count, totalValue] for each month.
     */
    @Query("SELECT MONTH(d.closeDate), COUNT(d), SUM(d.value) FROM Deal d " +
           "WHERE d.status = 'WON' AND YEAR(d.closeDate) = :year " +
           "GROUP BY MONTH(d.closeDate) ORDER BY MONTH(d.closeDate)")
    List<Object[]> getMonthlySummary(@Param("year") int year);

    /**
     * Find deals that don't have any commission calculations yet.
     * Demonstrates LEFT JOIN with IS NULL pattern (anti-join).
     */
    @Query("SELECT d FROM Deal d LEFT JOIN d.calculations c " +
           "WHERE c.id IS NULL AND d.status = 'WON'")
    List<Deal> findWonDealsWithoutCalculations();
}
