# Spring Boot ORM Module - Commission Calculator

## Overview

This module implements a **full Commission Calculator application** using Spring Boot ORM concepts. It builds on the domain model from `com.chapman.edu.commissions.model` and the Spring fundamentals from `com.chapman.edu.commissions.corespring` to demonstrate how to persist, query, and manage data in a relational database.

## Concepts Covered

### 1. Spring Data JPA Repositories and Custom Query Methods

**Package:** `com.chapman.edu.commissions.orm.repository`

Spring Data JPA eliminates boilerplate data access code by generating repository implementations at runtime from interface definitions.

#### Query Strategies Demonstrated:

| Strategy | Example | File |
|----------|---------|------|
| **Derived Query Methods** | `findByUsername(String)` - Spring generates SQL from method name | `UserRepository.java` |
| **@Query with JPQL** | `findActiveUsersByRole(@Param role)` - Object-oriented query language | `UserRepository.java` |
| **@Query with Native SQL** | `findTopPerformers(@Param limit)` - Raw SQL for DB-specific features | `UserRepository.java` |
| **JPA Specifications** | `DealSpecifications.hasStatus(WON).and(valueGreaterThan(50000))` - Dynamic query building | `DealSpecifications.java` |
| **@EntityGraph** | `findWithProductsAndSalesRepById(id)` - Solve N+1 problem | `DealRepository.java` |
| **JOIN FETCH** | `findDealsWithProductsBySalesRepAndStatus(...)` - Eager fetch in JPQL | `DealRepository.java` |
| **@Modifying** | `bulkUpdateStatus(...)` - Bulk UPDATE/DELETE operations | `CommissionCalculationRepository.java` |
| **Pagination** | `findByStatus(status, Pageable)` - Database-level pagination | `CommissionCalculationRepository.java` |
| **Aggregate Functions** | `calculateTotalValueBySalesRepAndStatus(...)` - SUM, COUNT, AVG | `DealRepository.java` |

#### Repository Files:
- `UserRepository.java` - Derived queries, JPQL, native SQL, search
- `DealRepository.java` - EntityGraph, JOIN FETCH, Specifications, pagination
- `CommissionPlanRepository.java` - Aggregate loading with multiple JOIN FETCH
- `CommissionCalculationRepository.java` - @Modifying bulk updates, aggregates
- `DisputeRepository.java` - Complex joins, pagination
- `DealSpecifications.java` - Criteria API specification pattern

**Processor:** `JpaProcessor.java` - Runs on startup, exercises all query types with logging

---

### 2. Entity Relationships, Mapping Strategies, and Database Design

**Package:** `com.chapman.edu.commissions.orm.entity`

#### Relationship Types:

| Relationship | Example | Annotations |
|-------------|---------|-------------|
| **@OneToMany / @ManyToOne** (bidirectional) | `CommissionPlan` <-> `CommissionRule` | `@OneToMany(mappedBy, cascade, orphanRemoval)` / `@ManyToOne` + `@JoinColumn` |
| **@OneToMany with CascadeType.ALL** | `Deal` -> `DealProduct` | Parent manages child lifecycle |
| **Self-referential** | `User.manager` -> `User` | `@ManyToOne` pointing back to same entity |
| **@ElementCollection** | `User.roles` -> `Set<UserRole>` | Collection of enums in separate table |
| **Multiple @ManyToOne to same entity** | `Dispute` -> `User` (salesRep + manager) | Two `@JoinColumn` with different names |

#### Key Concepts:

- **Owning vs. Inverse Side**: The owning side (with `@JoinColumn`) controls the FK. Changes on the inverse side (`mappedBy`) are NOT persisted.
- **CascadeType.ALL + orphanRemoval**: Parent entity manages the entire lifecycle of children.
- **FetchType.LAZY vs. EAGER**: LAZY loads on-demand (recommended default); EAGER loads immediately.
- **Aggregate Root Pattern**: `CommissionPlan` is the entry point for rules, tiers, and bonuses.

#### Entity Relationship Diagram:

```
User (users)
  |-- @ElementCollection -> user_roles
  |-- @ManyToOne(self) -> User (manager)
  |-- @OneToMany -> Deal

Deal (deals)
  |-- @ManyToOne -> User (salesRep)
  |-- @OneToMany(cascade=ALL) -> DealProduct
  |-- @OneToMany -> CommissionCalculation

CommissionPlan (commission_plans)
  |-- @OneToMany(cascade=ALL) -> CommissionRule
  |     |-- @OneToMany(cascade=ALL) -> RuleCondition
  |-- @OneToMany(cascade=ALL) -> CommissionTier
  |-- @OneToMany(cascade=ALL) -> BonusRule

CommissionCalculation (commission_calculations)
  |-- @ManyToOne -> Deal
  |-- @ManyToOne -> User (salesRep)
  |-- @ManyToOne -> CommissionPlan
  |-- @OneToMany(cascade=ALL) -> BonusCalculation
  |-- @OneToMany(cascade=ALL) -> AcceleratorCalculation

Dispute (disputes)
  |-- @ManyToOne -> CommissionCalculation
  |-- @ManyToOne -> User (salesRep)
  |-- @ManyToOne -> User (manager)
  |-- @OneToMany(cascade=ALL) -> DisputeComment
```

