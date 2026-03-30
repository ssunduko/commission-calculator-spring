import type {
  CommissionAnalytics,
  CommissionReport,
  ReportConfig,
  AnalyticsInsight,
  TrendData,
  SalesRepPerformance,
  TopDeal,
  PlanBreakdown,
  DealTypeBreakdown,
  ProductBreakdown,
  TerritoryBreakdown,
  TimeframeBreakdown,
  ForecastFactor,
  TopProduct, // Declare TopProduct here
} from "./types"
import type { CommissionCalculation, Deal, CommissionPlan } from "../commission-engine/types"
import { v4 as uuidv4 } from "uuid"

export class CommissionAnalyticsEngine {
  /**
   * Generate comprehensive commission analytics
   */
  async generateAnalytics(
    calculations: CommissionCalculation[],
    deals: Deal[],
    plans: CommissionPlan[],
    period: { start: string; end: string; type: string },
    previousPeriodCalculations?: CommissionCalculation[],
  ): Promise<CommissionAnalytics> {
    const startTime = Date.now()

    // Calculate summary metrics
    const summary = this.calculateSummaryMetrics(calculations, deals, previousPeriodCalculations)

    // Generate breakdowns
    const breakdowns = {
      byPlan: this.calculatePlanBreakdown(calculations, plans),
      byDealType: this.calculateDealTypeBreakdown(calculations, deals),
      byProduct: this.calculateProductBreakdown(calculations, deals),
      byTerritory: this.calculateTerritoryBreakdown(calculations, deals),
      byTimeframe: this.calculateTimeframeBreakdown(calculations, deals, period),
    }

    // Generate trends
    const trends = {
      commissionTrend: this.calculateCommissionTrend(calculations, period),
      dealVolumeTrend: this.calculateDealVolumeTrend(deals, period),
      quotaAttainmentTrend: this.calculateQuotaAttainmentTrend(calculations, period),
      conversionRateTrend: this.calculateConversionRateTrend(deals, period),
    }

    // Identify top performers
    const topPerformers = {
      salesReps: this.calculateTopSalesReps(calculations, deals),
      deals: this.calculateTopDeals(calculations, deals),
      products: this.calculateTopProducts(calculations, deals),
    }

    // Generate insights
    const insights = this.generateInsights(calculations, deals, summary, trends)

    // Generate forecasts
    const forecasts = this.generateForecasts(calculations, deals, trends)

    const processingTime = Date.now() - startTime

    console.log(`Analytics generated in ${processingTime}ms for ${calculations.length} calculations`)

    return {
      period: {
        start: period.start,
        end: period.end,
        type: period.type as any,
      },
      summary,
      breakdowns,
      trends,
      topPerformers,
      insights,
      forecasts,
    }
  }

  /**
   * Generate a comprehensive commission report
   */
  async generateReport(
    calculations: CommissionCalculation[],
    deals: Deal[],
    plans: CommissionPlan[],
    config: ReportConfig,
    generatedBy: string,
  ): Promise<CommissionReport> {
    const startTime = Date.now()

    // Get previous period data for comparison
    const previousPeriodCalculations = await this.getPreviousPeriodCalculations(config.period)

    // Generate analytics
    const analytics = await this.generateAnalytics(
      calculations,
      deals,
      plans,
      config.period,
      previousPeriodCalculations,
    )

    const processingTime = Date.now() - startTime

    return {
      id: uuidv4(),
      title: config.title,
      generatedAt: new Date().toISOString(),
      period: config.period,
      analytics,
      config,
      metadata: {
        generatedBy,
        version: "1.0.0",
        dataPoints: calculations.length,
        processingTime,
      },
    }
  }

