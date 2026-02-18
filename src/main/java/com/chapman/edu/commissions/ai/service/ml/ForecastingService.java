package com.chapman.edu.commissions.ai.service.ml;

import com.chapman.edu.commissions.ai.service.prompt.PromptTemplateService;
import com.chapman.edu.commissions.orm.entity.CommissionCalculation;
import com.chapman.edu.commissions.orm.entity.Deal;
import com.chapman.edu.commissions.orm.entity.DealStatus;
import com.chapman.edu.commissions.orm.entity.User;
import com.chapman.edu.commissions.orm.repository.CommissionCalculationRepository;
import com.chapman.edu.commissions.orm.repository.DealRepository;
import com.chapman.edu.commissions.orm.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ============================================================
 * SPRING AI SERVICE: ForecastingService
 * ============================================================
 *
 * CONCEPT: AI-Powered Forecasting with Domain Context
 * ------------------------------------------------------------
 * This service demonstrates how AI models can be used for business
 * forecasting by combining historical data with the model's ability
 * to identify patterns and trends.
 *
 * AI FORECASTING vs. TRADITIONAL FORECASTING:
 *
 * Traditional (Statistical Models):
 * - Linear regression, ARIMA, exponential smoothing
 * - Requires structured numerical data
 * - Produces precise numerical predictions
 * - Struggles with qualitative factors
 *
 * AI (LLM-based):
 * - Natural language analysis of trends
 * - Can incorporate qualitative factors (market conditions, rep experience)
 * - Produces narrative forecasts with reasoning
 * - Less precise numerically, but better at holistic analysis
 *
 * BEST PRACTICE:
 * Use AI forecasting to COMPLEMENT, not replace, statistical models.
 * The AI excels at synthesizing diverse data points into actionable
 * insights, while statistical models provide the numerical precision.
 *
 * CHAIN-OF-THOUGHT PROMPTING:
 * The forecast template explicitly asks the AI to:
 * 1. Analyze trends (forces the model to examine historical patterns)
 * 2. Identify factors (forces consideration of variables)
 * 3. Make projections (applies reasoning to form predictions)
 * 4. Give recommendations (converts analysis into action)
 *
 * This structured approach produces much better forecasts than simply
 * asking "predict next quarter's commissions."
 */
@Service
public class ForecastingService {

    private static final Logger log = LoggerFactory.getLogger(ForecastingService.class);

    private final ChatClient chatClient;
    private final PromptTemplateService promptTemplateService;
    private final CommissionCalculationRepository calculationRepository;
    private final DealRepository dealRepository;
    private final UserRepository userRepository;

    public ForecastingService(ChatClient commissionChatClient,
                               PromptTemplateService promptTemplateService,
                               CommissionCalculationRepository calculationRepository,
                               DealRepository dealRepository,
                               UserRepository userRepository) {
        this.chatClient = commissionChatClient;
        this.promptTemplateService = promptTemplateService;
        this.calculationRepository = calculationRepository;
        this.dealRepository = dealRepository;
        this.userRepository = userRepository;
    }

    /**
     * Generates a commission forecast for a sales representative.
     *
     * DATA GATHERING APPROACH:
     * 1. Load the user's historical commission calculations
     * 2. Load their current pipeline (open deals)
     * 3. Format both into structured text
     * 4. Use the forecast prompt template
     * 5. Send to Claude for analysis
     *
     * @param userId The ID of the sales representative
     * @return An AI-generated commission forecast
     */
    public String forecastCommissions(String userId) {
        log.info("Generating commission forecast for user: {}", userId);

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return "User not found with ID: " + userId;
        }

        // Gather historical data
        List<CommissionCalculation> historicalCalcs =
                calculationRepository.findBySalesRepId(userId);

