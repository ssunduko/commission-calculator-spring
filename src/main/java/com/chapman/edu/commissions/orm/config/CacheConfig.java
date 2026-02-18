package com.chapman.edu.commissions.orm.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cache configuration for the ORM module.
 *
 * ============================================================
 * SPRING CACHE ABSTRACTION
 * ============================================================
 *
 * WHAT IS CACHING?
 * Caching stores the results of expensive operations (database queries,
 * API calls, complex calculations) so that subsequent requests for the same
 * data can be served from memory instead of re-executing the operation.
 *
 * WHY CACHE?
 * - Reduce database load (fewer queries)
 * - Improve response times (memory is faster than disk)
 * - Reduce CPU usage (skip complex calculations)
 *
 * SPRING CACHE ABSTRACTION:
 * Spring provides a cache abstraction layer with annotations:
 * - @EnableCaching: Enables annotation-driven caching
 * - @Cacheable: Cache the method's return value
 * - @CacheEvict: Remove entries from cache
 * - @CachePut: Update cache without interfering with method execution
 * - @Caching: Combine multiple cache operations
 *
 * CACHE PROVIDERS (implementations):
 * 1. ConcurrentMapCacheManager (simple, in-memory, used here for education)
 *    - Uses ConcurrentHashMap internally
 *    - No expiration, no size limits
 *    - Suitable for single-instance applications
 *
 * 2. Caffeine (recommended for production single-instance)
 *    - Configurable max size, TTL (time-to-live), TTI (time-to-idle)
 *    - Near-optimal hit rates with TinyLFU eviction policy
 *    - Spring Boot auto-configures it if caffeine is on the classpath
 *
 * 3. Redis (recommended for distributed systems)
 *    - External cache server; shared across application instances
 *    - Supports expiration, pub/sub for cache invalidation
 *    - Required for microservices / multi-instance deployments
 *
 * 4. EhCache, Hazelcast, Infinispan (other options)
 *
 * CACHE NAMES:
 * Each cache name represents a separate cache "region" with its own entries.
 * For example, "commissionPlans" and "deals" are separate caches.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configure the CacheManager bean.
     *
     * ConcurrentMapCacheManager creates a new ConcurrentHashMap for each cache name.
     * We pre-define the cache names used throughout the application.
     *
     * IN PRODUCTION, you would replace this with:
     *
     * @Bean
     * public CacheManager cacheManager() {
     *     CaffeineCacheManager manager = new CaffeineCacheManager();
     *     manager.setCaffeine(Caffeine.newBuilder()
     *         .maximumSize(500)           // Max 500 entries
     *         .expireAfterWrite(10, TimeUnit.MINUTES)  // TTL: 10 minutes
     *         .recordStats());            // Enable statistics
     *     return manager;
     * }
     *
     * OR for Redis:
     *
     * @Bean
     * public CacheManager cacheManager(RedisConnectionFactory factory) {
     *     RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
     *         .entryTtl(Duration.ofMinutes(10))
     *         .disableCachingNullValues();
     *     return RedisCacheManager.builder(factory)
     *         .cacheDefaults(config)
     *         .build();
     * }
     */
    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager(
                "commissionPlans",  // Cache for commission plans
                "deals",            // Cache for deals
                "users",            // Cache for users
                "calculations"      // Cache for commission calculations
        );

        // allowNullValues: Whether to cache null results.
        // Setting to false prevents caching "not found" results,
        // which could mask newly created entities.
        cacheManager.setAllowNullValues(false);

        return cacheManager;
    }
}
