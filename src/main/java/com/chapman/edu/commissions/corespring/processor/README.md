# Spring Core Concepts - Demo Guide

This guide explains how to run the interactive demonstrations of all Spring Core concepts.

## Available Demos

### 1. AllConceptsDemo (ACTIVE BY DEFAULT)
**File:** `src/main/java/com/chapman/edu/commissions/corespring/demo/AllConceptsDemo.java`

**What it demonstrates:**
- Complete overview of all Spring concepts working together
- Dependency Injection (constructor, setter, field)
- Bean Scopes (singleton vs prototype)
- Bean Lifecycle (11 steps from creation to destruction)
- AOP (all advice types in action)
- Configuration strategies

**How to run:**
1. This demo is ACTIVE by default
2. Simply start the application: `mvn spring-boot:run`
3. Watch the console output for detailed explanations

### 2. DependencyInjectionDemo
**File:** `src/main/java/com/chapman/edu/commissions/corespring/demo/DependencyInjectionDemo.java`

**What it demonstrates:**
- Constructor injection (recommended)
- Setter injection (optional dependencies)
- Field injection (not recommended, shown for education)
- @Qualifier and @Primary annotations
- Multiple implementations of same interface
- Optional dependencies

**How to run:**
1. Comment out `@Component` in `AllConceptsDemo.java`
2. Uncomment `@Component` in `DependencyInjectionDemo.java`
3. Run: `mvn spring-boot:run`

### 3. BeanLifecycleDemo
**File:** `src/main/java/com/chapman/edu/commissions/corespring/demo/BeanLifecycleDemo.java`

**What it demonstrates:**
- Complete bean lifecycle (11 steps)
- Singleton scope (one instance per container)
- Prototype scope (new instance per request)
- @PostConstruct and @PreDestroy
- InitializingBean and DisposableBean
- BeanPostProcessor extension points

**How to run:**
1. Comment out `@Component` in `AllConceptsDemo.java`
2. Uncomment `@Component` in `BeanLifecycleDemo.java`
3. Run: `mvn spring-boot:run`
4. **Important:** Watch console during shutdown to see destruction lifecycle

### 4. AopDemo
**File:** `src/main/java/com/chapman/edu/commissions/corespring/demo/AopDemo.java`

**What it demonstrates:**
- @Before advice (pre-processing)
- @After advice (post-processing)
- @AfterReturning advice (successful execution)
- @Around advice (wrapping execution)
- Performance monitoring aspect
- Caching aspect
- Auditing aspect
- Proxy mechanism explanation

**How to run:**
1. Comment out `@Component` in `AllConceptsDemo.java`
2. Uncomment `@Component` in `AopDemo.java`
3. Run: `mvn spring-boot:run`

### 5. ConfigurationDemo
**File:** `src/main/java/com/chapman/edu/commissions/corespring/demo/ConfigurationDemo.java`

**What it demonstrates:**
- @Value and property injection
- SpEL (Spring Expression Language)
- Profile-based configuration
- Conditional bean registration
- Configuration strategies comparison

**How to run:**
1. Comment out `@Component` in `AllConceptsDemo.java`
2. Uncomment `@Component` in `ConfigurationDemo.java`
3. Run: `mvn spring-boot:run`

**Try different profiles:**
```bash
# Run with dev profile
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=dev

# Run with prod profile
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=prod

# Run with test profile
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=test
```

## Quick Start

1. **Run the comprehensive demo (recommended for first time):**
   ```bash
   mvn spring-boot:run
   ```
   The `AllConceptsDemo` will run automatically and show you everything.

2. **Run a specific demo:**
   - Open `AllConceptsDemo.java`
   - Comment out the `@Component` annotation
   - Open the specific demo file you want (e.g., `AopDemo.java`)
   - Uncomment the `@Component` annotation
   - Run: `mvn spring-boot:run`

3. **Run multiple demos:**
   - You can uncomment `@Component` on multiple demo classes
   - They will all run in sequence
   - Each demo prints a clear header and footer

## What to Watch For

### During Startup
- Bean creation messages
- `BeanPostProcessor BEFORE init` messages
- `@PostConstruct` messages
- Lifecycle callbacks (1-8)
- Proxy creation

### During Execution
- AOP aspect messages:
  - `[AUDIT @Before]`
  - `[PERFORMANCE @Around]`
  - `[AUDIT @AfterReturning]`
  - `[AUDIT @After]`
