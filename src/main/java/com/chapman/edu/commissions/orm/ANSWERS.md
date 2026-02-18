# Spring Boot ORM Module - Study Answers

## Section 1: Spring Data JPA Repositories and Custom Query Methods

### Conceptual Questions

---

**1. What is the purpose of extending `JpaRepository<Entity, ID>` in Spring Data JPA? What methods do you get for free without writing any implementation code?**

Extending `JpaRepository<Entity, ID>` tells Spring Data JPA to generate a fully functional repository implementation at runtime — you write only the interface, never the implementation class.

The two generic type parameters are:
- `Entity`: The JPA entity class (e.g., `User`)
- `ID`: The type of the entity's primary key (e.g., `String` for UUID-based IDs)

Methods you get for free (inherited from the repository hierarchy):

| Method | Source Interface | Description |
|--------|-----------------|-------------|
| `save(entity)` | CrudRepository | Insert or update an entity |
| `saveAll(entities)` | CrudRepository | Batch save |
| `findById(id)` | CrudRepository | Find one entity by primary key |
| `existsById(id)` | CrudRepository | Check if entity exists |
| `findAll()` | ListCrudRepository | Get all entities as a List |
| `findAll(Pageable)` | PagingAndSortingRepository | Paginated query |
| `findAll(Sort)` | PagingAndSortingRepository | Sorted query |
| `count()` | CrudRepository | Count all entities |
| `deleteById(id)` | CrudRepository | Delete by primary key |
| `delete(entity)` | CrudRepository | Delete an entity |
| `deleteAll()` | CrudRepository | Delete all entities |
| `flush()` | JpaRepository | Flush pending changes to DB |
| `saveAndFlush(entity)` | JpaRepository | Save + immediate flush |

The repository hierarchy is: `Repository` → `CrudRepository` → `ListCrudRepository` → `JpaRepository`.

**Example from this project:**
```java
// UserRepository.java — just declaring the interface gives you all CRUD methods
@Repository
public interface UserRepository extends JpaRepository<User, String> {
    // No implementation needed — Spring generates it at runtime
}
```

---

**2. Explain the three query creation strategies available in Spring Data JPA (derived queries, JPQL, native SQL). When would you use each one?**

**Strategy 1: Derived Query Methods**
Spring parses the method name and generates the query automatically.

```java
// UserRepository.java
Optional<User> findByUsername(String username);
// Generated: SELECT * FROM users WHERE username = ?

List<User> findByTerritoryAndActive(String territory, boolean active);
// Generated: SELECT * FROM users WHERE territory = ? AND active = ?
```

**When to use:** Simple queries with 1-3 conditions where the method name remains readable.

**Strategy 2: JPQL (Java Persistence Query Language)**
An object-oriented query language that operates on entities and properties, not tables and columns.

```java
// UserRepository.java
@Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role AND u.active = true")
List<User> findActiveUsersByRole(@Param("role") UserRole role);
```

**When to use:** Queries that involve JOINs, aggregate functions, subqueries, or complex logic that would make derived method names unreadable. JPQL is database-portable — it works the same on H2, MySQL, PostgreSQL, etc.

**Strategy 3: Native SQL**
Raw SQL executed directly against the database.

```java
// UserRepository.java
@Query(value = "SELECT u.* FROM users u INNER JOIN deals d ON d.sales_rep_id = u.id ...",
       nativeQuery = true)
List<User> findTopPerformers(@Param("limit") int limit);
```

**When to use:** When you need database-specific features (window functions, CTEs, stored procedures) or when the query cannot be efficiently expressed in JPQL.

**Trade-off summary:**

| Strategy | Portability | Complexity | Readability |
|----------|------------|------------|-------------|
| Derived | Database-portable | Low only | High for simple queries |
| JPQL | Database-portable | Medium-High | Medium |
| Native SQL | Database-specific | Any | Depends on SQL complexity |

---

**3. What is the N+1 problem in JPA? Using the Commission Calculator as an example, explain how loading 100 Deal entities with a LAZY `salesRep` relationship could result in 101 SQL queries.**

The N+1 problem occurs when loading a collection of entities (N) with lazy-loaded relationships triggers a separate SQL query for each relationship access.

**Example with the Commission Calculator:**

The `Deal` entity has a `@ManyToOne(fetch = FetchType.LAZY)` relationship to `User` (the sales rep):

```java
// Deal.java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "sales_rep_id", nullable = false)
private User salesRep;
```

If you load 100 deals and then access each deal's sales rep:

```java
List<Deal> deals = dealRepository.findAll();  // Query 1: SELECT * FROM deals
for (Deal deal : deals) {
    String repName = deal.getSalesRep().getFullName();  // Queries 2-101: SELECT * FROM users WHERE id = ?
}
```

**Query breakdown:**
1. **Query 1:** `SELECT * FROM deals` → returns 100 Deal objects with proxy sales reps
2. **Queries 2–101:** For each deal, `getSalesRep().getFullName()` triggers `SELECT * FROM users WHERE id = ?`

Total: **1 + 100 = 101 queries** for what should be a single query with a JOIN.

Even if many deals share the same sales rep, Hibernate may still issue separate queries (unless the first-level cache hits for already-loaded User entities in the same session).

---

**4. Compare `@EntityGraph` and `JOIN FETCH` for solving the N+1 problem. What are the advantages and disadvantages of each approach?**

Both techniques force eager loading of lazy relationships in a single query.

**@EntityGraph (declarative approach):**
```java
// DealRepository.java
@EntityGraph(attributePaths = {"products", "salesRep"})
Optional<Deal> findWithProductsAndSalesRepById(String id);
```

| Pros | Cons |
|------|------|
| Clean, declarative syntax | Less flexible — cannot add WHERE conditions on the joined entity |
| Works with derived query methods | Can cause Cartesian product issues with multiple collections |
| Easy to read and maintain | Limited to attribute paths (no nested filtering) |

**JOIN FETCH (JPQL approach):**
```java
// DealRepository.java
@Query("SELECT DISTINCT d FROM Deal d LEFT JOIN FETCH d.products " +
       "WHERE d.salesRep.id = :salesRepId AND d.status = :status")
List<Deal> findDealsWithProductsBySalesRepAndStatus(...);
```

| Pros | Cons |
|------|------|
| Full control over the query (filtering, ordering) | Requires writing JPQL |
| Can add conditions on joined entities | `DISTINCT` often required to avoid duplicates |
| Can selectively fetch only what's needed | More verbose |

**Key difference:** `@EntityGraph` is a "fetch plan" that overrides the default fetch strategy for an existing query. `JOIN FETCH` is an explicit JPQL clause that combines the fetch instruction with query logic.

**When to use each:**
- Use `@EntityGraph` for simple "load this entity with these relationships" scenarios.
- Use `JOIN FETCH` when you need to filter, sort, or apply conditions on the joined entities.

---

**5. What is the JPA Specification pattern? Why is it preferred over writing individual query methods for every possible filter combination in a search form?**

The Specification pattern (from Domain-Driven Design) allows building dynamic, composable queries at runtime by combining simple predicates.

**The problem:** A search form with 5 optional filters creates 2^5 = 32 possible combinations. Writing a repository method for each combination is impractical:
```java
// Without Specifications — combinatorial explosion:
findByStatus(...)
findByStatusAndMinValue(...)
findByStatusAndMinValueAndMaxValue(...)
findByMinValueAndSalesRepId(...)
// ... 28 more methods
```

**The solution:** Each filter becomes a reusable `Specification<Deal>` that can be combined:

```java
// DealSpecifications.java — each filter is a composable predicate
public static Specification<Deal> hasStatus(DealStatus status) {
    return (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("status"), status);
}

public static Specification<Deal> valueGreaterThan(BigDecimal minValue) {
    return (root, query, criteriaBuilder) ->
            criteriaBuilder.greaterThan(root.get("value"), minValue);
}
```

```java
// DealService.java — dynamically compose only the filters that are non-null
Specification<Deal> spec = Specification.where(null);
if (status != null)    spec = spec.and(DealSpecifications.hasStatus(status));
if (minValue != null)  spec = spec.and(DealSpecifications.valueGreaterThan(minValue));
if (maxValue != null)  spec = spec.and(DealSpecifications.valueLessThan(maxValue));
return dealRepository.findAll(spec, pageable);
```

**Requirements:** The repository must extend `JpaSpecificationExecutor<Deal>`:
```java
public interface DealRepository extends JpaRepository<Deal, String>, JpaSpecificationExecutor<Deal> { }
```

---

**6. Explain the difference between `@Query` with JPQL and `@Query(nativeQuery = true)` with SQL. Why does JPQL use entity names and property names rather than table names and column names?**

**JPQL** operates on the **object model** (entities and their properties):
```java
@Query("SELECT u FROM User u WHERE u.firstName = :name")
// Uses: entity name "User", property name "firstName"
```

**Native SQL** operates on the **database schema** (tables and columns):
```java
@Query(value = "SELECT * FROM users u WHERE u.first_name = :name", nativeQuery = true)
// Uses: table name "users", column name "first_name"
```

**Why JPQL uses entity/property names:**

1. **Abstraction:** JPQL abstracts away the database schema. The mapping between `firstName` (Java) and `first_name` (SQL) is defined once in the `@Column` annotation on the entity. JPQL queries reference the Java model, and Hibernate translates to the correct column names.

2. **Portability:** JPQL is database-agnostic. The same JPQL query works on H2, MySQL, PostgreSQL, and Oracle. Native SQL may use database-specific syntax (e.g., `LIMIT` in MySQL vs. `FETCH FIRST` in Oracle).

