package com.chapman.edu.commissions.architecture.verticalslice.features.registration;

import com.chapman.edu.commissions.architecture.verticalslice.domain.PackageTier;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserRoleJdbcRepository userRoleJdbcRepository;
    @Mock private SubscriptionPackageRepository packageRepository;
    @Mock private SubscriptionRepository subscriptionRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;

    private final PaymentGateway paymentGateway = new PaymentGateway();

    private RegistrationService registrationService;

    private SubscriptionPackage proPackage;

    @BeforeEach
    void setUp() {
        registrationService = new RegistrationService(
            userRepository,
            userRoleJdbcRepository,
            packageRepository,
            subscriptionRepository,
            paymentRepository,
            paymentGateway,
            passwordEncoder,
            jwtService);

        proPackage = new SubscriptionPackage(
            "PROFESSIONAL",
            "Pro",
            "teams",
            new BigDecimal("79.00"),
            10,
            500,
            PackageTier.PROFESSIONAL);
        proPackage.setId("pkg-pro");
    }

    private RegisterRequest validRequest() {
        return new RegisterRequest(
            "janedoe",
            "jane@example.com",
            "Jane",
            "Doe",
            "Str0ngPass!",
            "PROFESSIONAL",
            new RegisterRequest.PaymentDetails(
                "Jane Doe",
                "4242 4242 4242 4242",
                "12",
                "2030",
                "123"));
    }

    @Test
    void register_withValidRequest_createsUserSubscriptionAndChargesCard() {
        RegisterRequest request = validRequest();

        when(userRepository.existsByUsername("janedoe")).thenReturn(false);
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(packageRepository.findByCode("PROFESSIONAL")).thenReturn(Optional.of(proPackage));
        when(passwordEncoder.encode("Str0ngPass!")).thenReturn("$2a$10$encoded");
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId("usr-new");
            return u;
        });
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(inv -> {
            Subscription s = inv.getArgument(0);
            s.setId("sub-new");
            return s;
        });
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId("pay-new");
            return p;
        });
        when(jwtService.issueToken("usr-new", "janedoe")).thenReturn("jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(7200L);

        RegistrationResponse response = registrationService.register(request);

        assertThat(response.userId()).isEqualTo("usr-new");
        assertThat(response.username()).isEqualTo("janedoe");
        assertThat(response.fullName()).isEqualTo("Jane Doe");
        assertThat(response.subscriptionId()).isEqualTo("sub-new");
        assertThat(response.packageCode()).isEqualTo("PROFESSIONAL");
        assertThat(response.subscriptionStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(response.amountCharged()).isEqualByComparingTo("79.00");
        assertThat(response.cardLastFour()).isEqualTo("4242");
        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.expiresInSeconds()).isEqualTo(7200L);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertThat(saved.getPasswordHash()).isEqualTo("$2a$10$encoded");
        assertThat(saved.isActive()).isTrue();
        assertThat(saved.getRoles()).contains(UserRole.SALES_REP);

        verify(userRoleJdbcRepository).assignRole("usr-new", UserRole.SALES_REP);

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        Payment payment = paymentCaptor.getValue();
        assertThat(payment.getCardLastFour()).isEqualTo("4242");
        assertThat(payment.getCardBrand()).isEqualTo("VISA");
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(payment.getTransactionReference()).startsWith("TXN-");
    }

    @Test
    void register_withExistingUsername_throwsValidationException() {
        when(userRepository.existsByUsername("janedoe")).thenReturn(true);

        assertThatThrownBy(() -> registrationService.register(validRequest()))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Username already exists");

        verify(userRepository, never()).saveAndFlush(any(User.class));
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void register_withExistingEmail_throwsValidationException() {
        when(userRepository.existsByUsername("janedoe")).thenReturn(false);
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(true);

        assertThatThrownBy(() -> registrationService.register(validRequest()))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Email already registered");
    }

    @Test
    void register_withMissingPackage_throwsResourceNotFoundException() {
        when(userRepository.existsByUsername("janedoe")).thenReturn(false);
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(packageRepository.findByCode("PROFESSIONAL")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> registrationService.register(validRequest()))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("PROFESSIONAL");
    }

    @Test
    void register_whenCardDeclined_rejectsAndPersistsNothing() {
        when(userRepository.existsByUsername("janedoe")).thenReturn(false);
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(packageRepository.findByCode("PROFESSIONAL")).thenReturn(Optional.of(proPackage));

        RegisterRequest declined = new RegisterRequest(
            "janedoe",
            "jane@example.com",
            "Jane",
            "Doe",
            "Str0ngPass!",
            "PROFESSIONAL",
            new RegisterRequest.PaymentDetails(
                "Jane Doe",
                "4111 1111 1111 0000",
                "12",
                "2030",
                "123"));

        assertThatThrownBy(() -> registrationService.register(declined))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Payment failed");

        verify(userRepository, never()).saveAndFlush(any(User.class));
        verify(subscriptionRepository, never()).save(any(Subscription.class));
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(userRoleJdbcRepository, never()).assignRole(anyString(), eq(UserRole.SALES_REP));
    }

    @Test
    void register_withShortPassword_failsValidation() {
        RegisterRequest bad = new RegisterRequest(
            "janedoe",
            "jane@example.com",
            "Jane",
            "Doe",
            "short",
            "PROFESSIONAL",
            new RegisterRequest.PaymentDetails(
                "Jane Doe", "4242 4242 4242 4242", "12", "2030", "123"));

        assertThatThrownBy(() -> registrationService.register(bad))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("at least 8 characters");
    }

    @Test
    void register_withInvalidCardNumber_failsValidation() {
        RegisterRequest bad = new RegisterRequest(
            "janedoe",
            "jane@example.com",
            "Jane",
            "Doe",
            "Str0ngPass!",
            "PROFESSIONAL",
            new RegisterRequest.PaymentDetails(
                "Jane Doe", "1234", "12", "2030", "123"));

        assertThatThrownBy(() -> registrationService.register(bad))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Card number");
    }
}
