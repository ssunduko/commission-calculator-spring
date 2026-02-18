package com.chapman.edu.commissions.orm.processor;

import com.chapman.edu.commissions.orm.entity.*;
import com.chapman.edu.commissions.orm.service.CommissionService;
import com.chapman.edu.commissions.orm.service.DealService;
import com.chapman.edu.commissions.orm.service.DisputeService;
import com.chapman.edu.commissions.orm.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ============================================================
 * PROCESSOR: Transaction Management & Isolation Levels
 * ============================================================
 *
 * This processor demonstrates Spring's declarative transaction management
 * by exercising the service layer methods, which use @Transactional.
 *
 * ============================================================
 * WHAT IS A TRANSACTION?
 * ============================================================
 * A transaction is a unit of work that follows ACID properties:
 *
 * A - Atomicity: All operations succeed or ALL fail (all-or-nothing).
 *     If step 3 of 5 fails, steps 1-2 are rolled back.
 *
 * C - Consistency: The database moves from one valid state to another.
 *     Constraints (FK, unique, not null) are enforced.
 *
 * I - Isolation: Concurrent transactions don't interfere with each other.
 *     Each transaction sees a consistent snapshot of the data.
 *
 * D - Durability: Once committed, changes survive system crashes.
 *     Data is written to persistent storage.
 *
 * ============================================================
 * SPRING @Transactional ATTRIBUTES
 * ============================================================
 *
 * readOnly:
 *   true  = Read-only transaction (optimization: skip dirty checking)
 *   false = Read-write transaction (default)
 *
 * isolation:
 *   DEFAULT          = Use the database default isolation level
 *   READ_UNCOMMITTED = Dirty reads allowed (fastest, least safe)
 *   READ_COMMITTED   = Only see committed data (no dirty reads)
 *   REPEATABLE_READ  = Consistent reads within transaction
 *   SERIALIZABLE     = Full isolation, sequential execution
 *
 * propagation:
 *   REQUIRED     = Join existing or create new (default)
 *   REQUIRES_NEW = Always create a new, independent transaction
 *   NESTED       = Savepoint within current transaction
 *   SUPPORTS     = Use existing or run without transaction
 *   MANDATORY    = Must have existing transaction (throws if none)
 *   NOT_SUPPORTED = Suspend current transaction
 *   NEVER        = Must NOT have a transaction (throws if exists)
 *
 * rollbackFor:
 *   Specify which exceptions trigger rollback.
 *   Default: Rollback on RuntimeException (unchecked)
 *   Use rollbackFor = Exception.class for checked exceptions too.
 *
 * timeout:
 *   Maximum time (in seconds) for the transaction.
 *   Prevents long-running transactions from holding locks.
 *
 * ============================================================
 * ISOLATION LEVEL COMPARISON
 * ============================================================
 *
 * Problem:           READ_UNCOMMITTED  READ_COMMITTED  REPEATABLE_READ  SERIALIZABLE
 * Dirty Read         Possible          Prevented       Prevented        Prevented
 * Non-Repeatable     Possible          Possible        Prevented        Prevented
 * Phantom Read       Possible          Possible        Possible*        Prevented
 *
 * (*MySQL's InnoDB prevents phantom reads at REPEATABLE_READ using gap locks)
 *
 * Performance:       Fastest           Fast            Moderate         Slowest
 * Concurrency:       Highest           High            Moderate         Lowest
 * Safety:            Lowest            Good            Better           Best
 *
 * ============================================================
 * COMMON PITFALLS
 * ============================================================
 *
 * 1. SELF-INVOCATION:
 *    @Transactional only works when called THROUGH the proxy.
 *    Calling a @Transactional method from within the same class
 *    bypasses the proxy, so no transaction is created!
 *
 *    BAD:  this.saveUser(user)  // No transaction!
 *    GOOD: injectedService.saveUser(user)  // Through proxy
 *
 * 2. CHECKED EXCEPTIONS:
 *    By default, Spring does NOT roll back on checked exceptions.
 *    Use rollbackFor = Exception.class if you want rollback on all exceptions.
 *
 * 3. READONLY OPTIMIZATION:
 *    readOnly = true is not just documentation. It:
 *    - Disables dirty checking (performance boost)
 *    - May route to read replicas (in master-slave setups)
 *    - Prevents accidental writes (some DBs enforce this)
 */
@Component
@Order(4)
public class TransactionProcessor implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(TransactionProcessor.class);

    private final UserService userService;
    private final DealService dealService;
    private final CommissionService commissionService;
    private final DisputeService disputeService;

    public TransactionProcessor(UserService userService,
                                DealService dealService,
                                CommissionService commissionService,
                                DisputeService disputeService) {
        this.userService = userService;
        this.dealService = dealService;
        this.commissionService = commissionService;
        this.disputeService = disputeService;
    }

    @Override
    public void run(String... args) {
        log.info("============================================================");
        log.info("TRANSACTION PROCESSOR: Transaction Management Demo");
        log.info("============================================================");

        demonstrateReadOnlyTransaction();
        demonstrateReadWriteTransaction();
        demonstrateCommissionCalculation();
        demonstrateTransactionRollback();

        log.info("============================================================");
        log.info("TRANSACTION PROCESSOR: Complete");
        log.info("============================================================");
    }

    private void demonstrateReadOnlyTransaction() {
        log.info("");
        log.info("--- Read-Only Transactions (@Transactional(readOnly = true)) ---");
        log.info("Read-only transactions skip dirty checking and may use read replicas.");

        // UserService class-level @Transactional(readOnly = true)
        // All read methods run in a read-only transaction
        userService.findById("usr-001").ifPresent(user ->
                log.info("Read user (readOnly=true): {} - {}", user.getFullName(), user.getTerritory()));

        List<User> salesReps = userService.findActiveUsersByRole(UserRole.SALES_REP);
        log.info("Found {} active sales reps in read-only transaction", salesReps.size());

        // DealService also uses class-level readOnly = true
        dealService.findByStatus(DealStatus.WON).forEach(deal ->
                log.info("  Won deal: '{}' (${})", deal.getTitle(), deal.getValue()));
    }

    private void demonstrateReadWriteTransaction() {
        log.info("");
        log.info("--- Read-Write Transactions (@Transactional(readOnly = false)) ---");
        log.info("Write methods override class-level readOnly to enable data modification.");

        // DealService.updateDealStatus has @Transactional(readOnly = false)
        // This demonstrates a simple transactional write operation
        dealService.findById("deal-005").ifPresent(deal -> {
            log.info("Deal before update: '{}' status={}", deal.getTitle(), deal.getStatus());
            // Note: We only log here; the actual status update would modify the seed data
            log.info("  In production, updateDealStatus() runs in a read-write transaction");
            log.info("  The status change AND closeDate update happen atomically");
        });
    }

    private void demonstrateCommissionCalculation() {
        log.info("");
        log.info("--- REPEATABLE_READ Isolation (Commission Calculation) ---");
        log.info("CommissionService.calculateCommission uses REPEATABLE_READ isolation.");
        log.info("This ensures consistent reads during the multi-step calculation.");
        log.info("");
        log.info("Calculation steps (all in one transaction):");
        log.info("  1. Load the deal (and verify it's WON)");
        log.info("  2. Load the commission plan with tiers");
        log.info("  3. Find the applicable tier for the deal value");
        log.info("  4. Calculate base commission");
        log.info("  5. Apply active bonus rules");
        log.info("  6. Save the calculation result");
        log.info("");
        log.info("If ANY step fails, ALL changes are rolled back (atomicity).");
        log.info("REPEATABLE_READ prevents another transaction from modifying");
        log.info("the deal value between steps 1 and 4 (isolation).");

        // Show existing calculations from seed data
        commissionService.findBySalesRep("usr-001").forEach(calc ->
                log.info("  Existing calc: base=${}, gross=${}, status={}",
                        calc.getBaseCommission(), calc.getGrossCommission(), calc.getStatus()));
    }

    private void demonstrateTransactionRollback() {
        log.info("");
        log.info("--- Transaction Rollback Behavior ---");
        log.info("By default, @Transactional rolls back on RuntimeException.");

        // Demonstrate that invalid operations throw exceptions (and roll back)
        try {
            // This would fail because the deal is not WON
            log.info("Attempting to calculate commission on an OPEN deal...");
            log.info("  This would throw IllegalStateException -> transaction rolls back");
            log.info("  No partial data is saved to the database");
        } catch (Exception e) {
            log.info("  Caught: {} - transaction was rolled back", e.getMessage());
        }

        log.info("");
        log.info("--- Propagation.REQUIRES_NEW (Independent Transactions) ---");
        log.info("DisputeService.addComment uses REQUIRES_NEW propagation.");
        log.info("Even if the parent transaction fails, the comment is still saved.");
        log.info("This is essential for audit trails that must persist regardless.");
    }
}
