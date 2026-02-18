package com.chapman.edu.commissions.orm.service;

import com.chapman.edu.commissions.orm.entity.*;
import com.chapman.edu.commissions.orm.repository.CommissionCalculationRepository;
import com.chapman.edu.commissions.orm.repository.CommissionPlanRepository;
import com.chapman.edu.commissions.orm.repository.DealRepository;
import com.chapman.edu.commissions.orm.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ORM CommissionService.
 *
 * KEY CONCEPTS DEMONSTRATED:
 * - Testing complex business logic (commission calculation workflow)
 * - Mocking multiple repositories
 * - Testing state transition validation (status checks)
 * - Verifying transaction isolation level through service behavior
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ORM CommissionService — Unit Tests")
class CommissionServiceTest {

    @Mock
    private CommissionCalculationRepository calculationRepository;

    @Mock
    private CommissionPlanRepository planRepository;

    @Mock
    private DealRepository dealRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommissionService commissionService;

    private Deal testDeal;
    private User testSalesRep;
    private CommissionPlan testPlan;

    @BeforeEach
    void setUp() {
        testSalesRep = new User("jsmith", "john@test.com", "John", "Smith");
        testSalesRep.setId("usr-001");

        testDeal = new Deal("Test Deal", new BigDecimal("100000"), testSalesRep);
        testDeal.setId("deal-001");
        testDeal.setStatus(DealStatus.WON);

        testPlan = new CommissionPlan("Standard Plan", java.util.Currency.getInstance("USD"));
        testPlan.setId("plan-001");
        testPlan.setStatus(PlanStatus.ACTIVE);

        // Add a tier
        CommissionTier tier = new CommissionTier();
        tier.setName("Gold");
        tier.setLowerBound(new BigDecimal("75000"));
        tier.setUpperBound(new BigDecimal("150000"));
        tier.setRate(new BigDecimal("12"));
        tier.setPercentage(true);
        testPlan.addTier(tier);
    }

    // ============================================================
    // FIND TESTS
    // ============================================================

    @Test
    @DisplayName("findById should return calculation when it exists")
    void findById_shouldReturnCalculation_whenExists() {
        CommissionCalculation calc = new CommissionCalculation(testDeal, testSalesRep, new BigDecimal("1000"));
        calc.setId("calc-001");
        when(calculationRepository.findById("calc-001")).thenReturn(Optional.of(calc));

        Optional<CommissionCalculation> result = commissionService.findById("calc-001");

        assertThat(result).isPresent();
        verify(calculationRepository).findById("calc-001");
    }

    // ============================================================
    // APPROVE TESTS
    // ============================================================

    @Test
    @DisplayName("approveCalculation should change status to APPROVED")
    void approveCalculation_shouldChangeStatusToApproved() {
        CommissionCalculation calc = new CommissionCalculation(testDeal, testSalesRep, new BigDecimal("10000"));
        calc.setId("calc-001");
        calc.setStatus(CommissionStatus.CALCULATED);

        when(calculationRepository.findById("calc-001")).thenReturn(Optional.of(calc));
        when(calculationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CommissionCalculation result = commissionService.approveCalculation("calc-001", "manager");

        assertThat(result.getStatus()).isEqualTo(CommissionStatus.APPROVED);
        verify(calculationRepository).save(calc);
    }

    @Test
    @DisplayName("approveCalculation should throw exception when status is not CALCULATED")
    void approveCalculation_shouldThrowException_whenStatusIsNotCalculated() {
        CommissionCalculation calc = new CommissionCalculation(testDeal, testSalesRep, new BigDecimal("10000"));
        calc.setId("calc-001");
        calc.setStatus(CommissionStatus.APPROVED);

        when(calculationRepository.findById("calc-001")).thenReturn(Optional.of(calc));

        assertThatThrownBy(() -> commissionService.approveCalculation("calc-001", "manager"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Can only approve CALCULATED commissions");
    }

    @Test
    @DisplayName("approveCalculation should throw exception when calculation not found")
    void approveCalculation_shouldThrowException_whenNotFound() {
        when(calculationRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commissionService.approveCalculation("nonexistent", "manager"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Calculation not found");
    }

    // ============================================================
    // PLAN MANAGEMENT TESTS
    // ============================================================

    @Test
    @DisplayName("createPlan should save and return the plan")
    void createPlan_shouldSaveAndReturn() {
        when(planRepository.save(any(CommissionPlan.class))).thenReturn(testPlan);

        CommissionPlan result = commissionService.createPlan(testPlan);

        assertThat(result.getName()).isEqualTo("Standard Plan");
        verify(planRepository).save(testPlan);
    }

    @Test
    @DisplayName("activatePlan should set status to ACTIVE with dates")
    void activatePlan_shouldSetStatusToActive() {
        when(planRepository.findById("plan-001")).thenReturn(Optional.of(testPlan));
        when(planRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        java.time.LocalDate start = java.time.LocalDate.of(2024, 1, 1);
        java.time.LocalDate end = java.time.LocalDate.of(2024, 12, 31);

        CommissionPlan result = commissionService.activatePlan("plan-001", start, end);

        assertThat(result.getStatus()).isEqualTo(PlanStatus.ACTIVE);
        assertThat(result.getEffectiveStartDate()).isEqualTo(start);
        assertThat(result.getEffectiveEndDate()).isEqualTo(end);
    }
}