  /**
   * Calculate summary metrics
   */
  private calculateSummaryMetrics(
    calculations: CommissionCalculation[],
    deals: Deal[],
    previousPeriodCalculations?: CommissionCalculation[],
  ) {
    const totalCommissions = calculations.reduce((sum, calc) => sum + calc.netCommission, 0)
    const totalDeals = calculations.length
    const averageCommission = totalDeals > 0 ? totalCommissions / totalDeals : 0
    const averageDealSize = deals.length > 0 ? deals.reduce((sum, deal) => sum + deal.value, 0) / deals.length : 0

    // Calculate conversion rate (won deals / total deals)
    const wonDeals = deals.filter((deal) => deal.status === "won").length
    const totalOpportunities = deals.length
    const conversionRate = totalOpportunities > 0 ? (wonDeals / totalOpportunities) * 100 : 0

    // Calculate quota attainment (this would typically come from external data)
    const quotaAttainment = this.calculateQuotaAttainment(calculations)

    // Calculate growth rate
    let growthRate = 0
    let previousPeriodComparison = 0

    if (previousPeriodCalculations && previousPeriodCalculations.length > 0) {
      const previousTotalCommissions = previousPeriodCalculations.reduce((sum, calc) => sum + calc.netCommission, 0)
      if (previousTotalCommissions > 0) {
        growthRate = ((totalCommissions - previousTotalCommissions) / previousTotalCommissions) * 100
        previousPeriodComparison = previousTotalCommissions
      }
    }

    return {
      totalCommissions,
      totalDeals,
      averageCommission,
      averageDealSize,
      conversionRate,
      quotaAttainment,
      growthRate,
      previousPeriodComparison,
    }
  }

  /**
   * Calculate commission plan breakdown
   */
  private calculatePlanBreakdown(calculations: CommissionCalculation[], plans: CommissionPlan[]): PlanBreakdown[] {
    const planMap = new Map<string, { totalCommissions: number; dealCount: number }>()

    calculations.forEach((calc) => {
      const existing = planMap.get(calc.planId) || { totalCommissions: 0, dealCount: 0 }
      planMap.set(calc.planId, {
        totalCommissions: existing.totalCommissions + calc.netCommission,
        dealCount: existing.dealCount + 1,
      })
    })

    const totalCommissions = calculations.reduce((sum, calc) => sum + calc.netCommission, 0)

    return Array.from(planMap.entries()).map(([planId, data]) => {
      const plan = plans.find((p) => p.id === planId)
      return {
        planId,
        planName: plan?.name || "Unknown Plan",
        totalCommissions: data.totalCommissions,
        dealCount: data.dealCount,
        averageCommission: data.dealCount > 0 ? data.totalCommissions / data.dealCount : 0,
        percentage: totalCommissions > 0 ? (data.totalCommissions / totalCommissions) * 100 : 0,
        growth: 0, // Would calculate from previous period data
      }
    })
  }

  /**
   * Calculate deal type breakdown
   */
  private calculateDealTypeBreakdown(calculations: CommissionCalculation[], deals: Deal[]): DealTypeBreakdown[] {
    const dealMap = new Map<string, Deal>()
    deals.forEach((deal) => dealMap.set(deal.id, deal))

    const typeMap = new Map<string, { totalCommissions: number; dealCount: number; wonDeals: number }>()

    calculations.forEach((calc) => {
      const deal = dealMap.get(calc.dealId)
      if (deal) {
        const existing = typeMap.get(deal.dealType) || { totalCommissions: 0, dealCount: 0, wonDeals: 0 }
        typeMap.set(deal.dealType, {
          totalCommissions: existing.totalCommissions + calc.netCommission,
          dealCount: existing.dealCount + 1,
          wonDeals: existing.wonDeals + (deal.status === "won" ? 1 : 0),
        })
      }
    })

    const totalCommissions = calculations.reduce((sum, calc) => sum + calc.netCommission, 0)

    return Array.from(typeMap.entries()).map(([dealType, data]) => ({
      dealType,
      totalCommissions: data.totalCommissions,
      dealCount: data.dealCount,
      averageCommission: data.dealCount > 0 ? data.totalCommissions / data.dealCount : 0,
      percentage: totalCommissions > 0 ? (data.totalCommissions / totalCommissions) * 100 : 0,
      conversionRate: data.dealCount > 0 ? (data.wonDeals / data.dealCount) * 100 : 0,
    }))
  }

