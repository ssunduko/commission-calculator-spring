package com.chapman.edu.commissions.springboot.service;

import com.chapman.edu.commissions.model.Deal;
import com.chapman.edu.commissions.model.DealStatus;
import com.chapman.edu.commissions.springboot.dto.request.CreateDealRequest;
import com.chapman.edu.commissions.springboot.exception.BusinessValidationException;
import com.chapman.edu.commissions.springboot.exception.ResourceNotFoundException;
import com.chapman.edu.commissions.springboot.repository.DealRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for Spring Boot DealService.
 *
 * KEY CONCEPTS DEMONSTRATED:
 * - Testing the Service Layer pattern (Controller -> Service -> Repository)
 * - Mocking the Repository to isolate service business logic
 * - Testing business validation rules (e.g., can't reopen cancelled deal)
 * - Testing CRUD operations through the service layer
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SpringBoot DealService — Unit Tests")
class DealServiceTest {

    @Mock
    private DealRepository dealRepository;

    @InjectMocks
    private DealService dealService;

    private Deal testDeal;

    @BeforeEach
    void setUp() {
        testDeal = new Deal("Enterprise License", new BigDecimal("100000"), "rep-001");
        testDeal.setId("deal-001");
        testDeal.setStatus(DealStatus.OPEN);
    }

    @Test
    @DisplayName("createDeal should save and return new deal")
    void createDeal_shouldSaveAndReturnDeal() {
        CreateDealRequest request = new CreateDealRequest();
        request.setTitle("New Deal");
        request.setValue(new BigDecimal("50000"));
        request.setSalesRepId("rep-002");

        when(dealRepository.save(any(Deal.class))).thenAnswer(inv -> {
            Deal d = inv.getArgument(0);
            d.setId("deal-new");
            return d;
        });

        Deal result = dealService.createDeal(request);

        assertThat(result.getTitle()).isEqualTo("New Deal");
        assertThat(result.getStatus()).isEqualTo(DealStatus.OPEN);
        verify(dealRepository).save(any(Deal.class));
    }

    @Test
    @DisplayName("getDealById should return deal when it exists")
    void getDealById_shouldReturnDeal_whenExists() {
        when(dealRepository.findById("deal-001")).thenReturn(Optional.of(testDeal));

        Deal result = dealService.getDealById("deal-001");

        assertThat(result.getTitle()).isEqualTo("Enterprise License");
    }

    @Test
    @DisplayName("getDealById should throw ResourceNotFoundException when not found")
    void getDealById_shouldThrowResourceNotFoundException_whenNotExists() {
        when(dealRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dealService.getDealById("nonexistent"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getAllDeals should return all deals")
    void getAllDeals_shouldReturnAllDeals() {
        Deal deal2 = new Deal("Deal 2", new BigDecimal("25000"), "rep-002");
        deal2.setId("deal-002");

        when(dealRepository.findAll()).thenReturn(Arrays.asList(testDeal, deal2));

        List<Deal> result = dealService.getAllDeals();

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("getDealsBySalesRep should return filtered deals")
    void getDealsBySalesRep_shouldReturnFilteredDeals() {
        when(dealRepository.findBySalesRepId("rep-001")).thenReturn(List.of(testDeal));

        List<Deal> result = dealService.getDealsBySalesRep("rep-001");

        assertThat(result).hasSize(1);
        verify(dealRepository).findBySalesRepId("rep-001");
    }

    @Test
    @DisplayName("updateDealStatus should update and return deal")
    void updateDealStatus_shouldUpdateAndReturn() {
        when(dealRepository.findById("deal-001")).thenReturn(Optional.of(testDeal));
        when(dealRepository.save(any(Deal.class))).thenAnswer(inv -> inv.getArgument(0));

        Deal result = dealService.updateDealStatus("deal-001", DealStatus.WON);

        assertThat(result.getStatus()).isEqualTo(DealStatus.WON);
        verify(dealRepository).save(testDeal);
    }

    @Test
    @DisplayName("updateDealStatus should throw when reopening cancelled deal")
    void updateDealStatus_shouldThrowBusinessValidation_whenReopeningCancelledDeal() {
        testDeal.setStatus(DealStatus.CANCELLED);
        when(dealRepository.findById("deal-001")).thenReturn(Optional.of(testDeal));

        assertThatThrownBy(() -> dealService.updateDealStatus("deal-001", DealStatus.OPEN))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Cannot reopen a cancelled deal");

        verify(dealRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteDeal should delete when deal exists")
    void deleteDeal_shouldDelete_whenExists() {
        when(dealRepository.existsById("deal-001")).thenReturn(true);

        dealService.deleteDeal("deal-001");

        verify(dealRepository).deleteById("deal-001");
    }

    @Test
    @DisplayName("deleteDeal should throw ResourceNotFoundException when not found")
    void deleteDeal_shouldThrowResourceNotFoundException_whenNotExists() {
        when(dealRepository.existsById("nonexistent")).thenReturn(false);

        assertThatThrownBy(() -> dealService.deleteDeal("nonexistent"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(dealRepository, never()).deleteById(anyString());
    }
}
