package com.chapman.edu.commissions.architecture.orthogonal.features.deals;

import com.chapman.edu.commissions.architecture.orthogonal.domain.Deal;
import com.chapman.edu.commissions.architecture.orthogonal.domain.DealStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for managing Deal entities.
 * Uses Spring Data JPA for database persistence.
 */
@Repository
public interface DealRepository extends JpaRepository<Deal, String> {

    List<Deal> findBySalesRepId(String salesRepId);

    List<Deal> findByStatus(DealStatus status);
}
