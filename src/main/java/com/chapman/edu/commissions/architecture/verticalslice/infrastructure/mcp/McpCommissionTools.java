package com.chapman.edu.commissions.architecture.verticalslice.infrastructure.mcp;

import com.chapman.edu.commissions.architecture.verticalslice.domain.DealStatus;
import com.chapman.edu.commissions.architecture.verticalslice.domain.DisputeStatus;
import com.chapman.edu.commissions.architecture.verticalslice.domain.PlanStatus;
import com.chapman.edu.commissions.architecture.verticalslice.features.calculations.CalculateCommissionRequest;
import com.chapman.edu.commissions.architecture.verticalslice.features.calculations.CommissionCalculationResponse;
import com.chapman.edu.commissions.architecture.verticalslice.features.calculations.CommissionCalculationService;
import com.chapman.edu.commissions.architecture.verticalslice.features.deals.CreateDealRequest;
import com.chapman.edu.commissions.architecture.verticalslice.features.deals.DealResponse;
import com.chapman.edu.commissions.architecture.verticalslice.features.deals.DealService;
import com.chapman.edu.commissions.architecture.verticalslice.features.deals.UpdateDealRequest;
import com.chapman.edu.commissions.architecture.verticalslice.features.disputes.CreateDisputeRequest;
import com.chapman.edu.commissions.architecture.verticalslice.features.disputes.DisputeResponse;
import com.chapman.edu.commissions.architecture.verticalslice.features.disputes.DisputeService;
import com.chapman.edu.commissions.architecture.verticalslice.features.disputes.ResolveDisputeRequest;
import com.chapman.edu.commissions.architecture.verticalslice.features.plans.AddRuleToPlanRequest;
import com.chapman.edu.commissions.architecture.verticalslice.features.plans.CommissionPlanResponse;
import com.chapman.edu.commissions.architecture.verticalslice.features.plans.CommissionPlanService;
import com.chapman.edu.commissions.architecture.verticalslice.features.plans.CreateCommissionPlanRequest;
import com.chapman.edu.commissions.architecture.verticalslice.features.authentication.AuthService;
import com.chapman.edu.commissions.architecture.verticalslice.features.authentication.LoginRequest;
import com.chapman.edu.commissions.architecture.verticalslice.features.authentication.LoginResponse;
import com.chapman.edu.commissions.architecture.verticalslice.features.registration.RegisterRequest;
import com.chapman.edu.commissions.architecture.verticalslice.features.registration.RegistrationResponse;
import com.chapman.edu.commissions.architecture.verticalslice.features.registration.RegistrationService;
import com.chapman.edu.commissions.architecture.verticalslice.features.subscriptions.SubscriptionPackageResponse;
import com.chapman.edu.commissions.architecture.verticalslice.features.subscriptions.SubscriptionPackageService;
import com.chapman.edu.commissions.architecture.verticalslice.infrastructure.a2a.DisputeClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * MCP Tools Facade for Commission Calculator
 * Exposes all commission calculator functionality as MCP tools for AI agent integration.
 * This class separates MCP concerns from business logic by wrapping service methods.
 */
@Service
public class McpCommissionTools {

    private final DealService dealService;
    private final CommissionPlanService commissionPlanService;
    private final DisputeService disputeService;
    private final CommissionCalculationService calculationService;
    private final AuthService authService;
    private final RegistrationService registrationService;
    private final SubscriptionPackageService subscriptionPackageService;
    private final ObjectProvider<DisputeClient> disputeClientProvider;

    public McpCommissionTools(
            DealService dealService,
            CommissionPlanService commissionPlanService,
            DisputeService disputeService,
            CommissionCalculationService calculationService,
            AuthService authService,
            RegistrationService registrationService,
            SubscriptionPackageService subscriptionPackageService,
            ObjectProvider<DisputeClient> disputeClientProvider) {
        this.dealService = dealService;
        this.commissionPlanService = commissionPlanService;
        this.disputeService = disputeService;
        this.calculationService = calculationService;
        this.authService = authService;
        this.registrationService = registrationService;
        this.subscriptionPackageService = subscriptionPackageService;
        this.disputeClientProvider = disputeClientProvider;
    }

    // ==================== Deal Tools ====================

    @Tool(name = "createDeal",
            description = "Create a new deal with title, value, and sales rep ID. Returns the created deal details.")
    public DealResponse createDeal(CreateDealRequest request) {
        return dealService.createDeal(request);
    }

    @Tool(name = "getDeal",
            description = "Get a deal by its ID. Returns the deal details including title, value, status, and sales rep.")
    public DealResponse getDeal(String id) {
        return dealService.getDeal(id);
    }

