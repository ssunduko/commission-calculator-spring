package com.chapman.edu.commissions.architecture.verticalslice.features.calculations;

import com.chapman.edu.commissions.architecture.verticalslice.domain.CommissionCalculation;
import com.chapman.edu.commissions.architecture.verticalslice.domain.CommissionPlan;
import com.chapman.edu.commissions.architecture.verticalslice.domain.CommissionRule;
import com.chapman.edu.commissions.architecture.verticalslice.domain.CommissionStatus;
import com.chapman.edu.commissions.architecture.verticalslice.domain.Deal;
import com.chapman.edu.commissions.architecture.verticalslice.domain.RuleType;
import com.chapman.edu.commissions.architecture.verticalslice.features.deals.DealRepository;
import com.chapman.edu.commissions.architecture.verticalslice.features.plans.CommissionPlanRepository;
import com.chapman.edu.commissions.architecture.verticalslice.infrastructure.exceptions.ResourceNotFoundException;
import com.chapman.edu.commissions.architecture.verticalslice.infrastructure.exceptions.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for Story COMM-01.
 * Failing tests written before implementation. The AI's job is to make these pass.
 *
 * Run with: mvn test -Dtest=CommissionCalculationServiceTest
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CommissionCalculationService.calculate")
class CommissionCalculationServiceTest {

    @Mock
    DealRepository dealRepository;

    @Mock
    CommissionPlanRepository commissionPlanRepository;

    @Mock
    CommissionCalculationRepository commissionCalculationRepository;

    @InjectMocks
    CommissionCalculationService service;

    private Deal deal;
    private CommissionPlan plan;

    @BeforeEach
    void setUp() {
        deal = new Deal();
        deal.setId("deal-1");
        deal.setSalesRepId("rep-1");
        deal.setValue(new BigDecimal("10000.00"));

        plan = new CommissionPlan();
        plan.setId("plan-1");
        plan.setName("Standard 5%");
        plan.setRules(List.of(rule("std", new BigDecimal("5.0"), RuleType.STANDARD, 1)));
    }

    private CommissionRule rule(String name, BigDecimal rate, RuleType type, int priority) {
        CommissionRule r = new CommissionRule();
        r.setName(name);
        r.setRate(rate);
        r.setType(type);
        r.setPriority(priority);
        return r;
    }

    private void stubFound() {
        when(dealRepository.findById("deal-1")).thenReturn(Optional.of(deal));
        when(commissionPlanRepository.findById("plan-1")).thenReturn(Optional.of(plan));
        when(commissionCalculationRepository.save(any(CommissionCalculation.class)))
                .thenAnswer(inv -> {
                    CommissionCalculation c = inv.getArgument(0);
                    c.setId("calc-1");
                    return c;
                });
    }

    @Nested
    @DisplayName("happy path")
    class HappyPath {

        @Test
        @DisplayName("returns base/gross/net commission of 500.00 for 10000.00 deal at 5% rate")
        void returnsBaseCommission500For10kAt5Percent() {
            // Arrange
            stubFound();
            CalculateCommissionRequest request = new CalculateCommissionRequest("deal-1", "plan-1");

            // Act
            CommissionCalculationResponse response = service.calculate(request);

            // Assert
            assertThat(response.baseCommission()).isEqualByComparingTo("500.00");
            assertThat(response.grossCommission()).isEqualByComparingTo("500.00");
            assertThat(response.netCommission()).isEqualByComparingTo("500.00");
            assertThat(response.status()).isEqualTo(CommissionStatus.CALCULATED);
            assertThat(response.calculationDate()).isEqualTo(LocalDate.now());
            assertThat(response.dealId()).isEqualTo("deal-1");
            assertThat(response.salesRepId()).isEqualTo("rep-1");
            assertThat(response.planId()).isEqualTo("plan-1");
        }

        @Test
        @DisplayName("persists exactly one row with the response id")
        void persistsExactlyOneRow() {
            // Arrange
            stubFound();
            CalculateCommissionRequest request = new CalculateCommissionRequest("deal-1", "plan-1");

            // Act
            CommissionCalculationResponse response = service.calculate(request);

            // Assert
            assertThat(response.id()).isEqualTo("calc-1");
            verify(commissionCalculationRepository, times(1)).save(any(CommissionCalculation.class));
        }

        @Test
        @DisplayName("calling calculate twice produces two distinct persisted rows")
        void appendOnlyCreatesNewRowEachCall() {
            // Arrange
            when(dealRepository.findById("deal-1")).thenReturn(Optional.of(deal));
            when(commissionPlanRepository.findById("plan-1")).thenReturn(Optional.of(plan));
            when(commissionCalculationRepository.save(any(CommissionCalculation.class)))
                    .thenAnswer(inv -> {
                        CommissionCalculation c = inv.getArgument(0);
                        c.setId("calc-" + System.nanoTime());
                        return c;
                    });
            CalculateCommissionRequest request = new CalculateCommissionRequest("deal-1", "plan-1");

            // Act
            CommissionCalculationResponse first = service.calculate(request);
            CommissionCalculationResponse second = service.calculate(request);

            // Assert
            assertThat(first.id()).isNotEqualTo(second.id());
            verify(commissionCalculationRepository, times(2)).save(any(CommissionCalculation.class));
        }
    }