- Cache HIT/MISS messages
- Dependency injection status
- Bean scope comparisons

### During Shutdown (IMPORTANT!)
- `@PreDestroy` messages
- `DisposableBean.destroy()` calls
- Cleanup messages
- Note: Prototype beans are NOT destroyed by Spring

## Example Output

When you run `AllConceptsDemo`, you'll see:

```
================================================================================
SPRING CORE CONCEPTS - COMPREHENSIVE DEMONSTRATION
================================================================================
This demo shows ALL concepts working together:
• Dependency Injection & IoC
• Bean Lifecycle & Scopes
• Aspect-Oriented Programming (AOP)
• Configuration Strategies
================================================================================

--------------------------------------------------------------------------------
1. DEPENDENCY INJECTION (DI) & INVERSION OF CONTROL (IoC)
--------------------------------------------------------------------------------

CommissionCalculationService demonstrates three injection types:

✓ Constructor Injection (RECOMMENDED):
  - CommissionRuleEngine (required dependency)
  - NotificationService with @Qualifier (choosing specific implementation)

✓ Field Injection:
  - AuditLogger with @Qualifier("emailAuditLogger")
  - Works but not recommended for production
...
```

## Troubleshooting

**No output from demos:**
- Check that `@Component` is uncommented on at least one demo class
- Make sure the application started successfully

**Multiple demos running:**
- Only one demo should have `@Component` uncommented at a time
- Or run `AllConceptsDemo` which calls all concepts in one demo

**Aspects not firing:**
- Make sure `@EnableAspectJAutoProxy` is present in configuration
- Check that the method matches the pointcut expression
- Verify the bean is a Spring-managed bean (has @Component, @Service, etc.)

**Profile-specific beans not found:**
- Activate the profile: `-Dspring.profiles.active=dev`
- Check that the profile name matches exactly

## Learning Path

Recommended order for studying:

1. **Start with AllConceptsDemo** - Get the big picture
2. **DependencyInjectionDemo** - Understand DI types and best practices
3. **BeanLifecycleDemo** - Learn bean creation and destruction
4. **AopDemo** - See cross-cutting concerns in action
5. **ConfigurationDemo** - Explore configuration options

## Additional Resources

- **Lecture Notes:** `SPRING_CORE_LECTURE_NOTES.md` - Comprehensive theory
- **Questions:** `QUESTIONS.md` - Test your understanding
- **Answers:** `ANSWERS.md` - Detailed answers with explanations

## Example Classes

All demo classes use real domain models from the commission calculator:
- `Deal` - Sales deals
- `CommissionCalculation` - Calculated commissions
- `CommissionPlan` - Commission plans
- `CommissionRule` - Calculation rules

This makes the examples realistic and easier to understand in a business context.

## Key Files

### Domain Models
- `src/main/java/com/chapman/edu/commissions/model/Deal.java`
- `src/main/java/com/chapman/edu/commissions/model/CommissionCalculation.java`
- `src/main/java/com/chapman/edu/commissions/model/CommissionPlan.java`

### DI Examples
- `src/main/java/com/chapman/edu/commissions/corespring/di/CommissionCalculationService.java`
- `src/main/java/com/chapman/edu/commissions/corespring/di/EmailNotificationService.java`
- `src/main/java/com/chapman/edu/commissions/corespring/di/SmsNotificationService.java`

### Lifecycle Examples
- `src/main/java/com/chapman/edu/commissions/corespring/core/SingletonBean.java`
- `src/main/java/com/chapman/edu/commissions/corespring/core/PrototypeBean.java`
- `src/main/java/com/chapman/edu/commissions/corespring/core/LifecycleBean.java`
- `src/main/java/com/chapman/edu/commissions/corespring/core/CustomBeanPostProcessor.java`

### AOP Examples
- `src/main/java/com/chapman/edu/commissions/corespring/aop/AuditingAspect.java`
- `src/main/java/com/chapman/edu/commissions/corespring/aop/SecurityAspect.java`
- `src/main/java/com/chapman/edu/commissions/corespring/aop/CachingAspect.java`

### Configuration Examples
- `src/main/java/com/chapman/edu/commissions/corespring/config/AppConfig.java`
- `src/main/java/com/chapman/edu/commissions/corespring/config/ProfileConfig.java`
- `src/main/java/com/chapman/edu/commissions/corespring/config/ConditionalConfig.java`

Happy learning!
