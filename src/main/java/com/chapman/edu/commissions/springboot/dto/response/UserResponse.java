package com.chapman.edu.commissions.springboot.dto.response;

import java.time.LocalDate;
import java.util.Set;

/**
 * Response DTO for User data.
 *
 * NOTE: The passwordHash is intentionally EXCLUDED from this DTO.
 * Never expose sensitive data like passwords in API responses.
 */
public class UserResponse {
    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Set<String> roles;
    private boolean active;
    private String department;
    private String territory;
    private LocalDate createdDate;

    // --- Getters and Setters ---

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
    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getTerritory() { return territory; }
    public void setTerritory(String territory) { this.territory = territory; }
    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }
}
