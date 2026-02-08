package com.chapman.edu.commissions.corespring.core;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * Demonstrates BeanPostProcessor - a powerful Spring extension point.
 *
 * BeanPostProcessor allows you to modify bean instances AFTER they are created
 * but BEFORE they are fully initialized.
 *
 * KEY CONCEPTS:
 * - postProcessBeforeInitialization: called BEFORE @PostConstruct and init methods
 * - postProcessAfterInitialization: called AFTER all initialization
 * - Applied to ALL beans in the ApplicationContext
 * - Used internally by Spring for @Autowired, @Value, @PostConstruct, etc.
 * - Can wrap beans in proxies (used for AOP, transactions, etc.)
 *
 * LIFECYCLE ORDER:
 * 1. Constructor
 * 2. Dependency Injection
 * 3. BeanPostProcessor.postProcessBeforeInitialization() <-- THIS
 * 4. @PostConstruct / InitializingBean.afterPropertiesSet()
 * 5. Custom init-method
 * 6. BeanPostProcessor.postProcessAfterInitialization() <-- THIS
 * 7. Bean is ready to use
 */
@Component
public class CustomBeanPostProcessor implements BeanPostProcessor {

    /**
     * Called BEFORE initialization callbacks (@PostConstruct, afterPropertiesSet, init-method)
     *
     * @param bean the bean instance
     * @param beanName the name of the bean
     * @return the bean to use (can return original bean or a wrapped/modified version)
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        // Only log for our custom beans to reduce noise
        if (beanName.contains("corespring")) {
            System.out.println("BeanPostProcessor BEFORE init: " + beanName +
                             " [" + bean.getClass().getSimpleName() + "]");
        }
        return bean;  // Return original bean (or could return a proxy/wrapper)
    }

    /**
     * Called AFTER all initialization is complete
     * This is where proxies are typically created (AOP, transactions, etc.)
     *
     * @param bean the bean instance (fully initialized)
     * @param beanName the name of the bean
     * @return the bean to use (often a proxy wrapping the original bean)
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (beanName.contains("corespring")) {
            System.out.println("BeanPostProcessor AFTER init: " + beanName +
                             " [" + bean.getClass().getSimpleName() + "] - Ready for use");
        }

        // This is where you could wrap the bean in a proxy:
        // return Proxy.newProxyInstance(...) or use CGLIB
        return bean;
    }
}
