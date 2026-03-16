package com.chapman.edu.commissions.architecture.orthogonal.features.deals.handlers;

import com.chapman.edu.commissions.architecture.orthogonal.features.deals.DealRepository;
import com.chapman.edu.commissions.architecture.orthogonal.features.deals.commands.DeleteDealCommand;
import com.chapman.edu.commissions.architecture.orthogonal.infrastructure.exceptions.ResourceNotFoundException;
import com.chapman.edu.commissions.architecture.orthogonal.pipeline.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class DeleteDealHandler implements CommandHandler<DeleteDealCommand, Void> {

    private final DealRepository dealRepository;

    public DeleteDealHandler(DealRepository dealRepository) {
        this.dealRepository = dealRepository;
    }

    @Override
    public Void handle(DeleteDealCommand command) {
        if (!dealRepository.existsById(command.id())) {
            throw new ResourceNotFoundException("Deal", command.id());
        }
        dealRepository.deleteById(command.id());
        return null;
    }
}
