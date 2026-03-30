import type {
  Deal,
  CommissionPlan,
  CommissionCalculation,
  CalculationContext,
  CalculationResult,
  CalculationError,
  AuditEntry,
  RuleCondition,
  CommissionRule,
} from "./types"
import { v4 as uuidv4 } from "uuid"

export class CommissionCalculationEngine {
  private auditTrail: AuditEntry[] = []

  constructor() {
    this.addAuditEntry("engine_initialized", "system", {
      timestamp: new Date().toISOString(),
      version: "1.0.0",
    })
  }

  /**
   * Main calculation method that processes a deal against a commission plan
   */
  async calculateCommission(deal: Deal, plan: CommissionPlan, context: CalculationContext): Promise<CalculationResult> {
    const errors: CalculationError[] = []
    const warnings: CalculationError[] = []

    try {
      // Validate inputs
      const validationResult = this.validateInputs(deal, plan, context)
      if (!validationResult.isValid) {
        return {
          success: false,
          errors: validationResult.errors,
          warnings: [],
        }
      }

      // Initialize calculation
      const calculationId = uuidv4()
      this.addAuditEntry("calculation_started", context.salesRepId, {
        calculationId,
        dealId: deal.id,
        planId: plan.id,
        planVersion: plan.version,
      })

      // Convert currency if needed
      const normalizedDeal = await this.normalizeCurrency(deal, plan.currency, context)

      // Calculate base commission
      const baseCommission = await this.calculateBaseCommission(normalizedDeal, plan, context)

      // Apply bonuses
      const bonuses = await this.calculateBonuses(normalizedDeal, plan, context, baseCommission)

      // Apply accelerators
      const accelerators = await this.calculateAccelerators(normalizedDeal, plan, context, baseCommission)

      // Apply decelerators
      const decelerators = await this.calculateDecelerators(normalizedDeal, plan, context, baseCommission)

      // Handle adjustments
      const adjustments = await this.processAdjustments(normalizedDeal, plan, context)

      // Calculate gross commission
      const grossCommission = this.calculateGrossCommission(
        baseCommission,
        bonuses,
        accelerators,
        decelerators,
        adjustments,
      )

      // Apply rounding
      const roundedGrossCommission = this.applyRounding(grossCommission, plan.rounding)

      // Calculate taxes
      const taxes = this.calculateTaxes(roundedGrossCommission, plan.taxSettings, context)

      // Calculate net commission
      const netCommission = roundedGrossCommission - taxes

      // Create calculation result
      const calculation: CommissionCalculation = {
        id: calculationId,
        dealId: deal.id,
        salesRepId: context.salesRepId,
        planId: plan.id,
        planVersion: plan.version,
        calculationDate: new Date().toISOString(),
        baseCommission,
        bonuses,
        accelerators,
        decelerators,
        adjustments,
        grossCommission: roundedGrossCommission,
        taxes,
        netCommission,
        currency: plan.currency,
        exchangeRate: normalizedDeal.exchangeRate,
        status: "calculated",
        auditTrail: [...this.auditTrail],
        metadata: {
          originalDealValue: deal.value,
          originalCurrency: deal.currency,
          calculationEngine: "v1.0.0",
        },
      }

      this.addAuditEntry("calculation_completed", context.salesRepId, {
        calculationId,
        grossCommission: roundedGrossCommission,
        netCommission,
        baseCommission,
        bonusCount: bonuses.length,
        acceleratorCount: accelerators.length,
      })

      return {
        success: true,
        calculation,
        errors: [],
        warnings,
      }
    } catch (error) {
      const calculationError: CalculationError = {
        code: "CALCULATION_FAILED",
        message: error instanceof Error ? error.message : "Unknown calculation error",
        severity: "error",
        details: { error: error instanceof Error ? error.stack : error },
      }

      this.addAuditEntry("calculation_failed", context.salesRepId, {
        error: calculationError,
        dealId: deal.id,
        planId: plan.id,
      })

      return {
        success: false,
        errors: [calculationError],
        warnings,
      }
    }
  }

