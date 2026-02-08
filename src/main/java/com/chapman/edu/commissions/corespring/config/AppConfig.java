package com.chapman.edu.commissions.corespring.config;

import com.chapman.edu.commissions.corespring.core.LifecycleBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;

import java.math.BigDecimal;

/**
 * Java-based Spring Configuration
 *
 * KEY ANNOTATIONS:
 * - @Configuration: Indicates this class contains @Bean definitions
 * - @ComponentScan: Enables component scanning for @Component, @Service, @Repository
 * - @EnableAspectJAutoProxy: Enables AspectJ-based AOP support
 * - @PropertySource: Loads properties from external file
 *
 * CONFIGURATION STRATEGIES:
 * 1. XML Configuration (legacy, verbose, external to code)
 * 2. Java Configuration (type-safe, refactorable, this class!)
 * 3. Annotation-based (@Component, @Service, etc.)
 * 4. Mixing strategies (common in real applications)
 *
 * BEST PRACTICE: Use Java configuration for infrastructure beans,
 * annotations for business components, XML for legacy integration
 */
@Configuration
@ComponentScan(basePackages = "com.chapman.edu.commissions.corespring")
@EnableAspectJAutoProxy  // Enables AOP support
@PropertySource("classpath:application.properties")  // Load properties
public class AppConfig {

    /**
     * @Value annotation - inject properties from property files or environment
     * Supports SpEL (Spring Expression Language)
     */
    @Value("${commission.default.rate:0.10}")  // Default: 10% if property not found
    private BigDecimal defaultCommissionRate;

    @Value("${commission.bonus.threshold:10000}")
    private BigDecimal bonusThreshold;

    @Value("${app.name:Commission Calculator}")
    private String applicationName;

    /**
     * @Bean method - explicitly declares a bean
     * Method name = bean name (unless overridden with @Bean("customName"))
     * Return type = bean type
     *
     * Used when:
     * - Creating beans from third-party classes (can't add @Component)
     * - Complex bean creation logic
     * - Need to call methods during creation
     */
    @Bean
    public ConfigurationProperties configurationProperties() {
        ConfigurationProperties props = new ConfigurationProperties();
        props.setDefaultCommissionRate(defaultCommissionRate);
        props.setBonusThreshold(bonusThreshold);
        props.setApplicationName(applicationName);
        return props;
    }

    /**
     * Bean with custom initialization and destruction methods
     * Alternative to @PostConstruct/@PreDestroy for beans created with @Bean
     */
    @Bean(initMethod = "customInit", destroyMethod = "customDestroy")
    public LifecycleBean lifecycleBeanWithCustomMethods() {
        return new LifecycleBean();
    }

    /**
     * Bean with dependencies injected via method parameters
     * Spring automatically resolves and injects the dependencies
     */
    @Bean
    public DependencyExample dependencyExample(ConfigurationProperties config) {
        System.out.println("Creating DependencyExample bean with injected ConfigurationProperties");
        return new DependencyExample(config);
    }

    /**
     * Inner class to demonstrate @Bean with dependencies
     */
    public static class DependencyExample {
        private final ConfigurationProperties config;

        public DependencyExample(ConfigurationProperties config) {
            this.config = config;
        }

        public void printConfiguration() {
            System.out.println("Application: " + config.getApplicationName());
            System.out.println("Default Rate: " + config.getDefaultCommissionRate());
            System.out.println("Bonus Threshold: " + config.getBonusThreshold());
        }
    }
}
