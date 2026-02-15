package com.chapman.edu.commissions.springboot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * ============================================================================
 * WEB MVC CONFIGURATION — SPRING MVC CUSTOMIZATION
 * ============================================================================
 *
 * CONCEPT: Spring MVC (Model-View-Controller)
 * ---------------------------------------------
 * Spring MVC is the web framework within Spring that implements the MVC pattern:
 *
 *   Model  — The data to be displayed (passed to templates via Model object)
 *   View   — The presentation layer (Thymeleaf templates in our case)
 *   Controller — Handles HTTP requests, populates model, selects view
 *
 * Request Flow:
 *   1. Client sends HTTP request
 *   2. DispatcherServlet receives it (front controller pattern)
 *   3. HandlerMapping maps URL to controller method
 *   4. Controller processes request, returns view name + model data
 *   5. ViewResolver locates the Thymeleaf template
 *   6. Template engine renders HTML with model data
 *   7. Response sent back to client
 *
 * CONCEPT: WebMvcConfigurer
 * ---------------------------
 * By implementing WebMvcConfigurer, we can customize Spring MVC behavior:
 *   - Add resource handlers (CSS, JS, images)
 *   - Add view controllers (simple URL-to-view mappings)
 *   - Configure CORS, interceptors, formatters, etc.
 *
 * CONCEPT: Thymeleaf Templating
 * -------------------------------
 * Thymeleaf is a modern server-side template engine for Java. It:
 *   - Processes HTML templates with th:* attributes
 *   - Supports natural templating (templates are valid HTML)
 *   - Integrates with Spring Expression Language (SpEL)
 *   - Provides Spring Security dialect for role-based rendering
 *
 * Example Thymeleaf expressions:
 *   th:text="${variable}"          — Outputs text
 *   th:each="item : ${list}"      — Iterates over a collection
 *   th:if="${condition}"           — Conditional rendering
 *   th:href="@{/path}"            — URL expression
 *   sec:authorize="hasRole('X')"  — Security-aware rendering
 *
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurer
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * Register static resource handlers.
     *
     * This tells Spring MVC where to find static files (CSS, JavaScript, images).
     * By default, Spring Boot serves static content from:
     *   - classpath:/static/
     *   - classpath:/public/
     *   - classpath:/resources/
     *   - classpath:/META-INF/resources/
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }

    /**
     * Register simple automated controllers for views that don't need
     * custom logic. This avoids creating a full @Controller class for
     * simple URL-to-template mappings.
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Map /login URL directly to the login.html Thymeleaf template
        registry.addViewController("/login").setViewName("springboot/login");
    }
}