3. **Relationship navigation:** JPQL can traverse entity relationships using dot notation:
   ```sql
   -- JPQL: Navigate the @ManyToOne relationship
   SELECT u FROM User u WHERE u.manager.id = :managerId

   -- Equivalent native SQL: Must explicitly write the JOIN
   SELECT u.* FROM users u WHERE u.manager_id = :managerId
   ```

4. **Type safety:** JPQL validates entity and property names at startup. A typo like `u.firstNam` fails fast. Native SQL errors only surface at runtime.

---

**7. What does the `@Modifying` annotation do? Why is `clearAutomatically = true` important when using `@Modifying` queries? What entity lifecycle callbacks are bypassed by bulk update queries?**

`@Modifying` tells Spring Data JPA that a `@Query` method performs a write operation (UPDATE or DELETE) rather than a SELECT.

```java
// CommissionCalculationRepository.java
@Modifying(clearAutomatically = true, flushAutomatically = true)
@Query("UPDATE CommissionCalculation cc SET cc.status = :newStatus " +
       "WHERE cc.status = :currentStatus AND cc.calculationDate <= :beforeDate")
int bulkUpdateStatus(...);
```

**Why `clearAutomatically = true` is important:**

JPA maintains a **persistence context** (first-level cache) that holds loaded entities. A `@Modifying` query executes SQL directly against the database, **bypassing the persistence context**. Without clearing:

1. Entity A is loaded (cached in persistence context with `status = CALCULATED`)
2. Bulk update changes A's status to `APPROVED` in the database
3. Reading entity A returns the **stale cached version** with `status = CALCULATED`

`clearAutomatically = true` clears the persistence context after the query, forcing the next read to hit the database.

**Lifecycle callbacks bypassed by bulk operations:**
- `@PreUpdate` / `@PostUpdate` — entity update callbacks
- `@PreRemove` / `@PostRemove` — entity deletion callbacks
- JPA `EntityListener` callbacks
- Hibernate event listeners (e.g., Envers audit logging)
- `@Version` optimistic locking checks

This is because bulk queries execute a single SQL UPDATE/DELETE statement directly, never loading entities into memory.

### Code Analysis Questions

---

**8. Look at `UserRepository.java`. Explain what SQL query Spring Data JPA generates for the method `findByTerritoryAndActive(String territory, boolean active)`. Write the equivalent SQL query.**

Spring Data parses the method name: `findBy` + `Territory` + `And` + `Active`.

- `findBy` → SELECT query
- `Territory` → WHERE clause on the `territory` column
- `And` → AND operator
- `Active` → WHERE clause on the `active` column

**Generated JPQL:**
```sql
SELECT u FROM User u WHERE u.territory = :territory AND u.active = :active
```

**Equivalent SQL (what Hibernate generates):**
```sql
SELECT u.id, u.username, u.email, u.first_name, u.last_name,
       u.password_hash, u.active, u.last_login, u.created_date,
       u.created_by, u.manager_id, u.department, u.territory
FROM users u
WHERE u.territory = ?
  AND u.active = ?
```

The parameter placeholders (`?`) are bound to the method arguments at runtime, preventing SQL injection.

---

**9. In `DealRepository.java`, the `findDealsWithProductsBySalesRepAndStatus` method uses `SELECT DISTINCT d FROM Deal d LEFT JOIN FETCH d.products`. Why is `DISTINCT` necessary here? What happens without it?**

**Why DISTINCT is necessary:**

`LEFT JOIN FETCH d.products` performs a SQL LEFT JOIN between the `deals` and `deal_products` tables. If a deal has 3 products, the JOIN produces 3 rows for that deal (one per product). Without `DISTINCT`, Hibernate creates 3 duplicate `Deal` objects in the result list.

**Example without DISTINCT:**

For deal-001 (Acme Corp) with 2 products (dp-001, dp-002), the SQL JOIN produces:

| deal_id  | title                     | dp_id  |
|----------|---------------------------|--------|
| deal-001 | Acme Corp ERP Implementation | dp-001 |
| deal-001 | Acme Corp ERP Implementation | dp-002 |

Without `DISTINCT`, Hibernate returns a `List<Deal>` containing the **same Deal object twice** (both references point to the same Java object due to first-level cache, but the list has 2 entries).

With `DISTINCT`, Hibernate de-duplicates and returns each Deal only once.

**Important note:** The `DISTINCT` in JPQL serves a dual purpose:
1. It adds `DISTINCT` to the generated SQL
2. It tells Hibernate to de-duplicate the Java entity results

---

**10. Examine `DealSpecifications.java`. Write a service method that builds a dynamic query to find all WON deals with a value between $25,000 and $100,000 that belong to sales rep "usr-001", using Specifications.**

```java
public Page<Deal> findFilteredDeals(Pageable pageable) {
    Specification<Deal> spec = Specification.where(null);

    // Filter: status = WON
    spec = spec.and(DealSpecifications.hasStatus(DealStatus.WON));

    // Filter: value >= $25,000
    spec = spec.and(DealSpecifications.valueGreaterThan(new BigDecimal("25000")));

    // Filter: value < $100,000
    spec = spec.and(DealSpecifications.valueLessThan(new BigDecimal("100000")));

    // Filter: belongs to sales rep usr-001
    spec = spec.and(DealSpecifications.belongsToSalesRep("usr-001"));

    return dealRepository.findAll(spec, pageable);
}
```

This generates SQL equivalent to:
```sql
SELECT * FROM deals
WHERE status = 'WON'
  AND deal_value > 25000
  AND deal_value < 100000
  AND sales_rep_id = 'usr-001'
```

The beauty of Specifications is that each filter is optional — you only `.and()` the ones you need.

---

## Section 2: Entity Relationships, Mapping Strategies, and Database Design

### Conceptual Questions

---

**11. What is the difference between the "owning side" and the "inverse side" of a bidirectional JPA relationship? Which side controls the foreign key column? What happens if you only set the inverse side?**

In a bidirectional relationship, one side **owns** the foreign key column (the "owning side") and the other side is a **read-only mirror** (the "inverse side").

**Owning side:** Has the `@JoinColumn`, controls the FK column in the database.
**Inverse side:** Uses `mappedBy`, is a read-only view of the relationship.

**Example from User.java:**
```java
// OWNING SIDE — controls the manager_id FK column
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "manager_id")
private User manager;

// INVERSE SIDE — read-only mirror, no FK column
@OneToMany(mappedBy = "manager", fetch = FetchType.LAZY)
private List<User> directReports;
```

**What happens if you only set the inverse side:**

```java
manager.getDirectReports().add(salesRep);  // Only sets the inverse side
// The database is NOT updated! The manager_id column remains NULL.

salesRep.setManager(manager);  // Sets the owning side
// The database IS updated: manager_id = manager.id
```

**Rule of thumb:** The `@ManyToOne` side is always the owning side, because it's the table that physically contains the foreign key column.

**Best practice:** Always use helper methods that keep both sides in sync:
```java
public void addProduct(DealProduct product) {
    products.add(product);       // Update inverse side (in-memory)
    product.setDeal(this);       // Update owning side (persisted to DB)
}
```

---

**12. Explain the difference between `@ElementCollection` and `@OneToMany`. When would you use each? Use the `User.roles` field as an example of `@ElementCollection`.**

| Feature | `@ElementCollection` | `@OneToMany` |
|---------|---------------------|--------------|
| For | Value types (enums, strings, embeddables) | Entity types with their own `@Id` |
| Identity | Items have NO primary key | Each item has its own `@Id` |
| Lifecycle | Fully owned by parent | Can be independent |
| Table | Join table with no ID column | Entity table with ID column |

**`@ElementCollection` — User.roles:**
```java
// User.java — roles are enum values, not entities
@ElementCollection(targetClass = UserRole.class, fetch = FetchType.EAGER)
@CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
@Enumerated(EnumType.STRING)
@Column(name = "role")
private Set<UserRole> roles = new HashSet<>();
```

The `user_roles` table has no `id` column — its primary key is `(user_id, role)`. The roles have no identity of their own; they only exist as part of the User.

**`@OneToMany` — Deal.products:**
```java
// Deal.java — products are full entities with their own @Id
@OneToMany(mappedBy = "deal", cascade = CascadeType.ALL, orphanRemoval = true)
private List<DealProduct> products = new ArrayList<>();
```

Each `DealProduct` has its own `id`, `productName`, `price`, etc. It's a full entity that could theoretically exist independently.

**When to use each:**
- `@ElementCollection`: Simple collections of enums, strings, or small value objects. Use when items don't need their own identity.
- `@OneToMany`: When child items are full entities with their own fields, behaviors, and identity.

---

**13. What does `CascadeType.ALL` do? List all the cascade operations it includes. Using `Deal -> DealProduct` as an example, explain what happens when you call `dealRepository.save(deal)` after adding products to the deal.**

`CascadeType.ALL` propagates all JPA persistence operations from the parent entity to its children.

**The six cascade operations:**

| Cascade Type | What it does |
|-------------|-------------|
| `PERSIST` | When parent is saved (new), children are also saved |
| `MERGE` | When parent is updated, children are also updated |
| `REMOVE` | When parent is deleted, children are also deleted |
| `REFRESH` | When parent is refreshed from DB, children are also refreshed |
| `DETACH` | When parent is detached from persistence context, children are too |
| `ALL` | All of the above |

**Example with Deal → DealProduct:**

```java
// Deal.java
@OneToMany(mappedBy = "deal", cascade = CascadeType.ALL, orphanRemoval = true)
private List<DealProduct> products = new ArrayList<>();
```

