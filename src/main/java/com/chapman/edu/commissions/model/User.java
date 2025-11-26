package com.chapman.edu.commissions.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a user in the system.
 * Users can be sales representatives, managers, finance administrators, or system administrators.
 */
public class User {
    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String passwordHash;
    private Set<UserRole> roles;
    private boolean active;
    private LocalDateTime lastLogin;
    private LocalDate createdDate;
    private String createdBy;
    private String managerId;
    private String department;
    private String territory;
    
    /**
     * Default constructor
     */
    public User() {
        this.roles = new HashSet<>();
        this.active = true;
        this.createdDate = LocalDate.now();
    }
    
    /**
     * Constructor with essential fields
     */
    public User(String username, String email, String firstName, String lastName) {
        this();
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }
    
    // Getters and Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public Set<UserRole> getRoles() {
        return roles;
    }
    
    public void setRoles(Set<UserRole> roles) {
        this.roles = roles;
    }
    
    public void addRole(UserRole role) {
        this.roles.add(role);
    }
    
    public boolean hasRole(UserRole role) {
        return this.roles.contains(role);
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public LocalDateTime getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }
    
    public LocalDate getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public String getManagerId() {
        return managerId;
    }
    
    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    public String getTerritory() {
        return territory;
    }
    
    public void setTerritory(String territory) {
        this.territory = territory;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
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