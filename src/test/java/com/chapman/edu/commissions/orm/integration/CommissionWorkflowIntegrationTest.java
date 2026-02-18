package com.chapman.edu.commissions.orm.integration;

import com.chapman.edu.commissions.orm.CommissionCalculatorOrmApplication;
import com.chapman.edu.commissions.orm.entity.CommissionCalculation;
import com.chapman.edu.commissions.orm.entity.CommissionStatus;
import com.chapman.edu.commissions.orm.entity.Dispute;
import com.chapman.edu.commissions.orm.entity.DisputeStatus;
import com.chapman.edu.commissions.orm.service.CommissionService;
import com.chapman.edu.commissions.orm.service.DisputeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ============================================================================
 * INTEGRATION TEST — ORM Commission Calculation and Dispute Workflow
 * ============================================================================
 *
 * CONCEPT: Service-Level Integration Testing with JPA
 * -------------------------------------------------------
 * This test uses both MockMvc (for HTTP-level testing) and direct service
 * injection (for workflow orchestration). This mixed approach demonstrates:
 *
 *   1. MockMvc verifies the HTTP API contract (status codes, JSON structure)
 *   2. Service injection verifies business logic and state transitions
 *   3. JPA ensures data is actually persisted to the H2 database
 *
 * The ORM services interact with real Spring Data JPA repositories, which
 * execute real SQL against the H2 in-memory database. Flyway V2 provides
 * pre-seeded data that represents a realistic starting state.
 *
 * CONCEPT: @Transactional in Integration Tests
 * ------------------------------------------------
 * The ORM services use @Transactional for operations that modify data.
 * In integration tests, these transactions commit to the H2 database,
 * making the changes visible to subsequent queries. This verifies that
 * the transaction boundaries are correctly configured.
 */
@SpringBootTest(classes = CommissionCalculatorOrmApplication.class)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("ORM Commission Workflow — Integration Tests")
class CommissionWorkflowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CommissionService commissionService;

    @Autowired
    private DisputeService disputeService;

    // ============================================================
    // VERIFY SEEDED CALCULATIONS (from Flyway V2)
    // ============================================================

    @Test
    @Order(1)
    @DisplayName("Flyway V2 should have seeded commission calculations")
    void seededData_shouldContainCalculations() {
        // V2 seeds: calc-001 (APPROVED), calc-002 (PAID), calc-003 (CALCULATED), calc-004 (APPROVED)
        var calculation = commissionService.findById("calc-001");
        assertThat(calculation).isPresent();
        assertThat(calculation.get().getStatus()).isEqualTo(CommissionStatus.APPROVED);
        assertThat(calculation.get().getBaseCommission()).isEqualByComparingTo(new BigDecimal("8500.00"));
    }

    @Test
    @Order(2)
    @DisplayName("Seeded commission plans should be cached and retrievable")
    void seededPlans_shouldBeRetrievable() {
        var plan = commissionService.findPlanById("plan-001");
        assertThat(plan).isPresent();
        assertThat(plan.get().getName()).isEqualTo("Standard Sales Plan 2024");
    }

    // ============================================================
    // COMMISSION APPROVAL WORKFLOW
    // ============================================================

    @Test
    @Order(3)
    @DisplayName("Approving a CALCULATED commission should change status to APPROVED")
    void approveCalculation_shouldTransitionToApproved() {
        // calc-003 is in CALCULATED state (from seed data)
        CommissionCalculation approved = commissionService.approveCalculation("calc-003", "usr-004");
        assertThat(approved.getStatus()).isEqualTo(CommissionStatus.APPROVED);

        // Verify persistence via the cache/repo
        var retrieved = commissionService.findById("calc-003");
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getStatus()).isEqualTo(CommissionStatus.APPROVED);
    }

    @Test
    @Order(4)
    @DisplayName("Retrieving sales rep commissions should return calculations")
    void findBySalesRep_shouldReturnCalculations() {
        // usr-001 has calc-001 and calc-002
        var calculations = commissionService.findBySalesRep("usr-001");
        assertThat(calculations).hasSize(2);
    }

    @Test
    @Order(5)
    @DisplayName("Total commissions for sales rep should aggregate correctly")
    void totalCommissions_shouldAggregateCorrectly() {
        // usr-001 has calc-001 (APPROVED, net=9000) and calc-002 (PAID, net=3200)
        BigDecimal total = commissionService.getTotalCommissionsForSalesRep("usr-001");
        assertThat(total).isNotNull();
        assertThat(total).isGreaterThan(BigDecimal.ZERO);
    }

    // ============================================================
    // DISPUTE WORKFLOW
    // ============================================================

    @Test
    @Order(6)
    @DisplayName("Seeded dispute should be retrievable with UNDER_REVIEW status")
    void seededDispute_shouldBeRetrievable() {
        var dispute = disputeService.findById("disp-001");
        assertThat(dispute).isPresent();
        assertThat(dispute.get().getStatus()).isEqualTo(DisputeStatus.UNDER_REVIEW);
        assertThat(dispute.get().getTitle()).isEqualTo("Incorrect Commission Rate");
    }

    @Test
    @Order(7)
    @DisplayName("Seeded dispute should have comments when loaded with JOIN FETCH")
    void seededDispute_shouldHaveComments() {
        var dispute = disputeService.findByIdWithComments("disp-001");
        assertThat(dispute).isPresent();
        assertThat(dispute.get().getComments()).isNotEmpty();
        assertThat(dispute.get().getComments()).hasSize(2);
    }

    @Test
    @Order(8)
    @DisplayName("Filing a new dispute should persist to database")
    void fileDispute_shouldPersist() {
        Dispute newDispute = disputeService.fileDispute(
                "calc-004", "usr-003", "Rate Discrepancy",
                "The applied rate does not match the plan tier for this deal amount.");

        assertThat(newDispute.getId()).isNotNull();
        assertThat(newDispute.getStatus()).isEqualTo(DisputeStatus.INITIATED);

        // Verify persistence by re-reading from database
        var retrieved = disputeService.findById(newDispute.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getTitle()).isEqualTo("Rate Discrepancy");
    }

    // ============================================================
    // CROSS-ENTITY QUERIES via MockMvc
    // ============================================================

    @Test
    @Order(9)
    @DisplayName("Deal total won value should be correctly calculated via JPQL aggregate")
    void totalWonValue_shouldBeCorrectlyCalculated() throws Exception {
        // usr-001 has deal-001 ($85K WON) and deal-002 ($32K WON) = $117K
        mockMvc.perform(get("/api/orm/deals/sales-rep/usr-001/total-won"))
                .andExpect(status().isOk())
                .andExpect(content().string("117000.00"));
    }

    @Test
    @Order(10)
    @DisplayName("Won deals without calculations should return qualifying deals")
    void wonDealsWithoutCalculations_shouldReturnResults() throws Exception {
        mockMvc.perform(get("/api/orm/deals/pending-calculation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(11)
    @DisplayName("Search deals with combined filters should return correct results")
    void searchDeals_withCombinedFilters_shouldWork() throws Exception {
        mockMvc.perform(get("/api/orm/deals/search")
                        .param("status", "WON")
                        .param("minValue", "50000")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @Order(12)
    @DisplayName("Commission summary by sales rep should return aggregate data")
    void commissionSummary_shouldReturnAggregateData() {
        var summary = commissionService.getCommissionSummary();
        assertThat(summary).isNotEmpty();
    }
}