When you call:
```java
Deal deal = new Deal("Acme Corp", new BigDecimal("85000"), salesRep);
deal.addProduct(new DealProduct("PROD-ERP", "ERP License", 50, new BigDecimal("1200")));
deal.addProduct(new DealProduct("PROD-IMPL", "Services", 1, new BigDecimal("25000")));
dealRepository.save(deal);
```

Hibernate executes:
```sql
INSERT INTO deals (id, title, deal_value, status, sales_rep_id, ...) VALUES (?, ?, ?, ?, ?, ...);
INSERT INTO deal_products (id, product_id, product_name, quantity, price, deal_id) VALUES (?, ?, ?, ?, ?, ?);
INSERT INTO deal_products (id, product_id, product_name, quantity, price, deal_id) VALUES (?, ?, ?, ?, ?, ?);
```

A single `save(deal)` call persists the Deal and both DealProducts in the same transaction. Without `CascadeType.ALL`, you would need to explicitly save each product separately.

---

**14. What is `orphanRemoval = true`? How does it differ from `CascadeType.REMOVE`? Give a scenario using `Deal.products` where the difference matters.**

Both deal with deleting children, but they are triggered by different actions:

| Feature | `CascadeType.REMOVE` | `orphanRemoval = true` |
|---------|---------------------|----------------------|
| Trigger | Parent entity is **deleted** | Child is **removed from parent's collection** |
| Scenario | `dealRepository.delete(deal)` | `deal.getProducts().remove(product)` |

**Scenario where the difference matters:**

```java
Deal deal = dealRepository.findById("deal-001").get();

// Remove one product from the deal (NOT deleting the deal)
DealProduct productToRemove = deal.getProducts().get(0);
deal.getProducts().remove(productToRemove);
dealRepository.save(deal);
```

**With `orphanRemoval = true`:** The removed product is automatically deleted from the database:
```sql
DELETE FROM deal_products WHERE id = ?
```

**With only `CascadeType.REMOVE` (no orphanRemoval):** The product is removed from the in-memory collection but **remains in the database**. It becomes an "orphan" — a `deal_products` row that still references the deal but is no longer in the Deal's products list.

**Summary:** `CascadeType.REMOVE` means "delete children when the parent is deleted." `orphanRemoval = true` means "delete children when they are disconnected from the parent's collection."

---

**15. Why does the `Dispute` entity have TWO `@ManyToOne` relationships to `User` (salesRep and manager)? What problem would arise if both used the same `@JoinColumn` name?**

The Dispute entity models two distinct business roles:
- **salesRep:** The user who filed the dispute
- **manager:** The manager assigned to review the dispute

```java
// Dispute.java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "sales_rep_id", nullable = false)
private User salesRep;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "manager_id")
private User manager;
```

**If both used the same `@JoinColumn` name (e.g., both `user_id`):**

1. **Schema conflict:** The `disputes` table would have a single `user_id` column trying to store two different user references. Only one user could be stored — the second relationship would overwrite the first.

2. **Mapping ambiguity:** Hibernate would not know which `User` relationship the column refers to, causing a `MappingException` at startup.

3. **Data loss:** Even if Hibernate allowed it, you could only record either the sales rep OR the manager, not both.

Each `@JoinColumn` must have a **unique column name** so the database can store both foreign keys independently.

---

**16. Explain the Aggregate Root pattern as implemented by `CommissionPlan`. Why do the child entities (`CommissionRule`, `CommissionTier`, `BonusRule`) use `CascadeType.ALL`, but `Deal.calculations` does NOT use cascading?**

**Aggregate Root pattern:** An aggregate is a cluster of entities that are treated as a single unit for data changes. The "root" entity is the only entry point for modifications — all changes to child entities go through the root.

**CommissionPlan as Aggregate Root:**

```java
// CommissionPlan.java — root entity with cascading to children
@OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
private List<CommissionRule> rules;

@OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
private List<CommissionTier> tiers;

@OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
private List<BonusRule> bonuses;
```

Rules, tiers, and bonuses have no meaning without their plan. They are created, modified, and deleted through the plan. `CascadeType.ALL` + `orphanRemoval` ensures:
- Creating a plan creates its rules/tiers/bonuses
- Deleting a plan deletes all children
- Removing a rule from the list deletes it from the database

**Why Deal.calculations does NOT cascade:**

```java
// Deal.java — no cascade on calculations
@OneToMany(mappedBy = "deal", fetch = FetchType.LAZY)
private List<CommissionCalculation> calculations;
```

Commission calculations have an **independent lifecycle**:
- They are created by `CommissionService.calculateCommission()`, not by modifying the Deal
- They go through their own status workflow: CALCULATED → APPROVED → PAID
- Deleting a Deal should NOT automatically delete its commission calculations (financial records must be preserved for audit)
- Calculations reference multiple parents (Deal, User, CommissionPlan) — they don't belong exclusively to a Deal

**Rule of thumb:** Use cascading when children are "parts" of the parent (composition). Don't use cascading when children are independent entities that merely reference the parent (association).

### Code Analysis Questions

---

**17. Look at `User.java`. Explain why `equals()` uses `return id != null && id.equals(user.id)` instead of `Objects.equals(id, user.id)`. What would happen with `Objects.equals` when both `id` fields are null?**

```java
// User.java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    User user = (User) o;
    return id != null && id.equals(user.id);  // Note: returns false when id is null
}
```

**The problem with `Objects.equals(id, user.id)`:**

`Objects.equals(null, null)` returns `true`. This means two **unsaved entities** (both with `id = null`) would be considered equal:

```java
User user1 = new User("alice", "alice@test.com", "Alice", "A");  // id = null
User user2 = new User("bob", "bob@test.com", "Bob", "B");        // id = null

// With Objects.equals: user1.equals(user2) → true! (both null IDs)
// This is WRONG — they are different users!
```

If you add both to a `HashSet`, only one would be stored because the set considers them duplicates.

**The correct implementation** (`id != null && id.equals(user.id)`) returns `false` when either ID is null. Two unsaved entities are never considered equal (unless they are the same object reference, caught by `this == o`).

**The companion `hashCode()`:**
```java
@Override
public int hashCode() {
    return getClass().hashCode();  // Constant for all User instances
}
```

This returns the same value for all User instances. While this seems inefficient for HashMap (all Users go in the same bucket), it's necessary because the `id` changes from `null` to a UUID when the entity is persisted. If `hashCode()` used `id`, an entity's hash would change after saving, breaking its position in HashSets and HashMaps.

---

**18. The `User` entity uses `FetchType.EAGER` for `@ElementCollection roles` but `FetchType.LAZY` for `@OneToMany directReports`. Justify this design decision.**

```java
// FetchType.EAGER — always loaded with the User
@ElementCollection(targetClass = UserRole.class, fetch = FetchType.EAGER)
private Set<UserRole> roles;

// FetchType.LAZY — loaded only when accessed
@OneToMany(mappedBy = "manager", fetch = FetchType.LAZY)
private List<User> directReports;
```

**Why roles are EAGER:**
1. **Almost always needed:** Roles are checked for authorization on virtually every request (e.g., `@PreAuthorize`, security filters). Loading them lazily would just delay an inevitable query.
2. **Small dataset:** A user typically has 1–3 roles. The performance cost of eager loading is negligible.
3. **Simple values:** Roles are enum values, not full entities. The JOIN to `user_roles` adds minimal overhead.

**Why directReports are LAZY:**
1. **Rarely needed:** Most operations (login, authorization, profile views) don't need the list of direct reports.
2. **Potentially large:** A manager could have 5, 10, or 50 direct reports. Eager loading all of them (plus their own eager-loaded roles) every time any User is loaded would be wasteful.
3. **Full entities:** Each direct report is a complete User entity with its own properties and relationships. Loading the full User graph eagerly could trigger cascading queries.

**General rule:** Use EAGER for small, frequently-needed collections of value types. Use LAZY for large or rarely-accessed collections of entities.

---

**19. In `CommissionPlan.java`, the `rules` list uses `@OrderBy("priority ASC")`. How does this translate to SQL? What is the advantage of database-level ordering vs. application-level sorting?**

```java
// CommissionPlan.java
@OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
@OrderBy("priority ASC")
private List<CommissionRule> rules;
```

**SQL translation:**

When Hibernate loads the rules collection, it appends `ORDER BY priority ASC` to the query:

```sql
SELECT cr.id, cr.name, cr.description, cr.rate, cr.type, cr.priority, cr.plan_id
FROM commission_rules cr
WHERE cr.plan_id = ?
ORDER BY cr.priority ASC
```

**Advantages of database-level ordering:**

1. **Efficiency:** The database engine uses indexes (e.g., `idx_rule_priority`) to return sorted results without a separate sort step. Application-level sorting requires loading all data into memory first.

2. **Consistency:** Every time the collection is loaded, the order is guaranteed. Application-level sorting could be accidentally skipped in some code paths.

3. **Pagination compatibility:** If combined with `LIMIT`/`OFFSET`, database-level ordering ensures correct page contents. Application-level sorting after pagination would sort only the current page.

4. **Reduced memory:** For large collections, the database sorts during query execution. Application-level sorting (`Collections.sort()`) requires all elements in heap memory.

---

## Section 3: Database Migration with Flyway

### Conceptual Questions

---

**20. What problem does database migration solve? Why is manually running SQL scripts on production databases risky?**

**Problems solved by database migration:**

1. **Version tracking:** Migrations maintain a history of every schema change. You can see exactly what the database looks like at any point in time.

2. **Consistency across environments:** The same migration files are applied to dev, staging, and production, ensuring identical schemas. Manual scripts depend on humans remembering which scripts were already run.

3. **Automation:** Migrations run automatically on application startup. No manual intervention required.

4. **Team coordination:** When multiple developers change the schema, migrations prevent conflicts by enforcing sequential versioning.

