package com.chapman.edu.commissions.ai.controller;

import com.chapman.edu.commissions.ai.service.agent.CommissionReActAgent;
import com.chapman.edu.commissions.ai.service.workflow.CommissionWorkflowOrchestrator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * Thymeleaf Web Controller for AI Commission Calculator UI.
 *
 * Serves HTML pages that showcase AI functionality. The pages use
 * JavaScript fetch() to call the existing REST API endpoints in
 * {@link CommissionController}, keeping the UI decoupled from
 * the backend logic.
 */
@Controller
@RequestMapping("/ai")
public class AiWebController {

    private final CommissionReActAgent reActAgent;
    private final CommissionWorkflowOrchestrator workflowOrchestrator;

    public AiWebController(CommissionReActAgent reActAgent,
                           CommissionWorkflowOrchestrator workflowOrchestrator) {
        this.reActAgent = reActAgent;
        this.workflowOrchestrator = workflowOrchestrator;
    }

    @GetMapping
    public String dashboard(Model model) {
        // Provide agent tools and workflow agents for display
        Map<String, String> agentTools = new java.util.LinkedHashMap<>();
        reActAgent.getTools().forEach((name, tool) ->
                agentTools.put(name, tool.getDescription()));
        model.addAttribute("agentTools", agentTools);
        model.addAttribute("workflowAgents", workflowOrchestrator.getRegisteredAgents());
        return "ai/dashboard";
    }

    @GetMapping("/rag")
    public String rag() {
        return "ai/rag";
    }

    @GetMapping("/explainer")
    public String explainer() {
        return "ai/explainer";
    }

    @GetMapping("/disputes")
    public String disputes() {
        return "ai/disputes";
    }

    @GetMapping("/forecast")
    public String forecast() {
        return "ai/forecast";
    }

    @GetMapping("/anomaly")
    public String anomaly() {
        return "ai/anomaly";
    }

    @GetMapping("/moderation")
    public String moderation() {
        return "ai/moderation";
    }

    @GetMapping("/agent")
    public String agent(Model model) {
        Map<String, String> tools = new java.util.LinkedHashMap<>();
        reActAgent.getTools().forEach((name, tool) ->
                tools.put(name, tool.getDescription()));
        model.addAttribute("agentTools", tools);
        return "ai/agent";
    }

    @GetMapping("/workflow")
    public String workflow(Model model) {
        model.addAttribute("workflowAgents", workflowOrchestrator.getRegisteredAgents());
        return "ai/workflow";
    }
}
