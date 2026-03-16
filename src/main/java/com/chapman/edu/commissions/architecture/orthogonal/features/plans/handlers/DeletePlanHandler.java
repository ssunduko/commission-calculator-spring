package com.chapman.edu.commissions.architecture.orthogonal.features.plans.handlers;

import com.chapman.edu.commissions.architecture.orthogonal.features.plans.CommissionPlanRepository;
import com.chapman.edu.commissions.architecture.orthogonal.features.plans.commands.DeletePlanCommand;
import com.chapman.edu.commissions.architecture.orthogonal.infrastructure.exceptions.ResourceNotFoundException;
import com.chapman.edu.commissions.architecture.orthogonal.pipeline.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class DeletePlanHandler implements CommandHandler<DeletePlanCommand, Void> {
    private final CommissionPlanRepository planRepository;
    public DeletePlanHandler(CommissionPlanRepository planRepository) { this.planRepository = planRepository; }

    @Override
    public Void handle(DeletePlanCommand command) {
        if (!planRepository.existsById(command.id())) throw new ResourceNotFoundException("Commission Plan", command.id());
        planRepository.deleteById(command.id());
        return null;
    }
}
