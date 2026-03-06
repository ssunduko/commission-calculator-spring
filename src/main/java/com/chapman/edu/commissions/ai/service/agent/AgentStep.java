package com.chapman.edu.commissions.ai.service.agent;

/**
 * ============================================================
 * REACT AGENT: Agent Step (Thought-Action-Observation Triple)
 * ============================================================
 *
 * CONCEPT: The ReAct Loop Step
 * ------------------------------------------------------------
 * Each step in the ReAct loop produces a triple:
 *
 *   THOUGHT:      The agent's reasoning about what to do next
 *   ACTION:       The tool call the agent decides to make
 *   OBSERVATION:  The result returned by the tool
 *
 * These steps are accumulated as a "scratchpad" that gives the
 * agent memory of what it has already done within a single query.
 *
 * EXAMPLE STEP:
 *   Thought: I need to find Alice's deals to calculate her total commission.
 *   Action: lookup_deals(Alice)
 *   Observation: Found 3 deals: Acme Corp ($150,000), TechStart ($35,000)...
 *
 * WHY TRACK STEPS?
 * 1. TRANSPARENCY: Users can see the agent's reasoning chain
 * 2. DEBUGGING: If the agent gives a wrong answer, you can trace where it went wrong
 * 3. CONTEXT: Each step is fed back to the AI so it can build on prior observations
 * 4. AUDITABILITY: In financial applications, showing work is essential
 */
public class AgentStep {

    private final int stepNumber;
    private final String thought;
    private final String action;
    private final String actionInput;
    private final String observation;

    public AgentStep(int stepNumber, String thought, String action,
                     String actionInput, String observation) {
        this.stepNumber = stepNumber;
        this.thought = thought;
        this.action = action;
        this.actionInput = actionInput;
        this.observation = observation;
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public String getThought() {
        return thought;
    }

    public String getAction() {
        return action;
    }

    public String getActionInput() {
        return actionInput;
    }

    public String getObservation() {
        return observation;
    }

    @Override
    public String toString() {
        return String.format(
                "Step %d:\n  Thought: %s\n  Action: %s(%s)\n  Observation: %s",
                stepNumber, thought, action, actionInput, observation);
    }
}
