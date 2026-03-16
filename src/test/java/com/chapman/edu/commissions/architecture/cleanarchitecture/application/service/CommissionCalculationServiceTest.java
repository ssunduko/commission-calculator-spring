package com.chapman.edu.commissions.architecture.cleanarchitecture.application.service;

import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.CalculateCommissionCommand;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.CalculationResult;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.port.out.CommissionCalculationRepositoryPort;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.port.out.CommissionPlanRepositoryPort;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.port.out.DealRepositoryPort;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.exception.DomainException;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.exception.EntityNotFoundException;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.CommissionCalculation;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.CommissionPlan;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.CommissionRule;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.Deal;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.DealStatus;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.RuleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommissionCalculationServiceTest {

    @Mock
    private CommissionCalculationRepositoryPort calculationRepository;

    @Mock
    private DealRepositoryPort dealRepository;

    @Mock
    private CommissionPlanRepositoryPort planRepository;

    @InjectMocks
    private CommissionCalculationService commissionCalculationService;

    private Deal testDeal;
    private CommissionPlan testPlan;
    private CommissionCalculation testCalculation;

    @BeforeEach
    void setUp() {
        testDeal = new Deal("Enterprise Deal", new BigDecimal("100000"), "REP001");
        testDeal.setId("DEAL001");
        testDeal.setStatus(DealStatus.WON);

        testPlan = new CommissionPlan("Standard Plan", Currency.getInstance("USD"),
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
        testPlan.setId("PLAN001");
        CommissionRule rule = new CommissionRule("Base Rate", new BigDecimal("10"), RuleType.STANDARD);
        testPlan.addRule(rule);

        testCalculation = new CommissionCalculation("DEAL001", "REP001", new BigDecimal("10000.00"));
        testCalculation.setId("CALC001");
        testCalculation.setPlanId("PLAN001");
    }

    @Test
    @DisplayName("calculateCommission should return result for a valid WON deal with a plan")
    void calculateCommission_HappyPath_ShouldReturnCalculationResult() {
        // Given
        CalculateCommissionCommand command = new CalculateCommissionCommand("DEAL001", "PLAN001");
        when(dealRepository.findById("DEAL001")).thenReturn(Optional.of(testDeal));
        when(planRepository.findById("PLAN001")).thenReturn(Optional.of(testPlan));
        when(calculationRepository.save(any(CommissionCalculation.class))).thenAnswer(invocation -> {
            CommissionCalculation calc = invocation.getArgument(0);
            calc.setId("CALC001");
            return calc;
        });

        // When
        CalculationResult result = commissionCalculationService.calculateCommission(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.dealId()).isEqualTo("DEAL001");
        assertThat(result.salesRepId()).isEqualTo("REP001");
        assertThat(result.planId()).isEqualTo("PLAN001");
        assertThat(result.baseCommission()).isEqualByComparingTo(new BigDecimal("10000"));
        verify(dealRepository).findById("DEAL001");
        verify(planRepository).findById("PLAN001");
        verify(calculationRepository).save(any(CommissionCalculation.class));
    }

    @Test
    @DisplayName("calculateCommission should throw EntityNotFoundException when deal not found")
    void calculateCommission_DealNotFound_ShouldThrowEntityNotFoundException() {
        // Given
        CalculateCommissionCommand command = new CalculateCommissionCommand("MISSING_DEAL", "PLAN001");
        when(dealRepository.findById("MISSING_DEAL")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> commissionCalculationService.calculateCommission(command))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Deal")
                .hasMessageContaining("MISSING_DEAL");
        verify(dealRepository).findById("MISSING_DEAL");
        verify(planRepository, never()).findById(anyString());
        verify(calculationRepository, never()).save(any());
    }

    @Test
    @DisplayName("calculateCommission should throw EntityNotFoundException when plan not found")
    void calculateCommission_PlanNotFound_ShouldThrowEntityNotFoundException() {
        // Given
        CalculateCommissionCommand command = new CalculateCommissionCommand("DEAL001", "MISSING_PLAN");
        when(dealRepository.findById("DEAL001")).thenReturn(Optional.of(testDeal));
        when(planRepository.findById("MISSING_PLAN")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> commissionCalculationService.calculateCommission(command))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("CommissionPlan")
                .hasMessageContaining("MISSING_PLAN");
        verify(dealRepository).findById("DEAL001");
        verify(planRepository).findById("MISSING_PLAN");
        verify(calculationRepository, never()).save(any());
    }

    @Test
    @DisplayName("calculateCommission should throw DomainException when deal is not WON")
    void calculateCommission_DealNotWon_ShouldThrowDomainException() {
        // Given
        testDeal.setStatus(DealStatus.OPEN);
        CalculateCommissionCommand command = new CalculateCommissionCommand("DEAL001", "PLAN001");
        when(dealRepository.findById("DEAL001")).thenReturn(Optional.of(testDeal));

        // When & Then
        assertThatThrownBy(() -> commissionCalculationService.calculateCommission(command))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("WON");
        verify(dealRepository).findById("DEAL001");
        verify(planRepository, never()).findById(anyString());
        verify(calculationRepository, never()).save(any());
    }

    @Test
    @DisplayName("getCalculation should return result when calculation exists")
    void getCalculation_ExistingId_ShouldReturnCalculationResult() {
        // Given
        when(calculationRepository.findById("CALC001")).thenReturn(Optional.of(testCalculation));

        // When
        CalculationResult result = commissionCalculationService.getCalculation("CALC001");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("CALC001");
        assertThat(result.dealId()).isEqualTo("DEAL001");
        assertThat(result.salesRepId()).isEqualTo("REP001");
        verify(calculationRepository).findById("CALC001");
    }

    @Test
    @DisplayName("getCalculation should throw EntityNotFoundException when calculation not found")
    void getCalculation_NonExistingId_ShouldThrowEntityNotFoundException() {
        // Given
        when(calculationRepository.findById("MISSING")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> commissionCalculationService.getCalculation("MISSING"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("CommissionCalculation")
                .hasMessageContaining("MISSING");
        verify(calculationRepository).findById("MISSING");
    }

    @Test
    @DisplayName("getAllCalculations should return all calculations")
    void getAllCalculations_ShouldReturnAllCalculations() {
        // Given
        CommissionCalculation calc2 = new CommissionCalculation("DEAL002", "REP002", new BigDecimal("5000"));
        calc2.setId("CALC002");
        when(calculationRepository.findAll()).thenReturn(Arrays.asList(testCalculation, calc2));

        // When
        List<CalculationResult> results = commissionCalculationService.getAllCalculations();

        // Then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).id()).isEqualTo("CALC001");
        assertThat(results.get(1).id()).isEqualTo("CALC002");
        verify(calculationRepository).findAll();
    }

    @Test
    @DisplayName("getAllCalculations should return empty list when no calculations exist")
    void getAllCalculations_Empty_ShouldReturnEmptyList() {
        // Given
        when(calculationRepository.findAll()).thenReturn(List.of());

        // When
        List<CalculationResult> results = commissionCalculationService.getAllCalculations();

        // Then
        assertThat(results).isEmpty();
        verify(calculationRepository).findAll();
    }

    @Test
    @DisplayName("getCalculationsByDeal should return calculations for given deal ID")
    void getCalculationsByDeal_ShouldReturnFilteredCalculations() {
        // Given
        when(calculationRepository.findByDealId("DEAL001")).thenReturn(List.of(testCalculation));

        // When
        List<CalculationResult> results = commissionCalculationService.getCalculationsByDeal("DEAL001");

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).dealId()).isEqualTo("DEAL001");
        verify(calculationRepository).findByDealId("DEAL001");
    }

    @Test
    @DisplayName("getCalculationsByDeal should return empty list when no calculations for deal")
    void getCalculationsByDeal_NoResults_ShouldReturnEmptyList() {
        // Given
        when(calculationRepository.findByDealId("NONEXISTENT")).thenReturn(List.of());

        // When
        List<CalculationResult> results = commissionCalculationService.getCalculationsByDeal("NONEXISTENT");

        // Then
        assertThat(results).isEmpty();
        verify(calculationRepository).findByDealId("NONEXISTENT");
    }

    @Test
    @DisplayName("getCalculationsBySalesRep should return calculations for given sales rep ID")
    void getCalculationsBySalesRep_ShouldReturnFilteredCalculations() {
        // Given
        when(calculationRepository.findBySalesRepId("REP001")).thenReturn(List.of(testCalculation));

        // When
        List<CalculationResult> results = commissionCalculationService.getCalculationsBySalesRep("REP001");

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).salesRepId()).isEqualTo("REP001");
        verify(calculationRepository).findBySalesRepId("REP001");
    }

    @Test
    @DisplayName("getCalculationsBySalesRep should return empty list when no calculations for rep")
    void getCalculationsBySalesRep_NoResults_ShouldReturnEmptyList() {
        // Given
        when(calculationRepository.findBySalesRepId("NONEXISTENT")).thenReturn(List.of());

        // When
        List<CalculationResult> results = commissionCalculationService.getCalculationsBySalesRep("NONEXISTENT");

        // Then
        assertThat(results).isEmpty();
        verify(calculationRepository).findBySalesRepId("NONEXISTENT");
    }
}
