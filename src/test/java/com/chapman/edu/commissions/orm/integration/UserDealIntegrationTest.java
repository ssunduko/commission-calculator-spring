package com.chapman.edu.commissions.orm.integration;

import com.chapman.edu.commissions.orm.CommissionCalculatorOrmApplication;
import com.chapman.edu.commissions.orm.entity.DealProduct;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ============================================================================
 * INTEGRATION TEST — ORM User and Deal Lifecycle
 * ============================================================================
 *
 * CONCEPT: Full-Stack ORM Integration Testing
 * -----------------------------------------------
 * This test exercises the complete ORM stack:
 *
 *   HTTP Request → Controller → Service → JPA Repository → H2 Database
 *
 * Unlike the springboot module (which uses HashMap repositories), the ORM
 * module uses real JPA with H2 in-memory database and Flyway migrations.
 *
 * CONCEPT: Flyway Seed Data in Tests
 * --------------------------------------
 * The Flyway V2 migration seeds the H2 database with sample data:
 *   - 4 users (usr-001 through usr-004)
 *   - 3 commission plans
 *   - 6 deals with products
 *   - Commission calculations and disputes
 *
 * Integration tests can query this pre-seeded data, avoiding the need
 * for manual test data setup. This mirrors a production-like scenario
 * where the database already contains data.
 *
 * CONCEPT: @TestMethodOrder
 * ---------------------------
 * Tests are ordered so that read-only queries (which assert exact counts
 * from the Flyway seed data) run BEFORE mutation tests (create, update,
 * deactivate). Without ordering, a mutation test could run first and
 * change the data that a read test depends on.
 *
 * CONCEPT: ORM SecurityConfig
 * --------------------------------
 * The ORM module has its own SecurityConfig that permits all requests.
 * This means no authentication is needed — we can call endpoints directly.
 * This is by design: the ORM module focuses on teaching JPA/Hibernate
 * concepts, not security.
 */
