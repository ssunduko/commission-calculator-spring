package com.chapman.edu.commissions.orm.repository;

import com.chapman.edu.commissions.orm.entity.Dispute;
import com.chapman.edu.commissions.orm.entity.DisputeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ============================================================
 * SPRING DATA JPA REPOSITORY: DisputeRepository
 * ============================================================
 *
 * FEATURES DEMONSTRATED:
 * - Complex JPQL queries with multiple JOINs
 * - Pagination support
 * - Derived methods for common lookups
 */
@Repository
public interface DisputeRepository extends JpaRepository<Dispute, String> {

    /**
     * Find disputes by status with pagination.
     */
    Page<Dispute> findByStatus(DisputeStatus status, Pageable pageable);

    /**
     * Find disputes filed by a specific sales rep.
     */
    List<Dispute> findBySalesRepId(String salesRepId);

    /**
     * Find disputes assigned to a specific manager.
     */
    List<Dispute> findByManagerId(String managerId);

    /**
     * Find a dispute with its comments eagerly loaded.
     */
    @Query("SELECT DISTINCT d FROM Dispute d " +
           "LEFT JOIN FETCH d.comments " +
           "WHERE d.id = :disputeId")
    Optional<Dispute> findByIdWithComments(@Param("disputeId") String disputeId);

    /**
     * Find open disputes for a manager (not resolved, not cancelled).
     */
    @Query("SELECT d FROM Dispute d WHERE d.manager.id = :managerId " +
           "AND d.status NOT IN ('RESOLVED', 'CANCELLED', 'APPROVED', 'REJECTED')")
    List<Dispute> findOpenDisputesByManagerId(@Param("managerId") String managerId);

    /**
     * Count disputes by status for reporting.
     */
    @Query("SELECT d.status, COUNT(d) FROM Dispute d GROUP BY d.status")
    List<Object[]> countByStatus();
}
