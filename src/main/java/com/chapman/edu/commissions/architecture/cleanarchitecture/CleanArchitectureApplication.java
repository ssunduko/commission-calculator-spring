package com.chapman.edu.commissions.architecture.cleanarchitecture;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(
    scanBasePackages = "com.chapman.edu.commissions.architecture.cleanarchitecture",
    exclude = {
        org.springframework.ai.model.anthropic.autoconfigure.AnthropicChatAutoConfiguration.class
    }
)
@EntityScan("com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model")
@EnableJpaRepositories("com.chapman.edu.commissions.architecture.cleanarchitecture.adapter.out.persistence")
public class CleanArchitectureApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(CleanArchitectureApplication.class);
        app.setAdditionalProfiles("cleanarchitecture");
        app.run(args);
    }
}
