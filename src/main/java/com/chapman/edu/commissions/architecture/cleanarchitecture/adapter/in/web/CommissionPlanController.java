package com.chapman.edu.commissions.architecture.cleanarchitecture.adapter.in.web;

import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.AddRuleCommand;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.CreatePlanCommand;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.PlanResult;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.port.in.CommissionPlanUseCase;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.PlanStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/clean/plans")
public class CommissionPlanController {

    private final CommissionPlanUseCase commissionPlanUseCase;

    public CommissionPlanController(CommissionPlanUseCase commissionPlanUseCase) {
        this.commissionPlanUseCase = commissionPlanUseCase;
    }

    @PostMapping
    public ResponseEntity<PlanResult> createPlan(@RequestBody CreatePlanCommand command) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commissionPlanUseCase.createPlan(command));
    }

    @GetMapping
    public ResponseEntity<List<PlanResult>> getAllPlans(@RequestParam(required = false) PlanStatus status) {
        if (status != null) {
            return ResponseEntity.ok(commissionPlanUseCase.getPlansByStatus(status));
        }
        return ResponseEntity.ok(commissionPlanUseCase.getAllPlans());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlanResult> getPlan(@PathVariable String id) {
        return ResponseEntity.ok(commissionPlanUseCase.getPlan(id));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<PlanResult> activatePlan(@PathVariable String id) {
        return ResponseEntity.ok(commissionPlanUseCase.activatePlan(id));
    }

    @PostMapping("/{id}/rules")
    public ResponseEntity<PlanResult> addRuleToPlan(@PathVariable String id, @RequestBody AddRuleCommand command) {
        return ResponseEntity.ok(commissionPlanUseCase.addRuleToPlan(id, command));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlan(@PathVariable String id) {
        commissionPlanUseCase.deletePlan(id);
        return ResponseEntity.noContent().build();
    }
}