**Why manual scripts are risky:**

1. **Human error:** Forgetting to run a script, running scripts out of order, or running the same script twice.
2. **No audit trail:** No record of what was applied, when, or by whom.
3. **Inconsistent environments:** Dev might have changes that staging doesn't, leading to "works on my machine" bugs.
4. **No rollback path:** If a manual script breaks production, you have no standardized way to undo it.
5. **Partial application:** A script might fail halfway through, leaving the database in an inconsistent state.

---

**21. Explain the Flyway naming convention for versioned migrations (`V{version}__{description}.sql`). What happens if you accidentally use a single underscore instead of double?**

**Convention:** `V{version}__{description}.sql`

| Component | Required | Example | Purpose |
|-----------|----------|---------|---------|
| `V` | Yes | `V` | Marks as a versioned migration |
| `{version}` | Yes | `1`, `2.1`, `20240101` | Determines execution order |
| `__` | Yes | `__` | Double underscore separator |
| `{description}` | Yes | `create_commission_schema` | Human-readable description |
| `.sql` | Yes | `.sql` | File extension |

**Examples from this project:**
- `V1__create_commission_schema.sql` — Version 1, creates tables
- `V2__seed_sample_data.sql` — Version 2, inserts sample data
- `V3__add_audit_columns.sql` — Version 3, adds audit columns

**What happens with a single underscore (e.g., `V1_create_schema.sql`):**

Flyway would interpret `1_create_schema` as the **version number** (not `1` as version and `create_schema` as description). This typically causes:
- Flyway may fail to parse the version number, throwing a `FlywayException`
- Or it may interpret the entire string as the version, resulting in confusing version numbers
- The migration may be silently ignored because it doesn't match Flyway's expected naming pattern

---

**22. Why should you NEVER modify an already-applied Flyway migration? What error would Flyway produce? What should you do instead?**

**Why you must never modify applied migrations:**

When Flyway applies a migration, it records a **checksum** (hash of the file contents) in the `flyway_schema_history` table. On each subsequent startup, Flyway recalculates the checksum and compares it.

**Error produced:**

```
Flyway: Validate failed: Migrations have failed validation
Detected resolved migration not applied to database: V1__create_commission_schema.sql
Migration checksum mismatch for migration version 1
-> Applied to database : 1234567890
-> Resolved locally    : 9876543210
```

This error halts the application startup completely — the application will not start.

**What to do instead:**

Create a **new** migration file with the next version number:

```
V1__create_commission_schema.sql    ← Already applied. NEVER change.
V2__seed_sample_data.sql            ← Already applied. NEVER change.
V3__add_audit_columns.sql           ← Already applied. NEVER change.
V4__fix_missing_index.sql           ← NEW migration for your change
```

As demonstrated in this project: V1 created the schema, V2 seeded data, and V3 added audit columns — each as a separate, immutable migration.

---

**23. What is the difference between versioned migrations (`V1__...`) and repeatable migrations (`R__...`)? Give an example of when you would use each type.**

| Feature | Versioned (`V1__...`) | Repeatable (`R__...`) |
|---------|----------------------|----------------------|
| Execution | Applied **once**, never re-run | Re-run whenever the **checksum changes** |
| Ordering | Run in version order (V1, V2, V3...) | Run after all versioned migrations |
| Use case | Schema changes, data migrations | Views, stored procedures, reference data |

**Versioned migration example (used in this project):**
```sql
-- V1__create_commission_schema.sql — Applied once, creates tables
CREATE TABLE users (...);
CREATE TABLE deals (...);
```

**Repeatable migration example:**
```sql
-- R__create_commission_summary_view.sql — Re-applied when the view definition changes
CREATE OR REPLACE VIEW v_commission_summary AS
SELECT cc.id, d.title, u.first_name || ' ' || u.last_name AS rep_name, ...
FROM commission_calculations cc
JOIN deals d ON cc.deal_id = d.id
JOIN users u ON cc.sales_rep_id = u.id;
```

Views, functions, and stored procedures are ideal for repeatable migrations because they can be safely replaced (`CREATE OR REPLACE`). Table schema changes (CREATE TABLE, ALTER TABLE) must be versioned because they can't be safely re-applied.

---

**24. Compare Flyway and Liquibase. Under what circumstances would you choose one over the other?**

| Feature | Flyway | Liquibase |
|---------|--------|-----------|
| Migration format | SQL files (primary) | XML, YAML, JSON, or SQL |
| Learning curve | Simple | More complex |
| Rollback | Manual (or Flyway Teams paid) | Built-in rollback support |
| Diff generation | No | Yes (compare DB to model) |
| Database support | Many | More (including NoSQL) |
| Approach | SQL-first | Abstraction-first |

**Choose Flyway when:**
- Your team is comfortable writing SQL
- You want simplicity and minimal configuration
- You don't need automatic rollback generation
- You're using a single database platform

**Choose Liquibase when:**
- You need to support multiple database platforms from the same changelog
- You need built-in rollback capabilities
- You want to generate migrations by comparing database schemas
- You prefer declarative XML/YAML over writing SQL

**This project uses Flyway** because the SQL-first approach makes it easier for students to understand exactly what happens at the database level.

---

**25. What is the purpose of `spring.jpa.hibernate.ddl-auto=validate` when used with Flyway? What would happen if you used `ddl-auto=create-drop` in production?**

**`ddl-auto=validate`:** Hibernate compares its entity mappings against the existing database schema on startup. If the `@Entity` classes don't match the tables (e.g., a column is missing or has the wrong type), the application **fails to start** with a `SchemaManagementException`.

This is the correct setting when using Flyway because:
- Flyway manages the schema (DDL)
- Hibernate validates the schema matches its entity model
- Any mismatch is caught immediately at startup

**What `ddl-auto=create-drop` does in production:**

1. On startup: **DROPS all tables** and recreates them from entity annotations
2. On shutdown: **DROPS all tables** again

This means:
- **All production data is permanently destroyed** on every deployment
- Every table, index, foreign key, and view is recreated from scratch
- Data seeded by Flyway migrations is lost
- Customer data, financial records, audit trails — all deleted

**Summary of ddl-auto options:**

| Value | What it does | Use in |
|-------|-------------|--------|
| `validate` | Validates schema matches entities | **Production** (with Flyway) |
| `none` | Does nothing | Production (alternative) |
| `update` | Adds missing columns/tables | Development only |
| `create` | Drops and recreates all tables on startup | Testing only |
| `create-drop` | create + drops on shutdown | Unit tests only |

### Code Analysis Questions

---

**26. Look at `V1__create_commission_schema.sql`. Why does the `user_roles` table use `ON DELETE CASCADE` on its foreign key to `users`? What would happen WITHOUT cascade when you try to delete a user who has roles?**

```sql
-- V1__create_commission_schema.sql
CREATE TABLE user_roles (
    user_id VARCHAR(36) NOT NULL,
    role VARCHAR(50) NOT NULL,
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role)
);
```

**Why `ON DELETE CASCADE`:**

`user_roles` is an `@ElementCollection` table — roles have no independent existence without a user. When a user is deleted, their roles should be automatically deleted.

**Without `ON DELETE CASCADE`:**

```sql
DELETE FROM users WHERE id = 'usr-001';
-- ERROR: Cannot delete or update a parent row:
-- a foreign key constraint fails (`user_roles`, CONSTRAINT `fk_user_roles_user`
-- FOREIGN KEY (`user_id`) REFERENCES `users` (`id`))
```

The database would **refuse to delete the user** because child rows in `user_roles` still reference the user's ID. You would need to manually delete the roles first:

```sql
DELETE FROM user_roles WHERE user_id = 'usr-001';  -- Delete roles first
DELETE FROM users WHERE id = 'usr-001';              -- Then delete user
```

**Note:** Not all foreign keys in this schema use `ON DELETE CASCADE`. For example, `deals.sales_rep_id` references `users.id` without cascade — deleting a user should NOT automatically delete their deals.

---

**27. In `V3__add_audit_columns.sql`, why are the new columns added with `DEFAULT` values? What would happen if you added a `NOT NULL` column without a default to a table that already has data?**

```sql
-- V3__add_audit_columns.sql
ALTER TABLE deals ADD COLUMN version BIGINT DEFAULT 0;
ALTER TABLE commission_plans ADD COLUMN version BIGINT DEFAULT 0;
ALTER TABLE commission_calculations ADD COLUMN version BIGINT DEFAULT 0;
```

**Why `DEFAULT` values are necessary:**

When V3 runs, the tables already contain data from V2 (seed data). The `DEFAULT 0` ensures existing rows get a valid value for the new column.

**Without a default, adding a `NOT NULL` column to a populated table:**

```sql
ALTER TABLE deals ADD COLUMN version BIGINT NOT NULL;
-- ERROR: Column 'version' cannot be null
-- (Existing rows have no value for 'version')
```

The database cannot satisfy the `NOT NULL` constraint for the existing rows because there is no value to assign. The migration fails.

**Safe strategies for adding columns to populated tables:**

1. **Add with DEFAULT** (used here): `ADD COLUMN version BIGINT DEFAULT 0`
2. **Add as nullable, backfill, then add constraint:**
   ```sql
   ALTER TABLE deals ADD COLUMN version BIGINT;                 -- Nullable
   UPDATE deals SET version = 0;                                -- Backfill
   ALTER TABLE deals ALTER COLUMN version SET NOT NULL;          -- Add constraint
   ```

---

**28. The migration `V2__seed_sample_data.sql` separates schema (V1) from data (V2). Why is this a best practice? What problems could arise from mixing DDL and DML in the same migration?**

**Why separation is a best practice:**

