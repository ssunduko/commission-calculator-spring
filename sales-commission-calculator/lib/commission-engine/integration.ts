import { commissionEngine } from "./calculator"
import type { Deal, CommissionPlan, CalculationContext } from "./types"

/**
 * Integration layer for the commission engine with the UI components
 */
export class CommissionEngineIntegration {
  /**
   * Calculate commission for a deal and return formatted result for UI
   */
  async calculateDealCommission(
    dealId: string,
    planId: string,
    salesRepId: string,
  ): Promise<{
    success: boolean
    commission?: {
      baseCommission: number
      bonuses: number
      accelerators: number
      decelerators: number
      grossCommission: number
      taxes: number
      netCommission: number
      breakdown: Array<{
        type: string
        name: string
        amount: number
        description: string
      }>
    }
    errors?: string[]
  }> {
    try {
      // In a real implementation, these would be fetched from your data store
      const deal = await this.fetchDeal(dealId)
      const plan = await this.fetchCommissionPlan(planId)
      const context = await this.buildCalculationContext(salesRepId)

      const result = await commissionEngine.calculateCommission(deal, plan, context)

      if (!result.success) {
        return {
          success: false,
          errors: result.errors.map((e) => e.message),
        }
      }

      const calculation = result.calculation!
      const bonusTotal = calculation.bonuses.reduce((sum, b) => sum + b.amount, 0)
      const acceleratorTotal = calculation.accelerators.reduce(
        (sum, a) => sum + (a.acceleratedAmount - a.originalAmount),
        0,
      )
      const deceleratorTotal = calculation.decelerators.reduce(
        (sum, d) => sum + (d.deceleratedAmount - d.originalAmount),
        0,
      )

      // Build breakdown for UI display
      const breakdown = [
        {
          type: "base",
          name: "Base Commission",
          amount: calculation.baseCommission,
          description: `${plan.rules[0]?.rate || 0}% of deal value`,
        },
        ...calculation.bonuses.map((bonus) => ({
          type: "bonus",
          name: bonus.ruleName,
          amount: bonus.amount,
          description: bonus.reason,
        })),
        ...calculation.accelerators.map((acc) => ({
          type: "accelerator",
          name: acc.ruleName,
          amount: acc.acceleratedAmount - acc.originalAmount,
          description: `${acc.multiplier}x multiplier applied`,
        })),
        ...calculation.decelerators.map((dec) => ({
          type: "decelerator",
          name: dec.ruleName,
          amount: dec.deceleratedAmount - dec.originalAmount,
          description: `${dec.multiplier}x multiplier applied`,
        })),
      ]

      return {
        success: true,
        commission: {
          baseCommission: calculation.baseCommission,
          bonuses: bonusTotal,
          accelerators: acceleratorTotal,
          decelerators: deceleratorTotal,
          grossCommission: calculation.grossCommission,
          taxes: calculation.taxes,
          netCommission: calculation.netCommission,
          breakdown,
        },
      }
    } catch (error) {
      return {
        success: false,
        errors: [error instanceof Error ? error.message : "Unknown error occurred"],
      }
    }
  }

  /**
   * Batch calculate commissions for multiple deals
   */
  async batchCalculateCommissions(
    dealIds: string[],
    planId: string,
    salesRepId: string,
  ): Promise<
    Array<{
      dealId: string
      success: boolean
      commission?: any
      errors?: string[]
    }>
  > {
    const results = []

    for (const dealId of dealIds) {
      const result = await this.calculateDealCommission(dealId, planId, salesRepId)
      results.push({
        dealId,
        ...result,
      })
    }

    return results
  }

  /**
   * Get commission projection for pipeline deals
   */
  async getCommissionProjections(
    salesRepId: string,
    planId: string,
  ): Promise<{
    totalProjected: number
    dealProjections: Array<{
      dealId: string
      dealValue: number
      probability: number
      projectedCommission: number
      weightedCommission: number
    }>
  }> {
    // In a real implementation, fetch open deals for the sales rep
    const openDeals = await this.fetchOpenDeals(salesRepId)
    const plan = await this.fetchCommissionPlan(planId)
    const context = await this.buildCalculationContext(salesRepId)

    const dealProjections = []
    let totalProjected = 0

    for (const deal of openDeals) {
      const result = await commissionEngine.calculateCommission(deal, plan, context)

      if (result.success && result.calculation) {
        const projectedCommission = result.calculation.netCommission
        const weightedCommission = (projectedCommission * (deal.metadata?.probability || 50)) / 100

        dealProjections.push({
          dealId: deal.id,
          dealValue: deal.value,
          probability: deal.metadata?.probability || 50,
          projectedCommission,
          weightedCommission,
        })

        totalProjected += weightedCommission
      }
    }

    return {
      totalProjected,
      dealProjections,
    }
  }

