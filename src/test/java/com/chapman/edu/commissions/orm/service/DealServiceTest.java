package com.chapman.edu.commissions.orm.service;

import com.chapman.edu.commissions.orm.entity.Deal;
import com.chapman.edu.commissions.orm.entity.DealProduct;
import com.chapman.edu.commissions.orm.entity.DealStatus;
import com.chapman.edu.commissions.orm.entity.User;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ORM DealService.
 *
 * KEY CONCEPTS DEMONSTRATED:
 * - Mocking multiple dependencies (DealRepository + UserRepository)
 * - Testing transaction boundaries (createDeal creates Deal + Products in one tx)
 * - Testing business logic for status transitions
 * - Verifying cascade behavior through the service layer
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ORM DealService — Unit Tests")
class DealServiceTest {

    @Mock
    private DealRepository dealRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DealService dealService;

    private User testSalesRep;
    private Deal testDeal;

    @BeforeEach
    void setUp() {
        testSalesRep = new User("jsmith", "john@test.com", "John", "Smith");
        testSalesRep.setId("usr-001");

        testDeal = new Deal("Test Deal", new BigDecimal("50000"), testSalesRep);
        testDeal.setId("deal-001");
    }

    // ============================================================
    // FIND TESTS
    // ============================================================

    @Test
    @DisplayName("findById should return deal when deal exists")
    void findById_shouldReturnDeal_whenDealExists() {
        when(dealRepository.findById("deal-001")).thenReturn(Optional.of(testDeal));

        Optional<Deal> result = dealService.findById("deal-001");

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Test Deal");
    }

    @Test
    @DisplayName("findById should return empty when deal not found")
    void findById_shouldReturnEmpty_whenDealNotFound() {
        when(dealRepository.findById("nonexistent")).thenReturn(Optional.empty());

        Optional<Deal> result = dealService.findById("nonexistent");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByStatus should return deals with matching status")
    void findByStatus_shouldReturnDealsWithStatus() {
        when(dealRepository.findByStatus(DealStatus.WON)).thenReturn(List.of(testDeal));

        List<Deal> result = dealService.findByStatus(DealStatus.WON);

        assertThat(result).hasSize(1);
        verify(dealRepository).findByStatus(DealStatus.WON);
    }

    // ============================================================
    // CREATE TESTS
    // ============================================================

    @Test
    @DisplayName("createDeal should save deal with products")
    void createDeal_shouldSaveDealWithProducts() {
        when(userRepository.findById("usr-001")).thenReturn(Optional.of(testSalesRep));
        when(dealRepository.save(any(Deal.class))).thenAnswer(inv -> inv.getArgument(0));

        DealProduct product = new DealProduct();
        product.setProductName("Software License");
        product.setQuantity(10);
        product.setPrice(new BigDecimal("5000"));

        Deal result = dealService.createDeal("New Deal", new BigDecimal("50000"), "usr-001",
                List.of(product));

        assertThat(result.getTitle()).isEqualTo("New Deal");
        assertThat(result.getProducts()).hasSize(1);
        verify(dealRepository).save(any(Deal.class));
    }

    @Test
    @DisplayName("createDeal should throw exception when sales rep not found")
    void createDeal_shouldThrowException_whenSalesRepNotFound() {
        when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                dealService.createDeal("Deal", new BigDecimal("10000"), "nonexistent", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Sales rep not found");

        verify(dealRepository, never()).save(any());
    }

    // ============================================================
    // UPDATE STATUS TESTS
    // ============================================================

    @Test
    @DisplayName("updateDealStatus should update status and return deal")
    void updateDealStatus_shouldUpdateStatusAndReturnDeal() {
        when(dealRepository.findById("deal-001")).thenReturn(Optional.of(testDeal));
        when(dealRepository.save(any(Deal.class))).thenAnswer(inv -> inv.getArgument(0));

        Deal result = dealService.updateDealStatus("deal-001", DealStatus.WON);

        assertThat(result.getStatus()).isEqualTo(DealStatus.WON);
        verify(dealRepository).save(testDeal);
    }

    @Test
    @DisplayName("updateDealStatus should set close date when status is WON")
    void updateDealStatus_shouldSetCloseDate_whenStatusIsWon() {
        when(dealRepository.findById("deal-001")).thenReturn(Optional.of(testDeal));
        when(dealRepository.save(any(Deal.class))).thenAnswer(inv -> inv.getArgument(0));

        Deal result = dealService.updateDealStatus("deal-001", DealStatus.WON);

        assertThat(result.getCloseDate()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("updateDealStatus should throw exception when deal not found")
    void updateDealStatus_shouldThrowException_whenDealNotFound() {
        when(dealRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dealService.updateDealStatus("nonexistent", DealStatus.WON))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Deal not found");
    }

    // ============================================================
    // ADD PRODUCT TEST
    // ============================================================

    @Test
    @DisplayName("addProductToDeal should add product to existing deal")
    void addProductToDeal_shouldAddProductToExistingDeal() {
        when(dealRepository.findById("deal-001")).thenReturn(Optional.of(testDeal));
        when(dealRepository.save(any(Deal.class))).thenAnswer(inv -> inv.getArgument(0));

        DealProduct product = new DealProduct();
        product.setProductName("Addon License");
        product.setQuantity(5);
        product.setPrice(new BigDecimal("1000"));

        Deal result = dealService.addProductToDeal("deal-001", product);

        assertThat(result.getProducts()).hasSize(1);
        assertThat(result.getProducts().get(0).getProductName()).isEqualTo("Addon License");
    }
}
