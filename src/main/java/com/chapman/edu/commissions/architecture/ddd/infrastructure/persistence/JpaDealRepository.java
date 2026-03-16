package com.chapman.edu.commissions.architecture.ddd.infrastructure.persistence;

import com.chapman.edu.commissions.architecture.ddd.domain.deal.Deal;
import com.chapman.edu.commissions.architecture.ddd.domain.deal.DealRepository;
import com.chapman.edu.commissions.architecture.ddd.domain.deal.DealStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * CONCEPT: Infrastructure Repository (DDD)
 *
 * This interface implements the domain's DealRepository by extending
 * Spring Data JPA's JpaRepository. The domain defines WHAT it needs
 * (the DealRepository interface), and infrastructure provides HOW
 * (JPA implementation).
 *
 * This keeps the domain independent of persistence technology.
 * You could swap JPA for MongoDB by creating a new implementation
 * without touching the domain.
 */
@Repository
public interface JpaDealRepository extends JpaRepository<Deal, String>, DealRepository {
    List<Deal> findBySalesRepId(String salesRepId);
    List<Deal> findByStatus(DealStatus status);
}
