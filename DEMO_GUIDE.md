# Spring Core Concepts - Demo Guide

## Quick Start

### Run ONLY the Demos (Recommended)

```bash
mvn exec:java -Dexec.mainClass="com.chapman.edu.commissions.corespring.demo.DemoApplication"
```

**What this does:**
- Runs a lightweight Spring application
- NO database, NO web server, NO security
- ONLY loads demo beans
- Clean console output
- Fast startup (~9 seconds)

### Run Full Application (with database and web server)

```bash
mvn spring-boot:run
```

## Available Demonstrations

### 1. AllConceptsDemo ✅ ACTIVE BY DEFAULT

**Shows:** Complete overview of all Spring concepts working together

**Covers:**
- Dependency Injection (constructor, setter, field)
- Bean Scopes (singleton, prototype, request)
- Bean Lifecycle (all 11 steps)
- AOP (all advice types in action)
- Configuration strategies

**To run:**
```bash
mvn exec:java -Dexec.mainClass="com.chapman.edu.commissions.corespring.demo.DemoApplication"
```

**Expected output:**
```
================================================================================
SPRING CORE CONCEPTS - COMPREHENSIVE DEMONSTRATION
================================================================================
1. DEPENDENCY INJECTION (DI) & INVERSION OF CONTROL (IoC)
2. BEAN SCOPES
3. BEAN LIFECYCLE
4. ASPECT-ORIENTED PROGRAMMING (AOP)
5. COMPLETE WORKFLOW - ALL CONCEPTS TOGETHER
================================================================================
```

---

### 2. DIComparisonDemo ⭐ NEW

**Shows:** Direct comparison of code WITHOUT DI vs WITH DI

**Purpose:** Clearly demonstrates why Dependency Injection matters

**Covers:**
- Problems with tightly coupled code (`SimpleCalculationService`)
- Benefits of loose coupling (`CommissionCalculationService`)
- Testing differences
- Maintenance scenarios
- Side-by-side comparison table

**To run:**
1. Open `DIComparisonDemo.java`
2. Uncomment `@Component` on line 19
3. Comment out `@Component` on `AllConceptsDemo.java` line 79
4. Run: `mvn exec:java -Dexec.mainClass="com.chapman.edu.commissions.corespring.demo.DemoApplication"`

**Expected output:**
```
================================================================================
DEPENDENCY INJECTION COMPARISON DEMO
================================================================================
--- WITHOUT Dependency Injection (Anti-Pattern) ---
PROBLEMS with this approach:
❌ 1. TIGHT COUPLING
❌ 2. HARD TO TEST
❌ 3. VIOLATES SINGLE RESPONSIBILITY PRINCIPLE
...

--- WITH Dependency Injection (Best Practice) ---
BENEFITS of Dependency Injection:
✅ 1. LOOSE COUPLING
✅ 2. EASY TO TEST
✅ 3. FOLLOWS SINGLE RESPONSIBILITY PRINCIPLE
...
```

---

### 3. DependencyInjectionDemo

**Shows:** Deep dive into the three types of dependency injection

**Covers:**
- Constructor injection (recommended)
- Setter injection (optional dependencies)
- Field injection (not recommended)
- @Qualifier and @Primary
- Multiple implementations

**To run:**
1. Uncomment `@Component` in `DependencyInjectionDemo.java`
2. Comment out `@Component` in `AllConceptsDemo.java`
3. Run demo application

---

### 4. BeanLifecycleDemo

**Shows:** Bean creation, initialization, and destruction

**Covers:**
- Complete 11-step lifecycle
- Singleton vs Prototype scopes
- @PostConstruct and @PreDestroy
- InitializingBean and DisposableBean
- BeanPostProcessor

**Important:** Watch console during shutdown to see @PreDestroy

**To run:**
1. Uncomment `@Component` in `BeanLifecycleDemo.java`
2. Comment out `@Component` in `AllConceptsDemo.java`
3. Run demo application

---

### 5. AopDemo

**Shows:** Aspect-Oriented Programming concepts

