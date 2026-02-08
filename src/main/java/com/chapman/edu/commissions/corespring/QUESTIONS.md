# Spring Core Concepts - Study Questions

This document contains questions to test your understanding of Dependency Injection, IoC, Spring Core Container, AOP, and related concepts.

---

## Section 1: Dependency Injection & IoC Concepts

### Question 1.1: Understanding the Problem
**Question:** What are the three main problems with the following code?

```java
public class CommissionService {
    private EmailService emailService;

    public CommissionService() {
        this.emailService = new EmailService();
    }

    public void processCommission(Deal deal) {
        // ... calculate commission
        emailService.send("Commission calculated");
    }
}
```

### Question 1.2: IoC vs DI
**Question:** Explain the difference between Inversion of Control (IoC) and Dependency Injection (DI). Is DI a type of IoC, or is IoC a type of DI?

### Question 1.3: DIP vs DI
**Question:** What is the difference between the Dependency Inversion Principle (DIP) and Dependency Injection (DI)? Provide a code example showing DIP in action.

### Question 1.4: Injection Types
**Question:** Compare and contrast the three types of dependency injection (Constructor, Setter, Field). Which is recommended and why? When would you use each type?

### Question 1.5: Benefits of DI
**Question:** List and explain at least 5 benefits of using Dependency Injection. Provide a concrete example for each benefit.

### Question 1.6: Anti-Patterns
**Question:** Identify the anti-pattern in this code and explain why it's problematic:

```java
@Service
public class CommissionCalculator {
    public BigDecimal calculate(Deal deal) {
        NotificationService service = ServiceLocator.get(NotificationService.class);
        service.notify();
        return deal.getValue().multiply(new BigDecimal("0.10"));
    }
}
```

---

## Section 2: Spring Core Container

### Question 2.1: Bean Scopes
**Question:** Fill in the table:

| Scope | Instances Per Container | When Created | When Destroyed | Thread-Safe Required? | Use Case |
|-------|------------------------|--------------|----------------|----------------------|----------|
| singleton | ? | ? | ? | ? | ? |
| prototype | ? | ? | ? | ? | ? |
| request | ? | ? | ? | ? | ? |
| session | ? | ? | ? | ? | ? |

### Question 2.2: Scope Selection
**Question:** For each scenario, choose the appropriate bean scope and justify your choice:

a) A service that calculates commissions using stateless operations
b) A shopping cart object that stores items for a user
c) A report generator that accumulates data before producing output
d) A logging service that writes to a file
e) A user context object storing the current user's information for a web request

### Question 2.3: Bean Lifecycle
**Question:** Place the following lifecycle events in the correct order:

- [ ] @PreDestroy
- [ ] BeanPostProcessor.postProcessAfterInitialization()
- [ ] Constructor
- [ ] Custom destroy-method
- [ ] @PostConstruct
- [ ] Dependency Injection
- [ ] InitializingBean.afterPropertiesSet()
- [ ] BeanPostProcessor.postProcessBeforeInitialization()
- [ ] Custom init-method
- [ ] DisposableBean.destroy()
- [ ] Bean is ready to use

### Question 2.4: Prototype Scope Pitfall
**Question:** What is wrong with this code? What will happen when the application shuts down?

```java
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ReportGenerator {
    private File tempFile;

    @PostConstruct
    public void init() {
        tempFile = File.createTempFile("report", ".tmp");
    }

    @PreDestroy
    public void cleanup() {
        tempFile.delete();  // Will this execute?
    }
}
```

### Question 2.5: BeanPostProcessor
**Question:** Explain what a BeanPostProcessor is and when you would use it. What is the difference between `postProcessBeforeInitialization()` and `postProcessAfterInitialization()`? Which one is used by Spring AOP to create proxies?

### Question 2.6: @Primary vs @Qualifier
**Question:** Given this configuration, what will be injected in each case?

```java
@Service("emailService")
@Primary
public class EmailNotificationService implements NotificationService { }

@Service("smsService")
public class SmsNotificationService implements NotificationService { }

@Service
public class CommissionService {
    @Autowired
    private NotificationService service1;  // Which implementation?

    @Autowired
    @Qualifier("smsService")
    private NotificationService service2;  // Which implementation?

    @Autowired
    @Qualifier("emailService")
    private NotificationService service3;  // Which implementation?
}
```

---

## Section 3: Application Context & Configuration

