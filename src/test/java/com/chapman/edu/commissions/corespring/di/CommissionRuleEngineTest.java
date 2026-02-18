package com.chapman.edu.commissions.corespring.di;

import com.chapman.edu.commissions.model.Deal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for CommissionRuleEngine.
 *
 * KEY CONCEPTS DEMONSTRATED:
 * - Pure unit testing (no Spring context, no mocks needed)
 * - Testing business logic in isolation
 * - Boundary/edge case testing
 *
 * CommissionRuleEngine has no dependencies, so we test it directly
 * without any mocking framework. This is the simplest form of unit test.
 */
@DisplayName("CommissionRuleEngine — Unit Tests")
class CommissionRuleEngineTest {

    private CommissionRuleEngine ruleEngine;

    @BeforeEach
    void setUp() {
        ruleEngine = new CommissionRuleEngine();
    }

    // ============================================================
    // BASE COMMISSION TESTS
    // ============================================================

    @Test
    @DisplayName("calculateBaseCommission should return 10% of deal value")
    void calculateBaseCommission_shouldReturn10PercentOfDealValue() {
        // Arrange
        Deal deal = new Deal("Enterprise License", new BigDecimal("100000"), "rep-001");

        // Act
        BigDecimal result = ruleEngine.calculateBaseCommission(deal, "standard-plan");

        // Assert — 10% of 100,000 = 10,000
        assertThat(result).isEqualByComparingTo(new BigDecimal("10000.00"));
    }

    @Test
    @DisplayName("calculateBaseCommission should handle small deal values")
    void calculateBaseCommission_shouldHandleSmallValues() {
        Deal deal = new Deal("Starter Plan", new BigDecimal("100"), "rep-002");

        BigDecimal result = ruleEngine.calculateBaseCommission(deal, "basic-plan");

        // 10% of 100 = 10
        assertThat(result).isEqualByComparingTo(new BigDecimal("10.0"));
    }

    // ============================================================
    // BONUS CALCULATION TESTS
    // ============================================================

    @Test
    @DisplayName("calculateBonuses should return 5% for deals over $10,000")
    void calculateBonuses_shouldReturn5PercentForDealsOver10000() {
        Deal deal = new Deal("Premium License", new BigDecimal("50000"), "rep-001");

        BigDecimal result = ruleEngine.calculateBonuses(deal, "standard-plan");

        // 5% of 50,000 = 2,500
        assertThat(result).isEqualByComparingTo(new BigDecimal("2500.00"));
    }

    @Test
    @DisplayName("calculateBonuses should return zero for deals under $10,000")
    void calculateBonuses_shouldReturnZeroForDealsUnder10000() {
        Deal deal = new Deal("Small Deal", new BigDecimal("5000"), "rep-002");

        BigDecimal result = ruleEngine.calculateBonuses(deal, "basic-plan");

        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("calculateBonuses should return zero for deals exactly $10,000 (boundary)")
    void calculateBonuses_shouldReturnZeroForDealsExactly10000() {
        // Boundary test: the condition is > 10000, so exactly 10000 should NOT get a bonus
        Deal deal = new Deal("Boundary Deal", new BigDecimal("10000"), "rep-003");

        BigDecimal result = ruleEngine.calculateBonuses(deal, "standard-plan");

        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("calculateBonuses should return bonus for deals just over $10,000")
    void calculateBonuses_shouldReturnBonusForDealsJustOver10000() {
        // Boundary test: 10,001 should get the bonus
        Deal deal = new Deal("Just Over Boundary", new BigDecimal("10001"), "rep-003");

        BigDecimal result = ruleEngine.calculateBonuses(deal, "standard-plan");

        // 5% of 10,001 = 500.05
        assertThat(result).isEqualByComparingTo(new BigDecimal("500.05"));
    }
}