1. **Independent review:** Schema changes (DDL) can be reviewed for structural correctness, while data changes (DML) can be reviewed for business correctness. Mixing them makes code review harder.

2. **Different rollback strategies:** Schema changes (CREATE TABLE, ALTER TABLE) require different rollback approaches than data changes (INSERT, UPDATE). Separating them makes rollback planning clearer.

3. **Selective application:** In some environments (e.g., production), you want the schema but not the sample data. With separate files, you can control which migrations apply per environment.

4. **Transaction behavior:** Some databases (like PostgreSQL) support transactional DDL (CREATE TABLE inside a transaction), while others (like MySQL) auto-commit DDL statements. Mixing DDL and DML can lead to:
   - A table being created (auto-committed DDL) but INSERTs failing (rolled back DML)
   - The database left in an inconsistent state

5. **Testing:** Schema migrations can be tested by validating the table structure. Data migrations can be tested by querying the inserted data. Mixed migrations require both types of validation.

---

## Section 4: Transaction Management and Isolation Levels

### Conceptual Questions

---

**29. Explain the ACID properties of a transaction (Atomicity, Consistency, Isolation, Durability) using the commission calculation process as a real-world example.**

Using `CommissionService.calculateCommission()` as the example:

**Atomicity — "All or nothing"**

The commission calculation involves multiple steps: loading the deal, finding the tier, calculating the base commission, applying bonuses, and saving the result. If applying a bonus fails (e.g., data integrity error), the **entire transaction rolls back** — the base commission calculation is not saved either. You never end up with a partial calculation in the database.

**Consistency — "Valid state to valid state"**

Before the transaction: the database has a WON deal without a calculation.
After the transaction: the database has a WON deal with a properly calculated commission.
The database moves from one valid state to another. Constraints (NOT NULL, foreign keys, check constraints) are enforced. You cannot save a calculation with a null `deal_id` or reference a non-existent plan.

**Isolation — "Concurrent transactions don't interfere"**

If two users simultaneously trigger commission calculations, `Isolation.REPEATABLE_READ` ensures each calculation sees a consistent snapshot of the data. If User A reads a deal value of $85,000, it remains $85,000 throughout the calculation — even if User B updates the deal to $90,000 in a concurrent transaction.

**Durability — "Committed = permanent"**

Once `calculateCommission()` commits, the calculation result is permanently stored. Even if the server crashes immediately after, the data survives. This is guaranteed by the database's write-ahead logging (WAL) mechanism.

---

**30. What is a "dirty read"? A "non-repeatable read"? A "phantom read"? For each, provide a concrete example using the Commission Calculator domain.**

**Dirty Read — Reading uncommitted data**

Transaction A updates deal-001's value from $85,000 to $90,000 but hasn't committed yet.
Transaction B reads deal-001 and sees $90,000.
Transaction A rolls back (maybe the update was invalid).
Transaction B used $90,000 for its commission calculation — but the real value is still $85,000.

*Prevented by: READ_COMMITTED and above.*

**Non-Repeatable Read — Same query, different result**

Transaction A reads deal-001's value: $85,000.
Transaction B updates deal-001 to $90,000 and commits.
Transaction A reads deal-001 again: $90,000.

In `CommissionService.calculateCommission()`, the deal value is read in Step 1, and the calculation happens in Step 4. If the value changes between these steps, the commission is calculated on a different value than what was originally read. This is why `REPEATABLE_READ` is used.

*Prevented by: REPEATABLE_READ and above.*

**Phantom Read — New rows appear in a range query**

Transaction A queries: "Find all WON deals for usr-001" → returns 2 deals.
Transaction B inserts a new WON deal for usr-001 and commits.
Transaction A re-runs the same query → returns 3 deals.

A new row (phantom) appeared in the result set. This could affect aggregate calculations like "total won deal value."

*Prevented by: SERIALIZABLE.*

---

**31. Why does `CommissionService.calculateCommission()` use `Isolation.REPEATABLE_READ`? What specific data inconsistency could occur with `READ_COMMITTED` during a multi-step commission calculation?**

```java
@Transactional(isolation = Isolation.REPEATABLE_READ, readOnly = false)
public CommissionCalculation calculateCommission(String dealId, String planId) {
    Deal deal = dealRepository.findById(dealId);           // Step 1: Read deal value
    CommissionPlan plan = planRepository.findByIdWithTiers(planId);  // Step 2: Read plan tiers
    // Step 3: Find tier based on deal value
    // Step 4: Calculate base commission = deal.value * tier.rate / 100
    // Step 5: Apply bonuses
    // Step 6: Save calculation
}
```

**Inconsistency with `READ_COMMITTED`:**

1. Step 1: Read deal-001 with value = $85,000
2. A concurrent transaction updates deal-001 to $120,000 and commits
3. Step 2: Read the plan with tiers
4. Step 3: The tier lookup uses $85,000 (from Step 1), selecting the "Gold" tier (rate 12%)
5. But if any later step re-reads the deal, it now sees $120,000 — which should have selected the "Platinum" tier (rate 15%)

This creates an inconsistent calculation: the tier was selected based on $85,000, but the commission might be calculated on $120,000.

**`REPEATABLE_READ` prevents this** by guaranteeing that all reads within the transaction see the same data snapshot. Once the deal is read as $85,000 in Step 1, it remains $85,000 throughout the entire transaction, regardless of concurrent changes.

---

**32. Explain `Propagation.REQUIRES_NEW`. Why is it used for `UserService.recordLogin()`? What happens to the outer transaction when a REQUIRES_NEW method is called?**

```java
// UserService.java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void recordLogin(String userId) {
    userRepository.findById(userId).ifPresent(user -> {
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    });
}
```

**`Propagation.REQUIRES_NEW`:** Creates a **new, independent transaction** regardless of any existing transaction.

**What happens to the outer transaction:**

1. Outer transaction is **suspended** (paused)
2. A **new** transaction is opened for `recordLogin()`
3. `recordLogin()` executes and commits/rolls back independently
4. The outer transaction **resumes**

**Why it's used for `recordLogin()`:**

Login recording is an audit operation. If the outer business operation fails and rolls back, the login record should **still be saved**. The login timestamp is a factual event (the user did attempt to log in) that shouldn't be lost due to an unrelated failure.

**Example scenario:**
```
Outer Transaction: Process login + load dashboard data
  → recordLogin() runs in REQUIRES_NEW → commits login timestamp ✓
  → Dashboard data loading fails → outer transaction rolls back ✗
  → Login timestamp is preserved because it committed independently ✓
```

Without `REQUIRES_NEW`, the login timestamp would roll back with the outer transaction.

---

**33. By default, Spring's `@Transactional` rolls back on `RuntimeException` but NOT on checked `Exception`. Why? When would you use `rollbackFor = Exception.class`?**

**Why the default:**

In Java convention:
- **RuntimeExceptions** (unchecked) indicate programming errors or unexpected failures (NullPointerException, IllegalArgumentException, DataAccessException). These are unrecoverable — the transaction should roll back.
- **Checked Exceptions** indicate expected, recoverable conditions (IOException, ParseException). The caller is expected to handle them, and the transaction may still be in a valid state.

Spring follows this convention: unexpected errors → rollback, expected errors → let the caller decide.

**When to use `rollbackFor = Exception.class`:**

When **any** failure should roll back, regardless of exception type. This is demonstrated in `DisputeService.resolveDispute()`:

```java
@Transactional(readOnly = false, rollbackFor = Exception.class)
public Dispute resolveDispute(String disputeId, String resolution, ...) {
    // Updates dispute status, adds comment, and modifies calculation status
}
```

Dispute resolution involves multiple changes (dispute status, comment, calculation status). If **any** step fails — even from a checked exception like `IOException` from a notification service — the entire operation should roll back to prevent inconsistent state (e.g., dispute marked APPROVED but calculation not updated).

---

**34. What is the "self-invocation" problem with `@Transactional`? Why does calling a `@Transactional` method from within the same class bypass the transaction? How would you fix it?**

**The problem:**

Spring's `@Transactional` works through **AOP proxies**. Spring creates a proxy wrapper around your bean. External callers go through the proxy, which manages the transaction. But when a method calls another method **within the same class**, it's a direct `this.method()` call that **bypasses the proxy**.

```java
@Service
public class DealService {

    public void processDeal(String dealId) {
        // This calls the method directly (bypasses proxy)
        // @Transactional on updateDealStatus is IGNORED
        this.updateDealStatus(dealId, DealStatus.WON);
    }

    @Transactional(readOnly = false)
    public void updateDealStatus(String dealId, DealStatus newStatus) {
        // This runs WITHOUT a transaction when called from processDeal()
    }
}
```

**Why it happens:** `this` refers to the actual bean instance, not the proxy. The proxy only intercepts calls from outside the class.

**Fixes:**

1. **Extract to a separate service:** Move the `@Transactional` method to a different bean. Cross-bean calls go through the proxy.
   ```java
   @Service
   public class DealStatusService {
       @Transactional(readOnly = false)
       public void updateDealStatus(String dealId, DealStatus newStatus) { ... }
   }
   ```

2. **Self-injection:** Inject the bean into itself:
   ```java
   @Autowired
   private DealService self;

   public void processDeal(String dealId) {
       self.updateDealStatus(dealId, DealStatus.WON);  // Goes through proxy
   }
   ```

3. **Move the `@Transactional` to the calling method** so the entire operation is transactional.

---

**35. What does `readOnly = true` do in a transaction? List at least three performance benefits it provides.**

```java
@Service
@Transactional(readOnly = true)
public class UserService {
    // All methods default to readOnly = true
}
```

**Three performance benefits:**