### Question 3.1: Configuration Approaches
**Question:** Compare XML, Java, and Annotation-based configuration. List the pros and cons of each approach. When would you use each?

### Question 3.2: @Configuration vs @Component
**Question:** What is the difference between a class annotated with `@Configuration` and one annotated with `@Component`? What happens if you use `@Component` instead of `@Configuration` on a configuration class?

### Question 3.3: @Value and SpEL
**Question:** What will be the value of each field?

```java
// application.properties:
// commission.rate=0.15
// commission.enabled=true

@Component
public class Config {
    @Value("${commission.rate}")
    private BigDecimal rate1;  // ?

    @Value("${commission.bonus.rate:0.05}")
    private BigDecimal rate2;  // ? (property doesn't exist)

    @Value("#{${commission.rate} * 100}")
    private double percentage;  // ?

    @Value("#{${commission.enabled} ? 'ON' : 'OFF'}")
    private String status;  // ?
}
```

### Question 3.4: Profiles
**Question:** Design a profile configuration for a database connection that supports three environments: dev (H2 in-memory), test (H2 file-based), and prod (PostgreSQL). Show the code for all three profiles.

### Question 3.5: Conditional Beans
**Question:** What is the difference between `@ConditionalOnBean` and `@ConditionalOnMissingBean`? Provide a use case for each.

### Question 3.6: Mixing Configurations
**Question:** Is it possible to mix XML and Java configuration? If so, how? Show an example of importing XML configuration into a Java configuration class.

---

## Section 4: AOP Fundamentals

### Question 4.1: AOP Terminology
**Question:** Match the AOP term with its definition:

| Term | Definition |
|------|------------|
| Aspect | A. Expression that matches join points |
| Join Point | B. Object being advised |
| Advice | C. Modularization of a cross-cutting concern |
| Pointcut | D. Point during execution (e.g., method call) |
| Target Object | E. Action taken at a join point |
| Weaving | F. Process of applying aspects to target objects |

### Question 4.2: Advice Types
**Question:** For each scenario, choose the most appropriate advice type and explain why:

a) Log all method parameters before a method executes
b) Measure the execution time of a method
c) Send a notification after a successful commission calculation
d) Log exceptions thrown by any service method
e) Implement a caching mechanism (check cache before, store result after)
f) Execute cleanup code after a method regardless of success or failure

### Question 4.3: @Around Advice
**Question:** What is wrong with this @Around advice?

```java
@Around("execution(* calculateCommission(..))")
public Object cacheResult(ProceedingJoinPoint joinPoint) throws Throwable {
    String key = generateKey(joinPoint);
    if (cache.contains(key)) {
        return cache.get(key);
    }
    // Missing something here?
    return null;
}
```

### Question 4.4: Pointcut Expressions
**Question:** Write pointcut expressions for the following scenarios:

a) All methods in classes within the `com.chapman.edu.commissions.service` package and sub-packages
b) All methods starting with "calculate"
c) All methods annotated with `@Transactional`
d) All public methods that return `CommissionCalculation`
e) All methods in classes annotated with `@Service`
f) All methods in beans whose names end with "Service"

### Question 4.5: Self-Invocation Problem
**Question:** Explain the self-invocation problem in Spring AOP. Why does this happen? Show code demonstrating the problem and provide two solutions.

### Question 4.6: Proxy Mechanism
**Question:**
a) What are the two types of proxies Spring AOP can create?
b) When does Spring use each type?
c) What is the limitation of CGLIB proxies?
d) How do you force Spring to use CGLIB proxies even for interfaces?

### Question 4.7: Cross-Cutting Concerns
**Question:** List 6 examples of cross-cutting concerns that are good candidates for AOP. For each, explain why it's better implemented as an aspect rather than scattered throughout the codebase.

---

## Section 5: Annotations & Component Scanning

### Question 5.1: Stereotype Annotations
**Question:** What is the difference between `@Component`, `@Service`, `@Repository`, and `@Controller`? When should you use each? What additional functionality does `@Repository` provide?

### Question 5.2: Component Scanning
**Question:** What does this configuration do? Is there anything potentially problematic?

```java
@Configuration
@ComponentScan(basePackages = "com")
public class AppConfig { }
```

### Question 5.3: @Autowired Resolution
**Question:** Given the following, what happens in each injection scenario?

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

How would you fix this?

