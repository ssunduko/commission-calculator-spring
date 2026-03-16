package com.chapman.edu.commissions.architecture.orthogonal.features.disputes;

import com.chapman.edu.commissions.architecture.orthogonal.domain.DisputeStatus;
import com.chapman.edu.commissions.architecture.orthogonal.features.disputes.commands.*;
import com.chapman.edu.commissions.architecture.orthogonal.features.disputes.queries.GetAllDisputesQuery;
import com.chapman.edu.commissions.architecture.orthogonal.features.disputes.queries.GetDisputeQuery;
import com.chapman.edu.commissions.architecture.orthogonal.pipeline.CommandBus;
import com.chapman.edu.commissions.architecture.orthogonal.pipeline.QueryBus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orthogonal/disputes")
public class DisputeController {
    private final CommandBus commandBus;
    private final QueryBus queryBus;
    public DisputeController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @PostMapping
    public ResponseEntity<DisputeResponse> createDispute(@RequestBody CreateDisputeCommand command) {
        return new ResponseEntity<>(commandBus.dispatch(command), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DisputeResponse> getDispute(@PathVariable String id) {
        return ResponseEntity.ok(queryBus.dispatch(new GetDisputeQuery(id)));
    }

    @GetMapping
    public ResponseEntity<List<DisputeResponse>> getAllDisputes(
            @RequestParam(required = false) String salesRepId, @RequestParam(required = false) DisputeStatus status) {
        GetAllDisputesQuery query;
        if (salesRepId != null) query = GetAllDisputesQuery.bySalesRep(salesRepId);
        else if (status != null) query = GetAllDisputesQuery.byStatus(status);
        else query = GetAllDisputesQuery.all();
        return ResponseEntity.ok(queryBus.dispatch(query));
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<DisputeResponse> resolveDispute(@PathVariable String id, @RequestBody ResolveDisputeCommand command) {
        ResolveDisputeCommand fullCommand = new ResolveDisputeCommand(id, command.resolution(), command.resolvedBy(), command.approved());
        return ResponseEntity.ok(commandBus.dispatch(fullCommand));
    }

    @PostMapping("/{id}/escalate")
    public ResponseEntity<DisputeResponse> escalateDispute(@PathVariable String id) {
        return ResponseEntity.ok(commandBus.dispatch(new EscalateDisputeCommand(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDispute(@PathVariable String id) {
        commandBus.dispatch(new DeleteDisputeCommand(id));
        return ResponseEntity.noContent().build();
    }
}
