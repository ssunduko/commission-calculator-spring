package com.chapman.edu.commissions.orm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * ============================================================
 * JPA ENTITY: Company
 * ============================================================
 *
 * CONCEPT: @ManyToMany RELATIONSHIP (Many-to-Many)
 * ==================================================
 * A Many-to-Many relationship means each row in Table A can be linked to
 * multiple rows in Table B, AND each row in Table B can be linked to
 * multiple rows in Table A. Neither side is limited to one.
 *
 * REAL-WORLD EXAMPLES:
 * - A User can work with many Companies (multiple clients or accounts)
 * - A Company can have many Users (multiple sales reps covering it)
 * - Students <-> Courses: A student enrolls in many courses, a course has many students
 * - Authors <-> Books: An author writes many books, a book can have many authors
 *
 * HOW MANY-TO-MANY WORKS IN JPA:
 * 1. A JOIN TABLE is required because neither entity can hold the FK alone.
 *    If User had a company_id column, it could only reference ONE company.
 *    If Company had a user_id column, it could only reference ONE user.
 *    The join table 'user_companies' solves this with TWO FK columns.
 *
 * 2. The @JoinTable annotation defines:
 *    - name: The join table name ("user_companies")
 *    - joinColumns: FK pointing to THIS entity (Company -> company_id)
 *    - inverseJoinColumns: FK pointing to the OTHER entity (User -> user_id)
 *
 * 3. OWNING vs. INVERSE SIDE:
 *    - Owning side (Company.users): Has the @JoinTable definition.
 *      Adding/removing users from company.getUsers() writes to the join table.
 *    - Inverse side (User.companies): Has mappedBy = "users".
 *      This is a read-only mirror. Changes here alone are NOT persisted.
 *    - RULE: Always modify the OWNING side to persist relationship changes.
 *      For convenience, use helper methods that synchronize both sides.
 *
 * CONTRAST WITH OTHER RELATIONSHIPS:
 * +--------------+--------+------+-----------+----------------------------+
 * | Relationship | Side A | Join | Side B    | FK Location                |
 * +--------------+--------+------+-----------+----------------------------+
 * | @OneToOne    |   1    |  --  |    1      | FK in one table (unique)   |
 * | @ManyToOne   |   N    |  --  |    1      | FK in the "many" table     |
 * | @OneToMany   |   1    |  --  |    N      | FK in the "many" table     |
 * | @ManyToMany  |   N    |  JT  |    M      | Separate JOIN TABLE needed |
 * +--------------+--------+------+-----------+----------------------------+
 *
 * WHY A JOIN TABLE IS NEEDED:
 * Without a join table, you'd need to duplicate rows. Consider:
 *   User "Alice" works with Company "Acme" and Company "Beta"
 *   User "Bob" also works with Company "Acme"
 *
 *   Join table 'user_companies':
 *     | user_id  | company_id |
 *     |----------|------------|
 *     | alice-id | acme-id    |  <- Alice works with Acme
 *     | alice-id | beta-id    |  <- Alice works with Beta
 *     | bob-id   | acme-id    |  <- Bob works with Acme
 *
 *   This cleanly represents the N:M cardinality without data duplication.
 *
 * FETCH STRATEGY:
 * - FetchType.LAZY (default for @ManyToMany): The related Users are NOT
 *   loaded when a Company is fetched. They are loaded on first access.
 * - This is critical for performance: a Company might be associated with
 *   hundreds of users, and we don't want to load them all every time.
 *
 * CASCADE:
 * - No CascadeType: Companies and Users have independent lifecycles.
 *   Deleting a company should NOT delete the users (they exist independently).
 *   Deleting a user should NOT delete the companies they're associated with.
 *   Only the JOIN TABLE rows linking them should be removed.
 *
 * BEST PRACTICE — Use Set, not List:
 * - @ManyToMany should use Set<> to avoid Hibernate's "MultipleBagFetchException"
 *   when eagerly fetching multiple collections. Sets also prevent duplicates.
 *
 * DATABASE DESIGN:
 * - 'companies' table: Company data
 * - 'user_companies' join table: Two FK columns forming a composite key
 */
@Entity
@Table(name = "companies", indexes = {
        @Index(name = "idx_company_name", columnList = "name"),
        @Index(name = "idx_company_industry", columnList = "industry")
})
@Data
@NoArgsConstructor
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column
    private String industry;

    @Column
    private String website;

    @Column(length = 2000)
    private String description;

    @Column(name = "created_date", nullable = false)
    private LocalDate createdDate = LocalDate.now();

    /**
     * OWNING SIDE of the @ManyToMany relationship.
     *
     * @JoinTable defines the join table that bridges Company and User:
     *   - name = "user_companies": The join table name
     *   - joinColumns: FK from the join table to THIS entity (Company)
     *   - inverseJoinColumns: FK from the join table to the OTHER entity (User)
     *
     * This is the owning side, so adding/removing Users from this Set
     * will INSERT/DELETE rows in the 'user_companies' join table.
     *
     * IMPORTANT: Both sides must be kept in sync for in-memory consistency.
     * Use the helper methods addUser()/removeUser() below to ensure both
     * company.users and user.companies reflect the same state.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_companies",
            joinColumns = @JoinColumn(name = "company_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnore
    private Set<User> users = new HashSet<>();

    public Company(String name, String industry) {
        this.name = name;
        this.industry = industry;
        this.createdDate = LocalDate.now();
    }

    /**
     * BIDIRECTIONAL SYNC HELPER — Add a user to this company.
     *
     * In a @ManyToMany relationship, you must keep BOTH sides in sync:
     *   company.getUsers().add(user)    — owning side (persisted to DB)
     *   user.getCompanies().add(company) — inverse side (in-memory consistency)
     *
     * Without syncing both sides, the in-memory object graph would be
     * inconsistent: the company would know about the user, but the user
     * wouldn't know about the company (until the next DB read).
     */
    public void addUser(User user) {
        this.users.add(user);
        user.getCompanies().add(this);
    }

    /**
     * BIDIRECTIONAL SYNC HELPER — Remove a user from this company.
     */
    public void removeUser(User user) {
        this.users.remove(user);
        user.getCompanies().remove(this);
    }

    @Override
    public String toString() {
        return "Company{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", industry='" + industry + '\'' +
                ", website='" + website + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Company company = (Company) o;
        return id != null && id.equals(company.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
