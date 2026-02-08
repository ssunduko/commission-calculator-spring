package com.chapman.edu.commissions.corespring.demo;

import com.chapman.edu.commissions.corespring.core.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Runnable demo showing Bean Lifecycle and Scopes.
 *
 * Run this class to see:
 * - Singleton vs Prototype scope
 * - Complete bean lifecycle (constructor → @PostConstruct → @PreDestroy → destroy)
 * - BeanPostProcessor in action
 * - Request scope (if running in web context)
 *
 * To run: Uncomment @Component annotation and start the application
 */
//@Component  // Uncomment to run this demo
public class BeanLifecycleDemo implements CommandLineRunner {

    private final ApplicationContext context;
    private final SingletonBean singletonBean;
    private final LifecycleBean lifecycleBean;

    public BeanLifecycleDemo(ApplicationContext context,
                            SingletonBean singletonBean,
                            LifecycleBean lifecycleBean) {
        this.context = context;
        this.singletonBean = singletonBean;
        this.lifecycleBean = lifecycleBean;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n========================================");
        System.out.println("BEAN LIFECYCLE & SCOPES DEMO");
        System.out.println("========================================\n");

        demonstrateSingletonScope();
        demonstratePrototypeScope();
        demonstrateLifecycle();
        demonstrateBeanPostProcessor();

        System.out.println("Note: Watch the console at application shutdown to see:");
        System.out.println("- @PreDestroy methods being called");
        System.out.println("- Singleton beans being destroyed");
        System.out.println("- Prototype beans NOT being destroyed by Spring");
        System.out.println();
    }

    private void demonstrateSingletonScope() {
        System.out.println("--- Singleton Scope (Default) ---");

        // Request bean multiple times
        SingletonBean bean1 = context.getBean(SingletonBean.class);
        SingletonBean bean2 = context.getBean(SingletonBean.class);
        SingletonBean bean3 = singletonBean;  // Injected in constructor

        System.out.println("Requesting SingletonBean three times:");
        System.out.println("Bean 1 created at: " + bean1.getCreatedAt());
        System.out.println("Bean 2 created at: " + bean2.getCreatedAt());
        System.out.println("Bean 3 created at: " + bean3.getCreatedAt());

        System.out.println("\nAll three references point to SAME instance:");
        System.out.println("bean1 == bean2: " + (bean1 == bean2));
        System.out.println("bean2 == bean3: " + (bean2 == bean3));

        // Demonstrate shared state
        bean1.incrementRequestCount();
        bean2.incrementRequestCount();
        bean3.incrementRequestCount();

        System.out.println("\nShared state - request count: " + bean1.getRequestCount());
        System.out.println("⚠️  WARNING: Shared mutable state in singletons requires thread-safety!");
        System.out.println();
    }

    private void demonstratePrototypeScope() {
        System.out.println("--- Prototype Scope ---");

        // Each request creates NEW instance
        PrototypeBean proto1 = context.getBean(PrototypeBean.class);
        PrototypeBean proto2 = context.getBean(PrototypeBean.class);
        PrototypeBean proto3 = context.getBean(PrototypeBean.class);

        System.out.println("Requesting PrototypeBean three times:");
        System.out.println("Proto 1 ID: " + proto1.getInstanceId() + ", Created: " + proto1.getCreatedAt());
        System.out.println("Proto 2 ID: " + proto2.getInstanceId() + ", Created: " + proto2.getCreatedAt());
        System.out.println("Proto 3 ID: " + proto3.getInstanceId() + ", Created: " + proto3.getCreatedAt());

        System.out.println("\nAll three are DIFFERENT instances:");
        System.out.println("proto1 == proto2: " + (proto1 == proto2));
        System.out.println("proto2 == proto3: " + (proto2 == proto3));

        // Each has independent state
        proto1.performOperation();
        proto1.performOperation();
        proto2.performOperation();

        System.out.println("\nIndependent state:");
        System.out.println("Proto 1 operation count: " + proto1.getOperationCount());
        System.out.println("Proto 2 operation count: " + proto2.getOperationCount());
        System.out.println("Proto 3 operation count: " + proto3.getOperationCount());

        System.out.println("\n⚠️  IMPORTANT: @PreDestroy will NOT be called for prototype beans!");
        System.out.println("    Client is responsible for cleanup.");
        System.out.println();
    }

    private void demonstrateLifecycle() {
        System.out.println("--- Complete Bean Lifecycle ---");
        System.out.println("LifecycleBean demonstrates all lifecycle callbacks:");
        System.out.println("1. Constructor");
        System.out.println("2. Dependency Injection");
        System.out.println("3. BeanPostProcessor.postProcessBeforeInitialization()");
        System.out.println("4. @PostConstruct");
        System.out.println("5. InitializingBean.afterPropertiesSet()");
        System.out.println("6. Custom init-method");
        System.out.println("7. BeanPostProcessor.postProcessAfterInitialization()");
        System.out.println("8. Bean is ready to use");
        System.out.println();

        System.out.println("Calling business method:");
        lifecycleBean.doWork();

        System.out.println("\nAt shutdown (watch console):");
        System.out.println("9. @PreDestroy");
        System.out.println("10. DisposableBean.destroy()");
        System.out.println("11. Custom destroy-method");
        System.out.println();
    }

    private void demonstrateBeanPostProcessor() {
        System.out.println("--- BeanPostProcessor ---");
        System.out.println("CustomBeanPostProcessor is applied to ALL beans in the context.");
        System.out.println("Check the console output during startup to see:");
        System.out.println("- 'BeanPostProcessor BEFORE init' messages");
        System.out.println("- 'BeanPostProcessor AFTER init' messages");
        System.out.println();
        System.out.println("This is how Spring implements:");
        System.out.println("- @Autowired annotation processing");
        System.out.println("- @Value injection");
        System.out.println("- AOP proxy creation (in postProcessAfterInitialization)");
        System.out.println("- Transaction proxy creation");
        System.out.println();
    }
}
