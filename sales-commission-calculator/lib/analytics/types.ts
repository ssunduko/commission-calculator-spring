export interface CommissionAnalytics {
  period: {
    start: string
    end: string
    type: "daily" | "weekly" | "monthly" | "quarterly" | "yearly"
  }
  summary: {
    totalCommissions: number
    totalDeals: number
    averageCommission: number
    averageDealSize: number
    conversionRate: number
    quotaAttainment: number
    growthRate: number
    previousPeriodComparison: number
  }
  breakdowns: {
    byPlan: PlanBreakdown[]
    byDealType: DealTypeBreakdown[]
    byProduct: ProductBreakdown[]
    byTerritory: TerritoryBreakdown[]
    byTimeframe: TimeframeBreakdown[]
  }
  trends: {
    commissionTrend: TrendData[]
    dealVolumeTrend: TrendData[]
    quotaAttainmentTrend: TrendData[]
    conversionRateTrend: TrendData[]
  }
  topPerformers: {
    salesReps: SalesRepPerformance[]
    deals: TopDeal[]
    products: TopProduct[]
  }
  insights: AnalyticsInsight[]
  forecasts: {
    nextPeriodProjection: number
    confidenceInterval: [number, number]
    factors: ForecastFactor[]
  }
}

export interface PlanBreakdown {
  planId: string
  planName: string
  totalCommissions: number
  dealCount: number
  averageCommission: number
  percentage: number
  growth: number
}

export interface DealTypeBreakdown {
  dealType: string
  totalCommissions: number
  dealCount: number
  averageCommission: number
  percentage: number
  conversionRate: number
}

export interface ProductBreakdown {
  productId: string
  productName: string
  category: string
  totalCommissions: number
  dealCount: number
  averageCommission: number
  margin: number
  percentage: number
}

export interface TerritoryBreakdown {
  territory: string
  totalCommissions: number
  dealCount: number
  averageCommission: number
  quotaAttainment: number
  salesRepCount: number
}

export interface TimeframeBreakdown {
  period: string
  totalCommissions: number
  dealCount: number
  quotaAttainment: number
  growth: number
}

export interface TrendData {
  period: string
  value: number
  target?: number
  previousValue?: number
}

export interface SalesRepPerformance {
  salesRepId: string
  name: string
  totalCommissions: number
  dealCount: number
  averageCommission: number
  quotaAttainment: number
  rank: number
  growth: number
  efficiency: number
}

export interface TopDeal {
  dealId: string
  dealName: string
  company: string
  value: number
  commission: number
  salesRepId: string
  salesRepName: string
  closeDate: string
  dealType: string
}

export interface TopProduct {
  productId: string
  productName: string
  category: string
  totalRevenue: number
  totalCommissions: number
  dealCount: number
  averageCommission: number
  margin: number
}

export interface AnalyticsInsight {
  id: string
  type: "opportunity" | "risk" | "trend" | "anomaly"
  title: string
  description: string
  impact: "high" | "medium" | "low"
  actionable: boolean
  recommendation?: string
  data: Record<string, any>
}

export interface ForecastFactor {
  factor: string
  impact: number
  confidence: number
  description: string
}

export interface ReportConfig {
  title: string
  description?: string
  period: {
    start: string
    end: string
    type: "daily" | "weekly" | "monthly" | "quarterly" | "yearly"
  }
  filters: {
    salesReps?: string[]
    territories?: string[]
    products?: string[]
    dealTypes?: string[]
    commissionPlans?: string[]
    minDealValue?: number
    maxDealValue?: number
  }
  sections: ReportSection[]
  format: "pdf" | "excel" | "csv" | "json"
  schedule?: {
    frequency: "daily" | "weekly" | "monthly" | "quarterly"
    recipients: string[]
    enabled: boolean
  }
}

export interface ReportSection {
  id: string
  title: string
  type: "summary" | "chart" | "table" | "insights" | "forecast"
  config: Record<string, any>
  order: number
}

export interface CommissionReport {
  id: string
  title: string
  generatedAt: string
  period: {
    start: string
    end: string
  }
  analytics: CommissionAnalytics
  config: ReportConfig
  metadata: {
    generatedBy: string
    version: string
    dataPoints: number
    processingTime: number
  }
}