  /**
   * Calculate product breakdown
   */
  private calculateProductBreakdown(calculations: CommissionCalculation[], deals: Deal[]): ProductBreakdown[] {
    const dealMap = new Map<string, Deal>()
    deals.forEach((deal) => dealMap.set(deal.id, deal))

    const productMap = new Map<
      string,
      {
        name: string
        category: string
        totalRevenue: number
        totalCommissions: number
        dealCount: number
        margin: number
      }
    >()

    calculations.forEach((calc) => {
      const deal = dealMap.get(calc.dealId)
      if (deal && deal.products) {
        deal.products.forEach((product) => {
          const existing = productMap.get(product.id) || {
            name: product.name,
            category: product.category,
            totalRevenue: 0,
            totalCommissions: 0,
            dealCount: 0,
            margin: product.margin || 0,
          }

          // Allocate commission proportionally based on product value
          const productCommission = (calc.netCommission * product.totalPrice) / deal.value

          productMap.set(product.id, {
            ...existing,
            totalRevenue: existing.totalRevenue + product.totalPrice,
            totalCommissions: existing.totalCommissions + productCommission,
            dealCount: existing.dealCount + 1,
          })
        })
      }
    })

    const totalCommissions = calculations.reduce((sum, calc) => sum + calc.netCommission, 0)

    return Array.from(productMap.entries()).map(([productId, data]) => ({
      productId,
      productName: data.name,
      category: data.category,
      totalCommissions: data.totalCommissions,
      dealCount: data.dealCount,
      averageCommission: data.dealCount > 0 ? data.totalCommissions / data.dealCount : 0,
      margin: data.margin,
      percentage: totalCommissions > 0 ? (data.totalCommissions / totalCommissions) * 100 : 0,
    }))
  }

  /**
   * Calculate territory breakdown
   */
  private calculateTerritoryBreakdown(calculations: CommissionCalculation[], deals: Deal[]): TerritoryBreakdown[] {
    // Mock implementation - in real system, would get territory data from deals/sales reps
    const territories = ["North America", "Europe", "Asia Pacific", "Latin America"]

    return territories.map((territory) => {
      // Mock data - in real implementation, filter by actual territory
      const territoryCalculations = calculations.slice(0, Math.floor(calculations.length / territories.length))
      const totalCommissions = territoryCalculations.reduce((sum, calc) => sum + calc.netCommission, 0)

      return {
        territory,
        totalCommissions,
        dealCount: territoryCalculations.length,
        averageCommission: territoryCalculations.length > 0 ? totalCommissions / territoryCalculations.length : 0,
        quotaAttainment: 95 + Math.random() * 20, // Mock quota attainment
        salesRepCount: Math.floor(Math.random() * 10) + 5,
      }
    })
  }

