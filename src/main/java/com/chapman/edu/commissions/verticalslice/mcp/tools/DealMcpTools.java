package com.chapman.edu.commissions.verticalslice.mcp.tools;

import com.chapman.edu.commissions.verticalslice.domain.DealStatus;
import com.chapman.edu.commissions.verticalslice.features.deals.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * MCP Tools for Deal Management.
 * Exposes deal-related operations through the Model Context Protocol.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DealMcpTools {

    private final DealService dealService;

    public DealResponse createDeal(String title, BigDecimal value, String salesRepId) {

        log.info("MCP Tool: Creating deal with title={}, value={}, salesRepId={}", title, value, salesRepId);

        CreateDealRequest request = new CreateDealRequest(title, value, salesRepId);
        return dealService.createDeal(request);
    }

    public DealResponse getDeal(String dealId) {

        log.info("MCP Tool: Getting deal with ID={}", dealId);
        return dealService.getDeal(dealId);
    }

    public List<DealResponse> listDeals(String salesRepId, String status) {

        log.info("MCP Tool: Listing deals with salesRepId={}, status={}", salesRepId, status);

        if (salesRepId != null && !salesRepId.isBlank()) {
            return dealService.getDealsBySalesRep(salesRepId);
        } else if (status != null && !status.isBlank()) {
            DealStatus dealStatus = DealStatus.valueOf(status.toUpperCase());
            return dealService.getDealsByStatus(dealStatus);
        } else {
            return dealService.getAllDeals();
        }
    }

    public DealResponse updateDeal(String dealId, String title, BigDecimal value, String status, String closeDate) {

        log.info("MCP Tool: Updating deal ID={}", dealId);

        DealStatus dealStatus = (status != null && !status.isBlank())
                ? DealStatus.valueOf(status.toUpperCase())
                : null;

        LocalDate parsedCloseDate = (closeDate != null && !closeDate.isBlank())
                ? LocalDate.parse(closeDate)
                : null;

        UpdateDealRequest request = new UpdateDealRequest(title, value, dealStatus, parsedCloseDate);
        return dealService.updateDeal(dealId, request);
    }

    public String deleteDeal(String dealId) {

        log.info("MCP Tool: Deleting deal ID={}", dealId);
        dealService.deleteDeal(dealId);
        return "Deal " + dealId + " deleted successfully";
    }
}
