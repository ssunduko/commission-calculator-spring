package com.chapman.edu.commissions.architecture.verticalslice.e2e;

import com.chapman.edu.commissions.architecture.verticalslice.CommissionCalculatorApplication;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for Selenium end-to-end tests against the vertical slice architecture.
 *
 * Uses @SpringBootTest with RANDOM_PORT to start the full application,
 * TestRestTemplate for REST API verification, and HtmlUnitDriver (Selenium)
 * for browser-based verification (Swagger UI, H2 Console).
 */
@SpringBootTest(
    classes = CommissionCalculatorApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:seleniume2edb",
    "spring.flyway.enabled=false",
    "spring.security.user.name=admin",
    "spring.security.user.password=admin123"
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseSeleniumE2ETest {

    @LocalServerPort
    protected int port;

    @Autowired
    protected TestRestTemplate restTemplate;

    protected WebDriver driver;

    @BeforeAll
    void setUpWebDriver() {
        HtmlUnitDriver htmlUnitDriver = new HtmlUnitDriver();
        htmlUnitDriver.setJavascriptEnabled(true);
        driver = htmlUnitDriver;
    }

    @AfterAll
    void tearDownWebDriver() {
        if (driver != null) {
            driver.quit();
        }
    }

    protected String baseUrl() {
        return "http://localhost:" + port;
    }

    protected TestRestTemplate authenticated() {
        return restTemplate.withBasicAuth("admin", "admin123");
    }

    // --------------- Data creation helpers ---------------

    @SuppressWarnings("unchecked")
    protected Map<String, Object> createDeal(String title, BigDecimal value, String salesRepId) {
        Map<String, Object> request = new HashMap<>();
        request.put("title", title);
        request.put("value", value);
        request.put("salesRepId", salesRepId);

        ResponseEntity<Map> response = authenticated()
            .postForEntity("/api/deals", request, Map.class);
        return response.getBody();
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> createPlan(String name, String currencyCode) {
        Map<String, Object> request = new HashMap<>();
        request.put("name", name);
        request.put("currencyCode", currencyCode);
        request.put("effectiveStartDate", LocalDate.now().minusMonths(1).toString());
        request.put("effectiveEndDate", LocalDate.now().plusMonths(6).toString());

        ResponseEntity<Map> response = authenticated()
            .postForEntity("/api/plans", request, Map.class);
        return response.getBody();
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> activatePlan(String planId) {
        ResponseEntity<Map> response = authenticated()
            .postForEntity("/api/plans/" + planId + "/activate", null, Map.class);
        return response.getBody();
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> addRuleToPlan(String planId, String ruleName, BigDecimal rate) {
        Map<String, Object> request = new HashMap<>();
        request.put("name", ruleName);
        request.put("description", "Test rule");
        request.put("rate", rate);
        request.put("ruleType", "STANDARD");
        request.put("priority", 1);

        ResponseEntity<Map> response = authenticated()
            .postForEntity("/api/plans/" + planId + "/rules", request, Map.class);
        return response.getBody();
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> createCalculation(String dealId, String planId) {
        Map<String, Object> request = new HashMap<>();
        request.put("dealId", dealId);
        request.put("planId", planId);

        ResponseEntity<Map> response = authenticated()
            .postForEntity("/api/calculations", request, Map.class);
        return response.getBody();
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> createDispute(String calculationId, String salesRepId,
                                                 String title, String description) {
        Map<String, Object> request = new HashMap<>();
        request.put("calculationId", calculationId);
        request.put("salesRepId", salesRepId);
        request.put("title", title);
        request.put("description", description);

        ResponseEntity<Map> response = authenticated()
            .postForEntity("/api/disputes", request, Map.class);
        return response.getBody();
    }

    protected HttpEntity<Map<String, Object>> jsonEntity(Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    @SuppressWarnings("unchecked")
    protected <T> ResponseEntity<T> authenticatedExchange(String url, HttpMethod method,
                                                           HttpEntity<?> entity, Class<T> responseType) {
        return authenticated().exchange(url, method, entity, responseType);
    }
}