**Covers:**
- @Before, @After, @AfterReturning, @Around advice
- Performance monitoring
- Caching
- Auditing
- Proxy mechanism

**To run:**
1. Uncomment `@Component` in `AopDemo.java`
2. Comment out `@Component` in `AllConceptsDemo.java`
3. Run demo application

---

### 6. ConfigurationDemo

**Shows:** Spring configuration approaches

**Covers:**
- @Value and property injection
- SpEL (Spring Expression Language)
- Profiles (dev/test/prod)
- Conditional beans
- Configuration strategies

**To run:**
1. Uncomment `@Component` in `ConfigurationDemo.java`
2. Comment out `@Component` in `AllConceptsDemo.java`
3. Run demo application

**Try different profiles:**
```bash
mvn exec:java -Dexec.mainClass="com.chapman.edu.commissions.corespring.demo.DemoApplication" \
  -Dspring.profiles.active=dev
```

---

## Key Files for Learning

### Anti-Pattern Example (What NOT to do)

**`SimpleCalculationService.java`** - Shows problems without DI:
- Tight coupling to concrete classes
- Hard to test
- Violates Single Responsibility
- Hidden dependencies
- Inflexible and hard to maintain

**Use this to:** Understand WHY dependency injection is important

---

### Best Practice Example (What TO do)

**`CommissionCalculationService.java`** - Shows proper DI:
- Depends on interfaces
- Constructor injection for required dependencies
- Setter injection for optional dependencies
- Easy to test
- Flexible and maintainable

**Use this to:** Learn the correct DI pattern

---

## Educational Resources

### Lecture Notes
**File:** `SPRING_CORE_LECTURE_NOTES.md` (60+ pages)

**Contents:**
1. DI & IoC Concepts - Theory and examples
2. Spring Core Container - Beans, scopes, lifecycle
3. Application Context & Configuration
4. AOP Fundamentals - Complete guide
5. Annotations & Component Scanning
6. Best Practices & Integration

### Questions
**File:** `QUESTIONS.md` (60+ questions)

**Sections:**
- Multiple choice
- Code analysis
- True/false
- Practical scenarios
- Code review questions

### Answers
**File:** `ANSWERS.md` (Detailed solutions)

**Contents:**
- Complete explanations for all questions
- Code examples
- Common pitfalls
- Best practice recommendations

---

## Code Examples by Topic

### Dependency Injection
```
corespring/di/
├── SimpleCalculationService.java          ❌ WITHOUT DI (anti-pattern)
├── CommissionCalculationService.java      ✅ WITH DI (best practice)
├── NotificationService.java               Interface
├── EmailNotificationService.java          Implementation 1 (@Primary)
├── SmsNotificationService.java            Implementation 2
├── AuditLogger.java                       Interface
├── EmailAuditLogger.java                  Implementation (@Qualifier)
└── DatabaseAuditLogger.java               Implementation
```

### Bean Lifecycle & Scopes
```
corespring/core/
├── SingletonBean.java          Default scope (one per container)
├── PrototypeBean.java          New instance per request
├── RequestScopedBean.java      One per HTTP request
├── LifecycleBean.java          Complete lifecycle (11 steps)
└── CustomBeanPostProcessor.java   Extension point
```

### AOP
```
corespring/aop/
├── AuditingAspect.java         All advice types
├── SecurityAspect.java         @Around for authorization
└── CachingAspect.java          @Around for caching
```

### Configuration
```
corespring/config/
├── AppConfig.java              Java-based configuration
├── ProfileConfig.java          Environment-specific beans
├── ConditionalConfig.java      Feature flags
└── ConfigurationProperties.java   Type-safe properties
```

### Custom Annotations
```
corespring/annotations/
├── Auditable.java              For AOP pointcuts
└── RequiresPermission.java     For security aspects
```

---

## Learning Path

### For Beginners

1. **Start with DIComparisonDemo**
   - See the problem (SimpleCalculationService)
   - See the solution (CommissionCalculationService)
   - Understand WHY DI matters