  /**
   * Calculate timeframe breakdown
   */
  private calculateTimeframeBreakdown(
    calculations: CommissionCalculation[],
    deals: Deal[],
    period: { start: string; end: string; type: string },
  ): TimeframeBreakdown[] {
    const timeframes: TimeframeBreakdown[] = []
    const startDate = new Date(period.start)
    const endDate = new Date(period.end)

    // Generate time periods based on period type
    let currentDate = new Date(startDate)
    let periodIndex = 0

    while (currentDate < endDate && periodIndex < 12) {
      // Limit to 12 periods for performance
      let nextDate: Date
      let periodLabel: string

      switch (period.type) {
        case "daily":
          nextDate = new Date(currentDate)
          nextDate.setDate(nextDate.getDate() + 1)
          periodLabel = currentDate.toISOString().split("T")[0]
          break
        case "weekly":
          nextDate = new Date(currentDate)
          nextDate.setDate(nextDate.getDate() + 7)
          periodLabel = `Week of ${currentDate.toISOString().split("T")[0]}`
          break
        case "monthly":
          nextDate = new Date(currentDate)
          nextDate.setMonth(nextDate.getMonth() + 1)
          periodLabel = currentDate.toLocaleDateString("en-US", { year: "numeric", month: "long" })
          break
        case "quarterly":
          nextDate = new Date(currentDate)
          nextDate.setMonth(nextDate.getMonth() + 3)
          periodLabel = `Q${Math.floor(currentDate.getMonth() / 3) + 1} ${currentDate.getFullYear()}`
          break
        default:
          nextDate = new Date(currentDate)
          nextDate.setFullYear(nextDate.getFullYear() + 1)
          periodLabel = currentDate.getFullYear().toString()
      }

      // Filter calculations for this period
      const periodCalculations = calculations.filter((calc) => {
        const calcDate = new Date(calc.calculationDate)
        return calcDate >= currentDate && calcDate < nextDate
      })

      const totalCommissions = periodCalculations.reduce((sum, calc) => sum + calc.netCommission, 0)

      timeframes.push({
        period: periodLabel,
        totalCommissions,
        dealCount: periodCalculations.length,
        quotaAttainment: this.calculateQuotaAttainment(periodCalculations),
        growth: periodIndex > 0 ? Math.random() * 20 - 10 : 0, // Mock growth calculation
      })

      currentDate = nextDate
      periodIndex++
    }

    return timeframes
  }

  /**
   * Calculate commission trend
   */
  private calculateCommissionTrend(
    calculations: CommissionCalculation[],
    period: { start: string; end: string; type: string },
  ): TrendData[] {
    // Group calculations by time period
    const trendMap = new Map<string, { value: number; count: number }>()

    calculations.forEach((calc) => {
      const date = new Date(calc.calculationDate)
      let periodKey: string

      switch (period.type) {
        case "daily":
          periodKey = date.toISOString().split("T")[0]
          break
        case "weekly":
          const weekStart = new Date(date)
          weekStart.setDate(date.getDate() - date.getDay())
          periodKey = weekStart.toISOString().split("T")[0]
          break
        case "monthly":
          periodKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}`
          break
        default:
          periodKey = date.getFullYear().toString()
      }

      const existing = trendMap.get(periodKey) || { value: 0, count: 0 }
      trendMap.set(periodKey, {
        value: existing.value + calc.netCommission,
        count: existing.count + 1,
      })
    })

    return Array.from(trendMap.entries())
      .sort(([a], [b]) => a.localeCompare(b))
      .map(([period, data]) => ({
        period,
        value: data.value,
        target: data.value * 1.1, // Mock target (10% higher)
      }))
  }

  /**
   * Calculate deal volume trend
   */
  private calculateDealVolumeTrend(deals: Deal[], period: { start: string; end: string; type: string }): TrendData[] {
    const trendMap = new Map<string, number>()

    deals.forEach((deal) => {
      const date = new Date(deal.closeDate)
      let periodKey: string

      switch (period.type) {
        case "daily":
          periodKey = date.toISOString().split("T")[0]
          break
        case "weekly":
          const weekStart = new Date(date)
          weekStart.setDate(date.getDate() - date.getDay())
          periodKey = weekStart.toISOString().split("T")[0]
          break
        case "monthly":
          periodKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}`
          break
        default:
          periodKey = date.getFullYear().toString()
      }

      trendMap.set(periodKey, (trendMap.get(periodKey) || 0) + 1)
    })

