package com.chapman.edu.commissions.architecture.eventdriven.features.deals;

import com.chapman.edu.commissions.architecture.eventdriven.domain.Deal;
import com.chapman.edu.commissions.architecture.eventdriven.domain.DealStatus;
import com.chapman.edu.commissions.architecture.eventdriven.domain.event.DealCreatedEvent;
import com.chapman.edu.commissions.architecture.eventdriven.domain.event.DealDeletedEvent;
import com.chapman.edu.commissions.architecture.eventdriven.domain.event.DealUpdatedEvent;
import com.chapman.edu.commissions.architecture.eventdriven.infrastructure.exceptions.ResourceNotFoundException;
import com.chapman.edu.commissions.architecture.eventdriven.infrastructure.exceptions.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * CONCEPT: Event-Publishing Service
 *
 * This service follows the same CRUD pattern as the vertical-slice version,
 * but publishes domain events after each state change. The service itself
 * does not know WHO listens — it simply announces what happened.
 *
 * This decoupling is the key benefit of event-driven architecture:
 * new behaviors can be added by creating new listeners without
 * modifying the service.
 */
@Service
public class DealService {

    private static final Logger log = LoggerFactory.getLogger(DealService.class);

    private final DealRepository dealRepository;
    private final ApplicationEventPublisher eventPublisher;

    public DealService(DealRepository dealRepository, ApplicationEventPublisher eventPublisher) {
        this.dealRepository = dealRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Creates a new deal and publishes a {@link DealCreatedEvent}.
     *
     * CONCEPT: After persisting the deal, we publish an event so that any
     * interested listener (audit logger, notification sender, analytics
     * updater) can react — without the service needing to know about them.
     */
    public DealResponse createDeal(CreateDealRequest request) {
        request.validate();

        Deal deal = new Deal(request.title(), request.value(), request.salesRepId());
        Deal savedDeal = dealRepository.save(deal);

        log.info("Deal created: id={}, title={}", savedDeal.getId(), savedDeal.getTitle());

        eventPublisher.publishEvent(new DealCreatedEvent(
                savedDeal.getId(),
                savedDeal.getTitle(),
                savedDeal.getValue(),
                savedDeal.getSalesRepId()
        ));

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

    /**
     * Updates a deal and publishes a {@link DealUpdatedEvent} for each changed field.
     *
     * CONCEPT: Capturing old vs new values in the event enables listeners to make
     * decisions based on what changed. For example, a status change from OPEN to
     * CLOSED_WON might trigger automatic commission calculation, while a title
     * change might only update a search index.
     */
    public DealResponse updateDeal(String id, UpdateDealRequest request) {
        Deal deal = dealRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deal", id));

        // Capture old status before applying updates
        String oldStatus = deal.getStatus() != null ? deal.getStatus().name() : null;

        if (request.title() != null) {
            String oldTitle = deal.getTitle();
            deal.setTitle(request.title());
            eventPublisher.publishEvent(new DealUpdatedEvent(
                    deal.getId(), "title", oldTitle, request.title()));
        }
        if (request.value() != null) {
            if (request.value().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                throw new ValidationException("Deal value must be greater than zero");
            }
            String oldValue = deal.getValue() != null ? deal.getValue().toPlainString() : null;
            deal.setValue(request.value());
            eventPublisher.publishEvent(new DealUpdatedEvent(
                    deal.getId(), "value", oldValue, request.value().toPlainString()));
        }
        if (request.status() != null) {
            deal.setStatus(request.status());
            String newStatus = request.status().name();
            eventPublisher.publishEvent(new DealUpdatedEvent(
                    deal.getId(), "status", oldStatus, newStatus));
        }
        if (request.closeDate() != null) {
            String oldCloseDate = deal.getCloseDate() != null ? deal.getCloseDate().toString() : null;
            deal.setCloseDate(request.closeDate());
            eventPublisher.publishEvent(new DealUpdatedEvent(
                    deal.getId(), "closeDate", oldCloseDate, request.closeDate().toString()));
        }

        Deal updatedDeal = dealRepository.save(deal);

        log.info("Deal updated: id={}", updatedDeal.getId());

        return DealResponse.from(updatedDeal);
    }

    /**
     * Deletes a deal and publishes a {@link DealDeletedEvent}.
     *
     * CONCEPT: Even deletion publishes an event, allowing listeners to
     * clean up related data (cancel pending commission calculations,
     * remove from dashboards, etc.) without coupling the deal service
     * to those concerns.
     */
    public void deleteDeal(String id) {
        Deal deal = dealRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deal", id));

        dealRepository.deleteById(id);

        log.info("Deal deleted: id={}", id);

        eventPublisher.publishEvent(new DealDeletedEvent(
                deal.getId(),
                deal.getSalesRepId()
        ));
    }
}
