package com.chapman.edu.commissions.verticalslice.features.deals;

import com.chapman.edu.commissions.architecture.verticalslice.domain.Deal;
import com.chapman.edu.commissions.architecture.verticalslice.domain.DealStatus;
import com.chapman.edu.commissions.architecture.verticalslice.features.deals.*;
import com.chapman.edu.commissions.architecture.verticalslice.infrastructure.exceptions.ResourceNotFoundException;
import com.chapman.edu.commissions.architecture.verticalslice.infrastructure.exceptions.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DealServiceTest {

    @Mock
    private DealRepository dealRepository;

    @InjectMocks
    private DealService dealService;

    private Deal testDeal;

    @BeforeEach
    void setUp() {
        testDeal = new Deal("Test Deal", new BigDecimal("100000"), "REP001");
        testDeal.setId("1");
    }

    @Test
    void createDeal_WithValidRequest_ShouldReturnDealResponse() {
        // Given
        CreateDealRequest request = new CreateDealRequest("New Deal", new BigDecimal("50000"), "REP002");
        Deal savedDeal = new Deal("New Deal", new BigDecimal("50000"), "REP002");
        savedDeal.setId("2");

        when(dealRepository.save(any(Deal.class))).thenReturn(savedDeal);

        // When
        DealResponse response = dealService.createDeal(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo("2");
        assertThat(response.title()).isEqualTo("New Deal");
        assertThat(response.value()).isEqualTo(new BigDecimal("50000"));
        assertThat(response.salesRepId()).isEqualTo("REP002");
        verify(dealRepository, times(1)).save(any(Deal.class));
    }

    @Test
    void getDeal_WithExistingId_ShouldReturnDealResponse() {
        // Given
        when(dealRepository.findById("1")).thenReturn(Optional.of(testDeal));

        // When
        DealResponse response = dealService.getDeal("1");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo("1");
        assertThat(response.title()).isEqualTo("Test Deal");
        verify(dealRepository, times(1)).findById("1");
    }

    @Test
    void getDeal_WithNonExistingId_ShouldThrowResourceNotFoundException() {
        // Given
        when(dealRepository.findById("999")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> dealService.getDeal("999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Deal")
                .hasMessageContaining("999");
        verify(dealRepository, times(1)).findById("999");
    }

    @Test
    void getAllDeals_ShouldReturnAllDeals() {
        // Given
        Deal deal2 = new Deal("Another Deal", new BigDecimal("75000"), "REP002");
        deal2.setId("2");
        when(dealRepository.findAll()).thenReturn(Arrays.asList(testDeal, deal2));

        // When
        List<DealResponse> responses = dealService.getAllDeals();

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).id()).isEqualTo("1");
        assertThat(responses.get(1).id()).isEqualTo("2");
        verify(dealRepository, times(1)).findAll();
    }

    @Test
    void getDealsBySalesRep_ShouldReturnFilteredDeals() {
        // Given
        when(dealRepository.findBySalesRepId("REP001")).thenReturn(Arrays.asList(testDeal));

        // When
        List<DealResponse> responses = dealService.getDealsBySalesRep("REP001");

        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).salesRepId()).isEqualTo("REP001");
        verify(dealRepository, times(1)).findBySalesRepId("REP001");
    }

    @Test
    void getDealsByStatus_ShouldReturnFilteredDeals() {
        // Given
        testDeal.setStatus(DealStatus.WON);
        when(dealRepository.findByStatus(DealStatus.WON)).thenReturn(Arrays.asList(testDeal));

        // When
        List<DealResponse> responses = dealService.getDealsByStatus(DealStatus.WON);

        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).status()).isEqualTo(DealStatus.WON);
        verify(dealRepository, times(1)).findByStatus(DealStatus.WON);
    }

    @Test
    void updateDeal_WithValidData_ShouldUpdateAndReturnDeal() {
        // Given
        UpdateDealRequest request = new UpdateDealRequest(
                "Updated Title",
                new BigDecimal("120000"),
                DealStatus.WON,
                LocalDate.now()
        );
        when(dealRepository.findById("1")).thenReturn(Optional.of(testDeal));
        when(dealRepository.save(any(Deal.class))).thenReturn(testDeal);

        // When
        DealResponse response = dealService.updateDeal("1", request);

        // Then
        assertThat(response).isNotNull();
        verify(dealRepository, times(1)).findById("1");
        verify(dealRepository, times(1)).save(testDeal);
        assertThat(testDeal.getTitle()).isEqualTo("Updated Title");
        assertThat(testDeal.getValue()).isEqualTo(new BigDecimal("120000"));
    }

    @Test
    void updateDeal_WithNegativeValue_ShouldThrowValidationException() {
        // Given
        UpdateDealRequest request = new UpdateDealRequest(
                null,
                new BigDecimal("-1000"),
                null,
                null
        );
        when(dealRepository.findById("1")).thenReturn(Optional.of(testDeal));

        // When & Then
        assertThatThrownBy(() -> dealService.updateDeal("1", request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("value must be greater than zero");
        verify(dealRepository, times(1)).findById("1");
        verify(dealRepository, never()).save(any(Deal.class));
    }

    @Test
    void updateDeal_WithNonExistingId_ShouldThrowResourceNotFoundException() {
        // Given
        UpdateDealRequest request = new UpdateDealRequest(
                "Updated Title",
                null,
                null,
                null
        );
        when(dealRepository.findById("999")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> dealService.updateDeal("999", request))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(dealRepository, times(1)).findById("999");
    }

    @Test
    void deleteDeal_WithExistingId_ShouldDeleteDeal() {
        // Given
        when(dealRepository.existsById("1")).thenReturn(true);

        // When
        dealService.deleteDeal("1");

        // Then
        verify(dealRepository, times(1)).existsById("1");
        verify(dealRepository, times(1)).deleteById("1");
    }

    @Test
    void deleteDeal_WithNonExistingId_ShouldThrowResourceNotFoundException() {
        // Given
        when(dealRepository.existsById("999")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> dealService.deleteDeal("999"))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(dealRepository, times(1)).existsById("999");
        verify(dealRepository, never()).deleteById(anyString());
    }
}
