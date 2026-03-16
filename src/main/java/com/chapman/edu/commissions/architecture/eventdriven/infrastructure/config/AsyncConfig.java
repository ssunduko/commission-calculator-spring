package com.chapman.edu.commissions.architecture.eventdriven.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * CONCEPT: Asynchronous Event Processing
 *
 * In Event-Driven Architecture, events can be processed synchronously
 * or asynchronously. @EnableAsync activates Spring's async support,
 * allowing @Async-annotated event listeners to run in separate threads.
 *
 * Benefits of async event processing:
 * - Non-blocking: The publisher doesn't wait for listeners to finish
 * - Decoupling: Publishers and listeners operate independently
 * - Scalability: Event processing can be parallelized
 *
 * Trade-offs:
 * - Eventual consistency: State may not be immediately updated
 * - Error handling: Failures in async listeners need dedicated handling
 * - Ordering: Events may be processed out of order
 *
 * In this module, the EventStoreListener runs synchronously (to guarantee
 * persistence), while notification-style listeners use @Async.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);
}
