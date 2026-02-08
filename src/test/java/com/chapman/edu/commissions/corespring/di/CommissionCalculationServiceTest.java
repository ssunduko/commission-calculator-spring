package com.chapman.edu.commissions.corespring.di;

import com.chapman.edu.commissions.model.CommissionCalculation;
import com.chapman.edu.commissions.model.Deal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CommissionCalculationService demonstrating the benefits of Dependency Injection.
 *
 * KEY POINTS DEMONSTRATED:
 * 1. EASY TESTING - Dependencies are mocked, no real email/database needed
 * 2. ISOLATION - Test only the business logic, not the dependencies
 * 3. VERIFICATION - Can verify method calls and parameters
 * 4. CONTROL - Full control over dependency behavior
 * 5. FAST - No I/O operations, tests run in milliseconds
 *
 * COMPARE WITH: SimpleCalculationService which cannot be easily tested this way
 * because it creates its own dependencies with 'new' keyword.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CommissionCalculationService Tests - Demonstrating DI Benefits")
class CommissionCalculationServiceTest {

    // BENEFIT 1: Mock dependencies easily
    // Mockito creates fake implementations automatically
    @Mock
    private CommissionRuleEngine ruleEngine;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuditLogger auditLogger;

    @Mock
    private ValidationService validationService;

    // BENEFIT 2: Create service with mocked dependencies
    // Constructor injection for required dependencies
    // ReflectionTestUtils for field injection
    private CommissionCalculationService service;

    @Captor
    private ArgumentCaptor<CommissionCalculation> calculationCaptor;

    private Deal testDeal;

    @BeforeEach
    void setUp() {
        // BENEFIT: Easy to inject mocks manually for testing

        // Create service with constructor-injected mocks
        // Constructor injection makes this straightforward!
        service = new CommissionCalculationService(ruleEngine, notificationService);

        // Inject field-injected dependencies using Spring's ReflectionTestUtils
        // This simulates what Spring does with @Autowired field injection
        // NOTE: This demonstrates WHY constructor injection is preferred - field injection
        // requires reflection to test, while constructor injection is simple and direct!
        ReflectionTestUtils.setField(service, "auditLogger", auditLogger);

        // Inject setter-injected optional dependencies
        // Setter injection is easy to use in tests - just call the setter!
        service.setValidationService(validationService);

        // Create test data
        testDeal = new Deal("Enterprise Software License", new BigDecimal("10000"), "rep-001");
        testDeal.setId("deal-123");
    }

    @Test
    @DisplayName("Should calculate commission successfully with mocked dependencies")
    void testCalculateCommission_Success() {
        // BENEFIT 3: Control dependency behavior with 'when/thenReturn'
        // Define what mocks should return
        when(ruleEngine.calculateBaseCommission(testDeal, "standard-plan"))
            .thenReturn(new BigDecimal("1000.00"));

        when(ruleEngine.calculateBonuses(testDeal, "standard-plan"))
            .thenReturn(new BigDecimal("200.00"));

        // Act - call the method under test
        CommissionCalculation result = service.calculateCommission(testDeal, "standard-plan");

        // Assert - verify the result
        assertNotNull(result);
        assertEquals("deal-123", result.getDealId());
        assertEquals("rep-001", result.getSalesRepId());
        assertEquals(new BigDecimal("1000.00"), result.getBaseCommission());
        assertEquals(new BigDecimal("1200.00"), result.getGrossCommission());

        // BENEFIT 4: Verify interactions with dependencies
        // Verify that methods were called with expected parameters
        verify(ruleEngine).calculateBaseCommission(testDeal, "standard-plan");
        verify(ruleEngine).calculateBonuses(testDeal, "standard-plan");

        // BENEFIT 5: Verify notification was sent with correct calculation
        verify(notificationService).notifyCommissionCalculated(any(CommissionCalculation.class));

        // BENEFIT 6: Verify audit logging happened
        verify(auditLogger).log(contains("Commission calculated for deal: deal-123"));
    }

    @Test
    @DisplayName("Should call validation service when available")
    void testCalculateCommission_WithValidation() {
        // Arrange
        when(ruleEngine.calculateBaseCommission(any(), any()))
            .thenReturn(new BigDecimal("1000.00"));
        when(ruleEngine.calculateBonuses(any(), any()))
            .thenReturn(BigDecimal.ZERO);

        // ValidationService is optional (injected via setter with required=false)
        // Mockito will inject it via the setter if the mock is present

        // Act
        service.calculateCommission(testDeal, "standard-plan");

        // Assert - verify validation was called if ValidationService was injected
        verify(validationService).validateDeal(testDeal);
    }