        StringBuilder historicalData = new StringBuilder();
        if (historicalCalcs.isEmpty()) {
            historicalData.append("No historical commission data available.");
        } else {
            for (CommissionCalculation calc : historicalCalcs) {
                historicalData.append(String.format(
                        "- Date: %s | Base: $%s | Net: $%s | Status: %s\n",
                        calc.getCalculationDate(),
                        calc.getBaseCommission().toPlainString(),
                        calc.getNetCommission().toPlainString(),
                        calc.getStatus()
                ));
            }
        }

        // Gather pipeline data (open deals)
        List<Deal> openDeals = dealRepository.findByStatus(DealStatus.OPEN);
        StringBuilder pipelineData = new StringBuilder();
        if (openDeals.isEmpty()) {
            pipelineData.append("No open deals in the pipeline.");
        } else {
            for (Deal deal : openDeals) {
                pipelineData.append(String.format(
                        "- Deal: %s | Value: $%s | Status: %s | Created: %s\n",
                        deal.getTitle(),
                        deal.getValue().toPlainString(),
                        deal.getStatus(),
                        deal.getCreatedDate()
                ));
            }
        }

        // Use template service to build the forecast prompt
        Prompt prompt = promptTemplateService.createForecastPrompt(
                user.getFullName(),
                historicalData.toString(),
                pipelineData.toString()
        );

        String forecast = chatClient.prompt(prompt)
                .call()
                .content();

        log.info("Forecast generated for user: {}", userId);
        return forecast;
    }

    /**
     * Generates a team-level commission forecast.
     *
     * AGGREGATION PATTERN:
     * This method demonstrates gathering data across multiple entities
     * (all sales reps) to provide an organizational forecast.
     *
     * @return An AI-generated team commission forecast
     */
    public String forecastTeamCommissions() {
        log.info("Generating team commission forecast");

        List<CommissionCalculation> allCalcs = calculationRepository.findAll();
        List<Deal> openDeals = dealRepository.findByStatus(DealStatus.OPEN);
        List<Deal> wonDeals = dealRepository.findByStatus(DealStatus.WON);

        String response = chatClient.prompt()
                .system("""
                        You are a senior sales forecasting analyst.
                        Analyze team performance data and provide a comprehensive forecast.
                        Include total projected revenue, identify top and underperforming areas,
                        and suggest strategic actions.
                        """)
                .user(String.format("""
                        Team Performance Summary:
                        - Total commission calculations: %d
                        - Currently open deals: %d
                        - Won deals: %d

                        Open Pipeline Details:
                        %s

                        Recent Commissions:
                        %s

                        Please provide a team-level forecast for the next quarter with:
                        1. Projected total team commissions
                        2. Pipeline conversion analysis
                        3. Risk factors and opportunities
                        4. Strategic recommendations
                        """,
                        allCalcs.size(),
                        openDeals.size(),
                        wonDeals.size(),
                        formatDeals(openDeals),
                        formatCalculations(allCalcs)))
                .call()
                .content();

        return response;
    }

    private String formatDeals(List<Deal> deals) {
        if (deals.isEmpty()) return "No deals available.";
        StringBuilder sb = new StringBuilder();
        for (Deal deal : deals) {
            sb.append(String.format("- %s: $%s (%s)\n",
                    deal.getTitle(), deal.getValue().toPlainString(), deal.getStatus()));
        }
        return sb.toString();
    }

    private String formatCalculations(List<CommissionCalculation> calcs) {
        if (calcs.isEmpty()) return "No calculations available.";
        StringBuilder sb = new StringBuilder();
        int limit = Math.min(calcs.size(), 10);
        for (int i = 0; i < limit; i++) {
            CommissionCalculation calc = calcs.get(i);
            sb.append(String.format("- $%s (net) on %s [%s]\n",
                    calc.getNetCommission().toPlainString(),
                    calc.getCalculationDate(),
                    calc.getStatus()));
        }
        if (calcs.size() > 10) {
            sb.append(String.format("... and %d more calculations\n", calcs.size() - 10));
        }
        return sb.toString();
    }
}
