package com.chapman.edu.commissions.architecture.cleanarchitecture.application.port.out;

import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.Deal;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.DealStatus;

import java.util.List;
import java.util.Optional;

/**
 * Output port for Deal persistence operations.
 */
public interface DealRepositoryPort {

    Deal save(Deal deal);

    Optional<Deal> findById(String id);

    List<Deal> findAll();

    List<Deal> findBySalesRepId(String salesRepId);

    List<Deal> findByStatus(DealStatus status);

    void deleteById(String id);
}
