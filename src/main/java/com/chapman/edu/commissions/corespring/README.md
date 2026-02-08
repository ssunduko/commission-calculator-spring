# Spring Framework Core Concepts
## Software Development Lifecycle Course - Lecture Notes

**Topic:** Dependency Injection and Inversion of Control (IoC)
**Application Domain:** Commission Calculator System

---

## Table of Contents
1. [Introduction to DI & IoC Concepts](#1-introduction-to-di--ioc-concepts)
2. [Spring Core Container](#2-spring-core-container)
3. [Application Context & Configuration](#3-application-context--configuration)
4. [AOP Fundamentals](#4-aop-fundamentals)
5. [Annotations & Component Scanning](#5-annotations--component-scanning)
6. [Practical Examples & Best Practices](#6-practical-examples--best-practices)

---

## 1. Introduction to DI & IoC Concepts

### 1.1 The Problem: Tight Coupling

**Without Dependency Injection:**

```java
public class CommissionService {
    private EmailService emailService;  // Concrete dependency

    public CommissionService() {
        this.emailService = new EmailService();  // Tight coupling!
    }

    public void processCommission(Deal deal) {
        // ... calculate commission
        emailService.send("Commission calculated");
    }
}
```

**Problems:**
- Hard to test (can't mock EmailService)
- Hard to change implementation
- Violates Single Responsibility (creates its own dependencies)
- Hidden dependencies (not visible in constructor/interface)

### 1.2 Core Definitions

**Inversion of Control (IoC):**
- A design principle where control of object creation and lifecycle is inverted
- Instead of objects creating their dependencies, dependencies are provided to them
- Framework controls the flow, not the application

**Dependency Injection (DI):**
- A pattern implementing IoC
- Dependencies are "injected" into objects rather than created by them
- Three types: Constructor, Setter, and Field injection

**Dependency Inversion Principle (DIP):**
- High-level modules should not depend on low-level modules; both should depend on abstractions
- Part of SOLID principles
- Different from DI (DIP is design principle, DI is implementation pattern)

```java
// DIP in action: depend on abstractions
public class CommissionService {
    private NotificationService notificationService;  // Interface, not concrete class!

    // DI in action: dependencies injected via constructor
    public CommissionService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
}
```

### 1.3 Three Types of Dependency Injection

#### Constructor Injection (RECOMMENDED)

**Example:** `src/main/java/com/chapman/edu/commissions/corespring/di/CommissionCalculationService.java:40-48`

```java
@Service
public class CommissionCalculationService {
    private final CommissionRuleEngine ruleEngine;
    private final NotificationService notificationService;

    @Autowired  // Optional since Spring 4.3
    public CommissionCalculationService(
            CommissionRuleEngine ruleEngine,
            @Qualifier("defaultNotificationService") NotificationService notificationService) {
        this.ruleEngine = ruleEngine;
        this.notificationService = notificationService;
    }
}
```

**Advantages:**
- ✅ Immutable dependencies (final fields)
- ✅ Required dependencies are explicit
- ✅ Easy to test (pass mocks in constructor)
- ✅ Thread-safe
- ✅ Prevents partially constructed objects

**Disadvantages:**
- ❌ Constructor can get large with many dependencies (code smell - consider refactoring)

#### Setter Injection

**Example:** `src/main/java/com/chapman/edu/commissions/corespring/di/CommissionCalculationService.java:57-61`

```java
private ValidationService validationService;

@Autowired(required = false)  // Optional dependency
public void setValidationService(ValidationService validationService) {
    this.validationService = validationService;
}
```

**Advantages:**
- ✅ Good for optional dependencies
- ✅ Allows reconfiguration after construction
- ✅ Supports circular dependencies (though they should be avoided)

**Disadvantages:**
- ❌ Cannot use final fields (mutability issues)
- ❌ Can create partially constructed objects
- ❌ Dependencies not obvious

**Use Case:** Optional dependencies with sensible defaults

#### Field Injection (NOT RECOMMENDED)

**Example:** `src/main/java/com/chapman/edu/commissions/corespring/di/CommissionCalculationService.java:28-31`

```java
@Autowired
@Qualifier("emailAuditLogger")
private AuditLogger auditLogger;  // Injected directly into field
```

**Advantages:**
- ✅ Less boilerplate code
- ✅ Quick to write

**Disadvantages:**
- ❌ Cannot use final fields
- ❌ Hidden dependencies (not in constructor signature)
- ❌ Hard to test (requires reflection or Spring context)
- ❌ Cannot inject dependencies in unit tests without Spring
- ❌ Violates encapsulation

**Verdict:** Avoid in production code; acceptable only for quick prototypes

### 1.4 Benefits of Dependency Injection

1. **Loose Coupling:** Classes depend on abstractions, not concrete implementations
2. **Testability:** Easy to inject mocks and stubs for testing
3. **Maintainability:** Change implementations without modifying clients
4. **Single Responsibility:** Objects don't create their dependencies
5. **Flexibility:** Easy to switch implementations (dev vs prod, email vs SMS)
6. **Configuration Externalization:** Wire dependencies through configuration

### 1.5 Anti-Patterns to Avoid

**1. Service Locator Pattern (Don't use in Spring apps):**
```java
// ANTI-PATTERN - Don't do this!
public class CommissionService {
    public void calculate() {
        NotificationService ns = ServiceLocator.get(NotificationService.class);
        ns.notify();
    }
}
```

**2. Static Factories (Avoid when using DI):**
```java
// ANTI-PATTERN - Don't do this!
EmailService.getInstance().send();  // Global state, hard to test
```

**3. new Keyword for Dependencies:**
```java
// ANTI-PATTERN - Don't do this!
this.emailService = new EmailService();  // Tight coupling
```

**4. Circular Dependencies:**
```java
// ANTI-PATTERN - Don't do this!
class A { @Autowired B b; }
class B { @Autowired A a; }  // Circular dependency - bad design!
```

---

## 2. Spring Core Container

### 2.1 Bean Scopes

Spring supports several bean scopes that control bean lifecycle and instance management.

#### Singleton Scope (Default)

**Example:** `src/main/java/com/chapman/edu/commissions/corespring/core/SingletonBean.java:1-60`

```java
@Component  // Singleton is the default scope
public class SingletonBean {
    private final LocalDateTime createdAt;
    private int requestCount = 0;  // Shared state!

    public SingletonBean() {
        this.createdAt = LocalDateTime.now();
        System.out.println("SingletonBean constructor called at " + createdAt);
    }

    @PostConstruct
    public void init() {
        System.out.println("SingletonBean @PostConstruct - Initialization complete");
    }

    @PreDestroy
    public void cleanup() {
        System.out.println("SingletonBean @PreDestroy - Cleanup before destruction");
    }
}
```

**Characteristics:**
- One instance per Spring ApplicationContext
- Created during context initialization (eager) or first use (lazy)
- Shared by all clients
- Must be thread-safe (no mutable state or use synchronization)
- Default scope - no @Scope annotation needed

#### Prototype Scope

**Example:** `src/main/java/com/chapman/edu/commissions/corespring/core/PrototypeBean.java:1-78`

```java
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PrototypeBean {
    private final String instanceId;
    private int operationCount = 0;  // Instance-specific state OK

    public PrototypeBean() {
        this.instanceId = UUID.randomUUID().toString();
        System.out.println("PrototypeBean constructor called - Instance ID: " + instanceId);
    }

    @PostConstruct
    public void init() {
        System.out.println("PrototypeBean @PostConstruct - Instance ID: " + instanceId);
    }

    // WARNING: @PreDestroy is NOT called for prototype beans!
    @PreDestroy
    public void destroy() {
        System.out.println("THIS WON'T BE CALLED - prototype beans not destroyed by Spring");
    }
}
```

**Characteristics:**
- New instance created each time bean is requested
- Spring does NOT manage complete lifecycle (no @PreDestroy)
- Client responsible for cleanup
- Good for stateful beans
- Not pooled or cached

#### Request Scope (Web Applications)

**Example:** `src/main/java/com/chapman/edu/commissions/corespring/core/RequestScopedBean.java:1-68`

```java
@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RequestScopedBean {
    private final String requestId;
    private String userId;

    public RequestScopedBean() {
        this.requestId = UUID.randomUUID().toString();
        System.out.println("RequestScopedBean created for request: " + requestId);
    }

    @PreDestroy
    public void destroy() {
        System.out.println("RequestScopedBean destroyed - Request completed");
    }
}
```

**Characteristics:**
- One instance per HTTP request
- Requires web application context
- Destroyed at end of request
- Requires proxy mode for injection into singleton beans
- Also available: SESSION, APPLICATION scopes

#### Scope Comparison Table

| Scope | Instances | Lifecycle | Use Case |
|-------|-----------|-----------|----------|
| **singleton** | 1 per context | Container startup to shutdown | Stateless services, utilities |
| **prototype** | New per request | Creation only (no destroy) | Stateful objects, commands |
| **request** | 1 per HTTP request | Request start to end | Request-specific data |
| **session** | 1 per HTTP session | Session start to end | User session data |
| **application** | 1 per ServletContext | Context start to end | Shared application state |

### 2.2 Bean Lifecycle

#### Complete Lifecycle Order

**Example:** `src/main/java/com/chapman/edu/commissions/corespring/core/LifecycleBean.java:1-101`

```
1. Constructor
2. Setter methods (dependency injection)
3. BeanPostProcessor.postProcessBeforeInitialization()
4. @PostConstruct
5. InitializingBean.afterPropertiesSet()
6. Custom init-method
7. BeanPostProcessor.postProcessAfterInitialization()
8. Bean is ready to use
   ... application runs ...
9. @PreDestroy
10. DisposableBean.destroy()
11. Custom destroy-method
```

**Full Example:**

```java
@Component
public class LifecycleBean implements InitializingBean, DisposableBean {

    public LifecycleBean() {
        System.out.println("1. Constructor called");
    }

    @PostConstruct
    public void postConstruct() {
        System.out.println("4. @PostConstruct called");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("5. InitializingBean.afterPropertiesSet() called");
    }

    public void customInit() {
        System.out.println("6. Custom init-method called");
    }

    @PreDestroy
    public void preDestroy() {
        System.out.println("9. @PreDestroy called");
    }

    @Override
    public void destroy() throws Exception {
        System.out.println("10. DisposableBean.destroy() called");
    }

    public void customDestroy() {
        System.out.println("11. Custom destroy-method called");
    }
}
```

#### Lifecycle Callback Recommendations

| Approach | Pros | Cons | Recommendation |
|----------|------|------|----------------|
| **@PostConstruct / @PreDestroy** | ✅ Standard JSR-250<br>✅ Framework-agnostic<br>✅ Clear intent | ❌ Requires javax.annotation | **PREFERRED** |
| **InitializingBean / DisposableBean** | ✅ Type-safe<br>✅ IDE support | ❌ Couples code to Spring | Avoid |
| **@Bean(initMethod/destroyMethod)** | ✅ External configuration<br>✅ Works with 3rd party classes | ❌ String-based (no compile-time checking) | Use for 3rd party beans |

### 2.3 BeanPostProcessor

**Example:** `src/main/java/com/chapman/edu/commissions/corespring/core/CustomBeanPostProcessor.java:1-62`

```java
@Component
public class CustomBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        // Called BEFORE @PostConstruct
        System.out.println("BeanPostProcessor BEFORE init: " + beanName);
        return bean;  // Can return original or wrapped bean
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        // Called AFTER all initialization - perfect for creating proxies
        System.out.println("BeanPostProcessor AFTER init: " + beanName);
        return bean;  // Spring AOP returns proxies here!
    }
}
```

**Key Points:**
- Extension point for modifying beans during creation
- Applied to ALL beans in ApplicationContext
- Used internally by Spring for @Autowired, @Value, proxies
- `postProcessAfterInitialization` is where AOP proxies are created
- Can return original bean or wrap it

**Common Uses:**
- Custom annotation processing
- Creating proxies (AOP, transactions)
- Validation and verification
- Logging and debugging
- Modifying bean properties

### 2.4 Autowiring and Qualifier

#### @Autowired Behavior

```java
@Autowired  // By type
private NotificationService notificationService;
```

**Autowiring Resolution:**
1. Match by type
2. If multiple candidates exist → use @Primary or @Qualifier
3. If no candidates → error (unless @Autowired(required=false))

#### @Primary Annotation

**Example:** `src/main/java/com/chapman/edu/commissions/corespring/di/EmailNotificationService.java:1-27`

```java
@Service("defaultNotificationService")
@Primary  // This bean preferred when multiple NotificationService beans exist
public class EmailNotificationService implements NotificationService {
    @Override
    public void notifyCommissionCalculated(CommissionCalculation calculation) {
        System.out.println("EMAIL: Commission calculated - Amount: $" + calculation.getNetCommission());
    }
}
```

#### @Qualifier Annotation

**Example:** `src/main/java/com/chapman/edu/commissions/corespring/di/CommissionCalculationService.java:28-31`

```java
@Autowired
@Qualifier("emailAuditLogger")  // Specify which bean to inject
private AuditLogger auditLogger;
```

**Resolution Priority:**
1. Explicit @Qualifier trumps @Primary
2. @Primary used when no @Qualifier specified
3. Bean name matching as last resort

**Multiple Implementations Example:**

```java
// Interface
public interface NotificationService { void notify(); }

// Implementation 1
@Service("emailNotificationService")
@Primary
public class EmailNotificationService implements NotificationService { }

// Implementation 2
@Service("smsNotificationService")
public class SmsNotificationService implements NotificationService { }

// Client
@Service
public class CommissionService {
    @Autowired  // Gets EmailNotificationService (@Primary)
    private NotificationService defaultNotification;

    @Autowired
    @Qualifier("smsNotificationService")  // Gets SmsNotificationService
    private NotificationService smsNotification;
}
```

---

## 3. Application Context & Configuration

### 3.1 Configuration Strategies

Spring supports three main configuration approaches:

1. **XML Configuration** (Legacy)
2. **Java Configuration** (Modern, type-safe)
3. **Annotation-based** (Component scanning)

#### Strategy Comparison

| Approach | Pros | Cons | Best For |
|----------|------|------|----------|
| **XML** | ✅ Externalized<br>✅ No code changes | ❌ Verbose<br>❌ No type safety<br>❌ Hard to refactor | Legacy apps |
| **Java Config** | ✅ Type-safe<br>✅ Refactorable<br>✅ IDE support | ❌ Requires recompilation | Infrastructure beans |
| **Annotations** | ✅ Concise<br>✅ Co-located with code | ❌ Scattered config<br>❌ Requires classpath scanning | Business components |

### 3.2 Java Configuration

**Example:** `src/main/java/com/chapman/edu/commissions/corespring/config/AppConfig.java:1-96`

```java
@Configuration  // Marks class as source of bean definitions
@ComponentScan(basePackages = "com.chapman.edu.commissions.corespring")  // Enable component scanning
@EnableAspectJAutoProxy  // Enable AOP
@PropertySource("classpath:application.properties")  // Load properties
public class AppConfig {

    @Value("${commission.default.rate:0.10}")  // Inject property with default
    private BigDecimal defaultCommissionRate;

    @Bean  // Explicit bean definition
    public ConfigurationProperties configurationProperties() {
        ConfigurationProperties props = new ConfigurationProperties();
        props.setDefaultCommissionRate(defaultCommissionRate);
        return props;
    }

    @Bean(initMethod = "customInit", destroyMethod = "customDestroy")
    public LifecycleBean lifecycleBeanWithCustomMethods() {
        return new LifecycleBean();
    }

    @Bean
    public DependencyExample dependencyExample(ConfigurationProperties config) {
        // Dependencies auto-injected via method parameters
        return new DependencyExample(config);
    }
}
```

**Key Annotations:**
- `@Configuration`: Class contains bean definitions
- `@Bean`: Method produces a bean (method name = bean name by default)
- `@ComponentScan`: Enable automatic bean discovery
- `@EnableAspectJAutoProxy`: Turn on AOP support
- `@PropertySource`: Load external properties
- `@Import`: Import other configuration classes

### 3.3 XML Configuration (Legacy)

**Example: applicationContext.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                           http://www.springframework.org/schema/beans/spring-beans.xsd">

    <!-- Bean definition -->
    <bean id="commissionService" class="com.chapman.edu.CommissionService">
        <!-- Constructor injection -->
        <constructor-arg ref="notificationService"/>

        <!-- Setter injection -->
        <property name="validationService" ref="validationService"/>
    </bean>

    <bean id="notificationService" class="com.chapman.edu.EmailNotificationService"
          init-method="customInit" destroy-method="customDestroy"/>

    <!-- Property placeholder -->
    <context:property-placeholder location="classpath:application.properties"/>
</beans>
```

**Loading XML Configuration:**

```java
ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
CommissionService service = context.getBean(CommissionService.class);
```

### 3.4 Mixing Configuration Strategies

**Common Pattern: Java Config + Component Scanning**

```java
@Configuration
@ComponentScan(basePackages = "com.chapman.edu.commissions")  // Annotations for business logic
public class AppConfig {

    @Bean  // Java config for infrastructure
    public DataSource dataSource() {
        // Complex setup for third-party class
    }
}
```

**Importing XML into Java Config:**

```java
@Configuration
@ImportResource("classpath:legacy-config.xml")  // Import XML beans
public class ModernConfig {
    @Bean
    public NewService newService() { ... }
}
```

### 3.5 @Value and SpEL (Spring Expression Language)

**Simple Property Injection:**

```java
@Value("${commission.rate}")  // From properties file
private BigDecimal rate;

@Value("${commission.rate:0.10}")  // With default value
private BigDecimal rateWithDefault;
```

**SpEL Examples:**

```java
// System properties
@Value("#{systemProperties['user.home']}")
private String userHome;

// Environment variables
@Value("#{systemEnvironment['PATH']}")
private String path;

// Bean properties
@Value("#{configBean.commissionRate}")
private BigDecimal rate;

// Mathematical expressions
@Value("#{100 * 0.10}")
private Double calculation;

// Conditional expressions
@Value("#{configBean.enabled ? 'ON' : 'OFF'}")
private String status;

// Collections
@Value("#{configBean.rates[0]}")  // First element
private BigDecimal firstRate;

// Method invocation
@Value("#{configBean.getRate()}")
private BigDecimal rate;
```

### 3.6 Profiles

**Example:** `src/main/java/com/chapman/edu/commissions/corespring/config/ProfileConfig.java:1-126`

```java
@Configuration
public class ProfileConfig {

    @Bean
    @Profile("dev")  // Only active in dev profile
    public EnvironmentConfig devEnvironment() {
        EnvironmentConfig config = new EnvironmentConfig();
        config.setEnvironmentName("Development");
        config.setDebugEnabled(true);
        config.setDatabaseUrl("jdbc:h2:mem:devdb");
        return config;
    }

    @Bean
    @Profile("prod")  // Only active in prod profile
    public EnvironmentConfig prodEnvironment() {
        EnvironmentConfig config = new EnvironmentConfig();
        config.setEnvironmentName("Production");
        config.setDebugEnabled(false);
        config.setDatabaseUrl("jdbc:postgresql://prod-db:5432/commissions");
        return config;
    }

    @Bean
    @Profile("default")  // Active when no profile specified
    public EnvironmentConfig defaultEnvironment() {
        // Default configuration
    }
}
```

**Activating Profiles:**

```bash
# Command line
java -Dspring.profiles.active=dev -jar app.jar

# application.properties
spring.profiles.active=dev,debug

# Programmatically
SpringApplication app = new SpringApplication(App.class);
app.setAdditionalProfiles("dev");

# Test
@ActiveProfiles("test")
public class ServiceTest { }
```

**Multiple Profiles:**

```java
@Profile({"dev", "test"})  // Active in dev OR test
@Profile("!prod")  // Active when prod is NOT active
```

### 3.7 Conditional Beans

**Example:** `src/main/java/com/chapman/edu/commissions/corespring/config/ConditionalConfig.java:1-89`

```java
@Configuration
public class ConditionalConfig {

    @Bean
    @ConditionalOnProperty(name = "feature.advanced-calculations", havingValue = "true")
    public AdvancedCalculationEngine advancedCalculationEngine() {
        return new AdvancedCalculationEngine();
    }

    @Bean
    @ConditionalOnMissingBean(AdvancedCalculationEngine.class)  // Fallback
    public BasicCalculationEngine basicCalculationEngine() {
        return new BasicCalculationEngine();
    }

    @Bean
    @ConditionalOnClass(name = "com.mysql.jdbc.Driver")  // If class on classpath
    public DataSource mySqlDataSource() { ... }
}
```

**Common Conditional Annotations:**

| Annotation | Condition |
|------------|-----------|
| `@ConditionalOnProperty` | Property has specific value |
| `@ConditionalOnBean` | Bean exists |
| `@ConditionalOnMissingBean` | Bean doesn't exist (fallback) |
| `@ConditionalOnClass` | Class on classpath |
| `@ConditionalOnMissingClass` | Class not on classpath |
| `@ConditionalOnExpression` | SpEL expression is true |

**Use Cases:**
- Feature flags
- Auto-configuration
- Environment-specific beans
- Fallback implementations

### 3.8 Application Events

**Built-in Events:**

```java
@Component
public class ApplicationEventListener {

    @EventListener
    public void handleContextRefreshed(ContextRefreshedEvent event) {
        System.out.println("Application context initialized");
    }

    @EventListener
    public void handleContextStarted(ContextStartedEvent event) {
        System.out.println("Application context started");
    }

    @EventListener
    public void handleContextStopped(ContextStoppedEvent event) {
        System.out.println("Application context stopped");
    }

    @EventListener
    public void handleContextClosed(ContextClosedEvent event) {
        System.out.println("Application context closed");
    }
}
```

**Custom Events:**

```java
// Event
public class CommissionCalculatedEvent {
    private final String dealId;
    private final BigDecimal amount;

    public CommissionCalculatedEvent(String dealId, BigDecimal amount) {
        this.dealId = dealId;
        this.amount = amount;
    }
}

// Publisher
@Service
public class CommissionService {
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public void calculateCommission(Deal deal) {
        BigDecimal amount = calculate(deal);
        eventPublisher.publishEvent(new CommissionCalculatedEvent(deal.getId(), amount));
    }
}

// Listener
@Component
public class CommissionEventListener {
    @EventListener
    public void handleCommissionCalculated(CommissionCalculatedEvent event) {
        System.out.println("Commission calculated for deal: " + event.getDealId());
    }
}
```

---

## 4. AOP Fundamentals

### 4.1 What is AOP?

**Aspect-Oriented Programming (AOP):**
- Programming paradigm that modularizes cross-cutting concerns
- Separates business logic from infrastructure/system concerns
- Complements Object-Oriented Programming (OOP)

**Cross-Cutting Concerns:**
- Concerns that affect multiple parts of an application
- Examples: logging, security, transactions, caching, error handling, auditing

### 4.2 AOP Terminology

| Term | Definition | Example |
|------|------------|---------|
| **Aspect** | Modularization of a cross-cutting concern | `@Aspect` class containing advice methods |
| **Join Point** | Point during program execution | Method execution, exception thrown |
| **Advice** | Action taken at a join point | `@Before`, `@After`, `@Around` methods |
| **Pointcut** | Expression matching join points | `execution(* com.example..*.*(..))` |
| **Target Object** | Object being advised | Service with business logic |
| **AOP Proxy** | Object created by AOP framework | JDK dynamic proxy or CGLIB proxy |
| **Weaving** | Process of applying aspects | Compile-time, load-time, or runtime |

### 4.3 Advice Types

**Example:** `src/main/java/com/chapman/edu/commissions/corespring/aop/AuditingAspect.java:1-135`

#### @Before Advice

```java
@Before("auditableMethod()")
public void logBefore(JoinPoint joinPoint) {
    System.out.println("[AUDIT @Before] Method: " + joinPoint.getSignature().getName());
    System.out.println("[AUDIT @Before] Args: " + Arrays.toString(joinPoint.getArgs()));
}
```

**Characteristics:**
- Runs BEFORE method execution
- Cannot prevent method execution
- Cannot access return value
- Cannot catch exceptions

**Use Cases:** Logging, validation, security checks

#### @AfterReturning Advice

```java
@AfterReturning(pointcut = "calculationMethods()", returning = "result")
public void logAfterReturning(JoinPoint joinPoint, Object result) {
    System.out.println("[AUDIT @AfterReturning] Method: " + joinPoint.getSignature().getName());
    System.out.println("[AUDIT @AfterReturning] Returned: " + result);
}
```

**Characteristics:**
- Runs AFTER successful method execution
- Has access to return value
- Cannot modify return value
- Not called if exception thrown

**Use Cases:** Logging results, auditing, notifications

#### @AfterThrowing Advice

```java
@AfterThrowing(pointcut = "serviceLayer()", throwing = "ex")
public void logAfterThrowing(JoinPoint joinPoint, Exception ex) {
    System.out.println("[AUDIT @AfterThrowing] Method: " + joinPoint.getSignature().getName());
    System.out.println("[AUDIT @AfterThrowing] Exception: " + ex.getClass().getSimpleName());
}
```

**Characteristics:**
- Runs if method throws exception
- Has access to exception
- Cannot suppress exception
- Can log or transform exception information

**Use Cases:** Error logging, exception translation, alerting

#### @After Advice

```java
@After("auditableMethod()")
public void logAfter(JoinPoint joinPoint) {
    System.out.println("[AUDIT @After] Completed: " + joinPoint.getSignature().getName());
}
```

**Characteristics:**
- Runs after method execution (like finally block)
- Executes whether method succeeds or throws exception
- No access to return value or exception
- Always executes

**Use Cases:** Resource cleanup, final logging

#### @Around Advice (Most Powerful)

**Example:** `src/main/java/com/chapman/edu/commissions/corespring/aop/AuditingAspect.java:104-130`

```java
@Around("calculationMethods()")
public Object measurePerformance(ProceedingJoinPoint joinPoint) throws Throwable {
    long startTime = System.currentTimeMillis();

    System.out.println("[PERFORMANCE @Around] Starting: " + joinPoint.getSignature().getName());

    Object result;
    try {
        // Proceed with the actual method execution
        result = joinPoint.proceed();  // MUST call this!

        long duration = System.currentTimeMillis() - startTime;
        System.out.println("[PERFORMANCE @Around] Completed in: " + duration + " ms");

        return result;  // MUST return value
    } catch (Exception ex) {
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("[PERFORMANCE @Around] Failed after: " + duration + " ms");
        throw ex;  // Re-throw or handle
    }
}
```

**Characteristics:**
- Wraps method execution
- Can prevent method execution (don't call proceed())
- Can modify parameters and return value
- Must call `joinPoint.proceed()` to execute method
- Can catch and handle/transform exceptions
- Most powerful but also most complex

**Use Cases:**
- Performance monitoring
- Transaction management
- Caching
- Retry logic
- Security checks
- Parameter validation

### 4.4 Pointcut Expressions

**Syntax:**

```
execution(modifiers? return-type declaring-type?method-name(params) throws?)
```

**Examples:**

```java
// Any method in service layer
@Pointcut("execution(* com.chapman.edu.commissions.corespring.di..*(..))")
public void serviceLayer() {}

// Methods starting with "calculate"
@Pointcut("execution(* calculate*(..))")
public void calculationMethods() {}

// Methods with specific parameters
@Pointcut("execution(* processCommission(com.chapman.edu.commissions.model.Deal))")
public void processCommissionWithDeal() {}

// Any method in specific class
@Pointcut("execution(* com.chapman.edu.CommissionService.*(..))")
public void commissionServiceMethods() {}

// Methods annotated with @Auditable
@Pointcut("@annotation(com.chapman.edu.corespring.annotations.Auditable)")
public void auditableMethod() {}

// Any method with @Transactional annotation
@Pointcut("@annotation(org.springframework.transaction.annotation.Transactional)")
public void transactionalMethods() {}

// Combine pointcuts with logical operators
@Pointcut("serviceLayer() && calculationMethods()")
public void serviceCalculations() {}

// Within specific package
@Pointcut("within(com.chapman.edu.commissions.service..*)")
public void inServicePackage() {}

// Bean name matching
@Pointcut("bean(*Service)")
public void serviceBeans() {}
```

**Pointcut Designators:**

| Designator | Description | Example |
|------------|-------------|---------|
| `execution` | Match method execution | `execution(* com.example..*.*(..))` |
| `within` | Match within specific types | `within(com.example.service..*)` |
| `this` | Match proxy implements interface | `this(com.example.Service)` |
| `target` | Match target object type | `target(com.example.ServiceImpl)` |
| `args` | Match method parameters | `args(java.lang.String,..)` |
| `@annotation` | Match methods with annotation | `@annotation(Auditable)` |
| `@within` | Match classes with annotation | `@within(Service)` |
| `bean` | Match Spring bean names | `bean(*Service)` |

### 4.5 Proxy Mechanism

**Spring AOP uses proxies:**

1. **JDK Dynamic Proxy** (default for interfaces)
   - Target implements an interface
   - Proxy implements same interface
   - Uses `java.lang.reflect.Proxy`

2. **CGLIB Proxy** (for classes)
   - Target doesn't implement interface (or force with `proxyTargetClass=true`)
   - Proxy extends target class
   - Cannot proxy final classes or methods

**Example:**

```java
@EnableAspectJAutoProxy(proxyTargetClass = true)  // Force CGLIB proxies
public class AppConfig { }
```

**How It Works:**

```
Client → Proxy → Aspect Advice → Target Method
```

```java
// Without AOP
client.commissionService.calculate()  // Direct call

// With AOP
client.proxyOfCommissionService.calculate()  // Goes through proxy
  → @Before advice
  → @Around advice (before)
  → target.calculate()  // Actual method
  → @Around advice (after)
  → @After advice
  → @AfterReturning advice
```

### 4.6 Self-Invocation Pitfall

**Example:** `src/main/java/com/chapman/edu/commissions/corespring/aop/SecurityAspect.java:64-83`

**CRITICAL ISSUE:**

```java
@Service
public class CommissionService {

    @Auditable  // This aspect will NOT be applied!
    public void methodB() {
        System.out.println("Method B");
    }

    public void methodA() {
        // This call BYPASSES the proxy!
        this.methodB();  // ❌ No aspect applied
    }
}
```

**Why:**
- `this` refers to the actual object, not the proxy
- AOP aspects are only applied when calling through the proxy
- Internal method calls bypass the proxy

**Solutions:**

```java
// Solution 1: Inject self
@Service
public class CommissionService {
    @Autowired
    private ApplicationContext context;

    public void methodA() {
        CommissionService proxy = context.getBean(CommissionService.class);
        proxy.methodB();  // ✅ Goes through proxy
    }
}

// Solution 2: Move method to different class
@Service
public class AnotherService {
    @Auditable
    public void methodB() { }
}

@Service
public class CommissionService {
    @Autowired
    private AnotherService anotherService;

    public void methodA() {
        anotherService.methodB();  // ✅ Goes through proxy
    }
}

// Solution 3: Use AspectJ (compile-time weaving) instead of Spring AOP
```

### 4.7 Practical AOP Examples

#### Security Aspect

**Example:** `src/main/java/com/chapman/edu/commissions/corespring/aop/SecurityAspect.java:1-83`

```java
@Aspect
@Component
public class SecurityAspect {

    @Around("@annotation(com.chapman.edu.corespring.annotations.RequiresPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequiresPermission annotation = method.getAnnotation(RequiresPermission.class);
        String requiredPermission = annotation.value();

        if (!checkUserHasPermission(requiredPermission)) {
            throw new SecurityException("Access denied: requires " + requiredPermission);
        }

        return joinPoint.proceed();
    }
}

// Usage
@Service
public class CommissionService {
    @RequiresPermission("CALCULATE_COMMISSION")
    public CommissionCalculation calculate(Deal deal) {
        // Only executes if user has permission
    }
}
```

#### Caching Aspect

**Example:** `src/main/java/com/chapman/edu/commissions/corespring/aop/CachingAspect.java:1-68`

```java
@Aspect
@Component
public class CachingAspect {
    private final ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<>();

    @Around("execution(* com.chapman.edu.corespring.di.CommissionRuleEngine.calculate*(..))")
    public Object cacheCalculationResults(ProceedingJoinPoint joinPoint) throws Throwable {
        String cacheKey = buildCacheKey(joinPoint);

        if (cache.containsKey(cacheKey)) {
            System.out.println("[CACHE] HIT - Returning cached result");
            return cache.get(cacheKey);
        }

        Object result = joinPoint.proceed();
        cache.put(cacheKey, result);
        return result;
    }
}
```

---

## 5. Annotations & Component Scanning

### 5.1 Stereotype Annotations

Spring provides stereotype annotations for semantic bean classification:

```java
@Component   // Generic component
@Service     // Business logic layer
@Repository  // Data access layer
@Controller  // Web/presentation layer
```

**All are equivalent for bean registration but convey semantic meaning.**

#### @Component

```java
@Component
public class CommissionRuleEngine {
    public BigDecimal calculate(Deal deal) {
        // Generic component
    }
}
```

**Use:** Generic Spring-managed component

#### @Service

**Example:** `src/main/java/com/chapman/edu/commissions/corespring/di/CommissionCalculationService.java:26`

```java
@Service
public class CommissionCalculationService {
    // Business logic here
}
```

**Use:** Business service layer, contains business logic

#### @Repository

```java
@Repository
public class DealRepository {
    public Deal findById(String id) {
        // Data access logic
    }
}
```

**Use:**
- Data access layer (DAOs)
- Automatic exception translation (SQLException → DataAccessException)

#### @Controller / @RestController

```java
@RestController
@RequestMapping("/api/commissions")
public class CommissionController {
    @GetMapping("/{id}")
    public Commission getCommission(@PathVariable String id) {
        // Web layer
    }
}
```

**Use:** Web layer, handles HTTP requests

**Best Practice:** Use the most specific stereotype for clarity and potential framework enhancements.

### 5.2 Component Scanning

**Enable Scanning:**

```java
@Configuration
@ComponentScan(basePackages = "com.chapman.edu.commissions")  // Scan package and subpackages
public class AppConfig { }

// Multiple packages
@ComponentScan(basePackages = {"com.chapman.service", "com.chapman.repository"})

// Type-safe scanning (recommended)
@ComponentScan(basePackageClasses = {CommissionService.class, DealRepository.class})

// Exclude filters
@ComponentScan(
    basePackages = "com.chapman.edu",
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com.chapman.edu.legacy.*"
    )
)

// Include filters (disable default filters)
@ComponentScan(
    basePackages = "com.chapman.edu",
    useDefaultFilters = false,
    includeFilters = @ComponentScan.Filter(Service.class)
)
```

**Spring Boot Auto-scanning:**

```java
@SpringBootApplication  // Contains @ComponentScan
public class Application {
    // Automatically scans package and sub-packages
}
```

### 5.3 @Autowired in Detail

**Example:** `src/main/java/com/chapman/edu/commissions/corespring/di/CommissionCalculationService.java`

#### Basic Usage

```java
// Constructor injection (recommended)
@Autowired  // Optional since Spring 4.3
public CommissionService(NotificationService notificationService) {
    this.notificationService = notificationService;
}

// Field injection (not recommended)
@Autowired
private NotificationService notificationService;

// Setter injection
@Autowired
public void setNotificationService(NotificationService notificationService) {
    this.notificationService = notificationService;
}
```

#### Optional Dependencies

```java
// Option 1: required = false
@Autowired(required = false)
private ValidationService validationService;

// Option 2: Java 8 Optional
@Autowired
private Optional<ValidationService> validationService;

// Option 3: @Nullable
@Autowired
public void setValidationService(@Nullable ValidationService validationService) {
    this.validationService = validationService;
}
```

#### Collection Injection

```java
// Inject all beans of type NotificationService
@Autowired
private List<NotificationService> notificationServices;

// Map with bean names as keys
@Autowired
private Map<String, NotificationService> notificationServiceMap;
```

#### @Qualifier for Disambiguation

**Example:** `src/main/java/com/chapman/edu/commissions/corespring/di/CommissionCalculationService.java:44-47`

```java
@Autowired
public CommissionService(
    @Qualifier("emailNotificationService") NotificationService emailService,
    @Qualifier("smsNotificationService") NotificationService smsService) {
    // Inject specific implementations
}
```

#### @Primary for Default Selection

```java
@Service
@Primary  // This bean is preferred when multiple candidates exist
public class EmailNotificationService implements NotificationService { }

@Service
public class SmsNotificationService implements NotificationService { }

// Injection
@Autowired  // Gets EmailNotificationService (@Primary)
private NotificationService notificationService;
```

### 5.4 Custom Annotations

**Example:** `src/main/java/com/chapman/edu/commissions/corespring/annotations/Auditable.java`

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    String action() default "";
    boolean logParams() default true;
}
```

**Usage:**

```java
@Service
public class CommissionService {

    @Auditable(action = "CALCULATE_COMMISSION", logParams = true)
    public CommissionCalculation calculate(Deal deal) {
        // Method automatically audited by AOP
    }
}
```

**Aspect for Custom Annotation:**

```java
@Aspect
@Component
public class AuditingAspect {

    @Before("@annotation(auditable)")
    public void audit(JoinPoint joinPoint, Auditable auditable) {
        System.out.println("Auditing action: " + auditable.action());
        if (auditable.logParams()) {
            System.out.println("Parameters: " + Arrays.toString(joinPoint.getArgs()));
        }
    }
}
```

### 5.5 @Value and Property Injection

```java
@Component
public class CommissionConfig {

    // Simple property
    @Value("${commission.default.rate}")
    private BigDecimal defaultRate;

    // With default value
    @Value("${commission.bonus.enabled:true}")
    private boolean bonusEnabled;

    // List from properties
    @Value("${commission.excluded.products}")
    private List<String> excludedProducts;

    // SpEL expression
    @Value("#{${commission.default.rate} * 100}")
    private double ratePercentage;

    // System property
    @Value("#{systemProperties['user.home']}")
    private String userHome;
}
```

**Properties File (application.properties):**

```properties
commission.default.rate=0.10
commission.bonus.enabled=true
commission.excluded.products=TRIAL,SAMPLE,DEMO
```

### 5.6 @PostConstruct and @PreDestroy

```java
@Component
public class CommissionService {

    @PostConstruct  // Called after dependency injection
    public void init() {
        System.out.println("Service initialized - loading configuration");
        // Load caches, verify connections, etc.
    }

    @PreDestroy  // Called before bean destruction
    public void cleanup() {
        System.out.println("Service shutting down - releasing resources");
        // Close connections, flush caches, etc.
    }
}
```

**Execution Order:**
1. Constructor
2. Dependency Injection
3. @PostConstruct ← Initialization logic here
4. Bean ready for use
5. @PreDestroy ← Cleanup logic here
6. Bean destroyed

---

## 6. Practical Examples & Best Practices

### 6.1 Dependency Injection Best Practices

#### ✅ DO: Constructor Injection for Required Dependencies

```java
@Service
public class CommissionService {
    private final DealRepository dealRepository;  // final = immutable
    private final NotificationService notificationService;

    public CommissionService(DealRepository dealRepository,
                            NotificationService notificationService) {
        this.dealRepository = dealRepository;
        this.notificationService = notificationService;
    }
}
```

**Why:**
- Immutability (thread-safe)
- Explicit dependencies
- Easy to test
- Prevents null dependencies

#### ❌ DON'T: Field Injection

```java
@Service
public class CommissionService {
    @Autowired  // ❌ Hard to test, hidden dependencies
    private DealRepository dealRepository;
}
```

#### ✅ DO: Depend on Interfaces, Not Implementations

```java
// ✅ Good - depends on interface
public class CommissionService {
    private final NotificationService notificationService;
}

// ❌ Bad - depends on concrete class
public class CommissionService {
    private final EmailNotificationService emailService;
}
```

#### ✅ DO: Use @Qualifier for Multiple Implementations

```java
@Service
public class CommissionService {
    private final NotificationService emailService;
    private final NotificationService smsService;

    public CommissionService(
        @Qualifier("emailNotificationService") NotificationService emailService,
        @Qualifier("smsNotificationService") NotificationService smsService) {
        this.emailService = emailService;
        this.smsService = smsService;
    }
}
```

### 6.2 Bean Scope Best Practices

#### Singleton (Default) - Use for Stateless Beans

```java
@Service  // Singleton by default
public class CommissionCalculator {
    // ✅ No mutable state - thread-safe
    public BigDecimal calculate(Deal deal, CommissionPlan plan) {
        return deal.getValue().multiply(plan.getRate());
    }
}
```

#### Prototype - Use for Stateful Beans

```java
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CommissionReport {
    private List<CommissionCalculation> calculations = new ArrayList<>();  // Stateful

    public void addCalculation(CommissionCalculation calc) {
        calculations.add(calc);
    }
}
```

#### Request - Use for Web Request Context

```java
@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RequestContext {
    private String userId;
    private String sessionId;
    // Request-specific state
}
```

### 6.3 Configuration Best Practices

#### ✅ DO: Use Java Configuration for Infrastructure

```java
@Configuration
public class InfrastructureConfig {

    @Bean
    public DataSource dataSource() {
        // Complex third-party bean setup
    }

    @Bean
    public EntityManagerFactory entityManagerFactory() {
        // Infrastructure bean
    }
}
```

#### ✅ DO: Use @Component for Business Logic

```java
@Service  // Component scanning for business logic
public class CommissionService {
    // Business logic
}
```

#### ✅ DO: Externalize Configuration

```java
// application.properties
commission.default.rate=0.10
commission.bonus.threshold=10000

// Configuration class
@Configuration
public class CommissionConfig {
    @Value("${commission.default.rate}")
    private BigDecimal defaultRate;
}
```

#### ✅ DO: Use Profiles for Environments

```java
@Configuration
@Profile("prod")
public class ProductionConfig {
    @Bean
    public DataSource dataSource() {
        // Production database
    }
}

@Configuration
@Profile("dev")
public class DevelopmentConfig {
    @Bean
    public DataSource dataSource() {
        // H2 in-memory database
    }
}
```

### 6.4 AOP Best Practices

#### ✅ DO: Use AOP for Cross-Cutting Concerns

**Good Use Cases:**
- Logging and auditing
- Security and authorization
- Transaction management
- Performance monitoring
- Caching
- Error handling

```java
@Aspect
@Component
public class LoggingAspect {
    @Before("@annotation(Auditable)")
    public void logMethodEntry(JoinPoint joinPoint) {
        // Logging cross-cuts all services
    }
}
```

#### ❌ DON'T: Use AOP for Business Logic

```java
// ❌ Bad - business logic in aspect
@Around("execution(* calculateCommission(..))")
public Object addBonus(ProceedingJoinPoint joinPoint) throws Throwable {
    Object result = joinPoint.proceed();
    // Don't add business logic here!
    return result;
}
```

#### ✅ DO: Be Specific with Pointcuts

```java
// ✅ Good - specific pointcut
@Pointcut("execution(* com.chapman.edu.service.*Service.*(..))")
public void serviceMethods() {}

// ❌ Bad - too broad
@Pointcut("execution(* *(..))")  // Matches EVERYTHING!
public void allMethods() {}
```

#### ✅ DO: Remember Self-Invocation Limitation

```java
@Service
public class CommissionService {

    @Transactional
    public void methodA() { }

    public void methodB() {
        this.methodA();  // ❌ @Transactional NOT applied!
    }
}
```

### 6.5 Testing with Dependency Injection

#### Unit Testing with Constructor Injection

```java
@Service
public class CommissionService {
    private final DealRepository repository;

    public CommissionService(DealRepository repository) {
        this.repository = repository;
    }
}

// Test
public class CommissionServiceTest {
    @Test
    public void testCalculate() {
        DealRepository mockRepo = mock(DealRepository.class);
        CommissionService service = new CommissionService(mockRepo);  // Easy to test!

        // Test service
    }
}
```

#### Integration Testing with Spring Context

```java
@SpringBootTest
@ActiveProfiles("test")
public class CommissionServiceIntegrationTest {

    @Autowired
    private CommissionService commissionService;

    @MockBean  // Replace bean with mock
    private NotificationService notificationService;

    @Test
    public void testCalculateWithRealDependencies() {
        // Test with real Spring context
    }
}
```

### 6.6 Common Pitfalls and Solutions

#### Pitfall 1: Circular Dependencies

```java
// ❌ Problem
@Service
class A {
    @Autowired B b;
}

@Service
class B {
    @Autowired A a;
}
```

**Solution:** Refactor design to eliminate circular dependency

```java
// ✅ Solution: Extract common functionality
@Service
class A {
    @Autowired C c;
}

@Service
class B {
    @Autowired C c;
}

@Service
class C {
    // Shared functionality
}
```

#### Pitfall 2: Field Injection Testing Issues

```java
// ❌ Problem - hard to test
@Service
public class CommissionService {
    @Autowired
    private DealRepository repository;  // How to inject mock in test?
}
```

**Solution:** Use constructor injection

```java
// ✅ Solution
@Service
public class CommissionService {
    private final DealRepository repository;

    public CommissionService(DealRepository repository) {
        this.repository = repository;
    }
}
```

#### Pitfall 3: Stateful Singleton Beans

```java
// ❌ Problem - thread safety issue
@Service  // Singleton!
public class CommissionCalculator {
    private BigDecimal currentTotal = BigDecimal.ZERO;  // Shared mutable state!

    public void calculate(Deal deal) {
        currentTotal = currentTotal.add(deal.getValue());  // Race condition!
    }
}
```

**Solution:** Make singleton stateless or use prototype scope

```java
// ✅ Solution 1: Stateless singleton
@Service
public class CommissionCalculator {
    public BigDecimal calculate(Deal deal, BigDecimal runningTotal) {
        return runningTotal.add(deal.getValue());  // No state
    }
}

// ✅ Solution 2: Prototype scope for stateful beans
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CommissionCalculator {
    private BigDecimal currentTotal = BigDecimal.ZERO;  // OK - new instance each time
}
```

#### Pitfall 4: Missing @Configuration on Config Classes

```java
// ❌ Problem - beans not registered
public class AppConfig {  // Missing @Configuration!
    @Bean
    public DataSource dataSource() { }
}
```

**Solution:** Add @Configuration

```java
// ✅ Solution
@Configuration
public class AppConfig {
    @Bean
    public DataSource dataSource() { }
}
```

### 6.7 Performance Considerations

#### Bean Creation is Expensive

```java
// ❌ Expensive - creates new bean every time
@Bean
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public ExpensiveService expensiveService() {
    // Heavyweight initialization
}
```

**Solution:** Use singleton when possible, lazy initialization

```java
// ✅ Better - singleton with lazy initialization
@Bean
@Lazy  // Created only when first requested
public ExpensiveService expensiveService() {
    return new ExpensiveService();
}
```

#### Component Scanning Overhead

```java
// ❌ Scans everything
@ComponentScan("com.chapman")  // Too broad
```

**Solution:** Scan specific packages

```java
// ✅ Better - specific packages
@ComponentScan({"com.chapman.service", "com.chapman.repository"})
```

### 6.8 Real-World Example: Commission Calculator Service

**Complete Example Using All Concepts:**

```java
// Domain Model (from our codebase)
// src/main/java/com/chapman/edu/commissions/model/Deal.java
public class Deal {
    private String id;
    private BigDecimal value;
    private String salesRepId;
    // ... fields and methods
}

// Service Interface (DIP)
public interface CommissionCalculator {
    CommissionCalculation calculate(Deal deal, String planId);
}

// Service Implementation (DI)
@Service
@Transactional  // AOP for transactions
public class StandardCommissionCalculator implements CommissionCalculator {

    private final CommissionRuleEngine ruleEngine;  // Constructor injection
    private final NotificationService notificationService;

    @Autowired  // Optional since Spring 4.3
    public StandardCommissionCalculator(
        CommissionRuleEngine ruleEngine,
        @Qualifier("emailNotificationService") NotificationService notificationService) {
        this.ruleEngine = ruleEngine;
        this.notificationService = notificationService;
    }

    @Override
    @Auditable(action = "CALCULATE_COMMISSION")  // Custom annotation for AOP
    public CommissionCalculation calculate(Deal deal, String planId) {
        BigDecimal baseCommission = ruleEngine.calculateBaseCommission(deal, planId);

        CommissionCalculation calculation = new CommissionCalculation(
            deal.getId(),
            deal.getSalesRepId(),
            baseCommission
        );

        calculation.recalculate();
        notificationService.notifyCommissionCalculated(calculation);

        return calculation;
    }
}

// Configuration
@Configuration
@EnableAspectJAutoProxy  // Enable AOP
@ComponentScan(basePackages = "com.chapman.edu.commissions")
public class AppConfig {

    @Bean
    @Profile("prod")
    public NotificationService prodNotificationService() {
        return new EmailNotificationService();
    }

    @Bean
    @Profile("dev")
    public NotificationService devNotificationService() {
        return new LoggingNotificationService();  // Just logs, doesn't send emails
    }
}

// Aspect for Auditing
@Aspect
@Component
public class AuditingAspect {

    @Around("@annotation(auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        System.out.println("Auditing: " + auditable.action());

        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long duration = System.currentTimeMillis() - start;

        System.out.println("Completed in: " + duration + "ms");
        return result;
    }
}
```

---

## Summary Checklist

### Dependency Injection
- ✅ Use constructor injection for required dependencies
- ✅ Depend on interfaces, not implementations (DIP)
- ✅ Use @Qualifier for multiple implementations
- ✅ Use @Primary for default implementations
- ❌ Avoid field injection in production code
- ❌ Avoid circular dependencies

### Bean Lifecycle & Scopes
- ✅ Understand singleton (default), prototype, request, session scopes
- ✅ Use @PostConstruct for initialization logic
- ✅ Use @PreDestroy for cleanup logic (not called for prototype!)
- ✅ Keep singleton beans stateless and thread-safe
- ✅ Use prototype scope for stateful beans

### Configuration
- ✅ Use @Configuration for infrastructure beans
- ✅ Use @Component/@Service for business logic
- ✅ Externalize configuration with @Value and properties files
- ✅ Use @Profile for environment-specific configuration
- ✅ Use @Conditional for feature flags and optional beans

### AOP
- ✅ Use AOP for cross-cutting concerns (logging, security, caching)
- ✅ Use @Before for pre-processing
- ✅ Use @Around for wrapping (most powerful)
- ✅ Use specific pointcuts (avoid overly broad expressions)
- ❌ Don't use AOP for business logic
- ❌ Remember self-invocation limitation

### Annotations
- ✅ Use appropriate stereotype annotations (@Service, @Repository, etc.)
- ✅ Enable component scanning with @ComponentScan
- ✅ Use @Autowired on constructors (optional since Spring 4.3)
- ✅ Create custom annotations for domain-specific concerns

---

## Additional Resources

### Official Documentation
- [Spring Framework Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/)
- [Spring Core Container](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html)
- [Spring AOP](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#aop)

### Code Examples
- All examples in this lecture are available in the codebase:
  - `src/main/java/com/chapman/edu/commissions/corespring/di/` - Dependency Injection
  - `src/main/java/com/chapman/edu/commissions/corespring/core/` - Bean Lifecycle & Scopes
  - `src/main/java/com/chapman/edu/commissions/corespring/config/` - Configuration
  - `src/main/java/com/chapman/edu/commissions/corespring/aop/` - AOP Examples
  - `src/main/java/com/chapman/edu/commissions/corespring/annotations/` - Custom Annotations

### Practice Exercises
1. Create a new service with constructor injection
2. Implement a custom @Cacheable annotation with AOP
3. Configure different beans for dev/prod profiles
4. Create a BeanPostProcessor to modify beans after creation
5. Implement a prototype-scoped bean for report generation

---

**End of Lecture Notes**
