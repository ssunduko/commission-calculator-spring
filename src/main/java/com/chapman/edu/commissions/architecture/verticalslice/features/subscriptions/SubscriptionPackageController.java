package com.chapman.edu.commissions.architecture.verticalslice.features.subscriptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/subscription-packages")
public class SubscriptionPackageController {

    private final SubscriptionPackageService packageService;

    public SubscriptionPackageController(SubscriptionPackageService packageService) {
        this.packageService = packageService;
    }

    @GetMapping
    public ResponseEntity<List<SubscriptionPackageResponse>> listPackages() {
        return ResponseEntity.ok(packageService.listActivePackages());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionPackageResponse> getPackage(@PathVariable String id) {
        return ResponseEntity.ok(packageService.getPackage(id));
    }
}
