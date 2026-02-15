# ANSWERS -- Spring Boot Fundamentals

**Course:** Undergraduate SDLC -- Chapman University
**Project:** Commission Calculator (Spring Boot Module)

---

## Spring Boot Auto-Configuration & Starter Dependencies (1-3)

---

### Question 1: What does the @SpringBootApplication annotation combine, and what role does each play?

**Reference:** `CommissionCalculatorSpringBootApplication.java`

The `@SpringBootApplication` annotation is a convenience meta-annotation that combines three separate annotations into one. Looking at lines 55-63 of `CommissionCalculatorSpringBootApplication.java`:

```java
@SpringBootApplication(
    scanBasePackages = "com.chapman.edu.commissions.springboot",
    exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
    }
)
public class CommissionCalculatorSpringBootApplication {
```

The three annotations it combines are:

1. **`@Configuration`** -- Marks the class as a source of bean definitions for the application context. This means Spring treats it as a configuration class, similar to an XML configuration file but in Java. Any `@Bean`-annotated methods in this class (or in other `@Configuration` classes it imports) will have their return values registered as beans in the IoC container.

2. **`@EnableAutoConfiguration`** -- Tells Spring Boot to automatically configure beans based on the JARs on the classpath, existing beans, and property settings. For example, because `spring-boot-starter-web` is on the classpath (declared in `pom.xml`), Spring Boot automatically configures an embedded Tomcat server and Spring MVC. This is the core of Spring Boot's "convention over configuration" philosophy -- it guesses what you need and configures it for you.

3. **`@ComponentScan`** -- Tells Spring to scan the current package and all sub-packages for classes annotated with stereotype annotations (`@Component`, `@Service`, `@Repository`, `@Controller`, `@RestController`). In this project, the `scanBasePackages` attribute explicitly sets the scan root to `"com.chapman.edu.commissions.springboot"`, so Spring discovers all beans in that package hierarchy -- services like `DealService`, repositories like `DealRepository`, controllers like `DealController`, and so on.

Without `@SpringBootApplication`, you would need to declare all three annotations separately on your main class.

---

### Question 2: Why does CommissionCalculatorSpringBootApplication.java exclude DataSourceAutoConfiguration and HibernateJpaAutoConfiguration? What would happen if they were not excluded?

**Reference:** `CommissionCalculatorSpringBootApplication.java`, lines 58-62

```java
exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
}
```

**Why they are excluded:**

This project uses **HashMap-based repositories** (in-memory `ConcurrentHashMap` data stores) rather than a real relational database with JPA/Hibernate. You can see this in `DealRepository.java`:

```java
@Repository
public class DealRepository {
    private final Map<String, Deal> deals = new ConcurrentHashMap<>();
    // ...
}
```

Because `spring-boot-starter-data-jpa` is on the classpath (in `pom.xml`), Spring Boot's auto-configuration would normally try to:

1. **`DataSourceAutoConfiguration`** -- Automatically configure a `DataSource` bean (a database connection pool). It would look for database connection properties like `spring.datasource.url` and attempt to create a connection pool to a database.

2. **`HibernateJpaAutoConfiguration`** -- Automatically configure a JPA `EntityManagerFactory` using Hibernate as the JPA provider. It would try to scan for `@Entity`-annotated classes and set up ORM (Object-Relational Mapping).

**What would happen without the exclusions:**

If these auto-configurations were not excluded, Spring Boot would attempt to set up a full JPA/Hibernate stack. Since the application has H2 configured in `application.properties` (`spring.datasource.url=jdbc:h2:mem:commissiondb`), it would actually succeed in creating a DataSource and EntityManagerFactory. However, this would conflict with the HashMap-based repository design used in the springboot package. The application would have both an unused JPA layer consuming resources and the HashMap repositories actually serving data, creating confusion and potential conflicts. The exclusions make the intent clear: this module deliberately avoids database persistence in favor of simple in-memory stores for educational purposes.

---

### Question 3: List three Spring Boot starter dependencies used in this project (from pom.xml) and explain what each one auto-configures.

**Reference:** `pom.xml`

**1. `spring-boot-starter-web` (pom.xml, line 47)**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```
This starter auto-configures:
- An **embedded Tomcat** servlet container (no need to install a separate application server)
- **Spring MVC** framework for handling HTTP requests and responses
- **Jackson** library for automatic JSON serialization/deserialization (converting Java objects to/from JSON)
- Default error handling and content negotiation
- All controllers in this project (`DealController`, `DashboardController`, `AuthController`, etc.) rely on this starter

**2. `spring-boot-starter-security` (pom.xml, line 51)**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```
This starter auto-configures:
- **Spring Security filter chain** -- every HTTP request passes through security filters
- **Default authentication** with a generated password (overridden by our `SecurityConfig.java`)
- **CSRF protection** enabled by default for web forms
- **Session management** infrastructure
- **Password encoding** support (we define a `BCryptPasswordEncoder` bean in `SecurityConfig.java`)
- The project customizes this with JWT authentication (`JwtAuthenticationFilter.java`) and role-based access control

