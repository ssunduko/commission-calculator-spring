package com.chapman.edu.commissions.architecture.cleanarchitecture.application.service;

import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.CreateDealCommand;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.DealResult;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.UpdateDealCommand;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.port.in.DealUseCase;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.port.out.DealRepositoryPort;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.exception.EntityNotFoundException;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.Deal;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.DealStatus;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Application service implementing deal management use cases.
 */
@Service
public class DealService implements DealUseCase {

    private final DealRepositoryPort dealRepository;

    public DealService(DealRepositoryPort dealRepository) {
        this.dealRepository = dealRepository;
    }

    @Override
    public DealResult createDeal(CreateDealCommand command) {
        command.validate();
        Deal deal = new Deal(command.title(), command.value(), command.salesRepId());
        Deal saved = dealRepository.save(deal);
        return DealResult.from(saved);
    }

    @Override
    public DealResult getDeal(String id) {
        Deal deal = dealRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Deal", id));
        return DealResult.from(deal);
    }

    @Override
    public List<DealResult> getAllDeals() {
        return dealRepository.findAll().stream()
                .map(DealResult::from)
                .toList();
    }

    @Override
    public List<DealResult> getDealsBySalesRep(String salesRepId) {
        return dealRepository.findBySalesRepId(salesRepId).stream()
                .map(DealResult::from)
                .toList();
    }

    @Override
    public List<DealResult> getDealsByStatus(DealStatus status) {
        return dealRepository.findByStatus(status).stream()
                .map(DealResult::from)
                .toList();
    }

    @Override
    public DealResult updateDeal(String id, UpdateDealCommand command) {
        Deal deal = dealRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Deal", id));

        if (command.title() != null) {
            deal.setTitle(command.title());
        }
        if (command.value() != null) {
            deal.setValue(command.value());
        }
        if (command.status() != null) {
            deal.setStatus(command.status());
        }
        if (command.closeDate() != null) {
            deal.setCloseDate(command.closeDate());
        }

        Deal saved = dealRepository.save(deal);
        return DealResult.from(saved);
    }

    @Override
    public void deleteDeal(String id) {
        dealRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Deal", id));
        dealRepository.deleteById(id);
    }
}
