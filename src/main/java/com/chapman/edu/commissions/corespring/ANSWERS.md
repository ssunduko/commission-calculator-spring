# Spring Core Concepts - Answer Key

This document provides detailed answers to all questions in QUESTIONS.md.

---

## Section 1: Dependency Injection & IoC Concepts

### Answer 1.1: Understanding the Problem

**The three main problems are:**

1. **Tight Coupling:**
   - `CommissionService` is tightly coupled to `EmailService` concrete implementation
   - Cannot easily swap EmailService with SmsService or MockEmailService
   - Changes to EmailService constructor will break CommissionService

2. **Hard to Test:**
   - Cannot inject a mock EmailService for unit testing
   - Must use real EmailService in tests, requiring email infrastructure
   - Cannot verify email sending behavior without integration tests

3. **Violates Single Responsibility Principle:**
   - CommissionService is responsible for both commission processing AND creating its dependencies
   - Hidden dependency - not visible in constructor or method signature
   - Difficult to see what CommissionService actually needs

**Better approach using DI:**
```java
@Service
public class CommissionService {
    private final NotificationService notificationService;  // Interface!

    public CommissionService(NotificationService notificationService) {
        this.notificationService = notificationService;  // Injected!
    }

    public void processCommission(Deal deal) {
        // ... calculate commission
        notificationService.send("Commission calculated");
    }
}
```

### Answer 1.2: IoC vs DI

**Inversion of Control (IoC):**
- A design principle where control flow is inverted
- Instead of your code controlling the flow and creating objects, the framework controls it
- The framework calls your code (Hollywood Principle: "Don't call us, we'll call you")
- Examples: Dependency Injection, Event-driven programming, Template Method pattern

**Dependency Injection (DI):**
- A design pattern that implements IoC
- Dependencies are "injected" into objects rather than created by them
- Specifically about providing dependencies from outside
- A specific implementation technique of the broader IoC principle

**Relationship:**
- **DI is a type of IoC** (not the other way around)
- IoC is the general principle; DI is one way to achieve it
- All DI is IoC, but not all IoC is DI

**Analogy:**
- IoC = "Let the framework manage things"
- DI = "Let the framework provide your dependencies"

### Answer 1.3: DIP vs DI

**Dependency Inversion Principle (DIP):**
- A design principle from SOLID principles
- States: "Depend on abstractions, not on concretions"
- High-level modules should not depend on low-level modules; both should depend on abstractions
- About the DIRECTION of dependencies in your design

**Dependency Injection (DI):**
- A design pattern/technique
- About HOW dependencies are provided (injected vs created)
- About the MECHANISM of providing dependencies

**Key Difference:**
- DIP = WHAT to depend on (abstractions)
- DI = HOW to get dependencies (injection)

**DIP Example:**

```java
// WITHOUT DIP - depends on concrete class
public class CommissionService {
    private EmailNotificationService emailService;  // ❌ Concrete class
}

// WITH DIP - depends on abstraction
public class CommissionService {
    private NotificationService notificationService;  // ✅ Interface

    public CommissionService(NotificationService notificationService) {  // DI!
        this.notificationService = notificationService;
    }
}

// Low-level module depends on abstraction
public class EmailNotificationService implements NotificationService {
    // Implementation
}
```

**Both DIP and DI working together:**
- DIP ensures you depend on `NotificationService` interface
- DI ensures the concrete implementation is injected, not created

### Answer 1.4: Injection Types

**Constructor Injection:**

```java
@Service
public class CommissionService {
    private final DealRepository repository;  // Can be final!

    @Autowired  // Optional since Spring 4.3
    public CommissionService(DealRepository repository) {
        this.repository = repository;
    }
}
```

**Pros:**
- ✅ Immutable dependencies (final fields)
- ✅ Required dependencies are explicit
- ✅ Easy to test (pass mocks in constructor)
- ✅ Thread-safe
- ✅ Prevents partially constructed objects
- ✅ Dependencies visible in signature

**Cons:**
- ❌ Constructor can become large with many dependencies (code smell)

**When to use:** Always use for required dependencies (recommended approach)

---

**Setter Injection:**

```java
@Service
public class CommissionService {
    private ValidationService validationService;

    @Autowired(required = false)
    public void setValidationService(ValidationService validationService) {
        this.validationService = validationService;
    }
}
```

**Pros:**
- ✅ Good for optional dependencies
- ✅ Allows reconfiguration after construction
- ✅ Resolves circular dependencies (though avoid them)

**Cons:**
- ❌ Cannot use final fields
- ❌ Can create partially constructed objects
- ❌ Dependencies not immediately obvious

**When to use:** Optional dependencies with sensible defaults

---

**Field Injection:**

```java
@Service
public class CommissionService {
    @Autowired
    private DealRepository repository;
}
```

**Pros:**
- ✅ Less boilerplate
- ✅ Quick to write

**Cons:**
- ❌ Cannot use final fields
- ❌ Hidden dependencies
- ❌ Hard to test without Spring
- ❌ Cannot inject in unit tests
- ❌ Violates encapsulation
- ❌ Requires reflection

**When to use:** Never in production code; only for quick prototypes

**Recommendation:** Constructor injection for 99% of cases

### Answer 1.5: Benefits of DI

**1. Loose Coupling**
```java
// Loose coupling - depends on interface
@Service
public class CommissionService {
    private final NotificationService notificationService;  // Interface

    public CommissionService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
}

// Can easily switch implementations
@Service @Primary
public class EmailNotificationService implements NotificationService { }

@Service
public class SmsNotificationService implements NotificationService { }
```

**2. Testability**
```java
public class CommissionServiceTest {
    @Test
    public void testCalculate() {
        // Easy to inject mocks
        NotificationService mockNotification = mock(NotificationService.class);
        DealRepository mockRepository = mock(DealRepository.class);

        CommissionService service = new CommissionService(
            mockNotification,
            mockRepository
        );

        // Test with mocks - no real dependencies needed
        service.calculate(deal);
        verify(mockNotification).notify();
    }
}
```

**3. Maintainability**
```java
// Change implementation without modifying clients
@Configuration
public class AppConfig {
    @Bean
    @Profile("dev")
    public NotificationService devNotification() {
        return new LoggingNotificationService();  // Just logs, no real emails
    }

    @Bean
    @Profile("prod")
    public NotificationService prodNotification() {
        return new EmailNotificationService();  // Real emails
    }
}
```

**4. Single Responsibility**
```java
// Service only handles business logic, not dependency creation
@Service
public class CommissionService {
    // Spring creates and injects dependencies
    // Service focuses solely on commission calculation
}
```

**5. Flexibility**
```java
// Easy to add new implementations
@Service
@ConditionalOnProperty("notifications.slack.enabled")
public class SlackNotificationService implements NotificationService {
    // New implementation - existing code unchanged
}
```

**6. Configuration Externalization**
```java
// Wire dependencies through configuration, not code
@Configuration
public class AppConfig {
    @Bean
    public CommissionService commissionService(
        @Qualifier("emailService") NotificationService notification) {
        // Configuration in one place
        return new CommissionService(notification);
    }
}
```

### Answer 1.6: Anti-Patterns

**The anti-pattern is: Service Locator Pattern**

**Why it's problematic:**

1. **Hidden Dependencies:**
   ```java
   // Not clear that CommissionCalculator depends on NotificationService
   // Have to read method body to discover dependencies
   ```

2. **Hard to Test:**
   ```java
   // How do you inject a mock NotificationService?
   // ServiceLocator is global state - need to configure it in tests
   ```

3. **Tight Coupling to ServiceLocator:**
   ```java
   // Now coupled to ServiceLocator framework
   // Cannot use this class without ServiceLocator infrastructure
   ```

4. **Runtime Errors:**
   ```java
   // If NotificationService not registered, get runtime error
   // No compile-time safety
   ```

**Better approach with DI:**

```java
@Service
public class CommissionCalculator {
    private final NotificationService notificationService;

    public CommissionCalculator(NotificationService notificationService) {
        this.notificationService = notificationService;  // Explicit dependency
    }

    public BigDecimal calculate(Deal deal) {
        BigDecimal commission = deal.getValue().multiply(new BigDecimal("0.10"));
        notificationService.notify();  // Use injected dependency
        return commission;
    }
}
```

---

## Section 2: Spring Core Container

### Answer 2.1: Bean Scopes