**3. `spring-boot-starter-validation` (pom.xml, line 117)**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```
This starter auto-configures:
- **Hibernate Validator** as the Bean Validation (JSR 380) implementation
- A **`Validator` bean** that can be injected for programmatic validation (used in `ValidationProcessor.java`)
- Support for validation annotations like `@NotBlank`, `@Size`, `@DecimalMin`, `@Email` (used in `CreateDealRequest.java`)
- Automatic validation of `@Valid`-annotated `@RequestBody` parameters in controllers (used in `DealController.java`)
- Integration with Spring MVC to throw `MethodArgumentNotValidException` when validation fails

---

## Spring Initializr & Project Structure (4-6)

---

### Question 4: Describe the standard Spring Boot project directory structure. Where do Java source files, resources, templates, and test files go?

**Reference:** The project's directory layout

A standard Spring Boot project using Maven follows this directory structure:

```
commission-calculator-spring/
|-- pom.xml                          # Maven build configuration
|-- src/
|   |-- main/
|   |   |-- java/                    # Java source files
|   |   |   |-- com/chapman/edu/commissions/springboot/
|   |   |       |-- CommissionCalculatorSpringBootApplication.java  # Main class
|   |   |       |-- config/          # Configuration classes (SecurityConfig, AppConfig)
|   |   |       |-- controller/      # REST and MVC controllers
|   |   |       |-- service/         # Business logic layer
|   |   |       |-- repository/      # Data access layer
|   |   |       |-- dto/             # Data Transfer Objects (request/response)
|   |   |       |-- exception/       # Custom exceptions and handlers
|   |   |       |-- security/        # Security components (JWT, filters)
|   |   |       |-- mapper/          # Object mappers (DtoMapper)
|   |   |       |-- processor/       # CommandLineRunner processors
|   |   |       |-- util/            # Utility classes (SampleDataLoader)
|   |   |-- resources/
|   |       |-- application.properties       # Base configuration
|   |       |-- application-dev.properties   # Development profile config
|   |       |-- application-prod.properties  # Production profile config
|   |       |-- templates/                   # Thymeleaf HTML templates
|   |       |-- static/                      # Static assets (CSS, JS, images)
|   |-- test/
|       |-- java/                    # Test source files (mirrors main structure)
|       |-- resources/               # Test-specific resources
```

Key conventions:
- **`src/main/java/`** -- All Java source code, organized by package
- **`src/main/resources/`** -- Configuration files (`application.properties`), templates, and static assets
- **`src/main/resources/templates/`** -- Thymeleaf templates (HTML files rendered server-side by `@Controller` classes like `DashboardController.java`)
- **`src/main/resources/static/`** -- Static web resources (CSS, JavaScript, images) served directly
- **`src/test/java/`** -- Unit and integration test classes
- **`src/test/resources/`** -- Test-specific configuration and data files
- **`pom.xml`** -- Maven Project Object Model at the project root, defining dependencies and build plugins

The main application class (`CommissionCalculatorSpringBootApplication.java`) must be at the root of the package hierarchy so that `@ComponentScan` can discover all beans in sub-packages.

---

### Question 5: What is Spring Initializr and how is it used to bootstrap a project? What files does it generate?

**Reference:** `CommissionCalculatorSpringBootApplication.java`, lines 27-33

As documented in the main application class:

```java
 * This project was scaffolded using Spring Initializr (https://start.spring.io),
 * which generates a standard Maven/Gradle project with:
 *   - pom.xml / build.gradle with selected starter dependencies
 *   - This main application class
 *   - application.properties for configuration
 *   - Standard directory structure (src/main/java, src/main/resources, src/test)
```

**Spring Initializr** is a web-based tool (available at https://start.spring.io) that generates a ready-to-run Spring Boot project skeleton. It is the recommended way to start a new Spring Boot project.

**How to use it:**

1. Visit https://start.spring.io in a web browser
2. Choose the build tool (Maven or Gradle)
3. Select the Spring Boot version (this project uses 3.4.5 as seen in `pom.xml` line 8)
4. Specify project metadata: Group (`com.chapman.edu.commissions`), Artifact (`commission-calculator`), package name, Java version (21 in this project)
5. Select starter dependencies by searching and adding them (e.g., Spring Web, Spring Security, Spring Data JPA, Thymeleaf, Validation, DevTools, Actuator)
6. Click "Generate" to download a ZIP file

**Files it generates:**

- **`pom.xml`** (or `build.gradle`) -- Pre-configured with the selected Spring Boot parent, Java version, and all chosen starter dependencies
- **Main application class** -- `CommissionCalculatorSpringBootApplication.java` with the `@SpringBootApplication` annotation and `main()` method
- **`application.properties`** -- An empty (or minimal) configuration file in `src/main/resources/`
- **Test class** -- A skeleton test class with `@SpringBootTest` annotation
- **`.gitignore`** -- Pre-configured to exclude build artifacts, IDE files, and target directories
- **Standard directory structure** -- All the `src/main/java`, `src/main/resources`, `src/test/java`, and `src/test/resources` directories
- **Maven wrapper files** (`mvnw`, `mvnw.cmd`, `.mvn/`) -- Allow building the project without a local Maven installation

---

### Question 6: What is the purpose of the application.properties file and where must it be located?

**Reference:** `application.properties`, `application-dev.properties`, `application-prod.properties`

The `application.properties` file is Spring Boot's primary **externalized configuration** mechanism. It allows you to configure the application's behavior without modifying Java code.

**Location:** It must be located at `src/main/resources/application.properties`. Spring Boot automatically looks for this file on the classpath when the application starts.

**Purpose:** It configures virtually every aspect of the application. Looking at the project's `application.properties`:

```properties
# Application identity
spring.application.name=commission-calculator

# Server port
server.port=8081

# Security credentials
spring.security.user.name=admin
spring.security.user.password=admin123

# H2 Database Configuration
spring.datasource.url=jdbc:h2:mem:commissiondb
spring.jpa.hibernate.ddl-auto=create-drop

# Management/Actuator endpoints
management.endpoints.web.exposure.include=health,info

# Swagger/OpenAPI
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui/
```

Common categories of configuration include:
- **Server settings** -- port, context path, error handling
- **Database/DataSource** -- connection URL, credentials, JPA settings
- **Security** -- default credentials, JWT settings
- **Logging** -- log levels, log file locations
- **Management/Actuator** -- which monitoring endpoints to expose
- **Third-party library settings** -- Swagger, Thymeleaf, Jackson

Profile-specific files like `application-dev.properties` and `application-prod.properties` override the base file's values when their respective profile is active. This is covered in detail in Question 19.

---

## Dependency Injection & IoC Container (7-12)

---

### Question 7: Explain the difference between @Component, @Service, @Repository, and @Controller. Give an example of each from the springboot package.

**Reference:** `DtoMapper.java`, `DealService.java`, `DealRepository.java`, `DashboardController.java`, `DealController.java`

All four annotations are **stereotype annotations** that mark a class as a Spring-managed bean. They all derive from `@Component`, but each communicates a different architectural role and may enable layer-specific features.

**`@Component`** -- The most generic annotation. It marks a class as a Spring-managed bean with no specific layer semantics. Use it for utility or cross-cutting classes.

Example from `DtoMapper.java`:
```java
@Component
public class DtoMapper {
    public DealResponse toDealResponse(Deal deal) { ... }
}
```
And from `SampleDataLoader.java`:
```java
@Component
public class SampleDataLoader implements CommandLineRunner { ... }
```

**`@Service`** -- A specialization of `@Component` that indicates the class contains **business logic**. Functionally identical to `@Component`, but it communicates intent -- this bean belongs to the service/business layer.

Example from `DealService.java`:
```java
@Service
public class DealService {
    private final DealRepository dealRepository;
    public Deal createDeal(CreateDealRequest request) { ... }
}
```

**`@Repository`** -- A specialization of `@Component` that marks a **data access object (DAO)**. It has an additional feature beyond `@Component`: Spring automatically translates persistence-related exceptions into Spring's `DataAccessException` hierarchy, providing a consistent exception model regardless of the data store.

Example from `DealRepository.java`:
```java
@Repository
public class DealRepository {
    private final Map<String, Deal> deals = new ConcurrentHashMap<>();
    public Optional<Deal> findById(String id) { ... }
}
```

**`@Controller`** -- Marks a class as a **Spring MVC web controller** that returns **view names** (resolved by a template engine like Thymeleaf). The return value of handler methods is interpreted as a template path.

Example from `DashboardController.java`:
```java
@Controller
@RequestMapping("/springboot")
public class DashboardController {
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("dealCount", dealService.getDealCount());
        return "springboot/dashboard"; // resolves to templates/springboot/dashboard.html
    }
}
```

**`@RestController`** -- Combines `@Controller` + `@ResponseBody`. Return values are serialized directly to JSON/XML instead of being interpreted as view names. Used for REST API endpoints.

Example from `DealController.java`:
```java
@RestController
@RequestMapping("/api/deals")
public class DealController {
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DealResponse>> getDealById(@PathVariable String id) { ... }
}
```

The hierarchy can be summarized as:
```
@Component       -- Generic bean
|-- @Service     -- Business logic layer
|-- @Repository  -- Data access layer (+ exception translation)
|-- @Controller  -- Web MVC controller (returns view names)
    |-- @RestController  -- REST API controller (returns JSON)
```

---

### Question 8: Looking at DealService.java, explain how constructor injection works. Why is it preferred over field injection?

**Reference:** `DealService.java`, lines 55-63; `DependencyInjectionProcessor.java`, lines 43-59

In `DealService.java`, constructor injection is implemented as follows:

```java
@Service
public class DealService {

    private final DealRepository dealRepository;

    /**
     * Constructor injection -- the preferred way to inject dependencies.
     * Spring automatically provides the DealRepository bean.
     */
    public DealService(DealRepository dealRepository) {
        this.dealRepository = dealRepository;
    }
}
```

**How it works:**

1. When Spring starts up and performs component scanning, it discovers `DealService` (annotated with `@Service`) and `DealRepository` (annotated with `@Repository`).
2. Spring creates the `DealRepository` bean first (since it has no dependencies).
3. When creating `DealService`, Spring sees that its constructor requires a `DealRepository` parameter.
4. Spring looks in the IoC container for a bean of type `DealRepository`, finds the one it already created, and passes it to the constructor.
5. The `DealService` stores the injected dependency in its `final` field.
6. Since there is only one constructor, the `@Autowired` annotation is **optional** (Spring infers that the sole constructor should be used for injection).

**Why constructor injection is preferred over field injection:**

Field injection (as shown in `DependencyInjectionProcessor.java`) looks like this:
```java
@Autowired
private ApplicationContext applicationContext;
```

Constructor injection is preferred for these reasons:

1. **Immutability** -- Fields can be declared `final`, meaning they cannot be reassigned after construction. This prevents accidental modification and makes the object thread-safe.

2. **Testability** -- In unit tests, you can simply pass mock dependencies through the constructor: `new DealService(mockRepository)`. With field injection, you need reflection or a Spring test context to inject mocks.

3. **Explicit dependencies** -- The constructor signature clearly declares all dependencies. You can see at a glance what a class needs to function. Field injection hides dependencies.

4. **No null state** -- With constructor injection, an object is always in a valid state after creation. With field injection, there is a window between object creation and field injection where fields are `null`.

5. **Compile-time safety** -- If a required dependency is missing, the constructor call fails at compile time (in tests) or at startup. Field injection failures are only detected at runtime.

---

### Question 9: What is the ApplicationContext (IoC Container)? How does DependencyInjectionProcessor.java demonstrate its capabilities?

**Reference:** `DependencyInjectionProcessor.java`, `AppConfig.java`

The **ApplicationContext** is Spring's **Inversion of Control (IoC) Container**. It is the central interface for the Spring framework, responsible for:

- **Creating** bean instances (singletons by default)
- **Resolving and injecting** dependencies between beans
- **Managing the lifecycle** of beans (initialization, destruction callbacks)
- **Providing the environment** abstraction (properties, profiles)

As described in `AppConfig.java`:
```java
 * The ApplicationContext is Spring's IoC Container. It:
 *   - Creates and manages bean instances (singletons by default)
 *   - Resolves and injects dependencies between beans
 *   - Manages bean lifecycle (initialization, destruction)
 *   - Provides environment abstraction (properties, profiles)
```

**DependencyInjectionProcessor.java** demonstrates several capabilities of the ApplicationContext:

**1. Counting all beans in the container:**
```java
logger.info("Total beans in container: {}", applicationContext.getBeanDefinitionCount());
```
This shows that the ApplicationContext knows about every registered bean -- both your custom beans and the hundreds of beans auto-configured by Spring Boot.

**2. Listing custom beans by name:**
```java
String[] beanNames = applicationContext.getBeanDefinitionNames();
for (String name : beanNames) {
    if (name.contains("commission") || name.contains("deal") || ...) {
        logger.info("  Bean: {} -> {}", name,
            applicationContext.getBean(name).getClass().getSimpleName());
    }
}
```
This iterates over all bean definition names and filters for project-specific beans, showing the mapping from bean name to class type.

**3. Retrieving beans by type:**
```java
DealRepository dealRepo = applicationContext.getBean(DealRepository.class);
logger.info("Deal count from repository: {}", dealRepo.count());
```
You can programmatically request any bean by its class type -- the container resolves it.

**4. Verifying singleton scope:**
```java
DealRepository dealRepo2 = applicationContext.getBean(DealRepository.class);
logger.info("Same bean instance (singleton)? {}", dealRepo == dealRepo2);
```
This proves that Spring beans are singletons by default. Both calls to `getBean()` return the same object instance (`true`). The container maintains only one instance and shares it across all injection points.

**5. Both injection styles demonstrated in one class:**
```java
// Constructor injection (preferred)
private final DealService dealService;
private final UserService userService;