### Question 5.4: Optional Dependencies
**Question:** Show three different ways to declare an optional dependency in Spring. Explain when you might want an optional dependency.

### Question 5.5: Collection Injection
**Question:** What will be injected into each field?

```java
public interface NotificationService { }

@Component("emailService")
public class EmailService implements NotificationService { }

@Component("smsService")
public class SmsService implements NotificationService { }

@Service
public class NotificationManager {
    @Autowired
    private List<NotificationService> services;  // What's in this list?

    @Autowired
    private Map<String, NotificationService> serviceMap;  // What's in this map?
}
```

### Question 5.6: Custom Annotations
**Question:** Create a custom annotation `@RateLimited` that can be used on methods to limit the number of calls per minute. Show:
a) The annotation definition
b) An aspect that enforces the rate limiting
c) Example usage on a service method

---

## Section 6: Best Practices & Integration

### Question 6.1: Constructor vs Field Injection
**Question:** Why is constructor injection preferred over field injection? List at least 4 specific reasons with examples.

### Question 6.2: Circular Dependency
**Question:**
a) What is a circular dependency?
b) Show an example of a circular dependency
c) Why is it a design problem?
d) How can you resolve it without using `@Lazy`?

### Question 6.3: Singleton Thread Safety
**Question:** Identify the thread-safety issue in this code and fix it:

```java
@Service  // Singleton scope
public class CommissionCalculator {
    private BigDecimal runningTotal = BigDecimal.ZERO;

    public CommissionCalculation calculate(Deal deal) {
        runningTotal = runningTotal.add(deal.getValue());
        BigDecimal commission = runningTotal.multiply(new BigDecimal("0.10"));
        return new CommissionCalculation(deal.getId(), commission);
    }
}
```

### Question 6.4: Testing with DI
**Question:** Show how to write a unit test for this service without starting a Spring container:

```java
@Service
public class CommissionService {
    private final DealRepository dealRepository;
    private final CommissionCalculator calculator;

    public CommissionService(DealRepository dealRepository,
                            CommissionCalculator calculator) {
        this.dealRepository = dealRepository;
        this.calculator = calculator;
    }

    public CommissionCalculation processCommission(String dealId) {
        Deal deal = dealRepository.findById(dealId);
        return calculator.calculate(deal);
    }
}
```

### Question 6.5: Performance Optimization
**Question:** You have a configuration class that scans a large package hierarchy, causing slow startup times. How would you optimize this?

```java
@Configuration
@ComponentScan("com.chapman")  // Too broad, scans everything
public class AppConfig { }
```

### Question 6.6: Bean Creation Timing
**Question:**
a) When are singleton beans created by default?
b) How can you defer creation until first use?
c) When are prototype beans created?
d) What annotation would you use to ensure a bean is created at startup even if not used?

---

## Section 7: Advanced Scenarios

### Question 7.1: Aspect Ordering
**Question:** You have two aspects that both advise the same method. How do you control the order in which they execute? Show the code.

### Question 7.2: Dynamic Bean Registration
**Question:** Is it possible to register beans programmatically at runtime (not through configuration)? If so, how?

### Question 7.3: Conditional on Profile
**Question:** Create a configuration that:
- Uses an in-memory cache in dev and test profiles
- Uses Redis cache in prod profile
- Uses no cache if no profile is active (default)

### Question 7.4: Multiple ApplicationContexts
**Question:** In a Spring MVC application, you typically have a parent ApplicationContext (root) and child ApplicationContext (servlet). What is the benefit of this hierarchy? Can beans in the child context access beans in the parent context? Vice versa?

### Question 7.5: Lifecycle Callbacks Comparison
**Question:** You need to initialize a bean after all dependencies are injected. You have three options:
- `@PostConstruct`
- `InitializingBean.afterPropertiesSet()`
- `@Bean(initMethod = "init")`

Which should you choose and why? When would you use the other options?

### Question 7.6: Aspect on Multiple Interfaces
**Question:** Write a pointcut expression that matches methods from two different interfaces:
- All methods in `NotificationService` interface
- All methods in `AuditLogger` interface

---

## Section 8: Practical Application

### Question 8.1: Design Challenge
**Question:** Design a commission calculation system using Spring that includes:
- Multiple calculation strategies (percentage, tiered, bonus-based)
- Audit logging for all calculations
- Notifications after successful calculations
- Different configurations for dev and prod environments
- Caching of calculation results

