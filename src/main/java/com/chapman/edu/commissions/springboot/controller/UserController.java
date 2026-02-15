package com.chapman.edu.commissions.springboot.controller;

import com.chapman.edu.commissions.model.User;
import com.chapman.edu.commissions.model.UserRole;
import com.chapman.edu.commissions.springboot.dto.request.CreateUserRequest;
import com.chapman.edu.commissions.springboot.dto.response.ApiResponse;
import com.chapman.edu.commissions.springboot.dto.response.UserResponse;
import com.chapman.edu.commissions.springboot.mapper.DtoMapper;
import com.chapman.edu.commissions.springboot.service.UserService;
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
public class UserController {

    private final UserService userService;
    private final DtoMapper mapper;

    public UserController(UserService userService, DtoMapper mapper) {
        this.userService = userService;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(
            @RequestParam(required = false) String role) {

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

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable String id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(
            ApiResponse.success("User retrieved", mapper.toUserResponse(user)));
    }

    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        User user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created", mapper.toUserResponse(user)));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> deactivateUser(@PathVariable String id) {
        User user = userService.deactivateUser(id);
        return ResponseEntity.ok(
            ApiResponse.success("User deactivated", mapper.toUserResponse(user)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
