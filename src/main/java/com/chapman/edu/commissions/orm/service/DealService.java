package com.chapman.edu.commissions.orm.service;

import com.chapman.edu.commissions.orm.entity.Deal;
import com.chapman.edu.commissions.orm.entity.DealProduct;
import com.chapman.edu.commissions.orm.entity.DealStatus;
import com.chapman.edu.commissions.orm.entity.User;
import com.chapman.edu.commissions.orm.repository.DealRepository;
import com.chapman.edu.commissions.orm.repository.DealSpecifications;
import com.chapman.edu.commissions.orm.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * ============================================================
 * SERVICE LAYER: DealService
 * ============================================================
 *
 * TRANSACTION MANAGEMENT IN PRACTICE:
 * This service demonstrates how transactions protect data integrity
 * in a real application scenario.
 *
 * KEY CONCEPTS:
 * - Methods that only READ data use readOnly = true (default at class level)
 * - Methods that WRITE data override with readOnly = false
 * - Complex operations (like creating a deal with products) run in a
 *   single transaction, ensuring all-or-nothing consistency
 *
 * CACHING STRATEGY:
 * - Deals are cached by ID for fast lookups
 * - Cache is invalidated on updates/deletes to prevent stale data
 * - Paginated queries are NOT cached (too many possible parameter combinations)
 */
@Service
@Transactional(readOnly = true)
public class DealService {

    private static final Logger log = LoggerFactory.getLogger(DealService.class);

    private final DealRepository dealRepository;
    private final UserRepository userRepository;

    public DealService(DealRepository dealRepository, UserRepository userRepository) {
        this.dealRepository = dealRepository;
        this.userRepository = userRepository;
    }

    @Cacheable(value = "deals", key = "#id", unless = "#result == null")
    public Optional<Deal> findById(String id) {
        log.info("Cache MISS - Loading deal from database: {}", id);
        return dealRepository.findById(id);
    }

    public Optional<Deal> findByIdWithProducts(String id) {
        return dealRepository.findWithProductsAndSalesRepById(id);
    }

    public Page<Deal> findBySalesRep(String salesRepId, Pageable pageable) {
        return dealRepository.findBySalesRepId(salesRepId, pageable);
    }

    public List<Deal> findByStatus(DealStatus status) {
        return dealRepository.findByStatus(status);
    }

    /**
     * DYNAMIC QUERY using Specifications.
     * Builds the query based on which filter parameters are non-null.
     * This is the service method referenced in DealSpecifications' documentation.
     */
    public Page<Deal> searchDeals(DealStatus status, BigDecimal minValue,
                                   BigDecimal maxValue, String salesRepId,
                                   String titleSearch, Pageable pageable) {
        Specification<Deal> spec = Specification.where(null);

        if (status != null) {
            spec = spec.and(DealSpecifications.hasStatus(status));
        }
        if (minValue != null) {
            spec = spec.and(DealSpecifications.valueGreaterThan(minValue));
        }
        if (maxValue != null) {
            spec = spec.and(DealSpecifications.valueLessThan(maxValue));
        }
        if (salesRepId != null) {
            spec = spec.and(DealSpecifications.belongsToSalesRep(salesRepId));
        }
        if (titleSearch != null && !titleSearch.isBlank()) {
            spec = spec.and(DealSpecifications.titleContains(titleSearch));
        }

        return dealRepository.findAll(spec, pageable);
    }

    /**
     * Create a new deal with products.
     *
     * TRANSACTION GUARANTEES:
     * This method creates a Deal AND its DealProducts in a single transaction.
     * If saving any product fails, the entire transaction (including the deal
     * creation) is rolled back. This ensures data consistency.
     *
     * The CascadeType.ALL on Deal.products means calling dealRepository.save(deal)
     * also persists all products in the deal's products list.
     */
    @Transactional(readOnly = false)
    public Deal createDeal(String title, BigDecimal value, String salesRepId, List<DealProduct> products) {
        log.info("Creating deal: {} for sales rep: {}", title, salesRepId);

        User salesRep = userRepository.findById(salesRepId)
                .orElseThrow(() -> new IllegalArgumentException("Sales rep not found: " + salesRepId));

        Deal deal = new Deal(title, value, salesRep);

        if (products != null) {
            for (DealProduct product : products) {
                deal.addProduct(product);
            }
        }

        return dealRepository.save(deal);
    }

    /**
     * Update deal status.
     *
     * @CacheEvict: Removes the cached deal so the next findById
     * fetches the updated version from the database.
     */
    @Transactional(readOnly = false)
    @CacheEvict(value = "deals", key = "#dealId")
    public Deal updateDealStatus(String dealId, DealStatus newStatus) {
        log.info("Updating deal {} status to {}", dealId, newStatus);

        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new IllegalArgumentException("Deal not found: " + dealId));

        deal.setStatus(newStatus);

        if (newStatus == DealStatus.WON) {
            deal.setCloseDate(LocalDate.now());
        }

        return dealRepository.save(deal);
    }

    /**
     * Add a product to an existing deal.
     * Demonstrates modifying a child collection within a transaction.
     */
    @Transactional(readOnly = false)
    @CacheEvict(value = "deals", key = "#dealId")
    public Deal addProductToDeal(String dealId, DealProduct product) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new IllegalArgumentException("Deal not found: " + dealId));
        deal.addProduct(product);
        return dealRepository.save(deal);
    }

    public BigDecimal calculateTotalWonValue(String salesRepId) {
        return dealRepository.calculateTotalValueBySalesRepAndStatus(salesRepId, DealStatus.WON);
    }

    public List<Deal> findWonDealsWithoutCalculations() {
        return dealRepository.findWonDealsWithoutCalculations();
    }
}
