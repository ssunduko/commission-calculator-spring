package com.chapman.edu.commissions.ai.ml;

import com.chapman.edu.commissions.ai.service.prompt.PromptTemplateService;
import com.chapman.edu.commissions.ai.service.ml.ForecastingService;
import com.chapman.edu.commissions.orm.entity.*;
import com.chapman.edu.commissions.orm.repository.CommissionCalculationRepository;
import com.chapman.edu.commissions.orm.repository.DealRepository;
import com.chapman.edu.commissions.orm.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ForecastingService — Unit Tests")
class ForecastingServiceTest {

    @Mock(answer = org.mockito.Answers.RETURNS_DEEP_STUBS)
    private ChatClient chatClient;

    @Mock
    private PromptTemplateService promptTemplateService;

    @Mock
    private CommissionCalculationRepository calculationRepository;

    @Mock
    private DealRepository dealRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ForecastingService forecastingService;

    private User testUser;
    private List<CommissionCalculation> historicalCalcs;
    private List<Deal> openDeals;
    private List<Deal> wonDeals;

    @BeforeEach
    void setUp() {
        testUser = new User("alice", "alice@test.com", "Alice", "Johnson");
        testUser.setId("user-001");

        Deal wonDeal = new Deal("Enterprise License", new BigDecimal("150000"), testUser);
        wonDeal.setId("deal-001");
        wonDeal.setStatus(DealStatus.WON);

        Deal openDeal = new Deal("MegaCorp Transformation", new BigDecimal("500000"), testUser);
        openDeal.setId("deal-002");
        openDeal.setStatus(DealStatus.OPEN);

        CommissionCalculation calc = new CommissionCalculation(wonDeal, testUser, new BigDecimal("18000"));
        calc.setId("calc-001");
        calc.setNetCommission(new BigDecimal("19800"));
        calc.setStatus(CommissionStatus.APPROVED);

        historicalCalcs = List.of(calc);
        openDeals = List.of(openDeal);
        wonDeals = List.of(wonDeal);
    }

    @Nested
    @DisplayName("forecastCommissions")
    class ForecastCommissions {

        @Test
        @DisplayName("should return error when user not found")
        void shouldReturnErrorWhenUserNotFound() {
            when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());

            String result = forecastingService.forecastCommissions("nonexistent");

            assertThat(result).isEqualTo("User not found with ID: nonexistent");
            verifyNoInteractions(chatClient);
        }

        @Test
        @DisplayName("should generate forecast with historical data and pipeline")
        void shouldGenerateForecastWithData() {
            when(userRepository.findById("user-001")).thenReturn(Optional.of(testUser));
            when(calculationRepository.findBySalesRepId("user-001")).thenReturn(historicalCalcs);
            when(dealRepository.findByStatus(DealStatus.OPEN)).thenReturn(openDeals);
            when(promptTemplateService.createForecastPrompt(
                    anyString(), anyString(), anyString()
            )).thenReturn(new Prompt("forecast prompt"));
            when(chatClient.prompt(any(Prompt.class)).call().content())
                    .thenReturn("Based on historical performance, projected Q2 commission: $45,000");

            String result = forecastingService.forecastCommissions("user-001");

            assertThat(result).contains("$45,000");

            ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> histCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> pipeCaptor = ArgumentCaptor.forClass(String.class);

            verify(promptTemplateService).createForecastPrompt(
                    nameCaptor.capture(), histCaptor.capture(), pipeCaptor.capture()
            );

            assertThat(nameCaptor.getValue()).isEqualTo("Alice Johnson");
            assertThat(histCaptor.getValue()).contains("$19800");
            assertThat(pipeCaptor.getValue()).contains("MegaCorp Transformation");
        }

        @Test
        @DisplayName("should handle user with no historical data")
        void shouldHandleNoHistoricalData() {
            when(userRepository.findById("user-001")).thenReturn(Optional.of(testUser));
            when(calculationRepository.findBySalesRepId("user-001")).thenReturn(Collections.emptyList());
            when(dealRepository.findByStatus(DealStatus.OPEN)).thenReturn(Collections.emptyList());
            when(promptTemplateService.createForecastPrompt(
                    anyString(), anyString(), anyString()
            )).thenReturn(new Prompt("forecast prompt"));
            when(chatClient.prompt(any(Prompt.class)).call().content())
                    .thenReturn("Insufficient data for forecast");

            String result = forecastingService.forecastCommissions("user-001");

            ArgumentCaptor<String> histCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> pipeCaptor = ArgumentCaptor.forClass(String.class);
            verify(promptTemplateService).createForecastPrompt(
                    anyString(), histCaptor.capture(), pipeCaptor.capture()
            );
            assertThat(histCaptor.getValue()).contains("No historical commission data available");
            assertThat(pipeCaptor.getValue()).contains("No open deals in the pipeline");
        }
    }

    @Nested
    @DisplayName("forecastTeamCommissions")
    class ForecastTeamCommissions {

        @Test
        @DisplayName("should generate team forecast aggregating all data")
        void shouldGenerateTeamForecast() {
            when(calculationRepository.findAll()).thenReturn(historicalCalcs);
            when(dealRepository.findByStatus(DealStatus.OPEN)).thenReturn(openDeals);
            when(dealRepository.findByStatus(DealStatus.WON)).thenReturn(wonDeals);
            when(chatClient.prompt().system(anyString()).user(anyString()).call().content())
                    .thenReturn("Team forecast: $120,000 projected for next quarter");

            String result = forecastingService.forecastTeamCommissions();

            assertThat(result).contains("$120,000");
        }

        @Test
        @DisplayName("should handle empty data gracefully")
        void shouldHandleEmptyData() {
            when(calculationRepository.findAll()).thenReturn(Collections.emptyList());
            when(dealRepository.findByStatus(DealStatus.OPEN)).thenReturn(Collections.emptyList());
            when(dealRepository.findByStatus(DealStatus.WON)).thenReturn(Collections.emptyList());
            when(chatClient.prompt().system(anyString()).user(anyString()).call().content())
                    .thenReturn("No data available for team forecast");

            String result = forecastingService.forecastTeamCommissions();

            assertThat(result).isNotNull();
        }
    }
}
