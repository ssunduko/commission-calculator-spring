package com.chapman.edu.commissions.springboot.controller;

import com.chapman.edu.commissions.model.User;
import com.chapman.edu.commissions.model.UserRole;
import com.chapman.edu.commissions.springboot.dto.request.CreateUserRequest;
import com.chapman.edu.commissions.springboot.dto.response.ApiResponse;
import com.chapman.edu.commissions.springboot.dto.response.UserResponse;
import com.chapman.edu.commissions.springboot.mapper.DtoMapper;
import com.chapman.edu.commissions.springboot.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for User management.
 *
 * Access is restricted to SYSTEM_ADMIN and SALES_MANAGER roles
 * (configured in SecurityConfig URL rules).
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management — create, read, deactivate, and delete users")
public class UserController {

    private final UserService userService;
    private final DtoMapper mapper;

    public UserController(UserService userService, DtoMapper mapper) {
        this.userService = userService;
        this.mapper = mapper;
    }

    @Operation(summary = "List all users", description = "Retrieve all users, optionally filtered by role")
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(
            @Parameter(description = "Filter by role (SALES_REP, SALES_MANAGER, FINANCE_ADMIN, SYSTEM_ADMIN)") @RequestParam(required = false) String role) {

        List<User> users;
        if (role != null) {
            users = userService.getUsersByRole(UserRole.valueOf(role));
        } else {
            users = userService.getAllUsers();
        }

        List<UserResponse> responses = users.stream()
                .map(mapper::toUserResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Users retrieved", responses));
    }

    @Operation(summary = "Get user by ID", description = "Retrieve a specific user by their unique identifier")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @Parameter(description = "User ID", example = "user-001") @PathVariable String id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(
            ApiResponse.success("User retrieved", mapper.toUserResponse(user)));
    }

    @Operation(summary = "Create a new user", description = "Create a new user account. Requires SYSTEM_ADMIN role.")
    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        User user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created", mapper.toUserResponse(user)));
    }

    @Operation(summary = "Deactivate a user", description = "Deactivate a user account. Requires SYSTEM_ADMIN role.")
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> deactivateUser(
            @Parameter(description = "User ID", example = "user-003") @PathVariable String id) {
        User user = userService.deactivateUser(id);
        return ResponseEntity.ok(
            ApiResponse.success("User deactivated", mapper.toUserResponse(user)));
    }

    @Operation(summary = "Delete a user", description = "Permanently remove a user account. Requires SYSTEM_ADMIN role.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID", example = "user-003") @PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
