package com.chapman.edu.commissions.architecture.verticalslice.features.subscriptions;

import com.chapman.edu.commissions.architecture.verticalslice.domain.SubscriptionPackage;
import com.chapman.edu.commissions.architecture.verticalslice.infrastructure.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubscriptionPackageService {

    private final SubscriptionPackageRepository packageRepository;

    public SubscriptionPackageService(SubscriptionPackageRepository packageRepository) {
        this.packageRepository = packageRepository;
    }

    public List<SubscriptionPackageResponse> listActivePackages() {
        return packageRepository.findByActiveTrueOrderByMonthlyPriceAsc().stream()
            .map(SubscriptionPackageResponse::from)
            .collect(Collectors.toList());
    }

    public SubscriptionPackageResponse getPackage(String id) {
        SubscriptionPackage pkg = packageRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("SubscriptionPackage", id));
        return SubscriptionPackageResponse.from(pkg);
    }

    public SubscriptionPackageResponse getPackageByCode(String code) {
        SubscriptionPackage pkg = packageRepository.findByCode(code)
            .orElseThrow(() -> new ResourceNotFoundException("SubscriptionPackage", code));
        return SubscriptionPackageResponse.from(pkg);
    }
}