  /**
   * Recalculate commission for retroactive changes
   */
  async recalculateCommission(
    originalCalculation: CommissionCalculation,
    updatedDeal: Deal,
    plan: CommissionPlan,
    context: CalculationContext,
    reason: string,
  ): Promise<CalculationResult> {
    this.addAuditEntry("recalculation_started", context.salesRepId, {
      originalCalculationId: originalCalculation.id,
      reason,
      updatedDealId: updatedDeal.id,
    })

    const result = await this.calculateCommission(updatedDeal, plan, context)

    if (result.success && result.calculation) {
      result.calculation.metadata = {
        ...result.calculation.metadata,
        recalculation: true,
        originalCalculationId: originalCalculation.id,
        recalculationReason: reason,
      }
    }

    return result
  }

  /**
   * Handle deal cancellations
   */
  async handleDealCancellation(
    deal: Deal,
    originalCalculation: CommissionCalculation,
    context: CalculationContext,
  ): Promise<CalculationResult> {
    this.addAuditEntry("deal_cancellation", context.salesRepId, {
      dealId: deal.id,
      originalCalculationId: originalCalculation.id,
      originalCommission: originalCalculation.netCommission,
    })

    // Create a cancellation calculation with negative amounts
    const cancellationCalculation: CommissionCalculation = {
      ...originalCalculation,
      id: uuidv4(),
      calculationDate: new Date().toISOString(),
      baseCommission: -originalCalculation.baseCommission,
      bonuses: originalCalculation.bonuses.map((bonus) => ({
        ...bonus,
        amount: -bonus.amount,
      })),
      accelerators: originalCalculation.accelerators.map((acc) => ({
        ...acc,
        acceleratedAmount: -acc.acceleratedAmount,
      })),
      grossCommission: -originalCalculation.grossCommission,
      taxes: -originalCalculation.taxes,
      netCommission: -originalCalculation.netCommission,
      status: "calculated",
      auditTrail: [...this.auditTrail],
      metadata: {
        ...originalCalculation.metadata,
        cancellation: true,
        originalCalculationId: originalCalculation.id,
      },
    }

    return {
      success: true,
      calculation: cancellationCalculation,
      errors: [],
      warnings: [],
    }
  }

  /**
   * Process refunds and adjust commissions
   */
  async processRefund(
    deal: Deal,
    refundAmount: number,
    originalCalculation: CommissionCalculation,
    plan: CommissionPlan,
    context: CalculationContext,
  ): Promise<CalculationResult> {
    this.addAuditEntry("refund_processing", context.salesRepId, {
      dealId: deal.id,
      refundAmount,
      originalCalculationId: originalCalculation.id,
    })

    // Calculate refund percentage
    const refundPercentage = refundAmount / deal.value

    // Create adjusted deal
    const adjustedDeal: Deal = {
      ...deal,
      value: deal.value - refundAmount,
      refunds: [
        ...(deal.refunds || []),
        {
          id: uuidv4(),
          amount: refundAmount,
          date: new Date().toISOString(),
          reason: "Customer refund",
          type: refundAmount === deal.value ? "full" : "partial",
        },
      ],
    }

    // Recalculate commission
    const result = await this.recalculateCommission(
      originalCalculation,
      adjustedDeal,
      plan,
      context,
      `Refund processed: ${refundAmount}`,
    )

    if (result.success && result.calculation) {
      result.calculation.metadata = {
        ...result.calculation.metadata,
        refund: true,
        refundAmount,
        refundPercentage,
      }
    }

    return result
  }

