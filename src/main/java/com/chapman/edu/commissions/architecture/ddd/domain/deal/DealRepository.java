package com.chapman.edu.commissions.architecture.ddd.domain.deal;

import java.util.List;
import java.util.Optional;

/**
 * CONCEPT: Repository (DDD)
 *
 * In DDD, a Repository is a domain-level abstraction for retrieving
 * and persisting aggregate roots. The interface lives in the DOMAIN
 * layer (not infrastructure), keeping the domain independent of
 * persistence technology.
 *
 * Key rules:
 * - One repository per aggregate root
 * - Returns domain objects, not DTOs
 * - The implementation lives in infrastructure
 */
public interface DealRepository {
    Deal save(Deal deal);
    Optional<Deal> findById(String id);
    List<Deal> findAll();
    List<Deal> findBySalesRepId(String salesRepId);
    List<Deal> findByStatus(DealStatus status);
    boolean existsById(String id);
    void deleteById(String id);
}
