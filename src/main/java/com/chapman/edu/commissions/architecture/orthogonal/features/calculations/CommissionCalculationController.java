package com.chapman.edu.commissions.architecture.orthogonal.features.calculations;

import com.chapman.edu.commissions.architecture.orthogonal.features.calculations.commands.CalculateCommissionCommand;
import com.chapman.edu.commissions.architecture.orthogonal.features.calculations.queries.GetAllCalculationsQuery;
import com.chapman.edu.commissions.architecture.orthogonal.features.calculations.queries.GetCalculationQuery;
import com.chapman.edu.commissions.architecture.orthogonal.pipeline.CommandBus;
import com.chapman.edu.commissions.architecture.orthogonal.pipeline.QueryBus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orthogonal/calculations")
public class CommissionCalculationController {
    private final CommandBus commandBus;
    private final QueryBus queryBus;
    public CommissionCalculationController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @PostMapping
    public ResponseEntity<CommissionCalculationResponse> calculateCommission(@RequestBody CalculateCommissionCommand command) {
        return new ResponseEntity<>(commandBus.dispatch(command), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommissionCalculationResponse> getCalculation(@PathVariable String id) {
        return ResponseEntity.ok(queryBus.dispatch(new GetCalculationQuery(id)));
    }

    @GetMapping
    public ResponseEntity<List<CommissionCalculationResponse>> getAllCalculations(
            @RequestParam(required = false) String dealId, @RequestParam(required = false) String salesRepId) {
        GetAllCalculationsQuery query;
        if (dealId != null) query = GetAllCalculationsQuery.byDeal(dealId);
        else if (salesRepId != null) query = GetAllCalculationsQuery.bySalesRep(salesRepId);
        else query = GetAllCalculationsQuery.all();
        return ResponseEntity.ok(queryBus.dispatch(query));
    }
}
