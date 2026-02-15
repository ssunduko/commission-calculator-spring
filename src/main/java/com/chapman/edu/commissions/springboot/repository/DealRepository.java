package com.chapman.edu.commissions.springboot.repository;

import com.chapman.edu.commissions.model.Deal;
import com.chapman.edu.commissions.model.DealStatus;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * ============================================================================
 * HASHMAP-BASED REPOSITORY — DEAL DATA ACCESS
 * ============================================================================
 *
 * CONCEPT: @Repository
 * ----------------------
 * @Repository is a Spring stereotype annotation that marks this class as a
 * Data Access Object (DAO). It is a specialization of @Component with an
 * additional feature: automatic exception translation.
 *
 * When a @Repository bean throws a persistence-related exception (e.g.,
 * DataAccessException), Spring automatically translates it into Spring's
 * DataAccessException hierarchy, providing a consistent exception model
 * regardless of the underlying data store.
 *
 * CONCEPT: HashMap-Based Repository
 * ------------------------------------
 * In production, repositories typically use a database (via Spring Data JPA,
 * JDBC, or MongoDB). For this educational example, we use a ConcurrentHashMap
 * to store data in memory:
 *
 *   - No database setup required
 *   - Focus on Spring concepts rather than database configuration
 *   - ConcurrentHashMap provides thread-safe operations
 *   - Data is lost when the application restarts
 *
 * In a real Spring Data JPA application, you would instead:
 *   public interface DealRepository extends JpaRepository<Deal, String> {
 *       List<Deal> findBySalesRepId(String salesRepId);
 *       List<Deal> findByStatus(DealStatus status);
 *   }
 *   Spring Data automatically generates the implementation!
 *
 * @see org.springframework.stereotype.Repository
 */
@Repository
public class DealRepository {

    /**
     * ConcurrentHashMap is used as our in-memory data store.
     * Key = Deal ID (String), Value = Deal object
     *
     * ConcurrentHashMap vs HashMap:
     *   - ConcurrentHashMap is thread-safe (multiple threads can read/write safely)
     *   - HashMap is NOT thread-safe (can cause data corruption with concurrent access)
     *   - Web applications are inherently multi-threaded (each request = a thread)
     */
    private final Map<String, Deal> deals = new ConcurrentHashMap<>();

    /**
     * Save (create or update) a deal in the store.
     * If the deal has no ID, a UUID is generated.
     */
    public Deal save(Deal deal) {
        if (deal.getId() == null || deal.getId().isEmpty()) {
            deal.setId(UUID.randomUUID().toString());
        }
        deals.put(deal.getId(), deal);
        return deal;
    }

    /**
     * Find a deal by its ID.
     * Returns Optional.empty() if not found (avoids NullPointerException).
     */
    public Optional<Deal> findById(String id) {
        return Optional.ofNullable(deals.get(id));
    }

    /**
     * Retrieve all deals.
     */
    public List<Deal> findAll() {
        return new ArrayList<>(deals.values());
    }

    /**
     * Find all deals for a specific sales representative.
     */
    public List<Deal> findBySalesRepId(String salesRepId) {
        return deals.values().stream()
                .filter(deal -> salesRepId.equals(deal.getSalesRepId()))
                .collect(Collectors.toList());
    }

    /**
     * Find all deals with a specific status.
     */
    public List<Deal> findByStatus(DealStatus status) {
        return deals.values().stream()
                .filter(deal -> status.equals(deal.getStatus()))
                .collect(Collectors.toList());
    }

    /**
     * Delete a deal by ID.
     */
    public void deleteById(String id) {
        deals.remove(id);
    }

    /**
     * Check if a deal exists by ID.
     */
    public boolean existsById(String id) {
        return deals.containsKey(id);
    }

    /**
     * Count total deals.
     */
    public long count() {
        return deals.size();
    }
}
