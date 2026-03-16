package com.chapman.edu.commissions.architecture.cleanarchitecture.adapter.out.persistence;

import com.chapman.edu.commissions.architecture.cleanarchitecture.application.port.out.DisputeRepositoryPort;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.Dispute;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.DisputeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataDisputeRepository extends JpaRepository<Dispute, String>, DisputeRepositoryPort {

    List<Dispute> findBySalesRepId(String salesRepId);

    List<Dispute> findByStatus(DisputeStatus status);

    List<Dispute> findByCalculationId(String calculationId);
}
