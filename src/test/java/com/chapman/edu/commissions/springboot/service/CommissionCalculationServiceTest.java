package com.chapman.edu.commissions.springboot.service;

import com.chapman.edu.commissions.model.*;
import com.chapman.edu.commissions.springboot.dto.request.CalculateCommissionRequest;
import com.chapman.edu.commissions.springboot.exception.BusinessValidationException;
import com.chapman.edu.commissions.springboot.exception.ResourceNotFoundException;
import com.chapman.edu.commissions.springboot.repository.CommissionCalculationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for Spring Boot CommissionCalculationService.
 *
 * KEY CONCEPTS DEMONSTRATED:
 * - Testing multi-service orchestration (DealService + CommissionPlanService)
 * - Testing business validation across multiple dependencies
 * - Testing state machine transitions (CALCULATED -> APPROVED -> PAID)
 * - Mocking complex dependency chains
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SpringBoot CommissionCalculationService — Unit Tests")
class CommissionCalculationServiceTest {

    @Mock
    private CommissionCalculationRepository calculationRepository;

    @Mock
    private DealService dealService;

    @Mock
    private CommissionPlanService planService;

    @InjectMocks
    private CommissionCalculationService calculationService;

    private Deal testDeal;
    private CommissionPlan testPlan;

    @BeforeEach
    void setUp() {
        testDeal = new Deal("Enterprise License", new BigDecimal("100000"), "rep-001");
        testDeal.setId("deal-001");
        testDeal.setStatus(DealStatus.WON);

        testPlan = new CommissionPlan("Standard Plan", java.util.Currency.getInstance("USD"));
        testPlan.setId("plan-001");
        testPlan.setStatus(PlanStatus.ACTIVE);
        // Add a standard rule with 10% rate
        CommissionRule rule = new CommissionRule("Base Rate", new BigDecimal("10"), CommissionRule.RuleType.STANDARD);
        testPlan.addRule(rule);
    }

    @Test
    @DisplayName("getCalculationById should return calculation when exists")
    void getCalculationById_shouldReturnCalculation_whenExists() {
        CommissionCalculation calc = new CommissionCalculation("deal-001", "rep-001", new BigDecimal("10000"));
        calc.setId("calc-001");
        when(calculationRepository.findById("calc-001")).thenReturn(Optional.of(calc));

        CommissionCalculation result = calculationService.getCalculationById("calc-001");

        assertThat(result.getId()).isEqualTo("calc-001");
    }

    @Test
    @DisplayName("getCalculationById should throw when not found")
    void getCalculationById_shouldThrow_whenNotFound() {
        when(calculationRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> calculationService.getCalculationById("nonexistent"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getAllCalculations should return all calculations")
    void getAllCalculations_shouldReturnAll() {
        CommissionCalculation calc = new CommissionCalculation("deal-001", "rep-001", new BigDecimal("10000"));
        when(calculationRepository.findAll()).thenReturn(List.of(calc));

        List<CommissionCalculation> result = calculationService.getAllCalculations();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("approveCalculation should change status to APPROVED")
    void approveCalculation_shouldChangeStatusToApproved() {
        CommissionCalculation calc = new CommissionCalculation("deal-001", "rep-001", new BigDecimal("10000"));
        calc.setId("calc-001");
        calc.setStatus(CommissionCalculation.CommissionStatus.CALCULATED);

        when(calculationRepository.findById("calc-001")).thenReturn(Optional.of(calc));
        when(calculationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CommissionCalculation result = calculationService.approveCalculation("calc-001");

        assertThat(result.getStatus()).isEqualTo(CommissionCalculation.CommissionStatus.APPROVED);
    }

    @Test
    @DisplayName("approveCalculation should throw when status is not CALCULATED")
    void approveCalculation_shouldThrow_whenNotCalculated() {
        CommissionCalculation calc = new CommissionCalculation("deal-001", "rep-001", new BigDecimal("10000"));
        calc.setId("calc-001");
        calc.setStatus(CommissionCalculation.CommissionStatus.PAID);

        when(calculationRepository.findById("calc-001")).thenReturn(Optional.of(calc));

        assertThatThrownBy(() -> calculationService.approveCalculation("calc-001"))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Only CALCULATED commissions can be approved");
    }

    @Test
    @DisplayName("markAsPaid should change status to PAID")
    void markAsPaid_shouldChangeStatusToPaid() {
        CommissionCalculation calc = new CommissionCalculation("deal-001", "rep-001", new BigDecimal("10000"));
        calc.setId("calc-001");
        calc.setStatus(CommissionCalculation.CommissionStatus.APPROVED);

        when(calculationRepository.findById("calc-001")).thenReturn(Optional.of(calc));
        when(calculationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CommissionCalculation result = calculationService.markAsPaid("calc-001");

        assertThat(result.getStatus()).isEqualTo(CommissionCalculation.CommissionStatus.PAID);
        assertThat(result.getPayoutDate()).isNotNull();
    }

    @Test
    @DisplayName("markAsPaid should throw when status is not APPROVED")
    void markAsPaid_shouldThrow_whenNotApproved() {
        CommissionCalculation calc = new CommissionCalculation("deal-001", "rep-001", new BigDecimal("10000"));
        calc.setId("calc-001");
        calc.setStatus(CommissionCalculation.CommissionStatus.CALCULATED);

        when(calculationRepository.findById("calc-001")).thenReturn(Optional.of(calc));

        assertThatThrownBy(() -> calculationService.markAsPaid("calc-001"))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Only APPROVED commissions can be paid");
    }
}
