package com.chapman.edu.commissions.springboot.processor;

import com.chapman.edu.commissions.springboot.dto.request.*;
import com.chapman.edu.commissions.springboot.exception.BusinessValidationException;
import com.chapman.edu.commissions.springboot.exception.ResourceNotFoundException;
import com.chapman.edu.commissions.springboot.service.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

/**
 * ============================================================================
 * PROCESSOR: ERROR HANDLING & VALIDATION DEMONSTRATION
 * ============================================================================
 *
 * This runnable demonstrates:
 *   1. Bean Validation (JSR 380) — @Valid, @NotNull, @Size, @NotBlank
 *   2. Programmatic Validation — Using the Validator interface
 *   3. Custom Exceptions — ResourceNotFoundException, BusinessValidationException
 *   4. @ControllerAdvice — Global exception handling (explained, not runnable here)
 *   5. @ExceptionHandler — Mapping exceptions to HTTP responses
 *
 * The @ControllerAdvice and @ExceptionHandler work in the web layer
 * (intercepting HTTP requests). This processor demonstrates the VALIDATION
 * and EXCEPTION parts that those handlers would catch.
 */
@Component
@Order(4)
public class ValidationProcessor implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ValidationProcessor.class);

    private final Validator validator;
    private final DealService dealService;
    private final CommissionPlanService planService;
    private final DisputeService disputeService;

    public ValidationProcessor(Validator validator,
                               DealService dealService,
                               CommissionPlanService planService,
                               DisputeService disputeService) {
        this.validator = validator;
        this.dealService = dealService;
        this.planService = planService;
        this.disputeService = disputeService;
    }

    @Override
    public void run(String... args) {
        logger.info("");
        logger.info("╔══════════════════════════════════════════════════════════════╗");
        logger.info("║   ERROR HANDLING & VALIDATION DEMONSTRATION                 ║");
        logger.info("╚══════════════════════════════════════════════════════════════╝");

        demonstrateBeanValidation();
        demonstrateCustomExceptions();
        demonstrateBusinessValidation();

        logger.info("");
        logger.info("=== Validation & Error Handling Demo Complete ===");
        logger.info("");
    }

    /**
     * Demonstrates programmatic Bean Validation using JSR 380 annotations.
     * In controllers, @Valid triggers this automatically on @RequestBody objects.
     * Here we use the Validator bean to show what happens behind the scenes.
     */
    private void demonstrateBeanValidation() {
        logger.info("");
        logger.info("--- Bean Validation (JSR 380) ---");

        // Test 1: Invalid CreateDealRequest (missing required fields)
        logger.info("");
        logger.info("Validating an EMPTY CreateDealRequest...");
        CreateDealRequest emptyRequest = new CreateDealRequest();
        Set<ConstraintViolation<CreateDealRequest>> violations = validator.validate(emptyRequest);
        logger.info("  Violations found: {}", violations.size());
        violations.forEach(v -> logger.info("    {} → {}", v.getPropertyPath(), v.getMessage()));
        logger.info("  (In REST API, this returns HTTP 400 with field-level errors)");

        // Test 2: CreateDealRequest with invalid values
        logger.info("");
        logger.info("Validating a CreateDealRequest with invalid values...");
        CreateDealRequest invalidRequest = new CreateDealRequest();
        invalidRequest.setTitle("AB");  // Too short (min=3)
        invalidRequest.setValue(new BigDecimal("-100"));  // Negative (min=0.01)
        invalidRequest.setSalesRepId("");  // Blank
        Set<ConstraintViolation<CreateDealRequest>> violations2 = validator.validate(invalidRequest);
        logger.info("  Violations found: {}", violations2.size());
        violations2.forEach(v -> logger.info("    {} = '{}' → {}", v.getPropertyPath(), v.getInvalidValue(), v.getMessage()));

        // Test 3: Valid CreateDealRequest
        logger.info("");
        logger.info("Validating a VALID CreateDealRequest...");
        CreateDealRequest validRequest = new CreateDealRequest();
        validRequest.setTitle("Valid Deal Title");
        validRequest.setValue(new BigDecimal("25000"));
        validRequest.setSalesRepId("user-003");
        Set<ConstraintViolation<CreateDealRequest>> validViolations = validator.validate(validRequest);
        logger.info("  Violations found: {} (validation passed!)", validViolations.size());

        // Test 4: CreateUserRequest with @Email validation
        logger.info("");
        logger.info("Validating CreateUserRequest with @Email...");
        CreateUserRequest userReq = new CreateUserRequest();
        userReq.setUsername("ab");  // Too short
        userReq.setEmail("not-an-email");  // Invalid email
        userReq.setFirstName("");  // Blank
        userReq.setLastName("Test");
        userReq.setPassword("12345");  // Too short
        Set<ConstraintViolation<CreateUserRequest>> userViolations = validator.validate(userReq);
        logger.info("  Violations found: {}", userViolations.size());
        userViolations.forEach(v -> logger.info("    {} → {}", v.getPropertyPath(), v.getMessage()));
    }

    /**
     * Demonstrates custom exceptions that @ControllerAdvice catches.
     */
    private void demonstrateCustomExceptions() {
        logger.info("");
        logger.info("--- Custom Exceptions (@ControllerAdvice catches these) ---");

        // ResourceNotFoundException → HTTP 404
        logger.info("");
        logger.info("Triggering ResourceNotFoundException (HTTP 404)...");
        try {
            dealService.getDealById("non-existent-id");
        } catch (ResourceNotFoundException e) {
            logger.info("  Caught: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            logger.info("  Resource: {}, Field: {}, Value: {}",
                e.getResourceName(), e.getFieldName(), e.getFieldValue());
            logger.info("  GlobalExceptionHandler maps this to HTTP 404 Not Found");
        }

        // ResourceNotFoundException for User
        logger.info("");
        logger.info("Triggering ResourceNotFoundException for non-existent user...");
        try {
            dealService.getDealById("fake-deal-999");
        } catch (ResourceNotFoundException e) {
            logger.info("  Caught: {}", e.getMessage());
        }
    }

    /**
     * Demonstrates business validation exceptions.
     */
    private void demonstrateBusinessValidation() {
        logger.info("");
        logger.info("--- Business Validation (Beyond Bean Validation) ---");

        // BusinessValidationException → HTTP 422
        logger.info("");
        logger.info("Attempting to create a plan with end date before start date...");
        try {
            CreatePlanRequest badPlan = new CreatePlanRequest();
            badPlan.setName("Bad Plan");
            badPlan.setCurrencyCode("USD");
            badPlan.setEffectiveStartDate(LocalDate.of(2024, 12, 31));
            badPlan.setEffectiveEndDate(LocalDate.of(2024, 1, 1));  // Before start!
            planService.createPlan(badPlan);
        } catch (BusinessValidationException e) {
            logger.info("  Caught: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            logger.info("  GlobalExceptionHandler maps this to HTTP 422 Unprocessable Entity");
        }

        // Attempting to activate a non-DRAFT plan
        logger.info("");
        logger.info("Attempting to activate an already ACTIVE plan...");
        try {
            planService.activatePlan("plan-001");  // Already ACTIVE
        } catch (BusinessValidationException e) {
            logger.info("  Caught: {} - {}", e.getClass().getSimpleName(), e.getMessage());
        } catch (ResourceNotFoundException e) {
            logger.info("  Caught: {} - {}", e.getClass().getSimpleName(), e.getMessage());
        }
    }
}