    @Tool(name = "getAllDeals",
            description = "Get all deals in the system. Returns a list of all deals with their details.")
    public List<DealResponse> getAllDeals() {
        return dealService.getAllDeals();
    }

    @Tool(name = "getDealsBySalesRep",
            description = "Get all deals for a specific sales representative. Specify the sales rep ID.")
    public List<DealResponse> getDealsBySalesRep(String salesRepId) {
        return dealService.getDealsBySalesRep(salesRepId);
    }

    @Tool(name = "getDealsByStatus",
            description = "Get all deals with a specific status (OPEN, WON, LOST, PENDING). Specify the status.")
    public List<DealResponse> getDealsByStatus(DealStatus status) {
        return dealService.getDealsByStatus(status);
    }

    @Tool(name = "updateDeal",
            description = "Update an existing deal. Specify the deal ID and fields to update (title, value, status, closeDate).")
    public DealResponse updateDeal(String id, UpdateDealRequest request) {
        return dealService.updateDeal(id, request);
    }

    @Tool(name = "deleteDeal",
            description = "Delete a deal by its ID. This permanently removes the deal from the system.")
    public void deleteDeal(String id) {
        dealService.deleteDeal(id);
    }

    // ==================== Commission Plan Tools ====================

    @Tool(name = "createCommissionPlan",
            description = "Create a new commission plan with name, currency code, and effective dates. Returns the created plan details.")
    public CommissionPlanResponse createCommissionPlan(CreateCommissionPlanRequest request) {
        return commissionPlanService.createPlan(request);
    }

    @Tool(name = "getCommissionPlan",
            description = "Get a commission plan by its ID. Returns the plan details including rules and tiers.")
    public CommissionPlanResponse getCommissionPlan(String id) {
        return commissionPlanService.getPlan(id);
    }

    @Tool(name = "getAllCommissionPlans",
            description = "Get all commission plans in the system. Returns a list of all plans with their details.")
    public List<CommissionPlanResponse> getAllCommissionPlans() {
        return commissionPlanService.getAllPlans();
    }

    @Tool(name = "getCommissionPlansByStatus",
            description = "Get all commission plans with a specific status (DRAFT, ACTIVE, INACTIVE, ARCHIVED). Specify the status.")
    public List<CommissionPlanResponse> getCommissionPlansByStatus(PlanStatus status) {
        return commissionPlanService.getPlansByStatus(status);
    }

    @Tool(name = "activateCommissionPlan",
            description = "Activate a commission plan by its ID. Sets the plan status to ACTIVE.")
    public CommissionPlanResponse activateCommissionPlan(String id) {
        return commissionPlanService.activatePlan(id);
    }

    @Tool(name = "addRuleToPlan",
            description = "Add a commission rule to a plan. Specify plan ID, rule name, description, rate, type (STANDARD, ACCELERATOR, BONUS, etc.), and priority. Rate is a whole-number percentage (e.g. 5 for 5%, 10.5 for 10.5%) — not a decimal multiplier like 0.05.")
    public CommissionPlanResponse addRuleToPlan(String planId, AddRuleToPlanRequest request) {
        return commissionPlanService.addRuleToPlan(planId, request);
    }

    @Tool(name = "deleteCommissionPlan",
            description = "Delete a commission plan by its ID. This permanently removes the plan from the system.")
    public void deleteCommissionPlan(String id) {
        commissionPlanService.deletePlan(id);
    }

    // ==================== Dispute Tools ====================

    @Tool(name = "createDispute",
            description = "Create a new dispute for a commission calculation. Specify calculation ID, sales rep ID, title, and description.")
    public DisputeResponse createDispute(CreateDisputeRequest request) {
        return disputeService.createDispute(request);
    }

    @Tool(name = "resolveDispute",
            description = "Resolve a dispute by approving or rejecting it. Specify dispute ID, resolution notes, resolver ID, and approved status.")
    public DisputeResponse resolveDispute(String id, ResolveDisputeRequest request) {
        return disputeService.resolveDispute(id, request);
    }

    @Tool(name = "getAllDisputes",
            description = "Get all disputes in the system. Returns a list of all disputes with their details.")
    public List<DisputeResponse> getAllDisputes() {
        return disputeService.getAllDisputes();
    }

    @Tool(name = "escalateDispute",
            description = "Escalate a dispute to higher management. Specify the dispute ID.")
    public DisputeResponse escalateDispute(String id) {
        return disputeService.escalateDispute(id);
    }

    @Tool(name = "deleteDispute",
            description = "Delete a dispute by its ID. This permanently removes the dispute from the system.")
    public void deleteDispute(String id) {
        disputeService.deleteDispute(id);
    }

