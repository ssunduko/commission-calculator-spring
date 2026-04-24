package com.chapman.edu.commissions.architecture.verticalslice.features.subscriptions;

import com.chapman.edu.commissions.architecture.verticalslice.domain.Subscription;
import com.chapman.edu.commissions.architecture.verticalslice.domain.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, String> {

    Optional<Subscription> findFirstByUserIdAndStatusOrderByCreatedAtDesc(String userId, SubscriptionStatus status);

    List<Subscription> findByUserId(String userId);
}
