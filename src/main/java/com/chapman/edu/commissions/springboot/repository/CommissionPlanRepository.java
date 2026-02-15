package com.chapman.edu.commissions.springboot.repository;

import com.chapman.edu.commissions.model.CommissionPlan;
import com.chapman.edu.commissions.model.PlanStatus;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * HashMap-based repository for CommissionPlan entities.
 *
 * CONCEPT: Repository Pattern
 * -----------------------------
 * The Repository pattern mediates between the domain model and data mapping
 * layers using a collection-like interface for accessing domain objects.
 *
 * Benefits:
 *   - Decouples business logic from data access implementation
 *   - Makes the code testable (mock the repository in unit tests)
 *   - Allows swapping data stores (HashMap → JPA → MongoDB) without
 *     changing service layer code
 */
@Repository
public class CommissionPlanRepository {

    private final Map<String, CommissionPlan> plans = new ConcurrentHashMap<>();

    public CommissionPlan save(CommissionPlan plan) {
        if (plan.getId() == null || plan.getId().isEmpty()) {
            plan.setId(UUID.randomUUID().toString());
        }
        plans.put(plan.getId(), plan);
        return plan;
    }

    public Optional<CommissionPlan> findById(String id) {
        return Optional.ofNullable(plans.get(id));
    }

    public List<CommissionPlan> findAll() {
        return new ArrayList<>(plans.values());
    }

    public List<CommissionPlan> findByStatus(PlanStatus status) {
        return plans.values().stream()
                .filter(plan -> status.equals(plan.getStatus()))
                .collect(Collectors.toList());
    }

    /**
     * Find plans that are active on a given date.
     * A plan is active if its status is ACTIVE and the date falls within
     * the plan's effective date range.
     */
    public List<CommissionPlan> findActivePlansOnDate(LocalDate date) {
        return plans.values().stream()
                .filter(plan -> plan.isActiveOn(date))
                .collect(Collectors.toList());
    }

    public void deleteById(String id) {
        plans.remove(id);
    }

    public boolean existsById(String id) {
        return plans.containsKey(id);
    }

    public long count() {
        return plans.size();
    }
}
