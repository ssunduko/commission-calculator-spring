package com.chapman.edu.commissions.ai.service.workflow;

/**
 * ============================================================
 * AGENTIC WORKFLOW: Workflow Agent Interface
 * ============================================================
 *
 * CONCEPT: Specialized Agent Contract
 * ------------------------------------------------------------
 * Each agent in the workflow implements this interface. The
 * orchestrator calls agents polymorphically, without knowing
 * their internal implementation.
 *
 * AGENT vs. TOOL (important distinction):
 * ┌──────────────────────┬──────────────────────────────────────┐
 * │ Tool (ReAct)          │ Agent (Workflow)                      │
 * ├──────────────────────┼──────────────────────────────────────┤
 * │ Simple function       │ Has its own AI persona/system prompt  │
 * │ Stateless             │ Reads and writes shared state         │
 * │ Called by one agent   │ Coordinated by an orchestrator        │
 * │ Returns raw data      │ Returns AI-analyzed findings          │
 * │ No autonomy           │ Can reason independently              │
 * │ String in → String out│ State in → mutated State out          │
 * └──────────────────────┴──────────────────────────────────────┘
 *
 * KEY INSIGHT:
 * A ReAct agent uses TOOLS to gather data and reasons itself.
 * A workflow uses AGENTS that each reason independently, and an
 * orchestrator coordinates them. Think of it as:
 * - ReAct = one person using multiple tools
 * - Workflow = a team of specialists collaborating
 *
 * DESIGN:
 * - getName(): Identifies the agent for logging and stage mapping
 * - getStage(): Which workflow stage this agent handles
 * - execute(): The agent's core logic — reads state, calls AI,
 *              writes findings back to state
 */
public interface WorkflowAgent {

    /**
     * Human-readable name of this agent (e.g., "Data Gathering Agent").
     */
    String getName();

    /**
     * The workflow stage this agent is responsible for.
     */
    WorkflowStage getStage();

    /**
     * Execute this agent's task.
     *
     * CONTRACT:
     * 1. Read relevant data from state (previous agents' outputs)
     * 2. Perform AI-powered analysis using ChatClient
     * 3. Write findings back to state for downstream agents
     * 4. Log the stage completion with a brief summary
     * 5. Optionally raise flags for downstream agents
     *
     * @param state The shared workflow state (mutable)
     */
    void execute(WorkflowState state);
}