| Scope | Instances Per Container | When Created | When Destroyed | Thread-Safe Required? | Use Case |
|-------|------------------------|--------------|----------------|----------------------|----------|
| **singleton** | 1 | Container startup (or first use if @Lazy) | Container shutdown | Yes | Stateless services, utilities |
| **prototype** | New each time requested | When requested | Never (client responsible) | No | Stateful objects, commands |
| **request** | 1 per HTTP request | Request start | Request end | No | Request-specific data |
| **session** | 1 per HTTP session | Session start | Session end | No | User session data |

### Answer 2.2: Scope Selection

**a) Commission calculation service (stateless)**
```java
@Service  // Singleton (default)
public class CommissionCalculator {
    // Stateless operations - safe to share
    public BigDecimal calculate(Deal deal) {
        return deal.getValue().multiply(new BigDecimal("0.10"));
    }
}
```
**Justification:** Singleton is perfect for stateless services. No mutable state means thread-safe and efficient.

**b) Shopping cart (stores items)**
```java
@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ShoppingCart {
    private List<Item> items = new ArrayList<>();  // User-specific state
}
```
**Justification:** Session scope keeps cart data across multiple requests for the same user.

**c) Report generator (accumulates data)**
```java
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ReportGenerator {
    private List<Data> accumulatedData = new ArrayList<>();  // Stateful

    public void addData(Data data) {
        accumulatedData.add(data);
    }
}
```
**Justification:** Prototype scope because each report generation needs its own instance with separate state.

**d) Logging service (writes to file)**
```java
@Service  // Singleton
public class LoggingService {
    private final Logger logger = LoggerFactory.getLogger(LoggingService.class);

    public synchronized void log(String message) {  // Synchronized for thread-safety
        logger.info(message);
    }
}
```
**Justification:** Singleton is fine - loggers are thread-safe and stateless.

**e) User context (current user info for web request)**
```java
@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UserContext {
    private String userId;
    private String role;
}
```
**Justification:** Request scope provides user-specific data for each HTTP request.

### Answer 2.3: Bean Lifecycle

**Correct order:**

1. Constructor
2. Dependency Injection
3. BeanPostProcessor.postProcessBeforeInitialization()
4. @PostConstruct
5. InitializingBean.afterPropertiesSet()
6. Custom init-method
7. BeanPostProcessor.postProcessAfterInitialization()
8. Bean is ready to use
9. @PreDestroy
10. DisposableBean.destroy()
11. Custom destroy-method

**Memory aid:** "Constructor → Inject → Before → Post → Properties → Init → After → USE → Pre → Destroy → Custom"

### Answer 2.4: Prototype Scope Pitfall

**What's wrong:**

```java
@PreDestroy
public void cleanup() {
    tempFile.delete();  // ❌ THIS WILL NOT EXECUTE!
}
```

**Problem:**
- Spring does NOT manage the complete lifecycle of prototype beans
- `@PreDestroy` is never called for prototype beans
- Temporary file will never be deleted → **RESOURCE LEAK**

**What happens at shutdown:**
- Singleton beans: @PreDestroy called, resources cleaned up
- Prototype beans: Spring does nothing, instances just become eligible for GC

**Solution:**

```java
// Option 1: Manual cleanup (client responsibility)
ReportGenerator generator = context.getBean(ReportGenerator.class);
try {
    generator.generate();
} finally {
    generator.cleanup();  // Manual cleanup
}

// Option 2: Implement DisposableBean and manage yourself
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ReportGenerator implements DisposableBean {
    private File tempFile;

    @PostConstruct
    public void init() {
        tempFile = File.createTempFile("report", ".tmp");
    }

    @Override
    public void destroy() {
        if (tempFile != null) {
            tempFile.delete();
        }
    }
}

// Client must call destroy manually
((DisposableBean) generator).destroy();

// Option 3: Use try-with-resources if implementing AutoCloseable
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ReportGenerator implements AutoCloseable {
    @Override
    public void close() {
        if (tempFile != null) {
            tempFile.delete();
        }
    }
}

// Usage
try (ReportGenerator generator = context.getBean(ReportGenerator.class)) {
    generator.generate();
}  // Automatically calls close()
```

**Key Point:** For prototype beans, YOU are responsible for cleanup!

### Answer 2.5: BeanPostProcessor

**What is BeanPostProcessor:**
- Spring extension point for modifying beans after creation
- Applied to ALL beans in the ApplicationContext
- Allows custom bean modification before and after initialization

**When to use:**
- Custom annotation processing
- Creating proxies (AOP, transactions)
- Bean validation and verification
- Property modification
- Logging and debugging

**Difference between methods:**

**postProcessBeforeInitialization():**
- Called BEFORE init methods (@PostConstruct, afterPropertiesSet, init-method)
- Bean is constructed and dependencies are injected
- Can modify bean properties
- Cannot access bean functionality yet

**postProcessAfterInitialization():**
- Called AFTER all initialization is complete
- Bean is fully initialized and ready to use
- **This is where Spring AOP creates proxies!**
- Can wrap bean in a proxy

**Example:**
```java
@Component
public class CustomBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        // Called before @PostConstruct
        if (bean instanceof Auditable) {
            System.out.println("Preparing auditable bean: " + beanName);
        }
        return bean;  // Return original bean
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        // Called after all initialization - PROXY CREATION HAPPENS HERE
        if (needsProxy(bean)) {
            return createProxy(bean);  // Return proxy wrapping bean
        }
        return bean;
    }
}
```

**Which creates proxies?**
- **postProcessAfterInitialization()** creates proxies
- Spring AOP's `AnnotationAwareAspectJAutoProxyCreator` is a BeanPostProcessor
- It wraps beans in proxies in `postProcessAfterInitialization()`

### Answer 2.6: @Primary vs @Qualifier

**Analysis:**

```java
@Service("emailService")
@Primary  // This is the default
public class EmailNotificationService implements NotificationService { }

@Service("smsService")
public class SmsNotificationService implements NotificationService { }

@Service
public class CommissionService {
    @Autowired
    private NotificationService service1;  // EmailNotificationService (@Primary wins)

    @Autowired
    @Qualifier("smsService")
    private NotificationService service2;  // SmsNotificationService (@Qualifier wins)

    @Autowired
    @Qualifier("emailService")
    private NotificationService service3;  // EmailNotificationService (@Qualifier wins)
}
```

**Resolution rules:**
1. If `@Qualifier` is present → use the specified bean
2. If no `@Qualifier` → use `@Primary` bean if exists
3. If no `@Primary` → match by bean name
4. If still ambiguous → throw exception

**Priority:**
`@Qualifier` > `@Primary` > Bean name matching

---

## Section 3: Application Context & Configuration

### Answer 3.1: Configuration Approaches

**XML Configuration:**

```xml
<beans>
    <bean id="commissionService" class="com.example.CommissionService">
        <constructor-arg ref="repository"/>
    </bean>
</beans>
```

**Pros:**
- ✅ Externalized (change without recompiling)
- ✅ Can modify without source code access
- ✅ Good for legacy integration

**Cons:**
- ❌ Verbose
- ❌ No compile-time type checking
- ❌ Hard to refactor
- ❌ Prone to typos
- ❌ Poor IDE support

**When to use:** Legacy systems, third-party integration

---

**Java Configuration:**

```java
@Configuration
public class AppConfig {
    @Bean
    public CommissionService commissionService(DealRepository repository) {
        return new CommissionService(repository);
    }
}
```

**Pros:**
- ✅ Type-safe (compile-time checking)
- ✅ Refactorable
- ✅ Excellent IDE support
- ✅ Can use Java logic in configuration
- ✅ Single language (Java everywhere)

**Cons:**
- ❌ Requires recompilation for changes
- ❌ More verbose than annotations

**When to use:** Infrastructure beans, third-party beans, complex initialization

---

**Annotation-based:**

```java
@Service
public class CommissionService {
    @Autowired
    public CommissionService(DealRepository repository) { }
}
```

**Pros:**
- ✅ Concise
- ✅ Co-located with code
- ✅ Type-safe
- ✅ Less configuration overhead

**Cons:**
- ❌ Configuration scattered across codebase
- ❌ Requires classpath scanning (slower startup)
- ❌ Cannot use for third-party classes

**When to use:** Application beans, business logic, when you own the code

**Best Practice:**
- Annotations for application components
- Java config for infrastructure
- XML only for legacy integration

