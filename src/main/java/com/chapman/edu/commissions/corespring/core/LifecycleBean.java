package com.chapman.edu.commissions.corespring.core;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * Demonstrates the complete Spring Bean Lifecycle with all callback methods.
 *
 * FULL BEAN LIFECYCLE ORDER:
 * 1. Constructor
 * 2. Setter methods (dependency injection)
 * 3. BeanPostProcessor.postProcessBeforeInitialization()
 * 4. @PostConstruct
 * 5. InitializingBean.afterPropertiesSet()
 * 6. Custom init-method (if specified in @Bean or XML)
 * 7. BeanPostProcessor.postProcessAfterInitialization()
 * 8. Bean is ready to use
 * ... application runs ...
 * 9. @PreDestroy
 * 10. DisposableBean.destroy()
 * 11. Custom destroy-method (if specified)
 *
 * BEST PRACTICES:
 * - Use @PostConstruct/@PreDestroy (JSR-250) - most portable and clear
 * - Avoid implementing InitializingBean/DisposableBean (couples code to Spring)
 * - Custom init/destroy methods are useful for XML configuration
 */
@Component
public class LifecycleBean implements InitializingBean, DisposableBean {

    public LifecycleBean() {
        System.out.println("1. LifecycleBean: Constructor called");
    }

    /**
     * JSR-250 annotation - recommended approach
     * Called after dependency injection, before InitializingBean
     */
    @PostConstruct
    public void postConstruct() {
        System.out.println("4. LifecycleBean: @PostConstruct called");
    }

    /**
     * InitializingBean interface method
     * Called after @PostConstruct
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("5. LifecycleBean: InitializingBean.afterPropertiesSet() called");
    }

    /**
     * Custom init method (would be specified in @Bean(initMethod="customInit"))
     * Called after afterPropertiesSet()
     */
    public void customInit() {
        System.out.println("6. LifecycleBean: Custom init-method called");
    }

    public void doWork() {
        System.out.println("LifecycleBean: Performing business logic");
    }

    /**
     * JSR-250 annotation - recommended approach
     * Called before DisposableBean.destroy()
     */
    @PreDestroy
    public void preDestroy() {
        System.out.println("9. LifecycleBean: @PreDestroy called");
    }

    /**
     * DisposableBean interface method
     * Called after @PreDestroy
     */
    @Override
    public void destroy() throws Exception {
        System.out.println("10. LifecycleBean: DisposableBean.destroy() called");
    }

    /**
     * Custom destroy method (would be specified in @Bean(destroyMethod="customDestroy"))
     * Called after DisposableBean.destroy()
     */
    public void customDestroy() {
        System.out.println("11. LifecycleBean: Custom destroy-method called");
    }
}
