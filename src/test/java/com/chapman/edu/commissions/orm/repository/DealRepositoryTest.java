package com.chapman.edu.commissions.orm.repository;

import com.chapman.edu.commissions.orm.entity.Deal;
import com.chapman.edu.commissions.orm.entity.DealStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for DealRepository.
 *
 * KEY CONCEPTS DEMONSTRATED:
 * - @DataJpaTest with Flyway seed data
 * - Testing derived query methods, JPQL queries, and @EntityGraph
 * - Testing JPA Specifications (dynamic query building)
 * - Pagination and sorting
 *
 * The seed data (V2 migration) includes 6 deals:
 *   deal-001: WON, $85,000, usr-001
 *   deal-002: WON, $32,000, usr-001
 *   deal-003: WON, $120,000, usr-002
 *   deal-004: WON, $8,500, usr-003
 *   deal-005: OPEN, $250,000, usr-002
 *   deal-006: LOST, $5,000, usr-003
 */
@DataJpaTest
@DisplayName("DealRepository — Integration Tests")
class DealRepositoryTest {

    @Autowired
    private DealRepository dealRepository;

    // ============================================================
    // DERIVED QUERY TESTS
    // ============================================================

    @Test
    @DisplayName("findByStatus should return all deals with matching status")
    void findByStatus_shouldReturnDealsWithStatus() {
        List<Deal> wonDeals = dealRepository.findByStatus(DealStatus.WON);

        // V2 seed: 4 WON deals
        assertThat(wonDeals).hasSize(4);
        assertThat(wonDeals).allMatch(d -> d.getStatus() == DealStatus.WON);
    }

    @Test
    @DisplayName("findBySalesRepId should return paginated deals for a sales rep")
    void findBySalesRepId_shouldReturnPaginatedDeals() {
        // usr-001 has 2 deals (deal-001, deal-002)
        Page<Deal> page = dealRepository.findBySalesRepId("usr-001", PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("findByValueGreaterThan should return deals above threshold")
    void findByValueGreaterThan_shouldReturnDealsAboveThreshold() {
        List<Deal> largeDeals = dealRepository.findByValueGreaterThan(new BigDecimal("100000"));

        // deal-003: $120K, deal-005: $250K
        assertThat(largeDeals).hasSize(2);
        assertThat(largeDeals).allMatch(d -> d.getValue().compareTo(new BigDecimal("100000")) > 0);
    }

    // ============================================================
    // @EntityGraph TEST (solving N+1)
    // ============================================================

    @Test
    @DisplayName("findWithProductsAndSalesRepById should eagerly load products")
    void findWithProductsAndSalesRepById_shouldLoadEagerly() {
        Optional<Deal> deal = dealRepository.findWithProductsAndSalesRepById("deal-001");

        assertThat(deal).isPresent();
        assertThat(deal.get().getTitle()).isEqualTo("Acme Corp ERP Implementation");
        // Products should be loaded eagerly (no LazyInitializationException)
        assertThat(deal.get().getProducts()).hasSize(2);
    }

    // ============================================================
    // JPQL QUERY TESTS
    // ============================================================

    @Test
    @DisplayName("calculateTotalValueBySalesRepAndStatus should return correct sum")
    void calculateTotalValueBySalesRepAndStatus_shouldReturnCorrectSum() {
        // usr-001 has 2 WON deals: $85,000 + $32,000 = $117,000
        BigDecimal total = dealRepository.calculateTotalValueBySalesRepAndStatus("usr-001", DealStatus.WON);

        assertThat(total).isEqualByComparingTo(new BigDecimal("117000.00"));
    }

    @Test
    @DisplayName("findWonDealsWithoutCalculations should return deals missing calculations")
    void findWonDealsWithoutCalculations_shouldReturnDealsWithoutCalcs() {
        List<Deal> uncalculated = dealRepository.findWonDealsWithoutCalculations();

        // All 4 WON deals have calculations in V2 seed, so this should be empty
        // (unless seed data changes)
        assertThat(uncalculated).isEmpty();
    }

    // ============================================================
    // JPA SPECIFICATION TESTS (Dynamic Queries)
    // ============================================================

    @Test
    @DisplayName("Specification: hasStatus should filter by deal status")
    void specification_hasStatus_shouldFilterByStatus() {
        Specification<Deal> spec = DealSpecifications.hasStatus(DealStatus.WON);
        Page<Deal> result = dealRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(4);
        assertThat(result.getContent()).allMatch(d -> d.getStatus() == DealStatus.WON);
    }

    @Test
    @DisplayName("Specification: valueGreaterThan should filter by minimum value")
    void specification_valueGreaterThan_shouldFilterByMinValue() {
        Specification<Deal> spec = DealSpecifications.valueGreaterThan(new BigDecimal("50000"));
        Page<Deal> result = dealRepository.findAll(spec, PageRequest.of(0, 10));

        // deal-001: $85K, deal-003: $120K, deal-005: $250K
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent())
                .allMatch(d -> d.getValue().compareTo(new BigDecimal("50000")) > 0);
    }

    @Test
    @DisplayName("Specification: combining multiple filters should narrow results")
    void specification_combiningFilters_shouldNarrowResults() {
        Specification<Deal> spec = Specification.where(DealSpecifications.hasStatus(DealStatus.WON))
                .and(DealSpecifications.valueGreaterThan(new BigDecimal("50000")));

        Page<Deal> result = dealRepository.findAll(spec, PageRequest.of(0, 10));

        // WON + > $50K: deal-001 ($85K), deal-003 ($120K)
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(d -> d.getStatus() == DealStatus.WON);
        assertThat(result.getContent())
                .allMatch(d -> d.getValue().compareTo(new BigDecimal("50000")) > 0);
    }

    @Test
    @DisplayName("Specification: titleContains should filter by title keyword")
    void specification_titleContains_shouldFilterByTitle() {
        Specification<Deal> spec = DealSpecifications.titleContains("Corp");
        Page<Deal> result = dealRepository.findAll(spec, PageRequest.of(0, 10));

        // "Acme Corp" and "MegaCorp" match
        assertThat(result.getContent()).hasSizeGreaterThanOrEqualTo(1);
        assertThat(result.getContent())
                .allMatch(d -> d.getTitle().toLowerCase().contains("corp"));
    }
}
