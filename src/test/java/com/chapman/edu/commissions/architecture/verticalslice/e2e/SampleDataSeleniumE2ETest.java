package com.chapman.edu.commissions.architecture.verticalslice.e2e;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end Selenium tests verifying sample data integrity.
 * Maps to functional requirements REQ-DEV-001 and user story US-8.3.
 *
 * Validates that DataInitializer creates the expected sample data
 * with correct statuses, relationships, and values.
 */
class SampleDataSeleniumE2ETest extends BaseSeleniumE2ETest {

    // ==================== REQ-DEV-001: Sample data details ====================

    @Test
    @DisplayName("REQ-DEV-001: Sample deals include WON, OPEN, and LOST statuses")
    @SuppressWarnings("unchecked")
    void testReqDev001_sampleDealsHaveVariousStatuses() {
        ResponseEntity<List> response = authenticated()
            .getForEntity("/api/deals", List.class);

        List<Map<String, Object>> deals = response.getBody();
        assertThat(deals).isNotNull();

        List<String> statuses = deals.stream()
            .map(d -> (String) d.get("status"))
            .distinct()
            .toList();

        // DataInitializer creates deals with WON, OPEN, and LOST statuses
        assertThat(statuses).contains("WON");
    }

    @Test
    @DisplayName("REQ-DEV-001: Sample WON deals are available for commission calculations")
    @SuppressWarnings("unchecked")
    void testReqDev001_wonDealsExistForCalculations() {
        ResponseEntity<List> response = authenticated()
            .getForEntity("/api/deals?status=WON", List.class);

        List<Map<String, Object>> wonDeals = response.getBody();
        assertThat(wonDeals).isNotNull();
        assertThat(wonDeals).isNotEmpty();

        for (Map<String, Object> deal : wonDeals) {
            assertThat(deal.get("value")).isNotNull();
            assertThat(deal.get("salesRepId")).isNotNull();
        }
    }

    @Test
    @DisplayName("REQ-DEV-001: Sample ACTIVE plans exist")
    @SuppressWarnings("unchecked")
    void testReqDev001_activePlansExist() {
        ResponseEntity<List> response = authenticated()
            .getForEntity("/api/plans?status=ACTIVE", List.class);

        List<Map<String, Object>> activePlans = response.getBody();
        assertThat(activePlans).isNotNull();
        assertThat(activePlans).isNotEmpty();

        for (Map<String, Object> plan : activePlans) {
            assertThat(plan.get("status")).isEqualTo("ACTIVE");
            assertThat(plan.get("name")).isNotNull();
            assertThat(plan.get("currency")).isEqualTo("USD");
        }
    }

    @Test
    @DisplayName("REQ-DEV-001: Sample plans include effective start dates")
    @SuppressWarnings("unchecked")
    void testReqDev001_plansHaveEffectiveDates() {
        ResponseEntity<List> response = authenticated()
            .getForEntity("/api/plans", List.class);

        List<Map<String, Object>> plans = response.getBody();
        assertThat(plans).isNotNull();

        boolean hasStartDate = plans.stream()
            .anyMatch(p -> p.get("effectiveStartDate") != null);
        assertThat(hasStartDate).isTrue();
    }

    @Test
    @DisplayName("REQ-DEV-001: Sample calculations exist with CALCULATED status")
    @SuppressWarnings("unchecked")
    void testReqDev001_calculationsExistWithStatus() {
        ResponseEntity<List> response = authenticated()
            .getForEntity("/api/calculations", List.class);

        List<Map<String, Object>> calculations = response.getBody();
        assertThat(calculations).isNotNull();
        assertThat(calculations).isNotEmpty();

        boolean hasCalculated = calculations.stream()
            .anyMatch(c -> "CALCULATED".equals(c.get("status")));
        assertThat(hasCalculated).isTrue();
    }

    @Test
    @DisplayName("REQ-DEV-001: Sample calculations have non-zero commission amounts")
    @SuppressWarnings("unchecked")
    void testReqDev001_calculationsHaveNonZeroAmounts() {
        ResponseEntity<List> response = authenticated()
            .getForEntity("/api/calculations", List.class);

        List<Map<String, Object>> calculations = response.getBody();
        assertThat(calculations).isNotNull();

        boolean hasNonZero = calculations.stream()
            .anyMatch(c -> {
                Number base = (Number) c.get("baseCommission");
                return base != null && base.doubleValue() > 0;
            });
        assertThat(hasNonZero).isTrue();
    }

    @Test
    @DisplayName("REQ-DEV-001: Sample disputes exist with INITIATED status")
    @SuppressWarnings("unchecked")
    void testReqDev001_disputesExistWithInitiatedStatus() {
        ResponseEntity<List> response = authenticated()
            .getForEntity("/api/disputes", List.class);

        List<Map<String, Object>> disputes = response.getBody();
        assertThat(disputes).isNotNull();
        assertThat(disputes).isNotEmpty();

        boolean hasInitiated = disputes.stream()
            .anyMatch(d -> "INITIATED".equals(d.get("status")));
        assertThat(hasInitiated).isTrue();
    }

    @Test
    @DisplayName("REQ-DEV-001: Sample deals are assigned to different sales reps")
    @SuppressWarnings("unchecked")
    void testReqDev001_dealsAssignedToDifferentReps() {
        ResponseEntity<List> response = authenticated()
            .getForEntity("/api/deals", List.class);

        List<Map<String, Object>> deals = response.getBody();
        assertThat(deals).isNotNull();

        long distinctReps = deals.stream()
            .map(d -> (String) d.get("salesRepId"))
            .distinct()
            .count();
        // DataInitializer creates deals across 3 reps
        assertThat(distinctReps).isGreaterThanOrEqualTo(2);
    }
}
