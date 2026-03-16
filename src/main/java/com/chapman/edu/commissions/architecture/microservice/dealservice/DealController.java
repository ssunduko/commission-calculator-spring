package com.chapman.edu.commissions.architecture.microservice.dealservice;

import com.chapman.edu.commissions.architecture.microservice.common.dto.*;
import com.chapman.edu.commissions.architecture.microservice.dealservice.domain.DealStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * CONCEPT: Microservice REST API
 *
 * Each microservice exposes its own REST API. The Deal Service owns
 * all deal-related operations. Other services (like Calculation Service)
 * call this API via REST clients when they need deal data.
 */
@RestController
@RequestMapping("/api/deals")
public class DealController {
    private final DealService dealService;
    public DealController(DealService dealService) { this.dealService = dealService; }

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