### Answer 3.2: @Configuration vs @Component

**@Configuration:**
```java
@Configuration
public class AppConfig {
    @Bean
    public ServiceA serviceA() {
        return new ServiceA();
    }

    @Bean
    public ServiceB serviceB() {
        return new ServiceB(serviceA());  // Uses proxy!
    }
}
```

**@Component:**
```java
@Component
public class AppConfig {
    @Bean
    public ServiceA serviceA() {
        return new ServiceA();
    }

    @Bean
    public ServiceB serviceB() {
        return new ServiceB(serviceA());  // New instance!
    }
}
```

**Key Differences:**

1. **CGLIB Proxying:**
   - `@Configuration`: Class is proxied by CGLIB
   - `@Component`: No proxying

2. **@Bean Method Calls:**
   - `@Configuration`: Inter-bean calls go through proxy → same instance
   - `@Component`: Direct method call → new instance (breaks singleton!)

3. **Full vs Lite Mode:**
   - `@Configuration`: Full mode (proxied)
   - `@Component`: Lite mode (not proxied)

**Example of the difference:**

```java
@Configuration
public class FullConfig {
    @Bean
    public ServiceA serviceA() {
        return new ServiceA();
    }

    @Bean
    public ServiceB serviceB() {
        return new ServiceB(serviceA());  // ✅ Calls proxy → gets singleton
    }

    @Bean
    public ServiceC serviceC() {
        return new ServiceC(serviceA());  // ✅ Same instance as serviceB uses
    }
}

@Component
public class LiteConfig {
    @Bean
    public ServiceA serviceA() {
        return new ServiceA();
    }

    @Bean
    public ServiceB serviceB() {
        return new ServiceB(serviceA());  // ❌ Direct call → NEW instance
    }

    @Bean
    public ServiceC serviceC() {
        return new ServiceC(serviceA());  // ❌ DIFFERENT instance than serviceB
    }
}
```

**Always use @Configuration for configuration classes!**

### Answer 3.3: @Value and SpEL

```java
// application.properties:
// commission.rate=0.15
// commission.enabled=true

@Component
public class Config {
    @Value("${commission.rate}")
    private BigDecimal rate1;  // = 0.15 (from properties file)

    @Value("${commission.bonus.rate:0.05}")
    private BigDecimal rate2;  // = 0.05 (property doesn't exist, uses default)

    @Value("#{${commission.rate} * 100}")
    private double percentage;  // = 15.0 (SpEL: 0.15 * 100)

    @Value("#{${commission.enabled} ? 'ON' : 'OFF'}")
    private String status;  // = "ON" (SpEL ternary: true ? 'ON' : 'OFF')
}
```

**Explanation:**
- `${}`: Property placeholder (from properties file)
- `#{}`: SpEL expression (evaluated at runtime)
- `:` after `${}`: Default value if property not found
- Can nest `${}` inside `#{}` to evaluate properties in SpEL

### Answer 3.4: Profiles

**Multi-Environment Database Configuration:**

```java
@Configuration
public class DatabaseConfig {

    @Bean
    @Profile("dev")
    public DataSource devDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:devdb");
        config.setUsername("sa");
        config.setPassword("");
        config.setDriverClassName("org.h2.Driver");
        return new HikariDataSource(config);
    }

    @Bean
    @Profile("test")
    public DataSource testDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:file:./testdb;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");
        config.setDriverClassName("org.h2.Driver");
        return new HikariDataSource(config);
    }

    @Bean
    @Profile("prod")
    public DataSource prodDataSource(
        @Value("${db.url}") String url,
        @Value("${db.username}") String username,
        @Value("${db.password}") String password) {

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);  // jdbc:postgresql://prod-server:5432/commissions
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("org.postgresql.Driver");
        config.setMaximumPoolSize(20);
        config.setConnectionTimeout(30000);
        return new HikariDataSource(config);
    }
}
```

**Activate profiles:**

```bash
# Run with dev profile
java -Dspring.profiles.active=dev -jar app.jar

# Run with test profile
mvn test -Dspring.profiles.active=test

# Run with prod profile
java -Dspring.profiles.active=prod -jar app.jar
```

**application-prod.properties:**
```properties
db.url=jdbc:postgresql://prod-server:5432/commissions
db.username=prod_user
db.password=secure_password
```

### Answer 3.5: Conditional Beans

**@ConditionalOnBean:**
- Bean is created ONLY IF another bean exists
- Used for beans that depend on presence of other beans

```java
@Bean
@ConditionalOnBean(DataSource.class)  // Only if DataSource exists
public JdbcTemplate jdbcTemplate(DataSource dataSource) {
    return new JdbcTemplate(dataSource);
}
```

**Use case:** Creating a bean that requires another bean to function

**@ConditionalOnMissingBean:**
- Bean is created ONLY IF another bean does NOT exist
- Used for fallback/default implementations

```java
@Bean
@ConditionalOnMissingBean(CacheManager.class)  // Only if CacheManager doesn't exist
public CacheManager defaultCacheManager() {
    return new InMemoryCacheManager();  // Fallback implementation
}
```

**Use case:** Providing default implementation that can be overridden

**Combined example:**

```java
@Configuration
public class CacheConfig {

    // Custom implementation (if user provides it)
    @Bean
    @ConditionalOnProperty(name = "cache.type", havingValue = "redis")
    public CacheManager redisCacheManager() {
        return new RedisCacheManager();
    }

    // Fallback (if custom not provided)
    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager defaultCacheManager() {
        return new InMemoryCacheManager();
    }

    // Only create this if CacheManager exists
    @Bean
    @ConditionalOnBean(CacheManager.class)
    public CacheStatisticsCollector cacheStats(CacheManager cacheManager) {
        return new CacheStatisticsCollector(cacheManager);
    }
}
```

### Answer 3.6: Mixing Configurations

**Yes, you can mix XML and Java configuration.**

**Import XML into Java:**

```java
@Configuration
@ImportResource("classpath:legacy-context.xml")  // Import XML
public class ModernConfig {

    @Autowired
    private LegacyService legacyService;  // From XML

    @Bean
    public NewService newService() {
        return new NewService(legacyService);  // Java bean using XML bean
    }
}
```

**legacy-context.xml:**
```xml
<beans>
    <bean id="legacyService" class="com.example.LegacyService">
        <property name="timeout" value="5000"/>
    </bean>
</beans>
```

**Import Java into XML:**

```xml
<beans>
    <!-- Import Java configuration -->
    <bean class="com.example.ModernConfig"/>

    <!-- Use beans from Java config -->
    <bean id="oldService" class="com.example.OldService">
        <property name="newService" ref="newService"/>  <!-- From Java config -->
    </bean>
</beans>
```

**Best practice:** Gradually migrate from XML to Java/annotations

---

## Section 4: AOP Fundamentals

### Answer 4.1: AOP Terminology

| Term | Definition | Match |
|------|------------|-------|
| Aspect | C. Modularization of a cross-cutting concern | C |
| Join Point | D. Point during execution (e.g., method call) | D |
| Advice | E. Action taken at a join point | E |
| Pointcut | A. Expression that matches join points | A |
| Target Object | B. Object being advised | B |
| Weaving | F. Process of applying aspects to target objects | F |

**Examples:**

- **Aspect:** `@Aspect` class containing logging advice
- **Join Point:** Method execution of `calculateCommission()`
- **Advice:** `@Before` method that logs parameters
- **Pointcut:** `execution(* calculate*(...))`
- **Target Object:** `CommissionService` instance
- **Weaving:** Creating proxy to wrap `CommissionService`

### Answer 4.2: Advice Types

**a) Log all method parameters before execution:**
```java
@Before("serviceMethods()")
public void logParameters(JoinPoint joinPoint) {
    System.out.println("Parameters: " + Arrays.toString(joinPoint.getArgs()));
}
```
**Why @Before:** Don't need to control execution, just log before it runs

**b) Measure execution time:**
```java
@Around("serviceMethods()")
public Object measureTime(ProceedingJoinPoint joinPoint) throws Throwable {
    long start = System.currentTimeMillis();
    Object result = joinPoint.proceed();
    long duration = System.currentTimeMillis() - start;
    System.out.println("Execution time: " + duration + "ms");
    return result;
}
```
**Why @Around:** Need to wrap execution to measure before and after

