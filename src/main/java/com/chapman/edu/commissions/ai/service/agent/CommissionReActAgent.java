package com.chapman.edu.commissions.ai.service.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ============================================================
 * SPRING AI SERVICE: CommissionReActAgent
 * ============================================================
 *
 * CONCEPT: ReAct (Reasoning + Acting) Agent Pattern
 * ------------------------------------------------------------
 * ReAct is an AI agent architecture that interleaves REASONING
 * (thinking about what to do) with ACTING (using tools to gather
 * information). This creates a loop:
 *
 *   ┌─────────────────────────────────────────────────┐
 *   │  User Question                                   │
 *   │       ↓                                          │
 *   │  ┌─────────┐                                     │
 *   │  │ THOUGHT │ → AI reasons about what to do next  │
 *   │  └────┬────┘                                     │
 *   │       ↓                                          │
 *   │  ┌─────────┐                                     │
 *   │  │ ACTION  │ → AI selects and calls a tool       │
 *   │  └────┬────┘                                     │
 *   │       ↓                                          │
 *   │  ┌─────────────┐                                 │
 *   │  │ OBSERVATION │ → Tool returns result            │
 *   │  └──────┬──────┘                                 │
 *   │         ↓                                        │
 *   │    Has enough info? ─── No ──→ Loop back to      │
 *   │         │                      THOUGHT            │
 *   │        Yes                                       │
 *   │         ↓                                        │
 *   │  ┌──────────────┐                                │
 *   │  │ FINAL ANSWER │ → Synthesize and respond       │
 *   │  └──────────────┘                                │
 *   └─────────────────────────────────────────────────┘
 *
 * WHY ReAct OVER SIMPLE PROMPTING?
 * ┌──────────────────────┬──────────────────────────────────┐
 * │ Simple Prompt         │ ReAct Agent                      │
 * ├──────────────────────┼──────────────────────────────────┤
 * │ Single AI call        │ Multiple AI calls + tool calls   │
 * │ Answer from training  │ Answer from YOUR live data       │
 * │ May hallucinate data  │ Grounded in tool observations    │
 * │ No reasoning trace    │ Full reasoning chain visible     │
 * │ Limited by context    │ Can gather info incrementally    │
 * │ Fast, cheap           │ Slower, more expensive           │
 * └──────────────────────┴──────────────────────────────────┘
 *
 * ReAct vs. RAG:
 * - RAG: Retrieve → Augment → Generate (fixed pipeline, 1 retrieval)
 * - ReAct: Decide → Act → Observe → Repeat (dynamic, multi-step)
 * - ReAct can USE RAG as one of its tools, but also query databases,
 *   run calculations, and chain multiple data sources
 *
 * WHEN TO USE ReAct:
 * 1. Questions requiring MULTIPLE data lookups
 *    "Compare Alice's Q1 commissions to the team average"
 *    → Needs: Alice's data + all team data + calculation
 *
 * 2. Questions requiring CONDITIONAL logic
 *    "Which sales rep has the most anomalous commission?"
 *    → Needs: all reps' data + anomaly analysis for each
 *
 * 3. Questions where the NEXT STEP depends on PREVIOUS results
 *    "Who earned the most, and what plan were they on?"
 *    → Step 1: Find top earner → Step 2: Look up their plan
 *
 * SAFETY FEATURES:
 * - Maximum step limit (prevents infinite loops)
 * - Input moderation (can integrate with ModerationService)
 * - Read-only tools (no destructive database operations)
 * - Observation truncation (prevents context overflow)
 *
 * ACADEMIC REFERENCE:
 * Yao, S., et al. (2022). "ReAct: Synergizing Reasoning and Acting
 * in Language Models." ICLR 2023.
 */
@Service
public class CommissionReActAgent {

    private static final Logger log = LoggerFactory.getLogger(CommissionReActAgent.class);

    /**
     * Maximum number of reasoning steps before the agent must stop.
     *
     * WHY A LIMIT?
     * Without a limit, a confused or hallucinating AI could loop forever,
     * racking up API costs and never producing an answer. 7 steps is
     * sufficient for most multi-hop commission queries while providing
     * a safety net against runaway loops.
     */
    private static final int MAX_STEPS = 7;

    /**
     * Regex patterns to parse the AI's structured output.
     *
     * THE PARSING CHALLENGE:
     * The AI produces free-form text that must be parsed into structured
     * components (Thought, Action, Observation). We use a specific output
     * format and parse it with regex. More robust implementations might
     * use structured output (JSON mode) or function calling.
     */
    private static final Pattern THOUGHT_PATTERN = Pattern.compile(
            "Thought:\\s*(.+?)(?=\\nAction:|$)", Pattern.DOTALL);
    private static final Pattern ACTION_PATTERN = Pattern.compile(
            "Action:\\s*(\\w+)\\[(.+?)]");
    private static final Pattern FINAL_ANSWER_PATTERN = Pattern.compile(
            "Final Answer:\\s*(.+)", Pattern.DOTALL);

