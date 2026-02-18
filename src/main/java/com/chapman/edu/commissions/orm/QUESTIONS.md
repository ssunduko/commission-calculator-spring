# Spring Boot ORM Module - Study Questions

## Section 1: Spring Data JPA Repositories and Custom Query Methods

### Conceptual Questions

1. **What is the purpose of extending `JpaRepository<Entity, ID>` in Spring Data JPA? What methods do you get for free without writing any implementation code?**

2. **Explain the three query creation strategies available in Spring Data JPA (derived queries, JPQL, native SQL). When would you use each one?**

3. **What is the N+1 problem in JPA? Using the Commission Calculator as an example, explain how loading 100 Deal entities with a LAZY `salesRep` relationship could result in 101 SQL queries.**

4. **Compare `@EntityGraph` and `JOIN FETCH` for solving the N+1 problem. What are the advantages and disadvantages of each approach?**

5. **What is the JPA Specification pattern? Why is it preferred over writing individual query methods for every possible filter combination in a search form?**

6. **Explain the difference between `@Query` with JPQL and `@Query(nativeQuery = true)` with SQL. Why does JPQL use entity names and property names rather than table names and column names?**

7. **What does the `@Modifying` annotation do? Why is `clearAutomatically = true` important when using `@Modifying` queries? What entity lifecycle callbacks are bypassed by bulk update queries?**

### Code Analysis Questions

8. **Look at `UserRepository.java`. Explain what SQL query Spring Data JPA generates for the method `findByTerritoryAndActive(String territory, boolean active)`. Write the equivalent SQL query.**

9. **In `DealRepository.java`, the `findDealsWithProductsBySalesRepAndStatus` method uses `SELECT DISTINCT d FROM Deal d LEFT JOIN FETCH d.products`. Why is `DISTINCT` necessary here? What happens without it?**

10. **Examine `DealSpecifications.java`. Write a service method that builds a dynamic query to find all WON deals with a value between $25,000 and $100,000 that belong to sales rep "usr-001", using Specifications.**

---

## Section 2: Entity Relationships, Mapping Strategies, and Database Design

### Conceptual Questions

11. **What is the difference between the "owning side" and the "inverse side" of a bidirectional JPA relationship? Which side controls the foreign key column? What happens if you only set the inverse side?**

12. **Explain the difference between `@ElementCollection` and `@OneToMany`. When would you use each? Use the `User.roles` field as an example of `@ElementCollection`.**

13. **What does `CascadeType.ALL` do? List all the cascade operations it includes. Using `Deal -> DealProduct` as an example, explain what happens when you call `dealRepository.save(deal)` after adding products to the deal.**

14. **What is `orphanRemoval = true`? How does it differ from `CascadeType.REMOVE`? Give a scenario using `Deal.products` where the difference matters.**

15. **Why does the `Dispute` entity have TWO `@ManyToOne` relationships to `User` (salesRep and manager)? What problem would arise if both used the same `@JoinColumn` name?**

16. **Explain the Aggregate Root pattern as implemented by `CommissionPlan`. Why do the child entities (`CommissionRule`, `CommissionTier`, `BonusRule`) use `CascadeType.ALL`, but `Deal.calculations` does NOT use cascading?**

### Code Analysis Questions

17. **Look at `User.java`. Explain why `equals()` uses `return id != null && id.equals(user.id)` instead of `Objects.equals(id, user.id)`. What would happen with `Objects.equals` when both `id` fields are null?**

18. **The `User` entity uses `FetchType.EAGER` for `@ElementCollection roles` but `FetchType.LAZY` for `@OneToMany directReports`. Justify this design decision.**

19. **In `CommissionPlan.java`, the `rules` list uses `@OrderBy("priority ASC")`. How does this translate to SQL? What is the advantage of database-level ordering vs. application-level sorting?**

---

## Section 3: Database Migration with Flyway

### Conceptual Questions

20. **What problem does database migration solve? Why is manually running SQL scripts on production databases risky?**

21. **Explain the Flyway naming convention for versioned migrations (`V{version}__{description}.sql`). What happens if you accidentally use a single underscore instead of double?**

22. **Why should you NEVER modify an already-applied Flyway migration? What error would Flyway produce? What should you do instead?**

23. **What is the difference between versioned migrations (`V1__...`) and repeatable migrations (`R__...`)? Give an example of when you would use each type.**

24. **Compare Flyway and Liquibase. Under what circumstances would you choose one over the other?**

25. **What is the purpose of `spring.jpa.hibernate.ddl-auto=validate` when used with Flyway? What would happen if you used `ddl-auto=create-drop` in production?**

### Code Analysis Questions

26. **Look at `V1__create_commission_schema.sql`. Why does the `user_roles` table use `ON DELETE CASCADE` on its foreign key to `users`? What would happen WITHOUT cascade when you try to delete a user who has roles?**

