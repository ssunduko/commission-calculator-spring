package com.chapman.edu.commissions.architecture.ddd.infrastructure.persistence;

import com.chapman.edu.commissions.architecture.ddd.domain.dispute.Dispute;
import com.chapman.edu.commissions.architecture.ddd.domain.dispute.DisputeRepository;
import com.chapman.edu.commissions.architecture.ddd.domain.dispute.DisputeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA implementation of the domain DisputeRepository.
 * Spring Data JPA auto-generates the query methods at runtime.
 */
@Repository
public interface JpaDisputeRepository extends JpaRepository<Dispute, String>, DisputeRepository {
    List<Dispute> findBySalesRepId(String salesRepId);
    List<Dispute> findByStatus(DisputeStatus status);
}
