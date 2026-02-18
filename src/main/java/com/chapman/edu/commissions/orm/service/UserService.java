package com.chapman.edu.commissions.orm.service;

import com.chapman.edu.commissions.orm.entity.User;
import com.chapman.edu.commissions.orm.entity.UserRole;
import com.chapman.edu.commissions.orm.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ============================================================
 * SERVICE LAYER: UserService
 * ============================================================
 *
 * TRANSACTION MANAGEMENT:
 * Spring's @Transactional annotation manages database transactions declaratively.
 * Instead of manually calling begin(), commit(), rollback(), Spring handles
 * transaction boundaries through AOP proxies.
 *
 * HOW @Transactional WORKS:
 * 1. Spring creates a proxy around the service bean
 * 2. When a @Transactional method is called, the proxy:
 *    a. Opens a database transaction
 *    b. Executes the method
 *    c. Commits if no exception is thrown
 *    d. Rolls back if a RuntimeException is thrown
 *
 * CLASS-LEVEL @Transactional:
 * Applied here at the class level, meaning ALL methods run in a transaction.
 * readOnly = true: Optimization hint for read operations.
 * Methods that modify data override this with @Transactional(readOnly = false).
 *
 * CACHING STRATEGY:
 * - @Cacheable: Read-through cache (check cache first, then DB)
 * - @CachePut: Write-through cache (update DB and cache simultaneously)
 * - @CacheEvict: Remove stale entries when data changes
 */
@Service
@Transactional(readOnly = true)
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * @Cacheable: Cache the result of this method.
     *
     * How it works:
     * 1. First call: Executes the method, caches the result with key = id
     * 2. Subsequent calls with the same id: Returns cached result WITHOUT
     *    executing the method (no database query).
     *
     * Parameters:
     * - value = "users": The cache name (defined in CacheConfig)
     * - key = "#id": SpEL expression for the cache key
     *
     * Cache keys can be complex SpEL expressions:
     * - #id: Method parameter
     * - #result.id: Property of the return value
     * - #root.method.name + #id: Combine method name with parameter
     *
     * The "unless" attribute prevents caching when the condition is true.
     * Since Spring Cache unwraps Optional (caching null for empty Optionals),
     * we must skip caching when the result is empty — otherwise the cache
     * rejects the null value (allowNullValues = false in CacheConfig).
     */
    @Cacheable(value = "users", key = "#id", unless = "#result == null")
    public Optional<User> findById(String id) {
        log.info("Cache MISS - Loading user from database: {}", id);
        return userRepository.findById(id);
    }

    @Cacheable(value = "users", key = "'username:' + #username", unless = "#result == null")
    public Optional<User> findByUsername(String username) {
        log.info("Cache MISS - Loading user by username: {}", username);
        return userRepository.findByUsername(username);
    }

    public List<User> findByDepartment(String department) {
        return userRepository.findByDepartment(department);
    }

    public List<User> findActiveUsersByRole(UserRole role) {
        return userRepository.findActiveUsersByRole(role);
    }

    public List<User> findDirectReports(String managerId) {
        return userRepository.findDirectReportsByManagerId(managerId);
    }

    public List<User> searchByName(String searchTerm) {
        return userRepository.searchByName(searchTerm);
    }

    /**
     * @Transactional(readOnly = false): Overrides the class-level readOnly = true.
     * This method modifies data and needs a read-write transaction.
     *
     * @CachePut: Updates the cache with the new/modified entity.
     * Unlike @Cacheable, @CachePut ALWAYS executes the method
     * and puts the result in the cache.
     *
     * Use @CachePut (not @Cacheable) for write operations to ensure
     * the cache stays in sync with the database.
     */
    @Transactional(readOnly = false)
    @CachePut(value = "users", key = "#result.id")
    public User createUser(User user) {
        log.info("Creating user: {}", user.getUsername());
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }
        return userRepository.save(user);
    }

    /**
     * @CacheEvict: Removes the cached entry for this user.
     * Called when the user is updated, to prevent serving stale data.
     *
     * Alternative: Use @CachePut to update the cache immediately.
     * @CacheEvict is simpler but the next read will be a cache miss.
     */
    @Transactional(readOnly = false)
    @CacheEvict(value = "users", key = "#user.id")
    public User updateUser(User user) {
        log.info("Updating user: {}", user.getId());
        return userRepository.save(user);
    }

    /**
     * TRANSACTION PROPAGATION: Propagation.REQUIRES_NEW
     *
     * This creates a NEW, independent transaction regardless of any
     * existing transaction. This is useful for audit logging that must
     * be committed even if the outer transaction rolls back.
     *
     * Propagation options:
     * - REQUIRED (default): Join existing transaction or create a new one
     * - REQUIRES_NEW: Always create a new, independent transaction
     * - NESTED: Create a savepoint within the current transaction
     * - SUPPORTS: Run in transaction if one exists, otherwise non-transactional
     * - NOT_SUPPORTED: Suspend any existing transaction
     * - MANDATORY: Must run within an existing transaction (throws if none exists)
     * - NEVER: Must NOT run within a transaction (throws if one exists)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordLogin(String userId) {
        log.info("Recording login for user: {}", userId);
        userRepository.findById(userId).ifPresent(user -> {
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
        });
    }

    /**
     * TRANSACTION ISOLATION LEVEL: READ_COMMITTED
     *
     * Isolation levels control what data a transaction can see from
     * concurrent transactions:
     *
     * - READ_UNCOMMITTED: Can see uncommitted changes (dirty reads). Fastest but least safe.
     * - READ_COMMITTED: Only sees committed data. Prevents dirty reads.
     *   Most common default (PostgreSQL, Oracle, SQL Server).
     * - REPEATABLE_READ: Consistent reads within the transaction. Prevents
     *   dirty reads and non-repeatable reads. MySQL default.
     * - SERIALIZABLE: Full isolation. Transactions run as if sequential.
     *   Safest but slowest (lots of locking).
     *
     * DIRTY READ: Reading uncommitted data that might be rolled back.
     * NON-REPEATABLE READ: Reading same row twice gets different values
     *   (another transaction committed an update between reads).
     * PHANTOM READ: A range query returns different rows on re-execution
     *   (another transaction inserted/deleted rows).
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = false)
    @CacheEvict(value = "users", key = "#userId")
    public User deactivateUser(String userId) {
        log.info("Deactivating user: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.setActive(false);
        return userRepository.save(user);
    }

    /**
     * @CacheEvict with allEntries = true: Clears ALL entries in the "users" cache.
     * Use this when a bulk operation invalidates multiple cache entries
     * and it's impractical to evict them individually.
     *
     * WARNING: This is a nuclear option for the cache. Use sparingly
     * as it causes a "thundering herd" of cache misses.
     */
    @CacheEvict(value = "users", allEntries = true)
    public void clearUserCache() {
        log.info("Clearing entire users cache");
    }
}
