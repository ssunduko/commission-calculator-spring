package com.chapman.edu.commissions.architecture.verticalslice.features.authentication;

import com.chapman.edu.commissions.architecture.verticalslice.features.registration.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("verticalslice")
@Transactional
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:authitdb",
    "spring.flyway.enabled=true"
})
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listSubscriptionPackages_isPublic_andReturnsSeededPackages() throws Exception {
        mockMvc.perform(get("/api/subscription-packages"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(greaterThan(0)))
            .andExpect(jsonPath("$[?(@.code=='BASIC')].name").value(notNullValue()))
            .andExpect(jsonPath("$[?(@.code=='PROFESSIONAL')].name").value(notNullValue()))
            .andExpect(jsonPath("$[?(@.code=='ENTERPRISE')].name").value(notNullValue()));
    }

    @Test
    void login_withSeedCredentials_returnsJwtToken() throws Exception {
        Map<String, String> body = Map.of("username", "jsmith", "password", "sales123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.username").value("jsmith"))
            .andExpect(jsonPath("$.userId").value("usr-001"));
    }

    @Test
    void login_withWrongPassword_returnsUnauthorized() throws Exception {
        Map<String, String> body = Map.of("username", "jsmith", "password", "wrong");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").isNotEmpty());
    }

    @Test
    void login_withUnknownUser_returnsUnauthorized() throws Exception {
        Map<String, String> body = Map.of("username", "doesnotexist", "password", "whatever");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void register_withValidCard_createsUserAndReturnsJwt() throws Exception {
        RegisterRequest request = new RegisterRequest(
            "newuser1",
            "newuser1@example.com",
            "New",
            "User",
            "Sup3rSecret!",
            "PROFESSIONAL",
            new RegisterRequest.PaymentDetails(
                "New User", "4242 4242 4242 4242", "12", "2030", "123"));

        MvcResult result = mockMvc.perform(post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.userId").isNotEmpty())
            .andExpect(jsonPath("$.username").value("newuser1"))
            .andExpect(jsonPath("$.email").value("newuser1@example.com"))
            .andExpect(jsonPath("$.packageCode").value("PROFESSIONAL"))
            .andExpect(jsonPath("$.subscriptionStatus").value("ACTIVE"))
            .andExpect(jsonPath("$.paymentStatus").value("COMPLETED"))
            .andExpect(jsonPath("$.cardLastFour").value("4242"))
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andReturn();

        String response = result.getResponse().getContentAsString();
        Map<?, ?> body = objectMapper.readValue(response, Map.class);
        String username = (String) body.get("username");

        Map<String, String> login = Map.of("username", username, "password", "Sup3rSecret!");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void register_withDuplicateUsername_returnsConflict() throws Exception {
        RegisterRequest request = new RegisterRequest(
            "jsmith",
            "new-jsmith@example.com",
            "John",
            "Smith",
            "Sup3rSecret!",
            "BASIC",
            new RegisterRequest.PaymentDetails(
                "John Smith", "4242 4242 4242 4242", "12", "2030", "123"));

        mockMvc.perform(post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void register_withDeclinedCard_returnsBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest(
            "declineduser",
            "declined@example.com",
            "Declined",
            "Card",
            "Sup3rSecret!",
            "BASIC",
            new RegisterRequest.PaymentDetails(
                "Declined Card", "4111 1111 1111 0000", "12", "2030", "123"));

        mockMvc.perform(post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().is4xxClientError());
    }
}