27. **In `V3__add_audit_columns.sql`, why are the new columns added with `DEFAULT` values? What would happen if you added a `NOT NULL` column without a default to a table that already has data?**

28. **The migration `V2__seed_sample_data.sql` separates schema (V1) from data (V2). Why is this a best practice? What problems could arise from mixing DDL and DML in the same migration?**

---

## Section 4: Transaction Management and Isolation Levels

### Conceptual Questions

29. **Explain the ACID properties of a transaction (Atomicity, Consistency, Isolation, Durability) using the commission calculation process as a real-world example.**

30. **What is a "dirty read"? A "non-repeatable read"? A "phantom read"? For each, provide a concrete example using the Commission Calculator domain.**

31. **Why does `CommissionService.calculateCommission()` use `Isolation.REPEATABLE_READ`? What specific data inconsistency could occur with `READ_COMMITTED` during a multi-step commission calculation?**

32. **Explain `Propagation.REQUIRES_NEW`. Why is it used for `UserService.recordLogin()`? What happens to the outer transaction when a REQUIRES_NEW method is called?**

33. **By default, Spring's `@Transactional` rolls back on `RuntimeException` but NOT on checked `Exception`. Why? When would you use `rollbackFor = Exception.class`?**

34. **What is the "self-invocation" problem with `@Transactional`? Why does calling a `@Transactional` method from within the same class bypass the transaction? How would you fix it?**

35. **What does `readOnly = true` do in a transaction? List at least three performance benefits it provides.**

### Code Analysis Questions

36. **In `DealService.createDeal()`, a `Deal` and its `DealProducts` are saved in a single transaction. Explain what happens at the database level if saving the third product throws a `DataIntegrityViolationException`. Are the Deal and first two products persisted?**

37. **`DisputeService.resolveDispute()` uses `rollbackFor = Exception.class`. Why is this more restrictive than the default? Give an example of a checked exception that would be caught by this but not by the default rollback behavior.**

38. **Compare `UserService.createUser()` (uses `@CachePut`) with `UserService.updateUser()` (uses `@CacheEvict`). Why are different cache strategies appropriate for create vs. update?**

---

## Section 5: Caching Strategies with Spring Cache

### Conceptual Questions

39. **What is the difference between `@Cacheable` and `@CachePut`? When should you use each?**

40. **Explain the "cache-aside" (lazy loading) pattern. How does Spring's `@Cacheable` implement this pattern?**

41. **What is a "cache stampede" (thundering herd problem)? How can `@Cacheable(sync = true)` help prevent it?**

42. **Why is caching `CommissionPlan` objects a good idea, but caching paginated `Deal` search results is problematic?**

43. **What happens if you cache entities that have LAZY-loaded relationships? Why might you get a `LazyInitializationException` when accessing cached objects?**

44. **Compare ConcurrentMapCacheManager, Caffeine, and Redis as cache providers. When would you use each one? What are the trade-offs?**

45. **What is cache invalidation? Why is it considered one of the "two hard problems in computer science"? Give an example of stale cache data in the Commission Calculator.**

### Code Analysis Questions

46. **Look at `UserService.findById()`. Explain the SpEL expression `@Cacheable(value = "users", key = "#id")`. What is the cache key for `findById("usr-001")`? How does Spring know to check the cache before executing the method?**

47. **In `CommissionService.calculateCommission()`, the `@CacheEvict(value = "calculations", allEntries = true)` clears the entire calculations cache. Why not use a specific key? What is the trade-off of `allEntries = true`?**

48. **`UserService` uses `@CacheEvict` on `updateUser()` but `@CachePut` on `createUser()`. What would happen if you used `@Cacheable` instead of `@CachePut` on `createUser()`? Would the new user be saved to the database?**

---

## Integration Questions

49. **Trace the complete flow of calculating a commission: Starting from the REST API call `POST /api/orm/commissions/calculate?dealId=deal-001&planId=plan-001`, describe every layer involved (controller -> service -> repository -> database) and identify where transactions, caching, and entity relationships play a role.**

50. **Design a new feature: "Commission Recalculation". When a dispute is approved, the commission should be recalculated using the corrected data. Describe:**
    - **Which entities and repositories are involved?**
    - **What transaction isolation level would you use and why?**
    - **How would you handle cache invalidation?**
    - **What happens if the recalculation fails after the dispute status is already updated?**

51. **You notice that the application is slow when loading the "Sales Dashboard" which shows each sales rep with their deals, calculations, and total commission. The current implementation makes separate queries for each sales rep. Propose three different optimization strategies using concepts from this module (e.g., JOIN FETCH, @EntityGraph, caching, native queries).**

52. **A production deployment uses Flyway for migrations and Redis for caching across 3 application instances. A new developer modifies V2__seed_sample_data.sql to add more test data and deploys to production. What happens? How should this be handled instead?**
