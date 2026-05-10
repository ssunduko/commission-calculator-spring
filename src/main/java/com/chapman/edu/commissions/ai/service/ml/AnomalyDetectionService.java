package com.chapman.edu.commissions.ai.service.ml;

import com.chapman.edu.commissions.ai.service.prompt.PromptTemplateService;
import com.chapman.edu.commissions.orm.entity.CommissionCalculation;
import com.chapman.edu.commissions.orm.repository.CommissionCalculationRepository;
import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * ============================================================
 * SPRING AI SERVICE: AnomalyDetectionService
 * ============================================================
 *
 * CONCEPT: Combining Traditional Analytics with AI Analysis
 * ------------------------------------------------------------
 * This service demonstrates a hybrid approach where:
 * 1. Traditional code computes statistics (mean, std deviation)
 * 2. AI analyzes the data with those statistics as context
 *
 * WHY HYBRID?
 * - AI models are great at pattern recognition and natural language
 * - But they can be imprecise with complex math
 * - Solution: Compute numbers in Java, let AI interpret them
 *
 * ANOMALY DETECTION IN COMMISSIONS:
 * An anomalous commission might indicate:
 * - Data entry errors (wrong deal value)
 * - Rule misconfiguration (incorrect tier rate)
 * - Legitimate outlier (exceptionally large deal)
 * - Potential fraud (manually adjusted calculations)
 *
 * The service flags calculations that deviate significantly from
 * the mean, then uses AI to provide nuanced analysis of each anomaly.
 */
@Service
public class AnomalyDetectionService {

    private static final Logger log = LoggerFactory.getLogger(AnomalyDetectionService.class);

    private final ChatClient chatClient;
    private final PromptTemplateService promptTemplateService;
    private final CommissionCalculationRepository calculationRepository;

    public AnomalyDetectionService(ChatClient commissionChatClient,
                                    PromptTemplateService promptTemplateService,
                                    CommissionCalculationRepository calculationRepository) {
        this.chatClient = commissionChatClient;
        this.promptTemplateService = promptTemplateService;
        this.calculationRepository = calculationRepository;
    }

    /**
     * Detects anomalies in commission calculations using statistical
     * analysis combined with AI interpretation.
     *
     * ALGORITHM:
     * 1. Load all calculations from the database
     * 2. Compute mean and standard deviation of commission amounts
     * 3. Flag calculations that are > 2 standard deviations from mean
     * 4. Format data and statistics into a prompt
     * 5. Send to Claude for nuanced analysis and recommendations
     *
     * STATISTICAL FOUNDATION:
     * Under a normal distribution:
     * - 68% of data falls within 1 standard deviation of the mean
     * - 95% within 2 standard deviations
     * - 99.7% within 3 standard deviations
     * So anything > 2σ from the mean has < 5% probability of being "normal"
     *
     * @return An AI-generated anomaly analysis report
     */
    @Observed(name = "commission.anomaly.detect", contextualName = "anomaly-detect-all")
    public String detectAnomalies() {
        log.info("Running anomaly detection on commission calculations");

        List<CommissionCalculation> calculations = calculationRepository.findAll();

        if (calculations.isEmpty()) {
            return "No commission calculations available for anomaly detection.";
        }

        // Step 1: Compute statistics using traditional Java code
        BigDecimal sum = BigDecimal.ZERO;
        for (CommissionCalculation calc : calculations) {
            sum = sum.add(calc.getNetCommission());
        }
        BigDecimal mean = sum.divide(BigDecimal.valueOf(calculations.size()), 2, RoundingMode.HALF_UP);

        // Compute standard deviation
        BigDecimal varianceSum = BigDecimal.ZERO;
        for (CommissionCalculation calc : calculations) {
            BigDecimal diff = calc.getNetCommission().subtract(mean);
            varianceSum = varianceSum.add(diff.multiply(diff));
        }
        BigDecimal variance = varianceSum.divide(BigDecimal.valueOf(calculations.size()), 2, RoundingMode.HALF_UP);
        BigDecimal stdDev = BigDecimal.valueOf(Math.sqrt(variance.doubleValue()))
                .setScale(2, RoundingMode.HALF_UP);

        // Step 2: Build summary of calculations for AI analysis
        StringBuilder summary = new StringBuilder();
        for (CommissionCalculation calc : calculations) {
            BigDecimal deviation = calc.getNetCommission().subtract(mean).abs();
            boolean isAnomaly = stdDev.compareTo(BigDecimal.ZERO) > 0 &&
                    deviation.compareTo(stdDev.multiply(BigDecimal.valueOf(2))) > 0;

            summary.append(String.format(
                    "- ID: %s | Net: $%s | Status: %s%s\n",
                    calc.getId(),
                    calc.getNetCommission().toPlainString(),
                    calc.getStatus(),
                    isAnomaly ? " [FLAGGED - >2σ from mean]" : ""
            ));
        }

        // Step 3: Use prompt template to ask AI for analysis
        Prompt prompt = promptTemplateService.createAnomalyDetectionPrompt(
                summary.toString(),
                "Standard commission rules apply",
                mean.toPlainString(),
                stdDev.toPlainString()
        );

        String analysis = chatClient.prompt(prompt)
                .call()
                .content();

        log.info("Anomaly detection analysis completed");
        return analysis;
    }

    /**
     * Performs a quick anomaly check on a single calculation.
     *
     * SINGLE-ITEM ANALYSIS:
     * This method compares one calculation against the population
     * and asks the AI for a targeted assessment.
     *
     * This is useful for real-time validation: when a new commission
     * is calculated, immediately check if it seems reasonable.
     *
     * @param calculationId The ID of the calculation to check
     * @return An AI assessment of whether this calculation is anomalous
     */
    public String checkSingleCalculation(String calculationId) {
        CommissionCalculation calc = calculationRepository.findById(calculationId)
                .orElse(null);

        if (calc == null) {
            return "Calculation not found";
        }

        // Get population statistics for comparison
        List<CommissionCalculation> allCalcs = calculationRepository.findAll();
        BigDecimal sum = BigDecimal.ZERO;
        for (CommissionCalculation c : allCalcs) {
            sum = sum.add(c.getNetCommission());
        }
        BigDecimal avg = allCalcs.isEmpty() ? BigDecimal.ZERO :
                sum.divide(BigDecimal.valueOf(allCalcs.size()), 2, RoundingMode.HALF_UP);

        String response = chatClient.prompt()
                .system("You are a financial anomaly detection system. Be concise.")
                .user(String.format("""
                        Is this commission calculation anomalous?

                        Calculation: $%s (base: $%s, gross: $%s)
                        Population average: $%s
                        Total calculations in system: %d

                        Respond with: NORMAL or ANOMALOUS, followed by a brief explanation.
                        """,
                        calc.getNetCommission().toPlainString(),
                        calc.getBaseCommission().toPlainString(),
                        calc.getGrossCommission().toPlainString(),
                        avg.toPlainString(),
                        allCalcs.size()))
                .call()
                .content();

        return response;
    }
}
