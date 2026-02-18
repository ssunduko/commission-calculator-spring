package com.chapman.edu.commissions.ai.vectorstore;

import com.chapman.edu.commissions.ai.service.vectorstore.CommissionDocumentService;
import com.chapman.edu.commissions.orm.entity.*;
import com.chapman.edu.commissions.orm.repository.CommissionCalculationRepository;
import com.chapman.edu.commissions.orm.repository.CommissionPlanRepository;
import com.chapman.edu.commissions.orm.repository.DealRepository;
import com.chapman.edu.commissions.orm.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommissionDocumentService — Unit Tests")
class CommissionDocumentServiceTest {

    @Mock
    private VectorStore vectorStore;

    @Mock
    private DealRepository dealRepository;

    @Mock
    private CommissionPlanRepository planRepository;

    @Mock
    private CommissionCalculationRepository calculationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommissionDocumentService documentService;

    private User testUser;
    private Deal testDeal;
    private CommissionPlan testPlan;
    private CommissionCalculation testCalc;

    @BeforeEach
    void setUp() {
        testUser = new User("alice", "alice@test.com", "Alice", "Johnson");
        testUser.setId("user-001");
        testUser.addRole(UserRole.SALES_REP);
        testUser.setDepartment("Enterprise Sales");
        testUser.setTerritory("West Coast");

        testDeal = new Deal("Acme Corp License", new BigDecimal("150000"), testUser);
        testDeal.setId("deal-001");
        testDeal.setStatus(DealStatus.WON);
        testDeal.setCloseDate(LocalDate.of(2026, 1, 15));

        testPlan = new CommissionPlan("Standard Plan", Currency.getInstance("USD"));
        testPlan.setId("plan-001");
        testPlan.setStatus(PlanStatus.ACTIVE);
        testPlan.setEffectiveStartDate(LocalDate.of(2025, 1, 1));
        testPlan.setEffectiveEndDate(LocalDate.of(2026, 12, 31));

        testCalc = new CommissionCalculation(testDeal, testUser, new BigDecimal("18000"));
        testCalc.setId("calc-001");
        testCalc.setGrossCommission(new BigDecimal("19800"));
        testCalc.setNetCommission(new BigDecimal("19800"));
        testCalc.setStatus(CommissionStatus.APPROVED);
    }

    @Test
    @DisplayName("loadAllDocuments should load all entity types into vector store")
    @SuppressWarnings("unchecked")
    void loadAllDocuments_shouldLoadAllEntityTypes() {
        when(dealRepository.findAll()).thenReturn(List.of(testDeal));
        when(planRepository.findAll()).thenReturn(List.of(testPlan));
        when(planRepository.findByIdWithTiers(anyString())).thenReturn(Optional.of(testPlan));
        when(calculationRepository.findAll()).thenReturn(List.of(testCalc));
        when(userRepository.findAll()).thenReturn(List.of(testUser));

        documentService.loadAllDocuments();

        ArgumentCaptor<List<Document>> captor = ArgumentCaptor.forClass(List.class);
        verify(vectorStore).add(captor.capture());

        List<Document> documents = captor.getValue();
        // Should have 1 deal + 1 plan + 1 calc + 1 user = 4 documents
        assertThat(documents).hasSize(4);
    }

    @Test
    @DisplayName("loadAllDocuments should create deal documents with correct content and metadata")
    @SuppressWarnings("unchecked")
    void loadAllDocuments_shouldCreateDealDocumentsCorrectly() {
        when(dealRepository.findAll()).thenReturn(List.of(testDeal));
        when(planRepository.findAll()).thenReturn(Collections.emptyList());
        when(calculationRepository.findAll()).thenReturn(Collections.emptyList());
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        documentService.loadAllDocuments();

        ArgumentCaptor<List<Document>> captor = ArgumentCaptor.forClass(List.class);
        verify(vectorStore).add(captor.capture());

        List<Document> documents = captor.getValue();
        assertThat(documents).hasSize(1);

        Document dealDoc = documents.get(0);
        assertThat(dealDoc.getText())
                .contains("Acme Corp License")
                .contains("150000")
                .contains("Won")
                .contains("Closed on");
        assertThat(dealDoc.getMetadata())
                .containsEntry("type", "deal")
                .containsEntry("entityId", "deal-001")
                .containsEntry("status", "WON");
    }

