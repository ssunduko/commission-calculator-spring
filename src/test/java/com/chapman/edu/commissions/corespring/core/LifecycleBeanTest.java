package com.chapman.edu.commissions.corespring.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Tests demonstrating the Spring Bean Lifecycle.
 *
 * KEY CONCEPTS DEMONSTRATED:
 * - @PostConstruct and @PreDestroy callbacks
 * - InitializingBean and DisposableBean interfaces
 * - Custom init/destroy methods via @Bean annotation
 * - The order in which lifecycle callbacks are invoked
 *
 * We use AnnotationConfigApplicationContext to manually control
 * the container lifecycle (start and close) so we can verify
 * that destroy callbacks are invoked on shutdown.
 */
@DisplayName("Bean Lifecycle — Tests")
class LifecycleBeanTest {

    /**
     * Minimal config for this test — creates only the LifecycleBean.
     */
    @Configuration
    static class TestConfig {
        @Bean
        public LifecycleBean lifecycleBean() {
            return new LifecycleBean();
        }
    }

    @Test
    @DisplayName("LifecycleBean should be created and initialized without errors")
    void lifecycleBean_shouldInitializeSuccessfully() {
        // Creating the context triggers: Constructor -> @PostConstruct -> afterPropertiesSet
        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(TestConfig.class);

        LifecycleBean bean = context.getBean(LifecycleBean.class);

        // Bean should exist and be usable
        assertThat(bean).isNotNull();
        assertThatCode(bean::doWork).doesNotThrowAnyException();

        context.close(); // Triggers @PreDestroy -> destroy()
    }

    @Test
    @DisplayName("Closing the context should trigger destroy callbacks")
    void lifecycleBean_closingContext_shouldTriggerDestroyCallbacks() {
        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(TestConfig.class);

        LifecycleBean bean = context.getBean(LifecycleBean.class);
        assertThat(bean).isNotNull();

        // close() triggers @PreDestroy and DisposableBean.destroy()
        // If either throws, this test will fail
        assertThatCode(context::close).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("LifecycleBean should implement InitializingBean")
    void lifecycleBean_shouldImplementInitializingBean() {
        LifecycleBean bean = new LifecycleBean();

        // Verify the bean implements the interface
        assertThat(bean).isInstanceOf(org.springframework.beans.factory.InitializingBean.class);
    }

    @Test
    @DisplayName("LifecycleBean should implement DisposableBean")
    void lifecycleBean_shouldImplementDisposableBean() {
        LifecycleBean bean = new LifecycleBean();

        // Verify the bean implements the interface
        assertThat(bean).isInstanceOf(org.springframework.beans.factory.DisposableBean.class);
    }
}