  /**
   * Validate commission plan configuration
   */
  async validateCommissionPlan(plan: CommissionPlan): Promise<{
    isValid: boolean
    errors: string[]
    warnings: string[]
  }> {
    const errors: string[] = []
    const warnings: string[] = []

    // Basic validation
    if (!plan.name || plan.name.trim() === "") {
      errors.push("Plan name is required")
    }

    if (!plan.rules || plan.rules.length === 0) {
      errors.push("At least one commission rule is required")
    }

    // Validate rules
    plan.rules.forEach((rule, index) => {
      if (!rule.name || rule.name.trim() === "") {
        errors.push(`Rule ${index + 1}: Name is required`)
      }

      if (rule.rate < 0) {
        errors.push(`Rule ${index + 1}: Rate cannot be negative`)
      }

      if (rule.rateType === "percentage" && rule.rate > 100) {
        warnings.push(`Rule ${index + 1}: Rate exceeds 100%, this may result in very high commissions`)
      }
    })

    // Validate tiers
    if (plan.tiers && plan.tiers.length > 0) {
      const sortedTiers = [...plan.tiers].sort((a, b) => a.minThreshold - b.minThreshold)

      for (let i = 0; i < sortedTiers.length; i++) {
        const tier = sortedTiers[i]

        if (tier.minThreshold < 0) {
          errors.push(`Tier ${i + 1}: Minimum threshold cannot be negative`)
        }

        if (tier.maxThreshold && tier.maxThreshold <= tier.minThreshold) {
          errors.push(`Tier ${i + 1}: Maximum threshold must be greater than minimum threshold`)
        }

        // Check for gaps between tiers
        if (i > 0) {
          const previousTier = sortedTiers[i - 1]
          if (previousTier.maxThreshold && tier.minThreshold > previousTier.maxThreshold) {
            warnings.push(`Gap detected between tier ${i} and tier ${i + 1}`)
          }
        }
      }
    }

    // Validate bonuses
    plan.bonuses.forEach((bonus, index) => {
      if (bonus.startDate && bonus.endDate && bonus.startDate >= bonus.endDate) {
        errors.push(`Bonus ${index + 1}: Start date must be before end date`)
      }

      if (bonus.maxPayout && bonus.maxPayout <= 0) {
        errors.push(`Bonus ${index + 1}: Maximum payout must be positive`)
      }
    })

    // Validate accelerators and decelerators
    plan.accelerators.forEach((acc, index) => {
      if (acc.multiplier <= 0) {
        errors.push(`Accelerator ${index + 1}: Multiplier must be positive`)
      }

      if (acc.maxMultiplier && acc.maxMultiplier < acc.multiplier) {
        errors.push(`Accelerator ${index + 1}: Maximum multiplier cannot be less than base multiplier`)
      }
    })

    plan.decelerators.forEach((dec, index) => {
      if (dec.multiplier < 0) {
        errors.push(`Decelerator ${index + 1}: Multiplier cannot be negative`)
      }

      if (dec.minMultiplier && dec.minMultiplier > dec.multiplier) {
        errors.push(`Decelerator ${index + 1}: Minimum multiplier cannot be greater than base multiplier`)
      }
    })

    return {
      isValid: errors.length === 0,
      errors,
      warnings,
    }
  }