**c) Send notification after successful calculation:**
```java
@AfterReturning(pointcut = "calculateCommission()", returning = "result")
public void sendNotification(CommissionCalculation result) {
    notificationService.send("Commission calculated: " + result.getAmount());
}
```
**Why @AfterReturning:** Only execute if method succeeds, and need the result

**d) Log exceptions from service methods:**
```java
@AfterThrowing(pointcut = "serviceMethods()", throwing = "ex")
public void logException(JoinPoint joinPoint, Exception ex) {
    System.err.println("Exception in " + joinPoint.getSignature() + ": " + ex.getMessage());
}
```
**Why @AfterThrowing:** Specifically for exception handling

**e) Implement caching:**
```java
@Around("cacheable()")
public Object cache(ProceedingJoinPoint joinPoint) throws Throwable {
    String key = generateKey(joinPoint);
    if (cache.contains(key)) {
        return cache.get(key);  // Return without executing
    }
    Object result = joinPoint.proceed();
    cache.put(key, result);
    return result;
}
```
**Why @Around:** Need to conditionally skip execution and modify return value

**f) Execute cleanup regardless of outcome:**
```java
@After("serviceMethods()")
public void cleanup(JoinPoint joinPoint) {
    System.out.println("Cleaning up after " + joinPoint.getSignature());
    // Cleanup resources
}
```
**Why @After:** Executes always (like finally block), regardless of success/failure

### Answer 4.3: @Around Advice

**Problem:**

```java
@Around("execution(* calculateCommission(..))")
public Object cacheResult(ProceedingJoinPoint joinPoint) throws Throwable {
    String key = generateKey(joinPoint);
    if (cache.contains(key)) {
        return cache.get(key);
    }
    // ❌ MISSING: joinPoint.proceed()
    return null;
}
```

**Issues:**

1. **Method never executes** if cache misses
2. **Always returns null** for cache misses
3. **Result never cached** for future calls

**Fixed version:**

```java
@Around("execution(* calculateCommission(..))")
public Object cacheResult(ProceedingJoinPoint joinPoint) throws Throwable {
    String key = generateKey(joinPoint);

    // Check cache
    if (cache.contains(key)) {
        System.out.println("Cache HIT");
        return cache.get(key);
    }

    System.out.println("Cache MISS - executing method");

    // ✅ Execute the actual method
    Object result = joinPoint.proceed();

    // ✅ Store in cache
    cache.put(key, result);

    return result;
}
```

**Key rule:** @Around advice MUST call `joinPoint.proceed()` to execute the target method (unless intentionally preventing execution)

### Answer 4.4: Pointcut Expressions

**a) All methods in service package and sub-packages:**
```java
@Pointcut("execution(* com.chapman.edu.commissions.service..*(..))")
public void servicePackage() {}

// Explanation:
// execution(              - match method execution
//   *                     - any return type
//   com.chapman.edu.commissions.service.. - package and sub-packages
//   *                     - any class
//   (..)                  - any parameters
// )
```

**b) All methods starting with "calculate":**
```java
@Pointcut("execution(* calculate*(..))")
public void calculateMethods() {}

// Matches: calculate(), calculateCommission(), calculateBonus(), etc.
```

**c) All methods annotated with @Transactional:**
```java
@Pointcut("@annotation(org.springframework.transaction.annotation.Transactional)")
public void transactionalMethods() {}
```

**d) All public methods returning CommissionCalculation:**
```java
@Pointcut("execution(public com.chapman.edu.commissions.model.CommissionCalculation *(..))")
public void publicCommissionCalculationMethods() {}

// Explanation:
// public                          - public modifier
// com.chapman.edu...CommissionCalculation - return type
// *                               - any method name
// (..)                            - any parameters
```

**e) All methods in classes annotated with @Service:**
```java
@Pointcut("@within(org.springframework.stereotype.Service)")
public void serviceBeans() {}

// OR using within with target:
@Pointcut("within(@org.springframework.stereotype.Service *)")
public void serviceBeans2() {}
```

**f) All methods in beans whose names end with "Service":**
```java
@Pointcut("bean(*Service)")
public void serviceBeans() {}

// Matches beans: commissionService, dealService, notificationService, etc.
```

**Bonus - Combining pointcuts:**
```java
@Pointcut("servicePackage() && calculateMethods()")
public void serviceCalculations() {}

@Pointcut("serviceBeans() || repositoryBeans()")
public void dataAccessLayer() {}

@Pointcut("publicMethods() && !calculateMethods()")
public void publicNonCalculateMethods() {}
```

### Answer 4.5: Self-Invocation Problem

**The Problem:**

```java
@Service
public class CommissionService {

    @Transactional  // This will NOT work!
    public void processInternalCommission(Deal deal) {
        // Process commission
    }

    public void processBulkCommissions(List<Deal> deals) {
        for (Deal deal : deals) {
            // ❌ PROBLEM: Calling through 'this' bypasses proxy
            this.processInternalCommission(deal);
            // @Transactional aspect NOT applied!
        }
    }
}
```

**Why it happens:**
- Spring AOP uses proxies
- Proxy wraps your bean
- External calls go through proxy → aspects applied
- Internal calls (`this.method()`) bypass proxy → aspects NOT applied

**Visualization:**

```
External call:
Client → Proxy → @Transactional Aspect → Target.processInternalCommission() ✅

Internal call:
Client → Proxy → Target.processBulkCommissions()
                   ↓
                   this.processInternalCommission() ❌ (bypasses proxy)
```

**Solution 1: Self-Injection (Simple)**

```java
@Service
public class CommissionService {
    @Autowired
    private ApplicationContext context;

    @Transactional
    public void processInternalCommission(Deal deal) {
        // Process commission
    }

    public void processBulkCommissions(List<Deal> deals) {
        // Get proxy reference
        CommissionService self = context.getBean(CommissionService.class);

        for (Deal deal : deals) {
            self.processInternalCommission(deal);  // ✅ Goes through proxy
        }
    }
}
```

**Solution 2: Extract to Separate Class (Recommended)**

```java
@Service
public class CommissionProcessor {
    @Transactional
    public void processCommission(Deal deal) {
        // Process individual commission
    }
}

@Service
public class CommissionService {
    @Autowired
    private CommissionProcessor processor;

    public void processBulkCommissions(List<Deal> deals) {
        for (Deal deal : deals) {
            processor.processCommission(deal);  // ✅ External call through proxy
        }
    }
}
```

**Solution 3: AspectJ Weaving (Advanced)**

```java
// Use AspectJ compile-time or load-time weaving instead of Spring AOP
// No proxies involved - aspects woven directly into bytecode
@EnableLoadTimeWeaving
@Configuration
public class AspectJConfig { }
```

**Best Practice:** Solution 2 (separate class) - better design and clearer separation

### Answer 4.6: Proxy Mechanism

**a) Two types of proxies:**

1. **JDK Dynamic Proxy**
   - Uses `java.lang.reflect.Proxy`
   - Target must implement an interface
   - Proxy implements the same interface
   - Cannot proxy classes directly

2. **CGLIB Proxy**
   - Uses Code Generation Library
   - Creates subclass of target class
   - Can proxy classes without interfaces
   - Uses bytecode generation

**b) When Spring uses each:**

**JDK Dynamic Proxy (default when interface exists):**
```java
public interface NotificationService {
    void notify();
}

@Service
public class EmailService implements NotificationService {
    public void notify() { }
}

// Spring creates: Proxy implements NotificationService → delegates to EmailService
```

**CGLIB Proxy (when no interface or forced):**
```java
@Service
public class EmailService {  // No interface
    public void notify() { }
}

// Spring creates: Proxy extends EmailService → overrides methods
```

**c) CGLIB limitations:**

1. **Cannot proxy final classes:**
```java
@Service
public final class EmailService {  // ❌ Cannot create CGLIB proxy
    public void notify() { }
}
```

2. **Cannot proxy final methods:**
```java
@Service
public class EmailService {
    public final void notify() { }  // ❌ Cannot override final method
}
```

3. **Cannot proxy private methods:**
```java
@Service
public class EmailService {
    private void notify() { }  // ❌ Private methods not proxied
}
```

**d) Force CGLIB proxies:**

```java
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)  // Force CGLIB
public class AppConfig { }

// Or in Spring Boot:
spring.aop.proxy-target-class=true
```

**When to force CGLIB:**
- Need to inject as concrete class (not interface)
- Using class-based proxies consistently
- Some frameworks require CGLIB (e.g., certain transaction scenarios)

