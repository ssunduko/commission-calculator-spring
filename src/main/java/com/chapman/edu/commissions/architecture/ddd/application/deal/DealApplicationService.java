package com.chapman.edu.commissions.architecture.ddd.application.deal;

import com.chapman.edu.commissions.architecture.ddd.application.dto.*;
import com.chapman.edu.commissions.architecture.ddd.domain.deal.Deal;
import com.chapman.edu.commissions.architecture.ddd.domain.deal.DealRepository;
import com.chapman.edu.commissions.architecture.ddd.domain.deal.DealStatus;
import com.chapman.edu.commissions.architecture.ddd.domain.shared.DomainException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * CONCEPT: Application Service (DDD)
 *
 * An Application Service orchestrates a use case by coordinating domain
 * objects. It does NOT contain business logic — that belongs in the
 * domain layer (aggregates, domain services, value objects).
 *
 * The Application Service:
 * 1. Accepts a DTO (command/request) from the interface layer
 * 2. Loads the required aggregates from repositories
 * 3. Invokes domain methods on the aggregates
 * 4. Persists the changes
 * 5. Returns a DTO to the interface layer
 *
 * Application Services are the transaction boundary — each method
 * represents one unit of work.
 */
@Service
@Transactional
public class DealApplicationService {

    private static final Logger log = LoggerFactory.getLogger(DealApplicationService.class);
    private final DealRepository dealRepository;

    public DealApplicationService(DealRepository dealRepository) {
        this.dealRepository = dealRepository;
    }

    public DealDto createDeal(CreateDealRequest request) {
        request.validate();
        Deal deal = new Deal(request.title(), request.value(), request.salesRepId());
        Deal saved = dealRepository.save(deal);
        log.info("Deal created: id={}, title={}", saved.getId(), saved.getTitle());
        return DealDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public DealDto getDeal(String id) {
        Deal deal = dealRepository.findById(id)
                .orElseThrow(() -> new DomainException("Deal not found: " + id));
        return DealDto.fromEntity(deal);
    }

    @Transactional(readOnly = true)
    public List<DealDto> getAllDeals() {
        return dealRepository.findAll().stream().map(DealDto::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<DealDto> getDealsBySalesRep(String salesRepId) {
        return dealRepository.findBySalesRepId(salesRepId).stream().map(DealDto::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<DealDto> getDealsByStatus(DealStatus status) {
        return dealRepository.findByStatus(status).stream().map(DealDto::fromEntity).toList();
    }

    public DealDto updateDeal(String id, UpdateDealRequest request) {
        Deal deal = dealRepository.findById(id)
                .orElseThrow(() -> new DomainException("Deal not found: " + id));

        if (request.title() != null) deal.setTitle(request.title());
        if (request.value() != null) {
            if (request.value().compareTo(BigDecimal.ZERO) <= 0)
                throw new DomainException("Deal value must be greater than zero");
            deal.setValue(request.value());
        }
        if (request.status() != null) deal.setStatus(request.status());
        if (request.closeDate() != null) deal.setCloseDate(request.closeDate());

        Deal updated = dealRepository.save(deal);
        return DealDto.fromEntity(updated);
    }

    public void deleteDeal(String id) {
        if (!dealRepository.existsById(id)) throw new DomainException("Deal not found: " + id);
        dealRepository.deleteById(id);
    }
}
