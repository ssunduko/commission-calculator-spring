package com.chapman.edu.commissions.corespring.demo;

import com.chapman.edu.commissions.corespring.config.AppConfig;
import com.chapman.edu.commissions.corespring.config.ConfigurationProperties;
import com.chapman.edu.commissions.corespring.config.ProfileConfig;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Runnable demo showing Configuration concepts.
 *
 * Run this class to see:
 * - Java-based configuration
 * - @Value and property injection
 * - Profile-based configuration
 * - Conditional bean registration
 *
 * To run: Uncomment @Component annotation and start the application
 */
//@Component  // Uncomment to run this demo
public class ConfigurationDemo implements CommandLineRunner {

    private final ApplicationContext context;
    private final Environment environment;

    public ConfigurationDemo(ApplicationContext context,
                            Environment environment) {
        this.context = context;
        this.environment = environment;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n========================================");
        System.out.println("CONFIGURATION DEMO");
        System.out.println("========================================\n");

        demonstratePropertyInjection();
        demonstrateProfiles();
        demonstrateConditionalBeans();
        explainConfigurationStrategies();
    }

    private void demonstratePropertyInjection() {
        System.out.println("--- @Value and Property Injection ---");

        // Get the configuration properties bean
        ConfigurationProperties config = context.getBean(ConfigurationProperties.class);

        System.out.println("Properties loaded from application.properties:");
        System.out.println("Application Name: " + config.getApplicationName());
        System.out.println("Default Commission Rate: " + config.getDefaultCommissionRate());
        System.out.println("Bonus Threshold: " + config.getBonusThreshold());

        System.out.println("\nHow @Value works:");
        System.out.println("@Value(\"${commission.default.rate:0.10}\")");
        System.out.println("  - ${...} = property placeholder");
        System.out.println("  - :0.10 = default value if property not found");
        System.out.println();

        System.out.println("SpEL (Spring Expression Language) examples:");
        System.out.println("@Value(\"#{${commission.rate} * 100}\")  // Math expression");
        System.out.println("@Value(\"#{systemProperties['user.home']}\")  // System property");
        System.out.println("@Value(\"#{configBean.enabled ? 'ON' : 'OFF'}\")  // Conditional");
        System.out.println();
    }

    private void demonstrateProfiles() {
        System.out.println("--- Spring Profiles ---");

        String[] activeProfiles = environment.getActiveProfiles();
        String[] defaultProfiles = environment.getDefaultProfiles();

        System.out.println("Active profiles: " + String.join(", ",
            activeProfiles.length > 0 ? activeProfiles : new String[]{"none"}));
        System.out.println("Default profiles: " + String.join(", ", defaultProfiles));

        // Try to get environment-specific bean
        try {
            ProfileConfig.EnvironmentConfig envConfig =
                context.getBean(ProfileConfig.EnvironmentConfig.class);

            System.out.println("\nEnvironment Configuration:");
            System.out.println("Environment: " + envConfig.getEnvironmentName());
            System.out.println("Debug Enabled: " + envConfig.isDebugEnabled());
            System.out.println("Cache Enabled: " + envConfig.isCacheEnabled());
            System.out.println("Database URL: " + envConfig.getDatabaseUrl());
        } catch (Exception e) {
            System.out.println("\nNo profile-specific configuration found");
            System.out.println("To activate a profile, use: -Dspring.profiles.active=dev");
        }

        System.out.println("\nProfile usage:");
        System.out.println("@Profile(\"dev\") - Bean only created in dev profile");
        System.out.println("@Profile(\"prod\") - Bean only created in prod profile");
        System.out.println("@Profile({\"dev\", \"test\"}) - Bean created in dev OR test");
        System.out.println("@Profile(\"!prod\") - Bean created when NOT prod");
        System.out.println();

        System.out.println("Activate profiles:");
        System.out.println("- Command line: -Dspring.profiles.active=dev");
        System.out.println("- application.properties: spring.profiles.active=dev");
        System.out.println("- Environment variable: SPRING_PROFILES_ACTIVE=dev");
        System.out.println();
    }

    private void demonstrateConditionalBeans() {
        System.out.println("--- Conditional Bean Registration ---");

        System.out.println("Conditional annotations control bean creation:");
        System.out.println();

        System.out.println("@ConditionalOnProperty:");
        System.out.println("  @Bean");
        System.out.println("  @ConditionalOnProperty(name = \"feature.enabled\", havingValue = \"true\")");
        System.out.println("  public FeatureService featureService() { ... }");
        System.out.println("  → Only created if property is true");
        System.out.println();

        System.out.println("@ConditionalOnBean:");
        System.out.println("  @Bean");
        System.out.println("  @ConditionalOnBean(DataSource.class)");
        System.out.println("  public JdbcTemplate jdbcTemplate(DataSource ds) { ... }");
        System.out.println("  → Only created if DataSource bean exists");
        System.out.println();

        System.out.println("@ConditionalOnMissingBean:");
        System.out.println("  @Bean");
        System.out.println("  @ConditionalOnMissingBean(CacheManager.class)");
        System.out.println("  public CacheManager defaultCache() { ... }");
        System.out.println("  → Fallback bean, only if custom one not provided");
        System.out.println();

        System.out.println("@ConditionalOnClass:");
        System.out.println("  @Bean");
        System.out.println("  @ConditionalOnClass(name = \"redis.clients.jedis.Jedis\")");
        System.out.println("  public RedisCache redisCache() { ... }");
        System.out.println("  → Only if Redis library is on classpath");
        System.out.println();
    }

    private void explainConfigurationStrategies() {
        System.out.println("--- Configuration Strategies ---");

        System.out.println("1. XML Configuration (Legacy):");
        System.out.println("   <bean id=\"service\" class=\"com.example.Service\"/>");
        System.out.println("   Pros: Externalized, no recompilation");
        System.out.println("   Cons: Verbose, no type-safety, hard to refactor");
        System.out.println();

        System.out.println("2. Java Configuration (Modern):");
        System.out.println("   @Configuration");
        System.out.println("   public class AppConfig {");
        System.out.println("       @Bean");
        System.out.println("       public Service service() { return new Service(); }");
        System.out.println("   }");
        System.out.println("   Pros: Type-safe, refactorable, IDE support");
        System.out.println("   Cons: Requires recompilation");
        System.out.println();

        System.out.println("3. Annotation-based (Most Common):");
        System.out.println("   @Service");
        System.out.println("   public class MyService { ... }");
        System.out.println("   Pros: Concise, co-located with code");
        System.out.println("   Cons: Configuration scattered, requires scanning");
        System.out.println();

        System.out.println("Best Practice:");
        System.out.println("✓ Annotations for business components (@Service, @Repository)");
        System.out.println("✓ Java config for infrastructure beans (DataSource, etc.)");
        System.out.println("✓ XML only for legacy integration");
        System.out.println();
    }
}