**Processor:** `OrmProcessor.java` - Demonstrates relationship traversal and entity loading

---

### 3. Database Migration with Flyway

**Location:** `src/main/resources/db/migration/`

Flyway manages database schema versioning through numbered SQL migration scripts.

#### Migration Files:

| File | Purpose |
|------|---------|
| `V1__create_commission_schema.sql` | Creates all tables, foreign keys, indexes, and constraints |
| `V2__seed_sample_data.sql` | Inserts sample users, plans, deals, and calculations |
| `V3__add_audit_columns.sql` | Demonstrates incremental schema evolution (ALTER TABLE, CREATE VIEW) |

#### Key Configuration (`application.properties`):
```properties
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
spring.jpa.hibernate.ddl-auto=validate  # Flyway manages schema, Hibernate validates
```

#### Flyway vs. Liquibase:
- **Flyway**: SQL-based, simple naming convention, lightweight
- **Liquibase**: XML/YAML changelogs, database-agnostic, automatic rollback

**Processor:** `MigrationProcessor.java` - Inspects Flyway migration history and configuration

---

### 4. Transaction Management and Isolation Levels

**Package:** `com.chapman.edu.commissions.orm.service`

Spring's `@Transactional` annotation manages database transactions declaratively.

#### Transaction Attributes Demonstrated:

| Attribute | Where Used | Purpose |
|-----------|-----------|---------|
| `readOnly = true` | `UserService` (class-level) | Optimization for read operations |
| `readOnly = false` | `UserService.createUser()` | Override for write operations |
| `isolation = READ_COMMITTED` | `UserService.deactivateUser()` | Only see committed data |
| `isolation = REPEATABLE_READ` | `CommissionService.calculateCommission()` | Consistent reads during calculation |
| `propagation = REQUIRES_NEW` | `UserService.recordLogin()` | Independent transaction for audit |
| `rollbackFor = Exception.class` | `DisputeService.resolveDispute()` | Roll back on all exceptions |

#### Isolation Levels:

| Level | Dirty Read | Non-Repeatable Read | Phantom Read | Performance |
|-------|-----------|-------------------|-------------|-------------|
| READ_UNCOMMITTED | Yes | Yes | Yes | Fastest |
| READ_COMMITTED | No | Yes | Yes | Fast |
| REPEATABLE_READ | No | No | Possible | Moderate |
| SERIALIZABLE | No | No | No | Slowest |

#### Propagation Types:

| Type | Behavior |
|------|----------|
| REQUIRED | Join existing or create new (default) |
| REQUIRES_NEW | Always create a new, independent transaction |
| NESTED | Create a savepoint within the current transaction |
| SUPPORTS | Use existing or run without transaction |
| MANDATORY | Must have existing transaction |
| NOT_SUPPORTED | Suspend any existing transaction |
| NEVER | Must NOT have a transaction |

**Processor:** `TransactionProcessor.java` - Demonstrates transaction behavior with service calls

---

### 5. Caching Strategies with Spring Cache

**Configuration:** `com.chapman.edu.commissions.orm.config.CacheConfig`

#### Cache Annotations:

| Annotation | Purpose | Example |
|-----------|---------|---------|
| `@Cacheable` | Read-through: cache result, skip method on hit | `UserService.findById()` |
| `@CachePut` | Write-through: always execute, update cache | `UserService.createUser()` |
| `@CacheEvict` | Invalidate: remove stale entries | `UserService.updateUser()` |
| `@CacheEvict(allEntries)` | Nuclear: clear entire cache region | `UserService.clearUserCache()` |
| `@Caching` | Combine multiple cache operations | `CommissionService.approveCalculation()` |

#### Cache Regions:
- `commissionPlans` - Commission plan data (infrequently changed)
- `deals` - Deal data (moderate change frequency)
- `users` - User data (moderate change frequency)
- `calculations` - Commission calculation results

#### Cache Providers (from simple to production):
1. **ConcurrentMapCacheManager** (used here) - Simple HashMap, no expiration
2. **Caffeine** - High-performance, configurable TTL/size
3. **Redis** - Distributed, shared across instances
4. **EhCache/Hazelcast** - Enterprise-grade solutions

**Processor:** `CacheProcessor.java` - Demonstrates cache hits, misses, and eviction

---

## Project Structure

