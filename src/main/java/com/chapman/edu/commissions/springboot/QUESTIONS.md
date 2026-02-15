# Spring Boot Fundamentals -- Study Questions

**Course:** CPSC 350 -- Software Development Lifecycle (SDLC)
**Institution:** Chapman University
**Topic:** Spring Boot Fundamentals
**Project:** Commission Calculator (Spring Boot Module)

---

## Topic 1: Spring Boot Auto-Configuration and Starter Dependencies

**Q1.** Look at `CommissionCalculatorSpringBootApplication.java`. The `@SpringBootApplication` annotation is described as a convenience annotation that combines three other annotations. Name all three and explain what each one does in the context of this project.

**Q2.** In `pom.xml`, you can see dependencies like `spring-boot-starter-web`, `spring-boot-starter-security`, and `spring-boot-starter-validation`. What is the purpose of the "starter" dependency model? Why does including `spring-boot-starter-web` mean you do not need to separately declare dependencies for Tomcat and Spring MVC?

**Q3.** In `CommissionCalculatorSpringBootApplication.java`, the annotation includes `exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class}`. Why are these two auto-configuration classes excluded? What would happen if they were not excluded, given that the project uses HashMap-based repositories instead of a real database?

**Q4.** The `pom.xml` includes `spring-boot-starter-data-jpa` as a dependency, yet the application excludes JPA auto-configuration. Is this a contradiction? Under what circumstances might a project include a starter but then exclude part of its auto-configuration?

**Q5.** What is the role of `spring-boot-starter-parent` (declared as the `<parent>` in `pom.xml`)? What benefits does inheriting from this parent POM provide for dependency version management?

---

## Topic 2: Spring Initializr, Project Structure, and the Starter Dependency Model

**Q6.** According to the comments in `CommissionCalculatorSpringBootApplication.java`, this project was scaffolded using Spring Initializr. What is Spring Initializr, and what artifacts does it generate when you create a new project? List at least four things it produces.

**Q7.** Examine the project directory layout. The main application class is at `src/main/java/com/chapman/edu/commissions/springboot/`, configuration files are under `src/main/resources/`, and templates are at `src/main/resources/templates/springboot/`. Why does Spring Boot follow this specific directory structure? What is the significance of placing `application.properties` in `src/main/resources/`?

**Q8.** The `@SpringBootApplication` annotation on `CommissionCalculatorSpringBootApplication` includes `scanBasePackages = "com.chapman.edu.commissions.springboot"`. Why is `scanBasePackages` explicitly specified here? What could go wrong if this attribute were omitted in a multi-module project?

**Q9.** Looking at `CommissionCalculatorSpringBootApplication.java`, the `main()` method calls `SpringApplication.run()`. The Javadoc lists six things that happen when `run()` is called. Name and briefly explain at least four of these steps.

---

## Topic 3: RESTful API Development with Spring MVC

**Q10.** Looking at `DealController.java`, the class-level `@RequestMapping("/api/deals")` combined with `@GetMapping("/{id}")` on the `getDealById` method results in what full URL path? Explain how Spring MVC composes the final URL from class-level and method-level annotations.

**Q11.** In `DealController.java`, the `getAllDeals` method accepts two `@RequestParam` parameters: `status` and `salesRepId`. Both are marked `required = false`. What URL would a client use to filter deals by status? What happens if neither parameter is provided?

**Q12.** In `WebMvcConfig.java`, the Javadoc describes the Spring MVC request flow involving the DispatcherServlet, HandlerMapping, Controller, and ViewResolver. Describe this request flow step by step for a request to `GET /api/deals/deal-001` that is handled by `DealController.getDealById()`.

**Q13.** `DashboardController.java` uses `@Controller` while `DealController.java` uses `@RestController`. Both handle HTTP requests. What is the fundamental difference between these two annotations in terms of how their return values are interpreted? Looking at the `DashboardController.dashboard()` method, what does the return value `"springboot/dashboard"` actually represent?

**Q14.** The `HealthController.java` returns a `ResponseEntity<ApiResponse<Map<String, Object>>>`. Why is the response type wrapped in both `ApiResponse` and `ResponseEntity`? What would change if the method simply returned `Map<String, Object>` directly?

---

## Topic 4: Dependency Injection and IoC Container -- Beans, Component Scanning, @Autowired, @Component, @Service, @Repository

