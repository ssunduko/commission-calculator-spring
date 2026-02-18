package com.chapman.edu.commissions.corespring.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests demonstrating Spring Bean Scopes.
 *
 * KEY CONCEPTS DEMONSTRATED:
 * - SINGLETON scope: Same instance returned every time
 * - PROTOTYPE scope: New instance returned each time
 * - Using ApplicationContext.getBean() to verify scope behavior
 *
 * WHY @SpringBootTest?
 * We need the Spring container to be running so we can verify how
 * it manages bean scopes. Pure unit tests can't demonstrate this
 * because scope management is a container feature.
 *
 * @ContextConfiguration limits the context to only the classes we need,
 * avoiding loading the full application context.
 */
@SpringBootTest(classes = {SingletonBean.class, PrototypeBean.class})
@DisplayName("Bean Scope — Integration Tests")
class BeanScopeTest {

    @Autowired
    private ApplicationContext context;

    // ============================================================
    // SINGLETON SCOPE TESTS
    // ============================================================

    @Test
    @DisplayName("Singleton beans should return the same instance")
    void singletonBean_shouldReturnSameInstance() {
        // Request the bean twice from the container
        SingletonBean bean1 = context.getBean(SingletonBean.class);
        SingletonBean bean2 = context.getBean(SingletonBean.class);

        // Both references should point to the SAME object
        assertThat(bean1).isSameAs(bean2);
        assertThat(bean1.getCreatedAt()).isEqualTo(bean2.getCreatedAt());
    }

    @Test
    @DisplayName("Singleton beans should share state")
    void singletonBean_shouldShareState() {
        SingletonBean bean1 = context.getBean(SingletonBean.class);
        SingletonBean bean2 = context.getBean(SingletonBean.class);

        int initialCount = bean1.getRequestCount();
        bean1.incrementRequestCount();

        // bean2 sees the change because it's the SAME instance
        assertThat(bean2.getRequestCount()).isEqualTo(initialCount + 1);
    }

    // ============================================================
    // PROTOTYPE SCOPE TESTS
    // ============================================================

    @Test
    @DisplayName("Prototype beans should return different instances")
    void prototypeBean_shouldReturnDifferentInstances() {
        // Request the bean twice from the container
        PrototypeBean bean1 = context.getBean(PrototypeBean.class);
        PrototypeBean bean2 = context.getBean(PrototypeBean.class);

        // Each request should return a NEW object
        assertThat(bean1).isNotSameAs(bean2);
        assertThat(bean1.getInstanceId()).isNotEqualTo(bean2.getInstanceId());
    }

    @Test
    @DisplayName("Prototype beans should have independent state")
    void prototypeBean_shouldHaveIndependentState() {
        PrototypeBean bean1 = context.getBean(PrototypeBean.class);
        PrototypeBean bean2 = context.getBean(PrototypeBean.class);

        bean1.performOperation();
        bean1.performOperation();

        // bean2 should NOT be affected by bean1's operations
        assertThat(bean1.getOperationCount()).isEqualTo(2);
        assertThat(bean2.getOperationCount()).isEqualTo(0);
    }
}