    @Test
    @DisplayName("Should handle large commission calculations")
    void testCalculateCommission_LargeAmount() {
        // Arrange - test with large deal value
        Deal largeDeal = new Deal("Enterprise Agreement", new BigDecimal("1000000"), "rep-senior");
        largeDeal.setId("deal-large");

        when(ruleEngine.calculateBaseCommission(largeDeal, "premium-plan"))
            .thenReturn(new BigDecimal("100000.00"));

        when(ruleEngine.calculateBonuses(largeDeal, "premium-plan"))
            .thenReturn(new BigDecimal("50000.00"));

        // Act
        CommissionCalculation result = service.calculateCommission(largeDeal, "premium-plan");

        // Assert
        assertEquals(new BigDecimal("100000.00"), result.getBaseCommission());
        assertEquals(new BigDecimal("150000.00"), result.getGrossCommission());

        // Verify methods were called exactly once
        verify(ruleEngine, times(1)).calculateBaseCommission(largeDeal, "premium-plan");
        verify(ruleEngine, times(1)).calculateBonuses(largeDeal, "premium-plan");
    }

    @Test
    @DisplayName("Should handle zero bonus scenarios")
    void testCalculateCommission_ZeroBonus() {
        // Arrange - small deal with no bonus
        Deal smallDeal = new Deal("Starter Plan", new BigDecimal("1000"), "rep-002");
        smallDeal.setId("deal-small");

        when(ruleEngine.calculateBaseCommission(smallDeal, "basic-plan"))
            .thenReturn(new BigDecimal("100.00"));

        when(ruleEngine.calculateBonuses(smallDeal, "basic-plan"))
            .thenReturn(BigDecimal.ZERO);  // No bonus

        // Act
        CommissionCalculation result = service.calculateCommission(smallDeal, "basic-plan");

        // Assert
        assertEquals(new BigDecimal("100.00"), result.getBaseCommission());
        assertEquals(new BigDecimal("100.00"), result.getGrossCommission());
        assertEquals(result.getBaseCommission(), result.getGrossCommission(),
            "Gross should equal base when no bonus");
    }

    @Test
    @DisplayName("Should capture and verify notification details")
    void testCalculateCommission_VerifyNotificationDetails() {
        // Arrange
        when(ruleEngine.calculateBaseCommission(any(), any()))
            .thenReturn(new BigDecimal("1000.00"));
        when(ruleEngine.calculateBonuses(any(), any()))
            .thenReturn(new BigDecimal("200.00"));

        // Act
        service.calculateCommission(testDeal, "standard-plan");

        // BENEFIT 7: Capture arguments to verify exact values passed
        verify(notificationService).notifyCommissionCalculated(calculationCaptor.capture());

        CommissionCalculation capturedCalculation = calculationCaptor.getValue();
        assertEquals("deal-123", capturedCalculation.getDealId());
        assertEquals("rep-001", capturedCalculation.getSalesRepId());
        assertEquals(new BigDecimal("1000.00"), capturedCalculation.getBaseCommission());
        assertEquals(new BigDecimal("1200.00"), capturedCalculation.getGrossCommission());
    }

    @Test
    @DisplayName("Should verify audit logger receives correct message")
    void testCalculateCommission_VerifyAuditMessage() {
        // Arrange
        when(ruleEngine.calculateBaseCommission(any(), any()))
            .thenReturn(new BigDecimal("1000.00"));
        when(ruleEngine.calculateBonuses(any(), any()))
            .thenReturn(BigDecimal.ZERO);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        service.calculateCommission(testDeal, "standard-plan");

        // Assert - verify exact audit message
        verify(auditLogger).log(messageCaptor.capture());
        String auditMessage = messageCaptor.getValue();

        assertTrue(auditMessage.contains("Commission calculated for deal"),
            "Audit message should contain action description");
        assertTrue(auditMessage.contains("deal-123"),
            "Audit message should contain deal ID");
    }

