package com.chapman.edu.commissions.orm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ============================================================
 * JPA ENTITY: User
 * ============================================================
 *
 * ENTITY RELATIONSHIPS DEMONSTRATED:
 * - @ElementCollection: Maps a collection of basic types or embeddables
 *   to a separate table. Used here for the Set<UserRole> roles field.
 *   Unlike @OneToMany, @ElementCollection is for value types (enums, strings)
 *   that don't have their own identity/primary key.
 *
 * - @OneToMany (self-referential): A user (manager) can have many direct reports.
 *   This demonstrates a self-referencing relationship where the foreign key
 *   points back to the same table.
 *
 * - @ManyToOne (self-referential): Each user can have one manager.
 *   Combined with @OneToMany above, this forms a bidirectional self-referential
 *   relationship modeling an organizational hierarchy.
 *
 * MAPPING STRATEGIES:
 * - @CollectionTable: Specifies the table for @ElementCollection
 * - @JoinColumn: Defines the foreign key column
 * - @Enumerated(EnumType.STRING): Stores enum values as strings (not ordinals)
 *   BEST PRACTICE: Always use STRING, not ORDINAL. Ordinal breaks if you
 *   reorder enum constants.
 *
 * DATABASE DESIGN:
 * - 'users' table: Main user data
 * - 'user_roles' table: Join table for the roles collection
 * - Self-referential FK: manager_id -> users.id
 */
@Entity
@Table(name = "users", indexes = {
        // INDEX DESIGN: Create indexes on columns frequently used in WHERE clauses
        // or JOIN conditions to speed up queries.
        @Index(name = "idx_user_username", columnList = "username", unique = true),
        @Index(name = "idx_user_email", columnList = "email", unique = true),
        @Index(name = "idx_user_department", columnList = "department"),
        @Index(name = "idx_user_territory", columnList = "territory"),
        @Index(name = "idx_user_active", columnList = "active")
})
@Data
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "password_hash")
    private String passwordHash;

    /**
     * @ElementCollection: Used for collections of basic/embeddable types.
     * Creates a separate 'user_roles' table with columns:
     *   - user_id (FK to users.id)
     *   - role (the enum value as a STRING)
     *
     * FetchType.EAGER: Roles are loaded immediately with the user.
     * This is appropriate here because roles are almost always needed
     * when a user is loaded (for authorization checks).
     *
     * CONTRAST WITH @OneToMany:
     * - @ElementCollection: For value types without their own identity
     * - @OneToMany: For entity types with their own @Id and lifecycle
     */
    @ElementCollection(targetClass = UserRole.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Set<UserRole> roles = new HashSet<>();

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "created_date", nullable = false)
    private LocalDate createdDate = LocalDate.now();

    @Column(name = "created_by")
    private String createdBy;

    /**
     * SELF-REFERENTIAL @ManyToOne RELATIONSHIP:
     * Each user may have one manager (who is also a User).
     *
     * FetchType.LAZY: The manager is loaded only when accessed.
     * This prevents N+1 query problems when loading lists of users
     * where you don't need manager information.
     *
     * @JoinColumn(name = "manager_id"): The FK column in the 'users' table
     * that references the manager's id.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    @JsonIgnore
    private User manager;

    /**
     * SELF-REFERENTIAL @OneToMany RELATIONSHIP:
     * A manager can have many direct reports.
     *
     * mappedBy = "manager": The 'manager' field in User owns the relationship.
     * The 'directReports' side is the inverse (non-owning) side.
     *
     * OWNING vs. INVERSE SIDE:
     * - Owning side (@ManyToOne): Has the @JoinColumn, controls FK in DB
     * - Inverse side (@OneToMany with mappedBy): Read-only mirror of the relationship
     * - Changes to the owning side are persisted; changes to the inverse side alone are NOT
     */
    @OneToMany(mappedBy = "manager", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<User> directReports = new ArrayList<>();

    @Column
    private String department;

    @Column
    private String territory;

    /**
     * @OneToMany: A user (sales rep) can have many deals.
     * mappedBy = "salesRep": The Deal entity owns the relationship.
     *
     * CascadeType: Not used here because deals have an independent lifecycle.
     * Deleting a user should NOT automatically delete their deals.
     */
    @OneToMany(mappedBy = "salesRep", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Deal> deals = new ArrayList<>();

    /**
     * INVERSE SIDE of the @OneToOne relationship with UserProfile.
     *
     * mappedBy = "user": The UserProfile entity owns the relationship
     * (it has the @JoinColumn with the FK column 'user_id').
     * This side is a read-only mirror — setting user.setProfile(profile)
     * alone does NOT write the FK. You must also set profile.setUser(user).
     *
     * @OneToOne means exactly one UserProfile per User (and vice versa).
     * Unlike @OneToMany where many children reference one parent, here
     * the FK column in 'user_profiles' has a UNIQUE constraint so only
     * one profile row can reference a given user.
     *
     * CascadeType.ALL + orphanRemoval: The profile's lifecycle is tied to the user.
     * - When the user is persisted, the profile is auto-persisted (PERSIST)
     * - When the user is deleted, the profile is auto-deleted (REMOVE)
     * - If user.setProfile(null), the old profile is deleted (orphanRemoval)
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private UserProfile profile;

    /**
     * INVERSE SIDE of the @ManyToMany relationship with Company.
     *
     * mappedBy = "users": The Company entity owns the relationship
     * (it has the @JoinTable defining the 'user_companies' join table).
     * This side is a read-only mirror — adding to user.getCompanies()
     * alone does NOT insert rows in the join table. You must add via
     * company.addUser(user) to persist the association.
     *
     * @ManyToMany means a User can belong to many Companies, and a
     * Company can have many Users. This requires a JOIN TABLE because
     * neither entity's table can hold a single FK column to represent
     * the N:M cardinality — a User row can't have multiple company_id
     * values in one column, and vice versa.
     *
     * Set<Company> is used instead of List<Company> to prevent Hibernate's
     * MultipleBagFetchException and to avoid duplicate associations.
     */
    @ManyToMany(mappedBy = "users", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Company> companies = new HashSet<>();

    public User(String username, String email, String firstName, String lastName) {
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.roles = new HashSet<>();
        this.active = true;
        this.createdDate = LocalDate.now();
    }

    public void addRole(UserRole role) {
        this.roles.add(role);
    }

    public boolean hasRole(UserRole role) {
        return this.roles.contains(role);
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isSalesRep() {
        return hasRole(UserRole.SALES_REP);
    }

    public boolean isSalesManager() {
        return hasRole(UserRole.SALES_MANAGER);
    }

    public boolean isFinanceAdmin() {
        return hasRole(UserRole.FINANCE_ADMIN);
    }

    public boolean isSystemAdmin() {
        return hasRole(UserRole.SYSTEM_ADMIN);
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", roles=" + roles +
                ", active=" + active +
                '}';
    }

    // Override equals/hashCode to avoid infinite recursion with self-referential relationships
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        // Use a constant hashCode for JPA entities to work correctly with Sets
        // before the entity is persisted (when id is still null)
        return getClass().hashCode();
    }
}