    @Tool(name = "getDisputesBySalesRep",
            description = "Get all disputes for a specific sales representative. Specify the sales rep ID.")
    public List<DisputeResponse> getDisputesBySalesRep(String salesRepId) {
        return disputeService.getDisputesBySalesRep(salesRepId);
    }

    @Tool(name = "getDisputesByStatus",
            description = "Get all disputes with a specific status (OPEN, IN_REVIEW, RESOLVED, APPROVED, REJECTED). Specify the status.")
    public List<DisputeResponse> getDisputesByStatus(DisputeStatus status) {
        return disputeService.getDisputesByStatus(status);
    }

    @Tool(name = "getDispute",
            description = "Get a dispute by its ID. Returns the dispute details including status and resolution.")
    public DisputeResponse getDispute(String id) {
        return disputeService.getDispute(id);
    }

    // ==================== Commission Calculation Tools ====================

    @Tool(name = "getAllCommissionCalculations",
            description = "Get all commission calculations in the system. Returns a list of all calculations with their details.")
    public List<CommissionCalculationResponse> getAllCommissionCalculations() {
        return calculationService.getAllCalculations();
    }

    @Tool(name = "getCommissionCalculation",
            description = "Get a commission calculation by its ID. Returns the calculation details including base commission, adjustments, and final amount.")
    public CommissionCalculationResponse getCommissionCalculation(String id) {
        return calculationService.getCalculation(id);
    }

    @Tool(name = "calculateCommission",
            description = "Calculate commission for a deal using a commission plan. Specify deal ID and plan ID. Returns the calculation with base commission, adjustments, and final amount.")
    public CommissionCalculationResponse calculateCommission(CalculateCommissionRequest request) {
        return calculationService.calculateCommission(request);
    }

    @Tool(name = "getCalculationsBySalesRep",
            description = "Get all commission calculations for a specific sales representative. Specify the sales rep ID.")
    public List<CommissionCalculationResponse> getCalculationsBySalesRep(String salesRepId) {
        return calculationService.getCalculationsBySalesRep(salesRepId);
    }

    @Tool(name = "getCalculationsByDeal",
            description = "Get all commission calculations for a specific deal. Specify the deal ID.")
    public List<CommissionCalculationResponse> getCalculationsByDeal(String dealId) {
        return calculationService.getCalculationsByDeal(dealId);
    }

    // ==================== Authentication, Registration, Subscription Packages ====================

    @Tool(name = "listSubscriptionPackages",
            description = "List all active subscription packages that new users can sign up for. "
                    + "Returns id, code, name, tier, monthlyPrice, and limits for each package.")
    public List<SubscriptionPackageResponse> listSubscriptionPackages() {
        return subscriptionPackageService.listActivePackages();
    }

    @Tool(name = "registerUser",
            description = "Register a new user account and subscribe them to a package with a credit card payment. "
                    + "Requires username, email, firstName, lastName, password (>= 8 chars), packageCode "
                    + "(one of the codes returned by listSubscriptionPackages — e.g. BASIC, PROFESSIONAL, ENTERPRISE) "
                    + "and payment details (cardHolderName, cardNumber, expiryMonth, expiryYear, cvv). "
                    + "Returns the new user, subscription, payment summary, and an auth token.")
    public RegistrationResponse registerUser(RegisterRequest request) {
        return registrationService.register(request);
    }

    @Tool(name = "login",
            description = "Authenticate an existing user by username and password. "
                    + "Returns a JWT bearer token the caller can attach to subsequent requests.")
    public LoginResponse login(LoginRequest request) {
        return authService.login(request);
    }

    // ==================== A2A Delegation ====================

    @Tool(name = "delegateToDisputeAgent",
            description = "Delegate a dispute-filing task to the backend A2A dispute agent. "
                    + "Pass a natural-language instruction that includes the sales rep id or "
                    + "calculation id, the reason, and optionally a priority "
                    + "(LOW/MEDIUM/HIGH/URGENT). Returns the agent's reply text — typically the "
                    + "new dispute id and status. Use this when the user asks you to open or file "
                    + "a dispute and you want the specialist agent to resolve ids and create the "
                    + "record on your behalf.")
    public String delegateToDisputeAgent(String task) {
        DisputeClient client = disputeClientProvider.getIfAvailable();
        if (client == null) {
            return "Error: A2A dispute agent is not configured on this server "
                    + "(spring.ai.a2a.server.enabled must be true and the A2A beans must be on the classpath).";
        }
        return client.sendTask(task);
    }
}
