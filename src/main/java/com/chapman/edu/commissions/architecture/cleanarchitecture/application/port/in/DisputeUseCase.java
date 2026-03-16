package com.chapman.edu.commissions.architecture.cleanarchitecture.application.port.in;

import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.CreateDisputeCommand;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.DisputeResult;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.ResolveDisputeCommand;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.DisputeStatus;

import java.util.List;

/**
 * Input port defining dispute management use cases.
 */
public interface DisputeUseCase {

    DisputeResult createDispute(CreateDisputeCommand command);

    DisputeResult getDispute(String id);

    List<DisputeResult> getAllDisputes();

    List<DisputeResult> getDisputesBySalesRep(String salesRepId);

    List<DisputeResult> getDisputesByStatus(DisputeStatus status);

    DisputeResult resolveDispute(String id, ResolveDisputeCommand command);

    DisputeResult escalateDispute(String id);

    void deleteDispute(String id);
}
