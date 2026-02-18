package com.chapman.edu.commissions.orm.repository;

import com.chapman.edu.commissions.orm.entity.User;
import com.chapman.edu.commissions.orm.entity.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ============================================================
 * SPRING DATA JPA REPOSITORY: UserRepository
 * ============================================================
 *
 * WHAT IS A SPRING DATA JPA REPOSITORY?
 * A repository is an interface that provides data access methods
 * without requiring you to write implementation code. Spring Data JPA
 * generates the implementation at runtime.
 *
 * REPOSITORY HIERARCHY:
 *   Repository (marker interface)
 *     -> CrudRepository (basic CRUD: save, findById, delete, findAll)
 *       -> ListCrudRepository (returns List instead of Iterable)
 *         -> JpaRepository (adds JPA-specific: flush, saveAndFlush, batch ops)
 *
 * JpaRepository<User, String> means:
 *   - Entity type: User
 *   - Primary key type: String (UUID)
 *
 * INHERITED METHODS (from JpaRepository):
 *   save(entity), saveAll(entities), findById(id), existsById(id),
 *   findAll(), findAll(Pageable), findAll(Sort), count(), deleteById(id),
 *   delete(entity), deleteAll(), flush(), saveAndFlush(entity)
 *
 * QUERY CREATION STRATEGIES (demonstrated below):
 * 1. Derived Query Methods - Spring generates SQL from method name
 * 2. @Query with JPQL - Write JPA Query Language (object-oriented SQL)
 * 3. @Query with native SQL - Write database-specific SQL
 * 4. Pagination and Sorting - Built-in support via Pageable/Sort
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    // ============================================================
    // STRATEGY 1: DERIVED QUERY METHODS
    // ============================================================
    // Spring Data parses the method name to generate a query.
    // Keywords: findBy, countBy, existsBy, deleteBy
    // Property expressions: And, Or, Between, LessThan, GreaterThan,
    //   Like, Containing, In, True, False, OrderBy, Not, IsNull, etc.
    //
    // Method: findByUsername(String username)
    // Generated SQL: SELECT * FROM users WHERE username = ?
    // ============================================================

    /**
     * Find a user by their unique username.
     * Returns Optional to handle the case when no user is found.
     *
     * Generated: SELECT u FROM User u WHERE u.username = :username
     */
    Optional<User> findByUsername(String username);

    /**
     * Find a user by their email address.
     *
     * Generated: SELECT u FROM User u WHERE u.email = :email
     */
    Optional<User> findByEmail(String email);

    /**
     * Find all users in a specific department.
     *
     * Generated: SELECT u FROM User u WHERE u.department = :department
     */
    List<User> findByDepartment(String department);

    /**
     * Find all users in a specific territory who are active.
     * Multiple conditions are combined with AND.
     *
     * Generated: SELECT u FROM User u WHERE u.territory = :territory AND u.active = :active
     */
    List<User> findByTerritoryAndActive(String territory, boolean active);

    /**
     * Check if a username already exists.
     *
     * Generated: SELECT COUNT(u) > 0 FROM User u WHERE u.username = :username
     */
    boolean existsByUsername(String username);

    /**
     * Check if an email already exists.
     *
     * Generated: SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email
     */
    boolean existsByEmail(String email);

    /**
     * Find all active users, sorted and paginated.
     * The Pageable parameter enables pagination and sorting.
     *
     * Usage: userRepository.findByActive(true, PageRequest.of(0, 10, Sort.by("lastName")))
     *
     * Generated: SELECT u FROM User u WHERE u.active = :active ORDER BY ... LIMIT ... OFFSET ...
     */
    Page<User> findByActive(boolean active, Pageable pageable);

    /**
     * Find users whose last name starts with a prefix (case-insensitive).
     *
     * Generated: SELECT u FROM User u WHERE LOWER(u.lastName) LIKE LOWER(:prefix%)
     */
    List<User> findByLastNameStartingWithIgnoreCase(String prefix);

    // ============================================================
    // STRATEGY 2: JPQL QUERIES (@Query annotation)
    // ============================================================
    // JPQL (Java Persistence Query Language) is an object-oriented
    // query language that operates on entities and their properties,
    // NOT on database tables and columns.
    //
    // Key differences from SQL:
    // - Use entity class names (User), not table names (users)
    // - Use property names (firstName), not column names (first_name)
    // - JOIN syntax works on relationships, not foreign keys
    // ============================================================

    /**
     * Find users who have a specific role.
     * Uses JOIN on the @ElementCollection 'roles' set.
     *
     * JPQL: Joins the User entity with its roles collection.
     * The :role parameter is bound to the method parameter.
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role AND u.active = true")
    List<User> findActiveUsersByRole(@Param("role") UserRole role);

    /**
     * Find all sales reps managed by a specific manager.
     * Demonstrates navigating the self-referential relationship in JPQL.
     *
     * u.manager.id navigates the @ManyToOne relationship.
     */
    @Query("SELECT u FROM User u WHERE u.manager.id = :managerId AND u.active = true")
    List<User> findDirectReportsByManagerId(@Param("managerId") String managerId);

    /**
     * Search users by name (first or last) with a search term.
     * Uses LOWER() for case-insensitive search and CONCAT for LIKE pattern.
     *
     * This demonstrates JPQL string functions:
     * LOWER(), UPPER(), CONCAT(), SUBSTRING(), TRIM(), LENGTH()
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> searchByName(@Param("searchTerm") String searchTerm);

    /**
     * Count users by department.
     * Demonstrates aggregate functions in JPQL (COUNT, SUM, AVG, MIN, MAX).
     */
    @Query("SELECT u.department, COUNT(u) FROM User u WHERE u.active = true GROUP BY u.department")
    List<Object[]> countUsersByDepartment();

    // ============================================================
    // STRATEGY 3: NATIVE SQL QUERIES
    // ============================================================
    // Use native SQL when:
    // - You need database-specific features (window functions, CTEs)
    // - JPQL can't express the query efficiently
    // - You need to call stored procedures
    //
    // CAUTION: Native queries are NOT portable across databases.
    // H2 syntax may differ from MySQL, PostgreSQL, Oracle, etc.
    // ============================================================

    /**
     * Find top performers - users with the most won deals.
     * Uses native SQL with a subquery and window function concept.
     *
     * nativeQuery = true tells Spring to use raw SQL instead of JPQL.
     */
    @Query(value = "SELECT u.* FROM users u " +
                   "INNER JOIN deals d ON d.sales_rep_id = u.id " +
                   "WHERE d.status = 'WON' AND u.active = TRUE " +
                   "GROUP BY u.id, u.username, u.email, u.first_name, u.last_name, " +
                   "u.password_hash, u.active, u.last_login, u.created_date, u.created_by, " +
                   "u.manager_id, u.department, u.territory " +
                   "ORDER BY COUNT(d.id) DESC " +
                   "LIMIT :limit",
           nativeQuery = true)
    List<User> findTopPerformers(@Param("limit") int limit);
}