### Answer 4.7: Cross-Cutting Concerns

**1. Logging and Auditing**
```java
@Aspect
@Component
public class AuditingAspect {
    @Before("@annotation(Auditable)")
    public void audit(JoinPoint jp) {
        // Log who, what, when
    }
}
```
**Why AOP:** Logging needed across all layers; don't want to clutter every method with logging code

**2. Security and Authorization**
```java
@Aspect
@Component
public class SecurityAspect {
    @Around("@annotation(RequiresPermission)")
    public Object checkPermission(ProceedingJoinPoint jp) {
        // Verify user has permission
    }
}
```
**Why AOP:** Security checks needed on many methods; centralize security logic; easy to audit

**3. Transaction Management**
```java
@Transactional  // Spring's @Transactional uses AOP
public void saveData() { }
```
**Why AOP:** Transaction boundaries cross multiple methods; begin/commit/rollback logic is boilerplate

**4. Caching**
```java
@Aspect
@Component
public class CachingAspect {
    @Around("@annotation(Cacheable)")
    public Object cache(ProceedingJoinPoint jp) {
        // Check cache, execute if miss, store result
    }
}
```
**Why AOP:** Caching logic same for all methods; don't want cache code mixed with business logic

**5. Performance Monitoring**
```java
@Aspect
@Component
public class PerformanceAspect {
    @Around("serviceMethods()")
    public Object monitor(ProceedingJoinPoint jp) {
        // Measure execution time, log slow methods
    }
}
```
**Why AOP:** Monitoring needed everywhere; easy to enable/disable; centralized reporting

**6. Error Handling and Retry Logic**
```java
@Aspect
@Component
public class RetryAspect {
    @Around("@annotation(Retryable)")
    public Object retry(ProceedingJoinPoint jp) {
        // Retry on failure up to N times
    }
}
```
**Why AOP:** Retry logic is repetitive; separate technical concern from business logic

**Benefits of using AOP for these:**
- **Don't Repeat Yourself (DRY):** One place for cross-cutting logic
- **Separation of Concerns:** Business logic separate from infrastructure
- **Easy to Enable/Disable:** Remove aspect to disable feature
- **Consistent Application:** Can't forget to add logging/security
- **Easier Maintenance:** Change in one place affects all uses

---

## Section 5: Annotations & Component Scanning

### Answer 5.1: Stereotype Annotations

**@Component:**
```java
@Component
public class CommissionCalculator {
    // Generic Spring-managed component
}
```
**Use:** Generic component, utility classes, when no other stereotype fits

**@Service:**
```java
@Service
public class CommissionService {
    // Business logic
}
```
**Use:** Business service layer, contains business logic and orchestration

**@Repository:**
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
- **Extra functionality:** Automatic exception translation
  - Catches `SQLException`, `JPA exceptions`, etc.
  - Translates to Spring's `DataAccessException` hierarchy
  - Makes persistence technology agnostic

**@Controller / @RestController:**
```java
@RestController
public class CommissionController {
    @GetMapping("/commissions/{id}")
    public Commission get(@PathVariable String id) {
        // Web layer
    }
}
```
**Use:** Web/presentation layer, handles HTTP requests

**Relationships:**
- All are meta-annotations of `@Component`
- Functionally equivalent for bean registration
- Semantic meaning for architecture layers
- Future framework enhancements may add specific features

**@Repository additional functionality:**

```java
@Repository
public class JdbcDealRepository {
    @Autowired
    private JdbcTemplate jdbc;

    public Deal findById(String id) {
        try {
            return jdbc.queryForObject(
                "SELECT * FROM deals WHERE id = ?",
                new DealRowMapper(),
                id
            );
        } catch (EmptyResultDataAccessException ex) {
            // ✅ Spring automatically translates SQLException to DataAccessException
            throw new DealNotFoundException(id);
        }
    }
}
```

**Without @Repository, you'd get:**
```
SQLException: No data found
```

**With @Repository, you get:**
```
EmptyResultDataAccessException (subclass of DataAccessException)
```

### Answer 5.2: Component Scanning

**Analysis:**

```java
@Configuration
@ComponentScan(basePackages = "com")
public class AppConfig { }
```

**What it does:**
- Scans `com` package and ALL sub-packages
- Finds all classes annotated with `@Component`, `@Service`, `@Repository`, `@Controller`
- Registers them as beans in the ApplicationContext

**Problems:**

1. **Too Broad:**
   - Scans EVERY package starting with "com"
   - Includes third-party libraries (com.mysql, com.google, com.apache, etc.)
   - Unnecessary scanning slows startup

2. **Performance Impact:**
   - Thousands of classes scanned
   - Slow application startup
   - High memory usage during scanning

3. **Unexpected Bean Registration:**
   - May register beans from libraries you don't want
   - Potential conflicts with bean names
   - Hard to debug

**Better approach:**

```java
@Configuration
@ComponentScan(basePackages = {
    "com.chapman.edu.commissions.service",
    "com.chapman.edu.commissions.repository",
    "com.chapman.edu.commissions.controller"
})
public class AppConfig { }

// Or type-safe:
@ComponentScan(basePackageClasses = {
    CommissionService.class,  // Scans this package
    DealRepository.class       // Scans this package
})
public class AppConfig { }
```

**Best practice:**
- Be specific with packages
- Use `basePackageClasses` for type safety
- Exclude test packages

### Answer 5.3: @Autowired Resolution

**Scenario:**

```java
public interface NotificationService { }

@Component
public class EmailService implements NotificationService { }

@Component
public class SmsService implements NotificationService { }

@Service
public class OrderService {
    @Autowired
    private NotificationService service;  // What happens here?
}
```

**What happens:**
```
❌ ERROR: NoUniqueBeanDefinitionException
expected single matching bean but found 2: emailService, smsService
```

**Why:**
- Spring finds 2 beans of type `NotificationService`
- Doesn't know which to inject
- Throws exception at startup

**Solutions:**

**Solution 1: @Primary (mark default)**
```java
@Component
@Primary  // This one is preferred
public class EmailService implements NotificationService { }

@Component
public class SmsService implements NotificationService { }

// Now injection works - gets EmailService
```

**Solution 2: @Qualifier (specify which)**
```java
@Service
public class OrderService {
    @Autowired
    @Qualifier("emailService")  // Specify bean name
    private NotificationService service;
}
```

**Solution 3: Match bean name**
```java
@Service
public class OrderService {
    @Autowired
    private NotificationService emailService;  // Matches bean name
}
```

**Solution 4: Inject all (if you need both)**
```java
@Service
public class OrderService {
    @Autowired
    private List<NotificationService> services;  // Gets both
}
```

**Resolution order:**
1. Match by type
2. If multiple candidates → check for `@Primary`
3. If no `@Primary` → match by `@Qualifier`
4. If no `@Qualifier` → match by field/parameter name
5. If still ambiguous → throw exception

### Answer 5.4: Optional Dependencies

**Method 1: @Autowired(required = false)**
```java
@Service
public class CommissionService {
    private ValidationService validationService;

    @Autowired(required = false)
    public void setValidationService(ValidationService validationService) {
        this.validationService = validationService;
    }

    public void calculate(Deal deal) {
        if (validationService != null) {  // Check if present
            validationService.validate(deal);
        }
        // Continue with calculation
    }
}
```

**Method 2: Java 8 Optional**
```java
@Service
public class CommissionService {
    private final Optional<ValidationService> validationService;

    @Autowired
    public CommissionService(Optional<ValidationService> validationService) {
        this.validationService = validationService;
    }

    public void calculate(Deal deal) {
        validationService.ifPresent(vs -> vs.validate(deal));
        // Continue with calculation
    }
}
```