  /**
   * Apply admin override to calculation
   */
  async applyAdminOverride(
    originalCalculation: CommissionCalculation,
    overrideAmount: number,
    reason: string,
    adminUserId: string,
    context: CalculationContext,
  ): Promise<CalculationResult> {
    this.addAuditEntry("admin_override", adminUserId, {
      originalCalculationId: originalCalculation.id,
      originalAmount: originalCalculation.netCommission,
      overrideAmount,
      reason,
    })

    const overriddenCalculation: CommissionCalculation = {
      ...originalCalculation,
      id: uuidv4(),
      calculationDate: new Date().toISOString(),
      adjustments: [
        ...originalCalculation.adjustments,
        {
          adjustmentId: uuidv4(),
          amount: overrideAmount - originalCalculation.netCommission,
          reason,
          approvedBy: adminUserId,
          date: new Date().toISOString(),
        },
      ],
      grossCommission: overrideAmount,
      netCommission: overrideAmount,
      status: "calculated",
      auditTrail: [...this.auditTrail],
      metadata: {
        ...originalCalculation.metadata,
        adminOverride: true,
        originalCalculationId: originalCalculation.id,
        overrideReason: reason,
        overrideBy: adminUserId,
      },
    }

    return {
      success: true,
      calculation: overriddenCalculation,
      errors: [],
      warnings: [],
    }
  }

  /**
   * Validate calculation inputs
   */
  private validateInputs(
    deal: Deal,
    plan: CommissionPlan,
    context: CalculationContext,
  ): { isValid: boolean; errors: CalculationError[] } {
    const errors: CalculationError[] = []

    // Validate deal
    if (!deal.id) {
      errors.push({
        code: "INVALID_DEAL_ID",
        message: "Deal ID is required",
        field: "deal.id",
        severity: "error",
      })
    }

    if (!deal.value || deal.value <= 0) {
      errors.push({
        code: "INVALID_DEAL_VALUE",
        message: "Deal value must be greater than 0",
        field: "deal.value",
        severity: "error",
      })
    }

    if (!deal.currency) {
      errors.push({
        code: "MISSING_CURRENCY",
        message: "Deal currency is required",
        field: "deal.currency",
        severity: "error",
      })
    }

    // Validate plan
    if (!plan.id) {
      errors.push({
        code: "INVALID_PLAN_ID",
        message: "Commission plan ID is required",
        field: "plan.id",
        severity: "error",
      })
    }

    if (!plan.rules || plan.rules.length === 0) {
      errors.push({
        code: "NO_COMMISSION_RULES",
        message: "Commission plan must have at least one rule",
        field: "plan.rules",
        severity: "error",
      })
    }

    // Validate context
    if (!context.salesRepId) {
      errors.push({
        code: "MISSING_SALES_REP",
        message: "Sales representative ID is required",
        field: "context.salesRepId",
        severity: "error",
      })
    }

    return {
      isValid: errors.length === 0,
      errors,
    }
  }

  /**
   * Normalize currency for calculations
   */
  private async normalizeCurrency(deal: Deal, targetCurrency: string, context: CalculationContext): Promise<Deal> {
    if (deal.currency === targetCurrency) {
      return deal
    }

    const exchangeRate = context.organizationSettings.exchangeRates[deal.currency]
    if (!exchangeRate) {
      throw new Error(`Exchange rate not found for currency: ${deal.currency}`)
    }

    this.addAuditEntry("currency_conversion", context.salesRepId, {
      fromCurrency: deal.currency,
      toCurrency: targetCurrency,
      exchangeRate,
      originalValue: deal.value,
      convertedValue: deal.value * exchangeRate,
    })

    return {
      ...deal,
      value: deal.value * exchangeRate,
      originalValue: deal.originalValue ? deal.originalValue * exchangeRate : undefined,
      currency: targetCurrency,
      exchangeRate,
      products: deal.products.map((product) => ({
        ...product,
        unitPrice: product.unitPrice * exchangeRate,
        totalPrice: product.totalPrice * exchangeRate,
      })),
    }
  }

