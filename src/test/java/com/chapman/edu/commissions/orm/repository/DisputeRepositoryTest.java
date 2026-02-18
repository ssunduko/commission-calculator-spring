package com.chapman.edu.commissions.orm.repository;

import com.chapman.edu.commissions.orm.entity.Dispute;
import com.chapman.edu.commissions.orm.entity.DisputeStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for DisputeRepository.
 *
 * KEY CONCEPTS DEMONSTRATED:
 * - Testing pagination with @DataJpaTest
 * - JOIN FETCH queries for eager loading comments
 * - Multiple @ManyToOne relationships to the same entity (User)
 *
 * Seed data (V2): 1 dispute (disp-001) with 2 comments
 */
@DataJpaTest
@DisplayName("DisputeRepository — Integration Tests")
class DisputeRepositoryTest {

    @Autowired
    private DisputeRepository disputeRepository;

    @Test
    @DisplayName("findBySalesRepId should return disputes filed by a sales rep")
    void findBySalesRepId_shouldReturnDisputesForSalesRep() {
        // usr-002 filed dispute disp-001
        List<Dispute> disputes = disputeRepository.findBySalesRepId("usr-002");

        assertThat(disputes).hasSize(1);
        assertThat(disputes.get(0).getTitle()).isEqualTo("Incorrect Commission Rate");
    }

    @Test
    @DisplayName("findByManagerId should return disputes assigned to a manager")
    void findByManagerId_shouldReturnDisputesForManager() {
        // usr-004 is the manager assigned to disp-001
        List<Dispute> disputes = disputeRepository.findByManagerId("usr-004");

        assertThat(disputes).hasSize(1);
    }

    @Test
    @DisplayName("findByStatus should return paginated disputes with status")
    void findByStatus_shouldReturnPaginatedDisputes() {
        Page<Dispute> page = disputeRepository.findByStatus(
                DisputeStatus.UNDER_REVIEW, PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getStatus()).isEqualTo(DisputeStatus.UNDER_REVIEW);
    }

    @Test
    @DisplayName("findByIdWithComments should eagerly load comments")
    void findByIdWithComments_shouldEagerlyLoadComments() {
        Optional<Dispute> dispute = disputeRepository.findByIdWithComments("disp-001");

        assertThat(dispute).isPresent();
        // disp-001 has 2 comments in V2 seed data
        assertThat(dispute.get().getComments()).hasSize(2);
    }

    @Test
    @DisplayName("countByStatus should return status counts")
    void countByStatus_shouldReturnStatusCounts() {
        List<Object[]> counts = disputeRepository.countByStatus();

        assertThat(counts).isNotEmpty();
        // At least 1 UNDER_REVIEW dispute
        assertThat(counts).anyMatch(row ->
                row[0] == DisputeStatus.UNDER_REVIEW && ((Long) row[1]) >= 1);
    }
}