  /**
   * Test commission plan with sample data
   */
  async testCommissionPlan(
    plan: CommissionPlan,
    testScenarios: Array<{
      name: string
      dealValue: number
      dealType: string
      quotaAttainment?: number
      expectedCommission?: number
    }>,
  ): Promise<
    Array<{
      scenario: string
      success: boolean
      calculatedCommission: number
      expectedCommission?: number
      variance?: number
      details: any
    }>
  > {
    const results = []

    for (const scenario of testScenarios) {
      try {
        const testDeal: Deal = {
          id: `test-${Date.now()}`,
          value: scenario.dealValue,
          closeDate: new Date().toISOString(),
          createdDate: new Date().toISOString(),
          stage: "Closed Won",
          status: "won",
          products: [
            {
              id: "test-product",
              name: "Test Product",
              category: "Software",
              quantity: 1,
              unitPrice: scenario.dealValue,
              totalPrice: scenario.dealValue,
              commissionable: true,
            },
          ],
          currency: plan.currency,
          salesRepId: "test-rep",
          dealType: scenario.dealType as any,
        }

        const context: CalculationContext = {
          salesRepId: "test-rep",
          period: {
            start: new Date().toISOString(),
            end: new Date().toISOString(),
          },
          quotaAttainment: scenario.quotaAttainment || 100,
          organizationSettings: {
            baseCurrency: plan.currency,
            supportedCurrencies: [plan.currency],
            exchangeRates: {},
            fiscalYearStart: new Date().toISOString(),
            payoutSchedule: "monthly",
            defaultRounding: plan.rounding,
            auditRetention: 365,
          },
        }

        const result = await commissionEngine.calculateCommission(testDeal, plan, context)

        if (result.success && result.calculation) {
          const calculatedCommission = result.calculation.netCommission
          const variance = scenario.expectedCommission
            ? (Math.abs(calculatedCommission - scenario.expectedCommission) / scenario.expectedCommission) * 100
            : undefined

          results.push({
            scenario: scenario.name,
            success: true,
            calculatedCommission,
            expectedCommission: scenario.expectedCommission,
            variance,
            details: commissionEngine.getCalculationSummary(result.calculation),
          })
        } else {
          results.push({
            scenario: scenario.name,
            success: false,
            calculatedCommission: 0,
            expectedCommission: scenario.expectedCommission,
            details: { errors: result.errors.map((e) => e.message) },
          })
        }
      } catch (error) {
        results.push({
          scenario: scenario.name,
          success: false,
          calculatedCommission: 0,
          expectedCommission: scenario.expectedCommission,
          details: { error: error instanceof Error ? error.message : "Unknown error" },
        })
      }
    }

    return results
  }

  // Mock data fetching methods - in real implementation, these would connect to your data store
  private async fetchDeal(dealId: string): Promise<Deal> {
    // Mock implementation - replace with actual data fetching
    return {
      id: dealId,
      value: 50000,
      closeDate: "2024-02-28",
      createdDate: "2024-01-15",
      stage: "Closed Won",
      status: "won",
      products: [
        {
          id: "prod-1",
          name: "Enterprise Software License",
          category: "Software",
          quantity: 1,
          unitPrice: 50000,
          totalPrice: 50000,
          commissionable: true,
        },
      ],
      currency: "USD",
      salesRepId: "rep-123",
      dealType: "new_business",
    }
  }

  private async fetchCommissionPlan(planId: string): Promise<CommissionPlan> {
    // Mock implementation - replace with actual data fetching
    return {
      id: planId,
      name: "Standard Sales Plan",
      version: "2.1",
      effectiveDate: "2024-01-01",
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
      tiers: [],
      bonuses: [
        {
          id: "spif-bonus",
          name: "Q1 SPIF",
          type: "spif",
          conditions: [],
          amount: 1000,
          amountType: "fixed",
          startDate: "2024-01-01",
          endDate: "2024-03-31",
          frequency: "per_deal",
        },
      ],
      accelerators: [
        {
          id: "quota-accelerator",
          name: "Quota Accelerator",
          threshold: 100,
          multiplier: 1.2,
          conditions: [],
          resetPeriod: "monthly",
        },
      ],
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
    }
  }

  private async fetchOpenDeals(salesRepId: string): Promise<Deal[]> {
    // Mock implementation - replace with actual data fetching
    return [
      {
        id: "deal-open-1",
        value: 25000,
        closeDate: "2024-03-15",
        createdDate: "2024-02-01",
        stage: "Negotiation",
        status: "open",
        products: [
          {
            id: "prod-1",
            name: "Software License",
            category: "Software",
            quantity: 1,
            unitPrice: 25000,
            totalPrice: 25000,
            commissionable: true,
          },
        ],
        currency: "USD",
        salesRepId,
        dealType: "new_business",
        metadata: { probability: 80 },
      },
      {
        id: "deal-open-2",
        value: 15000,
        closeDate: "2024-04-01",
        createdDate: "2024-02-10",
        stage: "Proposal",
        status: "open",
        products: [
          {
            id: "prod-2",
            name: "Consulting Services",
            category: "Services",
            quantity: 1,
            unitPrice: 15000,
            totalPrice: 15000,
            commissionable: true,
          },
        ],
        currency: "USD",
        salesRepId,
        dealType: "upsell",
        metadata: { probability: 60 },
      },
    ]
  }

  private async buildCalculationContext(salesRepId: string): Promise<CalculationContext> {
    // Mock implementation - replace with actual context building
    return {
      salesRepId,
      period: {
        start: "2024-01-01",
        end: "2024-01-31",
      },
      quotaAttainment: 110,
      teamPerformance: 105,
      organizationSettings: {
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
      },
    }
  }
}

// Export integration instance
export const commissionIntegration = new CommissionEngineIntegration()
