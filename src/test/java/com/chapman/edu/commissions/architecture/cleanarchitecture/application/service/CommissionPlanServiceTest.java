package com.chapman.edu.commissions.architecture.cleanarchitecture.application.service;

import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.AddRuleCommand;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.CreatePlanCommand;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.PlanResult;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.port.out.CommissionPlanRepositoryPort;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.exception.DomainException;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.exception.EntityNotFoundException;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.CommissionPlan;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.PlanStatus;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.RuleType;
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
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommissionPlanServiceTest {

    @Mock
    private CommissionPlanRepositoryPort planRepository;

    @InjectMocks
    private CommissionPlanService planService;

    private CommissionPlan testPlan;

    @BeforeEach
    void setUp() {
        testPlan = new CommissionPlan("Standard Plan", Currency.getInstance("USD"),
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
        testPlan.setId("plan-1");
    }

    @Nested
    @DisplayName("createPlan")
    class CreatePlan {

        @Test
        @DisplayName("should create plan and return result when command is valid")
        void createPlan_WithValidCommand_ShouldReturnPlanResult() {
            // Given
            CreatePlanCommand command = new CreatePlanCommand(
                    "New Plan", "USD",
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)
            );
            CommissionPlan savedPlan = new CommissionPlan("New Plan", Currency.getInstance("USD"),
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
            savedPlan.setId("plan-2");

            when(planRepository.save(any(CommissionPlan.class))).thenReturn(savedPlan);

            // When
            PlanResult result = planService.createPlan(command);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo("plan-2");
            assertThat(result.name()).isEqualTo("New Plan");
            assertThat(result.currency()).isEqualTo("USD");
            assertThat(result.status()).isEqualTo("DRAFT");
            assertThat(result.effectiveStartDate()).isEqualTo(LocalDate.of(2026, 1, 1));
            assertThat(result.effectiveEndDate()).isEqualTo(LocalDate.of(2026, 12, 31));
            verify(planRepository, times(1)).save(any(CommissionPlan.class));
        }

        @Test
        @DisplayName("should throw DomainException when plan name is blank")
        void createPlan_WithBlankName_ShouldThrowDomainException() {
            // Given
            CreatePlanCommand command = new CreatePlanCommand(
                    "", "USD", LocalDate.now(), null
            );

            // When & Then
            assertThatThrownBy(() -> planService.createPlan(command))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("Plan name must not be blank");
            verify(planRepository, never()).save(any(CommissionPlan.class));
        }

        @Test
        @DisplayName("should throw DomainException when plan name is null")
        void createPlan_WithNullName_ShouldThrowDomainException() {
            // Given
            CreatePlanCommand command = new CreatePlanCommand(
                    null, "USD", LocalDate.now(), null
            );

            // When & Then
            assertThatThrownBy(() -> planService.createPlan(command))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("Plan name must not be blank");
            verify(planRepository, never()).save(any(CommissionPlan.class));
        }

        @Test
        @DisplayName("should throw DomainException when currency code is blank")
        void createPlan_WithBlankCurrency_ShouldThrowDomainException() {
            // Given
            CreatePlanCommand command = new CreatePlanCommand(
                    "Plan", "", LocalDate.now(), null
            );

            // When & Then
            assertThatThrownBy(() -> planService.createPlan(command))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("Currency code must not be blank");
            verify(planRepository, never()).save(any(CommissionPlan.class));
        }

        @Test
        @DisplayName("should throw DomainException when currency code is invalid")
        void createPlan_WithInvalidCurrency_ShouldThrowDomainException() {
            // Given
            CreatePlanCommand command = new CreatePlanCommand(
                    "Plan", "INVALID", LocalDate.now(), null
            );

            // When & Then
            assertThatThrownBy(() -> planService.createPlan(command))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("Invalid currency code");
            verify(planRepository, never()).save(any(CommissionPlan.class));
        }
    }

    @Nested
    @DisplayName("getPlan")
    class GetPlan {

        @Test
        @DisplayName("should return plan result when plan exists")
        void getPlan_WithExistingId_ShouldReturnPlanResult() {
            // Given
            when(planRepository.findById("plan-1")).thenReturn(Optional.of(testPlan));

            // When
            PlanResult result = planService.getPlan("plan-1");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo("plan-1");
            assertThat(result.name()).isEqualTo("Standard Plan");
            assertThat(result.currency()).isEqualTo("USD");
            verify(planRepository, times(1)).findById("plan-1");
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when plan does not exist")
        void getPlan_WithNonExistingId_ShouldThrowEntityNotFoundException() {
            // Given
            when(planRepository.findById("missing")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> planService.getPlan("missing"))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("CommissionPlan")
                    .hasMessageContaining("missing");
        }
    }

    @Nested
    @DisplayName("getAllPlans")
    class GetAllPlans {

        @Test
        @DisplayName("should return all plans")
        void getAllPlans_ShouldReturnAllPlans() {
            // Given
            CommissionPlan plan2 = new CommissionPlan("Premium Plan", Currency.getInstance("EUR"),
                    LocalDate.of(2026, 3, 1), LocalDate.of(2026, 9, 30));
            plan2.setId("plan-2");
            when(planRepository.findAll()).thenReturn(Arrays.asList(testPlan, plan2));

            // When
            List<PlanResult> results = planService.getAllPlans();

            // Then
            assertThat(results).hasSize(2);
            assertThat(results.get(0).id()).isEqualTo("plan-1");
            assertThat(results.get(1).id()).isEqualTo("plan-2");
            assertThat(results.get(1).currency()).isEqualTo("EUR");
            verify(planRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("should return empty list when no plans exist")
        void getAllPlans_WhenEmpty_ShouldReturnEmptyList() {
            // Given
            when(planRepository.findAll()).thenReturn(List.of());

            // When
            List<PlanResult> results = planService.getAllPlans();

            // Then
            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("getPlansByStatus")
    class GetPlansByStatus {

        @Test
        @DisplayName("should return plans filtered by status")
        void getPlansByStatus_ShouldReturnFilteredPlans() {
            // Given
            testPlan.setStatus(PlanStatus.ACTIVE);
            when(planRepository.findByStatus(PlanStatus.ACTIVE)).thenReturn(List.of(testPlan));

            // When
            List<PlanResult> results = planService.getPlansByStatus(PlanStatus.ACTIVE);

            // Then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).status()).isEqualTo("ACTIVE");
            verify(planRepository, times(1)).findByStatus(PlanStatus.ACTIVE);
        }

        @Test
        @DisplayName("should return empty list when no plans match status")
        void getPlansByStatus_WhenNoneMatch_ShouldReturnEmptyList() {
            // Given
            when(planRepository.findByStatus(PlanStatus.ACTIVE)).thenReturn(List.of());

            // When
            List<PlanResult> results = planService.getPlansByStatus(PlanStatus.ACTIVE);

            // Then
            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("activatePlan")
    class ActivatePlan {

        @Test
        @DisplayName("should activate plan and return result")
        void activatePlan_WithExistingPlan_ShouldActivate() {
            // Given
            when(planRepository.findById("plan-1")).thenReturn(Optional.of(testPlan));
            when(planRepository.save(any(CommissionPlan.class))).thenReturn(testPlan);

            // When
            PlanResult result = planService.activatePlan("plan-1");

            // Then
            assertThat(result).isNotNull();
            assertThat(testPlan.getStatus()).isEqualTo(PlanStatus.ACTIVE);
            verify(planRepository, times(1)).findById("plan-1");
            verify(planRepository, times(1)).save(testPlan);
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when plan does not exist")
        void activatePlan_WithNonExistingPlan_ShouldThrowEntityNotFoundException() {
            // Given
            when(planRepository.findById("missing")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> planService.activatePlan("missing"))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("CommissionPlan")
                    .hasMessageContaining("missing");
            verify(planRepository, never()).save(any(CommissionPlan.class));
        }
    }

    @Nested
    @DisplayName("addRuleToPlan")
    class AddRuleToPlan {

        @Test
        @DisplayName("should add rule to plan and return result")
        void addRuleToPlan_WithValidCommand_ShouldAddRule() {
            // Given
            AddRuleCommand command = new AddRuleCommand(
                    "Base Commission", "10% base rate",
                    new BigDecimal("10.00"), RuleType.STANDARD, 1
            );
            when(planRepository.findById("plan-1")).thenReturn(Optional.of(testPlan));
            when(planRepository.save(any(CommissionPlan.class))).thenReturn(testPlan);

            // When
            PlanResult result = planService.addRuleToPlan("plan-1", command);

            // Then
            assertThat(result).isNotNull();
            assertThat(testPlan.getRules()).hasSize(1);
            assertThat(testPlan.getRules().get(0).getName()).isEqualTo("Base Commission");
            assertThat(testPlan.getRules().get(0).getRate()).isEqualByComparingTo(new BigDecimal("10.00"));
            assertThat(testPlan.getRules().get(0).getType()).isEqualTo(RuleType.STANDARD);
            assertThat(testPlan.getRules().get(0).getDescription()).isEqualTo("10% base rate");
            assertThat(testPlan.getRules().get(0).getPriority()).isEqualTo(1);
            verify(planRepository, times(1)).save(testPlan);
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when plan does not exist")
        void addRuleToPlan_WithNonExistingPlan_ShouldThrowEntityNotFoundException() {
            // Given
            AddRuleCommand command = new AddRuleCommand(
                    "Rule", "desc", new BigDecimal("5.00"), RuleType.STANDARD, 1
            );
            when(planRepository.findById("missing")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> planService.addRuleToPlan("missing", command))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("CommissionPlan")
                    .hasMessageContaining("missing");
            verify(planRepository, never()).save(any(CommissionPlan.class));
        }

        @Test
        @DisplayName("should throw DomainException when rule name is blank")
        void addRuleToPlan_WithBlankRuleName_ShouldThrowDomainException() {
            // Given
            AddRuleCommand command = new AddRuleCommand(
                    "", "desc", new BigDecimal("5.00"), RuleType.STANDARD, 1
            );

            // When & Then
            assertThatThrownBy(() -> planService.addRuleToPlan("plan-1", command))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("Rule name must not be blank");
            verify(planRepository, never()).save(any(CommissionPlan.class));
        }

        @Test
        @DisplayName("should throw DomainException when rule rate is null")
        void addRuleToPlan_WithNullRate_ShouldThrowDomainException() {
            // Given
            AddRuleCommand command = new AddRuleCommand(
                    "Rule", "desc", null, RuleType.STANDARD, 1
            );

            // When & Then
            assertThatThrownBy(() -> planService.addRuleToPlan("plan-1", command))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("Rule rate must not be null");
            verify(planRepository, never()).save(any(CommissionPlan.class));
        }
    }

    @Nested
    @DisplayName("deletePlan")
    class DeletePlan {

        @Test
        @DisplayName("should delete plan when it exists")
        void deletePlan_WithExistingId_ShouldDelete() {
            // Given
            when(planRepository.findById("plan-1")).thenReturn(Optional.of(testPlan));

            // When
            planService.deletePlan("plan-1");

            // Then
            verify(planRepository, times(1)).findById("plan-1");
            verify(planRepository, times(1)).deleteById("plan-1");
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when plan does not exist")
        void deletePlan_WithNonExistingId_ShouldThrowEntityNotFoundException() {
            // Given
            when(planRepository.findById("missing")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> planService.deletePlan("missing"))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("CommissionPlan")
                    .hasMessageContaining("missing");
            verify(planRepository, never()).deleteById(anyString());
        }
    }
}