public DependencyInjectionProcessor(DealService dealService, UserService userService) {
    this.dealService = dealService;
    this.userService = userService;
}

// Field injection (shown for education)
@Autowired
private ApplicationContext applicationContext;
```

---

### Question 10: In SampleDataLoader.java, six dependencies are injected via the constructor. How does Spring resolve each one?

**Reference:** `SampleDataLoader.java`, lines 52-75

```java
@Component
public class SampleDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DealRepository dealRepository;
    private final CommissionPlanRepository planRepository;
    private final CommissionCalculationRepository calculationRepository;
    private final DisputeRepository disputeRepository;
    private final PasswordEncoder passwordEncoder;

    public SampleDataLoader(UserRepository userRepository,
                            DealRepository dealRepository,
                            CommissionPlanRepository planRepository,
                            CommissionCalculationRepository calculationRepository,
                            DisputeRepository disputeRepository,
                            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        // ... etc
    }
}
```

Spring resolves each of the six dependencies through the following process:

1. **`UserRepository`** -- Spring scans the classpath and finds a class annotated with `@Repository` of type `UserRepository`. It creates an instance (singleton) and injects it.

2. **`DealRepository`** -- Same process. Spring finds the `@Repository`-annotated `DealRepository` class, creates its singleton instance, and injects it.

3. **`CommissionPlanRepository`** -- Found via component scanning as another `@Repository` bean.

4. **`CommissionCalculationRepository`** -- Found via component scanning as another `@Repository` bean.

5. **`DisputeRepository`** -- Found via component scanning as another `@Repository` bean.

6. **`PasswordEncoder`** -- This one is different. There is no class annotated with `@Component` or `@Service` that implements `PasswordEncoder`. Instead, it is defined as a **`@Bean` method** in `SecurityConfig.java`:
   ```java
   @Bean
   public PasswordEncoder passwordEncoder() {
       return new BCryptPasswordEncoder();
   }
   ```
   Spring calls this method during startup, registers the returned `BCryptPasswordEncoder` instance as a bean of type `PasswordEncoder`, and injects it wherever `PasswordEncoder` is requested.

**The resolution algorithm Spring follows for each parameter:**

1. Look in the IoC container for a bean whose type matches the constructor parameter type.
2. If exactly one bean of that type exists, inject it.
3. If multiple beans of the same type exist, Spring looks for a `@Qualifier` annotation or tries to match by parameter name.
4. If no bean of the required type exists, Spring throws a `NoSuchBeanDefinitionException` at startup -- a **fail-fast** behavior that catches configuration errors before the application is fully running.

Since `SampleDataLoader` has only one constructor, the `@Autowired` annotation is optional. Spring automatically uses the sole constructor for dependency injection.

---

### Question 11: What is @Autowired and when is it optional? Explain with reference to the code.

**Reference:** `DependencyInjectionProcessor.java`, `DealService.java`, `SampleDataLoader.java`

**`@Autowired`** is a Spring annotation that tells the framework to automatically inject a dependency into a bean. It can be placed on:
- Constructors
- Fields
- Setter methods

**When `@Autowired` is optional:**

Since Spring 4.3, `@Autowired` is **optional on a constructor** when a class has **exactly one constructor**. Spring automatically uses the single constructor for dependency injection.

**Example where `@Autowired` is omitted (single constructor):**

From `DealService.java`:
```java
@Service
public class DealService {
    private final DealRepository dealRepository;

    // No @Autowired needed -- only one constructor
    public DealService(DealRepository dealRepository) {
        this.dealRepository = dealRepository;
    }
}
```

From `SampleDataLoader.java`:
```java
/**
 * Constructor injection -- Spring injects all repository beans automatically.
 * The @Autowired annotation is optional with a single constructor.
 */
