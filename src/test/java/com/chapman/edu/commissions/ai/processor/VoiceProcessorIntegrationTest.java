package com.chapman.edu.commissions.ai.processor;

import com.chapman.edu.commissions.ai.service.ml.AnomalyDetectionService;
import com.chapman.edu.commissions.ai.service.ml.CommissionExplainerService;
import com.chapman.edu.commissions.ai.service.ml.ForecastingService;
import com.chapman.edu.commissions.ai.service.moderation.ModerationService;
import com.chapman.edu.commissions.ai.service.rag.CommissionRagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Integration-style tests for VoiceProcessor with mocked services.
 * Tests the full voice pipeline: validation, routing, and persona configuration.
 */
class VoiceProcessorIntegrationTest {

    private VoiceProcessor voiceProcessor;
    private ModerationService moderationService;

    @BeforeEach
    void setUp() {
        moderationService = mock(ModerationService.class);
        voiceProcessor = new VoiceProcessor(
                mock(CommissionRagService.class),
                mock(CommissionExplainerService.class),
                mock(ForecastingService.class),
                mock(AnomalyDetectionService.class),
                moderationService
        );
    }

    @Test
    @DisplayName("Voice command routing demo returns all expected keys")
    void voiceRoutingDemoStructure() {
        Map<String, Object> result = voiceProcessor.demonstrateVoiceCommandRouting();
        assertThat(result).containsKeys("concept", "command_routing", "parameter_extraction");
    }

    @Test
    @DisplayName("Voice personas include all expected providers")
    void personasIncludeAllProviders() {
        Map<String, Object> result = voiceProcessor.demonstrateVoicePersonas();

        assertThat(result).containsKeys("openai_voices", "aws_polly_neural",
                "aws_polly_generative", "browser_fallback", "architecture");

        @SuppressWarnings("unchecked")
        List<String> openai = (List<String>) result.get("openai_voices");
        assertThat(openai).hasSizeGreaterThanOrEqualTo(10);
        assertThat(openai).anyMatch(v -> v.contains("nova"));

        @SuppressWarnings("unchecked")
        List<String> polly = (List<String>) result.get("aws_polly_neural");
        assertThat(polly).anyMatch(v -> v.contains("Joanna"));
    }

    @Test
    @DisplayName("Architecture describes both listening modes")
    void architectureDescribesListeningModes() {
        Map<String, Object> result = voiceProcessor.demonstrateVoicePersonas();

        @SuppressWarnings("unchecked")
        Map<String, String> arch = (Map<String, String>) result.get("architecture");
        assertThat(arch.get("listening_modes")).contains("On/Off").contains("Always-on");
        assertThat(arch.get("trigger_word")).contains("execute");
        assertThat(arch.get("stop_word")).contains("stop");
    }

    @Test
    @DisplayName("Full routing demo covers all 6 command types")
    void routingDemoCoversAllCommands() {
        Map<String, Object> result = voiceProcessor.demonstrateVoiceCommandRouting();

        @SuppressWarnings("unchecked")
        Map<String, String> routing = (Map<String, String>) result.get("command_routing");
        assertThat(routing).hasSize(6);

        // Verify each command routes to a different endpoint
        assertThat(routing.values()).extracting(String::toString)
                .anyMatch(v -> v.contains("rag/ask"))
                .anyMatch(v -> v.contains("report"))
                .anyMatch(v -> v.contains("explain"))
                .anyMatch(v -> v.contains("forecast/user"))
                .anyMatch(v -> v.contains("anomaly"))
                .anyMatch(v -> v.contains("forecast/team"));
    }
}
