package com.chapman.edu.commissions.springboot.controller;

import com.chapman.edu.commissions.model.Deal;
import com.chapman.edu.commissions.model.DealStatus;
import com.chapman.edu.commissions.springboot.dto.request.CreateDealRequest;
import com.chapman.edu.commissions.springboot.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

/**
 * ============================================================================
 * MVC CONTROLLER — THYMELEAF WEB INTERFACE
 * ============================================================================
 *
 * CONCEPT: @Controller vs @RestController
 * ------------------------------------------
 * @Controller (used here) — Returns VIEW NAMES that are resolved to
 *   Thymeleaf templates. The String return value is a template path:
 *     return "springboot/dashboard"  →  templates/springboot/dashboard.html
 *
 * @RestController — Returns DATA (JSON/XML) directly in the response body.
 *     return dealObject  →  {"id": "deal-001", "title": "Acme Corp"}
 *
 * CONCEPT: Spring MVC with Thymeleaf
 * -------------------------------------
 * The Model object is passed to every handler method. Data added to the Model
 * is available in the Thymeleaf template:
 *
 *   Java:  model.addAttribute("deals", dealList);
 *   HTML:  <tr th:each="deal : ${deals}">
 *            <td th:text="${deal.title}">Title</td>
 *          </tr>
 *
 * CONCEPT: Form Handling
 * -----------------------
 * Spring MVC provides form binding between HTML forms and Java objects:
 *   1. GET request: Controller adds empty form object to model
 *   2. Thymeleaf binds form fields to object properties
 *   3. POST request: Spring populates the object from form data
 *   4. @Valid triggers validation, BindingResult holds errors
 *   5. If errors exist, re-render form with error messages
 *   6. If valid, process the form and redirect
 *
 * @see org.springframework.stereotype.Controller
 * @see org.springframework.ui.Model
 */
@Controller
@RequestMapping("/springboot")
public class DashboardController {

    private final DealService dealService;
    private final CommissionPlanService planService;
    private final CommissionCalculationService calculationService;
    private final UserService userService;
    private final DisputeService disputeService;

    public DashboardController(DealService dealService,
                               CommissionPlanService planService,
                               CommissionCalculationService calculationService,
                               UserService userService,
                               DisputeService disputeService) {
        this.dealService = dealService;
        this.planService = planService;
        this.calculationService = calculationService;
        this.userService = userService;
        this.disputeService = disputeService;
    }

    /**
     * GET /springboot/dashboard — Main dashboard page
     *
     * The Model object carries data from the controller to the Thymeleaf template.
     * model.addAttribute("key", value) makes "key" available as ${key} in HTML.
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("dealCount", dealService.getDealCount());
        model.addAttribute("planCount", planService.getPlanCount());
        model.addAttribute("calculationCount", calculationService.getCalculationCount());
        model.addAttribute("userCount", userService.getUserCount());
        model.addAttribute("disputeCount", disputeService.getDisputeCount());
        model.addAttribute("recentDeals", dealService.getAllDeals());
        model.addAttribute("activePlans", planService.getActivePlans());

        // The return value is the Thymeleaf template path (without .html extension)
        return "springboot/dashboard";
    }

    /**
     * GET /springboot/deals — Deal list page
     */
    @GetMapping("/deals")
    public String dealList(Model model) {
        model.addAttribute("deals", dealService.getAllDeals());
        return "springboot/deals/list";
    }

    /**
     * GET /springboot/deals/{id} — Deal detail page
     */
    @GetMapping("/deals/{id}")
    public String dealDetail(@PathVariable String id, Model model) {
        model.addAttribute("deal", dealService.getDealById(id));
        return "springboot/deals/detail";
    }

    /**
     * GET /springboot/deals/new — New deal form
     *
     * Adds an empty CreateDealRequest to the model for Thymeleaf form binding.
     */
    @GetMapping("/deals/new")
    public String newDealForm(Model model) {
        model.addAttribute("dealRequest", new CreateDealRequest());
        model.addAttribute("salesReps", userService.getAllUsers());
        return "springboot/deals/form";
    }

    /**
     * POST /springboot/deals — Submit new deal form
     *
     * CONCEPT: BindingResult
     * BindingResult holds validation errors from @Valid. It must come
     * immediately after the validated parameter in the method signature.
     * If there are errors, we re-render the form (don't redirect).
     *
     * CONCEPT: RedirectAttributes
     * After a successful form submission, we redirect (POST-Redirect-GET pattern)
     * to prevent duplicate form submissions. RedirectAttributes carries flash
     * messages to the redirected page.
     */
    @PostMapping("/deals")
    public String createDeal(@Valid @ModelAttribute("dealRequest") CreateDealRequest request,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        // If validation fails, re-render the form with errors
        if (bindingResult.hasErrors()) {
            model.addAttribute("salesReps", userService.getAllUsers());
            return "springboot/deals/form";
        }

        dealService.createDeal(request);
        redirectAttributes.addFlashAttribute("successMessage", "Deal created successfully!");
        return "redirect:/springboot/deals";
    }

    /**
     * GET /springboot/plans — Commission plans list page
     */
    @GetMapping("/plans")
    public String planList(Model model) {
        model.addAttribute("plans", planService.getAllPlans());
        return "springboot/plans/list";
    }

    /**
     * GET /springboot/plans/{id} — Plan detail page
     */
    @GetMapping("/plans/{id}")
    public String planDetail(@PathVariable String id, Model model) {
        model.addAttribute("plan", planService.getPlanById(id));
        return "springboot/plans/detail";
    }

    /**
     * GET /springboot/calculations — Commission calculations list page
     */
    @GetMapping("/calculations")
    public String calculationList(Model model) {
        model.addAttribute("calculations", calculationService.getAllCalculations());
        return "springboot/calculations/list";
    }

    /**
     * GET /springboot/calculations/{id} — Calculation detail page
     */
    @GetMapping("/calculations/{id}")
    public String calculationDetail(@PathVariable String id, Model model) {
        model.addAttribute("calculation", calculationService.getCalculationById(id));
        return "springboot/calculations/detail";
    }
}
