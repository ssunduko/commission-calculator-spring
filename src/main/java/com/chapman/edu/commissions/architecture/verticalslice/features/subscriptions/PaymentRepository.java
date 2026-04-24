package com.chapman.edu.commissions.architecture.verticalslice.features.subscriptions;

import com.chapman.edu.commissions.architecture.verticalslice.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {

    List<Payment> findByUserIdOrderByCreatedAtDesc(String userId);

    List<Payment> findBySubscriptionIdOrderByCreatedAtDesc(String subscriptionId);
}
