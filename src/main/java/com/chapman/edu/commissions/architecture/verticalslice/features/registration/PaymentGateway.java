package com.chapman.edu.commissions.architecture.verticalslice.features.registration;

import com.chapman.edu.commissions.architecture.verticalslice.domain.PaymentStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Stubbed payment gateway. A real implementation would call Stripe/Braintree/etc.
 * This simulator approves any valid card except those ending in 0000 (used for
 * failure-path tests) and returns a transaction reference.
 */
@Component
public class PaymentGateway {

    public ChargeResult charge(String cardNumberDigits, String cardHolderName,
                               String expiryMonth, String expiryYear,
                               String cvv, BigDecimal amount, String currency) {
        if (cardNumberDigits == null || cardNumberDigits.isBlank()) {
            return ChargeResult.failed("Card number is missing");
        }
        if (cardNumberDigits.endsWith("0000")) {
            return ChargeResult.failed("Card declined by issuer");
        }
        if (amount == null || amount.signum() <= 0) {
            return ChargeResult.failed("Invalid charge amount");
        }
        String reference = "TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        return ChargeResult.success(reference);
    }

    public String detectCardBrand(String cardNumberDigits) {
        if (cardNumberDigits == null || cardNumberDigits.isEmpty()) {
            return "UNKNOWN";
        }
        char first = cardNumberDigits.charAt(0);
        if (first == '4') {
            return "VISA";
        }
        if (first == '5') {
            return "MASTERCARD";
        }
        if (first == '3') {
            return "AMEX";
        }
        if (first == '6') {
            return "DISCOVER";
        }
        return "OTHER";
    }

    public record ChargeResult(PaymentStatus status, String transactionReference, String failureReason) {
        public static ChargeResult success(String ref) {
            return new ChargeResult(PaymentStatus.COMPLETED, ref, null);
        }

        public static ChargeResult failed(String reason) {
            return new ChargeResult(PaymentStatus.FAILED, null, reason);
        }
    }
}
