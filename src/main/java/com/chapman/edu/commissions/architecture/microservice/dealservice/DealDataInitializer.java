package com.chapman.edu.commissions.architecture.microservice.dealservice;

import com.chapman.edu.commissions.architecture.microservice.dealservice.domain.Deal;
import com.chapman.edu.commissions.architecture.microservice.dealservice.domain.DealStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Seeds sample deals for the Deal Service microservice.
 */
@Component
public class DealDataInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DealDataInitializer.class);
    private final DealRepository dealRepository;

    public DealDataInitializer(DealRepository dealRepository) {
        this.dealRepository = dealRepository;
    }

    @Override
    public void run(String... args) {
        log.info("Initializing Deal Service sample data...");
        createDeal("Enterprise Software License", new BigDecimal("150000"), "ms_rep001", DealStatus.WON);
        createDeal("Cloud Services Contract", new BigDecimal("85000"), "ms_rep001", DealStatus.WON);
        createDeal("Consulting Services", new BigDecimal("45000"), "ms_rep002", DealStatus.WON);
        createDeal("Hardware Procurement", new BigDecimal("120000"), "ms_rep002", DealStatus.OPEN);
        createDeal("Annual Support Renewal", new BigDecimal("25000"), "ms_rep003", DealStatus.WON);
        createDeal("Training Package", new BigDecimal("15000"), "ms_rep003", DealStatus.LOST);
        log.info("Deal Service: 6 deals initialized");
    }

    private void createDeal(String title, BigDecimal value, String salesRepId, DealStatus status) {
        Deal deal = new Deal(title, value, salesRepId);
        deal.setStatus(status);
        if (status == DealStatus.WON) deal.setCloseDate(LocalDate.now().minusDays((int)(Math.random() * 30)));
        dealRepository.save(deal);
    }
}
