package com.chapman.edu.commissions.architecture.cleanarchitecture.application.port.in;

import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.CreateDealCommand;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.DealResult;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.UpdateDealCommand;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.DealStatus;

import java.util.List;

/**
 * Input port defining deal management use cases.
 */
public interface DealUseCase {

    DealResult createDeal(CreateDealCommand command);

    DealResult getDeal(String id);

    List<DealResult> getAllDeals();

    List<DealResult> getDealsBySalesRep(String salesRepId);

    List<DealResult> getDealsByStatus(DealStatus status);

    DealResult updateDeal(String id, UpdateDealCommand command);

    void deleteDeal(String id);
}
