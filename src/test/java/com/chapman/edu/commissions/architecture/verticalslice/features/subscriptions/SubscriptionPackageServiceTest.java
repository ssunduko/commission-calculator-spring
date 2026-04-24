package com.chapman.edu.commissions.architecture.verticalslice.features.subscriptions;

import com.chapman.edu.commissions.architecture.verticalslice.domain.PackageTier;
import com.chapman.edu.commissions.architecture.verticalslice.domain.SubscriptionPackage;
import com.chapman.edu.commissions.architecture.verticalslice.infrastructure.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionPackageServiceTest {

    @Mock
    private SubscriptionPackageRepository packageRepository;

    @InjectMocks
    private SubscriptionPackageService service;

    @Test
    void listActivePackages_returnsResponsesSortedByRepository() {
        SubscriptionPackage basic = new SubscriptionPackage("BASIC", "Starter", "solo", new BigDecimal("19"), 1, 50, PackageTier.BASIC);
        basic.setId("pkg-basic");
        SubscriptionPackage pro = new SubscriptionPackage("PROFESSIONAL", "Pro", "teams", new BigDecimal("79"), 10, 500, PackageTier.PROFESSIONAL);
        pro.setId("pkg-pro");
        when(packageRepository.findByActiveTrueOrderByMonthlyPriceAsc()).thenReturn(List.of(basic, pro));

        List<SubscriptionPackageResponse> result = service.listActivePackages();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).code()).isEqualTo("BASIC");
        assertThat(result.get(0).monthlyPrice()).isEqualByComparingTo("19");
        assertThat(result.get(1).code()).isEqualTo("PROFESSIONAL");
    }

    @Test
    void getPackageByCode_returnsResponse() {
        SubscriptionPackage pro = new SubscriptionPackage("PROFESSIONAL", "Pro", "teams", new BigDecimal("79"), 10, 500, PackageTier.PROFESSIONAL);
        pro.setId("pkg-pro");
        when(packageRepository.findByCode("PROFESSIONAL")).thenReturn(Optional.of(pro));

        SubscriptionPackageResponse response = service.getPackageByCode("PROFESSIONAL");

        assertThat(response.code()).isEqualTo("PROFESSIONAL");
        assertThat(response.name()).isEqualTo("Pro");
        assertThat(response.tier()).isEqualTo(PackageTier.PROFESSIONAL);
    }

    @Test
    void getPackage_withMissingId_throwsResourceNotFoundException() {
        when(packageRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPackage("missing"))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("SubscriptionPackage")
            .hasMessageContaining("missing");
    }
}
