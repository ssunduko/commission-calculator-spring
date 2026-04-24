package com.chapman.edu.commissions.architecture.verticalslice.features.registration;

import com.chapman.edu.commissions.architecture.verticalslice.domain.Payment;
import com.chapman.edu.commissions.architecture.verticalslice.domain.PaymentStatus;
import com.chapman.edu.commissions.architecture.verticalslice.domain.Subscription;
import com.chapman.edu.commissions.architecture.verticalslice.domain.SubscriptionPackage;
import com.chapman.edu.commissions.architecture.verticalslice.domain.SubscriptionStatus;
import com.chapman.edu.commissions.architecture.verticalslice.domain.User;
import com.chapman.edu.commissions.architecture.verticalslice.domain.UserRole;
import com.chapman.edu.commissions.architecture.verticalslice.features.authentication.JwtService;
import com.chapman.edu.commissions.architecture.verticalslice.features.subscriptions.PaymentRepository;
import com.chapman.edu.commissions.architecture.verticalslice.features.subscriptions.SubscriptionPackageRepository;
import com.chapman.edu.commissions.architecture.verticalslice.features.subscriptions.SubscriptionRepository;
import com.chapman.edu.commissions.architecture.verticalslice.infrastructure.data.UserRepository;
import com.chapman.edu.commissions.architecture.verticalslice.infrastructure.data.UserRoleJdbcRepository;
import com.chapman.edu.commissions.architecture.verticalslice.infrastructure.exceptions.ResourceNotFoundException;
import com.chapman.edu.commissions.architecture.verticalslice.infrastructure.exceptions.ValidationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class RegistrationService {

    private final UserRepository userRepository;
    private final UserRoleJdbcRepository userRoleJdbcRepository;
    private final SubscriptionPackageRepository packageRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public RegistrationService(UserRepository userRepository,
                               UserRoleJdbcRepository userRoleJdbcRepository,
                               SubscriptionPackageRepository packageRepository,
                               SubscriptionRepository subscriptionRepository,
                               PaymentRepository paymentRepository,
                               PaymentGateway paymentGateway,
                               PasswordEncoder passwordEncoder,
                               JwtService jwtService) {
        this.userRepository = userRepository;
        this.userRoleJdbcRepository = userRoleJdbcRepository;
        this.packageRepository = packageRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.paymentRepository = paymentRepository;
        this.paymentGateway = paymentGateway;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public RegistrationResponse register(RegisterRequest request) {
        request.validate();

        if (userRepository.existsByUsername(request.username())) {
            throw new ValidationException("Username already exists");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ValidationException("Email already registered");
        }

        SubscriptionPackage pkg = packageRepository.findByCode(request.packageCode())
            .orElseThrow(() -> new ResourceNotFoundException("SubscriptionPackage", request.packageCode()));
        if (!pkg.isActive()) {
            throw new ValidationException("Selected package is not available");
        }

        RegisterRequest.PaymentDetails pd = request.payment();
        String cardDigits = pd.cardNumber().replaceAll("\\s+", "");

        PaymentGateway.ChargeResult chargeResult = paymentGateway.charge(
            cardDigits, pd.cardHolderName(), pd.expiryMonth(), pd.expiryYear(),
            pd.cvv(), pkg.getMonthlyPrice(), "USD"
        );

        if (chargeResult.status() == PaymentStatus.FAILED) {
            throw new ValidationException("Payment failed: " + chargeResult.failureReason());
        }

        User user = new User(request.username(), request.email(), request.firstName(), request.lastName());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setActive(true);
        user.setCreatedBy("self-registration");
        user.addRole(UserRole.SALES_REP);
        User savedUser = userRepository.saveAndFlush(user);

        userRoleJdbcRepository.assignRole(savedUser.getId(), UserRole.SALES_REP);

        Subscription subscription = new Subscription(savedUser.getId(), pkg.getId());
        Subscription savedSubscription = subscriptionRepository.save(subscription);

        Payment payment = new Payment();
        payment.setSubscriptionId(savedSubscription.getId());
        payment.setUserId(savedUser.getId());
        payment.setAmount(pkg.getMonthlyPrice());
        payment.setCurrency("USD");
        payment.setStatus(chargeResult.status());
        payment.setCardHolderName(pd.cardHolderName());
        payment.setCardLastFour(cardDigits.substring(cardDigits.length() - 4));
        payment.setCardBrand(paymentGateway.detectCardBrand(cardDigits));
        payment.setTransactionReference(chargeResult.transactionReference());
        payment.setProcessedAt(LocalDateTime.now());
        payment.setCreatedAt(LocalDateTime.now());
        Payment savedPayment = paymentRepository.save(payment);

        if (chargeResult.status() != PaymentStatus.COMPLETED) {
            savedSubscription.setStatus(SubscriptionStatus.PENDING_PAYMENT);
            subscriptionRepository.save(savedSubscription);
        }

        String token = jwtService.issueToken(savedUser.getId(), savedUser.getUsername());

        return new RegistrationResponse(
            savedUser.getId(),
            savedUser.getUsername(),
            savedUser.getEmail(),
            savedUser.getFullName(),
            savedSubscription.getId(),
            pkg.getCode(),
            pkg.getName(),
            savedSubscription.getStatus(),
            savedPayment.getId(),
            savedPayment.getStatus(),
            savedPayment.getAmount(),
            savedPayment.getCardLastFour(),
            token,
            jwtService.getExpirationSeconds()
        );
    }
}
