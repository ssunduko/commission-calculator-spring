import { CommissionCalculationEngine } from "./calculator"
import type { Deal, CommissionPlan, CalculationContext, OrganizationSettings } from "./types"

export class CommissionEngineTestSuite {
  private engine: CommissionCalculationEngine
  private testResults: Array<{ name: string; passed: boolean; error?: string }> = []

  constructor() {
    this.engine = new CommissionCalculationEngine()
  }

  async runAllTests(): Promise<void> {
    console.log("🧪 Starting Commission Engine Test Suite...")

    await this.testBasicCommissionCalculation()
    await this.testTieredCommissionCalculation()
    await this.testBonusCalculation()
    await this.testAcceleratorCalculation()
    await this.testDeceleratorCalculation()
    await this.testCurrencyConversion()
    await this.testRefundProcessing()
    await this.testDealCancellation()
    await this.testAdminOverride()
    await this.testRoundingRules()
    await this.testTaxCalculation()
    await this.testErrorHandling()
    await this.testAuditTrail()

    this.printTestResults()
  }

  private async testBasicCommissionCalculation(): Promise<void> {
    try {
      const deal = this.createTestDeal({
        value: 10000,
        dealType: "new_business",
      })

      const plan = this.createTestPlan({
        rules: [
          {
            id: "base-rule",
            name: "Base Commission",
            type: "base_rate",
            conditions: [],
            rate: 10,
            rateType: "percentage",
            priority: 1,
            active: true,
          },
        ],
      })

      const context = this.createTestContext()
      const result = await this.engine.calculateCommission(deal, plan, context)

      if (result.success && result.calculation?.baseCommission === 1000) {
        this.testResults.push({ name: "Basic Commission Calculation", passed: true })
      } else {
        throw new Error(`Expected 1000, got ${result.calculation?.baseCommission}`)
      }
    } catch (error) {
      this.testResults.push({
        name: "Basic Commission Calculation",
        passed: false,
        error: error instanceof Error ? error.message : "Unknown error",
      })
    }
  }

  private async testTieredCommissionCalculation(): Promise<void> {
    try {
      const deal = this.createTestDeal({ value: 15000 })
      const plan = this.createTestPlan({
        tiers: [
          { id: "tier1", name: "Tier 1", minThreshold: 0, maxThreshold: 10000, rate: 5, rateType: "percentage" },
          { id: "tier2", name: "Tier 2", minThreshold: 10000, maxThreshold: 20000, rate: 8, rateType: "percentage" },
        ],
      })

      const context = this.createTestContext()
      const result = await this.engine.calculateCommission(deal, plan, context)

      // Expected: (10000 * 5%) + (5000 * 8%) = 500 + 400 = 900
      if (result.success && result.calculation?.baseCommission === 900) {
        this.testResults.push({ name: "Tiered Commission Calculation", passed: true })
      } else {
        throw new Error(`Expected 900, got ${result.calculation?.baseCommission}`)
      }
    } catch (error) {
      this.testResults.push({
        name: "Tiered Commission Calculation",
        passed: false,
        error: error instanceof Error ? error.message : "Unknown error",
      })
    }
  }

  private async testBonusCalculation(): Promise<void> {
    try {
      const deal = this.createTestDeal({ value: 10000 })
      const plan = this.createTestPlan({
        rules: [
          {
            id: "base-rule",
            name: "Base Commission",
            type: "base_rate",
            conditions: [],
            rate: 10,
            rateType: "percentage",
            priority: 1,
            active: true,
          },
        ],
        bonuses: [
          {
            id: "spif-bonus",
            name: "SPIF Bonus",
            type: "spif",
            conditions: [],
            amount: 500,
            amountType: "fixed",
            frequency: "per_deal",
          },
        ],
      })

      const context = this.createTestContext()
      const result = await this.engine.calculateCommission(deal, plan, context)

      if (
        result.success &&
        result.calculation?.baseCommission === 1000 &&
        result.calculation?.bonuses.length === 1 &&
        result.calculation?.bonuses[0].amount === 500
      ) {
        this.testResults.push({ name: "Bonus Calculation", passed: true })
      } else {
        throw new Error("Bonus calculation failed")
      }
    } catch (error) {
      this.testResults.push({
        name: "Bonus Calculation",
        passed: false,
        error: error instanceof Error ? error.message : "Unknown error",
      })
    }
  }