    @Test
    @DisplayName("Should not call notification if exception occurs in rule engine")
    void testCalculateCommission_ExceptionInRuleEngine() {
        // BENEFIT 8: Easy to test error scenarios with mocks
        // Arrange - simulate rule engine failure
        when(ruleEngine.calculateBaseCommission(any(), any()))
            .thenThrow(new RuntimeException("Rule engine failure"));

        // Act & Assert - verify exception is thrown
        assertThrows(RuntimeException.class, () -> {
            service.calculateCommission(testDeal, "standard-plan");
        });

        // BENEFIT 9: Verify notification was NOT sent due to exception
        verify(notificationService, never()).notifyCommissionCalculated(any());
    }

    @Test
    @DisplayName("Should handle different plan IDs correctly")
    void testCalculateCommission_DifferentPlans() {
        // Arrange - test multiple plan scenarios
        String[] planIds = {"basic-plan", "standard-plan", "premium-plan", "enterprise-plan"};
        BigDecimal[] expectedCommissions = {
            new BigDecimal("500.00"),
            new BigDecimal("1000.00"),
            new BigDecimal("1500.00"),
            new BigDecimal("2000.00")
        };

        for (int i = 0; i < planIds.length; i++) {
            // Setup mock behavior for each plan
            when(ruleEngine.calculateBaseCommission(testDeal, planIds[i]))
                .thenReturn(expectedCommissions[i]);
            when(ruleEngine.calculateBonuses(testDeal, planIds[i]))
                .thenReturn(BigDecimal.ZERO);

            // Act
            CommissionCalculation result = service.calculateCommission(testDeal, planIds[i]);

            // Assert
            assertEquals(expectedCommissions[i], result.getBaseCommission(),
                "Commission should match expected value for plan: " + planIds[i]);

            // Verify rule engine was called with correct plan ID
            verify(ruleEngine).calculateBaseCommission(testDeal, planIds[i]);
        }

        // Verify notification sent for each calculation
        verify(notificationService, times(planIds.length))
            .notifyCommissionCalculated(any());
    }

    @Test
    @DisplayName("Demonstrate dependency status reporting")
    void testGetDependencyStatus() {
        // This tests the helper method that shows all dependencies are injected
        String status = service.getDependencyStatus();

        assertNotNull(status);
        assertTrue(status.contains("RuleEngine: injected"),
            "Should show RuleEngine is injected");
        assertTrue(status.contains("NotificationService: injected"),
            "Should show NotificationService is injected");
        assertTrue(status.contains("AuditLogger: injected"),
            "Should show AuditLogger is injected");
    }

    /**
     * COMPARISON: How would this test look for SimpleCalculationService?
     *
     * public class SimpleCalculationServiceTest {
     *
     *     @Test
     *     public void testCalculateCommission() {
     *         // PROBLEM 1: Cannot inject mocks
     *         SimpleCalculationService service = new SimpleCalculationService();
     *         // Real EmailService created - will try to send emails!
     *         // Real DatabaseAuditLogger created - will try to write to database!
     *
     *         Deal deal = new Deal("Test", new BigDecimal("1000"), "rep-1");
     *         deal.setId("deal-1");
     *
     *         // PROBLEM 2: Cannot control dependency behavior
     *         // PROBLEM 3: Real I/O operations slow down tests
     *         CommissionCalculation result = service.calculateCommission(deal, "plan-1");
     *
     *         // PROBLEM 4: Can only assert on result, not interactions
     *         assertEquals(new BigDecimal("100.00"), result.getBaseCommission());
     *
     *         // PROBLEM 5: Cannot verify:
     *         // - Was notify() called?
     *         // - What parameters were passed to notify()?
     *         // - Was audit log written correctly?
     *         // - Were methods called in correct order?
     *     }
     *
     *     // PROBLEM 6: Cannot test error scenarios easily
     *     // How to simulate EmailService failure?
     *     // How to test if notification fails but calculation succeeds?
     *
     *     // PROBLEM 7: Tests are slow due to real I/O
     *     // PROBLEM 8: Tests may fail due to external systems (email server down, etc.)
     *     // PROBLEM 9: Cannot run tests in parallel (shared resources)
     *     // PROBLEM 10: Difficult to set up test environment (need email server, database)
     * }
     *
     * WITH DEPENDENCY INJECTION:
     * Fast tests (milliseconds)
     * Isolated tests (no external dependencies)
     * Reliable tests (no flaky failures from external systems)
     * Comprehensive verification (can verify all interactions)
     * Easy error scenario testing (mock exceptions)
     * Parallel test execution (no shared state)
     * Simple test setup (just create mocks)
     */
}
