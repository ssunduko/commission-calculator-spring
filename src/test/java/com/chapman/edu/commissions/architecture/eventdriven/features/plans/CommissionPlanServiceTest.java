package com.chapman.edu.commissions.architecture.eventdriven.features.plans;

import com.chapman.edu.commissions.architecture.eventdriven.domain.CommissionPlan;
import com.chapman.edu.commissions.architecture.eventdriven.domain.PlanStatus;
import com.chapman.edu.commissions.architecture.eventdriven.domain.event.CommissionPlanActivatedEvent;
import com.chapman.edu.commissions.architecture.eventdriven.domain.event.CommissionPlanCreatedEvent;
import com.chapman.edu.commissions.architecture.eventdriven.domain.event.RuleAddedToPlanEvent;
import com.chapman.edu.commissions.architecture.eventdriven.infrastructure.exceptions.ResourceNotFoundException;
import com.chapman.edu.commissions.architecture.eventdriven.infrastructure.exceptions.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

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

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private CommissionPlanService planService;

    private CommissionPlan testPlan;

    @BeforeEach
    void setUp() {
        planService = new CommissionPlanService(planRepository, eventPublisher);
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
        verify(eventPublisher).publishEvent(any(CommissionPlanCreatedEvent.class));
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
    void activatePlan_WithExistingPlan_ShouldActivatePlanAndPublishEvent() {
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
        verify(eventPublisher).publishEvent(any(CommissionPlanActivatedEvent.class));
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
