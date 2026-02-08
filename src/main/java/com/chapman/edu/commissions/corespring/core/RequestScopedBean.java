package com.chapman.edu.commissions.corespring.core;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Demonstrates REQUEST scope in Spring Web applications.
 * One instance per HTTP request.
 *
 * KEY CONCEPTS:
 * - Web-specific scope (requires web context)
 * - New instance created for each HTTP request
 * - Destroyed at the end of request processing
 * - Useful for holding request-specific state
 * - proxyMode = TARGET_CLASS creates a CGLIB proxy to inject into singleton beans
 */
@Component
@ConditionalOnWebApplication  // Only create this bean in web applications
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RequestScopedBean {

    private final String requestId;
    private final LocalDateTime requestTime;
    private String userId;

    public RequestScopedBean() {
        this.requestId = UUID.randomUUID().toString();
        this.requestTime = LocalDateTime.now();
        System.out.println("RequestScopedBean created for request: " + requestId);
    }

    @PostConstruct
    public void init() {
        System.out.println("RequestScopedBean @PostConstruct - Request ID: " + requestId);
    }

    @PreDestroy
    public void destroy() {
        System.out.println("RequestScopedBean @PreDestroy - Request ID: " + requestId +
                         " completed at " + LocalDateTime.now());
    }

    public String getRequestId() {
        return requestId;
    }

    public LocalDateTime getRequestTime() {
        return requestTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