**Q15.** In `AppConfig.java`, the Javadoc explains three types of Dependency Injection: constructor injection, setter injection, and field injection. Looking at `DealService.java`, which type of injection is used, and why is it considered the preferred approach? What are two advantages of this approach over field injection with `@Autowired`?

**Q16.** In `UserRepository.java`, the Javadoc states that `@Autowired` is optional when a class has a single constructor (Spring 4.3+). Look at the `UserService.java` constructor. Does it use `@Autowired`? Explain why Spring can still inject the `UserRepository` and `PasswordEncoder` dependencies without that annotation.

**Q17.** The `DtoMapper.java` is annotated with `@Component`, while `DealService.java` uses `@Service`, and `DealRepository.java` uses `@Repository`. All three are discovered during component scanning. What is the difference between `@Component`, `@Service`, and `@Repository`? What extra feature does `@Repository` provide beyond what `@Component` offers?

**Q18.** In `AppConfig.java`, the `restTemplate()` method is annotated with `@Bean`. How does the `@Bean` approach to defining beans differ from the `@Component`/`@Service`/`@Repository` approach? When would you use `@Bean` instead of a stereotype annotation?

**Q19.** `CommissionCalculationService.java` depends on three other beans: `CommissionCalculationRepository`, `DealService`, and `CommissionPlanService`. This is an example of service-layer orchestration. How does Spring resolve these dependencies at startup? What would happen if one of these beans (say `CommissionPlanService`) did not exist in the ApplicationContext?

**Q20.** `SampleDataLoader.java` implements `CommandLineRunner` and is annotated with `@Component`. Its constructor takes six parameters (five repositories and a `PasswordEncoder`). Explain how Spring's IoC container creates this bean, resolves all six dependencies, and determines when to call the `run()` method.

---

## Topic 5: Building REST APIs -- @RestController, @RequestMapping, HTTP Methods, Path Variables, Request Parameters, Response Entities

**Q21.** In `DealController.java`, the `createDeal` method returns `ResponseEntity.status(HttpStatus.CREATED).body(...)`, while `deleteDeal` returns `ResponseEntity.noContent().build()`. Why does the create operation return HTTP 201 instead of 200? Why does the delete operation return 204 with no body?

**Q22.** Looking at `DisputeController.java`, the `resolveDispute` method at `@PatchMapping("/{id}/resolve")` uses both `@PathVariable String id` and two `@RequestParam` values (`resolution` and `resolvedBy`). Write the complete HTTP request a client would send to resolve dispute `"dispute-001"` with a resolution of `"Approved"` and resolvedBy `"user-002"`.

**Q23.** In `CommissionCalculationController.java`, the `calculateCommission` method uses `@Valid @RequestBody CalculateCommissionRequest request`. Explain the role of each of these three annotations (`@Valid`, `@RequestBody`, and the implicit `@PostMapping`). What happens if the client sends a JSON body missing required fields?

**Q24.** Examine the `CommissionPlanController.java`. It has two `@PatchMapping` endpoints: `/{id}/activate` and `/{id}/archive`. Why did the developer choose `PATCH` instead of `PUT` for these operations? How does this follow REST API design conventions?

**Q25.** In `DealController.java`, the `getAllDeals` method wraps its return value in `ApiResponse.success("Deals retrieved successfully", responses)`. Looking at `ApiResponse.java`, what fields does every successful API response include? Why is it considered a best practice to wrap all responses in a consistent envelope structure?

---

## Topic 6: Application Properties, Profiles, and Externalized Configuration

**Q26.** Compare `application-dev.properties` and `application-prod.properties`. List at least four specific configuration differences between the dev and prod profiles, and explain the reasoning behind each difference (e.g., why is `spring.thymeleaf.cache` set to `false` in dev but `true` in prod?).

**Q27.** In `application-dev.properties`, the file header describes the property resolution order (highest to lowest priority). If `application.properties` sets `server.port=8081` and `application-dev.properties` sets `server.port=8082`, and you also pass `--server.port=9090` on the command line, which port will the application run on? Why?

**Q28.** In `JwtTokenProvider.java`, the `@Value("${app.jwt.secret:ThisIsASecretKeyForJWTTokenGenerationThatIsLongEnoughForHS256}")` annotation is used. What does the `${...}` syntax do? What does the colon (`:`) followed by a value represent? Where does Spring look for the property `app.jwt.secret`?

