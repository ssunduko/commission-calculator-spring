package com.chapman.edu.commissions.corespring.core;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;

/**
 * Demonstrates SINGLETON scope in Spring (default scope).
 * Only one instance exists per Spring container.
 *
 * KEY CONCEPTS:
 * - Singleton is the DEFAULT scope (no @Scope annotation needed)
 * - Only one instance per ApplicationContext
 * - Thread-safety concerns: must be stateless or thread-safe
 * - Full lifecycle management by Spring (init and destroy callbacks invoked)
 */
@Component
public class SingletonBean {

    private final LocalDateTime createdAt;
    private int requestCount = 0;  // WARNING: shared state in singleton!

    public SingletonBean() {
        this.createdAt = LocalDateTime.now();
        System.out.println("SingletonBean constructor called at " + createdAt);
    }

    /**
     * @PostConstruct: Called after dependency injection is complete
     * Order: Constructor -> Dependency Injection -> @PostConstruct
     */
    @PostConstruct
    public void init() {
        System.out.println("SingletonBean @PostConstruct - Initialization complete");
    }

    /**
     * @PreDestroy: Called before bean is removed from container
     * Invoked during application shutdown for singleton beans
     */
    @PreDestroy
    public void cleanup() {
        System.out.println("SingletonBean @PreDestroy - Cleanup before destruction");
        System.out.println("Total requests processed: " + requestCount);
    }

    /**
     * WARNING: This method modifies shared state!
     * In production, this would need synchronization for thread-safety
     */
    public void incrementRequestCount() {
        requestCount++;  // Thread-safety issue in real applications!
    }

    public int getRequestCount() {
        return requestCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
