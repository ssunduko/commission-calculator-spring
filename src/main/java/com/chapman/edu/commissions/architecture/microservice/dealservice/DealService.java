package com.chapman.edu.commissions.architecture.microservice.dealservice;

import com.chapman.edu.commissions.architecture.microservice.common.dto.CreateDealRequest;
import com.chapman.edu.commissions.architecture.microservice.common.dto.DealDto;
import com.chapman.edu.commissions.architecture.microservice.common.dto.UpdateDealRequest;
import com.chapman.edu.commissions.architecture.microservice.dealservice.domain.Deal;
import com.chapman.edu.commissions.architecture.microservice.dealservice.domain.DealStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CONCEPT: Microservice Business Logic
 *
 * The Deal Service owns all deal-related business logic. It uses shared DTOs
 * from the common package for its API responses, ensuring that other services
 * can consume deal data without depending on this service's domain model.
 */
@Service
public class DealService {
    private final DealRepository dealRepository;

    public DealService(DealRepository dealRepository) {
        this.dealRepository = dealRepository;
    }

    public DealDto createDeal(CreateDealRequest request) {
        request.validate();

        Deal deal = new Deal(request.title(), request.value(), request.salesRepId());
        Deal savedDeal = dealRepository.save(deal);

        return toDto(savedDeal);
    }

    public DealDto getDeal(String id) {
        Deal deal = dealRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Deal not found: " + id));
        return toDto(deal);
    }

    public List<DealDto> getAllDeals() {
        return dealRepository.findAll().stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    public List<DealDto> getDealsBySalesRep(String salesRepId) {
        return dealRepository.findBySalesRepId(salesRepId).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    public List<DealDto> getDealsByStatus(DealStatus status) {
        return dealRepository.findByStatus(status).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    public DealDto updateDeal(String id, UpdateDealRequest request) {
        Deal deal = dealRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Deal not found: " + id));

        if (request.title() != null) {
            deal.setTitle(request.title());
        }
        if (request.value() != null) {
            if (request.value().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Deal value must be greater than zero");
            }
            deal.setValue(request.value());
        }
        if (request.status() != null) {
            deal.setStatus(DealStatus.valueOf(request.status()));
        }
        if (request.closeDate() != null) {
            deal.setCloseDate(request.closeDate());
        }

        Deal updatedDeal = dealRepository.save(deal);
        return toDto(updatedDeal);
    }

    public void deleteDeal(String id) {
        if (!dealRepository.existsById(id)) {
            throw new RuntimeException("Deal not found: " + id);
        }
        dealRepository.deleteById(id);
    }

    private DealDto toDto(Deal deal) {
        return new DealDto(deal.getId(), deal.getTitle(), deal.getValue(),
            deal.getSalesRepId(), deal.getStatus().name(),
            deal.getCloseDate(), deal.getCreatedDate());
    }
}