Show the class structure, annotations, and configuration. Explain your design choices.

### Question 8.2: Refactoring Exercise
**Question:** Refactor this tightly-coupled code to use proper dependency injection and Spring best practices:

```java
public class CommissionProcessor {
    private EmailSender emailSender = new EmailSender();
    private Logger logger = LoggerFactory.getLogger(CommissionProcessor.class);
    private static CommissionCalculator calculator = new StandardCalculator();

    public void process(Deal deal) {
        logger.info("Processing deal: " + deal.getId());
        BigDecimal commission = calculator.calculate(deal);
        emailSender.send("Commission calculated: " + commission);
    }
}
```

### Question 8.3: AOP Design
**Question:** You need to implement the following requirements using AOP:
1. Log all method calls in the service layer with parameters and execution time
2. Require permission checks on sensitive operations
3. Automatically retry failed database operations up to 3 times
4. Cache results of expensive calculations for 5 minutes

Design the aspects needed and show the pointcut expressions.

### Question 8.4: Testing Strategy
**Question:** Design a testing strategy for a Spring application that includes:
- Unit tests (no Spring container)
- Integration tests (with Spring context)
- Tests for different profiles
- Tests that verify AOP aspects are applied

Show example test classes for each category.

### Question 8.5: Debugging Challenge
**Question:** An aspect is not being applied to a method. List 5 possible reasons why and how to verify/fix each one.

---

## Section 9: True/False Questions

### Question 9.1
**Question:** True or False? Explain your answer.

a) Spring creates singleton beans lazily by default
b) @PreDestroy is called for prototype-scoped beans
c) Field injection is the recommended approach for dependency injection
d) @Around advice must call `proceed()` on the ProceedingJoinPoint
e) @Primary takes precedence over @Qualifier
f) BeanPostProcessor is applied to all beans in the ApplicationContext
g) Circular dependencies can always be resolved using @Lazy
h) Spring AOP can intercept private methods
i) @Value can only inject strings
j) Constructor injection allows for immutable dependencies

### Question 9.2
**Question:** True or False? Explain your answer.

a) XML configuration is deprecated and should never be used
b) A class annotated with @Configuration uses CGLIB proxies by default
c) @Component and @Service are functionally identical
d) Prototype beans are pooled and reused
e) Self-invocation works fine with Spring AOP
f) @Repository provides automatic exception translation
g) You can have multiple @PostConstruct methods in a bean
h) @Autowired(required = false) prevents an exception if the bean doesn't exist
i) Singleton scope means one instance per JVM
j) AOP can only be used for cross-cutting concerns

---

## Section 10: Code Review Questions

### Question 10.1: Identify Issues
**Question:** Identify all issues in this code and explain how to fix them:

```java
@Service
public class CommissionService {
    @Autowired
    private static DealRepository repository;

    private NotificationService notificationService = new EmailNotificationService();

    @PostConstruct
    public void init() {
        repository.initialize();
    }

    public void calculate(Deal deal) {
        BigDecimal result = calculateInternal(deal);
        notifyStakeholders(result);
    }

    @Transactional
    private void calculateInternal(Deal deal) {
        // calculation logic
    }

    @Async
    private void notifyStakeholders(BigDecimal result) {
        notificationService.send("Result: " + result);
    }
}
```

### Question 10.2: Performance Issues
**Question:** Identify the performance issues in this configuration and suggest improvements:

```java
@Configuration
@ComponentScan("com")
public class AppConfig {

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ExpensiveService expensiveService() {
        ExpensiveService service = new ExpensiveService();
        service.loadLargeDataset();
        service.initializeConnections();
        return service;
    }

    @Bean
    public DataProcessor dataProcessor() {
        return new DataProcessor(expensiveService());
    }
}
```

### Question 10.3: Security Concerns
**Question:** Identify the security/design issues in this code:

```java
@Aspect
@Component
public class SecurityAspect {
    private String currentUser = "admin";

    @Before("@annotation(RequiresAuth)")
    public void checkAuth(JoinPoint joinPoint) {
        if (!isAuthorized(currentUser)) {
            throw new SecurityException("Not authorized");
        }
    }

    private boolean isAuthorized(String user) {
        return "admin".equals(user);
    }
}
```

---

**Total Questions: 60+**

These questions cover all major topics from the lecture notes and require both theoretical understanding and practical application of Spring concepts.