public SampleDataLoader(UserRepository userRepository,
                        DealRepository dealRepository,
                        CommissionPlanRepository planRepository,
                        CommissionCalculationRepository calculationRepository,
                        DisputeRepository disputeRepository,
                        PasswordEncoder passwordEncoder) {
```

**When `@Autowired` IS required:**

1. **Field injection** -- `@Autowired` is required because there is no other mechanism for Spring to know it should inject into the field:
   ```java
   // From DependencyInjectionProcessor.java
   @Autowired
   private ApplicationContext applicationContext;
   ```

2. **Multiple constructors** -- If a class has more than one constructor, Spring does not know which one to use for injection. You must annotate the desired constructor with `@Autowired`.

3. **Setter injection** -- If you want Spring to call a setter method to inject a dependency, you must annotate the setter with `@Autowired`.

**Best practice:** Use single-constructor injection (without `@Autowired`) for all production code. It provides immutability, testability, and clarity. The `@Autowired` field injection shown in `DependencyInjectionProcessor.java` is explicitly noted as educational and "not recommended for production code."

---

### Question 12: What is component scanning and how does @SpringBootApplication enable it?

**Reference:** `CommissionCalculatorSpringBootApplication.java`, `AppConfig.java`

**Component scanning** is the process by which Spring automatically discovers and registers beans by scanning the classpath for classes annotated with stereotype annotations (`@Component`, `@Service`, `@Repository`, `@Controller`, `@RestController`).

**How `@SpringBootApplication` enables it:**

Since `@SpringBootApplication` includes `@ComponentScan` (as discussed in Question 1), it triggers component scanning automatically. By default, `@ComponentScan` scans the package of the annotated class and all its sub-packages. In this project, the scan base is explicitly configured:

```java
@SpringBootApplication(
    scanBasePackages = "com.chapman.edu.commissions.springboot",
    // ...
)
public class CommissionCalculatorSpringBootApplication {
```

**What happens during component scanning:**

1. At startup, Spring scans the `com.chapman.edu.commissions.springboot` package and all sub-packages.
2. It finds classes annotated with stereotype annotations:
   - `@Service`: `DealService`, `CommissionPlanService`, `UserService`, `CommissionCalculationService`, `DisputeService`, `CustomUserDetailsService`
   - `@Repository`: `DealRepository`, `CommissionPlanRepository`, `UserRepository`, `CommissionCalculationRepository`, `DisputeRepository`
   - `@RestController`: `DealController`, `CommissionCalculationController`, `AuthController`, `HealthController`
   - `@Controller`: `DashboardController`
   - `@Component`: `DtoMapper`, `SampleDataLoader`, `DependencyInjectionProcessor`, `JwtTokenProvider`, `JwtAuthenticationFilter`, all processors
   - `@Configuration`: `SecurityConfig`, `AppConfig`, `WebMvcConfig`, `DevToolsConfig`
3. For each discovered class, Spring creates a bean definition and registers it in the ApplicationContext.
4. Spring then resolves dependencies between beans and instantiates them in the correct order.

As described in `AppConfig.java`:
```java
 * Spring automatically discovers beans through component scanning. Annotations
 * that mark a class as a Spring-managed bean:
 *   - @Component   -- Generic Spring bean
 *   - @Service     -- Business logic layer
 *   - @Repository  -- Data access layer (adds exception translation)
 *   - @Controller  -- Web MVC controller (returns views)
 *   - @RestController -- REST API controller (returns JSON/XML directly)
 *
 * All these are specializations of @Component. Spring's component scanner finds
 * them in packages specified by @ComponentScan (or @SpringBootApplication).
```

---

## Building REST APIs (13-18)

---

### Question 13: What is the difference between @Controller (DashboardController.java) and @RestController (DealController.java)?

**Reference:** `DashboardController.java`, `DealController.java`

The key difference lies in how return values from handler methods are interpreted:

**`@Controller`** (used in `DashboardController.java`) -- Return values are treated as **view names** that a template engine (Thymeleaf) resolves to HTML templates:

```java
@Controller
@RequestMapping("/springboot")
public class DashboardController {

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("dealCount", dealService.getDealCount());
        return "springboot/dashboard";
        // Spring resolves this to: templates/springboot/dashboard.html
    }
}
```

The `Model` object carries data from the controller to the Thymeleaf template. The return value `"springboot/dashboard"` is a template path, not data sent to the client.

**`@RestController`** (used in `DealController.java`) -- Return values are **serialized directly to JSON** (or XML) and written into the HTTP response body. `@RestController` is a convenience annotation that combines `@Controller` + `@ResponseBody`:

```java
@RestController
@RequestMapping("/api/deals")
public class DealController {

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DealResponse>> getDealById(@PathVariable String id) {
        Deal deal = dealService.getDealById(id);
        DealResponse response = mapper.toDealResponse(deal);
        return ResponseEntity.ok(ApiResponse.success("Deal retrieved successfully", response));
        // The ApiResponse<DealResponse> object is serialized to JSON automatically
    }
}
```

As noted in `DealController.java`:
```java
 * Without @RestController, you'd need @ResponseBody on every method:
 *   @Controller
 *   public class DealController {
 *       @GetMapping("/api/deals")
 *       @ResponseBody                      // <-- needed for each method
 *       public List<Deal> getDeals() { ... }
 *   }
```

| Feature              | `@Controller`                      | `@RestController`                    |
|----------------------|------------------------------------|--------------------------------------|
| Return value         | View name (String)                 | Data object (serialized to JSON)     |
| Template engine      | Yes (Thymeleaf)                    | No                                   |
| Response body        | HTML rendered from template        | JSON/XML                             |
| `@ResponseBody`      | Not included (add per method)      | Included automatically               |
| Model object         | Used to pass data to templates     | Not typically used                   |
| Use case             | Web UI (browser-rendered pages)    | REST API (programmatic clients)      |

---

### Question 14: In DealController.java, explain what @GetMapping("/{id}") does and how @PathVariable works.

**Reference:** `DealController.java`, lines 140-148

```java
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<DealResponse>> getDealById(
        @PathVariable String id) {

    Deal deal = dealService.getDealById(id);
    DealResponse response = mapper.toDealResponse(deal);
    return ResponseEntity.ok(ApiResponse.success("Deal retrieved successfully", response));
}
```

**`@GetMapping("/{id}")`** is a composed annotation that is shorthand for `@RequestMapping(method = RequestMethod.GET, path = "/{id}")`. It maps HTTP GET requests to this handler method.

The `"/{id}"` part is a **URI template** with a path variable placeholder. Since the class-level `@RequestMapping("/api/deals")` defines the base path, the full URL pattern becomes `/api/deals/{id}`.

The `{id}` in curly braces defines a **path variable** -- a dynamic segment of the URL that can match any value.

**`@PathVariable`** extracts the value from the URL path segment and binds it to the method parameter:

- Request: `GET /api/deals/deal-001`
- `{id}` captures `"deal-001"`
- The `id` parameter receives the value `"deal-001"`

Spring matches the path variable name `{id}` to the method parameter name `id` automatically. If the names differ, you can specify the mapping explicitly: `@PathVariable("id") String dealId`.

The `DealController` class uses `@PathVariable` in multiple methods:
- `GET /api/deals/{id}` -- Get a deal by ID
- `PATCH /api/deals/{id}/status` -- Update a deal's status
- `DELETE /api/deals/{id}` -- Delete a deal

Path variables are part of the resource identifier and follow RESTful conventions. They identify **which resource** you are operating on.

---

### Question 15: How do @RequestParam parameters work in the getAllDeals() method of DealController.java? What makes them different from @PathVariable?

**Reference:** `DealController.java`, lines 110-130

```java
@GetMapping
public ResponseEntity<ApiResponse<List<DealResponse>>> getAllDeals(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String salesRepId) {

    List<Deal> deals;
    if (status != null) {
        deals = dealService.getDealsByStatus(DealStatus.valueOf(status));
    } else if (salesRepId != null) {
        deals = dealService.getDealsBySalesRep(salesRepId);
    } else {
        deals = dealService.getAllDeals();
    }
    // ...
}
```

**`@RequestParam`** extracts values from the **query string** of the URL (the part after `?`). For example:
- `GET /api/deals?status=WON` -- `status` = `"WON"`, `salesRepId` = `null`
- `GET /api/deals?salesRepId=user-003` -- `status` = `null`, `salesRepId` = `"user-003"`
- `GET /api/deals` -- both are `null`

The `required = false` attribute makes the parameter optional. Without it, Spring would return a 400 Bad Request if the query parameter is missing. Since both are optional, the method can handle three scenarios: filter by status, filter by sales rep, or return all deals.

**Key differences between `@RequestParam` and `@PathVariable`:**

| Feature            | `@PathVariable`                    | `@RequestParam`                     |
|--------------------|------------------------------------|--------------------------------------|
| URL location       | Part of the path: `/deals/{id}`    | Query string: `/deals?status=WON`   |
| Purpose            | Identify a specific resource       | Filter, sort, or paginate results   |
| Required by default| Yes                                | Yes (configurable with `required`)  |
| Example URL        | `/api/deals/deal-001`              | `/api/deals?status=WON`             |
| Default value      | None                               | Configurable with `defaultValue`    |
| RESTful semantics  | Resource identifier                | Optional modifiers/filters          |

In RESTful design:
- **Path variables** identify the resource: "Give me deal with ID `deal-001`"
- **Request parameters** modify the request: "Give me all deals, but only those with status `WON`"

---

### Question 16: Explain the ResponseEntity class and how it is used to control HTTP status codes in the controller methods.

**Reference:** `DealController.java`

`ResponseEntity<T>` is a Spring class that represents the **entire HTTP response**: status code, headers, and body. It gives the controller full control over what the client receives.

From the Javadoc in `DealController.java`:
```java
 * ResponseEntity<T> provides full control over the HTTP response:
 *   - Status code (200, 201, 404, etc.)
 *   - Response headers
 *   - Response body
```

Here are the different ways `ResponseEntity` is used in `DealController.java`:

**200 OK -- Successful retrieval** (used for GET requests):
```java
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<DealResponse>> getDealById(@PathVariable String id) {
    Deal deal = dealService.getDealById(id);
    DealResponse response = mapper.toDealResponse(deal);
    return ResponseEntity.ok(ApiResponse.success("Deal retrieved successfully", response));
}
```
`ResponseEntity.ok(body)` is a shorthand for `ResponseEntity.status(HttpStatus.OK).body(body)`.

**201 Created -- Successful creation** (used for POST requests):
```java
@PostMapping
public ResponseEntity<ApiResponse<DealResponse>> createDeal(
        @Valid @RequestBody CreateDealRequest request) {
    Deal deal = dealService.createDeal(request);
    DealResponse response = mapper.toDealResponse(deal);
    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("Deal created successfully", response));
}
```
The `.status(HttpStatus.CREATED)` explicitly sets the response code to 201, indicating a resource was successfully created.

**204 No Content -- Successful deletion** (used for DELETE requests):
```java
@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteDeal(@PathVariable String id) {
    dealService.deleteDeal(id);
    return ResponseEntity.noContent().build();
}
```
`ResponseEntity.noContent().build()` returns a 204 status with no response body -- the standard REST response for a successful deletion where there is nothing to return.

Additionally, in `GlobalExceptionHandler.java`, `ResponseEntity` is used to return error status codes:
- **400 Bad Request** -- `new ResponseEntity<>(error, HttpStatus.BAD_REQUEST)` for validation failures
- **404 Not Found** -- `new ResponseEntity<>(error, HttpStatus.NOT_FOUND)` for missing resources
- **422 Unprocessable Entity** -- `new ResponseEntity<>(error, HttpStatus.UNPROCESSABLE_ENTITY)` for business rule violations
- **500 Internal Server Error** -- `new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR)` for unexpected errors

---

### Question 17: What HTTP methods map to CRUD operations? Give the endpoint examples from DealController.java.

**Reference:** `DealController.java`, lines 53-68

As documented in `DealController.java`:

```java
 * REST (Representational State Transfer) APIs use HTTP methods semantically:
 *
 *   GET    /api/deals        -- List all deals (Read)
 *   GET    /api/deals/{id}   -- Get a specific deal (Read)
 *   POST   /api/deals        -- Create a new deal (Create)
 *   PUT    /api/deals/{id}   -- Update an entire deal (Update)
 *   PATCH  /api/deals/{id}   -- Partially update a deal (Partial Update)
 *   DELETE /api/deals/{id}   -- Delete a deal (Delete)