  /**
   * Calculate base commission
   */
  private async calculateBaseCommission(
    deal: Deal,
    plan: CommissionPlan,
    context: CalculationContext,
  ): Promise<number> {
    let baseCommission = 0
    const applicableRules = plan.rules.filter((rule) => rule.active).sort((a, b) => a.priority - b.priority)

    for (const rule of applicableRules) {
      if (this.evaluateRuleConditions(rule.conditions, deal, context)) {
        const ruleCommission = this.calculateRuleCommission(rule, deal, context)
        baseCommission += ruleCommission

        this.addAuditEntry("rule_applied", context.salesRepId, {
          ruleId: rule.id,
          ruleName: rule.name,
          ruleType: rule.type,
          commission: ruleCommission,
          dealValue: deal.value,
        })
      }
    }

    // Apply tier calculations if configured
    if (plan.tiers && plan.tiers.length > 0) {
      const tierCommission = this.calculateTierCommission(deal, plan.tiers, context)
      baseCommission = Math.max(baseCommission, tierCommission)
    }

    return baseCommission
  }

  /**
   * Calculate bonuses
   */
  private async calculateBonuses(
    deal: Deal,
    plan: CommissionPlan,
    context: CalculationContext,
    baseCommission: number,
  ) {
    const bonuses = []

    for (const bonusRule of plan.bonuses) {
      if (this.evaluateRuleConditions(bonusRule.conditions, deal, context)) {
        const bonusAmount =
          bonusRule.amountType === "percentage" ? (baseCommission * bonusRule.amount) / 100 : bonusRule.amount

        // Check max payout limit
        const finalBonusAmount = bonusRule.maxPayout ? Math.min(bonusAmount, bonusRule.maxPayout) : bonusAmount

        bonuses.push({
          ruleId: bonusRule.id,
          ruleName: bonusRule.name,
          amount: finalBonusAmount,
          reason: `${bonusRule.type} bonus applied`,
        })

        this.addAuditEntry("bonus_applied", context.salesRepId, {
          bonusRuleId: bonusRule.id,
          bonusType: bonusRule.type,
          amount: finalBonusAmount,
          baseCommission,
        })
      }
    }

    return bonuses
  }

  /**
   * Calculate accelerators
   */
  private async calculateAccelerators(
    deal: Deal,
    plan: CommissionPlan,
    context: CalculationContext,
    baseCommission: number,
  ) {
    const accelerators = []

    for (const acceleratorRule of plan.accelerators) {
      if (this.evaluateRuleConditions(acceleratorRule.conditions, deal, context)) {
        // Check if threshold is met (e.g., quota attainment)
        const quotaAttainment = context.quotaAttainment || 0

        if (quotaAttainment >= acceleratorRule.threshold) {
          const multiplier = Math.min(
            acceleratorRule.multiplier,
            acceleratorRule.maxMultiplier || acceleratorRule.multiplier,
          )

          const acceleratedAmount = baseCommission * multiplier

          accelerators.push({
            ruleId: acceleratorRule.id,
            ruleName: acceleratorRule.name,
            originalAmount: baseCommission,
            acceleratedAmount,
            multiplier,
          })

          this.addAuditEntry("accelerator_applied", context.salesRepId, {
            acceleratorRuleId: acceleratorRule.id,
            threshold: acceleratorRule.threshold,
            quotaAttainment,
            multiplier,
            originalAmount: baseCommission,
            acceleratedAmount,
          })
        }
      }
    }

    return accelerators
  }

  /**
   * Calculate decelerators
   */
  private async calculateDecelerators(
    deal: Deal,
    plan: CommissionPlan,
    context: CalculationContext,
    baseCommission: number,
  ) {
    const decelerators = []

    for (const deceleratorRule of plan.decelerators) {
      if (this.evaluateRuleConditions(deceleratorRule.conditions, deal, context)) {
        const quotaAttainment = context.quotaAttainment || 0

        if (quotaAttainment < deceleratorRule.threshold) {
          const multiplier = Math.max(
            deceleratorRule.multiplier,
            deceleratorRule.minMultiplier || deceleratorRule.multiplier,
          )

          const deceleratedAmount = baseCommission * multiplier

          decelerators.push({
            ruleId: deceleratorRule.id,
            ruleName: deceleratorRule.name,
            originalAmount: baseCommission,
            deceleratedAmount,
            multiplier,
          })

          this.addAuditEntry("decelerator_applied", context.salesRepId, {
            deceleratorRuleId: deceleratorRule.id,
            threshold: deceleratorRule.threshold,
            quotaAttainment,
            multiplier,
            originalAmount: baseCommission,
            deceleratedAmount,
          })
        }
      }
    }

    return decelerators
  }