    @Nested
    @DisplayName("not-found errors")
    class NotFoundErrors {

        @Test
        @DisplayName("missing deal throws ResourceNotFoundException naming 'deal' and persists nothing")
        void dealNotFound() {
            // Arrange
            when(dealRepository.findById("missing-deal")).thenReturn(Optional.empty());
            CalculateCommissionRequest request = new CalculateCommissionRequest("missing-deal", "plan-1");

            // Act / Assert
            assertThatThrownBy(() -> service.calculate(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("deal")
                    .hasMessageContaining("missing-deal");
            verify(commissionCalculationRepository, never()).save(any());
        }

        @Test
        @DisplayName("missing plan throws ResourceNotFoundException naming 'plan' and persists nothing")
        void planNotFound() {
            // Arrange
            when(dealRepository.findById("deal-1")).thenReturn(Optional.of(deal));
            when(commissionPlanRepository.findById("missing-plan")).thenReturn(Optional.empty());
            CalculateCommissionRequest request = new CalculateCommissionRequest("deal-1", "missing-plan");

            // Act / Assert
            assertThatThrownBy(() -> service.calculate(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("plan")
                    .hasMessageContaining("missing-plan");
            verify(commissionCalculationRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("request validation")
    class RequestValidation {

        @Test
        @DisplayName("null dealId fails CalculateCommissionRequest.validate()")
        void nullDealId() {
            assertThatThrownBy(() -> new CalculateCommissionRequest(null, "plan-1").validate())
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("dealId");
        }

        @Test
        @DisplayName("blank dealId fails CalculateCommissionRequest.validate()")
        void blankDealId() {
            assertThatThrownBy(() -> new CalculateCommissionRequest("   ", "plan-1").validate())
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("dealId");
        }

        @Test
        @DisplayName("null planId fails CalculateCommissionRequest.validate()")
        void nullPlanId() {
            assertThatThrownBy(() -> new CalculateCommissionRequest("deal-1", null).validate())
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("planId");
        }

        @Test
        @DisplayName("blank planId fails CalculateCommissionRequest.validate()")
        void blankPlanId() {
            assertThatThrownBy(() -> new CalculateCommissionRequest("deal-1", "").validate())
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("planId");
        }
    }

    @Nested
    @DisplayName("plan rule selection")
    class PlanRuleSelection {

        @Test
        @DisplayName("plan with no STANDARD rule throws ValidationException naming the plan id")
        void noStandardRule() {
            // Arrange
            plan.setRules(List.of(
                    rule("bonus", new BigDecimal("100"), RuleType.BONUS, 1),
                    rule("acc",   new BigDecimal("1.5"), RuleType.ACCELERATOR, 2)
            ));
            when(dealRepository.findById("deal-1")).thenReturn(Optional.of(deal));
            when(commissionPlanRepository.findById("plan-1")).thenReturn(Optional.of(plan));
            CalculateCommissionRequest request = new CalculateCommissionRequest("deal-1", "plan-1");

            // Act / Assert
            assertThatThrownBy(() -> service.calculate(request))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("plan-1");
            verify(commissionCalculationRepository, never()).save(any());
        }

        @Test
        @DisplayName("when multiple STANDARD rules exist, the lowest priority value wins")
        void lowestPriorityStandardRuleWins() {
            // Arrange: priority=1 rate=5, priority=2 rate=10. priority 1 (lower number) should win.
            plan.setRules(List.of(
                    rule("std-low",  new BigDecimal("5.0"),  RuleType.STANDARD, 1),
                    rule("std-high", new BigDecimal("10.0"), RuleType.STANDARD, 2)
            ));
            stubFound();
            CalculateCommissionRequest request = new CalculateCommissionRequest("deal-1", "plan-1");

            // Act
            CommissionCalculationResponse response = service.calculate(request);

            // Assert: 10000 * 5% = 500.00, NOT 1000.00
            assertThat(response.baseCommission()).isEqualByComparingTo("500.00");
        }
    }

    @Nested
    @DisplayName("decimal precision")
    class DecimalPrecision {

        @Test
        @DisplayName("scale=2 with HALF_UP rounding: 10000.00 * 3.333% = 333.30")
        void halfUpRoundingAtScale2() {
            // Arrange
            plan.setRules(List.of(rule("std", new BigDecimal("3.333"), RuleType.STANDARD, 1)));
            stubFound();
            CalculateCommissionRequest request = new CalculateCommissionRequest("deal-1", "plan-1");

            // Act
            CommissionCalculationResponse response = service.calculate(request);

            // Assert
            assertThat(response.baseCommission()).isEqualByComparingTo("333.30");
            assertThat(response.baseCommission().scale()).isEqualTo(2);
        }
    }
}
