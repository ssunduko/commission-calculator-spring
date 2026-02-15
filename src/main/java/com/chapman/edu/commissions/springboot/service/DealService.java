package com.chapman.edu.commissions.springboot.service;

import com.chapman.edu.commissions.model.Deal;
import com.chapman.edu.commissions.model.DealStatus;
import com.chapman.edu.commissions.springboot.dto.request.CreateDealRequest;
import com.chapman.edu.commissions.springboot.exception.BusinessValidationException;
import com.chapman.edu.commissions.springboot.exception.ResourceNotFoundException;
import com.chapman.edu.commissions.springboot.repository.DealRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ============================================================================
 * SERVICE LAYER — DEAL BUSINESS LOGIC
 * ============================================================================
 *
 * CONCEPT: @Service and the Service Layer Pattern
 * --------------------------------------------------
 * The Service layer sits between Controllers and Repositories:
 *
 *   Controller → Service → Repository
 *
 * The Service layer is responsible for:
 *   1. Business logic — Rules, validations, calculations
 *   2. Transaction management — Ensuring data consistency
 *   3. Orchestration — Coordinating multiple repositories
 *   4. DTO conversion — Transforming data between layers
 *
 * @Service is a specialization of @Component. It's functionally identical
 * but communicates intent: "this class contains business logic."
 *
 * Why not put business logic in controllers?
 *   - Controllers should only handle HTTP concerns (request/response)
 *   - Business logic in services is reusable across multiple controllers
 *   - Services are easier to unit test (no HTTP dependencies)
 *   - Follows the Single Responsibility Principle (SRP)
 *
 * CONCEPT: Dependency Injection in Services
 * --------------------------------------------
 * This service depends on DealRepository. Spring injects it via the
 * constructor. The service doesn't know (or care) whether the repository
 * uses a HashMap, JPA, MongoDB, or any other data store. This is the
 * essence of Dependency Inversion Principle — depend on abstractions.
 *
 * @see org.springframework.stereotype.Service
 */
@Service
public class DealService {

    private static final Logger logger = LoggerFactory.getLogger(DealService.class);

    private final DealRepository dealRepository;

    /**
     * Constructor injection — the preferred way to inject dependencies.
     * Spring automatically provides the DealRepository bean.
     */
    public DealService(DealRepository dealRepository) {
        this.dealRepository = dealRepository;
    }

    /**
     * Create a new deal from a request DTO.
     */
    public Deal createDeal(CreateDealRequest request) {
        Deal deal = new Deal(request.getTitle(), request.getValue(), request.getSalesRepId());
        deal.setStatus(DealStatus.OPEN);

        Deal saved = dealRepository.save(deal);
        logger.info("Created deal: {} (ID: {})", saved.getTitle(), saved.getId());
        return saved;
    }

    /**
     * Get a deal by ID or throw ResourceNotFoundException.
     */
    public Deal getDealById(String id) {
        return dealRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deal", "id", id));
    }

    /**
     * Get all deals.
     */
    public List<Deal> getAllDeals() {
        return dealRepository.findAll();
    }

    /**
     * Get all deals for a sales representative.
     */
    public List<Deal> getDealsBySalesRep(String salesRepId) {
        return dealRepository.findBySalesRepId(salesRepId);
    }

    /**
     * Get all deals with a specific status.
     */
    public List<Deal> getDealsByStatus(DealStatus status) {
        return dealRepository.findByStatus(status);
    }

    /**
     * Update deal status with business validation.
     * Business rules: Cannot reopen a cancelled deal.
     */
    public Deal updateDealStatus(String id, DealStatus newStatus) {
        Deal deal = getDealById(id);

        // Business rule: Cannot reopen a cancelled deal
        if (deal.getStatus() == DealStatus.CANCELLED && newStatus == DealStatus.OPEN) {
            throw new BusinessValidationException(
                "Cannot reopen a cancelled deal. Create a new deal instead.");
        }

        deal.setStatus(newStatus);
        logger.info("Updated deal {} status to {}", id, newStatus);
        return dealRepository.save(deal);
    }

    /**
     * Delete a deal by ID.
     */
    public void deleteDeal(String id) {
        if (!dealRepository.existsById(id)) {
            throw new ResourceNotFoundException("Deal", "id", id);
        }
        dealRepository.deleteById(id);
        logger.info("Deleted deal: {}", id);
    }

    /**
     * Get total deal count.
     */
    public long getDealCount() {
        return dealRepository.count();
    }
}
