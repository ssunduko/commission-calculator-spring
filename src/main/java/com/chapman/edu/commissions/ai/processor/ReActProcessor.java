package com.chapman.edu.commissions.ai.processor;

import com.chapman.edu.commissions.ai.service.agent.AgentResult;
import com.chapman.edu.commissions.ai.service.agent.AgentStep;
import com.chapman.edu.commissions.ai.service.agent.CommissionReActAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ============================================================
 * PROCESSOR: ReActProcessor
 * ============================================================
 *
 * CONCEPT: ReAct (Reasoning + Acting) Agent
 * ------------------------------------------------------------
 * This processor demonstrates the ReAct agent pattern, where an
 * AI agent reasons step-by-step and uses tools to gather data
 * before producing a final answer.
 *
 * ReAct vs. OTHER AI PATTERNS:
 *
 * 1. SIMPLE PROMPT (AiProcessor):
 *    User → AI → Answer
 *    Fast, but answer comes from training data only.
 *
 * 2. RAG (RagProcessor):
 *    User → Retrieve docs → AI + Context → Answer
 *    Grounded in data, but single-step retrieval.
 *
 * 3. ReAct AGENT (this processor):
 *    User → Think → Act → Observe → Think → Act → ... → Answer
 *    Multi-step reasoning with dynamic tool selection.
 *    Can combine multiple data sources and calculations.
 *
 * WHEN TO USE EACH:
 * - Simple questions → Simple Prompt ("What is a commission?")
 * - Data lookups → RAG ("What plans are available?")
 * - Complex analysis → ReAct ("Compare Alice's performance to the team average")
 *
 * THE ReAct LOOP:
 * Each iteration produces a Thought-Action-Observation triple:
 *
 *   Thought: "I need to find Alice's user ID first"
 *   Action: lookup_user[Alice]
 *   Observation: "User: Alice Johnson | ID: abc-123 | ..."
 *
 *   Thought: "Now I need her commission calculations"
 *   Action: lookup_calculations[abc-123]
 *   Observation: "Found 2 calculations: ..."
 *
 *   Thought: "I have enough data to answer"
 *   Final Answer: "Alice earned $22,880 in total commissions..."
 */
@Service
public class ReActProcessor {

    private static final Logger log = LoggerFactory.getLogger(ReActProcessor.class);

    private final CommissionReActAgent agent;

    public ReActProcessor(CommissionReActAgent agent) {
        this.agent = agent;
    }

    /**
     * Demonstrates the ReAct agent with a multi-step commission question.
     *
     * @param question The complex question requiring multi-step reasoning
     * @return Map containing the agent's answer and reasoning chain
     */
    public Map<String, Object> demonstrateReActAgent(String question) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("question", question);

        AgentResult agentResult = agent.execute(question);

        result.put("success", agentResult.isSuccess());
        result.put("final_answer", agentResult.getFinalAnswer());
        result.put("total_steps", agentResult.getTotalSteps());

        // Include the reasoning chain
        Map<String, Object> reasoningChain = new LinkedHashMap<>();
        for (AgentStep step : agentResult.getSteps()) {
            reasoningChain.put("step_" + step.getStepNumber(), Map.of(
                    "thought", step.getThought(),
                    "action", step.getAction() + "[" + step.getActionInput() + "]",
                    "observation", step.getObservation()
            ));
        }
        result.put("reasoning_chain", reasoningChain);

        return result;
    }

    /**
     * Returns information about the agent's registered tools.
     */
    public Map<String, String> getAvailableTools() {
        Map<String, String> toolInfo = new LinkedHashMap<>();
        agent.getTools().forEach((name, tool) ->
                toolInfo.put(name, tool.getDescription()));
        return toolInfo;
    }
}
