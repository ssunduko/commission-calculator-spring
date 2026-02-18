package com.chapman.edu.commissions.orm.repository;

import com.chapman.edu.commissions.orm.entity.Deal;
import com.chapman.edu.commissions.orm.entity.DealStatus;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * ============================================================
 * JPA SPECIFICATIONS: Dynamic Query Building
 * ============================================================
 *
 * WHAT ARE SPECIFICATIONS?
 * Specifications implement the Specification pattern from Domain-Driven Design.
 * They allow you to build complex, dynamic queries by composing simple predicates.
 *
 * WHY USE SPECIFICATIONS?
 * Consider a search form with optional filters:
 *   - Status (optional)
 *   - Min value (optional)
 *   - Max value (optional)
 *   - Date range (optional)
 *   - Sales rep (optional)
 *
 * WITHOUT Specifications, you'd need methods for every combination:
 *   findByStatus, findByStatusAndMinValue, findByStatusAndDateRange,
 *   findByMinValueAndMaxValue, findByStatusAndMinValueAndDateRange...
 *   (2^5 = 32 possible combinations!)
 *
 * WITH Specifications, each filter is a reusable predicate:
 *   hasStatus(WON).and(valueGreaterThan(10000)).and(closedAfter(date))
 *
 * HOW IT WORKS:
 * 1. Each method returns a Specification<Deal> (a functional interface)
 * 2. The Specification has a toPredicate() method that creates a JPA Criteria predicate
 * 3. Specifications are combined with .and(), .or(), .not()
 * 4. The repository's findAll(Specification) method executes the combined query
 *
 * REQUIRED: The repository must extend JpaSpecificationExecutor<Deal>
 */
public class DealSpecifications {

    /**
     * Filter by deal status.
     *
     * The lambda receives (root, query, criteriaBuilder):
     * - root: Represents the Deal entity (like 'FROM Deal d')
     * - query: The overall query (used for subqueries, distinct, etc.)
     * - criteriaBuilder: Factory for creating predicates (=, >, <, LIKE, etc.)
     */
    public static Specification<Deal> hasStatus(DealStatus status) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), status);
    }

    /**
     * Filter deals with value greater than a minimum.
     */
    public static Specification<Deal> valueGreaterThan(BigDecimal minValue) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThan(root.get("value"), minValue);
    }

    /**
     * Filter deals with value less than a maximum.
     */
    public static Specification<Deal> valueLessThan(BigDecimal maxValue) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThan(root.get("value"), maxValue);
    }

    /**
     * Filter deals closed after a specific date.
     */
    public static Specification<Deal> closedAfter(LocalDate date) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("closeDate"), date);
    }

    /**
     * Filter deals closed before a specific date.
     */
    public static Specification<Deal> closedBefore(LocalDate date) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get("closeDate"), date);
    }

    /**
     * Filter deals by sales rep ID.
     * Navigates the @ManyToOne relationship to User.
     */
    public static Specification<Deal> belongsToSalesRep(String salesRepId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("salesRep").get("id"), salesRepId);
    }

    /**
     * Filter deals with title containing a search term (case-insensitive).
     */
    public static Specification<Deal> titleContains(String searchTerm) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")),
                        "%" + searchTerm.toLowerCase() + "%"
                );
    }

    /**
     * USAGE EXAMPLE (in a service):
     *
     * public Page<Deal> searchDeals(DealStatus status, BigDecimal minValue,
     *                                BigDecimal maxValue, String salesRepId,
     *                                Pageable pageable) {
     *     Specification<Deal> spec = Specification.where(null); // Start with no filter
     *
     *     if (status != null) {
     *         spec = spec.and(DealSpecifications.hasStatus(status));
     *     }
     *     if (minValue != null) {
     *         spec = spec.and(DealSpecifications.valueGreaterThan(minValue));
     *     }
     *     if (maxValue != null) {
     *         spec = spec.and(DealSpecifications.valueLessThan(maxValue));
     *     }
     *     if (salesRepId != null) {
     *         spec = spec.and(DealSpecifications.belongsToSalesRep(salesRepId));
     *     }
     *
     *     return dealRepository.findAll(spec, pageable);
     * }
     */
}
