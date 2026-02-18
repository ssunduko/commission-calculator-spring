package com.chapman.edu.commissions.springboot.mapper;

import com.chapman.edu.commissions.model.*;
import com.chapman.edu.commissions.springboot.dto.response.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for DtoMapper.
 *
 * KEY CONCEPTS DEMONSTRATED:
 * - Testing the Mapper pattern (Domain -> DTO conversion)
 * - Verifying all fields are correctly mapped
 * - Testing null-safety in mappings
 * - Pure unit tests (no Spring context needed)
 *
 * WHY TEST MAPPERS?
 * Mappers are a common source of bugs when domain models change
 * but the mapper isn't updated. Testing ensures all fields are
 * correctly transferred between layers.
 */
@DisplayName("DtoMapper — Unit Tests")
class DtoMapperTest {

    private DtoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new DtoMapper();
    }

    // ============================================================
    // DEAL MAPPING TESTS
    // ============================================================

    @Test
    @DisplayName("toDealResponse should map all fields correctly")
    void toDealResponse_shouldMapAllFields() {
        Deal deal = new Deal("Enterprise License", new BigDecimal("100000"), "rep-001");
        deal.setId("deal-001");
        deal.setStatus(DealStatus.WON);
        deal.setCloseDate(LocalDate.of(2024, 3, 15));

        DealResponse response = mapper.toDealResponse(deal);

        assertThat(response.getId()).isEqualTo("deal-001");
        assertThat(response.getTitle()).isEqualTo("Enterprise License");
        assertThat(response.getValue()).isEqualByComparingTo(new BigDecimal("100000"));
        assertThat(response.getStatus()).isEqualTo("WON");
        assertThat(response.getSalesRepId()).isEqualTo("rep-001");
        assertThat(response.getCloseDate()).isEqualTo(LocalDate.of(2024, 3, 15));
    }

    @Test
    @DisplayName("toDealResponse should handle null products gracefully")
    void toDealResponse_shouldHandleNullProducts() {
        Deal deal = new Deal("Simple Deal", new BigDecimal("10000"), "rep-002");
        deal.setId("deal-002");

        DealResponse response = mapper.toDealResponse(deal);

        assertThat(response.getId()).isEqualTo("deal-002");
        // products list should be empty or null, not cause NPE
    }

    // ============================================================
    // USER MAPPING TESTS
    // ============================================================

    @Test
    @DisplayName("toUserResponse should map all fields correctly")
    void toUserResponse_shouldMapAllFields() {
        User user = new User("jsmith", "john@test.com", "John", "Smith");
        user.setId("user-001");
        user.setActive(true);
        user.setDepartment("Sales");
        user.setTerritory("West Coast");
        user.addRole(UserRole.SALES_REP);

        UserResponse response = mapper.toUserResponse(user);

        assertThat(response.getId()).isEqualTo("user-001");
        assertThat(response.getUsername()).isEqualTo("jsmith");
        assertThat(response.getEmail()).isEqualTo("john@test.com");
        assertThat(response.getFirstName()).isEqualTo("John");
        assertThat(response.getLastName()).isEqualTo("Smith");
        assertThat(response.isActive()).isTrue();
        assertThat(response.getDepartment()).isEqualTo("Sales");
    }

    @Test
    @DisplayName("toUserResponse should map roles correctly")
    void toUserResponse_shouldMapRoles() {
        User user = new User("admin", "admin@test.com", "Admin", "User");
        user.setId("user-admin");
        user.addRole(UserRole.SYSTEM_ADMIN);
        user.addRole(UserRole.FINANCE_ADMIN);

        UserResponse response = mapper.toUserResponse(user);

        assertThat(response.getRoles()).containsExactlyInAnyOrder("SYSTEM_ADMIN", "FINANCE_ADMIN");
    }

    // ============================================================
    // COMMISSION PLAN MAPPING TESTS
    // ============================================================

    @Test
    @DisplayName("toCommissionPlanResponse should map all fields correctly")
    void toCommissionPlanResponse_shouldMapAllFields() {
        CommissionPlan plan = new CommissionPlan("Standard Plan", Currency.getInstance("USD"));
        plan.setId("plan-001");
        plan.setStatus(PlanStatus.ACTIVE);
        plan.setEffectiveStartDate(LocalDate.of(2024, 1, 1));
        plan.setEffectiveEndDate(LocalDate.of(2024, 12, 31));
        plan.setCreatedBy("admin");

        CommissionPlanResponse response = mapper.toCommissionPlanResponse(plan);

        assertThat(response.getId()).isEqualTo("plan-001");
        assertThat(response.getName()).isEqualTo("Standard Plan");
        assertThat(response.getCurrency()).isEqualTo("USD");
        assertThat(response.getStatus()).isEqualTo("ACTIVE");
        assertThat(response.getCreatedBy()).isEqualTo("admin");
    }

    // ============================================================
    // COMMISSION CALCULATION MAPPING TESTS
    // ============================================================

    @Test
    @DisplayName("toCommissionCalculationResponse should map all fields correctly")
    void toCommissionCalculationResponse_shouldMapAllFields() {
        CommissionCalculation calc = new CommissionCalculation("deal-001", "rep-001", new BigDecimal("10000"));
        calc.setId("calc-001");
        calc.setGrossCommission(new BigDecimal("10500"));
        calc.setNetCommission(new BigDecimal("10500"));
        calc.setPlanId("plan-001");
        calc.setCalculatedBy("system");

        CommissionCalculationResponse response = mapper.toCommissionCalculationResponse(calc);

        assertThat(response.getId()).isEqualTo("calc-001");
        assertThat(response.getDealId()).isEqualTo("deal-001");
        assertThat(response.getSalesRepId()).isEqualTo("rep-001");
        assertThat(response.getBaseCommission()).isEqualByComparingTo(new BigDecimal("10000"));
        assertThat(response.getGrossCommission()).isEqualByComparingTo(new BigDecimal("10500"));
        assertThat(response.getPlanId()).isEqualTo("plan-001");
        assertThat(response.getCalculatedBy()).isEqualTo("system");
    }

    // ============================================================
    // DISPUTE MAPPING TESTS
    // ============================================================

    @Test
    @DisplayName("toDisputeResponse should map all fields correctly")
    void toDisputeResponse_shouldMapAllFields() {
        Dispute dispute = new Dispute("calc-001", "rep-001", "Incorrect Rate", "Description");
        dispute.setId("disp-001");
        dispute.setManagerId("mgr-001");
        dispute.setStatus(DisputeStatus.UNDER_REVIEW);

        DisputeResponse response = mapper.toDisputeResponse(dispute);

        assertThat(response.getId()).isEqualTo("disp-001");
        assertThat(response.getCalculationId()).isEqualTo("calc-001");
        assertThat(response.getSalesRepId()).isEqualTo("rep-001");
        assertThat(response.getTitle()).isEqualTo("Incorrect Rate");
        assertThat(response.getStatus()).isEqualTo("UNDER_REVIEW");
    }
}