  private async testAcceleratorCalculation(): Promise<void> {
    try {
      const deal = this.createTestDeal({ value: 10000 })
      const plan = this.createTestPlan({
        rules: [
          {
            id: "base-rule",
            name: "Base Commission",
            type: "base_rate",
            conditions: [],
            rate: 10,
            rateType: "percentage",
            priority: 1,
            active: true,
          },
        ],
        accelerators: [
          {
            id: "quota-accelerator",
            name: "Quota Accelerator",
            threshold: 100,
            multiplier: 1.5,
            conditions: [],
            resetPeriod: "monthly",
          },
        ],
      })

      const context = this.createTestContext({ quotaAttainment: 120 })
      const result = await this.engine.calculateCommission(deal, plan, context)

      if (
        result.success &&
        result.calculation?.accelerators.length === 1 &&
        result.calculation?.accelerators[0].acceleratedAmount === 1500
      ) {
        this.testResults.push({ name: "Accelerator Calculation", passed: true })
      } else {
        throw new Error("Accelerator calculation failed")
      }
    } catch (error) {
      this.testResults.push({
        name: "Accelerator Calculation",
        passed: false,
        error: error instanceof Error ? error.message : "Unknown error",
      })
    }
  }

  private async testDeceleratorCalculation(): Promise<void> {
    try {
      const deal = this.createTestDeal({ value: 10000 })
      const plan = this.createTestPlan({
        rules: [
          {
            id: "base-rule",
            name: "Base Commission",
            type: "base_rate",
            conditions: [],
            rate: 10,
            rateType: "percentage",
            priority: 1,
            active: true,
          },
        ],
        decelerators: [
          {
            id: "quota-decelerator",
            name: "Quota Decelerator",
            threshold: 80,
            multiplier: 0.8,
            conditions: [],
            resetPeriod: "monthly",
          },
        ],
      })

      const context = this.createTestContext({ quotaAttainment: 70 })
      const result = await this.engine.calculateCommission(deal, plan, context)

      if (
        result.success &&
        result.calculation?.decelerators.length === 1 &&
        result.calculation?.decelerators[0].deceleratedAmount === 800
      ) {
        this.testResults.push({ name: "Decelerator Calculation", passed: true })
      } else {
        throw new Error("Decelerator calculation failed")
      }
    } catch (error) {
      this.testResults.push({
        name: "Decelerator Calculation",
        passed: false,
        error: error instanceof Error ? error.message : "Unknown error",
      })
    }
  }

  private async testCurrencyConversion(): Promise<void> {
    try {
      const deal = this.createTestDeal({
        value: 10000,
        currency: "EUR",
      })

      const plan = this.createTestPlan({
        currency: "USD",
        rules: [
          {
            id: "base-rule",
            name: "Base Commission",
            type: "base_rate",
            conditions: [],
            rate: 10,
            rateType: "percentage",
            priority: 1,
            active: true,
          },
        ],
      })

      const context = this.createTestContext({
        organizationSettings: {
          ...this.createTestOrganizationSettings(),
          exchangeRates: { EUR: 1.1 },
        },
      })

      const result = await this.engine.calculateCommission(deal, plan, context)

      // Expected: 10000 EUR * 1.1 = 11000 USD, commission = 1100 USD
      if (result.success && result.calculation?.baseCommission === 1100) {
        this.testResults.push({ name: "Currency Conversion", passed: true })
      } else {
        throw new Error(`Expected 1100, got ${result.calculation?.baseCommission}`)
      }
    } catch (error) {
      this.testResults.push({
        name: "Currency Conversion",
        passed: false,
        error: error instanceof Error ? error.message : "Unknown error",
      })
    }
  }

  private async testRefundProcessing(): Promise<void> {
    try {
      const deal = this.createTestDeal({ value: 10000 })
      const plan = this.createTestPlan({
        rules: [
          {
            id: "base-rule",
            name: "Base Commission",
            type: "base_rate",
            conditions: [],
            rate: 10,
            rateType: "percentage",
            priority: 1,
            active: true,
          },
        ],
      })

      const context = this.createTestContext()

      // First calculate original commission
      const originalResult = await this.engine.calculateCommission(deal, plan, context)

      if (!originalResult.success || !originalResult.calculation) {
        throw new Error("Original calculation failed")
      }

      // Process refund
      const refundResult = await this.engine.processRefund(deal, 3000, originalResult.calculation, plan, context)

      // Expected: (10000 - 3000) * 10% = 700
      if (refundResult.success && refundResult.calculation?.baseCommission === 700) {
        this.testResults.push({ name: "Refund Processing", passed: true })
      } else {
        throw new Error(`Expected 700, got ${refundResult.calculation?.baseCommission}`)
      }
    } catch (error) {
      this.testResults.push({
        name: "Refund Processing",
        passed: false,
        error: error instanceof Error ? error.message : "Unknown error",
      })
    }
  }