```

Here is the CRUD-to-HTTP mapping with actual endpoints from `DealController.java`:

| CRUD Operation | HTTP Method | Endpoint                      | Controller Method        | Status Code |
|----------------|-------------|-------------------------------|--------------------------|-------------|
| **Create**     | POST        | `POST /api/deals`             | `createDeal()`           | 201 Created |
| **Read** (all) | GET         | `GET /api/deals`              | `getAllDeals()`           | 200 OK      |
| **Read** (one) | GET         | `GET /api/deals/{id}`         | `getDealById()`          | 200 OK      |
| **Update**     | PATCH       | `PATCH /api/deals/{id}/status`| `updateDealStatus()`     | 200 OK      |
| **Delete**     | DELETE      | `DELETE /api/deals/{id}`      | `deleteDeal()`           | 204 No Content |

The corresponding Spring annotations that map each HTTP method are:
- `@GetMapping` -- shorthand for `@RequestMapping(method = RequestMethod.GET)`
- `@PostMapping` -- shorthand for `@RequestMapping(method = RequestMethod.POST)`
- `@PatchMapping` -- shorthand for `@RequestMapping(method = RequestMethod.PATCH)`
- `@DeleteMapping` -- shorthand for `@RequestMapping(method = RequestMethod.DELETE)`
- `@PutMapping` -- shorthand for `@RequestMapping(method = RequestMethod.PUT)` (not used in this controller but available)

The distinction between PUT and PATCH: PUT replaces the entire resource, while PATCH updates only specific fields. This controller uses PATCH for updating just the deal status, which is a partial update.

---

### Question 18: What does @RequestBody do in the createDeal() method and how does Spring deserialize JSON to a Java object?

**Reference:** `DealController.java`, lines 161-172; `CreateDealRequest.java`

```java
@PostMapping
public ResponseEntity<ApiResponse<DealResponse>> createDeal(
        @Valid @RequestBody CreateDealRequest request) {

    Deal deal = dealService.createDeal(request);
    DealResponse response = mapper.toDealResponse(deal);
    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("Deal created successfully", response));
}
```

**What `@RequestBody` does:**

`@RequestBody` tells Spring to read the HTTP request body and deserialize (convert) it into the specified Java object type (`CreateDealRequest`). When a client sends a POST request with a JSON body, Spring needs to know that the body should be parsed and mapped to a method parameter -- `@RequestBody` provides this instruction.

**How Spring deserializes JSON to a Java object:**

1. The client sends an HTTP POST request with `Content-Type: application/json` and a JSON body:
   ```json
   {
     "title": "Acme Corp Software License",
     "value": 45000,
     "salesRepId": "user-003"
   }
   ```

2. Spring's `DispatcherServlet` intercepts the request and routes it to the `createDeal()` method based on the URL and HTTP method.

3. Because `@RequestBody` is present, Spring invokes a **`HttpMessageConverter`** to convert the JSON bytes into a Java object. The `spring-boot-starter-web` dependency includes the **Jackson** library, which provides `MappingJackson2HttpMessageConverter`.

4. Jackson uses the `CreateDealRequest` class's structure to map JSON fields to Java fields:
   ```java
   public class CreateDealRequest {
       private String title;       // maps from "title" in JSON
       private BigDecimal value;   // maps from "value" in JSON
       private String salesRepId;  // maps from "salesRepId" in JSON
   }
   ```
   Jackson matches JSON property names to Java field names (or setter methods). It handles type conversion automatically (e.g., the JSON number `45000` becomes a `BigDecimal`).

5. The `@Valid` annotation (placed before `@RequestBody`) triggers **Bean Validation** on the deserialized object. Spring checks the validation annotations on `CreateDealRequest`:
   ```java
   @NotBlank(message = "Deal title is required")
   @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
   private String title;

   @NotNull(message = "Deal value is required")
   @DecimalMin(value = "0.01", message = "Deal value must be greater than zero")
   private BigDecimal value;

   @NotBlank(message = "Sales representative ID is required")
   private String salesRepId;
   ```

6. If validation passes, the populated `CreateDealRequest` object is passed to the method body. If validation fails, a `MethodArgumentNotValidException` is thrown and caught by `GlobalExceptionHandler`.

The reverse process (Java object to JSON) happens automatically on the return value -- Jackson serializes the `ApiResponse<DealResponse>` object into JSON for the HTTP response body.

---

## Application Properties & Profiles (19-21)

---

### Question 19: Compare application-dev.properties and application-prod.properties. What are three key differences and why?

**Reference:** `application-dev.properties`, `application-prod.properties`

Here are three key differences:

**1. Error Detail Exposure**

Dev:
```properties
server.error.include-message=always
server.error.include-binding-errors=always
server.error.include-stacktrace=always
```

Prod:
```properties
server.error.include-message=never
server.error.include-binding-errors=never
server.error.include-stacktrace=never
```

**Why:** In development, you want to see the full error message, binding errors, and stack trace to quickly diagnose bugs. In production, exposing stack traces to clients is a **security risk** -- it reveals internal class names, library versions, and code paths that an attacker could exploit. Production errors are logged server-side but hidden from the client.

**2. Logging Levels**

Dev:
```properties
logging.level.com.chapman.edu.commissions.springboot=DEBUG
logging.level.org.springframework.web=DEBUG
logging.level.org.springframework.security=DEBUG
```

Prod:
```properties
logging.level.com.chapman.edu.commissions.springboot=WARN
logging.level.org.springframework.web=WARN
logging.level.org.springframework.security=WARN
```

**Why:** DEBUG logging produces verbose output that is invaluable during development for tracing request flows, SQL queries, and security decisions. In production, this volume of logging would fill disk space rapidly, degrade performance, and make it harder to find actual problems. WARN level in production only logs warnings and errors, keeping logs focused and manageable.

**3. JWT Secret and Expiration**

Dev:
```properties
app.jwt.secret=DevSecretKeyForJWTTokenGenerationThatIsLongEnoughForHS256Algorithm
app.jwt.expirationMs=3600000
```

Prod:
```properties
app.jwt.secret=${APP_JWT_SECRET:ProductionSecretKeyMustBeSetViaEnvironmentVariable123}
app.jwt.expirationMs=86400000
```

**Why:** The development JWT secret is a simple, hardcoded string -- convenient for local testing. The production secret uses `${APP_JWT_SECRET:...}` syntax, meaning it reads from an **environment variable** first and falls back to a default only if unset. In real production, the secret should be injected via environment variables or a secrets manager (never hardcoded). The expiration differs too: 1 hour in dev (for quick iteration and testing token expiry) versus 24 hours in production (so users do not need to log in too frequently).

Additional notable differences include: server ports (8082 dev vs 8080 prod), template caching (disabled in dev for immediate changes, enabled in prod for performance), DevTools (enabled in dev, disabled in prod), H2 console (enabled in dev for data inspection, disabled in prod for security), and JSON pretty-printing (enabled in dev for readability, disabled in prod for smaller payloads).

---

### Question 20: How does @Value work in ConfigurationProcessor.java? What does the ":defaultValue" syntax mean?

**Reference:** `ConfigurationProcessor.java`, lines 63-76

```java
@Value("${spring.application.name:unknown}")
private String appName;

@Value("${server.port:8080}")
private String serverPort;

@Value("${app.jwt.expirationMs:86400000}")
private long jwtExpiration;

@Value("${spring.thymeleaf.cache:true}")
private boolean thymeleafCache;
```

**How `@Value` works:**

The `@Value` annotation injects values from externalized configuration sources (primarily `application.properties` and its profile-specific variants) into Spring bean fields. When Spring creates a bean, it evaluates each `@Value` expression and populates the field with the resolved value.

The `${property.key}` syntax is called a **property placeholder**. Spring looks up the specified property key from its environment sources in this priority order:
1. Command-line arguments
2. JVM system properties
3. OS environment variables
4. Profile-specific properties (e.g., `application-dev.properties`)
5. Base `application.properties`
6. Default properties

**The `:defaultValue` syntax:**

The colon (`:`) followed by a value inside the `${}` expression provides a **fallback default**. If the property key is not found in any configuration source, Spring uses the default value instead of throwing an error.

Breaking down each example:

- `${spring.application.name:unknown}` -- Look up `spring.application.name`. If not found, use `"unknown"`. In this project, it resolves to `"commission-calculator"` from `application.properties`.

- `${server.port:8080}` -- Look up `server.port`. If not found, use `8080`. It resolves to `8081` from `application.properties` (or `8082` if the dev profile is active).

- `${app.jwt.expirationMs:86400000}` -- Look up `app.jwt.expirationMs`. If not found, use `86400000` (24 hours). The value varies by profile: `3600000` (1 hour) in dev, `86400000` (24 hours) in prod.

- `${spring.thymeleaf.cache:true}` -- Look up `spring.thymeleaf.cache`. If not found, default to `true`. In dev profile, this is set to `false`.

Spring also handles **automatic type conversion**. Even though property values are strings in the properties file, Spring converts them to the target field type: `String`, `long`, `boolean`, `int`, etc.

The `@Value` annotation is also used in `JwtTokenProvider.java` to externalize JWT configuration:
```java
@Value("${app.jwt.secret:ThisIsASecretKeyForJWT...}")
private String jwtSecret;

