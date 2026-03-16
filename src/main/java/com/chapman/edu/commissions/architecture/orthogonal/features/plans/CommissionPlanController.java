package com.chapman.edu.commissions.architecture.orthogonal.features.plans;

import com.chapman.edu.commissions.architecture.orthogonal.domain.PlanStatus;
import com.chapman.edu.commissions.architecture.orthogonal.features.plans.commands.ActivatePlanCommand;
import com.chapman.edu.commissions.architecture.orthogonal.features.plans.commands.AddRuleToPlanCommand;
import com.chapman.edu.commissions.architecture.orthogonal.features.plans.commands.CreatePlanCommand;
import com.chapman.edu.commissions.architecture.orthogonal.features.plans.commands.DeletePlanCommand;
import com.chapman.edu.commissions.architecture.orthogonal.features.plans.queries.GetAllPlansQuery;
import com.chapman.edu.commissions.architecture.orthogonal.features.plans.queries.GetPlanQuery;
import com.chapman.edu.commissions.architecture.orthogonal.pipeline.CommandBus;
import com.chapman.edu.commissions.architecture.orthogonal.pipeline.QueryBus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orthogonal/plans")
public class CommissionPlanController {
    private final CommandBus commandBus;
    private final QueryBus queryBus;
    public CommissionPlanController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @PostMapping
    public ResponseEntity<CommissionPlanResponse> createPlan(@RequestBody CreatePlanCommand command) {
        return new ResponseEntity<>(commandBus.dispatch(command), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommissionPlanResponse> getPlan(@PathVariable String id) {
        return ResponseEntity.ok(queryBus.dispatch(new GetPlanQuery(id)));
    }

    @GetMapping
    public ResponseEntity<List<CommissionPlanResponse>> getAllPlans(@RequestParam(required = false) PlanStatus status) {
        GetAllPlansQuery query = status != null ? GetAllPlansQuery.byStatus(status) : GetAllPlansQuery.all();
        return ResponseEntity.ok(queryBus.dispatch(query));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<CommissionPlanResponse> activatePlan(@PathVariable String id) {
        return ResponseEntity.ok(commandBus.dispatch(new ActivatePlanCommand(id)));
    }

    @PostMapping("/{id}/rules")
    public ResponseEntity<CommissionPlanResponse> addRuleToPlan(@PathVariable String id, @RequestBody AddRuleToPlanCommand command) {
        AddRuleToPlanCommand fullCommand = new AddRuleToPlanCommand(id, command.name(), command.description(), command.rate(), command.ruleType(), command.priority());
        return ResponseEntity.ok(commandBus.dispatch(fullCommand));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlan(@PathVariable String id) {
        commandBus.dispatch(new DeletePlanCommand(id));
        return ResponseEntity.noContent().build();
    }
}
