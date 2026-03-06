package com.chapman.edu.commissions.springboot.controller;

import com.chapman.edu.commissions.model.Deal;
import com.chapman.edu.commissions.model.DealStatus;
import com.chapman.edu.commissions.springboot.dto.request.CreateDealRequest;
import com.chapman.edu.commissions.springboot.dto.response.ApiResponse;
import com.chapman.edu.commissions.springboot.dto.response.DealResponse;
import com.chapman.edu.commissions.springboot.mapper.DtoMapper;
import com.chapman.edu.commissions.springboot.service.DealService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ============================================================================
 * REST CONTROLLER — DEAL API ENDPOINTS
 * ============================================================================
 *
 * CONCEPT: @RestController
 * --------------------------
 * @RestController is a convenience annotation that combines:
 *   @Controller + @ResponseBody
 *
 * - @Controller marks this class as a Spring MVC controller
 * - @ResponseBody means return values are serialized directly to JSON/XML
 *   (instead of being resolved as view names for Thymeleaf)
 *
 * Without @RestController, you'd need @ResponseBody on every method:
 *   @Controller
 *   public class DealController {
 *       @GetMapping("/api/deals")
 *       @ResponseBody                      // <-- needed for each method
 *       public List<Deal> getDeals() { ... }
 *   }
 *
 * CONCEPT: @RequestMapping
 * --------------------------
 * @RequestMapping at the class level defines the base URL path for all
 * endpoints in this controller. All methods' paths are relative to this base:
 *   Base: /api/deals
 *   GET /api/deals       → getAllDeals()
 *   GET /api/deals/{id}  → getDealById()
 *   POST /api/deals      → createDeal()
 *
 * CONCEPT: RESTful API Design
 * ------------------------------
 * REST (Representational State Transfer) APIs use HTTP methods semantically:
 *
 *   GET    /api/deals        — List all deals (Read)
 *   GET    /api/deals/{id}   — Get a specific deal (Read)
 *   POST   /api/deals        — Create a new deal (Create)
 *   PUT    /api/deals/{id}   — Update an entire deal (Update)
 *   PATCH  /api/deals/{id}   — Partially update a deal (Partial Update)
 *   DELETE /api/deals/{id}   — Delete a deal (Delete)
 *
 * HTTP Status Codes:
 *   200 OK — Successful read/update
 *   201 Created — Successful creation
 *   204 No Content — Successful deletion
 *   400 Bad Request — Invalid input
 *   404 Not Found — Resource doesn't exist
 *   500 Internal Server Error — Server failure
 *
 * CONCEPT: ResponseEntity
 * -------------------------
 * ResponseEntity<T> provides full control over the HTTP response:
 *   - Status code (200, 201, 404, etc.)
 *   - Response headers
 *   - Response body
 *
 * Example:
 *   return ResponseEntity.status(HttpStatus.CREATED).body(dealResponse);
 *   return ResponseEntity.ok(dealResponse);
 *   return ResponseEntity.notFound().build();
 *
 * @see org.springframework.web.bind.annotation.RestController
 * @see org.springframework.web.bind.annotation.RequestMapping
 */
@RestController
@RequestMapping("/api/deals")
@Tag(name = "Deals", description = "Deal management — create, read, update, and delete sales deals")
public class DealController {

    private static final Logger logger = LoggerFactory.getLogger(DealController.class);

    private final DealService dealService;
    private final DtoMapper mapper;

    public DealController(DealService dealService, DtoMapper mapper) {
        this.dealService = dealService;
        this.mapper = mapper;
    }

