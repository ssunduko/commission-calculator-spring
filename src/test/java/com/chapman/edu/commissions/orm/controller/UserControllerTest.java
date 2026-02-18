package com.chapman.edu.commissions.orm.controller;

import com.chapman.edu.commissions.orm.config.SecurityConfig;
import com.chapman.edu.commissions.orm.entity.User;
import com.chapman.edu.commissions.orm.entity.UserRole;
import com.chapman.edu.commissions.orm.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * WebMvc tests for ORM UserController.
 *
 * KEY CONCEPTS DEMONSTRATED:
 * - @WebMvcTest: Loads only the web layer (controller + filters), NOT the full app
 * - @MockitoBean: Replaces the real service bean with a Mockito mock in the Spring context
 * - MockMvc: Simulates HTTP requests without starting a real HTTP server
 * - @Import(SecurityConfig.class): Imports the ORM SecurityConfig which permits all requests
 *
 * WHY @Import(SecurityConfig.class)?
 * The ORM module has its own SecurityConfig that permits all requests.
 * Without importing it, @WebMvcTest would use Spring Security defaults
 * which require authentication for all endpoints.
 */
@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
@DisplayName("ORM UserController — WebMvc Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("jsmith", "john@test.com", "John", "Smith");
        testUser.setId("usr-001");
        testUser.addRole(UserRole.SALES_REP);
        testUser.setDepartment("Sales");
    }

    // ============================================================
    // GET ENDPOINTS
    // ============================================================

    @Test
    @DisplayName("GET /api/orm/users/{id} should return 200 when user exists")
    void getUserById_shouldReturn200_whenUserExists() throws Exception {
        when(userService.findById("usr-001")).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/orm/users/usr-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("jsmith"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Smith"));
    }

    @Test
    @DisplayName("GET /api/orm/users/{id} should return 404 when user not found")
    void getUserById_shouldReturn404_whenUserNotFound() throws Exception {
        when(userService.findById("nonexistent")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/orm/users/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/orm/users/department/{dept} should return 200 with users")
    void getUsersByDepartment_shouldReturn200() throws Exception {
        when(userService.findByDepartment("Sales")).thenReturn(List.of(testUser));

        mockMvc.perform(get("/api/orm/users/department/Sales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].username").value("jsmith"));
    }

    @Test
    @DisplayName("GET /api/orm/users/role/{role} should return 200 with users")
    void getUsersByRole_shouldReturn200() throws Exception {
        when(userService.findActiveUsersByRole(UserRole.SALES_REP)).thenReturn(List.of(testUser));

        mockMvc.perform(get("/api/orm/users/role/SALES_REP"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].username").value("jsmith"));
    }

    @Test
    @DisplayName("GET /api/orm/users/search?name= should return 200 with matching users")
    void searchUsers_shouldReturn200() throws Exception {
        when(userService.searchByName("smith")).thenReturn(List.of(testUser));

        mockMvc.perform(get("/api/orm/users/search").param("name", "smith"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].lastName").value("Smith"));
    }

    // ============================================================
    // POST / PUT ENDPOINTS
    // ============================================================

    @Test
    @DisplayName("POST /api/orm/users should return 201 when user created")
    void createUser_shouldReturn201() throws Exception {
        when(userService.createUser(any(User.class))).thenReturn(testUser);

        mockMvc.perform(post("/api/orm/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("jsmith"));
    }

    @Test
    @DisplayName("PUT /api/orm/users/{id}/deactivate should return 200")
    void deactivateUser_shouldReturn200() throws Exception {
        testUser.setActive(false);
        when(userService.deactivateUser("usr-001")).thenReturn(testUser);

        mockMvc.perform(put("/api/orm/users/usr-001/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }
}
