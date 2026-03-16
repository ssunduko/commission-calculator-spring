package com.chapman.edu.commissions.architecture.verticalslice.features.plans;

import com.chapman.edu.commissions.architecture.verticalslice.domain.CommissionPlan;
import com.chapman.edu.commissions.architecture.verticalslice.domain.PlanStatus;
import com.chapman.edu.commissions.architecture.verticalslice.features.plans.CommissionPlanRepository;
import com.chapman.edu.commissions.architecture.verticalslice.features.plans.CommissionPlanResponse;
import com.chapman.edu.commissions.architecture.verticalslice.features.plans.CommissionPlanService;
import com.chapman.edu.commissions.architecture.verticalslice.features.plans.CreateCommissionPlanRequest;
import com.chapman.edu.commissions.architecture.verticalslice.infrastructure.exceptions.ResourceNotFoundException;
import com.chapman.edu.commissions.architecture.verticalslice.infrastructure.exceptions.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private CommissionPlanRepository planRepository;

    @InjectMocks
    private CommissionPlanService planService;

    private CommissionPlan testPlan;

    @BeforeEach
    void setUp() {
        testPlan = new CommissionPlan("Standard Plan", Currency.getInstance("USD"));
        testPlan.setId("1");
        testPlan.setStatus(PlanStatus.DRAFT);
    }

    @Test
    void createPlan_WithValidRequest_ShouldReturnPlanResponse() {
        // Given
        CreateCommissionPlanRequest request = new CreateCommissionPlanRequest(
                "New Plan",
                "USD",
                LocalDate.now(),
                LocalDate.now().plusMonths(6)
        );
        CommissionPlan savedPlan = new CommissionPlan("New Plan", Currency.getInstance("USD"));
        savedPlan.setId("2");

        when(planRepository.save(any(CommissionPlan.class))).thenReturn(savedPlan);

        // When
        CommissionPlanResponse response = planService.createPlan(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo("2");
        assertThat(response.name()).isEqualTo("New Plan");
        assertThat(response.currency()).isEqualTo("USD");
        verify(planRepository, times(1)).save(any(CommissionPlan.class));
    }

    @Test
    void createPlan_WithInvalidCurrency_ShouldThrowValidationException() {
        // Given
        CreateCommissionPlanRequest request = new CreateCommissionPlanRequest(
                "New Plan",
                "INVALID",
                LocalDate.now(),
                null
        );

        // When & Then
        assertThatThrownBy(() -> planService.createPlan(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid currency code");
        verify(planRepository, never()).save(any(CommissionPlan.class));
    }

    @Test
    void getPlan_WithExistingId_ShouldReturnPlanResponse() {
        // Given
        when(planRepository.findById("1")).thenReturn(Optional.of(testPlan));

        // When
        CommissionPlanResponse response = planService.getPlan("1");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo("1");
        assertThat(response.name()).isEqualTo("Standard Plan");
        verify(planRepository, times(1)).findById("1");
    }

    @Test
    void getPlan_WithNonExistingId_ShouldThrowResourceNotFoundException() {
        // Given
        when(planRepository.findById("999")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> planService.getPlan("999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Commission Plan")
                .hasMessageContaining("999");
    }

    @Test
    void getAllPlans_ShouldReturnAllPlans() {
        // Given
        CommissionPlan plan2 = new CommissionPlan("Premium Plan", Currency.getInstance("EUR"));
        plan2.setId("2");
        when(planRepository.findAll()).thenReturn(Arrays.asList(testPlan, plan2));

        // When
        List<CommissionPlanResponse> responses = planService.getAllPlans();

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).id()).isEqualTo("1");
        assertThat(responses.get(1).id()).isEqualTo("2");
        verify(planRepository, times(1)).findAll();
    }

    @Test
    void getPlansByStatus_ShouldReturnFilteredPlans() {
        // Given
        testPlan.setStatus(PlanStatus.ACTIVE);
        when(planRepository.findByStatus(PlanStatus.ACTIVE)).thenReturn(Arrays.asList(testPlan));

        // When
        List<CommissionPlanResponse> responses = planService.getPlansByStatus(PlanStatus.ACTIVE);

        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).status()).isEqualTo(PlanStatus.ACTIVE);
        verify(planRepository, times(1)).findByStatus(PlanStatus.ACTIVE);
    }

    @Test
    void activatePlan_WithExistingPlan_ShouldActivatePlan() {
        // Given
        when(planRepository.findById("1")).thenReturn(Optional.of(testPlan));
        when(planRepository.save(any(CommissionPlan.class))).thenReturn(testPlan);

        // When
        CommissionPlanResponse response = planService.activatePlan("1");

        // Then
        assertThat(response).isNotNull();
        assertThat(testPlan.getStatus()).isEqualTo(PlanStatus.ACTIVE);
        verify(planRepository, times(1)).findById("1");
        verify(planRepository, times(1)).save(testPlan);
    }

    @Test
    void deletePlan_WithExistingId_ShouldDeletePlan() {
        // Given
        when(planRepository.existsById("1")).thenReturn(true);

        // When
        planService.deletePlan("1");

        // Then
        verify(planRepository, times(1)).existsById("1");
        verify(planRepository, times(1)).deleteById("1");
    }

    @Test
    void deletePlan_WithNonExistingId_ShouldThrowResourceNotFoundException() {
        // Given
        when(planRepository.existsById("999")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> planService.deletePlan("999"))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(planRepository, times(1)).existsById("999");
        verify(planRepository, never()).deleteById(anyString());
    }
}
