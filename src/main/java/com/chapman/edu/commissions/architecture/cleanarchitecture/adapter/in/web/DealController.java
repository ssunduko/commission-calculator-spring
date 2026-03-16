package com.chapman.edu.commissions.architecture.cleanarchitecture.adapter.in.web;

import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.CreateDealCommand;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.DealResult;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.UpdateDealCommand;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.port.in.DealUseCase;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.DealStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/clean/deals")
public class DealController {

    private final DealUseCase dealUseCase;

    public DealController(DealUseCase dealUseCase) {
        this.dealUseCase = dealUseCase;
    }

    @PostMapping
    public ResponseEntity<DealResult> createDeal(@RequestBody CreateDealCommand command) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dealUseCase.createDeal(command));
    }

    @GetMapping
    public ResponseEntity<List<DealResult>> getAllDeals(
            @RequestParam(required = false) String salesRepId,
            @RequestParam(required = false) DealStatus status) {
        if (salesRepId != null) {
            return ResponseEntity.ok(dealUseCase.getDealsBySalesRep(salesRepId));
        }
        if (status != null) {
            return ResponseEntity.ok(dealUseCase.getDealsByStatus(status));
        }
        return ResponseEntity.ok(dealUseCase.getAllDeals());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DealResult> getDeal(@PathVariable String id) {
        return ResponseEntity.ok(dealUseCase.getDeal(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DealResult> updateDeal(@PathVariable String id, @RequestBody UpdateDealCommand command) {
        return ResponseEntity.ok(dealUseCase.updateDeal(id, command));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeal(@PathVariable String id) {
        dealUseCase.deleteDeal(id);
        return ResponseEntity.noContent().build();
    }
}