    return Array.from(trendMap.entries())
      .sort(([a], [b]) => a.localeCompare(b))
      .map(([period, value]) => ({
        period,
        value,
      }))
  }

  /**
   * Calculate quota attainment trend
   */
  private calculateQuotaAttainmentTrend(
    calculations: CommissionCalculation[],
    period: { start: string; end: string; type: string },
  ): TrendData[] {
    // Mock implementation - in real system, would calculate based on actual quotas
    const trendData: TrendData[] = []
    const startDate = new Date(period.start)
    const endDate = new Date(period.end)

    const currentDate = new Date(startDate)
    while (currentDate < endDate) {
      const periodKey = currentDate.toISOString().split("T")[0]
      const quotaAttainment = 80 + Math.random() * 40 // Mock data between 80-120%

      trendData.push({
        period: periodKey,
        value: quotaAttainment,
        target: 100,
      })

      currentDate.setDate(currentDate.getDate() + 7) // Weekly intervals
    }

    return trendData
  }

  /**
   * Calculate conversion rate trend
   */
  private calculateConversionRateTrend(
    deals: Deal[],
    period: { start: string; end: string; type: string },
  ): TrendData[] {
    const trendMap = new Map<string, { total: number; won: number }>()

    deals.forEach((deal) => {
      const date = new Date(deal.closeDate)
      const periodKey = date.toISOString().split("T")[0]

      const existing = trendMap.get(periodKey) || { total: 0, won: 0 }
      trendMap.set(periodKey, {
        total: existing.total + 1,
        won: existing.won + (deal.status === "won" ? 1 : 0),
      })
    })

    return Array.from(trendMap.entries())
      .sort(([a], [b]) => a.localeCompare(b))
      .map(([period, data]) => ({
        period,
        value: data.total > 0 ? (data.won / data.total) * 100 : 0,
      }))
  }

  /**
   * Calculate top sales reps
   */
  private calculateTopSalesReps(calculations: CommissionCalculation[], deals: Deal[]): SalesRepPerformance[] {
    const repMap = new Map<string, { totalCommissions: number; dealCount: number; totalDealValue: number }>()

    calculations.forEach((calc) => {
      const existing = repMap.get(calc.salesRepId) || { totalCommissions: 0, dealCount: 0, totalDealValue: 0 }
      const deal = deals.find((d) => d.id === calc.dealId)

      repMap.set(calc.salesRepId, {
        totalCommissions: existing.totalCommissions + calc.netCommission,
        dealCount: existing.dealCount + 1,
        totalDealValue: existing.totalDealValue + (deal?.value || 0),
      })
    })

    const reps = Array.from(repMap.entries()).map(([salesRepId, data]) => ({
      salesRepId,
      name: this.getSalesRepName(salesRepId), // Mock function
      totalCommissions: data.totalCommissions,
      dealCount: data.dealCount,
      averageCommission: data.dealCount > 0 ? data.totalCommissions / data.dealCount : 0,
      quotaAttainment: 90 + Math.random() * 30, // Mock quota attainment
      rank: 0, // Will be set after sorting
      growth: Math.random() * 40 - 20, // Mock growth rate
      efficiency: data.totalDealValue > 0 ? (data.totalCommissions / data.totalDealValue) * 100 : 0,
    }))

    // Sort by total commissions and assign ranks
    reps.sort((a, b) => b.totalCommissions - a.totalCommissions)
    reps.forEach((rep, index) => {
      rep.rank = index + 1
    })

    return reps.slice(0, 10) // Top 10
  }

  /**
   * Calculate top deals
   */
  private calculateTopDeals(calculations: CommissionCalculation[], deals: Deal[]): TopDeal[] {
    const dealMap = new Map<string, Deal>()
    deals.forEach((deal) => dealMap.set(deal.id, deal))

    const topDeals = calculations
      .map((calc) => {
        const deal = dealMap.get(calc.dealId)
        if (!deal) return null

        return {
          dealId: calc.dealId,
          dealName: deal.metadata?.name || `Deal ${calc.dealId}`,
          company: deal.metadata?.company || "Unknown Company",
          value: deal.value,
          commission: calc.netCommission,
          salesRepId: calc.salesRepId,
          salesRepName: this.getSalesRepName(calc.salesRepId),
          closeDate: deal.closeDate,
          dealType: deal.dealType,
        }
      })
      .filter((deal): deal is TopDeal => deal !== null)
      .sort((a, b) => b.commission - a.commission)
      .slice(0, 10)

    return topDeals
  }

  /**
   * Calculate top products
   */
  private calculateTopProducts(calculations: CommissionCalculation[], deals: Deal[]): TopProduct[] {
    const dealMap = new Map<string, Deal>()
    deals.forEach((deal) => dealMap.set(deal.id, deal))

    const productMap = new Map<
      string,
      {
        name: string
        category: string
        totalRevenue: number
        totalCommissions: number
        dealCount: number
        margin: number
      }
    >()

    calculations.forEach((calc) => {
      const deal = dealMap.get(calc.dealId)
      if (deal && deal.products) {
        deal.products.forEach((product) => {
          const existing = productMap.get(product.id) || {
            name: product.name,
            category: product.category,
            totalRevenue: 0,
            totalCommissions: 0,
            dealCount: 0,
            margin: product.margin || 0,
          }

          const productCommission = (calc.netCommission * product.totalPrice) / deal.value

          productMap.set(product.id, {
            ...existing,
            totalRevenue: existing.totalRevenue + product.totalPrice,
            totalCommissions: existing.totalCommissions + productCommission,
            dealCount: existing.dealCount + 1,
          })
        })
      }
    })

    return Array.from(productMap.entries())
      .map(([productId, data]) => ({
        productId,
        productName: data.name,
        category: data.category,
        totalRevenue: data.totalRevenue,
        totalCommissions: data.totalCommissions,
        dealCount: data.dealCount,
        averageCommission: data.dealCount > 0 ? data.totalCommissions / data.dealCount : 0,
        margin: data.margin,
      }))
      .sort((a, b) => b.totalCommissions - a.totalCommissions)
      .slice(0, 10)
  }

  /**
   * Generate actionable insights
   */
  private generateInsights(
    calculations: CommissionCalculation[],
    deals: Deal[],
    summary: any,
    trends: any,
  ): AnalyticsInsight[] {
    const insights: AnalyticsInsight[] = []

    // Growth opportunity insight
    if (summary.growthRate > 20) {
      insights.push({
        id: uuidv4(),
        type: "opportunity",
        title: "Strong Growth Momentum",
        description: `Commission earnings have grown by ${summary.growthRate.toFixed(1)}% compared to the previous period.`,
        impact: "high",
        actionable: true,
        recommendation: "Consider increasing sales targets and expanding successful strategies to other territories.",
        data: { growthRate: summary.growthRate },
      })
    }

    // Conversion rate insight
    if (summary.conversionRate < 20) {
      insights.push({
        id: uuidv4(),
        type: "risk",
        title: "Low Conversion Rate",
        description: `Current conversion rate of ${summary.conversionRate.toFixed(1)}% is below industry standards.`,
        impact: "high",
        actionable: true,
        recommendation: "Review lead qualification process and provide additional sales training.",
        data: { conversionRate: summary.conversionRate },
      })
    }

    // Quota attainment insight
    if (summary.quotaAttainment < 80) {
      insights.push({
        id: uuidv4(),
        type: "risk",
        title: "Quota Attainment Below Target",
        description: `Current quota attainment of ${summary.quotaAttainment.toFixed(1)}% indicates potential challenges.`,
        impact: "medium",
        actionable: true,
        recommendation: "Analyze pipeline health and consider adjusting quotas or providing additional support.",
        data: { quotaAttainment: summary.quotaAttainment },
      })
    }

    // Deal size trend insight
    if (summary.averageDealSize > 50000) {
      insights.push({
        id: uuidv4(),
        type: "opportunity",
        title: "Large Deal Focus Paying Off",
        description: `Average deal size of $${summary.averageDealSize.toLocaleString()} indicates successful enterprise focus.`,
        impact: "medium",
        actionable: true,
        recommendation: "Continue focusing on enterprise deals and consider expanding enterprise sales team.",
        data: { averageDealSize: summary.averageDealSize },
      })
    }

    // Seasonal trend insight
    const commissionTrend = trends.commissionTrend
    if (commissionTrend.length >= 3) {
      const recentTrend = commissionTrend.slice(-3)
      const isIncreasing = recentTrend.every(
        (point, index) => index === 0 || point.value > recentTrend[index - 1].value,
      )

      if (isIncreasing) {
        insights.push({
          id: uuidv4(),
          type: "trend",
          title: "Positive Commission Trend",
          description: "Commission earnings have been consistently increasing over the last 3 periods.",
          impact: "medium",
          actionable: false,
          data: { trend: "increasing", periods: 3 },
        })
      }
    }

    return insights
  }

  /**
   * Generate forecasts
   */
  private generateForecasts(calculations: CommissionCalculation[], deals: Deal[], trends: any) {
    // Simple linear regression for next period projection
    const commissionTrend = trends.commissionTrend
    if (commissionTrend.length < 2) {
      return {
        nextPeriodProjection: 0,
        confidenceInterval: [0, 0] as [number, number],
        factors: [],
      }
    }

    // Calculate trend slope
    const values = commissionTrend.map((point: TrendData) => point.value)
    const n = values.length
    const sumX = (n * (n - 1)) / 2
    const sumY = values.reduce((sum, val) => sum + val, 0)
    const sumXY = values.reduce((sum, val, index) => sum + val * index, 0)
    const sumX2 = (n * (n - 1) * (2 * n - 1)) / 6

    const slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX)
    const intercept = (sumY - slope * sumX) / n

    const nextPeriodProjection = slope * n + intercept
    const confidenceInterval: [number, number] = [nextPeriodProjection * 0.8, nextPeriodProjection * 1.2]

    const factors: ForecastFactor[] = [
      {
        factor: "Historical Trend",
        impact: Math.abs(slope) / nextPeriodProjection,
        confidence: 0.7,
        description: "Based on commission trend over recent periods",
      },
      {
        factor: "Seasonal Patterns",
        impact: 0.1,
        confidence: 0.5,
        description: "Seasonal variations in sales performance",
      },
      {
        factor: "Pipeline Health",
        impact: 0.15,
        confidence: 0.6,
        description: "Current pipeline value and conversion rates",
      },
    ]

    return {
      nextPeriodProjection: Math.max(0, nextPeriodProjection),
      confidenceInterval,
      factors,
    }
  }

  /**
   * Calculate quota attainment for a set of calculations
   */
  private calculateQuotaAttainment(calculations: CommissionCalculation[]): number {
    // Mock implementation - in real system, would compare against actual quotas
    const totalCommissions = calculations.reduce((sum, calc) => sum + calc.netCommission, 0)
    const mockQuota = totalCommissions * (0.9 + Math.random() * 0.3) // Mock quota around actual performance
    return mockQuota > 0 ? (totalCommissions / mockQuota) * 100 : 0
  }

  /**
   * Get sales rep name (mock implementation)
   */
  private getSalesRepName(salesRepId: string): string {
    const names = [
      "Sarah Johnson",
      "Mike Chen",
      "Emily Rodriguez",
      "David Kim",
      "Lisa Thompson",
      "James Wilson",
      "Maria Garcia",
      "Robert Taylor",
    ]
    return names[Math.abs(salesRepId.split("").reduce((sum, char) => sum + char.charCodeAt(0), 0)) % names.length]
  }

  /**
   * Get previous period calculations (mock implementation)
   */
  private async getPreviousPeriodCalculations(period: {
    start: string
    end: string
    type: string
  }): Promise<CommissionCalculation[]> {
    // Mock implementation - in real system, would fetch from database
    return []
  }
}

// Export singleton instance
export const analyticsEngine = new CommissionAnalyticsEngine()
