package com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Domain entity representing a user (sales representative or manager).
 */
@Entity
@Table(name = "users")
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

    public User() {
        this.roles = new HashSet<>();
        this.active = true;
        this.createdDate = LocalDate.now();
    }

    public User(String username, String email, String firstName, String lastName) {
        this();
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
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

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Set<UserRole> getRoles() { return roles; }
    public void setRoles(Set<UserRole> roles) { this.roles = roles; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getManagerId() { return managerId; }
    public void setManagerId(String managerId) { this.managerId = managerId; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getTerritory() { return territory; }
    public void setTerritory(String territory) { this.territory = territory; }
}
