package com.chapman.edu.commissions.ai.processor;

import com.chapman.edu.commissions.ai.service.ml.AnomalyDetectionService;
import com.chapman.edu.commissions.ai.service.ml.CommissionExplainerService;
import com.chapman.edu.commissions.ai.service.ml.ForecastingService;
import com.chapman.edu.commissions.ai.service.moderation.ModerationService;
import com.chapman.edu.commissions.ai.service.rag.CommissionRagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for VoiceProcessor — tests command routing, parameter extraction,
 * and voice input validation without requiring Spring context or AI APIs.
 */
class VoiceProcessorTest {

    private VoiceProcessor voiceProcessor;

    @BeforeEach
    void setUp() {
        voiceProcessor = new VoiceProcessor(
                mock(CommissionRagService.class),
                mock(CommissionExplainerService.class),
                mock(ForecastingService.class),
                mock(AnomalyDetectionService.class),
                mock(ModerationService.class)
        );
    }

    @Nested
    @DisplayName("Command Classification")
    class CommandClassification {

        @Test
        @DisplayName("Routes question commands to RAG ask endpoint")
        void routesQuestionToAsk() {
            String result = voiceProcessor.classifyCommand("What commission plans are available");
            assertThat(result).contains("ask").contains("/api/ai/rag/ask");
        }

        @Test
        @DisplayName("Routes anomaly commands to anomaly endpoint")
        void routesAnomalyDetection() {
            assertThat(voiceProcessor.classifyCommand("Detect anomalies")).contains("anomaly");
            assertThat(voiceProcessor.classifyCommand("Scan all calculations")).contains("anomaly");
        }

        @Test
        @DisplayName("Routes team forecast commands")
        void routesTeamForecast() {
            assertThat(voiceProcessor.classifyCommand("Team forecast")).contains("team");
            assertThat(voiceProcessor.classifyCommand("Show team commission forecast")).contains("team");
        }

        @Test
        @DisplayName("Routes individual forecast commands")
        void routesForecast() {
            assertThat(voiceProcessor.classifyCommand("Forecast for user one")).contains("forecast");
            assertThat(voiceProcessor.classifyCommand("Predict commissions for user 5")).contains("forecast");
        }

        @Test
        @DisplayName("Routes explain commands")
        void routesExplain() {
            assertThat(voiceProcessor.classifyCommand("Explain calculation one")).contains("explain");
            assertThat(voiceProcessor.classifyCommand("Show me calculation 3 details")).contains("explain");
        }

        @Test
        @DisplayName("Routes report commands")
        void routesReport() {
            assertThat(voiceProcessor.classifyCommand("Report for Alice Johnson")).contains("report");
            assertThat(voiceProcessor.classifyCommand("Performance review for Bob")).contains("report");
        }

        @Test
        @DisplayName("Defaults to ask for unrecognized commands")
        void defaultsToAsk() {
            assertThat(voiceProcessor.classifyCommand("Hello there")).contains("ask");
            assertThat(voiceProcessor.classifyCommand("random stuff")).contains("ask");
        }
    }

    @Nested
    @DisplayName("ID Extraction")
    class IdExtraction {

        @Test
        @DisplayName("Extracts numeric IDs from text")
        void extractsNumericId() {
            assertThat(voiceProcessor.extractId("calculation 42")).isEqualTo("42");
            assertThat(voiceProcessor.extractId("user 1")).isEqualTo("1");
            assertThat(voiceProcessor.extractId("check 123")).isEqualTo("123");
        }

        @Test
        @DisplayName("Converts number words to digits")
        void convertsNumberWords() {
            assertThat(voiceProcessor.extractId("calculation one")).isEqualTo("1");
            assertThat(voiceProcessor.extractId("user two")).isEqualTo("2");
            assertThat(voiceProcessor.extractId("forecast for user five")).isEqualTo("5");
            assertThat(voiceProcessor.extractId("explain calculation ten")).isEqualTo("10");
        }

        @Test
        @DisplayName("Returns null when no ID found")
        void returnsNullForNoId() {
            assertThat(voiceProcessor.extractId("detect anomalies")).isNull();
            assertThat(voiceProcessor.extractId("team forecast")).isNull();
        }

        @Test
        @DisplayName("Prefers numeric digits over words")
        void prefersDigitsOverWords() {
            assertThat(voiceProcessor.extractId("item 7 from section one")).isEqualTo("7");
        }
    }

    @Nested
    @DisplayName("Name Extraction")
    class NameExtraction {

        @Test
        @DisplayName("Extracts name after 'for'")
        void extractsNameAfterFor() {
            assertThat(voiceProcessor.extractName("report for Alice Johnson")).isEqualTo("Alice Johnson");
            assertThat(voiceProcessor.extractName("forecast for Bob Smith")).isEqualTo("Bob Smith");
        }

        @Test
        @DisplayName("Extracts name after 'about'")
        void extractsNameAfterAbout() {
            assertThat(voiceProcessor.extractName("tell me about Jane Doe")).isEqualTo("Jane Doe");
        }

        @Test
        @DisplayName("Strips trailing punctuation from names")
        void stripsTrailingPunctuation() {
            assertThat(voiceProcessor.extractName("report for Alice.")).isEqualTo("Alice");
            assertThat(voiceProcessor.extractName("forecast for Bob!")).isEqualTo("Bob");
        }

        @Test
        @DisplayName("Returns null when no name pattern found")
        void returnsNullForNoName() {
            assertThat(voiceProcessor.extractName("detect anomalies")).isNull();
            assertThat(voiceProcessor.extractName("team forecast")).isNull();
        }
    }

    @Nested
    @DisplayName("Voice Command Routing Demo")
    class RoutingDemo {

        @Test
        @DisplayName("Returns routing table with all sample commands")
        void returnsRoutingTable() {
            Map<String, Object> result = voiceProcessor.demonstrateVoiceCommandRouting();
            assertThat(result).containsKey("command_routing");
            assertThat(result).containsKey("parameter_extraction");

            @SuppressWarnings("unchecked")
            Map<String, String> routing = (Map<String, String>) result.get("command_routing");
            assertThat(routing).hasSize(6);
            assertThat(routing).containsKey("What commission plans are available");
            assertThat(routing).containsKey("Detect anomalies");
        }
    }

    @Nested
    @DisplayName("Voice Personas Demo")
    class PersonasDemo {

        @Test
        @DisplayName("Returns all voice providers and architecture info")
        void returnsPersonas() {
            Map<String, Object> result = voiceProcessor.demonstrateVoicePersonas();
            assertThat(result).containsKey("openai_voices");
            assertThat(result).containsKey("aws_polly_neural");
            assertThat(result).containsKey("aws_polly_generative");
            assertThat(result).containsKey("architecture");

            @SuppressWarnings("unchecked")
            List<String> openai = (List<String>) result.get("openai_voices");
            assertThat(openai).contains("nova (warm female)");

            @SuppressWarnings("unchecked")
            Map<String, String> arch = (Map<String, String>) result.get("architecture");
            assertThat(arch).containsEntry("trigger_word", "\"execute\" — ends command and dispatches");
            assertThat(arch).containsEntry("stop_word", "\"stop\" — silences narrator immediately");
        }
    }
}