1. **Hibernate skips dirty checking:** Normally, Hibernate compares every managed entity's current state against its original state at flush time to detect changes. With `readOnly = true`, Hibernate knows nothing will be modified, so it skips this comparison entirely. For queries returning hundreds of entities, this saves significant CPU time.

2. **Database read optimization:** The JDBC driver and database can optimize for read-only transactions:
   - Some databases route read-only transactions to read replicas
   - Lock acquisition can be skipped or reduced
   - MySQL's InnoDB avoids setting up undo log segments for read-only transactions

3. **No flush at commit:** Read-only transactions skip the flush phase (synchronization of in-memory entities to the database). This eliminates unnecessary SQL UPDATE statements and reduces database round-trips.

**Additional benefit:** Read-only transactions serve as documentation — they signal to developers that the method should not modify data. If a developer accidentally adds a `save()` call, the framework may flag or silently ignore it.

### Code Analysis Questions

---

**36. In `DealService.createDeal()`, a `Deal` and its `DealProducts` are saved in a single transaction. Explain what happens at the database level if saving the third product throws a `DataIntegrityViolationException`. Are the Deal and first two products persisted?**

```java
// DealService.java
@Transactional(readOnly = false)
public Deal createDeal(String title, BigDecimal value, String salesRepId, List<DealProduct> products) {
    User salesRep = userRepository.findById(salesRepId).orElseThrow(...);
    Deal deal = new Deal(title, value, salesRep);
    for (DealProduct product : products) {
        deal.addProduct(product);
    }
    return dealRepository.save(deal);  // CascadeType.ALL saves deal + all products
}
```

**What happens:**

1. Transaction begins
2. `dealRepository.save(deal)` triggers Hibernate to flush:
   - `INSERT INTO deals (...)` → succeeds
   - `INSERT INTO deal_products (...)` → product 1 succeeds
   - `INSERT INTO deal_products (...)` → product 2 succeeds
   - `INSERT INTO deal_products (...)` → product 3 throws `DataIntegrityViolationException`
3. `DataIntegrityViolationException` extends `RuntimeException`
4. Spring's transaction manager catches the exception and issues a **ROLLBACK**
5. The database **undoes ALL changes** — the Deal and first two products are NOT persisted

**Answer:** No. The Deal and first two products are **not** persisted. The entire transaction rolls back due to atomicity — it's all or nothing. After the rollback, the database is in the exact same state as before `createDeal()` was called.

---

**37. `DisputeService.resolveDispute()` uses `rollbackFor = Exception.class`. Why is this more restrictive than the default? Give an example of a checked exception that would be caught by this but not by the default rollback behavior.**

```java
@Transactional(readOnly = false, rollbackFor = Exception.class)
public Dispute resolveDispute(String disputeId, String resolution, String resolvedBy, boolean approved) {
    // 1. Update dispute status
    // 2. Add resolution comment
    // 3. If approved, update calculation status to ADJUSTED
}
```

**Why more restrictive:**

| Exception Type | Default `@Transactional` | With `rollbackFor = Exception.class` |
|---------------|------------------------|------------------------------------|
| RuntimeException (unchecked) | Rolls back | Rolls back |
| Error | Rolls back | Rolls back |
| Exception (checked) | **Commits** | **Rolls back** |
| IOException (checked) | **Commits** | **Rolls back** |

**Example checked exception:**

Suppose `resolveDispute()` calls an external notification service that throws `javax.mail.MessagingException` (a checked exception) when sending an email to the sales rep:

```java
dispute.setStatus(approved ? DisputeStatus.APPROVED : DisputeStatus.REJECTED);
calculationRepository.save(calc);         // Step 1: Update calculation status
notificationService.sendEmail(salesRep);  // Step 2: Throws MessagingException!
```

- **Default behavior:** The `MessagingException` (checked) would NOT trigger a rollback. The dispute would be marked as APPROVED and the calculation updated, but the notification never sent — an inconsistent state.

- **With `rollbackFor = Exception.class`:** The entire transaction rolls back. The dispute status and calculation are unchanged. The operation can be retried safely.

---

**38. Compare `UserService.createUser()` (uses `@CachePut`) with `UserService.updateUser()` (uses `@CacheEvict`). Why are different cache strategies appropriate for create vs. update?**

```java
// CREATE — @CachePut
@CachePut(value = "users", key = "#result.id")
public User createUser(User user) {
    return userRepository.save(user);
}

// UPDATE — @CacheEvict
@CacheEvict(value = "users", key = "#user.id")
public User updateUser(User user) {
    return userRepository.save(user);
}
```

**`@CachePut` for CREATE:**
- The new user didn't exist in the cache before
- `@CachePut` **always executes the method** and stores the result in the cache
- After creation, the user is immediately available in cache for subsequent reads
- This is a "write-through" pattern: write to DB and cache simultaneously

**`@CacheEvict` for UPDATE:**
- The old version of the user may be in the cache
- `@CacheEvict` **removes the stale entry** from the cache
- The next `findById()` call will be a cache miss, loading the fresh data from the database
- This is simpler and safer than `@CachePut` for updates

**Why not `@CachePut` for update?**
You could use `@CachePut` for update too. However, there's a subtlety: if the user is cached under multiple keys (e.g., `#id` and `'username:' + #username`), `@CachePut` only updates one key while the other becomes stale. `@CacheEvict` removes the specific key, and the next read re-populates the cache with fresh data.

**Why not `@Cacheable` for create?**
`@Cacheable` checks the cache first. For a new user, the cache won't have an entry, so it would execute the method and cache the result. But the cache key is `#result.id`, which isn't known until after the method executes. `@CachePut` is designed for this — it always executes the method and caches the result.

---

## Section 5: Caching Strategies with Spring Cache

### Conceptual Questions

---

**39. What is the difference between `@Cacheable` and `@CachePut`? When should you use each?**

| Feature | `@Cacheable` | `@CachePut` |
|---------|-------------|-------------|
| Cache check | Checks cache **before** executing method | Does NOT check cache |
| Method execution | **Skips** method if cache hit | **Always** executes method |
| Cache write | Only on cache miss | Always writes to cache |
| Use case | Read operations | Write operations (create/update) |

**`@Cacheable` — Read-through cache:**
```java
@Cacheable(value = "users", key = "#id")
public Optional<User> findById(String id) {
    log.info("Cache MISS");  // Only logged on first call
    return userRepository.findById(id);
}
// Call 1: Cache miss → executes method → caches result → returns
// Call 2: Cache hit → returns cached result → method NOT executed
```

**`@CachePut` — Write-through cache:**
```java
@CachePut(value = "users", key = "#result.id")
public User createUser(User user) {
    return userRepository.save(user);  // Always executes
}
// Every call: executes method → updates cache → returns
```

**When to use each:**
- `@Cacheable`: For read methods where repeated calls with the same parameters should return cached results.
- `@CachePut`: For write methods where you want to update the cache with the method's return value.

---

**40. Explain the "cache-aside" (lazy loading) pattern. How does Spring's `@Cacheable` implement this pattern?**

**Cache-aside pattern:**

1. Application receives a read request
2. Check the cache for the data
3. **Cache HIT:** Return the cached data immediately
4. **Cache MISS:** Query the database, store the result in the cache, return the result

```
Client → [Cache?] → HIT → Return cached data
                  → MISS → [Database] → Store in cache → Return data
```

**How `@Cacheable` implements it:**

```java
@Cacheable(value = "users", key = "#id")
public Optional<User> findById(String id) {
    return userRepository.findById(id);
}
```

Spring's AOP proxy wraps this method with cache-aside logic:

```java
// Pseudocode of what Spring generates:
public Optional<User> findById(String id) {
    // Step 1: Check cache
    Object cached = cacheManager.getCache("users").get(id);
    if (cached != null) {
        return (Optional<User>) cached;  // Cache HIT
    }

    // Step 2: Cache MISS — execute the actual method
    Optional<User> result = delegate.findById(id);

    // Step 3: Store in cache for next time
    cacheManager.getCache("users").put(id, result);

    return result;
}
```

The developer writes only the database query; Spring adds the caching logic transparently.

---

**41. What is a "cache stampede" (thundering herd problem)? How can `@Cacheable(sync = true)` help prevent it?**

**Cache stampede:** When a popular cache entry expires, many concurrent requests simultaneously discover the cache miss and all hit the database at once.

**Example scenario:**
1. 100 users request `/api/orm/users/usr-001` simultaneously
2. The cache entry for `usr-001` just expired (or was evicted)
3. All 100 requests check the cache → all get cache misses
4. All 100 requests query the database simultaneously: `SELECT * FROM users WHERE id = 'usr-001'`
5. The database is overwhelmed by 100 identical queries

**`@Cacheable(sync = true)`:**

```java
@Cacheable(value = "users", key = "#id", sync = true)
public Optional<User> findById(String id) {
    return userRepository.findById(id);
}
```

With `sync = true`, Spring synchronizes cache access so that only **one thread** executes the method on a cache miss. Other threads **wait** for the first thread to populate the cache, then read from cache.

**Flow with sync:**
1. Thread 1 checks cache → miss → acquires lock → queries database → populates cache → releases lock
2. Threads 2–100 check cache → miss → wait for lock → lock released → cache hit → return cached data

Result: 1 database query instead of 100.

---

**42. Why is caching `CommissionPlan` objects a good idea, but caching paginated `Deal` search results is problematic?**

**CommissionPlan — good cache candidate:**
- **Low cardinality:** Only a few plans exist (3 in the seed data)
- **Rarely changes:** Plans are configured once and used for months
- **Frequently read:** Every commission calculation needs to load a plan
- **Predictable cache keys:** Simple `planId` as key

```java
@Cacheable(value = "commissionPlans", key = "#planId")
public Optional<CommissionPlan> findPlanById(String planId) { ... }
```

