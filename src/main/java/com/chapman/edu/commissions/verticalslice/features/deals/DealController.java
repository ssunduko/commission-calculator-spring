package com.chapman.edu.commissions.verticalslice.features.deals;

import com.chapman.edu.commissions.verticalslice.domain.DealStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Deal Management.
 * Handles all HTTP requests related to deals.
 */
@RestController
@RequestMapping("/api/deals")
public class DealController {
    private final DealService dealService;

    public DealController(DealService dealService) {
        this.dealService = dealService;
    }

    @PostMapping
    public ResponseEntity<DealResponse> createDeal(@RequestBody CreateDealRequest request) {
        DealResponse response = dealService.createDeal(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DealResponse> getDeal(@PathVariable String id) {
        DealResponse response = dealService.getDeal(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<DealResponse>> getAllDeals(
        @RequestParam(required = false) String salesRepId,
        @RequestParam(required = false) DealStatus status
    ) {
        List<DealResponse> deals;

        if (salesRepId != null) {
            deals = dealService.getDealsBySalesRep(salesRepId);
        } else if (status != null) {
            deals = dealService.getDealsByStatus(status);
        } else {
            deals = dealService.getAllDeals();
        }

        return ResponseEntity.ok(deals);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DealResponse> updateDeal(
        @PathVariable String id,
        @RequestBody UpdateDealRequest request
    ) {
        DealResponse response = dealService.updateDeal(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeal(@PathVariable String id) {
        dealService.deleteDeal(id);
        return ResponseEntity.noContent().build();
    }
}
