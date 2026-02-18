package com.chapman.edu.commissions.corespring.di;

import com.chapman.edu.commissions.model.Deal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Unit tests for ValidationService.
 *
 * KEY CONCEPTS DEMONSTRATED:
 * - Testing validation logic
 * - Verifying exception messages
 * - Testing both positive and negative cases
 * - assertThatThrownBy for exception testing (AssertJ)
 * - assertThatCode for verifying no exception is thrown
 */
@DisplayName("ValidationService — Unit Tests")
class ValidationServiceTest {

    private ValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new ValidationService();
    }

    // ============================================================
    // NEGATIVE CASES — Validation should reject invalid data
    // ============================================================

    @Test
    @DisplayName("Should throw exception when deal is null")
    void validateDeal_withNullDeal_shouldThrowException() {
        assertThatThrownBy(() -> validationService.validateDeal(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Deal cannot be null");
    }

    @Test
    @DisplayName("Should throw exception when deal value is null")
    void validateDeal_withNullValue_shouldThrowException() {
        Deal deal = new Deal("Test Deal", null, "rep-001");

        assertThatThrownBy(() -> validationService.validateDeal(deal))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Deal value must be positive");
    }

    @Test
    @DisplayName("Should throw exception when deal value is negative")
    void validateDeal_withNegativeValue_shouldThrowException() {
        Deal deal = new Deal("Test Deal", new BigDecimal("-100"), "rep-001");

        assertThatThrownBy(() -> validationService.validateDeal(deal))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Deal value must be positive");
    }

    @Test
    @DisplayName("Should throw exception when deal value is zero")
    void validateDeal_withZeroValue_shouldThrowException() {
        Deal deal = new Deal("Test Deal", BigDecimal.ZERO, "rep-001");

        assertThatThrownBy(() -> validationService.validateDeal(deal))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Deal value must be positive");
    }

    @Test
    @DisplayName("Should throw exception when sales rep ID is null")
    void validateDeal_withNullSalesRepId_shouldThrowException() {
        Deal deal = new Deal("Test Deal", new BigDecimal("10000"), null);

        assertThatThrownBy(() -> validationService.validateDeal(deal))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Sales rep ID is required");
    }

    @Test
    @DisplayName("Should throw exception when sales rep ID is empty")
    void validateDeal_withEmptySalesRepId_shouldThrowException() {
        Deal deal = new Deal("Test Deal", new BigDecimal("10000"), "");

        assertThatThrownBy(() -> validationService.validateDeal(deal))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Sales rep ID is required");
    }

    // ============================================================
    // POSITIVE CASE — Valid deal should pass validation
    // ============================================================

    @Test
    @DisplayName("Should not throw exception for a valid deal")
    void validateDeal_withValidDeal_shouldNotThrowException() {
        Deal deal = new Deal("Valid Deal", new BigDecimal("50000"), "rep-001");

        assertThatCode(() -> validationService.validateDeal(deal))
                .doesNotThrowAnyException();
    }
}
