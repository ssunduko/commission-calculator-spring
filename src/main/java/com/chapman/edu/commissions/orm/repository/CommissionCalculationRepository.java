package com.chapman.edu.commissions.orm.repository;

import com.chapman.edu.commissions.orm.entity.CommissionCalculation;
import com.chapman.edu.commissions.orm.entity.CommissionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * ============================================================
 * SPRING DATA JPA REPOSITORY: CommissionCalculationRepository
 * ============================================================
 *
 * ADVANCED FEATURES DEMONSTRATED:
 * - @Modifying queries (UPDATE/DELETE operations)
 * - Projection queries (returning specific fields)
 * - Aggregate queries (SUM, COUNT, AVG)
 * - Pagination with custom queries
 *
 * @Modifying QUERIES:
 * By default, @Query methods are SELECT queries (read-only).
 * For UPDATE or DELETE operations, you must add @Modifying.
 * The @Modifying annotation tells Spring that this query modifies data.
 *
 * IMPORTANT: @Modifying queries bypass the persistence context (1st level cache).
 * This means entities already loaded in the session may be stale after a
 * bulk update. Use clearAutomatically = true to clear the cache.
 */
@Repository
public interface CommissionCalculationRepository extends JpaRepository<CommissionCalculation, String> {

    /**
     * Find calculations by status with pagination.
     */
    Page<CommissionCalculation> findByStatus(CommissionStatus status, Pageable pageable);

    /**
     * Find calculations for a specific deal.
     */
    List<CommissionCalculation> findByDealId(String dealId);

    /**
     * Find calculations for a sales rep.
     */
    List<CommissionCalculation> findBySalesRepId(String salesRepId);

    /**
     * Find calculations by sales rep and status.
     */
    List<CommissionCalculation> findBySalesRepIdAndStatus(String salesRepId, CommissionStatus status);

    /**
     * Find calculations within a date range.
     */
    List<CommissionCalculation> findByCalculationDateBetween(LocalDate start, LocalDate end);

    /**
     * Calculate total commissions for a sales rep.
     * Demonstrates aggregate queries in JPQL.
     */
    @Query("SELECT COALESCE(SUM(cc.netCommission), 0) FROM CommissionCalculation cc " +
           "WHERE cc.salesRep.id = :salesRepId AND cc.status IN :statuses")
    BigDecimal calculateTotalCommissionBySalesRep(
            @Param("salesRepId") String salesRepId,
            @Param("statuses") List<CommissionStatus> statuses);

    /**
     * Get commission summary by sales rep.
     * Returns: [salesRepId, salesRepName, totalCalculations, totalCommission]
     */
    @Query("SELECT cc.salesRep.id, " +
           "CONCAT(cc.salesRep.firstName, ' ', cc.salesRep.lastName), " +
           "COUNT(cc), SUM(cc.netCommission) " +
           "FROM CommissionCalculation cc " +
           "WHERE cc.status IN ('APPROVED', 'PAID') " +
           "GROUP BY cc.salesRep.id, cc.salesRep.firstName, cc.salesRep.lastName " +
           "ORDER BY SUM(cc.netCommission) DESC")
    List<Object[]> getCommissionSummaryBySalesRep();

    /**
     * BULK UPDATE with @Modifying.
     *
     * @Modifying: Required for UPDATE/DELETE queries.
     * clearAutomatically = true: Clears the persistence context after execution
     *   to prevent stale entity data.
     * flushAutomatically = true: Flushes pending changes before executing
     *   to ensure the bulk update sees the latest data.
     *
     * IMPORTANT: This executes as a single SQL UPDATE statement,
     * bypassing entity lifecycle callbacks (@PreUpdate, etc.)
     * and JPA event listeners.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE CommissionCalculation cc SET cc.status = :newStatus " +
           "WHERE cc.status = :currentStatus AND cc.calculationDate <= :beforeDate")
    int bulkUpdateStatus(
            @Param("currentStatus") CommissionStatus currentStatus,
            @Param("newStatus") CommissionStatus newStatus,
            @Param("beforeDate") LocalDate beforeDate);

    /**
     * Find calculations pending payout.
     */
    @Query("SELECT cc FROM CommissionCalculation cc " +
           "JOIN FETCH cc.deal " +
           "JOIN FETCH cc.salesRep " +
           "WHERE cc.status = 'APPROVED' AND cc.payoutDate <= :payoutDate")
    List<CommissionCalculation> findCalculationsPendingPayout(@Param("payoutDate") LocalDate payoutDate);
}
