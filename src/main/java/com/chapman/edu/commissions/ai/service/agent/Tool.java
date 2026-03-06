package com.chapman.edu.commissions.ai.service.agent;

/**
 * ============================================================
 * REACT AGENT: Tool Definition
 * ============================================================
 *
 * CONCEPT: Tools as Functions Available to the AI Agent
 * ------------------------------------------------------------
 * In a ReAct agent, "tools" are functions the AI can choose to
 * invoke during its reasoning process. Each tool has:
 *
 * - name:        Unique identifier the AI uses to call the tool
 * - description: Natural language description so the AI knows WHEN to use it
 * - executor:    The Java function that actually runs when invoked
 *
 * ANALOGY:
 * Think of tools as the agent's "hands." The AI brain (LLM) decides
 * WHAT to do, and tools execute the physical action (query a database,
 * call an API, perform a calculation).
 *
 * DESIGN PRINCIPLE:
 * Tools should be:
 * - ATOMIC: Do one thing well (single responsibility)
 * - DESCRIPTIVE: The AI must understand what the tool does from its description
 * - SAFE: Tools should not have destructive side effects
 * - DETERMINISTIC: Same input should produce same output (when possible)
 */
public class Tool {

    private final String name;
    private final String description;
    private final ToolExecutor executor;

    public Tool(String name, String description, ToolExecutor executor) {
        this.name = name;
        this.description = description;
        this.executor = executor;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String execute(String input) {
        return executor.execute(input);
    }

    /**
     * Functional interface for tool execution.
     *
     * Each tool takes a String input (the argument from the AI)
     * and returns a String result (the observation for the AI).
     *
     * WHY STRING IN/OUT?
     * The AI communicates in natural language. By using strings,
     * the tool interface stays simple and the AI can pass free-form
     * arguments. The tool implementation parses as needed.
     */
    @FunctionalInterface
    public interface ToolExecutor {
        String execute(String input);
    }
}