**Method 3: @Nullable (Spring's annotation)**
```java
@Service
public class CommissionService {
    private final ValidationService validationService;

    @Autowired
    public CommissionService(@Nullable ValidationService validationService) {
        this.validationService = validationService;  // Can be null
    }

    public void calculate(Deal deal) {
        if (validationService != null) {
            validationService.validate(deal);
        }
        // Continue with calculation
    }
}
```

**When to use optional dependencies:**

1. **Feature flags:**
   ```java
   // Caching is optional - app works without it
   @Autowired(required = false)
   private CacheManager cacheManager;
   ```

2. **Backward compatibility:**
   ```java
   // New service added - old code still works without it
   @Autowired(required = false)
   private NewFeatureService newFeature;
   ```

3. **Environmental differences:**
   ```java
   // Metrics only available in prod
   @Autowired(required = false)
   private MetricsCollector metrics;
   ```

4. **Graceful degradation:**
   ```java
   // Email service might not be configured in dev
   @Autowired(required = false)
   private EmailService emailService;
   ```

**Best practice:** Use constructor injection for required dependencies, optional for truly optional ones

### Answer 5.5: Collection Injection

**Scenario:**

```java
public interface NotificationService { }

@Component("emailService")
public class EmailService implements NotificationService { }

@Component("smsService")
public class SmsService implements NotificationService { }

@Service
public class NotificationManager {
    @Autowired
    private List<NotificationService> services;

    @Autowired
    private Map<String, NotificationService> serviceMap;
}
```

**What gets injected:**

**services List:**
```java
// List contains BOTH implementations
services = [
    EmailService instance,
    SmsService instance
]

// Usage:
public void notifyAll(String message) {
    for (NotificationService service : services) {
        service.notify(message);  // Calls both email and SMS
    }
}
```

**serviceMap Map:**
```java
// Map with bean names as keys
serviceMap = {
    "emailService" -> EmailService instance,
    "smsService" -> SmsService instance
}

// Usage:
public void notifyVia(String type, String message) {
    NotificationService service = serviceMap.get(type + "Service");
    if (service != null) {
        service.notify(message);
    }
}

notificationManager.notifyVia("email", "Hello");  // Uses email
notificationManager.notifyVia("sms", "Hello");    // Uses SMS
```

**Order in List:**
- Default: beans in arbitrary order
- Control with `@Order` annotation:

```java
@Component
@Order(1)  // First in list
public class EmailService implements NotificationService { }

@Component
@Order(2)  // Second in list
public class SmsService implements NotificationService { }
```

**Use cases:**
- **List:** Execute all implementations (broadcast notifications)
- **Map:** Choose implementation dynamically (strategy pattern)

### Answer 5.6: Custom Annotations

**a) Annotation definition:**

```java
package com.chapman.edu.commissions.corespring.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Limits the number of method calls per minute.
 * If limit exceeded, throws RateLimitExceededException.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {
    /**
     * Maximum calls per minute
     */
    int callsPerMinute() default 100;

    /**
     * Error message when limit exceeded
     */
    String message() default "Rate limit exceeded";
}
```

**b) Aspect implementation:**

```java
package com.chapman.edu.commissions.corespring.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Aspect
@Component
public class RateLimitingAspect {

    // Track calls per method per minute
    private final ConcurrentHashMap<String, RateLimitTracker> rateLimiters = new ConcurrentHashMap<>();

    @Around("@annotation(com.chapman.edu.commissions.corespring.annotations.RateLimited)")
    public Object enforceRateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        // Get method and annotation
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RateLimited rateLimited = method.getAnnotation(RateLimited.class);

        // Get or create rate limiter for this method
        String methodKey = method.getDeclaringClass().getName() + "." + method.getName();
        RateLimitTracker tracker = rateLimiters.computeIfAbsent(
            methodKey,
            k -> new RateLimitTracker(rateLimited.callsPerMinute())
        );

        // Check rate limit
        if (!tracker.allowRequest()) {
            throw new RateLimitExceededException(
                rateLimited.message() + " (max " + rateLimited.callsPerMinute() + " calls/min)"
            );
        }

        // Proceed with method execution
        return joinPoint.proceed();
    }

    /**
     * Tracks call count per minute with automatic reset
     */
    private static class RateLimitTracker {
        private final int maxCalls;
        private final AtomicInteger callCount = new AtomicInteger(0);
        private volatile long windowStart = System.currentTimeMillis();

        public RateLimitTracker(int maxCalls) {
            this.maxCalls = maxCalls;
        }

        public synchronized boolean allowRequest() {
            long now = System.currentTimeMillis();
            long elapsed = now - windowStart;

            // Reset window if minute elapsed
            if (elapsed > 60_000) {
                callCount.set(0);
                windowStart = now;
            }

            // Check if under limit
            int current = callCount.incrementAndGet();
            return current <= maxCalls;
        }
    }

    /**
     * Exception thrown when rate limit exceeded
     */
    public static class RateLimitExceededException extends RuntimeException {
        public RateLimitExceededException(String message) {
            super(message);
        }
    }
}
```

**c) Example usage:**

```java
@Service
public class CommissionService {

    /**
     * Limit expensive calculations to 10 per minute
     */
    @RateLimited(callsPerMinute = 10, message = "Too many calculation requests")
    public CommissionCalculation calculateComplexCommission(Deal deal) {
        // Expensive calculation
        return performComplexCalculation(deal);
    }

    /**
     * Limit API calls to 100 per minute
     */
    @RateLimited(callsPerMinute = 100)
    public List<Deal> getDeals() {
        // API call
        return dealRepository.findAll();
    }

    /**
     * No rate limit
     */
    public Deal getDeal(String id) {
        return dealRepository.findById(id);
    }
}
```

**Usage and behavior:**

```java
CommissionService service = context.getBean(CommissionService.class);

// First 10 calls succeed
for (int i = 0; i < 10; i++) {
    service.calculateComplexCommission(deal);  // ✅ Success
}

// 11th call within same minute fails
try {
    service.calculateComplexCommission(deal);
} catch (RateLimitExceededException ex) {
    System.out.println(ex.getMessage());  // "Too many calculation requests (max 10 calls/min)"
}

// After 1 minute, counter resets
Thread.sleep(60_000);
service.calculateComplexCommission(deal);  // ✅ Success again
```

---

## Section 6: Best Practices & Integration

### Answer 6.1: Constructor vs Field Injection

**Why constructor injection is preferred:**

**1. Immutability (final fields):**

```java
// ✅ Constructor injection - immutable
@Service
public class CommissionService {
    private final DealRepository repository;  // final = thread-safe

    public CommissionService(DealRepository repository) {
        this.repository = repository;
    }
}

// ❌ Field injection - mutable
@Service
public class CommissionService {
    @Autowired
    private DealRepository repository;  // Cannot be final
}
```

**2. Required dependencies are explicit:**

```java
// ✅ Constructor - clear what's required
public CommissionService(DealRepository repo, NotificationService notif) {
    // Constructor signature shows all dependencies
}

// ❌ Field - hidden dependencies
@Autowired private DealRepository repo;
@Autowired private NotificationService notif;
// Have to read entire class to find dependencies
```

**3. Easy to test:**

```java
// ✅ Constructor - easy unit testing
@Test
public void testCalculate() {
    DealRepository mockRepo = mock(DealRepository.class);
    NotificationService mockNotif = mock(NotificationService.class);

    // No Spring needed!
    CommissionService service = new CommissionService(mockRepo, mockNotif);

    service.calculate(deal);
    verify(mockNotif).notify();
}

// ❌ Field injection - requires Spring or reflection
@Test
public void testCalculate() {
    CommissionService service = new CommissionService();
    // How do I inject mocks? Need ReflectionTestUtils or Spring context
}
```

**4. Prevents partially constructed objects:**

```java
// ✅ Constructor - object fully constructed
CommissionService service = new CommissionService(repo, notif);
// All dependencies set, safe to use immediately

// ❌ Field injection - object constructed before injection
CommissionService service = new CommissionService();
// Temporarily in invalid state before Spring injects fields
// If someone calls methods before injection completes → NullPointerException
```

**5. Compiler enforces dependencies:**

```java
// ✅ Constructor - won't compile without dependencies
new CommissionService();  // ❌ Compile error - missing parameters

// ❌ Field injection - compiles fine
new CommissionService();  // ✅ Compiles, but dependencies are null!
```

**6. Encourages better design:**

```java
// ❌ Field injection - too many dependencies (code smell)
@Service
public class GodService {
    @Autowired private Dep1 dep1;
    @Autowired private Dep2 dep2;
    @Autowired private Dep3 dep3;
    // ... 20 more fields
    // Easy to add more - doesn't look bad
}

// ✅ Constructor injection - obvious code smell
public GodService(Dep1 dep1, Dep2 dep2, Dep3 dep3, ... Dep20 dep20) {
    // Constructor is huge! Clear signal to refactor
}
```

**Summary comparison:**