2. **Read lecture notes Section 1**
   - DI & IoC Concepts
   - Three injection types
   - Benefits and anti-patterns

3. **Run DependencyInjectionDemo**
   - See DI in action
   - Understand @Qualifier and @Primary

4. **Answer questions in QUESTIONS.md Section 1**
   - Test your understanding

### For Intermediate

1. **Run AllConceptsDemo**
   - See all concepts together
   - Understand the big picture

2. **Run BeanLifecycleDemo**
   - Learn bean scopes
   - Understand lifecycle callbacks

3. **Run AopDemo**
   - See cross-cutting concerns
   - Understand advice types

4. **Read lecture notes Sections 2-4**
   - Deep dive into concepts

### For Advanced

1. **Study all demo source code**
   - Understand implementation details
   - See best practices in action

2. **Complete all questions**
   - Test comprehensive understanding

3. **Modify demos**
   - Add your own aspects
   - Create custom annotations
   - Implement new scopes

---

## Common Issues & Solutions

### Demo Not Running

**Problem:** No output when running DemoApplication

**Solution:** Make sure at least one demo has `@Component` uncommented

---

### Multiple Demos Running

**Problem:** See output from multiple demos

**Solution:** Only ONE demo should have `@Component` uncommented at a time

---

### AOP Not Working

**Problem:** Aspects not executing

**Solution:**
- Verify `@EnableAspectJAutoProxy` in AppConfig.java
- Check pointcut expression matches
- Ensure bean is Spring-managed (@Component, @Service, etc.)

---

### Git Ownership Error

**Problem:** `fatal: detected dubious ownership in repository`

**Solution:**
```bash
git config --global --add safe.directory 'C:/Commission Calculator/commission-calculator-spring'
```

---

## Tips for Instructors

### Classroom Demonstration

1. **Start with the problem:**
   - Show `SimpleCalculationService.java`
   - Run `DIComparisonDemo` (WITHOUT DI section)
   - Ask: "How would you test this?"
   - Ask: "What if we want to change from Email to SMS?"

2. **Introduce the solution:**
   - Show `CommissionCalculationService.java`
   - Run `DIComparisonDemo` (WITH DI section)
   - Demonstrate easy testing with mocks
   - Show configuration flexibility

3. **Deep dive:**
   - Run `AllConceptsDemo` for complete overview
   - Run individual demos for specific topics
   - Live code modifications to demonstrate concepts

### Assignments

**Assignment 1: DI Refactoring**
- Give students `SimpleCalculationService.java`
- Ask them to refactor it to use DI
- Compare with `CommissionCalculationService.java`

**Assignment 2: Add New Feature**
- Ask students to add SMS notification
- Should NOT modify `CommissionCalculationService`
- Only configuration/new implementation class

**Assignment 3: Create Custom Aspect**
- Create `@RateLimited` annotation
- Implement aspect to enforce rate limiting
- Apply to service methods

**Assignment 4: Testing**
- Write unit tests for `CommissionCalculationService`
- Use mocks for dependencies
- Compare with testing `SimpleCalculationService`

---

## Additional Commands

### Compile Only
```bash
mvn clean compile
```

### Run with Specific Profile
```bash
mvn exec:java -Dexec.mainClass="com.chapman.edu.commissions.corespring.demo.DemoApplication" \
  -Dspring.profiles.active=prod
```

### Run in Debug Mode
```bash
mvn exec:java -Dexec.mainClass="com.chapman.edu.commissions.corespring.demo.DemoApplication" \
  -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
```

---

## Summary

✅ **6 runnable demos** showing all Spring concepts
✅ **Anti-pattern example** (SimpleCalculationService) showing problems
✅ **Best practice example** (CommissionCalculationService) showing solution
✅ **60+ pages of lecture notes** with theory and examples
✅ **60+ practice questions** with detailed answers
✅ **30+ example classes** demonstrating concepts

**All ready for your undergraduate Software Development Lifecycle class!** 🎓
