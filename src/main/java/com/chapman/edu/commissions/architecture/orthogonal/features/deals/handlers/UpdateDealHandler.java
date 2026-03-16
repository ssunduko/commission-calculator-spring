package com.chapman.edu.commissions.architecture.orthogonal.features.deals.handlers;

import com.chapman.edu.commissions.architecture.orthogonal.domain.Deal;
import com.chapman.edu.commissions.architecture.orthogonal.features.deals.DealRepository;
import com.chapman.edu.commissions.architecture.orthogonal.features.deals.DealResponse;
import com.chapman.edu.commissions.architecture.orthogonal.features.deals.commands.UpdateDealCommand;
import com.chapman.edu.commissions.architecture.orthogonal.infrastructure.exceptions.ResourceNotFoundException;
import com.chapman.edu.commissions.architecture.orthogonal.infrastructure.exceptions.ValidationException;
import com.chapman.edu.commissions.architecture.orthogonal.pipeline.CommandHandler;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class UpdateDealHandler implements CommandHandler<UpdateDealCommand, DealResponse> {

    private final DealRepository dealRepository;

    public UpdateDealHandler(DealRepository dealRepository) {
        this.dealRepository = dealRepository;
    }

    @Override
    public DealResponse handle(UpdateDealCommand command) {
        Deal deal = dealRepository.findById(command.id())
                .orElseThrow(() -> new ResourceNotFoundException("Deal", command.id()));

        if (command.title() != null) {
            deal.setTitle(command.title());
        }
        if (command.value() != null) {
            if (command.value().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException("Deal value must be greater than zero");
            }
            deal.setValue(command.value());
        }
        if (command.status() != null) {
            deal.setStatus(command.status());
        }
        if (command.closeDate() != null) {
            deal.setCloseDate(command.closeDate());
        }

        Deal updated = dealRepository.save(deal);
        return DealResponse.from(updated);
    }
}
