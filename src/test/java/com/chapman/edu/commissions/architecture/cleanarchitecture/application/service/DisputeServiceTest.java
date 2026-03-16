package com.chapman.edu.commissions.architecture.cleanarchitecture.application.service;

import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.CreateDisputeCommand;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.DisputeResult;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.ResolveDisputeCommand;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.port.out.DisputeRepositoryPort;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.exception.DomainException;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.exception.EntityNotFoundException;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.Dispute;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.DisputeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DisputeServiceTest {

    @Mock
    private DisputeRepositoryPort disputeRepository;

    @InjectMocks
    private DisputeService disputeService;

    private Dispute testDispute;

    @BeforeEach
    void setUp() {
        testDispute = new Dispute("CALC001", "REP001", "Test Dispute", "Test Description");
        testDispute.setId("DISPUTE001");
    }

    @Test
    @DisplayName("createDispute should create and return a new dispute")
    void createDispute_ValidCommand_ShouldReturnDisputeResult() {
        // Given
        CreateDisputeCommand command = new CreateDisputeCommand(
                "CALC002", "REP002", "Commission Rate Dispute", "The commission rate applied is incorrect"
        );
        Dispute savedDispute = new Dispute("CALC002", "REP002", "Commission Rate Dispute", "The commission rate applied is incorrect");
        savedDispute.setId("DISPUTE002");

        when(disputeRepository.save(any(Dispute.class))).thenReturn(savedDispute);

        // When
        DisputeResult result = disputeService.createDispute(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("DISPUTE002");
        assertThat(result.calculationId()).isEqualTo("CALC002");
        assertThat(result.salesRepId()).isEqualTo("REP002");
        assertThat(result.title()).isEqualTo("Commission Rate Dispute");
        assertThat(result.status()).isEqualTo("INITIATED");
        verify(disputeRepository).save(any(Dispute.class));
    }

    @Test
    @DisplayName("getDispute should return dispute when it exists")
    void getDispute_ExistingId_ShouldReturnDisputeResult() {
        // Given
        when(disputeRepository.findById("DISPUTE001")).thenReturn(Optional.of(testDispute));

        // When
        DisputeResult result = disputeService.getDispute("DISPUTE001");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("DISPUTE001");
        assertThat(result.title()).isEqualTo("Test Dispute");
        verify(disputeRepository).findById("DISPUTE001");
    }

    @Test
    @DisplayName("getDispute should throw EntityNotFoundException when dispute not found")
    void getDispute_NonExistingId_ShouldThrowEntityNotFoundException() {
        // Given
        when(disputeRepository.findById("MISSING")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> disputeService.getDispute("MISSING"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Dispute")
                .hasMessageContaining("MISSING");
        verify(disputeRepository).findById("MISSING");
    }

    @Test
    @DisplayName("getAllDisputes should return all disputes")
    void getAllDisputes_ShouldReturnAllDisputes() {
        // Given
        Dispute dispute2 = new Dispute("CALC003", "REP002", "Another Dispute", "Another Description");
        dispute2.setId("DISPUTE002");
        when(disputeRepository.findAll()).thenReturn(Arrays.asList(testDispute, dispute2));

        // When
        List<DisputeResult> results = disputeService.getAllDisputes();

        // Then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).id()).isEqualTo("DISPUTE001");
        assertThat(results.get(1).id()).isEqualTo("DISPUTE002");
        verify(disputeRepository).findAll();
    }

    @Test
    @DisplayName("getAllDisputes should return empty list when no disputes exist")
    void getAllDisputes_Empty_ShouldReturnEmptyList() {
        // Given
        when(disputeRepository.findAll()).thenReturn(List.of());

        // When
        List<DisputeResult> results = disputeService.getAllDisputes();

        // Then
        assertThat(results).isEmpty();
        verify(disputeRepository).findAll();
    }

    @Test
    @DisplayName("getDisputesBySalesRep should return filtered disputes")
    void getDisputesBySalesRep_ShouldReturnFilteredDisputes() {
        // Given
        when(disputeRepository.findBySalesRepId("REP001")).thenReturn(List.of(testDispute));

        // When
        List<DisputeResult> results = disputeService.getDisputesBySalesRep("REP001");

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).salesRepId()).isEqualTo("REP001");
        verify(disputeRepository).findBySalesRepId("REP001");
    }

    @Test
    @DisplayName("getDisputesByStatus should return filtered disputes")
    void getDisputesByStatus_ShouldReturnFilteredDisputes() {
        // Given
        when(disputeRepository.findByStatus(DisputeStatus.INITIATED)).thenReturn(List.of(testDispute));

        // When
        List<DisputeResult> results = disputeService.getDisputesByStatus(DisputeStatus.INITIATED);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).status()).isEqualTo("INITIATED");
        verify(disputeRepository).findByStatus(DisputeStatus.INITIATED);
    }

    @Test
    @DisplayName("getDisputesByStatus should return empty list when no disputes match")
    void getDisputesByStatus_NoResults_ShouldReturnEmptyList() {
        // Given
        when(disputeRepository.findByStatus(DisputeStatus.ESCALATED)).thenReturn(List.of());

        // When
        List<DisputeResult> results = disputeService.getDisputesByStatus(DisputeStatus.ESCALATED);

        // Then
        assertThat(results).isEmpty();
        verify(disputeRepository).findByStatus(DisputeStatus.ESCALATED);
    }

    @Test
    @DisplayName("resolveDispute should approve dispute when approved is true")
    void resolveDispute_Approve_ShouldSetStatusApproved() {
        // Given
        ResolveDisputeCommand command = new ResolveDisputeCommand(
                "Dispute has been reviewed and approved", "MANAGER001", true
        );
        when(disputeRepository.findById("DISPUTE001")).thenReturn(Optional.of(testDispute));
        when(disputeRepository.save(any(Dispute.class))).thenReturn(testDispute);

        // When
        DisputeResult result = disputeService.resolveDispute("DISPUTE001", command);

        // Then
        assertThat(result).isNotNull();
        assertThat(testDispute.getStatus()).isEqualTo(DisputeStatus.APPROVED);
        assertThat(testDispute.getResolution()).isEqualTo("Dispute has been reviewed and approved");
        assertThat(testDispute.getResolvedBy()).isEqualTo("MANAGER001");
        verify(disputeRepository).findById("DISPUTE001");
        verify(disputeRepository).save(testDispute);
    }

    @Test
    @DisplayName("resolveDispute should reject dispute when approved is false")
    void resolveDispute_Reject_ShouldSetStatusRejected() {
        // Given
        ResolveDisputeCommand command = new ResolveDisputeCommand(
                "Dispute has been reviewed and rejected", "MANAGER001", false
        );
        when(disputeRepository.findById("DISPUTE001")).thenReturn(Optional.of(testDispute));
        when(disputeRepository.save(any(Dispute.class))).thenReturn(testDispute);

        // When
        DisputeResult result = disputeService.resolveDispute("DISPUTE001", command);

        // Then
        assertThat(result).isNotNull();
        assertThat(testDispute.getStatus()).isEqualTo(DisputeStatus.REJECTED);
        verify(disputeRepository).save(testDispute);
    }

    @Test
    @DisplayName("resolveDispute should throw DomainException when dispute is already approved")
    void resolveDispute_AlreadyApproved_ShouldThrowDomainException() {
        // Given
        testDispute.setStatus(DisputeStatus.APPROVED);
        ResolveDisputeCommand command = new ResolveDisputeCommand(
                "Trying to resolve again", "MANAGER001", true
        );
        when(disputeRepository.findById("DISPUTE001")).thenReturn(Optional.of(testDispute));

        // When & Then
        assertThatThrownBy(() -> disputeService.resolveDispute("DISPUTE001", command))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("already been resolved");
        verify(disputeRepository).findById("DISPUTE001");
        verify(disputeRepository, never()).save(any(Dispute.class));
    }

    @Test
    @DisplayName("resolveDispute should throw DomainException when dispute is already rejected")
    void resolveDispute_AlreadyRejected_ShouldThrowDomainException() {
        // Given
        testDispute.setStatus(DisputeStatus.REJECTED);
        ResolveDisputeCommand command = new ResolveDisputeCommand(
                "Trying to resolve again", "MANAGER001", true
        );
        when(disputeRepository.findById("DISPUTE001")).thenReturn(Optional.of(testDispute));

        // When & Then
        assertThatThrownBy(() -> disputeService.resolveDispute("DISPUTE001", command))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("already been resolved");
        verify(disputeRepository, never()).save(any(Dispute.class));
    }

    @Test
    @DisplayName("resolveDispute should throw EntityNotFoundException when dispute not found")
    void resolveDispute_NotFound_ShouldThrowEntityNotFoundException() {
        // Given
        ResolveDisputeCommand command = new ResolveDisputeCommand(
                "Resolution", "MANAGER001", true
        );
        when(disputeRepository.findById("MISSING")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> disputeService.resolveDispute("MISSING", command))
                .isInstanceOf(EntityNotFoundException.class);
        verify(disputeRepository).findById("MISSING");
    }

    @Test
    @DisplayName("escalateDispute should escalate dispute successfully")
    void escalateDispute_HappyPath_ShouldEscalateDispute() {
        // Given
        when(disputeRepository.findById("DISPUTE001")).thenReturn(Optional.of(testDispute));
        when(disputeRepository.save(any(Dispute.class))).thenReturn(testDispute);

        // When
        DisputeResult result = disputeService.escalateDispute("DISPUTE001");

        // Then
        assertThat(result).isNotNull();
        assertThat(testDispute.isEscalated()).isTrue();
        assertThat(testDispute.getStatus()).isEqualTo(DisputeStatus.ESCALATED);
        verify(disputeRepository).findById("DISPUTE001");
        verify(disputeRepository).save(testDispute);
    }

    @Test
    @DisplayName("escalateDispute should throw DomainException when dispute is already escalated")
    void escalateDispute_AlreadyEscalated_ShouldThrowDomainException() {
        // Given
        testDispute.setEscalated(true);
        when(disputeRepository.findById("DISPUTE001")).thenReturn(Optional.of(testDispute));

        // When & Then
        assertThatThrownBy(() -> disputeService.escalateDispute("DISPUTE001"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("already escalated");
        verify(disputeRepository).findById("DISPUTE001");
        verify(disputeRepository, never()).save(any(Dispute.class));
    }

    @Test
    @DisplayName("escalateDispute should throw DomainException when dispute is already resolved")
    void escalateDispute_AlreadyResolved_ShouldThrowDomainException() {
        // Given
        testDispute.setStatus(DisputeStatus.APPROVED);
        when(disputeRepository.findById("DISPUTE001")).thenReturn(Optional.of(testDispute));

        // When & Then
        assertThatThrownBy(() -> disputeService.escalateDispute("DISPUTE001"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("resolved");
        verify(disputeRepository).findById("DISPUTE001");
        verify(disputeRepository, never()).save(any(Dispute.class));
    }

    @Test
    @DisplayName("escalateDispute should throw EntityNotFoundException when dispute not found")
    void escalateDispute_NotFound_ShouldThrowEntityNotFoundException() {
        // Given
        when(disputeRepository.findById("MISSING")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> disputeService.escalateDispute("MISSING"))
                .isInstanceOf(EntityNotFoundException.class);
        verify(disputeRepository).findById("MISSING");
    }

    @Test
    @DisplayName("deleteDispute should delete dispute when it exists")
    void deleteDispute_ExistingId_ShouldDeleteDispute() {
        // Given
        when(disputeRepository.findById("DISPUTE001")).thenReturn(Optional.of(testDispute));

        // When
        disputeService.deleteDispute("DISPUTE001");

        // Then
        verify(disputeRepository).findById("DISPUTE001");
        verify(disputeRepository).deleteById("DISPUTE001");
    }

    @Test
    @DisplayName("deleteDispute should throw EntityNotFoundException when dispute not found")
    void deleteDispute_NonExistingId_ShouldThrowEntityNotFoundException() {
        // Given
        when(disputeRepository.findById("MISSING")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> disputeService.deleteDispute("MISSING"))
                .isInstanceOf(EntityNotFoundException.class);
        verify(disputeRepository).findById("MISSING");
        verify(disputeRepository, never()).deleteById(anyString());
    }
}