```
com.chapman.edu.commissions.orm/
├── CommissionCalculatorOrmApplication.java    # Application entry point
├── config/
│   └── CacheConfig.java                      # Cache configuration
├── entity/                                    # JPA entities with relationship mappings
│   ├── User.java                             # @ElementCollection, self-referential
│   ├── Deal.java                             # @OneToMany with cascade
│   ├── DealProduct.java                      # @ManyToOne (child entity)
│   ├── CommissionPlan.java                   # Aggregate root
│   ├── CommissionRule.java                   # @ManyToOne + @OneToMany
│   ├── CommissionTier.java                   # @ManyToOne
│   ├── RuleCondition.java                    # Leaf entity
│   ├── BonusRule.java                        # @ManyToOne
│   ├── CommissionCalculation.java            # Multiple @ManyToOne
│   ├── BonusCalculation.java                 # Child of calculation
│   ├── AcceleratorCalculation.java           # Child of calculation
│   ├── Dispute.java                          # Multiple User references
│   ├── DisputeComment.java                   # @ManyToOne to Dispute
│   └── [Enums: UserRole, DealStatus, PlanStatus, CommissionStatus, etc.]
├── repository/                                # Spring Data JPA repositories
│   ├── UserRepository.java                   # Derived, JPQL, native queries
│   ├── DealRepository.java                   # EntityGraph, Specifications
│   ├── CommissionPlanRepository.java         # Aggregate loading
│   ├── CommissionCalculationRepository.java  # @Modifying, aggregates
│   ├── DisputeRepository.java                # Complex joins
│   └── DealSpecifications.java               # Criteria API specifications
├── service/                                   # Business logic with transactions + caching
│   ├── UserService.java                      # @Transactional, @Cacheable, isolation levels
│   ├── DealService.java                      # Dynamic search, transaction management
│   ├── CommissionService.java                # Core calculation engine, REPEATABLE_READ
│   └── DisputeService.java                   # REQUIRES_NEW, rollbackFor
├── controller/                                # REST API endpoints
│   ├── UserController.java                   # User CRUD + search
│   ├── DealController.java                   # Deal management + filtering
│   ├── CommissionController.java             # Commission calculation + plans
│   └── DisputeController.java                # Dispute workflow
└── processor/                                 # Educational CommandLineRunner demos
    ├── JpaProcessor.java                     # Query strategies demo
    ├── OrmProcessor.java                     # Entity relationships demo
    ├── MigrationProcessor.java               # Flyway migration demo
    ├── TransactionProcessor.java             # Transaction management demo
    └── CacheProcessor.java                   # Caching strategies demo
```

## Running the Application

### Option 1: Run from IDE (Recommended)

Run the main class directly from your IDE (IntelliJ IDEA, Eclipse, VS Code):

1. Open `CommissionCalculatorOrmApplication.java` located at:
   ```
   src/main/java/com/chapman/edu/commissions/orm/CommissionCalculatorOrmApplication.java
   ```
2. Right-click the file and select **Run 'CommissionCalculatorOrmApplication'**
3. Or click the green **Run** button next to the `main()` method on line 35

### Option 2: Run from Command Line (Maven)

```bash
# From the project root directory
./mvnw spring-boot:run -Dspring-boot.run.main-class=com.chapman.edu.commissions.orm.CommissionCalculatorOrmApplication
```

### What Happens on Startup

The application will:
1. Start with H2 in-memory database
2. Apply Flyway migrations (V1, V2, V3)
3. Run processor demos in order:
   - **JpaProcessor** (Order 1) - Spring Data JPA query methods
   - **OrmProcessor** (Order 2) - Entity relationships & mapping
   - **MigrationProcessor** (Order 3) - Flyway migration status
   - **TransactionProcessor** (Order 4) - Transaction management
   - **CacheProcessor** (Order 5) - Caching strategies
4. Start REST API on port 8081

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/orm/users/{id}` | Get user by ID |
| GET | `/api/orm/users/search?name=john` | Search users by name |
| POST | `/api/orm/deals` | Create a new deal |
| GET | `/api/orm/deals/search?status=WON&minValue=50000` | Search deals with filters |
| POST | `/api/orm/commissions/calculate?dealId=X&planId=Y` | Calculate commission |
| PUT | `/api/orm/commissions/calculations/{id}/approve` | Approve calculation |
| POST | `/api/orm/disputes` | File a dispute |
| GET | `/h2-console` | H2 Database Console (JDBC URL: jdbc:h2:mem:commissiondb) |

## Database

- **Type:** H2 In-Memory Database
- **Console:** http://localhost:8081/h2-console
- **JDBC URL:** `jdbc:h2:mem:commissiondb`
- **Username:** `sa`
- **Password:** (empty)

## Technologies

- Spring Boot 3.4.5
- Spring Data JPA (Hibernate)
- H2 Database
- Flyway Migration
- Spring Cache (ConcurrentMapCacheManager)
- Lombok