| Aspect | Constructor | Field |
|--------|-------------|-------|
| Immutability | ✅ final fields | ❌ Cannot use final |
| Testability | ✅ No Spring needed | ❌ Requires Spring or reflection |
| Explicitness | ✅ Dependencies in signature | ❌ Hidden in class body |
| Null safety | ✅ Cannot be null | ❌ Can be null |
| Code smell detection | ✅ Large constructor is obvious | ❌ Many fields look OK |

**Recommendation:** Always use constructor injection in production code

### Answer 6.2: Circular Dependency

**a) What is a circular dependency:**

Two or more beans depend on each other, forming a cycle:
- Bean A depends on Bean B
- Bean B depends on Bean A

**b) Example:**

```java
@Service
public class CommissionService {
    private final DealService dealService;

    @Autowired
    public CommissionService(DealService dealService) {
        this.dealService = dealService;
    }

    public void calculateCommission(Deal deal) {
        dealService.updateDealStatus(deal);
    }
}

@Service
public class DealService {
    private final CommissionService commissionService;

    @Autowired
    public DealService(CommissionService commissionService) {
        this.commissionService = commissionService;
    }

    public void createDeal(Deal deal) {
        commissionService.calculateCommission(deal);
    }
}

// ❌ Error at startup:
// BeanCurrentlyInCreationException: Circular dependency
```

**c) Why it's a design problem:**

1. **Violates Single Responsibility:**
   - If A needs B and B needs A, they're too tightly coupled
   - Should be one service or have separate responsibilities

2. **Hard to understand:**
   - Difficult to reason about initialization order
   - Changes to one affect the other

3. **Testing nightmare:**
   - Cannot test one without the other
   - Mocking becomes complex

4. **Indicates poor design:**
   - Usually means responsibilities are not well separated
   - Often a sign of missing abstraction

**d) Resolve without @Lazy:**

**Solution 1: Extract common functionality**

```java
// Extract shared logic to new service
@Service
public class DealProcessor {
    public void processDeal(Deal deal) {
        // Common processing logic
    }
}

@Service
public class CommissionService {
    private final DealProcessor processor;

    @Autowired
    public CommissionService(DealProcessor processor) {
        this.processor = processor;
    }
}

@Service
public class DealService {
    private final DealProcessor processor;

    @Autowired
    public DealService(DealProcessor processor) {
        this.processor = processor;
    }
}
```

**Solution 2: Use events (decouple)**

```java
@Service
public class DealService {
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public void createDeal(Deal deal) {
        dealRepository.save(deal);
        eventPublisher.publishEvent(new DealCreatedEvent(deal));
        // No direct dependency on CommissionService
    }
}

@Service
public class CommissionService {
    @EventListener
    public void handleDealCreated(DealCreatedEvent event) {
        calculateCommission(event.getDeal());
    }
}
```

**Solution 3: Introduce interface/abstraction**

```java
// Interface
public interface DealNotifier {
    void notifyDealUpdated(Deal deal);
}

@Service
public class CommissionService implements DealNotifier {
    // No dependency on DealService

    @Override
    public void notifyDealUpdated(Deal deal) {
        calculateCommission(deal);
    }
}

@Service
public class DealService {
    private final DealNotifier notifier;  // Dependency on abstraction

    @Autowired
    public DealService(DealNotifier notifier) {
        this.notifier = notifier;
    }
}
```

**Solution 4: Refactor to eliminate circular logic**

```java
// Often circular dependency means poor separation
// Merge into one service if they're that tightly coupled

@Service
public class DealCommissionService {
    public void createDealAndCalculateCommission(Deal deal) {
        dealRepository.save(deal);
        CommissionCalculation calc = calculateCommission(deal);
        commissionRepository.save(calc);
    }
}
```

**Best practice:** Circular dependencies indicate design smell - refactor to eliminate them

### Answer 6.3: Singleton Thread Safety

**Problem:**

```java
@Service  // Singleton scope
public class CommissionCalculator {
    private BigDecimal runningTotal = BigDecimal.ZERO;  // ❌ Shared mutable state!

    public CommissionCalculation calculate(Deal deal) {
        runningTotal = runningTotal.add(deal.getValue());
        BigDecimal commission = runningTotal.multiply(new BigDecimal("0.10"));
        return new CommissionCalculation(deal.getId(), commission);
    }
}
```

**Thread-safety issues:**

```java
// Thread 1: calculate(Deal value=100)
runningTotal = 0 + 100 = 100
commission = 100 * 0.10 = 10  // Expects 10

// Thread 2: calculate(Deal value=200) BEFORE Thread 1 finishes
runningTotal = 100 + 200 = 300  // ❌ Includes Thread 1's value!
commission = 300 * 0.10 = 30  // Expected 20, got 30!

// Thread 1 continues:
commission = 300 * 0.10 = 30  // ❌ Expected 10, got 30!
```

**Race condition results:**
- Incorrect calculations
- Unpredictable results
- Hard to debug (works sometimes, fails randomly)

**Solution 1: Make singleton stateless (RECOMMENDED)**

```java
@Service  // Singleton
public class CommissionCalculator {
    // ✅ No mutable state - thread-safe

    public CommissionCalculation calculate(Deal deal) {
        BigDecimal commission = deal.getValue().multiply(new BigDecimal("0.10"));
        return new CommissionCalculation(deal.getId(), commission);
    }

    // If you need accumulated total, pass it as parameter
    public BigDecimal calculateWithRunningTotal(Deal deal, BigDecimal currentTotal) {
        return currentTotal.add(deal.getValue().multiply(new BigDecimal("0.10")));
    }
}
```

**Solution 2: Use prototype scope (if truly need state)**

```java
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)  // New instance each time
public class CommissionCalculator {
    private BigDecimal runningTotal = BigDecimal.ZERO;  // ✅ Each instance has own state

    public CommissionCalculation calculate(Deal deal) {
        runningTotal = runningTotal.add(deal.getValue());
        BigDecimal commission = runningTotal.multiply(new BigDecimal("0.10"));
        return new CommissionCalculation(deal.getId(), commission);
    }
}
```

**Solution 3: Use ThreadLocal (if state is per-thread)**

```java
@Service
public class CommissionCalculator {
    private final ThreadLocal<BigDecimal> runningTotal =
        ThreadLocal.withInitial(() -> BigDecimal.ZERO);  // ✅ One per thread

    public CommissionCalculation calculate(Deal deal) {
        BigDecimal current = runningTotal.get();
        current = current.add(deal.getValue());
        runningTotal.set(current);

        BigDecimal commission = current.multiply(new BigDecimal("0.10"));
        return new CommissionCalculation(deal.getId(), commission);
    }

    public void reset() {
        runningTotal.remove();  // Clean up when done
    }
}
```

**Solution 4: Synchronization (if must have shared state)**

```java
@Service
public class CommissionCalculator {
    private BigDecimal runningTotal = BigDecimal.ZERO;

    public synchronized CommissionCalculation calculate(Deal deal) {  // ✅ Thread-safe
        runningTotal = runningTotal.add(deal.getValue());
        BigDecimal commission = runningTotal.multiply(new BigDecimal("0.10"));
        return new CommissionCalculation(deal.getId(), commission);
    }
}
// ⚠️ Downside: Performance bottleneck (only one thread at a time)
```

**Best practice:** Solution 1 - keep singleton beans stateless

### Answer 6.4: Testing with DI

**Unit test without Spring container:**

```java
package com.chapman.edu.commissions.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class CommissionServiceTest {

    @Mock  // Mockito creates mock
    private DealRepository dealRepository;

    @Mock
    private CommissionCalculator calculator;

    @InjectMocks  // Mockito injects mocks into constructor
    private CommissionService commissionService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);  // Initialize mocks
    }

    @Test
    public void testProcessCommission_Success() {
        // Arrange
        String dealId = "deal-123";
        Deal deal = new Deal("Test Deal", new BigDecimal("1000"), "rep-1");
        deal.setId(dealId);

        CommissionCalculation expectedCalc = new CommissionCalculation(
            dealId, "rep-1", new BigDecimal("100")
        );

        // Mock behavior
        when(dealRepository.findById(dealId)).thenReturn(deal);
        when(calculator.calculate(deal)).thenReturn(expectedCalc);

        // Act
        CommissionCalculation result = commissionService.processCommission(dealId);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("100"), result.getBaseCommission());

        // Verify interactions
        verify(dealRepository, times(1)).findById(dealId);
        verify(calculator, times(1)).calculate(deal);
    }

    @Test
    public void testProcessCommission_DealNotFound() {
        // Arrange
        String dealId = "nonexistent";
        when(dealRepository.findById(dealId)).thenReturn(null);

        // Act & Assert
        assertThrows(DealNotFoundException.class, () -> {
            commissionService.processCommission(dealId);
        });

        // Verify calculator never called
        verify(calculator, never()).calculate(any());
    }
}
```