    @Test
    @DisplayName("loadAllDocuments should create user documents with correct content and metadata")
    @SuppressWarnings("unchecked")
    void loadAllDocuments_shouldCreateUserDocumentsCorrectly() {
        when(dealRepository.findAll()).thenReturn(Collections.emptyList());
        when(planRepository.findAll()).thenReturn(Collections.emptyList());
        when(calculationRepository.findAll()).thenReturn(Collections.emptyList());
        when(userRepository.findAll()).thenReturn(List.of(testUser));

        documentService.loadAllDocuments();

        ArgumentCaptor<List<Document>> captor = ArgumentCaptor.forClass(List.class);
        verify(vectorStore).add(captor.capture());

        List<Document> documents = captor.getValue();
        assertThat(documents).hasSize(1);

        Document userDoc = documents.get(0);
        assertThat(userDoc.getText())
                .contains("Alice")
                .contains("Johnson")
                .contains("Enterprise Sales")
                .contains("West Coast");
        assertThat(userDoc.getMetadata())
                .containsEntry("type", "user")
                .containsEntry("entityId", "user-001")
                .containsEntry("username", "alice");
    }

    @Test
    @DisplayName("loadAllDocuments should create plan documents with tier information")
    @SuppressWarnings("unchecked")
    void loadAllDocuments_shouldCreatePlanDocumentsWithTiers() {
        CommissionTier tier = new CommissionTier("Starter", BigDecimal.ZERO,
                new BigDecimal("25000"), new BigDecimal("5"));
        testPlan.addTier(tier);

        when(dealRepository.findAll()).thenReturn(Collections.emptyList());
        when(planRepository.findAll()).thenReturn(List.of(testPlan));
        when(planRepository.findByIdWithTiers("plan-001")).thenReturn(Optional.of(testPlan));
        when(calculationRepository.findAll()).thenReturn(Collections.emptyList());
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        documentService.loadAllDocuments();

        ArgumentCaptor<List<Document>> captor = ArgumentCaptor.forClass(List.class);
        verify(vectorStore).add(captor.capture());

        List<Document> documents = captor.getValue();
        assertThat(documents).hasSize(1);

        Document planDoc = documents.get(0);
        assertThat(planDoc.getText())
                .contains("Standard Plan")
                .contains("Starter")
                .contains("5%");
        assertThat(planDoc.getMetadata())
                .containsEntry("type", "commission_plan")
                .containsEntry("planName", "Standard Plan");
    }

    @Test
    @DisplayName("loadAllDocuments should not add documents when all repositories are empty")
    void loadAllDocuments_shouldNotAddWhenEmpty() {
        when(dealRepository.findAll()).thenReturn(Collections.emptyList());
        when(planRepository.findAll()).thenReturn(Collections.emptyList());
        when(calculationRepository.findAll()).thenReturn(Collections.emptyList());
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        documentService.loadAllDocuments();

        verify(vectorStore, never()).add(anyList());
    }

    @Test
    @DisplayName("loadAllDocuments should create calculation documents with correct amounts")
    @SuppressWarnings("unchecked")
    void loadAllDocuments_shouldCreateCalculationDocumentsCorrectly() {
        when(dealRepository.findAll()).thenReturn(Collections.emptyList());
        when(planRepository.findAll()).thenReturn(Collections.emptyList());
        when(calculationRepository.findAll()).thenReturn(List.of(testCalc));
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        documentService.loadAllDocuments();

        ArgumentCaptor<List<Document>> captor = ArgumentCaptor.forClass(List.class);
        verify(vectorStore).add(captor.capture());

        List<Document> documents = captor.getValue();
        Document calcDoc = documents.get(0);
        assertThat(calcDoc.getText())
                .contains("18000")
                .contains("19800")
                .contains("Approved");
        assertThat(calcDoc.getMetadata())
                .containsEntry("type", "commission_calculation")
                .containsEntry("entityId", "calc-001");
    }
}
