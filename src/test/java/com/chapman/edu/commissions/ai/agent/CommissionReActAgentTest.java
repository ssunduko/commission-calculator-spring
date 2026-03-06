package com.chapman.edu.commissions.ai.agent;

import com.chapman.edu.commissions.ai.service.agent.AgentResult;
import com.chapman.edu.commissions.ai.service.agent.CommissionReActAgent;
import com.chapman.edu.commissions.ai.service.agent.Tool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommissionReActAgent — Unit Tests")
class CommissionReActAgentTest {

    @Mock(answer = org.mockito.Answers.RETURNS_DEEP_STUBS)
    private ChatClient chatClient;

    private CommissionReActAgent agent;

    @BeforeEach
    void setUp() {
        agent = new CommissionReActAgent(chatClient);
    }

    @Nested
    @DisplayName("Tool Registration")
    class ToolRegistration {

        @Test
        @DisplayName("should register and retrieve tools")
        void shouldRegisterTools() {
            agent.registerTool(new Tool("test_tool", "A test tool", input -> "result"));

            assertThat(agent.getTools()).hasSize(1);
            assertThat(agent.getTools()).containsKey("test_tool");
        }

        @Test
        @DisplayName("should register multiple tools")
        void shouldRegisterMultipleTools() {
            agent.registerTool(new Tool("tool_a", "Tool A", input -> "a"));
            agent.registerTool(new Tool("tool_b", "Tool B", input -> "b"));

            assertThat(agent.getTools()).hasSize(2);
        }

        @Test
        @DisplayName("should return immutable copy of tools map")
        void shouldReturnImmutableCopy() {
            agent.registerTool(new Tool("test", "Test", input -> ""));

            var tools = agent.getTools();
            assertThat(tools).hasSize(1);

            // Original agent still works after getting tools
            agent.registerTool(new Tool("test2", "Test2", input -> ""));
            assertThat(agent.getTools()).hasSize(2);
            // The previously returned copy should not have changed
            assertThat(tools).hasSize(1);
        }
    }

    @Nested
    @DisplayName("ReAct Execution — Final Answer")
    class FinalAnswer {

        @Test
        @DisplayName("should return final answer when AI responds immediately")
        void shouldReturnFinalAnswerDirectly() {
            when(chatClient.prompt().system(anyString()).user(anyString()).call().content())
                    .thenReturn("Thought: I can answer this from general knowledge.\nFinal Answer: Commission is a percentage-based payment.");

            AgentResult result = agent.execute("What is a commission?");

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getFinalAnswer()).contains("Commission is a percentage-based payment");
            assertThat(result.getTotalSteps()).isZero();
        }

