package com.chapman.edu.commissions.architecture.ddd.interfaces.rest;

import com.chapman.edu.commissions.architecture.ddd.application.deal.DealApplicationService;
import com.chapman.edu.commissions.architecture.ddd.application.dto.*;
import com.chapman.edu.commissions.architecture.ddd.domain.deal.DealStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CONCEPT: Interface Layer (DDD)
 *
 * In DDD, controllers belong to the Interface layer -- the outermost layer
 * that translates between external protocols (HTTP, gRPC, messaging) and
 * the Application layer. Controllers should be thin and contain no
 * business logic.
 *
 * The dependency flow: Interface -> Application -> Domain
 * The domain NEVER depends on outer layers.
 */
@RestController
@RequestMapping("/api/ddd/deals")
public class DealController {

    private final DealApplicationService dealService;

    public DealController(DealApplicationService dealService) {
        this.dealService = dealService;
    }

    @PostMapping
    public ResponseEntity<DealDto> createDeal(@RequestBody CreateDealRequest request) {
        return new ResponseEntity<>(dealService.createDeal(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DealDto> getDeal(@PathVariable String id) {
        return ResponseEntity.ok(dealService.getDeal(id));
    }

    @GetMapping
    public ResponseEntity<List<DealDto>> getAllDeals(
            @RequestParam(required = false) String salesRepId,
            @RequestParam(required = false) DealStatus status) {
        if (salesRepId != null) return ResponseEntity.ok(dealService.getDealsBySalesRep(salesRepId));
        if (status != null) return ResponseEntity.ok(dealService.getDealsByStatus(status));
        return ResponseEntity.ok(dealService.getAllDeals());
    }

    @PutMapping("/{id}")
    public ResponseEntity<DealDto> updateDeal(@PathVariable String id, @RequestBody UpdateDealRequest request) {
        return ResponseEntity.ok(dealService.updateDeal(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeal(@PathVariable String id) {
        dealService.deleteDeal(id);
        return ResponseEntity.noContent().build();
    }
}