  /**
   * Process manual adjustments
   */
  private async processAdjustments(deal: Deal, plan: CommissionPlan, context: CalculationContext) {
    const adjustments = []

    if (deal.adjustments) {
      for (const adjustment of deal.adjustments) {
        adjustments.push({
          adjustmentId: adjustment.id,
          amount: adjustment.type === "increase" ? adjustment.amount : -adjustment.amount,
          reason: adjustment.reason,
          approvedBy: adjustment.approvedBy,
          date: adjustment.date,
        })

        this.addAuditEntry("adjustment_processed", context.salesRepId, {
          adjustmentId: adjustment.id,
          type: adjustment.type,
          amount: adjustment.amount,
          reason: adjustment.reason,
        })
      }
    }

    return adjustments
  }

  /**
   * Calculate gross commission
   */
  private calculateGrossCommission(
    baseCommission: number,
    bonuses: any[],
    accelerators: any[],
    decelerators: any[],
    adjustments: any[],
  ): number {
    let grossCommission = baseCommission

    // Add bonuses
    grossCommission += bonuses.reduce((sum, bonus) => sum + bonus.amount, 0)

    // Apply accelerators
    const acceleratorAmount = accelerators.reduce((sum, acc) => sum + (acc.acceleratedAmount - acc.originalAmount), 0)
    grossCommission += acceleratorAmount

    // Apply decelerators
    const deceleratorAmount = decelerators.reduce((sum, dec) => sum + (dec.deceleratedAmount - dec.originalAmount), 0)
    grossCommission += deceleratorAmount

    // Apply adjustments
    grossCommission += adjustments.reduce((sum, adj) => sum + adj.amount, 0)

    return Math.max(0, grossCommission) // Ensure non-negative
  }

  /**
   * Apply rounding rules
   */
  private applyRounding(amount: number, roundingRule: any): number {
    const factor = Math.pow(10, roundingRule.precision)

    switch (roundingRule.method) {
      case "floor":
        return Math.floor(amount * factor) / factor
      case "ceil":
        return Math.ceil(amount * factor) / factor
      case "round":
      default:
        return Math.round(amount * factor) / factor
    }
  }

  /**
   * Calculate taxes
   */
  private calculateTaxes(grossCommission: number, taxSettings: any, context: CalculationContext): number {
    if (!taxSettings.enabled) {
      return 0
    }

    const taxAmount = (grossCommission * taxSettings.rate) / 100

    this.addAuditEntry("tax_calculated", context.salesRepId, {
      grossCommission,
      taxRate: taxSettings.rate,
      taxAmount,
      jurisdiction: taxSettings.jurisdiction,
    })

    return taxAmount
  }

  /**
   * Evaluate rule conditions
   */
  private evaluateRuleConditions(conditions: RuleCondition[], deal: Deal, context: CalculationContext): boolean {
    if (!conditions || conditions.length === 0) {
      return true
    }

    let result = true
    let currentLogicalOperator = "AND"

    for (const condition of conditions) {
      const conditionResult = this.evaluateCondition(condition, deal, context)

      if (currentLogicalOperator === "AND") {
        result = result && conditionResult
      } else {
        result = result || conditionResult
      }

      currentLogicalOperator = condition.logicalOperator || "AND"
    }

    return result
  }