  private async testDealCancellation(): Promise<void> {
    try {
      const deal = this.createTestDeal({ value: 10000, status: "cancelled" })
      const plan = this.createTestPlan({
        rules: [
          {
            id: "base-rule",
            name: "Base Commission",
            type: "base_rate",
            conditions: [],
            rate: 10,
            rateType: "percentage",
            priority: 1,
            active: true,
          },
        ],
      })

      const context = this.createTestContext()

      // Original calculation
      const originalCalculation = {
        id: "original-calc",
        dealId: deal.id,
        salesRepId: context.salesRepId,
        planId: plan.id,
        planVersion: plan.version,
        calculationDate: new Date().toISOString(),
        baseCommission: 1000,
        bonuses: [],
        accelerators: [],
        decelerators: [],
        adjustments: [],
        grossCommission: 1000,
        taxes: 0,
        netCommission: 1000,
        currency: "USD",
        status: "calculated" as const,
        auditTrail: [],
      }

      const cancellationResult = await this.engine.handleDealCancellation(deal, originalCalculation, context)

      if (cancellationResult.success && cancellationResult.calculation?.netCommission === -1000) {
        this.testResults.push({ name: "Deal Cancellation", passed: true })
      } else {
        throw new Error("Deal cancellation failed")
      }
    } catch (error) {
      this.testResults.push({
        name: "Deal Cancellation",
        passed: false,
        error: error instanceof Error ? error.message : "Unknown error",
      })
    }
  }

  private async testAdminOverride(): Promise<void> {
    try {
      const originalCalculation = {
        id: "original-calc",
        dealId: "deal-123",
        salesRepId: "rep-123",
        planId: "plan-123",
        planVersion: "1.0",
        calculationDate: new Date().toISOString(),
        baseCommission: 1000,
        bonuses: [],
        accelerators: [],
        decelerators: [],
        adjustments: [],
        grossCommission: 1000,
        taxes: 0,
        netCommission: 1000,
        currency: "USD",
        status: "calculated" as const,
        auditTrail: [],
      }

      const context = this.createTestContext()
      const overrideResult = await this.engine.applyAdminOverride(
        originalCalculation,
        1500,
        "Performance bonus adjustment",
        "admin-123",
        context,
      )

      if (overrideResult.success && overrideResult.calculation?.netCommission === 1500) {
        this.testResults.push({ name: "Admin Override", passed: true })
      } else {
        throw new Error("Admin override failed")
      }
    } catch (error) {
      this.testResults.push({
        name: "Admin Override",
        passed: false,
        error: error instanceof Error ? error.message : "Unknown error",
      })
    }
  }

  private async testRoundingRules(): Promise<void> {
    try {
      const deal = this.createTestDeal({ value: 10333 })
      const plan = this.createTestPlan({
        rules: [
          {
            id: "base-rule",
            name: "Base Commission",
            type: "base_rate",
            conditions: [],
            rate: 10.5,
            rateType: "percentage",
            priority: 1,
            active: true,
          },
        ],
        rounding: {
          method: "round",
          precision: 2,
          currency: true,
        },
      })

      const context = this.createTestContext()
      const result = await this.engine.calculateCommission(deal, plan, context)

      // Expected: 10333 * 10.5% = 1084.965, rounded to 1084.97
      if (result.success && result.calculation?.grossCommission === 1084.97) {
        this.testResults.push({ name: "Rounding Rules", passed: true })
      } else {
        throw new Error(`Expected 1084.97, got ${result.calculation?.grossCommission}`)
      }
    } catch (error) {
      this.testResults.push({
        name: "Rounding Rules",
        passed: false,
        error: error instanceof Error ? error.message : "Unknown error",
      })
    }
  }

  private async testTaxCalculation(): Promise<void> {
    try {
      const deal = this.createTestDeal({ value: 10000 })
      const plan = this.createTestPlan({
        rules: [
          {
            id: "base-rule",
            name: "Base Commission",
            type: "base_rate",
            conditions: [],
            rate: 10,
            rateType: "percentage",
            priority: 1,
            active: true,
          },
        ],
        taxSettings: {
          enabled: true,
          rate: 20,
          jurisdiction: "US",
          exemptions: [],
        },
      })

      const context = this.createTestContext()
      const result = await this.engine.calculateCommission(deal, plan, context)

      // Expected: 1000 commission, 200 tax, 800 net
      if (
        result.success &&
        result.calculation?.grossCommission === 1000 &&
        result.calculation?.taxes === 200 &&
        result.calculation?.netCommission === 800
      ) {
        this.testResults.push({ name: "Tax Calculation", passed: true })
      } else {
        throw new Error("Tax calculation failed")
      }
    } catch (error) {
      this.testResults.push({
        name: "Tax Calculation",
        passed: false,
        error: error instanceof Error ? error.message : "Unknown error",
      })
    }
  }