@SpringBootTest(classes = CommissionCalculatorOrmApplication.class)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("ORM User & Deal — Integration Tests")
class UserDealIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ============================================================
    // USER ENDPOINTS — Read-only queries on Flyway-seeded data
    // ============================================================

    @Test
    @Order(1)
    @DisplayName("GET /api/orm/users/{id} should return seeded user from Flyway V2")
    void getUserById_shouldReturnSeededUser() throws Exception {
        mockMvc.perform(get("/api/orm/users/usr-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("jsmith"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.department").value("Sales"))
                .andExpect(jsonPath("$.territory").value("West Coast"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @Order(2)
    @DisplayName("GET /api/orm/users/{id} should return 404 for non-existent user")
    void getUserById_shouldReturn404_forNonExistentUser() throws Exception {
        mockMvc.perform(get("/api/orm/users/non-existent"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/orm/users/username/{username} should find user by username")
    void getUserByUsername_shouldReturnUser() throws Exception {
        mockMvc.perform(get("/api/orm/users/username/ajones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("usr-002"))
                .andExpect(jsonPath("$.firstName").value("Alice"));
    }

    @Test
    @Order(4)
    @DisplayName("GET /api/orm/users/department/{dept} should return users in department")
    void getUsersByDepartment_shouldReturnFilteredUsers() throws Exception {
        mockMvc.perform(get("/api/orm/users/department/Sales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    @Order(5)
    @DisplayName("GET /api/orm/users/role/SALES_REP should return sales reps")
    void getUsersByRole_shouldReturnUsersWithRole() throws Exception {
        mockMvc.perform(get("/api/orm/users/role/SALES_REP"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    @Order(6)
    @DisplayName("GET /api/orm/users/{managerId}/direct-reports should return direct reports")
    void getDirectReports_shouldReturnManagedUsers() throws Exception {
        // usr-004 (Maria) manages usr-001, usr-002, usr-003
        mockMvc.perform(get("/api/orm/users/usr-004/direct-reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    @Order(7)
    @DisplayName("GET /api/orm/users/search?name=smith should find matching users")
    void searchUsers_shouldReturnMatchingUsers() throws Exception {
        mockMvc.perform(get("/api/orm/users/search").param("name", "smith"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].lastName").value("Smith"));
    }

    // ============================================================
    // DEAL ENDPOINTS — Read-only queries on Flyway-seeded data
    // ============================================================

    @Test
    @Order(8)
    @DisplayName("GET /api/orm/deals/{id} should return seeded deal from Flyway V2")
    void getDealById_shouldReturnSeededDeal() throws Exception {
        mockMvc.perform(get("/api/orm/deals/deal-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Acme Corp ERP Implementation"))
                .andExpect(jsonPath("$.status").value("WON"));
    }

    @Test
    @Order(9)
    @DisplayName("GET /api/orm/deals/status/WON should return won deals")
    void getDealsByStatus_shouldReturnWonDeals() throws Exception {
        mockMvc.perform(get("/api/orm/deals/status/WON"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    @Order(10)
    @DisplayName("GET /api/orm/deals/search should return paginated results")
    void searchDeals_shouldReturnPaginatedResults() throws Exception {
        mockMvc.perform(get("/api/orm/deals/search")
                        .param("status", "WON")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(4));
    }

    @Test
    @Order(11)
    @DisplayName("GET /api/orm/deals/search with value range should filter correctly")
    void searchDeals_withValueRange_shouldFilter() throws Exception {
        mockMvc.perform(get("/api/orm/deals/search")
                        .param("minValue", "50000")
                        .param("maxValue", "200000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @Order(12)
    @DisplayName("GET /api/orm/deals/sales-rep/{id} should return paginated deals for rep")
    void getDealsBySalesRep_shouldReturnDeals() throws Exception {
        // usr-001 (jsmith) has 2 deals: deal-001 and deal-002
        mockMvc.perform(get("/api/orm/deals/sales-rep/usr-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @Order(13)
    @DisplayName("GET /api/orm/deals/sales-rep/{id}/total-won should return total value")
    void getTotalWonValue_shouldReturnCorrectTotal() throws Exception {
        // usr-001 has deal-001 ($85,000) + deal-002 ($32,000) = $117,000
        mockMvc.perform(get("/api/orm/deals/sales-rep/usr-001/total-won"))
                .andExpect(status().isOk())
                .andExpect(content().string("117000.00"));
    }

    // ============================================================
    // USER CRUD — Mutation tests (run AFTER read-only queries)
    // ============================================================

    @Test
    @Order(20)
    @DisplayName("POST /api/orm/users should create a new user and persist to H2")
    void createUser_shouldPersistAndReturn201() throws Exception {
        String userJson = """
                {
                    "username": "newuser",
                    "email": "new@chapman.edu",
                    "firstName": "New",
                    "lastName": "User",
                    "passwordHash": "hashed",
                    "department": "Engineering"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/orm/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andReturn();

        // Verify the user was actually persisted by fetching it
        String userId = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("id").asText();

        mockMvc.perform(get("/api/orm/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newuser"));
    }

    @Test
    @Order(21)
    @DisplayName("PUT /api/orm/users/{id}/deactivate should deactivate the user")
    void deactivateUser_shouldSetActiveToFalse() throws Exception {
        mockMvc.perform(put("/api/orm/users/usr-003/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        // Verify persistence
        mockMvc.perform(get("/api/orm/users/usr-003"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    // ============================================================
    // DEAL CRUD — Mutation tests (run AFTER read-only queries)
    // ============================================================

    @Test
    @Order(22)
    @DisplayName("POST /api/orm/deals should create a deal linked to an existing sales rep")
    void createDeal_shouldPersistDeal() throws Exception {
        // Use usr-002 (not usr-001) to avoid affecting earlier sales rep count assertions
        MvcResult result = mockMvc.perform(post("/api/orm/deals")
                        .param("title", "Integration Test Deal")
                        .param("value", "55000")
                        .param("salesRepId", "usr-002"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Integration Test Deal"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andReturn();

        // Verify persistence
        String dealId = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("id").asText();

        mockMvc.perform(get("/api/orm/deals/" + dealId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Integration Test Deal"));
    }

    @Test
    @Order(23)
    @DisplayName("PUT /api/orm/deals/{id}/status should update deal status")
    void updateDealStatus_shouldPersistStatusChange() throws Exception {
        // deal-005 is currently OPEN
        mockMvc.perform(put("/api/orm/deals/deal-005/status")
                        .param("status", "WON"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("WON"));
    }

    @Test
    @Order(24)
    @DisplayName("POST /api/orm/deals/{id}/products should add product to deal")
    void addProductToDeal_shouldPersistProduct() throws Exception {
        DealProduct product = new DealProduct();
        product.setProductId("PROD-TEST");
        product.setProductName("Test Product");
        product.setQuantity(5);
        product.setPrice(new BigDecimal("1000"));
        product.setDiscount(BigDecimal.ZERO);

        mockMvc.perform(post("/api/orm/deals/deal-005/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isOk());
    }
}
