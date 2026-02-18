package com.chapman.edu.commissions.orm.controller;

import com.chapman.edu.commissions.orm.entity.Deal;
import com.chapman.edu.commissions.orm.entity.DealProduct;
import com.chapman.edu.commissions.orm.entity.DealStatus;
import com.chapman.edu.commissions.orm.service.DealService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * ============================================================
 * REST CONTROLLER: DealController
 * ============================================================
 *
 * Exposes Deal management and search endpoints.
 * Demonstrates pagination, filtering, and CRUD operations.
 */
@RestController
@RequestMapping("/api/orm/deals")
public class DealController {

    private final DealService dealService;

    public DealController(DealService dealService) {
        this.dealService = dealService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Deal> getDealById(@PathVariable String id) {
        return dealService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<Deal> getDealWithProducts(@PathVariable String id) {
        return dealService.findByIdWithProducts(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Deal>> getDealsByStatus(@PathVariable DealStatus status) {
        return ResponseEntity.ok(dealService.findByStatus(status));
    }

    /**
     * Search deals with dynamic filtering and pagination.
     * All filter parameters are optional.
     */
    @GetMapping("/search")
    public ResponseEntity<Page<Deal>> searchDeals(
            @RequestParam(required = false) DealStatus status,
            @RequestParam(required = false) BigDecimal minValue,
            @RequestParam(required = false) BigDecimal maxValue,
            @RequestParam(required = false) String salesRepId,
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Page<Deal> results = dealService.searchDeals(
                status, minValue, maxValue, salesRepId, title,
                PageRequest.of(page, size, sort));

        return ResponseEntity.ok(results);
    }

    @GetMapping("/sales-rep/{salesRepId}")
    public ResponseEntity<Page<Deal>> getDealsBySalesRep(
            @PathVariable String salesRepId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                dealService.findBySalesRep(salesRepId, PageRequest.of(page, size)));
    }

    @PostMapping
    public ResponseEntity<Deal> createDeal(
            @RequestParam String title,
            @RequestParam BigDecimal value,
            @RequestParam String salesRepId) {
        Deal deal = dealService.createDeal(title, value, salesRepId, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(deal);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Deal> updateDealStatus(
            @PathVariable String id,
            @RequestParam DealStatus status) {
        return ResponseEntity.ok(dealService.updateDealStatus(id, status));
    }

    @PostMapping("/{id}/products")
    public ResponseEntity<Deal> addProduct(
            @PathVariable String id,
            @RequestBody DealProduct product) {
        return ResponseEntity.ok(dealService.addProductToDeal(id, product));
    }

    @GetMapping("/sales-rep/{salesRepId}/total-won")
    public ResponseEntity<BigDecimal> getTotalWonValue(@PathVariable String salesRepId) {
        return ResponseEntity.ok(dealService.calculateTotalWonValue(salesRepId));
    }

    @GetMapping("/pending-calculation")
    public ResponseEntity<List<Deal>> getWonDealsWithoutCalculations() {
        return ResponseEntity.ok(dealService.findWonDealsWithoutCalculations());
    }
}
