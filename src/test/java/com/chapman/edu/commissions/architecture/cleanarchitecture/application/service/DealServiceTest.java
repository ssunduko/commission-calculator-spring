package com.chapman.edu.commissions.architecture.cleanarchitecture.application.service;

import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.CreateDealCommand;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.DealResult;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.UpdateDealCommand;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.port.out.DealRepositoryPort;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.exception.DomainException;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.exception.EntityNotFoundException;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.Deal;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.DealStatus;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DealService (Clean Architecture)")
class DealServiceTest {

    @Mock
    private DealRepositoryPort dealRepository;

    @InjectMocks
    private DealService dealService;

    private Deal testDeal;

    @BeforeEach
    void setUp() {
        testDeal = new Deal("Test Deal", new BigDecimal("100000"), "REP001");
        testDeal.setId("deal-1");
    }

    @Nested
    @DisplayName("createDeal")
    class CreateDeal {

        @Test
        @DisplayName("should create deal and return result when command is valid")
        void createDeal_WithValidCommand_ShouldReturnDealResult() {
            // Given
            CreateDealCommand command = new CreateDealCommand("New Deal", new BigDecimal("50000"), "REP002");
            Deal savedDeal = new Deal("New Deal", new BigDecimal("50000"), "REP002");
            savedDeal.setId("deal-2");

            when(dealRepository.save(any(Deal.class))).thenReturn(savedDeal);

            // When
            DealResult result = dealService.createDeal(command);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo("deal-2");
            assertThat(result.title()).isEqualTo("New Deal");
            assertThat(result.value()).isEqualTo(new BigDecimal("50000"));
            assertThat(result.salesRepId()).isEqualTo("REP002");
            assertThat(result.status()).isEqualTo("OPEN");
            verify(dealRepository, times(1)).save(any(Deal.class));
        }

        @Test
        @DisplayName("should throw DomainException when title is blank")
        void createDeal_WithBlankTitle_ShouldThrowDomainException() {
            // Given
            CreateDealCommand command = new CreateDealCommand("", new BigDecimal("50000"), "REP002");

            // When & Then
            assertThatThrownBy(() -> dealService.createDeal(command))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("title must not be blank");
            verify(dealRepository, never()).save(any(Deal.class));
        }

        @Test
        @DisplayName("should throw DomainException when title is null")
        void createDeal_WithNullTitle_ShouldThrowDomainException() {
            // Given
            CreateDealCommand command = new CreateDealCommand(null, new BigDecimal("50000"), "REP002");

            // When & Then
            assertThatThrownBy(() -> dealService.createDeal(command))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("title must not be blank");
            verify(dealRepository, never()).save(any(Deal.class));
        }

        @Test
        @DisplayName("should throw DomainException when value is zero")
        void createDeal_WithZeroValue_ShouldThrowDomainException() {
            // Given
            CreateDealCommand command = new CreateDealCommand("Deal", BigDecimal.ZERO, "REP002");

            // When & Then
            assertThatThrownBy(() -> dealService.createDeal(command))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("value must be greater than zero");
            verify(dealRepository, never()).save(any(Deal.class));
        }

        @Test
        @DisplayName("should throw DomainException when value is negative")
        void createDeal_WithNegativeValue_ShouldThrowDomainException() {
            // Given
            CreateDealCommand command = new CreateDealCommand("Deal", new BigDecimal("-100"), "REP002");

            // When & Then
            assertThatThrownBy(() -> dealService.createDeal(command))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("value must be greater than zero");
            verify(dealRepository, never()).save(any(Deal.class));
        }

        @Test
        @DisplayName("should throw DomainException when value is null")
        void createDeal_WithNullValue_ShouldThrowDomainException() {
            // Given
            CreateDealCommand command = new CreateDealCommand("Deal", null, "REP002");

            // When & Then
            assertThatThrownBy(() -> dealService.createDeal(command))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("value must be greater than zero");
            verify(dealRepository, never()).save(any(Deal.class));
        }

        @Test
        @DisplayName("should throw DomainException when salesRepId is blank")
        void createDeal_WithBlankSalesRepId_ShouldThrowDomainException() {
            // Given
            CreateDealCommand command = new CreateDealCommand("Deal", new BigDecimal("50000"), "");

            // When & Then
            assertThatThrownBy(() -> dealService.createDeal(command))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("Sales rep ID must not be blank");
            verify(dealRepository, never()).save(any(Deal.class));
        }

        @Test
        @DisplayName("should throw DomainException when salesRepId is null")
        void createDeal_WithNullSalesRepId_ShouldThrowDomainException() {
            // Given
            CreateDealCommand command = new CreateDealCommand("Deal", new BigDecimal("50000"), null);

            // When & Then
            assertThatThrownBy(() -> dealService.createDeal(command))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("Sales rep ID must not be blank");
            verify(dealRepository, never()).save(any(Deal.class));
        }
    }

    @Nested
    @DisplayName("getDeal")
    class GetDeal {

