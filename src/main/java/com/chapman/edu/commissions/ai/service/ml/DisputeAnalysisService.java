package com.chapman.edu.commissions.ai.service.ml;

import com.chapman.edu.commissions.ai.service.prompt.PromptTemplateService;
import com.chapman.edu.commissions.orm.entity.CommissionCalculation;
import com.chapman.edu.commissions.orm.entity.Dispute;
import com.chapman.edu.commissions.orm.repository.DisputeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

/**
 * ============================================================
 * SPRING AI SERVICE: DisputeAnalysisService
 * ============================================================
 *
 * CONCEPT: AI-Powered Business Process Analysis
 * ------------------------------------------------------------
 * This service uses Claude AI to analyze commission disputes,
 * providing objective assessments and resolution recommendations.
 *
 * BUSINESS CONTEXT:
 * Commission disputes are common in sales organizations. A sales rep
 * may dispute a calculation if they believe:
 * - The wrong commission rate was applied
 * - A deal was not attributed correctly
 * - Bonuses were not included
 * - The deal value was recorded incorrectly
 *
 * AI VALUE ADD:
 * Instead of a manager manually reviewing each dispute, the AI can:
 * 1. Quickly analyze the dispute against the calculation data
 * 2. Identify if the dispute has merit based on the numbers
 * 3. Suggest a fair resolution
 * 4. Prioritize disputes that need human attention
 *
 * This doesn't REPLACE human judgment — it AUGMENTS it by providing
 * a data-driven starting point for the resolution process.
 *
 * PROMPT TEMPLATE USAGE:
 * This service uses the dispute-analysis.st template, demonstrating
 * how prompt templates separate the prompt logic from the service logic.
 * If the prompt needs adjustment, you edit the .st file without
 * changing any Java code.
 */
@Service
public class DisputeAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(DisputeAnalysisService.class);

    private final ChatClient chatClient;
    private final PromptTemplateService promptTemplateService;
    private final DisputeRepository disputeRepository;

    public DisputeAnalysisService(ChatClient commissionChatClient,
                                   PromptTemplateService promptTemplateService,
                                   DisputeRepository disputeRepository) {
        this.chatClient = commissionChatClient;
        this.promptTemplateService = promptTemplateService;
        this.disputeRepository = disputeRepository;
    }

    /**
     * Analyzes a commission dispute using AI.
     *
     * INTEGRATION FLOW:
     * 1. Load the dispute and its related commission calculation from JPA
     * 2. Extract relevant data points (amounts, status, description)
     * 3. Build a structured prompt using the template service
     * 4. Send to Claude for analysis
     * 5. Return the AI's assessment
     *
     * PROMPT ENGINEERING NOTE:
     * The dispute-analysis.st template is designed to elicit a balanced,
     * objective response by:
     * - Presenting facts from both sides (the dispute and the calculation)
     * - Asking for specific assessments (validity, factors, resolution)
     * - Requesting actionable next steps
     *
     * @param disputeId The ID of the dispute to analyze
     * @return An AI-generated analysis of the dispute
     */
    public String analyzeDispute(String disputeId) {
        log.info("Analyzing dispute: {}", disputeId);

        Dispute dispute = disputeRepository.findById(disputeId)
                .orElse(null);

        if (dispute == null) {
            return "Dispute not found with ID: " + disputeId;
        }

        CommissionCalculation calc = dispute.getCalculation();

        // Build prompt using template service
        Prompt prompt = promptTemplateService.createDisputeAnalysisPrompt(
                dispute.getTitle(),
                dispute.getDescription(),
                dispute.getSalesRep().getFullName(),
                dispute.getStatus().name(),
                calc.getBaseCommission().toPlainString(),
                calc.getGrossCommission().toPlainString(),
                calc.getNetCommission().toPlainString(),
                calc.getDeal().getValue().toPlainString()
        );

        String analysis = chatClient.prompt(prompt)
                .call()
                .content();

        log.info("Dispute analysis completed for: {}", disputeId);
        return analysis;
    }

    /**
     * Provides a quick AI assessment of dispute priority.
     *
     * LIGHTWEIGHT AI CALL:
     * Not every AI interaction needs a complex template. This method
     * uses a simple inline prompt to get a quick priority assessment.
     *
     * This demonstrates the difference between:
     * - Full analysis (above): Template-driven, comprehensive, for detailed review
     * - Quick triage (here): Inline prompt, concise, for queue management
     *
     * @param disputeId The ID of the dispute to triage
     * @return A brief priority assessment (HIGH, MEDIUM, LOW with reason)
     */
    public String triageDispute(String disputeId) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElse(null);

        if (dispute == null) {
            return "Dispute not found";
        }

        String response = chatClient.prompt()
                .system("You are a dispute triage system. Respond with exactly: " +
                        "PRIORITY: [HIGH|MEDIUM|LOW] - [one sentence reason]")
                .user(String.format("Dispute: %s. Description: %s. Amount: $%s",
                        dispute.getTitle(),
                        dispute.getDescription(),
                        dispute.getCalculation().getNetCommission().toPlainString()))
                .call()
                .content();

        return response;
    }
}