**Deal search results — poor cache candidate:**
- **High cardinality of keys:** Search parameters create too many unique combinations:
  - `status=WON, page=0, size=10, sort=value`
  - `status=WON, minValue=25000, page=0, size=10`
  - `status=OPEN, salesRepId=usr-001, page=2, size=20`
  - ... thousands of possible combinations
- **Frequently changing data:** Deals are created, updated, and closed regularly
- **Cache invalidation nightmare:** When a deal status changes, which cached search results contain that deal? You'd need to evict ALL search result caches
- **Low reuse:** The same search with the exact same parameters is unlikely to be repeated frequently

Caching paginated results leads to high memory usage, low hit rates, and complex invalidation logic.

---

**43. What happens if you cache entities that have LAZY-loaded relationships? Why might you get a `LazyInitializationException` when accessing cached objects?**

**The problem:**

When an entity with lazy relationships is loaded inside a transaction, Hibernate keeps the persistence context (session) open. Accessing the lazy relationship works because the session can execute the follow-up query.

But when the entity is cached and later retrieved:
1. The cached entity is a **detached** object (no longer associated with any Hibernate session)
2. Its lazy relationships are **uninitialized proxies** (never loaded)
3. Attempting to access a lazy relationship on a detached entity triggers:

```
org.hibernate.LazyInitializationException:
  could not initialize proxy - no Session
```

**Example:**

```java
@Cacheable(value = "deals", key = "#id")
public Optional<Deal> findById(String id) {
    return dealRepository.findById(id);  // products are LAZY
}

// Later...
Deal deal = dealService.findById("deal-001").get();  // From cache (detached)
deal.getProducts();  // LazyInitializationException!
```

**Solutions:**
1. **Eagerly fetch before caching:** Use `JOIN FETCH` or `@EntityGraph` so the cached entity has all needed data initialized
2. **Cache DTOs instead of entities:** Convert entities to DTOs (with all needed fields) before caching
3. **Use `findByIdWithProducts()` for cache** — a method that uses JOIN FETCH
4. **Don't cache entities with lazy relationships** — cache only simple entities or IDs

---

**44. Compare ConcurrentMapCacheManager, Caffeine, and Redis as cache providers. When would you use each one?**

| Feature | ConcurrentMapCacheManager | Caffeine | Redis |
|---------|--------------------------|----------|-------|
| Storage | JVM heap (ConcurrentHashMap) | JVM heap | External server |
| Expiration | No | Yes (TTL, TTI) | Yes (TTL) |
| Size limit | No (grows until OOM) | Yes (configurable max) | Yes (configurable max memory) |
| Eviction | None | TinyLFU (near-optimal) | LRU, LFU, etc. |
| Multi-instance | No (per-JVM) | No (per-JVM) | Yes (shared across instances) |
| Persistence | No | No | Yes (optional) |
| Performance | Fastest | Very fast | Network latency |
| Statistics | No | Yes | Yes |

**When to use each:**

**ConcurrentMapCacheManager** (used in this project):
- Educational projects and prototypes
- Single-instance applications with small datasets
- When you don't need expiration or size limits

**Caffeine:**
- Production single-instance applications
- When you need TTL, size limits, and eviction policies
- High-throughput applications where cache performance is critical

**Redis:**
- Multi-instance deployments (microservices, load-balanced apps)
- When cached data must be shared across application instances
- When cache must survive application restarts
- When you need cache pub/sub for invalidation across instances

```java
// CacheConfig.java — this project uses ConcurrentMapCacheManager
@Bean
public CacheManager cacheManager() {
    ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager(
            "commissionPlans", "deals", "users", "calculations");
    cacheManager.setAllowNullValues(false);
    return cacheManager;
}
```

---

**45. What is cache invalidation? Why is it considered one of the "two hard problems in computer science"? Give an example of stale cache data in the Commission Calculator.**

**Cache invalidation** is the process of removing or updating cached data when the underlying data changes.

**The famous quote:** "There are only two hard things in Computer Science: cache invalidation and naming things." — Phil Karlton

**Why it's hard:**

1. **Knowing WHEN to invalidate:** How do you know that data changed? What if it was changed directly in the database, bypassing the application?

2. **Knowing WHAT to invalidate:** If a user changes their name, which cached objects contain that user's name? User cache, deal cache (embedded rep name), calculation summaries?

3. **Timing:** Between the database update and cache invalidation, there's a window where the cache has stale data.

4. **Distributed invalidation:** In multi-instance deployments, all instances must invalidate their caches. If instance A updates a user but instance B still has the old cached version, inconsistency occurs.

**Example of stale cache in the Commission Calculator:**

This exact bug was found during integration testing:

```java
// UserService.java — BEFORE the fix
@Transactional(readOnly = false)
// Missing @CacheEvict!
public User deactivateUser(String userId) {
    User user = userRepository.findById(userId).orElseThrow(...);
    user.setActive(false);
    return userRepository.save(user);
}
```

1. `findById("usr-003")` loads the user and caches it with `active = true`
2. `deactivateUser("usr-003")` sets `active = false` in the database
3. But the cache still has `active = true` (no `@CacheEvict`)
4. The next `findById("usr-003")` returns the **stale cached version** with `active = true`
5. The user appears active even though the database says they're deactivated

**Fix:** Add `@CacheEvict(value = "users", key = "#userId")` to `deactivateUser()`.

### Code Analysis Questions

---

**46. Look at `UserService.findById()`. Explain the SpEL expression `@Cacheable(value = "users", key = "#id")`. What is the cache key for `findById("usr-001")`? How does Spring know to check the cache before executing the method?**

```java
@Cacheable(value = "users", key = "#id")
public Optional<User> findById(String id) {
    log.info("Cache MISS - Loading user from database: {}", id);
    return userRepository.findById(id);
}
```

**SpEL expression breakdown:**

| Part | Meaning |
|------|---------|
| `value = "users"` | Cache name — identifies which cache region to use (defined in `CacheConfig`) |
| `key = "#id"` | SpEL expression — `#id` refers to the method parameter named `id` |

**Cache key for `findById("usr-001")`:** The key is the String `"usr-001"`.

The cache stores: `"users"` cache → key `"usr-001"` → value `Optional<User>`.

**How Spring checks the cache before executing:**

Spring's `@EnableCaching` annotation activates cache AOP proxies. When the application context is created:

1. Spring detects `@Cacheable` on `findById()` and creates a proxy around `UserService`
2. When external code calls `userService.findById("usr-001")`, the call goes through the proxy
3. The proxy evaluates the SpEL expression `#id` to `"usr-001"`
4. The proxy calls `cacheManager.getCache("users").get("usr-001")`
5. If found (cache hit): return the cached value, **skip the method entirely** (the log statement never executes)
6. If not found (cache miss): execute the actual method, cache the result, return it

---

**47. In `CommissionService.calculateCommission()`, the `@CacheEvict(value = "calculations", allEntries = true)` clears the entire calculations cache. Why not use a specific key? What is the trade-off of `allEntries = true`?**

```java
@CacheEvict(value = "calculations", allEntries = true)
public CommissionCalculation calculateCommission(String dealId, String planId) {
    // Creates a NEW calculation — the new ID is unknown until after save
}
```

**Why not a specific key:**

The method creates a **new** `CommissionCalculation` with a UUID generated at save time. Before the method executes, we don't know what the new ID will be — so we can't specify `key = "#result.id"` for eviction.

Additionally, creating a new calculation may invalidate cached aggregate queries (e.g., "get all calculations for sales rep X") that are stored in the same cache. A specific key eviction would only remove one entry, leaving stale aggregate results.

**Trade-offs of `allEntries = true`:**

| Pros | Cons |
|------|------|
| Guaranteed no stale data | Clears all cached calculations (not just the affected one) |
| Simple to implement | "Thundering herd" — the next request for ANY calculation is a cache miss |
| No need to track all affected keys | Temporary performance degradation as the cache repopulates |

This is sometimes called the "nuclear option" for caching. It's appropriate here because commission calculations are infrequent (triggered manually), so the temporary cache miss spike is acceptable.

---

**48. `UserService` uses `@CacheEvict` on `updateUser()` but `@CachePut` on `createUser()`. What would happen if you used `@Cacheable` instead of `@CachePut` on `createUser()`? Would the new user be saved to the database?**

```java
// Hypothetical — using @Cacheable on a create method (BAD)
@Cacheable(value = "users", key = "#result.id")
public User createUser(User user) {
    return userRepository.save(user);
}
```

**Problem 1: SpEL evaluation timing**

`@Cacheable` evaluates the key expression **before** executing the method. The key `#result.id` refers to the method's return value, which doesn't exist yet. This would throw a SpEL evaluation error because `#result` is only available in the `unless` condition of `@Cacheable`, not in the `key` expression.

**Problem 2: If using a different key (e.g., `#user.username`)**

```java
@Cacheable(value = "users", key = "#user.username")
public User createUser(User user) {
    return userRepository.save(user);
}
```

If someone previously attempted to create a user with the same username and the result was cached:
- `@Cacheable` would find the cached result and **skip the method entirely**
- The new user would **NOT be saved** to the database
- The method would return the previously cached result

**This is why `@CachePut` exists:** it always executes the method (ensuring the database write happens) and then updates the cache with the result. For write operations, always use `@CachePut` or `@CacheEvict`, never `@Cacheable`.

---

## Integration Questions

---

**49. Trace the complete flow of calculating a commission: Starting from the REST API call `POST /api/orm/commissions/calculate?dealId=deal-001&planId=plan-001`, describe every layer involved (controller -> service -> repository -> database) and identify where transactions, caching, and entity relationships play a role.**

