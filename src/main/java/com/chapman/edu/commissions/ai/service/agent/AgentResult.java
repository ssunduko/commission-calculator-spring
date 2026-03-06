package com.chapman.edu.commissions.ai.service.agent;

import java.util.List;

/**
 * ============================================================
 * REACT AGENT: Agent Result
 * ============================================================
 *
 * CONCEPT: The Final Output of a ReAct Agent Execution
 * ------------------------------------------------------------
 * After the agent finishes its Thought-Action-Observation loop,
 * it produces a final result containing:
 *
 * - finalAnswer:  The synthesized answer to the user's question
 * - steps:        The full reasoning chain (for transparency)
 * - totalSteps:   How many iterations the agent took
 * - success:      Whether the agent reached a conclusion
 *
 * TRANSPARENCY IN AI:
 * Unlike a simple chatbot that returns just an answer, a ReAct
 * agent exposes its entire reasoning process. This is critical
 * for financial applications where:
 * - Auditors need to verify HOW a conclusion was reached
 * - Users need to trust that the AI examined the right data
 * - Developers need to debug incorrect or incomplete answers
 */
public class AgentResult {

    private final String originalQuestion;
    private final String finalAnswer;
    private final List<AgentStep> steps;
    private final boolean success;

    public AgentResult(String originalQuestion, String finalAnswer,
                       List<AgentStep> steps, boolean success) {
        this.originalQuestion = originalQuestion;
        this.finalAnswer = finalAnswer;
        this.steps = steps;
        this.success = success;
    }

    public String getOriginalQuestion() {
        return originalQuestion;
    }

    public String getFinalAnswer() {
        return finalAnswer;
    }

    public List<AgentStep> getSteps() {
        return steps;
    }

    public int getTotalSteps() {
        return steps.size();
    }

    public boolean isSuccess() {
        return success;
    }
}