**Q29.** Looking at `application-prod.properties`, the JWT secret is defined as `app.jwt.secret=${APP_JWT_SECRET:ProductionSecretKeyMustBeSetViaEnvironmentVariable123}`. Why is it a security best practice to set the production JWT secret via an environment variable rather than hardcoding it in a properties file? What other methods for externalizing secrets are listed in the file comments?

---

## Topic 7: Spring Boot DevTools and Hot Reload for Rapid Development

**Q30.** `DevToolsConfig.java` is annotated with `@Profile("dev")`. What does this annotation mean? If you start the application without specifying any profile, will this configuration class be loaded? How would you activate the dev profile from the command line?

**Q31.** According to the comments in `DevToolsConfig.java`, DevTools uses a two-classloader strategy for automatic restarts. Explain how the "base classloader" and "restart classloader" work together and why this approach makes restarts faster than a full cold start.

**Q32.** In `pom.xml`, the `spring-boot-devtools` dependency is declared with `<scope>runtime</scope>` and `<optional>true</optional>`. What do these two settings accomplish? Why is it important that DevTools is not included in production builds?

**Q33.** The `application-dev.properties` file sets `spring.devtools.livereload.enabled=true` and `spring.thymeleaf.cache=false`. How do LiveReload and disabled template caching work together to improve the development workflow when editing Thymeleaf templates in the `templates/springboot/` directory?

---

## Topic 8: Error Handling and Validation Best Practices

**Q34.** In `GlobalExceptionHandler.java`, the generic `handleGenericException` method (the "safety net") returns the message `"An unexpected error occurred. Please try again later."` instead of the actual exception message. Why is this considered a security best practice? What kind of information might an exception message leak to an attacker?

**Q35.** Look at the `ApiErrorResponse.java` class. It includes fields for `status`, `error`, `message`, `validationErrors`, `path`, and `timestamp`. Compare this to Spring Boot's default error response. Why does the project define its own custom error response structure instead of using the default?

**Q36.** In `DealService.java`, the `updateDealStatus` method throws a `BusinessValidationException` when trying to reopen a cancelled deal. Trace the full path this exception takes from the point it is thrown to the HTTP response the client receives. Name each class involved in this chain.

---

## Topic 9: Exception Handling and Validation -- @ControllerAdvice, @ExceptionHandler, Bean Validation

**Q37.** In `CreateDealRequest.java`, the `title` field has two validation annotations: `@NotBlank(message = "Deal title is required")` and `@Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")`. If a client sends a POST request to `/api/deals` with `{"title": "", "value": 50000, "salesRepId": "user-003"}`, which validation annotation(s) will fail? What HTTP status code will the client receive?

**Q38.** In `CreatePlanRequest.java`, the Javadoc explains the difference between `@NotNull`, `@NotBlank`, and `@NotEmpty`. Given that `effectiveStartDate` (a `LocalDate`) uses `@NotNull` while `name` (a `String`) uses `@NotBlank`, explain why you cannot use `@NotBlank` on a `LocalDate` field. What would happen if you tried?

**Q39.** Look at the `handleValidationErrors` method in `GlobalExceptionHandler.java`. It processes a `MethodArgumentNotValidException` and builds a map of field-level validation errors. If a `CreateUserRequest` is submitted with a blank username, an invalid email `"not-an-email"`, and a password of `"ab"`, how many entries would the `validationErrors` map contain, and what would each entry look like? (Refer to the validation annotations in `CreateUserRequest.java`.)

**Q40.** `GlobalExceptionHandler.java` handles four specific exception types and one generic `Exception` catch-all. What HTTP status codes do `ResourceNotFoundException`, `BusinessValidationException`, `UnauthorizedException`, and the generic catch-all map to, respectively? Why is it important that the specific exception handlers are defined alongside the generic one?

**Q41.** In `DashboardController.java`, the `createDeal` method uses `BindingResult` for validation in the form-based (Thymeleaf) flow, while `DealController.java` uses `@Valid` without `BindingResult` in the REST API flow. What is the difference in how validation failures are handled in these two approaches? What happens differently when validation fails in each controller?

---

## Topic 10: Spring Security Basics -- Authentication, Authorization, Security Filters, JWT, and RBAC

**Q42.** In `SecurityConfig.java`, two `SecurityFilterChain` beans are defined: `apiSecurityFilterChain` (with `@Order(1)`) and `webSecurityFilterChain` (with `@Order(2)`). Why does the application need two separate security filter chains? What determines which chain handles a given request?