    /**
     * GET /api/deals — List all deals
     *
     * CONCEPT: @GetMapping
     * @GetMapping is shorthand for @RequestMapping(method = RequestMethod.GET).
     * Other shortcuts: @PostMapping, @PutMapping, @PatchMapping, @DeleteMapping
     *
     * CONCEPT: @RequestParam — Query Parameters
     * Request parameters are URL query strings: /api/deals?status=WON&salesRepId=user-003
     * They are optional by default (required=false). You can set default values.
     */
    @Operation(summary = "List all deals", description = "Retrieve all deals, optionally filtered by status or sales rep ID")
    @GetMapping
    public ResponseEntity<ApiResponse<List<DealResponse>>> getAllDeals(
            @Parameter(description = "Filter by deal status (OPEN, WON, LOST, CANCELLED)")
            @RequestParam(required = false) String status,
            @Parameter(description = "Filter by sales representative ID")
            @RequestParam(required = false) String salesRepId) {

        List<Deal> deals;

        if (status != null) {
            deals = dealService.getDealsByStatus(DealStatus.valueOf(status));
        } else if (salesRepId != null) {
            deals = dealService.getDealsBySalesRep(salesRepId);
        } else {
            deals = dealService.getAllDeals();
        }

        List<DealResponse> responses = deals.stream()
                .map(mapper::toDealResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Deals retrieved successfully", responses));
    }

    /**
     * GET /api/deals/{id} — Get a specific deal
     *
     * CONCEPT: @PathVariable
     * Path variables are parts of the URL path: /api/deals/{id}
     * The {id} placeholder is extracted and passed as a method parameter.
     * Example: GET /api/deals/deal-001 → id = "deal-001"
     */
    @Operation(summary = "Get deal by ID", description = "Retrieve a specific deal by its unique identifier")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DealResponse>> getDealById(
            @Parameter(description = "Deal ID", example = "deal-001") @PathVariable String id) {

        Deal deal = dealService.getDealById(id);
        DealResponse response = mapper.toDealResponse(deal);

        return ResponseEntity.ok(ApiResponse.success("Deal retrieved successfully", response));
    }

    /**
     * POST /api/deals — Create a new deal
     *
     * CONCEPT: @RequestBody and @Valid
     * @RequestBody tells Spring to deserialize the HTTP request body (JSON)
     * into the CreateDealRequest object.
     *
     * @Valid triggers Bean Validation on the request object. If validation
     * fails, a MethodArgumentNotValidException is thrown and caught by
     * our GlobalExceptionHandler.
     */
    @Operation(summary = "Create a new deal", description = "Create a new sales deal with title, value, and assigned sales rep")
    @PostMapping
    public ResponseEntity<ApiResponse<DealResponse>> createDeal(
            @Valid @RequestBody CreateDealRequest request) {

        Deal deal = dealService.createDeal(request);
        DealResponse response = mapper.toDealResponse(deal);

        // Return 201 Created with the new deal in the body
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Deal created successfully", response));
    }

    /**
     * PATCH /api/deals/{id}/status — Update deal status
     *
     * CONCEPT: @PatchMapping
     * PATCH is used for partial updates (only changing specific fields).
     * PUT would be for replacing the entire resource.
     */
    @Operation(summary = "Update deal status", description = "Change the status of an existing deal (e.g., OPEN → WON, OPEN → LOST)")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<DealResponse>> updateDealStatus(
            @Parameter(description = "Deal ID", example = "deal-001") @PathVariable String id,
            @Parameter(description = "New status (OPEN, WON, LOST, CANCELLED)") @RequestParam String status) {

        Deal deal = dealService.updateDealStatus(id, DealStatus.valueOf(status));
        DealResponse response = mapper.toDealResponse(deal);

        return ResponseEntity.ok(ApiResponse.success("Deal status updated", response));
    }

    /**
     * DELETE /api/deals/{id} — Delete a deal
     *
     * Returns 204 No Content — standard response for successful deletion
     * with no body to return.
     */
    @Operation(summary = "Delete a deal", description = "Permanently remove a deal by its ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeal(@Parameter(description = "Deal ID", example = "deal-001") @PathVariable String id) {
        dealService.deleteDeal(id);
        return ResponseEntity.noContent().build();
    }
}