@Value("${app.jwt.expirationMs:86400000}")
private long jwtExpirationMs;
```

---

### Question 21: Explain Spring profiles. How do you activate the "dev" profile and what changes when it is active?

**Reference:** `application-dev.properties`, `ConfigurationProcessor.java`, `DevToolsConfig.java`

**Spring profiles** are a mechanism for segregating parts of your application configuration and making them available only in specific environments. They allow you to have different behaviors for development, testing, staging, and production without changing code.

**How profiles work:**

- `application.properties` -- Always loaded (base configuration)
- `application-dev.properties` -- Loaded only when the "dev" profile is active
- `application-prod.properties` -- Loaded only when the "prod" profile is active

Profile-specific properties **override** base properties. For example, `server.port=8082` in `application-dev.properties` overrides `server.port=8081` in `application.properties`.

**How to activate the "dev" profile:**

As documented in `application-dev.properties`:
```properties
# HOW TO ACTIVATE A PROFILE:
#   1. Command line:  java -jar app.jar --spring.profiles.active=dev
#   2. Environment variable: SPRING_PROFILES_ACTIVE=dev
#   3. application.properties: spring.profiles.active=dev
#   4. IDE run configuration: Add --spring.profiles.active=dev
```

**What changes when the "dev" profile is active:**

1. **Server port changes** from 8081 to 8082 (avoids conflicts with other instances)

2. **DevTools are enabled:**
   ```properties
   spring.devtools.restart.enabled=true
   spring.devtools.livereload.enabled=true
   ```
   The application auto-restarts on code changes and triggers browser refresh on template changes.

3. **Template caching is disabled:**
   ```properties
   spring.thymeleaf.cache=false
   ```
   Changes to Thymeleaf HTML templates are reflected immediately without restarting.

4. **Detailed error information is exposed:**
   ```properties
   server.error.include-message=always
   server.error.include-binding-errors=always
   server.error.include-stacktrace=always
   ```

5. **Verbose DEBUG logging is enabled:**
   ```properties
   logging.level.com.chapman.edu.commissions.springboot=DEBUG
   logging.level.org.springframework.web=DEBUG
   logging.level.org.springframework.security=DEBUG
   ```

6. **JWT expiration is shortened** to 1 hour (3,600,000 ms) for faster testing of token expiry.

7. **JSON responses are pretty-printed:**
   ```properties
   spring.jackson.serialization.indent-output=true
   ```

8. **Profile-specific beans are activated.** The `DevToolsConfig` class is annotated with `@Profile("dev")`, meaning it is only loaded when the dev profile is active:
   ```java
   @Configuration
   @Profile("dev")
   public class DevToolsConfig { ... }
   ```

The `ConfigurationProcessor.java` demonstrates how to check active profiles programmatically:
```java
String[] activeProfiles = environment.getActiveProfiles();
logger.info("Active profiles: {}", Arrays.toString(activeProfiles));
```

---

## DevTools & Hot Reload (22-23)

---

### Question 22: What features does Spring Boot DevTools provide? Why is it marked as optional and runtime scope in pom.xml?

**Reference:** `pom.xml` (lines 121-126), `DevToolsConfig.java`, `ConfigurationProcessor.java`

**DevTools dependency in pom.xml:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

**Features DevTools provides** (as documented in `DevToolsConfig.java` and `ConfigurationProcessor.java`):

1. **Automatic Restart** -- When files on the classpath change (Java source compiled, properties updated), DevTools automatically restarts the application. This is much faster than a manual stop-and-start because it uses a two-classloader approach (see Question 23).

2. **LiveReload** -- DevTools includes a built-in LiveReload server. When static resources or templates change, the browser automatically refreshes to show the updated content. You need to install a LiveReload browser extension for this to work.

3. **Development-Friendly Property Defaults** -- DevTools automatically sets sensible defaults for development:
   - Thymeleaf template caching is disabled (changes appear immediately)
   - Detailed error pages are shown
   - H2 console is enabled

4. **Remote Development Support** -- DevTools supports remote restart and remote update for applications deployed to remote servers (useful for cloud-based development).

**Why `scope=runtime`:**

The `runtime` scope means DevTools classes are available at runtime but not at compile time. Your Java code never directly references DevTools classes -- DevTools works behind the scenes through auto-configuration. Setting it as runtime scope ensures it cannot accidentally be used as a compile-time dependency.

**Why `optional=true`:**

The `optional` flag has two important effects:

1. **Transitive dependency exclusion** -- If another project depends on this project, it will NOT inherit the DevTools dependency. DevTools is strictly for this project's development.

2. **Production exclusion** -- When the application is packaged as a JAR for production (`mvn package`), DevTools is automatically excluded. Spring Boot detects that the application is running from a packaged JAR and disables DevTools. The `optional=true` ensures it is not bundled in the production artifact, preventing any development features from accidentally running in production.

As stated in `ConfigurationProcessor.java`:
```
DevTools is automatically disabled when running as JAR.
The optional=true in pom.xml prevents production inclusion.
```

---

### Question 23: How does DevTools achieve fast restart? Explain the two-classloader approach.

**Reference:** `DevToolsConfig.java`, `ConfigurationProcessor.java`

As documented in `DevToolsConfig.java`:

```java
 * When files on the classpath change, DevTools automatically restarts the
 * application. It uses two classloaders:
 *   - Base classloader: loads third-party JARs (doesn't restart)
 *   - Restart classloader: loads your project classes (restarts on change)
 * This makes restarts much faster than a full cold start.
```

**The two-classloader approach:**

When a Spring Boot application starts with DevTools, it creates two separate classloaders:

1. **Base Classloader** -- This classloader loads classes that do not change: third-party libraries (Spring Framework, Jackson, JJWT, Hibernate Validator, etc.) from JAR files in the Maven repository. These classes are loaded once and cached permanently. Since third-party JARs never change during development, there is no reason to reload them.

2. **Restart Classloader** -- This classloader loads **your application classes** -- the classes in `com.chapman.edu.commissions.springboot` and its sub-packages. When you modify a file and save it, the IDE compiles the changed class, and DevTools detects the change on the classpath.

**What happens on a detected change:**

1. DevTools monitors the classpath for file modifications.
2. When a change is detected (e.g., you modified `DealService.java` and saved it), DevTools:
   a. **Discards** the old Restart Classloader (along with all your application classes).
   b. **Creates a new** Restart Classloader.
   c. Reloads only your project's classes with the new classloader.
   d. Re-initializes the Spring ApplicationContext with the updated classes.
3. The Base Classloader is **never discarded**, so all third-party libraries remain in memory.

**Why this is fast:**

A full cold start requires loading all JARs from disk, which can take many seconds for a large project with dozens of dependencies. With the two-classloader approach, the third-party JARs (which constitute the vast majority of loaded classes) are already in memory. Only your project's classes (a much smaller set) are reloaded. This reduces restart time from many seconds to typically under 2 seconds.

As described in `ConfigurationProcessor.java`:
```
DevTools uses two classloaders (base + restart) for speed:
  - Only reloads YOUR code, not third-party JARs
```

For static resources (HTML templates, CSS, JavaScript), DevTools uses an even lighter approach: it triggers a **LiveReload** notification to the browser without restarting the application at all, since these files do not require recompilation.

---

## Exception Handling & Validation (24-27)

---

### Question 24: Looking at CreateDealRequest.java, explain what @NotBlank, @Size, and @DecimalMin do. What triggers these validations?

**Reference:** `CreateDealRequest.java`

```java
public class CreateDealRequest {

    @NotBlank(message = "Deal title is required")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;

    @NotNull(message = "Deal value is required")
    @DecimalMin(value = "0.01", message = "Deal value must be greater than zero")
    private BigDecimal value;

    @NotBlank(message = "Sales representative ID is required")
    private String salesRepId;
}
```

**What each annotation does:**

**`@NotBlank`** -- Validates that a String is not null, not empty (`""`), and not only whitespace (`"   "`). It is stricter than `@NotNull` (which only checks for null) and `@NotEmpty` (which allows whitespace-only strings). Applied to `title` and `salesRepId`, it ensures these fields contain meaningful text.

**`@Size(min = 3, max = 100)`** -- Validates that the length of a String (or size of a collection) falls within the specified range. Applied to `title`, it ensures the title is between 3 and 100 characters long. If a user submits `"AB"` (2 characters), this validation fails.

**`@DecimalMin(value = "0.01")`** -- Validates that a numeric value (here, `BigDecimal`) is greater than or equal to the specified minimum. Applied to `value`, it ensures the deal value is at least 0.01, preventing zero or negative deal values. The `value` attribute is a String representation of the minimum.

**What triggers these validations:**

Validations are triggered in two ways in this project:

**1. Declarative validation (in controllers) -- triggered by `@Valid`:**

In `DealController.java`:
```java
@PostMapping
public ResponseEntity<ApiResponse<DealResponse>> createDeal(
        @Valid @RequestBody CreateDealRequest request) {
```

When a client sends a POST request, Spring:
1. Deserializes the JSON body into a `CreateDealRequest` object
2. Sees the `@Valid` annotation and runs the Bean Validation framework
3. Checks all constraint annotations on the object's fields
4. If any constraint is violated, Spring throws a `MethodArgumentNotValidException` **before** the method body executes
5. `GlobalExceptionHandler` catches the exception and returns a 400 Bad Request response

**2. Programmatic validation (in processors) -- using the `Validator` bean:**

In `ValidationProcessor.java`:
```java
CreateDealRequest emptyRequest = new CreateDealRequest();
Set<ConstraintViolation<CreateDealRequest>> violations = validator.validate(emptyRequest);
violations.forEach(v -> logger.info("    {} -> {}", v.getPropertyPath(), v.getMessage()));
```

Here, the `Validator` bean is used directly to validate an object and inspect individual violations. This is useful for validating objects outside of a controller context.

---

### Question 25: How does GlobalExceptionHandler.java work? Explain @ControllerAdvice and @ExceptionHandler.

**Reference:** `GlobalExceptionHandler.java`

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationErrors(...) { ... }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(...) { ... }

    @ExceptionHandler(BusinessValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessValidation(...) { ... }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(...) { ... }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(...) { ... }
}
```

**`@ControllerAdvice`:**

`@ControllerAdvice` is a specialization of `@Component` that provides **centralized exception handling** across all controllers in the application. Without it, each controller would need its own try-catch blocks. As documented in the file:

```java
 * @ControllerAdvice is a specialization of @Component that allows you to handle
 * exceptions across ALL controllers in one centralized place.
```

When any controller method (in `DealController`, `CommissionCalculationController`, `AuthController`, etc.) throws an exception, Spring intercepts it and routes it to the matching `@ExceptionHandler` method in this class.

**`@ExceptionHandler`:**

The `@ExceptionHandler` annotation marks a method as the handler for a specific exception type. When an exception of that type is thrown anywhere in a controller, Spring routes it to the matching handler.

The handler methods in this project:

1. **`handleValidationErrors(MethodArgumentNotValidException)`** -- Catches validation failures from `@Valid`. Extracts field-level errors from the `BindingResult` and returns **HTTP 400** with detailed validation error messages.

2. **`handleResourceNotFound(ResourceNotFoundException)`** -- Catches custom "not found" exceptions (e.g., when `getDealById()` fails to find a deal). Returns **HTTP 404**.

3. **`handleBusinessValidation(BusinessValidationException)`** -- Catches business rule violations (e.g., trying to reopen a cancelled deal). Returns **HTTP 422 Unprocessable Entity**.

4. **`handleUnauthorized(UnauthorizedException)`** -- Catches authentication/authorization failures. Returns **HTTP 401**.

5. **`handleGenericException(Exception)`** -- The catch-all safety net for any unhandled exception. Returns **HTTP 500** with a generic error message. Critically, it logs the full stack trace server-side but only returns a generic message to the client to avoid leaking internal details.

All handler methods return a `ResponseEntity<ApiErrorResponse>`, providing a **consistent error response structure** across the entire API:
```json
{
    "status": 404,
    "error": "Not Found",
    "message": "Deal not found with id: 'fake-id'",
    "path": "/api/deals/fake-id",
    "timestamp": "2024-01-15T10:30:00"
}
```

---

### Question 26: In ValidationProcessor.java, how is the Validator bean used for programmatic validation? How does this differ from declarative validation with @Valid?

**Reference:** `ValidationProcessor.java`, `DealController.java`

**Programmatic validation using the `Validator` bean** (in `ValidationProcessor.java`):

```java
@Component
public class ValidationProcessor implements CommandLineRunner {

    private final Validator validator;  // Injected Bean Validation Validator

    public ValidationProcessor(Validator validator, ...) {
        this.validator = validator;
    }

    private void demonstrateBeanValidation() {
        // Create an object to validate
        CreateDealRequest emptyRequest = new CreateDealRequest();

        // Programmatically validate the object
        Set<ConstraintViolation<CreateDealRequest>> violations = validator.validate(emptyRequest);

        // Inspect each violation
        logger.info("Violations found: {}", violations.size());
        violations.forEach(v ->
            logger.info("    {} -> {}", v.getPropertyPath(), v.getMessage()));
    }
}
```

The `Validator` bean (from `jakarta.validation.Validator`) is injected via constructor injection. You call `validator.validate(object)` to explicitly validate an object and receive a `Set<ConstraintViolation>` containing all violations. Each `ConstraintViolation` provides:
- `getPropertyPath()` -- which field failed (e.g., `title`)
- `getMessage()` -- the error message (e.g., `"Deal title is required"`)
- `getInvalidValue()` -- the actual value that was invalid

**Declarative validation with `@Valid`** (in `DealController.java`):

```java
@PostMapping
public ResponseEntity<ApiResponse<DealResponse>> createDeal(
        @Valid @RequestBody CreateDealRequest request) {
    // This line is only reached if validation passes
    Deal deal = dealService.createDeal(request);
}
```

With `@Valid`, validation is **automatic and implicit**. You simply annotate the method parameter, and Spring runs validation before your method body executes. If validation fails, a `MethodArgumentNotValidException` is thrown and handled by `GlobalExceptionHandler`.

**Key differences:**

| Aspect                | Programmatic (`Validator`)         | Declarative (`@Valid`)              |
|-----------------------|------------------------------------|--------------------------------------|
| Trigger               | Explicit call to `validator.validate()` | Automatic when method is invoked |
| Context               | Anywhere (services, processors, tests) | Controller method parameters only |
| Error handling        | You inspect the `Set<ConstraintViolation>` | Exception thrown automatically    |
| Control flow          | You decide what to do with violations | Spring throws exception on failure |
| Use case              | Service-layer validation, conditional validation, batch processing | HTTP request body validation |
| Error response        | Custom (you build it)              | Handled by `GlobalExceptionHandler` |

Programmatic validation is useful when you need validation outside of the HTTP request lifecycle -- for example, validating objects in a `CommandLineRunner`, a scheduled task, or a service method where you want to handle violations differently than returning a 400 response.

---

### Question 27: Trace the flow: A client POSTs an invalid CreateDealRequest. What happens step by step from controller to error response?

**Reference:** `DealController.java`, `CreateDealRequest.java`, `GlobalExceptionHandler.java`, `ApiErrorResponse.java`

Suppose a client sends this invalid request:

```
POST /api/deals
Content-Type: application/json

{
    "title": "AB",
    "value": -100,
    "salesRepId": ""
}
```

Here is the complete step-by-step flow:

**Step 1: HTTP Request Arrives**
The embedded Tomcat server receives the HTTP POST request to `/api/deals`.

**Step 2: Security Filter Chain**
The request passes through Spring Security's filter chain. The `JwtAuthenticationFilter` checks for a JWT token in the Authorization header and authenticates the user (assuming a valid token is present).

**Step 3: DispatcherServlet Routes the Request**
Spring's `DispatcherServlet` matches the URL `/api/deals` and HTTP method `POST` to `DealController.createDeal()`.

**Step 4: JSON Deserialization (`@RequestBody`)**
Jackson's `MappingJackson2HttpMessageConverter` reads the request body and deserializes it into a `CreateDealRequest` object:
- `title` = `"AB"`
- `value` = `BigDecimal("-100")`
- `salesRepId` = `""`

**Step 5: Bean Validation (`@Valid`)**
Spring sees the `@Valid` annotation on the parameter and invokes the Hibernate Validator. It checks each constraint annotation:

- `@NotBlank` on `title`: passes (title is not blank)
- `@Size(min=3, max=100)` on `title`: **FAILS** -- `"AB"` has length 2, minimum is 3
- `@NotNull` on `value`: passes (value is not null)
- `@DecimalMin("0.01")` on `value`: **FAILS** -- `-100` is less than `0.01`
- `@NotBlank` on `salesRepId`: **FAILS** -- `""` is blank

Three violations are collected.

**Step 6: MethodArgumentNotValidException is Thrown**
Because validation failed, Spring throws a `MethodArgumentNotValidException` containing a `BindingResult` with all three `FieldError` objects. The `createDeal()` method body **never executes**.

**Step 7: GlobalExceptionHandler Catches the Exception**
Spring routes the exception to `GlobalExceptionHandler.handleValidationErrors()`:

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ApiErrorResponse> handleValidationErrors(
        MethodArgumentNotValidException ex, HttpServletRequest request) {

    Map<String, List<String>> validationErrors = new HashMap<>();
    for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
        validationErrors
            .computeIfAbsent(fieldError.getField(), key -> new ArrayList<>())
            .add(fieldError.getDefaultMessage());
    }

    ApiErrorResponse error = new ApiErrorResponse(
        HttpStatus.BAD_REQUEST.value(), "Bad Request",
        "Validation failed for one or more fields",
        request.getRequestURI()
    );
    error.setValidationErrors(validationErrors);

    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
}
```

**Step 8: ApiErrorResponse is Constructed**
An `ApiErrorResponse` object is built with:
- `status`: 400
- `error`: "Bad Request"
- `message`: "Validation failed for one or more fields"
- `path`: "/api/deals"
- `timestamp`: current time
- `validationErrors`: a map of field names to error messages

**Step 9: JSON Response is Sent**
Jackson serializes the `ApiErrorResponse` to JSON, and Spring returns it with HTTP status 400:

```json
{
    "status": 400,
    "error": "Bad Request",
    "message": "Validation failed for one or more fields",
    "validationErrors": {
        "title": ["Title must be between 3 and 100 characters"],
        "value": ["Deal value must be greater than zero"],
        "salesRepId": ["Sales representative ID is required"]
    },
    "path": "/api/deals",
    "timestamp": "2024-01-15T10:30:00"
}
```

The client receives a clear, structured error response with field-level details that can be used to display validation errors in a form.

---

## Spring Security (28-30)

---

### Question 28: Describe the JWT authentication flow implemented in this project, from login to accessing a protected endpoint.

**Reference:** `AuthController.java`, `JwtTokenProvider.java`, `JwtAuthenticationFilter.java`, `CustomUserDetailsService.java`, `SecurityConfig.java`

The complete JWT authentication flow involves two phases: **login** and **accessing protected resources**.

**Phase 1: Login (obtaining a JWT token)**

1. **Client sends credentials** to the public login endpoint:
   ```
   POST /api/auth/login
   Content-Type: application/json

   { "username": "admin", "password": "admin123" }
   ```
   This endpoint is configured as `permitAll()` in `SecurityConfig.java`, so no authentication is needed.

2. **AuthController receives the request** (`AuthController.java`):
   ```java
   @PostMapping("/login")
   public ResponseEntity<ApiResponse<AuthResponse>> login(
           @Valid @RequestBody LoginRequest request) {
       Authentication authentication = authenticationManager.authenticate(
           new UsernamePasswordAuthenticationToken(
               request.getUsername(), request.getPassword()
           )
       );
   ```

3. **AuthenticationManager validates credentials.** It delegates to `CustomUserDetailsService.loadUserByUsername()`, which:
   - Looks up the user in `UserRepository` by username
   - Converts the user's roles to Spring Security `GrantedAuthority` objects (prefixed with `ROLE_`)
   - Returns a `UserDetails` object containing the username, BCrypt password hash, and authorities

   The `AuthenticationManager` then compares the submitted password against the stored BCrypt hash using the `PasswordEncoder` bean.

4. **If authentication succeeds**, `JwtTokenProvider` generates a signed JWT:
   ```java
   String jwt = tokenProvider.generateToken(request.getUsername());
   ```
   The token contains: `sub` (username), `iat` (issued time), `exp` (expiration time), signed with HMAC-SHA256 using the secret key from `app.jwt.secret`.

5. **The JWT is returned** to the client in the response body:
   ```json
   {
       "success": true,
       "data": {
           "token": "eyJhbGciOiJIUzI1NiJ9...",
           "tokenType": "Bearer",
           "username": "admin",
           "roles": ["ROLE_SYSTEM_ADMIN"]
       }
   }
   ```

**Phase 2: Accessing a Protected Endpoint**

6. **Client sends a request with the JWT** in the Authorization header:
   ```
   GET /api/deals/deal-001
   Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
   ```

7. **`JwtAuthenticationFilter` intercepts the request** (runs before Spring's default filters, as configured in `SecurityConfig`):
   ```java
   String jwt = extractJwtFromRequest(request);  // Strips "Bearer " prefix
   ```

8. **Token is validated** by `JwtTokenProvider.validateToken()`:
   - Verifies the HMAC-SHA256 signature (not tampered with)
   - Checks the token is not expired
   - Ensures the token is well-formed

9. **Username is extracted** from the token's `sub` claim:
   ```java
   String username = tokenProvider.getUsernameFromToken(jwt);
   ```

10. **User details are loaded** from `CustomUserDetailsService` to get current roles/authorities.

11. **SecurityContext is populated**:
    ```java
    SecurityContextHolder.getContext().setAuthentication(authentication);
    ```
    This tells Spring Security: "This request is authenticated as user X with roles Y."

12. **Authorization rules are checked.** Based on `SecurityConfig.java` URL rules and `@PreAuthorize` method annotations, Spring verifies the user has the required roles.

13. **If authorized**, the request proceeds to the controller method and the response is returned. If not authorized, Spring returns HTTP 403 Forbidden.

---

### Question 29: Looking at SecurityConfig.java, explain the two SecurityFilterChain beans. Why are there two and what does @Order do?

**Reference:** `SecurityConfig.java`

`SecurityConfig.java` defines two `SecurityFilterChain` beans to handle two distinct types of traffic: REST API requests and browser-based web page requests. Each requires fundamentally different security strategies.

**Filter Chain 1: API Security** (`@Order(1)` -- higher priority)

```java
@Bean
@Order(1)
public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
    http
        .securityMatcher("/api/**")
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/api/health").permitAll()
            .requestMatchers("/api/admin/**").hasRole("SYSTEM_ADMIN")
            .requestMatchers("/api/users/**").hasAnyRole("SYSTEM_ADMIN", "SALES_MANAGER")
            .anyRequest().authenticated()
        )
        .sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .addFilterBefore(jwtAuthenticationFilter,
            UsernamePasswordAuthenticationFilter.class);
    return http.build();
}
```

This chain:
- **Matches only `/api/**` URLs** (`.securityMatcher("/api/**")`)
- **Disables CSRF** because API clients use JWT tokens (not cookies), so CSRF attacks do not apply
- **Uses JWT authentication** via the custom `JwtAuthenticationFilter` added before the default `UsernamePasswordAuthenticationFilter`
- **Is stateless** (`SessionCreationPolicy.STATELESS`) -- no HTTP session is created; each request must carry its own JWT
- Defines role-based URL rules: auth endpoints are public, admin endpoints require SYSTEM_ADMIN, user endpoints require SYSTEM_ADMIN or SALES_MANAGER

**Filter Chain 2: Web Security** (`@Order(2)` -- lower priority)

```java
@Bean
@Order(2)
public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/login", "/css/**", "/js/**", "/images/**").permitAll()
            .requestMatchers("/swagger-ui/**", "/api-docs/**", "/v3/api-docs/**").permitAll()
            .requestMatchers("/h2-console/**").permitAll()
            .requestMatchers("/actuator/**").permitAll()
            .anyRequest().authenticated()
        )
        .formLogin(form -> form
            .loginPage("/login")
            .defaultSuccessUrl("/springboot/dashboard", true)
            .permitAll()
        )
        .logout(logout -> logout
            .logoutSuccessUrl("/login?logout")
            .permitAll()
        )
        .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
        .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"));
    return http.build();
}
```

This chain:
- **Matches all non-API URLs** (has no `.securityMatcher`, so it catches everything not matched by Chain 1)
- **Uses form-based login** (browser login page at `/login`, redirects to dashboard on success)
- **Uses sessions** (standard stateful web sessions with cookies)
- **Enables CSRF protection** (browser forms need CSRF tokens, except H2 console)
- Allows static resources, Swagger UI, H2 console, and actuator endpoints without authentication

**Why `@Order` matters:**

`@Order` controls the evaluation order of the filter chains. When a request arrives, Spring checks filter chains in order of priority (lower number = higher priority):

1. Chain 1 (`@Order(1)`) checks first: "Does this URL match `/api/**`?" If yes, this chain handles the request with JWT/stateless security.
2. Chain 2 (`@Order(2)`) checks second: "Does this URL match?" Since it has no `securityMatcher`, it matches everything else, handling web pages with form login/session security.

Without `@Order`, Spring could not determine which chain to apply first, and API requests might incorrectly be processed with form login or web requests might fail JWT authentication.

---

### Question 30: What is @PreAuthorize and how is it used in CommissionCalculationController.java for role-based access control?

**Reference:** `CommissionCalculationController.java`, `SecurityConfig.java`

**`@PreAuthorize`** is a Spring Security annotation that enforces authorization checks **before** a method executes. It uses **Spring Expression Language (SpEL)** to evaluate access rules. This is called **method-level security**, which is more fine-grained than URL-based security rules.

Method-level security is enabled by the `@EnableMethodSecurity` annotation on `SecurityConfig.java`:
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // Enables @PreAuthorize, @PostAuthorize, @Secured annotations
public class SecurityConfig { ... }
```

**How it is used in `CommissionCalculationController.java`:**

**1. Approving a calculation -- restricted to managers and finance:**
```java
@PatchMapping("/{id}/approve")
@PreAuthorize("hasAnyRole('SALES_MANAGER', 'FINANCE_ADMIN', 'SYSTEM_ADMIN')")
public ResponseEntity<ApiResponse<CommissionCalculationResponse>> approveCalculation(
        @PathVariable String id) {
    CommissionCalculation calc = calculationService.approveCalculation(id);
    return ResponseEntity.ok(
        ApiResponse.success("Calculation approved", mapper.toCommissionCalculationResponse(calc)));
}
```

The `hasAnyRole('SALES_MANAGER', 'FINANCE_ADMIN', 'SYSTEM_ADMIN')` expression means the authenticated user must have at least one of these three roles. A `SALES_REP` user who attempts to call this endpoint will receive an HTTP 403 Forbidden response, even though they are authenticated.

**2. Marking a calculation as paid -- restricted to finance only:**
```java
@PatchMapping("/{id}/pay")
@PreAuthorize("hasAnyRole('FINANCE_ADMIN', 'SYSTEM_ADMIN')")
public ResponseEntity<ApiResponse<CommissionCalculationResponse>> markAsPaid(
        @PathVariable String id) {
    CommissionCalculation calc = calculationService.markAsPaid(id);
    return ResponseEntity.ok(
        ApiResponse.success("Calculation marked as paid", mapper.toCommissionCalculationResponse(calc)));
}
```

This is even more restrictive -- only `FINANCE_ADMIN` and `SYSTEM_ADMIN` can process payments. `SALES_MANAGER` users are excluded from this operation.

**How `@PreAuthorize` works at runtime:**

1. A client sends a request (e.g., `PATCH /api/calculations/calc-002/approve`) with a JWT token.
2. `JwtAuthenticationFilter` validates the token and populates the `SecurityContext` with the user's authorities (e.g., `[ROLE_SALES_REP]`).
3. Spring routes the request to `approveCalculation()`.
4. Before executing the method body, Spring evaluates the `@PreAuthorize` expression.
5. `hasAnyRole('SALES_MANAGER', 'FINANCE_ADMIN', 'SYSTEM_ADMIN')` checks if the user's authorities contain `ROLE_SALES_MANAGER`, `ROLE_FINANCE_ADMIN`, or `ROLE_SYSTEM_ADMIN`.
6. If the user has `ROLE_SALES_REP`, the check fails and Spring throws an `AccessDeniedException`, which results in an HTTP 403 Forbidden response.
7. If the user has one of the required roles, the method executes normally.

Note that `hasRole()` automatically prepends `ROLE_` to the role name. So `hasRole('SYSTEM_ADMIN')` checks for the authority `ROLE_SYSTEM_ADMIN`. This matches the convention used in `CustomUserDetailsService.java`:
```java
Set<GrantedAuthority> authorities = user.getRoles().stream()
    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
    .collect(Collectors.toSet());
```

Common SpEL expressions usable with `@PreAuthorize`:
- `hasRole('ADMIN')` -- user must have the ADMIN role
- `hasAnyRole('ADMIN', 'MANAGER')` -- user must have at least one of the listed roles
- `isAuthenticated()` -- any authenticated user
- `#id == authentication.name` -- user can only access their own data (parameter-level security)
- `permitAll()` -- everyone (including unauthenticated users)

This approach enables a layered security model: URL-based rules in `SecurityConfig` provide broad access control, while `@PreAuthorize` on individual methods provides precise, operation-specific authorization.
