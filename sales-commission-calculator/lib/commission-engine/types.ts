export interface Deal {
  id: string
  value: number
  originalValue?: number
  closeDate: string
  createdDate: string
  stage: string
  status: "open" | "won" | "lost" | "cancelled"
  products: DealProduct[]
  currency: string
  exchangeRate?: number
  salesRepId: string
  managerId?: string
  dealType: "new_business" | "renewal" | "upsell" | "cross_sell"
  refunds?: Refund[]
  adjustments?: Adjustment[]
  metadata?: Record<string, any>
}

export interface DealProduct {
  id: string
  name: string
  category: string
  quantity: number
  unitPrice: number
  totalPrice: number
  commissionable: boolean
  margin?: number
}

export interface Refund {
  id: string
  amount: number
  date: string
  reason: string
  type: "full" | "partial"
}

export interface Adjustment {
  id: string
  amount: number
  date: string
  reason: string
  type: "increase" | "decrease"
  approvedBy: string
}

export interface CommissionPlan {
  id: string
  name: string
  version: string
  effectiveDate: string
  expiryDate?: string
  currency: string
  rules: CommissionRule[]
  tiers: CommissionTier[]
  bonuses: BonusRule[]
  accelerators: AcceleratorRule[]
  decelerators: DeceleratorRule[]
  rounding: RoundingRule
  taxSettings: TaxSettings
  metadata?: Record<string, any>
}

export interface CommissionRule {
  id: string
  name: string
  type: "base_rate" | "tiered" | "threshold" | "product_specific" | "deal_type"
  conditions: RuleCondition[]
  rate: number
  rateType: "percentage" | "fixed"
  priority: number
  active: boolean
}

export interface RuleCondition {
  field: string
  operator: "equals" | "greater_than" | "less_than" | "contains" | "in" | "between"
  value: any
  logicalOperator?: "AND" | "OR"
}

export interface CommissionTier {
  id: string
  name: string
  minThreshold: number
  maxThreshold?: number
  rate: number
  rateType: "percentage" | "fixed"
  cumulativeFrom?: number
}

export interface BonusRule {
  id: string
  name: string
  type: "spif" | "quota_achievement" | "product_bonus" | "team_bonus"
  conditions: RuleCondition[]
  amount: number
  amountType: "percentage" | "fixed"
  startDate?: string
  endDate?: string
  maxPayout?: number
  frequency: "per_deal" | "monthly" | "quarterly" | "annual"
}

export interface AcceleratorRule {
  id: string
  name: string
  threshold: number
  multiplier: number
  maxMultiplier?: number
  conditions: RuleCondition[]
  resetPeriod: "monthly" | "quarterly" | "annual"
}

export interface DeceleratorRule {
  id: string
  name: string
  threshold: number
  multiplier: number
  minMultiplier?: number
  conditions: RuleCondition[]
  resetPeriod: "monthly" | "quarterly" | "annual"
}

export interface RoundingRule {
  method: "round" | "floor" | "ceil"
  precision: number
  currency: boolean
}

export interface TaxSettings {
  enabled: boolean
  rate: number
  jurisdiction: string
  exemptions: string[]
}

export interface CommissionCalculation {
  id: string
  dealId: string
  salesRepId: string
  planId: string
  planVersion: string
  calculationDate: string
  baseCommission: number
  bonuses: BonusCalculation[]
  accelerators: AcceleratorCalculation[]
  decelerators: DeceleratorCalculation[]
  adjustments: AdjustmentCalculation[]
  grossCommission: number
  taxes: number
  netCommission: number
  currency: string
  exchangeRate?: number
  status: "calculated" | "approved" | "paid" | "disputed" | "cancelled"
  auditTrail: AuditEntry[]
  metadata?: Record<string, any>
}

export interface BonusCalculation {
  ruleId: string
  ruleName: string
  amount: number
  reason: string
}

export interface AcceleratorCalculation {
  ruleId: string
  ruleName: string
  originalAmount: number
  acceleratedAmount: number
  multiplier: number
}

export interface DeceleratorCalculation {
  ruleId: string
  ruleName: string
  originalAmount: number
  deceleratedAmount: number
  multiplier: number
}

export interface AdjustmentCalculation {
  adjustmentId: string
  amount: number
  reason: string
  approvedBy: string
  date: string
}

export interface AuditEntry {
  id: string
  timestamp: string
  action: string
  userId: string
  details: Record<string, any>
  previousValue?: any
  newValue?: any
}

export interface CalculationContext {
  salesRepId: string
  period: {
    start: string
    end: string
  }
  quotaAttainment?: number
  teamPerformance?: number
  previousCalculations?: CommissionCalculation[]
  organizationSettings: OrganizationSettings
}

export interface OrganizationSettings {
  baseCurrency: string
  supportedCurrencies: string[]
  exchangeRates: Record<string, number>
  fiscalYearStart: string
  payoutSchedule: "monthly" | "quarterly" | "annual"
  defaultRounding: RoundingRule
  auditRetention: number
}

export interface CalculationError {
  code: string
  message: string
  field?: string
  severity: "error" | "warning" | "info"
  details?: Record<string, any>
}

export interface CalculationResult {
  success: boolean
  calculation?: CommissionCalculation
  errors: CalculationError[]
  warnings: CalculationError[]
}
