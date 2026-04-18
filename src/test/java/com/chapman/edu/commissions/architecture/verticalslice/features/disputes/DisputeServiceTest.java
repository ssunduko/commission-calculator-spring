package com.chapman.edu.commissions.architecture.verticalslice.features.disputes;

import com.chapman.edu.commissions.architecture.verticalslice.domain.Dispute;
import com.chapman.edu.commissions.architecture.verticalslice.domain.DisputePriority;
import com.chapman.edu.commissions.architecture.verticalslice.domain.DisputeStatus;
import com.chapman.edu.commissions.architecture.verticalslice.features.disputes.*;
import com.chapman.edu.commissions.architecture.verticalslice.infrastructure.exceptions.ResourceNotFoundException;
import com.chapman.edu.commissions.architecture.verticalslice.infrastructure.exceptions.ValidationException;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DisputeServiceTest {

    @Mock
    private DisputeRepository disputeRepository;

    @InjectMocks
    private DisputeService disputeService;

    private Dispute testDispute;

    @BeforeEach
    void setUp() {
        testDispute = new Dispute("CALC001", "REP001", "Test Dispute", "Test Description");
        testDispute.setId("1");
    }

    @Test
    void createDispute_WithValidRequest_ShouldReturnDisputeResponse() {
        // Given
        CreateDisputeRequest request = new CreateDisputeRequest(
                "CALC002",
                "REP002",
                "Commission Rate Dispute",
                "The commission rate applied is incorrect",
                null
        );
        Dispute savedDispute = new Dispute("CALC002", "REP002", "Commission Rate Dispute", "The commission rate applied is incorrect");
        savedDispute.setId("2");

        when(disputeRepository.save(any(Dispute.class))).thenReturn(savedDispute);

        // When
        DisputeResponse response = disputeService.createDispute(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo("2");
        assertThat(response.title()).isEqualTo("Commission Rate Dispute");
        assertThat(response.calculationId()).isEqualTo("CALC002");
        assertThat(response.salesRepId()).isEqualTo("REP002");
        assertThat(response.priority()).isEqualTo(DisputePriority.MEDIUM);
        verify(disputeRepository, times(1)).save(any(Dispute.class));
    }

    @Test
    void createDispute_WithExplicitPriority_ShouldPersistPriority() {
        // Given
        CreateDisputeRequest request = new CreateDisputeRequest(
                "CALC002",
                "REP002",
                "Urgent Issue",
                "This is time-critical",
                DisputePriority.URGENT
        );
        when(disputeRepository.save(any(Dispute.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        DisputeResponse response = disputeService.createDispute(request);

        // Then
        assertThat(response.priority()).isEqualTo(DisputePriority.URGENT);
        verify(disputeRepository, times(1)).save(argThat(d -> d.getPriority() == DisputePriority.URGENT));
    }

    @Test
    void getDisputesByPriority_ShouldReturnFilteredDisputes() {
        // Given
        testDispute.setPriority(DisputePriority.HIGH);
        when(disputeRepository.findByPriority(DisputePriority.HIGH)).thenReturn(Arrays.asList(testDispute));

        // When
        List<DisputeResponse> responses = disputeService.getDisputesByPriority(DisputePriority.HIGH);

        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).priority()).isEqualTo(DisputePriority.HIGH);
        verify(disputeRepository, times(1)).findByPriority(DisputePriority.HIGH);
    }

    @Test
    void getDispute_WithExistingId_ShouldReturnDisputeResponse() {
        // Given
        when(disputeRepository.findById("1")).thenReturn(Optional.of(testDispute));

        // When
        DisputeResponse response = disputeService.getDispute("1");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo("1");
        assertThat(response.title()).isEqualTo("Test Dispute");
        verify(disputeRepository, times(1)).findById("1");
    }

    @Test
    void getDispute_WithNonExistingId_ShouldThrowResourceNotFoundException() {
        // Given
        when(disputeRepository.findById("999")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> disputeService.getDispute("999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Dispute")
                .hasMessageContaining("999");
        verify(disputeRepository, times(1)).findById("999");
    }

    @Test
    void getAllDisputes_ShouldReturnAllDisputes() {
        // Given
        Dispute dispute2 = new Dispute("CALC003", "REP002", "Another Dispute", "Another Description");
        dispute2.setId("2");
        when(disputeRepository.findAll()).thenReturn(Arrays.asList(testDispute, dispute2));

        // When
        List<DisputeResponse> responses = disputeService.getAllDisputes();

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).id()).isEqualTo("1");
        assertThat(responses.get(1).id()).isEqualTo("2");
        verify(disputeRepository, times(1)).findAll();
    }

    @Test
    void getDisputesBySalesRep_ShouldReturnFilteredDisputes() {
        // Given
        when(disputeRepository.findBySalesRepId("REP001")).thenReturn(Arrays.asList(testDispute));

        // When
        List<DisputeResponse> responses = disputeService.getDisputesBySalesRep("REP001");

        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).salesRepId()).isEqualTo("REP001");
        verify(disputeRepository, times(1)).findBySalesRepId("REP001");
    }

    @Test
    void getDisputesByStatus_ShouldReturnFilteredDisputes() {
        // Given
        testDispute.setStatus(DisputeStatus.UNDER_REVIEW);
        when(disputeRepository.findByStatus(DisputeStatus.UNDER_REVIEW)).thenReturn(Arrays.asList(testDispute));

        // When
        List<DisputeResponse> responses = disputeService.getDisputesByStatus(DisputeStatus.UNDER_REVIEW);

        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).status()).isEqualTo(DisputeStatus.UNDER_REVIEW);
        verify(disputeRepository, times(1)).findByStatus(DisputeStatus.UNDER_REVIEW);
    }

    @Test
    void resolveDispute_WithValidRequest_ShouldResolveDispute() {
        // Given
        ResolveDisputeRequest request = new ResolveDisputeRequest(
                "Dispute has been reviewed and approved",
                "MANAGER001",
                true
        );
        when(disputeRepository.findById("1")).thenReturn(Optional.of(testDispute));
        when(disputeRepository.save(any(Dispute.class))).thenReturn(testDispute);

        // When
        DisputeResponse response = disputeService.resolveDispute("1", request);

        // Then
        assertThat(response).isNotNull();
        assertThat(testDispute.getStatus()).isEqualTo(DisputeStatus.APPROVED);
        assertThat(testDispute.getResolution()).isEqualTo("Dispute has been reviewed and approved");
        assertThat(testDispute.getResolvedBy()).isEqualTo("MANAGER001");
        verify(disputeRepository, times(1)).findById("1");
        verify(disputeRepository, times(1)).save(testDispute);
    }

    @Test
    void resolveDispute_WhenApprovedFalse_ShouldRejectDispute() {
        // Given
        ResolveDisputeRequest request = new ResolveDisputeRequest(
                "Dispute has been reviewed and rejected",
                "MANAGER001",
                false
        );
        when(disputeRepository.findById("1")).thenReturn(Optional.of(testDispute));
        when(disputeRepository.save(any(Dispute.class))).thenReturn(testDispute);

        // When
        DisputeResponse response = disputeService.resolveDispute("1", request);

        // Then
        assertThat(response).isNotNull();
        assertThat(testDispute.getStatus()).isEqualTo(DisputeStatus.REJECTED);
        verify(disputeRepository, times(1)).save(testDispute);
    }

    @Test
    void resolveDispute_WithAlreadyResolvedDispute_ShouldThrowValidationException() {
        // Given
        testDispute.setStatus(DisputeStatus.RESOLVED);
        ResolveDisputeRequest request = new ResolveDisputeRequest(
                "Trying to resolve again",
                "MANAGER001",
                true
        );
        when(disputeRepository.findById("1")).thenReturn(Optional.of(testDispute));

        // When & Then
        assertThatThrownBy(() -> disputeService.resolveDispute("1", request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("already resolved");
        verify(disputeRepository, times(1)).findById("1");
        verify(disputeRepository, never()).save(any(Dispute.class));
    }

    @Test
    void resolveDispute_WithAlreadyApprovedDispute_ShouldThrowValidationException() {
        // Given
        testDispute.setStatus(DisputeStatus.APPROVED);
        ResolveDisputeRequest request = new ResolveDisputeRequest(
                "Trying to resolve again",
                "MANAGER001",
                true
        );
        when(disputeRepository.findById("1")).thenReturn(Optional.of(testDispute));

        // When & Then
        assertThatThrownBy(() -> disputeService.resolveDispute("1", request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("already resolved");
        verify(disputeRepository, never()).save(any(Dispute.class));
    }

    @Test
    void resolveDispute_WithNonExistingId_ShouldThrowResourceNotFoundException() {
        // Given
        ResolveDisputeRequest request = new ResolveDisputeRequest(
                "Resolution",
                "MANAGER001",
                true
        );
        when(disputeRepository.findById("999")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> disputeService.resolveDispute("999", request))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(disputeRepository, times(1)).findById("999");
    }

    @Test
    void escalateDispute_WithValidId_ShouldEscalateDispute() {
        // Given
        when(disputeRepository.findById("1")).thenReturn(Optional.of(testDispute));
        when(disputeRepository.save(any(Dispute.class))).thenReturn(testDispute);

        // When
        DisputeResponse response = disputeService.escalateDispute("1");

        // Then
        assertThat(response).isNotNull();
        assertThat(testDispute.isEscalated()).isTrue();
        assertThat(testDispute.getStatus()).isEqualTo(DisputeStatus.ESCALATED);
        verify(disputeRepository, times(1)).findById("1");
        verify(disputeRepository, times(1)).save(testDispute);
    }

    @Test
    void escalateDispute_WithAlreadyEscalatedDispute_ShouldThrowValidationException() {
        // Given
        testDispute.setEscalated(true);
        when(disputeRepository.findById("1")).thenReturn(Optional.of(testDispute));

        // When & Then
        assertThatThrownBy(() -> disputeService.escalateDispute("1"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("already escalated");
        verify(disputeRepository, times(1)).findById("1");
        verify(disputeRepository, never()).save(any(Dispute.class));
    }

    @Test
    void escalateDispute_WithNonExistingId_ShouldThrowResourceNotFoundException() {
        // Given
        when(disputeRepository.findById("999")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> disputeService.escalateDispute("999"))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(disputeRepository, times(1)).findById("999");
    }

    @Test
    void deleteDispute_WithExistingId_ShouldDeleteDispute() {
        // Given
        when(disputeRepository.existsById("1")).thenReturn(true);

        // When
        disputeService.deleteDispute("1");

        // Then
        verify(disputeRepository, times(1)).existsById("1");
        verify(disputeRepository, times(1)).deleteById("1");
    }

    @Test
    void deleteDispute_WithNonExistingId_ShouldThrowResourceNotFoundException() {
        // Given
        when(disputeRepository.existsById("999")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> disputeService.deleteDispute("999"))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(disputeRepository, times(1)).existsById("999");
        verify(disputeRepository, never()).deleteById(anyString());
    }
}
