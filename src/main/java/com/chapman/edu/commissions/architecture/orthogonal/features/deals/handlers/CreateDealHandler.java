package com.chapman.edu.commissions.architecture.orthogonal.features.deals.handlers;

import com.chapman.edu.commissions.architecture.orthogonal.domain.Deal;
import com.chapman.edu.commissions.architecture.orthogonal.features.deals.DealRepository;
import com.chapman.edu.commissions.architecture.orthogonal.features.deals.DealResponse;
import com.chapman.edu.commissions.architecture.orthogonal.features.deals.commands.CreateDealCommand;
import com.chapman.edu.commissions.architecture.orthogonal.pipeline.CommandHandler;
import org.springframework.stereotype.Component;

/**
 * CONCEPT: Single-Responsibility Handler
 *
 * This handler does ONE thing: create a deal. It contains ONLY business logic.
 * Cross-cutting concerns (logging, validation, auditing) are handled by
 * aspects — the handler doesn't know they exist.
 */
@Component
public class CreateDealHandler implements CommandHandler<CreateDealCommand, DealResponse> {

    private final DealRepository dealRepository;

    public CreateDealHandler(DealRepository dealRepository) {
        this.dealRepository = dealRepository;
    }

    @Override
    public DealResponse handle(CreateDealCommand command) {
        Deal deal = new Deal(command.title(), command.value(), command.salesRepId());
        Deal saved = dealRepository.save(deal);
        return DealResponse.from(saved);
    }
}
