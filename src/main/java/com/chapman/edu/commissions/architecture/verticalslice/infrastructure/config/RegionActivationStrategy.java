package com.chapman.edu.commissions.architecture.verticalslice.infrastructure.config;

import org.togglz.core.activation.Parameter;
import org.togglz.core.activation.ParameterBuilder;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.user.FeatureUser;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Custom Togglz ActivationStrategy that enables features based on server region.
 *
 * Region is resolved in order:
 *   1. X-Region HTTP header (per-request override)
 *   2. APP_REGION environment variable (server-level default)
 *
 * Configure via /togglz-console:
 *   Strategy: region
 *   Parameter "regions": comma-separated list of enabled regions (e.g. "us-east,eu-west")
 *
 * Example use cases:
 *   - Roll out currency conversion only in EU regions first
 *   - Enable beta features only in staging environment (APP_REGION=staging)
 *   - Test features per-request by passing X-Region header
 */
@Component
public class RegionActivationStrategy implements ActivationStrategy {

    public static final String ID = "region";
    public static final String PARAM_REGIONS = "regions";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "Server/Region Strategy";
    }

    @Override
    public boolean isActive(FeatureState featureState, FeatureUser user) {
        String enabledRegions = featureState.getParameter(PARAM_REGIONS);
        if (enabledRegions == null || enabledRegions.isBlank()) {
            return false;
        }
        String currentRegion = resolveCurrentRegion();
        if (currentRegion == null || currentRegion.isBlank()) {
            return false;
        }
        for (String region : enabledRegions.split(",")) {
            if (region.trim().equalsIgnoreCase(currentRegion.trim())) {
                return true;
            }
        }
        return false;
    }
    private String resolveCurrentRegion() {
        // 1. Check X-Region HTTP header (per-request)
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                String headerRegion = attrs.getRequest().getHeader("X-Region");
                if (headerRegion != null && !headerRegion.isBlank()) {
                    return headerRegion;
                }
            }
        } catch (Exception ignored) {
            // Not in a web request context — fall through to env var
        }

        // 2. Fall back to APP_REGION environment variable (server-level)
        return System.getenv("APP_REGION");
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[]{
                ParameterBuilder.create(PARAM_REGIONS)
                        .label("Enabled Regions")
                        .description("Comma-separated list of regions where this feature is active (e.g. us-east,eu-west,staging)")
        };
    }

}