  private async testErrorHandling(): Promise<void> {
    try {
      const invalidDeal = this.createTestDeal({ value: -1000 }) // Invalid negative value
      const plan = this.createTestPlan()
      const context = this.createTestContext()

      const result = await this.engine.calculateCommission(invalidDeal, plan, context)

      if (!result.success && result.errors.length > 0) {
        this.testResults.push({ name: "Error Handling", passed: true })
      } else {
        throw new Error("Error handling failed - should have returned errors")
      }
    } catch (error) {
      this.testResults.push({
        name: "Error Handling",
        passed: false,
        error: error instanceof Error ? error.message : "Unknown error",
      })
    }
  }

  private async testAuditTrail(): Promise<void> {
    try {
      const deal = this.createTestDeal({ value: 10000 })
      const plan = this.createTestPlan({
        rules: [
          {
            id: "base-rule",
            name: "Base Commission",
            type: "base_rate",
            conditions: [],
            rate: 10,
            rateType: "percentage",
            priority: 1,
            active: true,
          },
        ],
      })

      const context = this.createTestContext()
      const result = await this.engine.calculateCommission(deal, plan, context)

      if (result.success && result.calculation?.auditTrail && result.calculation.auditTrail.length > 0) {
        this.testResults.push({ name: "Audit Trail", passed: true })
      } else {
        throw new Error("Audit trail not generated")
      }
    } catch (error) {
      this.testResults.push({
        name: "Audit Trail",
        passed: false,
        error: error instanceof Error ? error.message : "Unknown error",
      })
    }
  }

  private createTestDeal(overrides: Partial<Deal> = {}): Deal {
    return {
      id: "test-deal-123",
      value: 10000,
      closeDate: "2024-01-31",
      createdDate: "2024-01-01",
      stage: "Closed Won",
      status: "won",
      products: [
        {
          id: "prod-1",
          name: "Test Product",
          category: "Software",
          quantity: 1,
          unitPrice: 10000,
          totalPrice: 10000,
          commissionable: true,
        },
      ],
      currency: "USD",
      salesRepId: "rep-123",
      dealType: "new_business",
      ...overrides,
    }
  }

  private createTestPlan(overrides: Partial<CommissionPlan> = {}): CommissionPlan {
    return {
      id: "test-plan-123",
      name: "Test Plan",
      version: "1.0",
      effectiveDate: "2024-01-01",
      currency: "USD",
      rules: [],
      tiers: [],
      bonuses: [],
      accelerators: [],
      decelerators: [],
      rounding: {
        method: "round",
        precision: 2,
        currency: true,
      },
      taxSettings: {
        enabled: false,
        rate: 0,
        jurisdiction: "US",
        exemptions: [],
      },
      ...overrides,
    }
  }

  private createTestContext(overrides: Partial<CalculationContext> = {}): CalculationContext {
    return {
      salesRepId: "rep-123",
      period: {
        start: "2024-01-01",
        end: "2024-01-31",
      },
      quotaAttainment: 100,
      organizationSettings: this.createTestOrganizationSettings(),
      ...overrides,
    }
  }

  private createTestOrganizationSettings(): OrganizationSettings {
    return {
      baseCurrency: "USD",
      supportedCurrencies: ["USD", "EUR", "GBP"],
      exchangeRates: {
        EUR: 1.1,
        GBP: 1.3,
      },
      fiscalYearStart: "2024-01-01",
      payoutSchedule: "monthly",
      defaultRounding: {
        method: "round",
        precision: 2,
        currency: true,
      },
      auditRetention: 365,
    }
  }

  private printTestResults(): void {
    console.log("\n📊 Test Results Summary:")
    console.log("========================")

    const passed = this.testResults.filter((r) => r.passed).length
    const total = this.testResults.length

    this.testResults.forEach((result) => {
      const status = result.passed ? "✅" : "❌"
      console.log(`${status} ${result.name}`)
      if (!result.passed && result.error) {
        console.log(`   Error: ${result.error}`)
      }
    })

    console.log(`\n📈 Overall: ${passed}/${total} tests passed (${((passed / total) * 100).toFixed(1)}%)`)

    if (passed === total) {
      console.log("🎉 All tests passed! Commission engine is ready for production.")
    } else {
      console.log("⚠️  Some tests failed. Please review and fix issues before deployment.")
    }
  }
}

// Export test runner
export const runCommissionEngineTests = async () => {
  const testSuite = new CommissionEngineTestSuite()
  await testSuite.runAllTests()
}