**Q43.** Looking at `SecurityConfig.java`, CSRF protection is disabled for the API filter chain (`.csrf(csrf -> csrf.disable())`) but left enabled for the web filter chain (except for `/h2-console/**`). Why is it safe to disable CSRF for REST APIs that use JWT authentication? Why should CSRF remain enabled for the form-based web interface?

**Q44.** In `JwtAuthenticationFilter.java`, the `doFilterInternal` method performs six steps (extract JWT, validate token, extract username, load user details, create Authentication object, set SecurityContext). Explain why setting the `SecurityContext` with `SecurityContextHolder.getContext().setAuthentication(authentication)` is necessary. What would happen if this step were skipped even though the token was valid?

**Q45.** `CustomUserDetailsService.java` converts application `UserRole` values to Spring Security `GrantedAuthority` objects by prepending `"ROLE_"`. For example, `UserRole.SALES_REP` becomes `"ROLE_SALES_REP"`. In `SecurityConfig.java`, the authorization rule uses `.hasRole("SYSTEM_ADMIN")` (without the prefix). How does Spring Security reconcile this? What would happen if `hasRole("ROLE_SYSTEM_ADMIN")` were used instead?

**Q46.** Looking at `AuthController.java`, trace the complete authentication flow when a client sends `POST /api/auth/login` with `{"username": "admin", "password": "admin123"}`. Which classes are involved, in what order, and what does each class do? Start from the controller and include the `AuthenticationManager`, `CustomUserDetailsService`, `PasswordEncoder`, and `JwtTokenProvider`.

**Q47.** In `CommissionCalculationController.java`, the `approveCalculation` method has `@PreAuthorize("hasAnyRole('SALES_MANAGER', 'FINANCE_ADMIN', 'SYSTEM_ADMIN')")` and the `markAsPaid` method has `@PreAuthorize("hasAnyRole('FINANCE_ADMIN', 'SYSTEM_ADMIN')")`. A user with only the `SALES_MANAGER` role attempts to call `markAsPaid`. What happens? What HTTP status code is returned? Which annotation in `SecurityConfig.java` enables this method-level security?

**Q48.** In `UserController.java`, the `createUser`, `deactivateUser`, and `deleteUser` methods are all annotated with `@PreAuthorize("hasRole('SYSTEM_ADMIN')")`. Meanwhile, in `SecurityConfig.java`, the URL-level rule states `.requestMatchers("/api/users/**").hasAnyRole("SYSTEM_ADMIN", "SALES_MANAGER")`. If a `SALES_MANAGER` sends a POST to `/api/users`, what happens? Explain how URL-level and method-level security interact.

---

## Practical Application and Scenario Questions

**Q49.** You are tasked with adding a new REST endpoint `GET /api/deals/{id}/calculations` that returns all commission calculations for a specific deal. Which existing controller would you modify (or would you create a new one)? What service method would you call? Write the method signature and annotations you would add.

**Q50.** A new business requirement says that deal values must not exceed $10,000,000. You need to add validation to enforce this rule. Looking at `CreateDealRequest.java`, which validation annotation would you add to the `value` field and what would it look like? Where in the system would the validation error be handled if a user submits a deal exceeding this limit?

**Q51.** Currently, the `HealthController.java` endpoint is publicly accessible (configured as `permitAll()` in `SecurityConfig.java`). If you wanted to restrict the health endpoint so that only authenticated users with the `SYSTEM_ADMIN` role could access it, what specific changes would you make to `SecurityConfig.java`?

**Q52.** Looking at `SampleDataLoader.java`, the passwords are encoded using `passwordEncoder.encode("admin123")`. The `PasswordEncoder` bean is defined as `BCryptPasswordEncoder` in `SecurityConfig.java`. If you were to inspect the stored password hash for the admin user, would it look the same every time the application starts? Why or why not? What property of BCrypt makes it suitable for password storage?

---

## Key for Self-Assessment

For each question, you should be able to:

1. **Identify** the relevant source file(s) in the `springboot` package.
2. **Explain** the Spring Boot concept using your own words.
3. **Trace** the flow through the application (request to response, exception to error, bean creation to injection).
4. **Apply** the concept to a new scenario (adding endpoints, modifying configuration, changing security rules).
5. **Compare** related concepts and articulate when you would choose one approach over another.
