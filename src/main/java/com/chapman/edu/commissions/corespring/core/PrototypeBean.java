package com.chapman.edu.commissions.corespring.core;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Demonstrates PROTOTYPE scope in Spring.
 * A new instance is created each time the bean is requested.
 *
 * KEY CONCEPTS:
 * - @Scope annotation to specify bean scope
 * - Prototype beans are NOT managed for complete lifecycle (no @PreDestroy)
 * - Each request gets a new instance
 * - Useful for stateful beans or beans that should not be shared
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PrototypeBean {

    private final String instanceId;
    private final LocalDateTime createdAt;
    private int operationCount = 0;

    public PrototypeBean() {
        this.instanceId = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        System.out.println("PrototypeBean constructor called - Instance ID: " + instanceId);
    }

    @PostConstruct
    public void init() {
        System.out.println("PrototypeBean @PostConstruct - Instance ID: " + instanceId);
    }

    /**
     * WARNING: @PreDestroy is NOT called for prototype beans!
     * Spring does not manage the complete lifecycle of prototype beans.
     * The client code is responsible for cleanup.
     */
    @PreDestroy
    public void destroy() {
        System.out.println("PrototypeBean @PreDestroy - Instance ID: " + instanceId +
                         " (THIS WON'T BE CALLED - prototype beans are not destroyed by Spring)");
    }

    public void performOperation() {
        operationCount++;
        System.out.println("Operation performed on instance " + instanceId +
                         " (Count: " + operationCount + ")");
    }

    public String getInstanceId() {
        return instanceId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public int getOperationCount() {
        return operationCount;
    }
}