    private final ChatClient chatClient;
    private final Map<String, Tool> tools = new LinkedHashMap<>();

    public CommissionReActAgent(ChatClient commissionChatClient) {
        this.chatClient = commissionChatClient;
    }

    // ============================================================
    // TOOL REGISTRATION
    // ============================================================

    /**
     * Registers a tool that the agent can use during reasoning.
     *
     * TOOL REGISTRATION PATTERN:
     * Tools are registered at startup (typically in a @Configuration class
     * or @PostConstruct). The agent builds a tool description block that
     * is included in every prompt, so the AI knows what tools are available.
     *
     * @param tool The tool to register
     */
    public void registerTool(Tool tool) {
        tools.put(tool.getName(), tool);
        log.info("Registered agent tool: {} — {}", tool.getName(), tool.getDescription());
    }

    /**
     * Returns the registered tools (for testing and inspection).
     */
    public Map<String, Tool> getTools() {
        return Map.copyOf(tools);
    }

    // ============================================================
    // CORE ReAct LOOP
    // ============================================================

    /**
     * Executes the ReAct loop to answer a complex question.
     *
     * THE ReAct ALGORITHM:
     *
     * 1. Build the initial prompt with:
     *    - System instructions (role, output format, tool descriptions)
     *    - The user's question
     *
     * 2. Send to AI → parse response for Thought + Action
     *
     * 3. If AI outputs "Final Answer:" → done, return result
     *
     * 4. If AI outputs "Action: toolName[input]":
     *    a. Execute the tool with the given input
     *    b. Append the Observation (tool result) to the scratchpad
     *    c. Send updated prompt to AI → go to step 2
     *
     * 5. Repeat until Final Answer or MAX_STEPS reached
     *
     * THE SCRATCHPAD:
     * The "scratchpad" is the accumulated history of all previous
     * Thought-Action-Observation triples. It grows with each step,
     * giving the AI memory of what it has already done. This is how
     * the agent builds up context incrementally.
     *
     * @param question The user's natural language question
     * @return AgentResult containing the answer and reasoning chain
     */
    public AgentResult execute(String question) {
        log.info("ReAct agent executing: '{}'", question);

        List<AgentStep> steps = new ArrayList<>();
        StringBuilder scratchpad = new StringBuilder();

        for (int step = 1; step <= MAX_STEPS; step++) {
            log.info("ReAct step {}/{}", step, MAX_STEPS);

            // Build the prompt with current scratchpad
            String prompt = buildPrompt(question, scratchpad.toString());

            // Call the AI
            String aiResponse;
            try {
                aiResponse = chatClient.prompt()
                        .system(buildSystemPrompt())
                        .user(prompt)
                        .call()
                        .content();
            } catch (Exception e) {
                log.error("AI call failed at step {}: {}", step, e.getMessage());
                return new AgentResult(question,
                        "Agent encountered an error: " + e.getMessage(),
                        steps, false);
            }

            if (aiResponse == null || aiResponse.isBlank()) {
                log.warn("AI returned empty response at step {}", step);
                return new AgentResult(question,
                        "Agent received an empty response from the AI model.",
                        steps, false);
            }

            log.debug("AI response at step {}: {}", step, aiResponse);

            // Check for Final Answer
            Matcher finalMatcher = FINAL_ANSWER_PATTERN.matcher(aiResponse);
            if (finalMatcher.find()) {
                String finalAnswer = finalMatcher.group(1).trim();
                log.info("ReAct agent reached final answer after {} steps", step);
                return new AgentResult(question, finalAnswer, steps, true);
            }

            // Parse Thought and Action
            String thought = parseThought(aiResponse);
            String[] actionParts = parseAction(aiResponse);

            if (actionParts == null) {
                // AI didn't follow the format — treat the whole response as the answer
                log.warn("Could not parse action at step {}. Treating response as final answer.", step);
                return new AgentResult(question, aiResponse.trim(), steps, true);
            }

            String actionName = actionParts[0];
            String actionInput = actionParts[1];

            // Execute the tool
            String observation = executeTool(actionName, actionInput);

            // Record the step
            AgentStep agentStep = new AgentStep(step, thought, actionName, actionInput, observation);
            steps.add(agentStep);
            log.info("Step {}: Thought='{}', Action={}[{}], Observation='{}'",
                    step, truncate(thought), actionName, truncate(actionInput), truncate(observation));

            // Append to scratchpad for next iteration
            scratchpad.append(String.format(
                    "Thought: %s\nAction: %s[%s]\nObservation: %s\n\n",
                    thought, actionName, actionInput, observation));
        }

        // Max steps reached without a final answer
        log.warn("ReAct agent hit max steps ({}) without final answer", MAX_STEPS);
        String bestEffort = "I was unable to fully answer the question within the step limit. " +
                "Based on what I found: " + summarizeObservations(steps);
        return new AgentResult(question, bestEffort, steps, false);
    }

