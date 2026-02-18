package com.chapman.edu.commissions.springboot.controller;

import com.chapman.edu.commissions.model.User;
import com.chapman.edu.commissions.model.UserRole;
import com.chapman.edu.commissions.springboot.config.SecurityConfig;
import com.chapman.edu.commissions.springboot.dto.response.UserResponse;
import com.chapman.edu.commissions.springboot.mapper.DtoMapper;
import com.chapman.edu.commissions.springboot.security.CustomUserDetailsService;
import com.chapman.edu.commissions.springboot.security.JwtAuthenticationFilter;
import com.chapman.edu.commissions.springboot.security.JwtTokenProvider;
import com.chapman.edu.commissions.springboot.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * WebMvc tests for Spring Boot UserController.
 *
 * KEY CONCEPTS DEMONSTRATED:
 * - Role-based access control testing with @WithMockUser
 * - The /api/users/** endpoints require SYSTEM_ADMIN or SALES_MANAGER role
 *   (configured in SecurityConfig)
 */
@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
@DisplayName("SpringBoot UserController — WebMvc Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private DtoMapper mapper;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    private UserResponse testUserResponse;

    @BeforeEach
    void setUp() {
        testUserResponse = new UserResponse();
        testUserResponse.setId("user-001");
        testUserResponse.setUsername("jsmith");
        testUserResponse.setEmail("john@test.com");
        testUserResponse.setFirstName("John");
        testUserResponse.setLastName("Smith");
        testUserResponse.setActive(true);
        testUserResponse.setRoles(Set.of("SALES_REP"));
    }

    @Test
    @WithMockUser(roles = "SYSTEM_ADMIN")
    @DisplayName("GET /api/users should return 200 for SYSTEM_ADMIN")
    void getAllUsers_shouldReturn200() throws Exception {
        User user = new User("jsmith", "john@test.com", "John", "Smith");
        user.setId("user-001");
        when(userService.getAllUsers()).thenReturn(List.of(user));
        when(mapper.toUserResponse(any(User.class))).thenReturn(testUserResponse);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].username").value("jsmith"));
    }

    @Test
    @WithMockUser(roles = "SALES_MANAGER")
    @DisplayName("GET /api/users/{id} should return 200 for SALES_MANAGER")
    void getUserById_shouldReturn200() throws Exception {
        User user = new User("jsmith", "john@test.com", "John", "Smith");
        user.setId("user-001");
        when(userService.getUserById("user-001")).thenReturn(user);
        when(mapper.toUserResponse(any(User.class))).thenReturn(testUserResponse);

        mockMvc.perform(get("/api/users/user-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("jsmith"));
    }

    @Test
    @WithMockUser(roles = "SALES_REP")
    @DisplayName("GET /api/users should return 403 for SALES_REP (insufficient role)")
    void getAllUsers_shouldReturn403_forSalesRep() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/users should return 401 without authentication")
    void getAllUsers_shouldReturn401_withoutAuth() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }
}
