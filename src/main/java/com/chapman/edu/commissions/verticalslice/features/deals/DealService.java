package com.chapman.edu.commissions.verticalslice.features.deals;

import com.chapman.edu.commissions.verticalslice.infrastructure.exceptions.ResourceNotFoundException;
import com.chapman.edu.commissions.verticalslice.infrastructure.exceptions.ValidationException;
import com.chapman.edu.commissions.verticalslice.domain.Deal;
import com.chapman.edu.commissions.verticalslice.domain.DealStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing deals.
 * Contains business logic for deal operations.
 */
@Service
public class DealService {
    private final DealRepository dealRepository;

    public DealService(DealRepository dealRepository) {
        this.dealRepository = dealRepository;
    }

    public DealResponse createDeal(CreateDealRequest request) {
        request.validate();

        Deal deal = new Deal(request.title(), request.value(), request.salesRepId());
        Deal savedDeal = dealRepository.save(deal);

        return DealResponse.from(savedDeal);
    }

    public DealResponse getDeal(String id) {
        Deal deal = dealRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Deal", id));
        return DealResponse.from(deal);
    }

    public List<DealResponse> getAllDeals() {
        return dealRepository.findAll().stream()
            .map(DealResponse::from)
            .collect(Collectors.toList());
    }

    public List<DealResponse> getDealsBySalesRep(String salesRepId) {
        return dealRepository.findBySalesRepId(salesRepId).stream()
            .map(DealResponse::from)
            .collect(Collectors.toList());
    }

    public List<DealResponse> getDealsByStatus(DealStatus status) {
        return dealRepository.findByStatus(status).stream()
            .map(DealResponse::from)
            .collect(Collectors.toList());
    }

    public DealResponse updateDeal(String id, UpdateDealRequest request) {
        Deal deal = dealRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Deal", id));

        if (request.title() != null) {
            deal.setTitle(request.title());
        }
        if (request.value() != null) {
            if (request.value().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                throw new ValidationException("Deal value must be greater than zero");
            }
            deal.setValue(request.value());
        }
        if (request.status() != null) {
            deal.setStatus(request.status());
        }
        if (request.closeDate() != null) {
            deal.setCloseDate(request.closeDate());
        }

        Deal updatedDeal = dealRepository.save(deal);
        return DealResponse.from(updatedDeal);
    }

    public void deleteDeal(String id) {
        if (!dealRepository.existsById(id)) {
            throw new ResourceNotFoundException("Deal", id);
        }
        dealRepository.deleteById(id);
    }
}
