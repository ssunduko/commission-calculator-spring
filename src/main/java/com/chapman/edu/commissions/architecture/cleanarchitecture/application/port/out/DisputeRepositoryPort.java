package com.chapman.edu.commissions.architecture.cleanarchitecture.application.port.out;

import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.Dispute;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.DisputeStatus;

import java.util.List;
import java.util.Optional;

/**
 * Output port for Dispute persistence operations.
 */
public interface DisputeRepositoryPort {

    Dispute save(Dispute dispute);

    Optional<Dispute> findById(String id);

    List<Dispute> findAll();

    List<Dispute> findBySalesRepId(String salesRepId);

    List<Dispute> findByStatus(DisputeStatus status);

    List<Dispute> findByCalculationId(String calculationId);

    void deleteById(String id);
}
