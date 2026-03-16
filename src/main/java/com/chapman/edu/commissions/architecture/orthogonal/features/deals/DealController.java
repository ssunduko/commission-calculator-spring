package com.chapman.edu.commissions.architecture.orthogonal.features.deals;

import com.chapman.edu.commissions.architecture.orthogonal.domain.DealStatus;
import com.chapman.edu.commissions.architecture.orthogonal.features.deals.commands.CreateDealCommand;
import com.chapman.edu.commissions.architecture.orthogonal.features.deals.commands.DeleteDealCommand;
import com.chapman.edu.commissions.architecture.orthogonal.features.deals.commands.UpdateDealCommand;
import com.chapman.edu.commissions.architecture.orthogonal.features.deals.queries.GetAllDealsQuery;
import com.chapman.edu.commissions.architecture.orthogonal.features.deals.queries.GetDealQuery;
import com.chapman.edu.commissions.architecture.orthogonal.pipeline.CommandBus;
import com.chapman.edu.commissions.architecture.orthogonal.pipeline.QueryBus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CONCEPT: Thin Controller (Orthogonal Architecture)
 *
 * Controllers in orthogonal architecture are extremely thin — they only
 * translate HTTP requests into Commands/Queries and dispatch them.
 * The controller has NO business logic and NO service dependency.
 * It only knows about the bus.
 */
@RestController
@RequestMapping("/api/orthogonal/deals")
public class DealController {

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public DealController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @PostMapping
    public ResponseEntity<DealResponse> createDeal(@RequestBody CreateDealCommand command) {
        DealResponse response = commandBus.dispatch(command);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DealResponse> getDeal(@PathVariable String id) {
        DealResponse response = queryBus.dispatch(new GetDealQuery(id));
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<DealResponse>> getAllDeals(
            @RequestParam(required = false) String salesRepId,
            @RequestParam(required = false) DealStatus status) {
        GetAllDealsQuery query;
        if (salesRepId != null) {
            query = GetAllDealsQuery.bySalesRep(salesRepId);
        } else if (status != null) {
            query = GetAllDealsQuery.byStatus(status);
        } else {
            query = GetAllDealsQuery.all();
        }
        List<DealResponse> deals = queryBus.dispatch(query);
        return ResponseEntity.ok(deals);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DealResponse> updateDeal(
            @PathVariable String id, @RequestBody UpdateDealCommand command) {
        // Reconstruct command with the path variable ID
        UpdateDealCommand fullCommand = new UpdateDealCommand(
                id, command.title(), command.value(), command.status(), command.closeDate());
        DealResponse response = commandBus.dispatch(fullCommand);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeal(@PathVariable String id) {
        commandBus.dispatch(new DeleteDealCommand(id));
        return ResponseEntity.noContent().build();
    }
}