**Alternative: Manual mock creation (no Mockito):**

```java
public class CommissionServiceTest {

    private CommissionService commissionService;
    private MockDealRepository dealRepository;
    private MockCalculator calculator;

    @BeforeEach
    public void setup() {
        dealRepository = new MockDealRepository();
        calculator = new MockCalculator();

        // ✅ Constructor injection makes this easy!
        commissionService = new CommissionService(dealRepository, calculator);
    }

    @Test
    public void testProcessCommission() {
        // Arrange
        Deal deal = new Deal("Test", new BigDecimal("1000"), "rep-1");
        dealRepository.addDeal("deal-1", deal);
        calculator.setReturnValue(new CommissionCalculation("deal-1", "rep-1", new BigDecimal("100")));

        // Act
        CommissionCalculation result = commissionService.processCommission("deal-1");

        // Assert
        assertEquals(new BigDecimal("100"), result.getBaseCommission());
        assertTrue(calculator.wasCalculateCalled());
    }

    // Simple mock implementations
    private static class MockDealRepository implements DealRepository {
        private Map<String, Deal> deals = new HashMap<>();

        public void addDeal(String id, Deal deal) {
            deals.put(id, deal);
        }

        @Override
        public Deal findById(String id) {
            return deals.get(id);
        }
    }

    private static class MockCalculator implements CommissionCalculator {
        private CommissionCalculation returnValue;
        private boolean calculateCalled = false;

        public void setReturnValue(CommissionCalculation calc) {
            this.returnValue = calc;
        }

        @Override
        public CommissionCalculation calculate(Deal deal) {
            calculateCalled = true;
            return returnValue;
        }

        public boolean wasCalculateCalled() {
            return calculateCalled;
        }
    }
}
```

**Key points:**
- ✅ No Spring container needed (fast tests)
- ✅ Complete control over dependencies
- ✅ Constructor injection makes mocking trivial
- ✅ Tests run in milliseconds
- ✅ Can test in isolation

**Comparison with field injection:**

```java
// ❌ With field injection - harder to test
@Service
public class CommissionService {
    @Autowired
    private DealRepository dealRepository;  // How to inject mock?

    // Options:
    // 1. Use Spring TestContext (slow)
    // 2. Use ReflectionTestUtils (brittle)
    // 3. Add setter just for testing (pollutes API)
}
```

**Best practice:** Constructor injection enables fast, simple unit tests

### Answer 6.5: Performance Optimization

**Problem:**

```java
@Configuration
@ComponentScan("com.chapman")  // Too broad, scans everything
public class AppConfig { }
```

**Issues:**
- Scans thousands of classes (including libraries)
- Slow application startup
- Unnecessary memory usage
- Potential bean name conflicts

**Solution 1: Specific packages**

```java
@Configuration
@ComponentScan(basePackages = {
    "com.chapman.edu.commissions.service",
    "com.chapman.edu.commissions.repository",
    "com.chapman.edu.commissions.controller"
})
public class AppConfig { }
```

**Solution 2: Type-safe scanning (recommended)**

```java
@Configuration
@ComponentScan(basePackageClasses = {
    CommissionService.class,      // Scans this package
    DealRepository.class,          // Scans this package
    CommissionController.class     // Scans this package
})
public class AppConfig { }
```

**Benefits:**
- ✅ Refactor-safe (class rename doesn't break config)
- ✅ Compile-time checking
- ✅ IDE support (click to navigate)

**Solution 3: Exclude specific packages**

```java
@Configuration
@ComponentScan(
    basePackages = "com.chapman.edu.commissions",
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com.chapman.edu.commissions.legacy.*"
    )
)
public class AppConfig { }
```

**Solution 4: Use @Import for explicit configuration**

```java
@Configuration
@Import({
    ServiceConfig.class,
    RepositoryConfig.class,
    ControllerConfig.class
})
public class AppConfig {
    // No component scanning - all beans explicitly configured
}
```

**Performance comparison:**

```
@ComponentScan("com")
- Scans: 10,000+ classes
- Startup time: 15 seconds
- Memory: 200 MB

@ComponentScan(basePackages = "com.chapman.edu.commissions.service")
- Scans: 50 classes
- Startup time: 2 seconds ✅
- Memory: 50 MB ✅
```

**Best practices:**
1. Be as specific as possible
2. Use `basePackageClasses` for type safety
3. Exclude test packages and legacy code
4. Consider explicit configuration for critical beans
5. Profile startup time to identify issues

### Answer 6.6: Bean Creation Timing

**a) When are singleton beans created by default?**

**Answer: During ApplicationContext initialization (eager loading)**

```java
@Configuration
public class AppConfig {
    @Bean
    public ExpensiveService expensiveService() {
        System.out.println("Creating ExpensiveService...");
        return new ExpensiveService();
    }
}

// On startup:
ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
// Output: "Creating ExpensiveService..." (created immediately)
```

**b) How to defer creation until first use?**

**Answer: Use @Lazy annotation**

```java
@Bean
@Lazy  // Don't create until first requested
public ExpensiveService expensiveService() {
    System.out.println("Creating ExpensiveService...");
    return new ExpensiveService();
}

// Startup - no output (not created)
ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

// First use - now it's created
ExpensiveService service = context.getBean(ExpensiveService.class);
// Output: "Creating ExpensiveService..."
```

**Can also use on injection point:**

```java
@Service
public class MyService {
    @Autowired
    @Lazy  // Injects proxy, actual bean created on first method call
    private ExpensiveService expensiveService;

    public void doSomething() {
        expensiveService.process();  // NOW the real bean is created
    }
}
```

**c) When are prototype beans created?**

**Answer: Each time the bean is requested (always lazy)**

```java
@Bean
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public ReportGenerator reportGenerator() {
    System.out.println("Creating ReportGenerator...");
    return new ReportGenerator();
}

// Startup - no output (prototypes never created at startup)
ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

// Each request creates new instance
ReportGenerator gen1 = context.getBean(ReportGenerator.class);  // Output: "Creating..."
ReportGenerator gen2 = context.getBean(ReportGenerator.class);  // Output: "Creating..." again
```

**d) Ensure bean is created at startup even if not used?**

**Answer: Use @DependsOn or @Lazy(false) (default for singleton)**

**Option 1: @DependsOn forces creation**

```java
@Configuration
public class AppConfig {

    @Bean
    public InitializationService initService() {
        System.out.println("Initializing...");
        return new InitializationService();
    }

    @Bean
    @DependsOn("initService")  // Forces initService to be created first
    public MainService mainService() {
        return new MainService();
    }
}
```

**Option 2: Inject into another bean**

```java
@Configuration
public class AppConfig {

    @Bean
    public StartupBean startupBean() {
        return new StartupBean();
    }

    @Bean
    public ApplicationRunner runner(StartupBean startupBean) {
        // Injecting ensures startupBean is created
        return args -> {
            System.out.println("Application started");
        };
    }
}
```

**Option 3: Implement ApplicationListener**

```java
@Component
public class StartupListener implements ApplicationListener<ContextRefreshedEvent> {

    public StartupListener() {
        System.out.println("StartupListener created");
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        System.out.println("Context refreshed - perform startup tasks");
    }
}
```

**Summary:**

| Bean Type | Default Creation | Force Early Creation | Defer Creation |
|-----------|------------------|---------------------|----------------|
| Singleton | Startup (eager) | @DependsOn, inject in another bean | @Lazy |
| Prototype | On request (lazy) | N/A (always lazy) | N/A |
| Request | Per HTTP request | N/A | N/A |
| Session | Per HTTP session | N/A | N/A |

---

Due to length constraints, I'll continue with the remaining sections. Would you like me to continue with Sections 7-10 in the next response?

**Sections remaining:**
- Section 7: Advanced Scenarios
- Section 8: Practical Application
- Section 9: True/False Questions
- Section 10: Code Review Questions

I'll continue creating the answers and then create the runnable Processor demo files.