        @Test
        @DisplayName("should handle AI returning unstructured response as answer")
        void shouldTreatUnstructuredResponseAsAnswer() {
            when(chatClient.prompt().system(anyString()).user(anyString()).call().content())
                    .thenReturn("The answer is 42.");

            AgentResult result = agent.execute("What is the answer?");

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getFinalAnswer()).isEqualTo("The answer is 42.");
        }
    }

    @Nested
    @DisplayName("ReAct Execution — Tool Usage")
    class ToolUsage {

        @Test
        @DisplayName("should execute tool and reach final answer")
        void shouldExecuteToolAndAnswer() {
            agent.registerTool(new Tool("lookup_plan", "Look up plans", input -> "Standard Plan: 5%-15%"));

            // Step 1: AI wants to use a tool
            // Step 2: AI gives final answer after seeing observation
            when(chatClient.prompt().system(anyString()).user(anyString()).call().content())
                    .thenReturn("Thought: I need to look up the active plans.\nAction: lookup_plan[active]")
                    .thenReturn("Thought: I now have the plan information.\nFinal Answer: The Standard Plan has rates from 5% to 15%.");

            AgentResult result = agent.execute("What plans are active?");

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getFinalAnswer()).contains("Standard Plan");
            assertThat(result.getTotalSteps()).isEqualTo(1);
            assertThat(result.getSteps().get(0).getAction()).isEqualTo("lookup_plan");
            assertThat(result.getSteps().get(0).getObservation()).contains("Standard Plan: 5%-15%");
        }

        @Test
        @DisplayName("should handle multi-step tool execution")
        void shouldHandleMultiStepExecution() {
            agent.registerTool(new Tool("lookup_user", "Look up user", input -> "User: Alice | ID: abc-123"));
            agent.registerTool(new Tool("lookup_calculations", "Look up calcs", input -> "Total: $19,800"));

            when(chatClient.prompt().system(anyString()).user(anyString()).call().content())
                    .thenReturn("Thought: I need to find Alice's ID first.\nAction: lookup_user[Alice]")
                    .thenReturn("Thought: Now I need her calculations.\nAction: lookup_calculations[abc-123]")
                    .thenReturn("Thought: I have all the data.\nFinal Answer: Alice earned $19,800 in commissions.");

            AgentResult result = agent.execute("How much did Alice earn?");

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getTotalSteps()).isEqualTo(2);
            assertThat(result.getSteps().get(0).getAction()).isEqualTo("lookup_user");
            assertThat(result.getSteps().get(1).getAction()).isEqualTo("lookup_calculations");
        }

        @Test
        @DisplayName("should handle unknown tool gracefully")
        void shouldHandleUnknownTool() {
            agent.registerTool(new Tool("lookup_user", "Look up user", input -> "User: Alice"));

            when(chatClient.prompt().system(anyString()).user(anyString()).call().content())
                    .thenReturn("Thought: Let me search.\nAction: nonexistent_tool[test]")
                    .thenReturn("Thought: That tool doesn't exist, let me try another.\nAction: lookup_user[Alice]")
                    .thenReturn("Thought: Got it.\nFinal Answer: Alice is a sales rep.");

            AgentResult result = agent.execute("Who is Alice?");

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getSteps().get(0).getObservation()).contains("Unknown tool");
            assertThat(result.getSteps().get(1).getAction()).isEqualTo("lookup_user");
        }

        @Test
        @DisplayName("should handle tool execution error gracefully")
        void shouldHandleToolError() {
            agent.registerTool(new Tool("failing_tool", "Always fails",
                    input -> { throw new RuntimeException("DB connection lost"); }));

            when(chatClient.prompt().system(anyString()).user(anyString()).call().content())
                    .thenReturn("Thought: Let me try.\nAction: failing_tool[test]")
                    .thenReturn("Thought: The tool errored, I'll report that.\nFinal Answer: Unable to retrieve data due to a system error.");

            AgentResult result = agent.execute("Get some data");

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getSteps().get(0).getObservation()).contains("Error executing tool");
        }
    }

    @Nested
    @DisplayName("ReAct Execution — Error Handling")
    class ErrorHandling {

        @Test
        @DisplayName("should handle AI returning null response")
        void shouldHandleNullAiResponse() {
            when(chatClient.prompt().system(anyString()).user(anyString()).call().content())
                    .thenReturn(null);

            AgentResult result = agent.execute("What is happening?");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getFinalAnswer()).contains("empty response");
        }

        @Test
        @DisplayName("should handle AI exception")
        void shouldHandleAiException() {
            when(chatClient.prompt().system(anyString()).user(anyString()).call().content())
                    .thenThrow(new RuntimeException("API rate limit exceeded"));

            AgentResult result = agent.execute("Test question");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getFinalAnswer()).contains("error");
        }

        @Test
        @DisplayName("should stop at max steps and provide best-effort answer")
        void shouldStopAtMaxSteps() {
            agent.registerTool(new Tool("loop_tool", "A tool", input -> "Some data"));

            // AI keeps requesting the same tool without reaching a final answer
            when(chatClient.prompt().system(anyString()).user(anyString()).call().content())
                    .thenReturn("Thought: Need more data.\nAction: loop_tool[query]");

            AgentResult result = agent.execute("Infinite loop question");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getTotalSteps()).isEqualTo(7); // MAX_STEPS
            assertThat(result.getFinalAnswer()).contains("unable to fully answer");
        }
    }

    @Nested
    @DisplayName("Tool Execution")
    class ToolExecution {

        @Test
        @DisplayName("should pass correct input to tool")
        void shouldPassCorrectInput() {
            final String[] capturedInput = {null};
            agent.registerTool(new Tool("capture_tool", "Captures input",
                    input -> { capturedInput[0] = input; return "OK"; }));

            when(chatClient.prompt().system(anyString()).user(anyString()).call().content())
                    .thenReturn("Thought: Testing.\nAction: capture_tool[hello world]")
                    .thenReturn("Thought: Done.\nFinal Answer: Done.");

            agent.execute("Test");

            assertThat(capturedInput[0]).isEqualTo("hello world");
        }
    }
}