        @Test
        @DisplayName("should return deal result when deal exists")
        void getDeal_WithExistingId_ShouldReturnDealResult() {
            // Given
            when(dealRepository.findById("deal-1")).thenReturn(Optional.of(testDeal));

            // When
            DealResult result = dealService.getDeal("deal-1");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo("deal-1");
            assertThat(result.title()).isEqualTo("Test Deal");
            assertThat(result.value()).isEqualTo(new BigDecimal("100000"));
            assertThat(result.salesRepId()).isEqualTo("REP001");
            verify(dealRepository, times(1)).findById("deal-1");
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when deal does not exist")
        void getDeal_WithNonExistingId_ShouldThrowEntityNotFoundException() {
            // Given
            when(dealRepository.findById("missing-id")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> dealService.getDeal("missing-id"))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Deal")
                    .hasMessageContaining("missing-id");
            verify(dealRepository, times(1)).findById("missing-id");
        }
    }

    @Nested
    @DisplayName("getAllDeals")
    class GetAllDeals {

        @Test
        @DisplayName("should return all deals")
        void getAllDeals_ShouldReturnAllDeals() {
            // Given
            Deal deal2 = new Deal("Second Deal", new BigDecimal("75000"), "REP002");
            deal2.setId("deal-2");
            when(dealRepository.findAll()).thenReturn(Arrays.asList(testDeal, deal2));

            // When
            List<DealResult> results = dealService.getAllDeals();

            // Then
            assertThat(results).hasSize(2);
            assertThat(results.get(0).id()).isEqualTo("deal-1");
            assertThat(results.get(1).id()).isEqualTo("deal-2");
            verify(dealRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("should return empty list when no deals exist")
        void getAllDeals_WhenNoneExist_ShouldReturnEmptyList() {
            // Given
            when(dealRepository.findAll()).thenReturn(Collections.emptyList());

            // When
            List<DealResult> results = dealService.getAllDeals();

            // Then
            assertThat(results).isEmpty();
            verify(dealRepository, times(1)).findAll();
        }
    }

    @Nested
    @DisplayName("getDealsBySalesRep")
    class GetDealsBySalesRep {

        @Test
        @DisplayName("should return deals filtered by sales rep ID")
        void getDealsBySalesRep_ShouldReturnFilteredDeals() {
            // Given
            when(dealRepository.findBySalesRepId("REP001")).thenReturn(List.of(testDeal));

            // When
            List<DealResult> results = dealService.getDealsBySalesRep("REP001");

            // Then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).salesRepId()).isEqualTo("REP001");
            verify(dealRepository, times(1)).findBySalesRepId("REP001");
        }

        @Test
        @DisplayName("should return empty list when sales rep has no deals")
        void getDealsBySalesRep_WhenNoneMatch_ShouldReturnEmptyList() {
            // Given
            when(dealRepository.findBySalesRepId("REP999")).thenReturn(Collections.emptyList());

            // When
            List<DealResult> results = dealService.getDealsBySalesRep("REP999");

            // Then
            assertThat(results).isEmpty();
            verify(dealRepository, times(1)).findBySalesRepId("REP999");
        }
    }

    @Nested
    @DisplayName("getDealsByStatus")
    class GetDealsByStatus {

        @Test
        @DisplayName("should return deals filtered by status")
        void getDealsByStatus_ShouldReturnFilteredDeals() {
            // Given
            testDeal.setStatus(DealStatus.WON);
            when(dealRepository.findByStatus(DealStatus.WON)).thenReturn(List.of(testDeal));

            // When
            List<DealResult> results = dealService.getDealsByStatus(DealStatus.WON);

            // Then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).status()).isEqualTo("WON");
            verify(dealRepository, times(1)).findByStatus(DealStatus.WON);
        }

        @Test
        @DisplayName("should return empty list when no deals match status")
        void getDealsByStatus_WhenNoneMatch_ShouldReturnEmptyList() {
            // Given
            when(dealRepository.findByStatus(DealStatus.CANCELLED)).thenReturn(Collections.emptyList());

            // When
            List<DealResult> results = dealService.getDealsByStatus(DealStatus.CANCELLED);

            // Then
            assertThat(results).isEmpty();
            verify(dealRepository, times(1)).findByStatus(DealStatus.CANCELLED);
        }
    }

    @Nested
    @DisplayName("updateDeal")
    class UpdateDeal {

        @Test
        @DisplayName("should update all fields and return result")
        void updateDeal_WithAllFields_ShouldUpdateAndReturnResult() {
            // Given
            LocalDate closeDate = LocalDate.of(2026, 6, 15);
            UpdateDealCommand command = new UpdateDealCommand(
                    "Updated Title", new BigDecimal("120000"), DealStatus.WON, closeDate);

            when(dealRepository.findById("deal-1")).thenReturn(Optional.of(testDeal));
            when(dealRepository.save(any(Deal.class))).thenReturn(testDeal);

            // When
            DealResult result = dealService.updateDeal("deal-1", command);

            // Then
            assertThat(result).isNotNull();
            verify(dealRepository, times(1)).findById("deal-1");
            verify(dealRepository, times(1)).save(testDeal);
            assertThat(testDeal.getTitle()).isEqualTo("Updated Title");
            assertThat(testDeal.getValue()).isEqualTo(new BigDecimal("120000"));
            assertThat(testDeal.getStatus()).isEqualTo(DealStatus.WON);
            assertThat(testDeal.getCloseDate()).isEqualTo(closeDate);
        }

        @Test
        @DisplayName("should update only title when other fields are null")
        void updateDeal_WithOnlyTitle_ShouldUpdateTitleOnly() {
            // Given
            UpdateDealCommand command = new UpdateDealCommand("New Title", null, null, null);
            BigDecimal originalValue = testDeal.getValue();
            DealStatus originalStatus = testDeal.getStatus();

            when(dealRepository.findById("deal-1")).thenReturn(Optional.of(testDeal));
            when(dealRepository.save(any(Deal.class))).thenReturn(testDeal);

            // When
            dealService.updateDeal("deal-1", command);

            // Then
            assertThat(testDeal.getTitle()).isEqualTo("New Title");
            assertThat(testDeal.getValue()).isEqualTo(originalValue);
            assertThat(testDeal.getStatus()).isEqualTo(originalStatus);
            assertThat(testDeal.getCloseDate()).isNull();
            verify(dealRepository, times(1)).save(testDeal);
        }

        @Test
        @DisplayName("should update only value when other fields are null")
        void updateDeal_WithOnlyValue_ShouldUpdateValueOnly() {
            // Given
            UpdateDealCommand command = new UpdateDealCommand(null, new BigDecimal("200000"), null, null);
            String originalTitle = testDeal.getTitle();

            when(dealRepository.findById("deal-1")).thenReturn(Optional.of(testDeal));
            when(dealRepository.save(any(Deal.class))).thenReturn(testDeal);

            // When
            dealService.updateDeal("deal-1", command);

            // Then
            assertThat(testDeal.getTitle()).isEqualTo(originalTitle);
            assertThat(testDeal.getValue()).isEqualTo(new BigDecimal("200000"));
            verify(dealRepository, times(1)).save(testDeal);
        }

        @Test
        @DisplayName("should update only status when other fields are null")
        void updateDeal_WithOnlyStatus_ShouldUpdateStatusOnly() {
            // Given
            UpdateDealCommand command = new UpdateDealCommand(null, null, DealStatus.LOST, null);

            when(dealRepository.findById("deal-1")).thenReturn(Optional.of(testDeal));
            when(dealRepository.save(any(Deal.class))).thenReturn(testDeal);

            // When
            dealService.updateDeal("deal-1", command);

            // Then
            assertThat(testDeal.getStatus()).isEqualTo(DealStatus.LOST);
            verify(dealRepository, times(1)).save(testDeal);
        }

        @Test
        @DisplayName("should update only closeDate when other fields are null")
        void updateDeal_WithOnlyCloseDate_ShouldUpdateCloseDateOnly() {
            // Given
            LocalDate closeDate = LocalDate.of(2026, 12, 31);
            UpdateDealCommand command = new UpdateDealCommand(null, null, null, closeDate);

            when(dealRepository.findById("deal-1")).thenReturn(Optional.of(testDeal));
            when(dealRepository.save(any(Deal.class))).thenReturn(testDeal);

            // When
            dealService.updateDeal("deal-1", command);

            // Then
            assertThat(testDeal.getCloseDate()).isEqualTo(closeDate);
            verify(dealRepository, times(1)).save(testDeal);
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when deal does not exist")
        void updateDeal_WithNonExistingId_ShouldThrowEntityNotFoundException() {
            // Given
            UpdateDealCommand command = new UpdateDealCommand("Updated", null, null, null);
            when(dealRepository.findById("missing-id")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> dealService.updateDeal("missing-id", command))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Deal")
                    .hasMessageContaining("missing-id");
            verify(dealRepository, times(1)).findById("missing-id");
            verify(dealRepository, never()).save(any(Deal.class));
        }
    }

    @Nested
    @DisplayName("deleteDeal")
    class DeleteDeal {

        @Test
        @DisplayName("should delete deal when it exists")
        void deleteDeal_WithExistingId_ShouldDeleteDeal() {
            // Given
            when(dealRepository.findById("deal-1")).thenReturn(Optional.of(testDeal));

            // When
            dealService.deleteDeal("deal-1");

            // Then
            verify(dealRepository, times(1)).findById("deal-1");
            verify(dealRepository, times(1)).deleteById("deal-1");
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when deal does not exist")
        void deleteDeal_WithNonExistingId_ShouldThrowEntityNotFoundException() {
            // Given
            when(dealRepository.findById("missing-id")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> dealService.deleteDeal("missing-id"))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Deal")
                    .hasMessageContaining("missing-id");
            verify(dealRepository, times(1)).findById("missing-id");
            verify(dealRepository, never()).deleteById(anyString());
        }
    }
}
