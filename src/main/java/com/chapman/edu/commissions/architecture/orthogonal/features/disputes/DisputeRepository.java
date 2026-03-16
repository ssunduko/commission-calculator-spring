package com.chapman.edu.commissions.architecture.orthogonal.features.disputes;

import com.chapman.edu.commissions.architecture.orthogonal.domain.Dispute;
import com.chapman.edu.commissions.architecture.orthogonal.domain.DisputeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for managing Dispute entities.
 * Uses Spring Data JPA for database persistence.
 */
@Repository
public interface DisputeRepository extends JpaRepository<Dispute, String> {

    List<Dispute> findBySalesRepId(String salesRepId);

    List<Dispute> findByStatus(DisputeStatus status);

    List<Dispute> findByCalculationId(String calculationId);
}