  /**
   * Evaluate individual condition
   */
  private evaluateCondition(condition: RuleCondition, deal: Deal, context: CalculationContext): boolean {
    const fieldValue = this.getFieldValue(condition.field, deal, context)

    switch (condition.operator) {
      case "equals":
        return fieldValue === condition.value
      case "greater_than":
        return Number(fieldValue) > Number(condition.value)
      case "less_than":
        return Number(fieldValue) < Number(condition.value)
      case "contains":
        return String(fieldValue).includes(String(condition.value))
      case "in":
        return Array.isArray(condition.value) && condition.value.includes(fieldValue)
      case "between":
        return (
          Array.isArray(condition.value) &&
          Number(fieldValue) >= Number(condition.value[0]) &&
          Number(fieldValue) <= Number(condition.value[1])
        )
      default:
        return false
    }
  }

  /**
   * Get field value from deal or context
   */
  private getFieldValue(field: string, deal: Deal, context: CalculationContext): any {
    const fieldParts = field.split(".")
    let value: any = { deal, context }

    for (const part of fieldParts) {
      value = value?.[part]
    }

    return value
  }

  /**
   * Calculate rule-based commission
   */
  private calculateRuleCommission(rule: CommissionRule, deal: Deal, context: CalculationContext): number {
    if (rule.rateType === "fixed") {
      return rule.rate
    }

    // Percentage-based calculation
    let commissionableValue = deal.value

    // For product-specific rules, calculate based on matching products
    if (rule.type === "product_specific") {
      commissionableValue = deal.products
        .filter((product) => product.commissionable)
        .reduce((sum, product) => sum + product.totalPrice, 0)
    }

    return (commissionableValue * rule.rate) / 100
  }

  /**
   * Calculate tier-based commission
   */
  private calculateTierCommission(deal: Deal, tiers: any[], context: CalculationContext): number {
    let commission = 0
    let remainingValue = deal.value

    for (const tier of tiers.sort((a, b) => a.minThreshold - b.minThreshold)) {
      if (remainingValue <= 0) break

      const tierMin = tier.minThreshold
      const tierMax = tier.maxThreshold || Number.POSITIVE_INFINITY
      const tierValue = Math.min(remainingValue, tierMax - tierMin)

      if (tierValue > 0) {
        const tierCommission = tier.rateType === "fixed" ? tier.rate : (tierValue * tier.rate) / 100

        commission += tierCommission
        remainingValue -= tierValue

        this.addAuditEntry("tier_applied", context.salesRepId, {
          tierId: tier.id,
          tierName: tier.name,
          tierValue,
          tierCommission,
          tierRate: tier.rate,
        })
      }
    }

    return commission
  }

  /**
   * Add audit trail entry
   */
  private addAuditEntry(action: string, userId: string, details: Record<string, any>): void {
    this.auditTrail.push({
      id: uuidv4(),
      timestamp: new Date().toISOString(),
      action,
      userId,
      details,
    })
  }

  /**
   * Get calculation summary for reporting
   */
  getCalculationSummary(calculation: CommissionCalculation): Record<string, any> {
    return {
      calculationId: calculation.id,
      dealId: calculation.dealId,
      salesRepId: calculation.salesRepId,
      planId: calculation.planId,
      baseCommission: calculation.baseCommission,
      totalBonuses: calculation.bonuses.reduce((sum, bonus) => sum + bonus.amount, 0),
      totalAccelerators: calculation.accelerators.reduce(
        (sum, acc) => sum + (acc.acceleratedAmount - acc.originalAmount),
        0,
      ),
      totalDecelerators: calculation.decelerators.reduce(
        (sum, dec) => sum + (dec.deceleratedAmount - dec.originalAmount),
        0,
      ),
      totalAdjustments: calculation.adjustments.reduce((sum, adj) => sum + adj.amount, 0),
      grossCommission: calculation.grossCommission,
      taxes: calculation.taxes,
      netCommission: calculation.netCommission,
      currency: calculation.currency,
      status: calculation.status,
      calculationDate: calculation.calculationDate,
    }
  }
}

// Export singleton instance
export const commissionEngine = new CommissionCalculationEngine()
