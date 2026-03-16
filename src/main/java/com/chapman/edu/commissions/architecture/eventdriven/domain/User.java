package com.chapman.edu.commissions.architecture.eventdriven.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a user in the system.
 * Users can be sales representatives, managers, finance administrators, or system administrators.
 */
@Entity
@Table(name = "users")
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

    @Transient
    private Set<UserRole> roles = new HashSet<>();

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "created_date", nullable = false)
    private LocalDate createdDate = LocalDate.now();

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "manager_id")
    private String managerId;

    @Column
    private String department;

    @Column
    private String territory;

    /**
     * Constructor with essential fields
     */
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

    /**
     * Get the full name of the user
     * @return the full name (first name + last name)
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Check if the user is a sales representative
     * @return true if the user has the SALES_REP role
     */
    public boolean isSalesRep() {
        return hasRole(UserRole.SALES_REP);
    }

    /**
     * Check if the user is a sales manager
     * @return true if the user has the SALES_MANAGER role
     */
    public boolean isSalesManager() {
        return hasRole(UserRole.SALES_MANAGER);
    }

    /**
     * Check if the user is a finance administrator
     * @return true if the user has the FINANCE_ADMIN role
     */
    public boolean isFinanceAdmin() {
        return hasRole(UserRole.FINANCE_ADMIN);
    }

    /**
     * Check if the user is a system administrator
     * @return true if the user has the SYSTEM_ADMIN role
     */
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
}
