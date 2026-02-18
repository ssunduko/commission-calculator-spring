package com.chapman.edu.commissions.orm.processor;

import com.chapman.edu.commissions.orm.entity.User;
import com.chapman.edu.commissions.orm.service.CommissionService;
import com.chapman.edu.commissions.orm.service.DealService;
import com.chapman.edu.commissions.orm.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cache.CacheManager;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * ============================================================
 * PROCESSOR: Caching Strategies with Spring Cache
 * ============================================================
 *
 * This processor demonstrates Spring Cache abstraction by exercising
 * cached service methods and observing cache hits/misses.
 *
 * ============================================================
 * SPRING CACHE ANNOTATIONS
 * ============================================================
 *
 * @Cacheable(value = "cacheName", key = "#param")
 *   - READ-THROUGH CACHE
 *   - First call: Executes method, stores result in cache
 *   - Subsequent calls with same key: Returns cached result (skips method)
 *   - Use for: Frequently read, infrequently changed data
 *
 * @CachePut(value = "cacheName", key = "#result.id")
 *   - WRITE-THROUGH CACHE
 *   - ALWAYS executes the method, then updates the cache
 *   - Use for: Write operations where you want the cache updated immediately
 *   - Different from @Cacheable: @CachePut always runs the method
 *
 * @CacheEvict(value = "cacheName", key = "#id")
 *   - CACHE INVALIDATION
 *   - Removes specific entry (or all entries) from the cache
 *   - Use for: After updates/deletes that make cached data stale
 *   - allEntries = true: Nuclear option, clears entire cache region
 *
 * @Caching(cacheable = {...}, put = {...}, evict = {...})
 *   - COMBINED OPERATIONS
 *   - Apply multiple cache operations to a single method
 *   - Use for: Methods that affect multiple cache regions
 *
 * ============================================================
 * CACHE KEY STRATEGIES
 * ============================================================
 *
 * SpEL (Spring Expression Language) for key definitions:
 * - #id                    : Method parameter named 'id'
 * - #user.id               : Property of a method parameter
 * - #result.id             : Property of the return value
 * - #root.method.name      : The method name
 * - #root.target.class.name: The class name
 * - T(String).valueOf(#id) : Static method call
 *
 * ============================================================
 * CACHING PATTERNS
 * ============================================================
 *
 * 1. CACHE-ASIDE (Lazy Loading):
 *    App checks cache -> miss? -> load from DB -> put in cache
 *    Spring's @Cacheable implements this automatically.
 *
 * 2. WRITE-THROUGH:
 *    App writes to cache AND DB simultaneously.
 *    Spring's @CachePut implements this.
 *
 * 3. WRITE-BEHIND (Write-Back):
 *    App writes to cache, async process writes to DB later.
 *    NOT built into Spring Cache (requires custom implementation).
 *
 * 4. REFRESH-AHEAD:
 *    Proactively refresh cache entries before they expire.
 *    Implemented with scheduled tasks + @CachePut.
 *
 * ============================================================
 * CACHE PROVIDERS (in order of complexity)
 * ============================================================
 *
 * 1. ConcurrentMapCacheManager (used in this project)
 *    - Simple HashMap-based cache
 *    - No expiration, no size limits
 *    - Good for: Development, testing, simple applications
 *    - Bad for: Production (memory leaks from unbounded cache)
 *
 * 2. Caffeine
 *    - High-performance, near-optimal cache
 *    - Configurable: max size, TTL, TTI, stats
 *    - Good for: Production single-instance applications
 *    - Config: spring.cache.caffeine.spec=maximumSize=500,expireAfterWrite=10m
 *
 * 3. Redis
 *    - Distributed, external cache server
 *    - Shared across all application instances
 *    - Supports expiration, pub/sub, persistence
 *    - Good for: Microservices, clustered deployments
 *    - Config: spring.cache.type=redis + spring.redis.host=localhost
 *
 * 4. EhCache / Hazelcast / Infinispan
 *    - Enterprise-grade caching solutions
 *    - Various features: replication, persistence, management
 *
 * ============================================================
 * COMMON PITFALLS
 * ============================================================
 *
 * 1. SELF-INVOCATION (same as @Transactional):
 *    @Cacheable only works through the proxy. Calling a cached
 *    method from within the same class bypasses the cache!
 *
 * 2. CACHING NULL VALUES:
 *    By default, null results are cached. This can mask newly
 *    created entities. Set allowNullValues = false in CacheConfig.
 *
 * 3. CACHE STAMPEDE (Thundering Herd):
 *    When cache expires, many threads simultaneously miss and
 *    all hit the database. Solution: Use sync = true on @Cacheable
 *    to only allow one thread to populate the cache.
 *
 * 4. STALE DATA:
 *    Cached data may be outdated if another service/process modifies
 *    the database directly. Solution: TTL (time-to-live) or
 *    event-driven cache invalidation.
 */
@Component
@Order(5)
public class CacheProcessor implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CacheProcessor.class);

    private final UserService userService;
    private final DealService dealService;
    private final CommissionService commissionService;
    private final CacheManager cacheManager;

    public CacheProcessor(UserService userService,
                          DealService dealService,
                          CommissionService commissionService,
                          CacheManager cacheManager) {
        this.userService = userService;
        this.dealService = dealService;
        this.commissionService = commissionService;
        this.cacheManager = cacheManager;
    }

    @Override
    public void run(String... args) {
        log.info("============================================================");
        log.info("CACHE PROCESSOR: Spring Cache Strategies Demo");
        log.info("============================================================");

        demonstrateCacheableAnnotation();
        demonstrateCacheEviction();
        demonstrateCacheManager();

        log.info("============================================================");
        log.info("CACHE PROCESSOR: Complete");
        log.info("============================================================");
    }

    private void demonstrateCacheableAnnotation() {
        log.info("");
        log.info("--- @Cacheable: Read-Through Caching ---");
        log.info("First call = cache MISS (hits database). Second call = cache HIT (skips database).");

        // First call: Cache MISS - will log "Cache MISS - Loading user from database"
        log.info("Call 1 (should be MISS):");
        Optional<User> user1 = userService.findById("usr-001");
        user1.ifPresent(u -> log.info("  Result: {}", u.getFullName()));

        // Second call: Cache HIT - the log message in findById will NOT appear
        log.info("Call 2 (should be HIT - no 'Cache MISS' log):");
        Optional<User> user2 = userService.findById("usr-001");
        user2.ifPresent(u -> log.info("  Result: {} (from cache!)", u.getFullName()));

        // Different key: Cache MISS again
        log.info("Call 3 with different ID (should be MISS):");
        Optional<User> user3 = userService.findById("usr-002");
        user3.ifPresent(u -> log.info("  Result: {}", u.getFullName()));

        // Same key as call 3: Cache HIT
        log.info("Call 4 same as call 3 (should be HIT):");
        Optional<User> user4 = userService.findById("usr-002");
        user4.ifPresent(u -> log.info("  Result: {} (from cache!)", u.getFullName()));

        log.info("");
        log.info("--- Caching Deals ---");
        log.info("Call 1 (MISS):");
        dealService.findById("deal-001");
        log.info("Call 2 (HIT - no database query):");
        dealService.findById("deal-001");

        log.info("");
        log.info("--- Caching Commission Plans ---");
        log.info("Plans change infrequently, making them ideal for caching.");
        log.info("Call 1 (MISS):");
        commissionService.findPlanById("plan-001");
        log.info("Call 2 (HIT):");
        commissionService.findPlanById("plan-001");
    }

    private void demonstrateCacheEviction() {
        log.info("");
        log.info("--- @CacheEvict: Cache Invalidation ---");
        log.info("When data is updated, the cached entry must be evicted.");

        // Load user into cache
        log.info("Loading user into cache (MISS):");
        userService.findById("usr-003");

        // Verify it's cached (HIT)
        log.info("Verifying cache (HIT - no log):");
        userService.findById("usr-003");

        // Clear the cache
        log.info("Clearing user cache with @CacheEvict(allEntries=true)...");
        userService.clearUserCache();

        // Cache miss again after eviction
        log.info("After eviction (MISS again):");
        userService.findById("usr-003");
    }

    private void demonstrateCacheManager() {
        log.info("");
        log.info("--- CacheManager: Inspecting Cache State ---");
        log.info("The CacheManager provides programmatic access to cache metadata.");

        log.info("Cache provider: {}", cacheManager.getClass().getSimpleName());
        log.info("Available caches: {}", cacheManager.getCacheNames());

        cacheManager.getCacheNames().forEach(name -> {
            var cache = cacheManager.getCache(name);
            if (cache != null) {
                log.info("  Cache '{}': type={}", name, cache.getClass().getSimpleName());
            }
        });
    }
}