**Layer 1: Controller** (`CommissionController.java`)

```java
@PostMapping("/calculate")
public ResponseEntity<CommissionCalculation> calculateCommission(
        @RequestParam String dealId, @RequestParam String planId) {
    CommissionCalculation calc = commissionService.calculateCommission(dealId, planId);
    return ResponseEntity.status(HttpStatus.CREATED).body(calc);
}
```

Spring parses the query parameters `dealId=deal-001` and `planId=plan-001`. No transaction yet.

**Layer 2: Service** (`CommissionService.calculateCommission()`)

```
@Transactional(isolation = Isolation.REPEATABLE_READ, readOnly = false)
@CacheEvict(value = "calculations", allEntries = true)
```

**Transaction begins** with REPEATABLE_READ isolation. All subsequent database operations run within this transaction.

**Step 2a:** Load the deal
```java
Deal deal = dealRepository.findById(dealId).orElseThrow(...);
```
- **Cache:** `DealService.findById()` would check the "deals" cache, but here the repository is called directly — no cache check.
- **Repository:** `dealRepository.findById("deal-001")` → Spring Data generates `SELECT * FROM deals WHERE id = 'deal-001'`
- **Entity relationships:** `deal.salesRep` is a LAZY proxy (not loaded yet)

**Step 2b:** Validate deal status
```java
if (deal.getStatus() != DealStatus.WON) throw new IllegalStateException(...);
```

**Step 2c:** Load the plan with tiers
```java
CommissionPlan plan = planRepository.findByIdWithTiers(planId).orElseThrow(...);
```
- **Repository:** Custom JPQL with `JOIN FETCH p.tiers` → single SQL query loads plan + tiers
- **Entity relationships:** `LEFT JOIN FETCH p.tiers` eagerly loads the CommissionTier children

**Step 2d:** Find applicable tier
```java
for (CommissionTier tier : plan.getTiers()) {
    if (tier.containsValue(dealValue)) {
        commissionRate = tier.getRate();
        break;
    }
}
```
- Deal value $85,000 falls in tier "Gold" ($75K–$150K) → rate = 12%
- No database query — tiers were already loaded by JOIN FETCH

**Step 2e:** Calculate base commission
```java
BigDecimal baseCommission = dealValue.multiply(commissionRate).divide(new BigDecimal("100"), 2, HALF_UP);
// $85,000 * 12 / 100 = $10,200.00
```

**Step 2f:** Apply bonuses
```java
CommissionPlan planWithBonuses = planRepository.findByIdWithBonuses(planId).orElse(plan);
```
- Another JOIN FETCH query loads bonus rules
- Active bonuses are applied to the base commission

**Step 2g:** Save the calculation
```java
CommissionCalculation saved = calculationRepository.save(calculation);
```
- **Repository:** Hibernate generates INSERT INTO commission_calculations (...)
- **Cascade:** If bonus calculations were added, CascadeType.ALL triggers INSERT INTO bonus_calculations (...)
- **Transaction commits** — all database changes become permanent
- **Cache:** `@CacheEvict(allEntries = true)` clears the entire "calculations" cache

**Layer 3: Controller response**
Spring serializes the `CommissionCalculation` to JSON and returns HTTP 201 Created.

---

**50. Design a new feature: "Commission Recalculation". When a dispute is approved, the commission should be recalculated using the corrected data.**

**Entities and repositories involved:**
- `Dispute` + `DisputeRepository` — to retrieve the approved dispute
- `CommissionCalculation` + `CommissionCalculationRepository` — to update the old calculation and create a new one
- `Deal` + `DealRepository` — to get the deal (with corrected data)
- `CommissionPlan` + `CommissionPlanRepository` — to get the plan tiers and bonuses
- `User` + `UserRepository` — to get the sales rep

**Transaction isolation level:** `Isolation.REPEATABLE_READ`

Same reasoning as `calculateCommission()`: we read the deal value, plan tiers, and bonuses across multiple queries. REPEATABLE_READ ensures these reads are consistent throughout the recalculation, preventing non-repeatable reads if another transaction modifies the deal or plan concurrently.

**Cache invalidation:**

```java
@Caching(evict = {
    @CacheEvict(value = "calculations", allEntries = true),  // Invalidate all cached calculations
    @CacheEvict(value = "deals", key = "#dealId")            // Invalidate the deal (if its value was corrected)
})
public CommissionCalculation recalculate(String disputeId) { ... }
```

Both the "calculations" and potentially the "deals" cache need invalidation, since the recalculation may use corrected deal data.

**What happens if recalculation fails after dispute status is updated?**

Use `rollbackFor = Exception.class` so the entire operation is atomic:

```java
@Transactional(isolation = Isolation.REPEATABLE_READ, readOnly = false, rollbackFor = Exception.class)
```

If the recalculation fails:
- The dispute status update rolls back (remains APPROVED, not marked as recalculated)
- The old calculation is preserved unchanged
- No partial recalculation is saved
- The operation can be safely retried

Alternatively, use `Propagation.REQUIRES_NEW` for the recalculation step so the dispute resolution commits independently. Then if recalculation fails, the dispute is still approved and recalculation can be retried later.

---

**51. You notice that the application is slow when loading the "Sales Dashboard" which shows each sales rep with their deals, calculations, and total commission. The current implementation makes separate queries for each sales rep. Propose three different optimization strategies using concepts from this module.**

**Strategy 1: JOIN FETCH with a single JPQL query**

Replace N+1 queries with a single query that loads all needed data:

```java
@Query("SELECT DISTINCT u FROM User u " +
       "LEFT JOIN FETCH u.deals d " +
       "LEFT JOIN FETCH d.calculations c " +
       "WHERE u.active = true AND d.status = 'WON'")
List<User> findAllSalesRepsWithDealsAndCalculations();
```

**Pros:** Single SQL query, no N+1 problem.
**Cons:** May cause Cartesian product with multiple collections. Consider splitting into two queries (fetch deals first, then calculations in a second query).

**Strategy 2: Spring Cache with a dedicated cache region**

Cache the dashboard data since it changes infrequently:

```java
@Cacheable(value = "dashboardData", key = "'salesDashboard'")
public List<SalesDashboardDTO> getSalesDashboardData() {
    // Execute the complex query once, cache the result
    return userRepository.findAllSalesRepsWithDealsAndCalculations()
            .stream().map(this::toDTO).collect(Collectors.toList());
}
```

Invalidate the cache when deals or calculations change:
```java
@CacheEvict(value = "dashboardData", allEntries = true)
public Deal updateDealStatus(...) { ... }
```

**Pros:** Subsequent dashboard loads are instant (from cache).
**Cons:** Cache invalidation complexity; data may be slightly stale.

**Strategy 3: Native SQL aggregate query**

Use the database's aggregation capabilities instead of loading entities:

```java
@Query(value = "SELECT u.id, u.first_name, u.last_name, " +
               "COUNT(d.id) AS deal_count, " +
               "COALESCE(SUM(d.deal_value), 0) AS total_value, " +
               "COALESCE(SUM(cc.net_commission), 0) AS total_commission " +
               "FROM users u " +
               "LEFT JOIN deals d ON d.sales_rep_id = u.id AND d.status = 'WON' " +
               "LEFT JOIN commission_calculations cc ON cc.sales_rep_id = u.id " +
               "  AND cc.status IN ('APPROVED', 'PAID') " +
               "WHERE u.active = TRUE " +
               "GROUP BY u.id, u.first_name, u.last_name",
       nativeQuery = true)
List<Object[]> getDashboardSummary();
```

Or use the existing `v_commission_summary` view from V3 migration.

**Pros:** Single query, minimal data transferred (only aggregates, not full entities), leverages database indexes.
**Cons:** Returns raw arrays (not type-safe), database-specific SQL.

---

**52. A production deployment uses Flyway for migrations and Redis for caching across 3 application instances. A new developer modifies V2__seed_sample_data.sql to add more test data and deploys to production. What happens? How should this be handled instead?**

**What happens:**

1. The application starts and Flyway checks `flyway_schema_history`
2. V2 was already applied with checksum `ABC123`
3. The modified V2 file now has checksum `XYZ789`
4. Flyway detects the **checksum mismatch** and throws:

```
FlywayValidateException: Migration checksum mismatch for migration version 2
-> Applied to database : ABC123
-> Resolved locally    : XYZ789
```

5. **The application fails to start** on all 3 instances
6. Production is completely down

**Why this is especially dangerous in the described setup:**
- All 3 instances fail simultaneously (they all run the same Flyway validation)
- Redis cache contains data based on the old schema — even if the app recovered, cached data could be inconsistent
- If the developer had somehow forced Flyway to skip validation (repair), the new INSERT statements might fail due to duplicate primary keys or other constraints

**How it should be handled:**

1. **Never modify applied migrations.** Create a new migration:
   ```
   V4__add_additional_test_data.sql
   ```

2. **Don't put test data in production migrations.** Use:
   - **Spring profiles:** `@Profile("dev")` on a `DataLoader` component
   - **Repeatable migrations:** `R__seed_test_data.sql` (only in test environments)
   - **Flyway callbacks:** `afterMigrate.sql` that only runs in dev/test

3. **Use environment-specific Flyway locations:**
   ```properties
   # application-dev.properties
   spring.flyway.locations=classpath:db/migration,classpath:db/testdata

   # application-prod.properties
   spring.flyway.locations=classpath:db/migration
   ```

4. **Redis cache invalidation:** After deploying any migration that changes data, run a cache flush (`FLUSHDB` or targeted invalidation) across all Redis instances to prevent stale cached data.

5. **Code review process:** PRs that modify existing migration files should be automatically rejected by CI checks.