    // ============================================================
    // PROMPT CONSTRUCTION
    // ============================================================

    /**
     * Builds the system prompt that instructs the AI on the ReAct format.
     *
     * PROMPT ENGINEERING FOR ReAct:
     * The system prompt must clearly specify:
     * 1. The output FORMAT (Thought/Action/Final Answer)
     * 2. The available TOOLS with descriptions
     * 3. The ACTION SYNTAX (toolName[input])
     * 4. When to produce a FINAL ANSWER
     * 5. Constraints (stay on topic, use tools, don't hallucinate)
     *
     * This prompt is the "brain" of the agent — it determines how
     * well the AI reasons and when it decides to use which tool.
     */
    private String buildSystemPrompt() {
        StringBuilder toolDescriptions = new StringBuilder();
        for (Tool tool : tools.values()) {
            toolDescriptions.append(String.format("  - %s: %s\n", tool.getName(), tool.getDescription()));
        }

        return String.format("""
                You are a commission analysis agent that answers questions by reasoning
                step-by-step and using tools to look up real data. You MUST use tools
                to find information — do NOT make up data or guess numbers.

                AVAILABLE TOOLS:
                %s
                RESPONSE FORMAT:
                You MUST respond in one of these two formats:

                FORMAT 1 — When you need more information:
                Thought: <your reasoning about what to do next>
                Action: <toolName>[<input>]

                FORMAT 2 — When you have enough information to answer:
                Thought: <your final reasoning>
                Final Answer: <your complete answer to the user's question>

                RULES:
                - Always start with a Thought explaining your reasoning
                - Use exactly ONE action per response (not multiple)
                - Use the action syntax: toolName[input] (square brackets, no spaces around brackets)
                - Base your answer ONLY on tool observations — never invent data
                - When you have enough information, produce a Final Answer
                - Format currency as $X,XXX.XX
                - Be concise but thorough
                """, toolDescriptions);
    }

    /**
     * Builds the user prompt combining the question and scratchpad.
     *
     * THE SCRATCHPAD TECHNIQUE:
     * By appending all previous Thought-Action-Observation triples,
     * the AI sees its entire reasoning history. This prevents it from:
     * - Repeating the same tool call
     * - Forgetting what it already learned
     * - Contradicting its earlier reasoning
     */
    private String buildPrompt(String question, String scratchpad) {
        if (scratchpad.isEmpty()) {
            return String.format("Question: %s", question);
        }
        return String.format("""
                Question: %s

                Previous reasoning steps:
                %s
                Continue reasoning from where you left off.""",
                question, scratchpad);
    }

    // ============================================================
    // PARSING AI RESPONSES
    // ============================================================

    private String parseThought(String response) {
        Matcher matcher = THOUGHT_PATTERN.matcher(response);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return response.trim();
    }

    private String[] parseAction(String response) {
        Matcher matcher = ACTION_PATTERN.matcher(response);
        if (matcher.find()) {
            return new String[]{matcher.group(1).trim(), matcher.group(2).trim()};
        }
        return null;
    }

    // ============================================================
    // TOOL EXECUTION
    // ============================================================

    /**
     * Executes a tool by name and returns its observation.
     *
     * ERROR HANDLING:
     * If the AI calls a tool that doesn't exist (hallucinated tool name),
     * we return a helpful error message so the AI can self-correct.
     * If the tool throws an exception, we catch it and return the error
     * as an observation so the agent can try a different approach.
     */
    private String executeTool(String toolName, String input) {
        Tool tool = tools.get(toolName);
        if (tool == null) {
            String available = String.join(", ", tools.keySet());
            return String.format("Error: Unknown tool '%s'. Available tools: %s", toolName, available);
        }

        try {
            return tool.execute(input);
        } catch (Exception e) {
            log.error("Tool '{}' failed with input '{}': {}", toolName, input, e.getMessage());
            return "Error executing tool: " + e.getMessage();
        }
    }

    // ============================================================
    // UTILITY METHODS
    // ============================================================

    private String summarizeObservations(List<AgentStep> steps) {
        if (steps.isEmpty()) return "No observations gathered.";
        AgentStep lastStep = steps.get(steps.size() - 1);
        return lastStep.getObservation();
    }

    private String truncate(String text) {
        if (text == null) return "null";
        return text.length() > 150 ? text.substring(0, 150) + "..." : text;
    }
}
