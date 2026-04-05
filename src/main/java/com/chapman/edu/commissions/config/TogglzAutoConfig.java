package com.chapman.edu.commissions.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.core.user.SimpleFeatureUser;
import org.togglz.core.user.UserProvider;

/**
 * Global Togglz auto-configuration that provides a default UserProvider.
 * This is registered via META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
 * so it's available to ALL @SpringBootApplication classes in the project, not just
 * those that scan the verticalslice package.
 */
@Configuration
@ConditionalOnClass(UserProvider.class)
@ConditionalOnProperty(name = "togglz.enabled", havingValue = "true")
public class TogglzAutoConfig {

    @Bean
    @ConditionalOnMissingBean(UserProvider.class)
    public UserProvider togglzUserProvider() {
        return () -> new SimpleFeatureUser("admin", true);
    }
}
