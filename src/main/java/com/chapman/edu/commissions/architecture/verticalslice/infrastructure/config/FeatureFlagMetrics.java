package com.chapman.edu.commissions.architecture.verticalslice.infrastructure.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.togglz.core.Feature;
import org.togglz.core.manager.FeatureManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Feature Flag Observability — exposes Togglz state as Micrometer metrics and structured logs.
 *
 * Metrics exposed:
 *   feature_flag_state{feature="CURRENCY_CONVERSION"}  — gauge: 1=enabled, 0=disabled
 *   feature_flag_checked_total{feature="..."}           — counter: number of flag checks
 *
 * Logs:
 *   Periodic state summary every 60s at INFO level
 *   State change detection and logging
 */
@Component
public class FeatureFlagMetrics {

    private static final Logger log = LoggerFactory.getLogger(FeatureFlagMetrics.class);

    private final FeatureManager featureManager;
    private final MeterRegistry meterRegistry;
    private final Map<String, AtomicInteger> stateGauges = new ConcurrentHashMap<>();
    private final Map<String, Boolean> previousStates = new ConcurrentHashMap<>();
    private final Map<String, Counter> checkCounters = new ConcurrentHashMap<>();

    public FeatureFlagMetrics(FeatureManager featureManager, MeterRegistry meterRegistry) {
        this.featureManager = featureManager;
        this.meterRegistry = meterRegistry;
        registerMetrics();
    }

    private void registerMetrics() {
        for (Feature feature : featureManager.getFeatures()) {
            String name = feature.name();

            // Gauge: current state (1=enabled, 0=disabled)
            AtomicInteger stateValue = new AtomicInteger(featureManager.isActive(feature) ? 1 : 0);
            stateGauges.put(name, stateValue);
            Gauge.builder("feature_flag_state", stateValue, AtomicInteger::get)
                    .tag("feature", name)
                    .description("Current state of feature flag (1=enabled, 0=disabled)")
                    .register(meterRegistry);

            // Counter: total checks
            Counter counter = Counter.builder("feature_flag_checked_total")
                    .tag("feature", name)
                    .description("Total number of times this feature flag was checked")
                    .register(meterRegistry);
            checkCounters.put(name, counter);

            // Track initial state
            previousStates.put(name, featureManager.isActive(feature));
        }

        log.info("[FeatureFlags] Registered metrics for {} feature flags", stateGauges.size());
    }

    /**
     * Record that a feature flag was checked (call from controllers/services).
     */
    public void recordCheck(Feature feature) {
        Counter counter = checkCounters.get(feature.name());
        if (counter != null) {
            counter.increment();
        }
    }

    /**
     * Periodically refresh gauge values and detect state changes.
     */
    @Scheduled(fixedRate = 10000)
    public void refreshMetrics() {
        for (Feature feature : featureManager.getFeatures()) {
            String name = feature.name();
            boolean currentState = featureManager.isActive(feature);

            // Update gauge
            AtomicInteger gauge = stateGauges.get(name);
            if (gauge != null) {
                gauge.set(currentState ? 1 : 0);
            }

            // Detect and log state changes
            Boolean previous = previousStates.get(name);
            if (previous != null && previous != currentState) {
                log.warn("[FeatureFlags] FLAG CHANGED: {} {} → {}",
                        name,
                        previous ? "ENABLED" : "DISABLED",
                        currentState ? "ENABLED" : "DISABLED");
            }
            previousStates.put(name, currentState);
        }
    }

    /**
     * Log a summary of all feature flag states every 60 seconds.
     */
    @Scheduled(fixedRate = 60000, initialDelay = 5000)
    public void logStateSummary() {
        StringBuilder sb = new StringBuilder("[FeatureFlags] Current state:");
        for (Feature feature : featureManager.getFeatures()) {
            boolean active = featureManager.isActive(feature);
            sb.append(String.format(" %s=%s", feature.name(), active ? "ON" : "OFF"));
        }
        log.info(sb.toString());
    }
}
