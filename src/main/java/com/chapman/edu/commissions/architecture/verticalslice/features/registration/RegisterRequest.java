package com.chapman.edu.commissions.architecture.verticalslice.features.registration;

public record RegisterRequest(
    String username,
    String email,
    String firstName,
    String lastName,
    String password,
    String packageCode,
    PaymentDetails payment
) {

    public record PaymentDetails(
        String cardHolderName,
        String cardNumber,
        String expiryMonth,
        String expiryYear,
        String cvv
    ) {
    }

    public void validate() {
        requireField(username, "username");
        requireField(email, "email");
        requireField(firstName, "firstName");
        requireField(lastName, "lastName");
        requireField(password, "password");
        requireField(packageCode, "packageCode");

        if (!email.contains("@")) {
            throw new IllegalArgumentException("Email must be a valid address");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
        if (payment == null) {
            throw new IllegalArgumentException("Payment details are required");
        }
        requireField(payment.cardHolderName(), "payment.cardHolderName");
        requireField(payment.cardNumber(), "payment.cardNumber");
        requireField(payment.expiryMonth(), "payment.expiryMonth");
        requireField(payment.expiryYear(), "payment.expiryYear");
        requireField(payment.cvv(), "payment.cvv");

        String digits = payment.cardNumber().replaceAll("\\s+", "");
        if (!digits.matches("\\d{13,19}")) {
            throw new IllegalArgumentException("Card number must be 13-19 digits");
        }
        if (!payment.cvv().matches("\\d{3,4}")) {
            throw new IllegalArgumentException("CVV must be 3 or 4 digits");
        }
    }

    private static void requireField(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
    }
}
