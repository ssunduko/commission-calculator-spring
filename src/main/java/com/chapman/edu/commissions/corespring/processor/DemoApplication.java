package com.chapman.edu.commissions.corespring.processor;

import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@ComponentScan(
        basePackages = {
                "com.chapman.edu.commissions.corespring",
                "com.chapman.edu.commissions.model"  // Need model classes (Deal, etc.)
        },
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.chapman.edu.commissions.verticalslice.*")
        }
)
public class DemoApplication {

    public static void main(String[] args) {
        // Disable banner for cleaner output
        SpringApplication app = new SpringApplication(DemoApplication.class);
        app.setBannerMode(org.springframework.boot.Banner.Mode.OFF);

        // Set properties for minimal configuration (no web server, no database)
        System.setProperty("spring.main.web-application-type", "NONE");
        System.setProperty("spring.autoconfigure.exclude",
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration");

        app.run(args);
    }
}
