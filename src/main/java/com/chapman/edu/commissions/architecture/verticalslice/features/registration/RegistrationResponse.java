package com.chapman.edu.commissions.architecture.verticalslice.features.registration;

import com.chapman.edu.commissions.architecture.verticalslice.domain.PaymentStatus;
import com.chapman.edu.commissions.architecture.verticalslice.domain.SubscriptionStatus;

import java.math.BigDecimal;

public record RegistrationResponse(
    String userId,
    String username,
    String email,
    String fullName,
    String subscriptionId,
    String packageCode,
    String packageName,
    SubscriptionStatus subscriptionStatus,
    String paymentId,
    PaymentStatus paymentStatus,
    BigDecimal amountCharged,
    String cardLastFour,
    String token,
    long expiresInSeconds
) {
}
