"use client"

import type React from "react"

import { useEffect, useState } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Progress } from "@/components/ui/progress"
import { Alert, AlertDescription } from "@/components/ui/alert"
import {
  TrendingUp,
  DollarSign,
  Target,
  AlertTriangle,
  Download,
  Calendar,
  BarChart3,
  Activity,
  Lightbulb,
} from "lucide-react"
import {
  LineChart,
  Line,
  AreaChart,
  Area,
  PieChart,
  Pie,
  Cell,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from "recharts"
import type { CommissionAnalytics } from "@/lib/analytics/types"
import { dealsApi, calculationsApi, plansApi, type DealResponse, type CommissionCalculationResponse, type CommissionPlanResponse } from "@/lib/api"

/* -------------------------------------------------------------------------- */
/*                        COLOR HELPER (pie chart palette)                    */
/* -------------------------------------------------------------------------- */

const COLORS = ["#0088FE", "#00C49F", "#FFBB28", "#FF8042", "#8884D8", "#82CA9D", "#FFC658", "#FF7C7C"]

/* -------------------------------------------------------------------------- */
/*                     Build analytics from API data                          */
/* -------------------------------------------------------------------------- */

function buildAnalytics(
  deals: DealResponse[],
  calcs: CommissionCalculationResponse[],
  plans: CommissionPlanResponse[],
): CommissionAnalytics {
  const calcByDeal = new Map(calcs.map((c) => [c.dealId, c]))
  const planMap = new Map(plans.map((p) => [p.id, p]))
  const wonDeals = deals.filter((d) => d.status === "WON")

  const totalCommissions = calcs.reduce((s, c) => s + (c.netCommission ?? 0), 0)
  const totalDeals = wonDeals.length
  const avgCommission = totalDeals > 0 ? totalCommissions / totalDeals : 0
  const avgDealSize = totalDeals > 0 ? wonDeals.reduce((s, d) => s + d.value, 0) / totalDeals : 0

  // Group deals by month for trends
  const monthNames = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"]
  const byMonth = new Map<string, { commissions: number; deals: number }>()
  wonDeals.forEach((d) => {
    const date = d.closeDate || d.createdDate
    const month = date ? monthNames[new Date(date).getMonth()] : "Unknown"
    const entry = byMonth.get(month) || { commissions: 0, deals: 0 }
    const calc = calcByDeal.get(d.id)
    entry.commissions += calc?.netCommission ?? 0
    entry.deals += 1
    byMonth.set(month, entry)
  })

  const timeframeBreakdown = Array.from(byMonth.entries()).map(([period, data]) => ({
    period,
    totalCommissions: data.commissions,
    dealCount: data.deals,
    quotaAttainment: 0,
    growth: 0,
  }))

  const commissionTrend = timeframeBreakdown.map((t) => ({
    period: t.period,
    value: t.totalCommissions,
    target: avgCommission * 1.1,
  }))

  const dealVolumeTrend = timeframeBreakdown.map((t) => ({
    period: t.period,
    value: t.dealCount,
  }))

  // Group by plan
  const byPlan = new Map<string, { name: string; commissions: number; count: number }>()
  calcs.forEach((c) => {
    const plan = planMap.get(c.planId)
    const name = plan?.name ?? c.planId ?? "Unknown"
    const entry = byPlan.get(c.planId) || { name, commissions: 0, count: 0 }
    entry.commissions += c.netCommission ?? 0
    entry.count += 1
    byPlan.set(c.planId, entry)
  })

  const planBreakdown = Array.from(byPlan.entries()).map(([id, data]) => ({
    planId: id,
    planName: data.name,
    totalCommissions: data.commissions,
    dealCount: data.count,
    averageCommission: data.count > 0 ? data.commissions / data.count : 0,
    percentage: totalCommissions > 0 ? (data.commissions / totalCommissions) * 100 : 0,
    growth: 0,
  }))

  // Group by status as deal type proxy
  const statusCounts = new Map<string, { commissions: number; count: number }>()
  deals.forEach((d) => {
    const entry = statusCounts.get(d.status) || { commissions: 0, count: 0 }
    const calc = calcByDeal.get(d.id)
    entry.commissions += calc?.netCommission ?? 0
    entry.count += 1
    statusCounts.set(d.status, entry)
  })

  const dealTypeBreakdown = Array.from(statusCounts.entries()).map(([type, data]) => ({
    dealType: type,
    totalCommissions: data.commissions,
    dealCount: data.count,
    averageCommission: data.count > 0 ? data.commissions / data.count : 0,
    percentage: totalCommissions > 0 ? (data.commissions / totalCommissions) * 100 : 0,
    conversionRate: 0,
  }))

  // Top performers by salesRepId
  const bySalesRep = new Map<string, { commissions: number; count: number }>()
  calcs.forEach((c) => {
    const entry = bySalesRep.get(c.salesRepId) || { commissions: 0, count: 0 }
    entry.commissions += c.netCommission ?? 0
    entry.count += 1
    bySalesRep.set(c.salesRepId, entry)
  })

  const salesReps = Array.from(bySalesRep.entries())
    .sort((a, b) => b[1].commissions - a[1].commissions)
    .map(([id, data], i) => ({
      salesRepId: id,
      name: id,
      totalCommissions: data.commissions,
      dealCount: data.count,
      averageCommission: data.count > 0 ? data.commissions / data.count : 0,
      quotaAttainment: 0,
      rank: i + 1,
      growth: 0,
      efficiency: 0,
    }))

  // Top deals
  const topDeals = wonDeals
    .map((d) => {
      const calc = calcByDeal.get(d.id)
      return {
        dealId: d.id,
        dealName: d.title,
        company: d.salesRepId,
        value: d.value,
        commission: calc?.netCommission ?? 0,
        salesRepId: d.salesRepId,
        salesRepName: d.salesRepId,
        closeDate: d.closeDate ?? "",
        dealType: d.status,
      }
    })
    .sort((a, b) => b.commission - a.commission)
    .slice(0, 5)

  // Insights derived from data
  const insights: CommissionAnalytics["insights"] = []
  if (totalDeals > 0) {
    const highValueDeals = wonDeals.filter((d) => d.value > avgDealSize * 1.5)
    if (highValueDeals.length > 0) {
      insights.push({
        id: "insight-1",
        type: "opportunity",
        title: `${highValueDeals.length} high-value deals above average`,
        description: `${highValueDeals.length} deals exceeded 1.5x the average deal size of ${avgDealSize.toFixed(0)}. Focus on replicating this success.`,
        impact: "high",
        actionable: true,
        recommendation: "Analyze winning strategies from high-value deals and share with the team.",
        data: { count: highValueDeals.length },
      })
    }
  }
  if (deals.filter((d) => d.status === "OPEN").length > wonDeals.length) {
    insights.push({
      id: "insight-2",
      type: "risk",
      title: "More open deals than closed",
      description: "Pipeline has more open deals than won deals. Review stalled opportunities.",
      impact: "medium",
      actionable: true,
      recommendation: "Conduct pipeline review to identify and address blockers.",
      data: { open: deals.filter((d) => d.status === "OPEN").length, won: wonDeals.length },
    })
  }

  return {
    period: { start: "", end: "", type: "monthly" },
    summary: {
      totalCommissions,
      totalDeals,
      averageCommission: avgCommission,
      averageDealSize: avgDealSize,
      conversionRate: deals.length > 0 ? (wonDeals.length / deals.length) * 100 : 0,
      quotaAttainment: 0,
      growthRate: 0,
      previousPeriodComparison: 0,
    },
    breakdowns: {
      byPlan: planBreakdown,
      byDealType: dealTypeBreakdown,
      byProduct: [],
      byTerritory: [],
      byTimeframe: timeframeBreakdown,
    },
    trends: {
      commissionTrend,
      dealVolumeTrend,
      quotaAttainmentTrend: [],
      conversionRateTrend: [],
    },
    topPerformers: { salesReps, deals: topDeals, products: [] },
    insights,
    forecasts: {
      nextPeriodProjection: totalCommissions * 1.05,
      confidenceInterval: [totalCommissions * 0.9, totalCommissions * 1.2],
      factors: [
        { factor: "Pipeline Health", impact: 0.8, confidence: 0.7, description: "Based on current open deals." },
        { factor: "Historical Trend", impact: 0.7, confidence: 0.75, description: "Based on prior period performance." },
      ],
    },
  }
}

/* -------------------------------------------------------------------------- */
/*                            DASHBOARD COMPONENT                             */
/* -------------------------------------------------------------------------- */

export function AnalyticsDashboard() {
  /* ------------------------------- State ---------------------------------- */
  const [selectedPeriod, setSelectedPeriod] = useState<"daily" | "weekly" | "monthly" | "quarterly">("monthly")
  const [analytics, setAnalytics] = useState<CommissionAnalytics | null>(null)
  const [isLoading, setIsLoading] = useState<boolean>(true)

  /* ------------------------------ API fetch ------------------------------- */
  useEffect(() => {
    const load = async () => {
      setIsLoading(true)
      try {
        const [deals, calcs, plans] = await Promise.all([
          dealsApi.getAll(),
          calculationsApi.getAll(),
          plansApi.getAll(),
        ])
        setAnalytics(buildAnalytics(deals, calcs, plans))
      } catch (err) {
        console.error("Failed to load analytics:", err)
        setAnalytics(null)
      } finally {
        setIsLoading(false)
      }
    }
    load()
  }, [selectedPeriod])

  /* ----------------------------- Utilities -------------------------------- */
  const fmtCurrency = (n: number) =>
    new Intl.NumberFormat("en-US", { style: "currency", currency: "USD", maximumFractionDigits: 0 }).format(n)

  const insightIcon = (t: string) =>
    ({
      opportunity: <TrendingUp className="h-4 w-4 text-green-500" />,
      risk: <AlertTriangle className="h-4 w-4 text-red-500" />,
      trend: <Activity className="h-4 w-4 text-blue-500" />,
    })[t as keyof any] ?? <Lightbulb className="h-4 w-4 text-yellow-500" />

  const insightBg = (t: string) =>
    ({
      opportunity: "border-green-200 bg-green-50",
      risk: "border-red-200 bg-red-50",
      trend: "border-blue-200 bg-blue-50",
    })[t as keyof any] ?? "border-yellow-200 bg-yellow-50"

  /* --------------------------- Loading / Error --------------------------- */
  if (isLoading)
    return (
      <div className="min-h-screen flex items-center justify-center">
        <span className="animate-pulse text-muted-foreground">Loading analytics…</span>
      </div>
    )

  if (!analytics)
    return (
      <div className="p-6">
        <Alert>
          <AlertTriangle className="h-4 w-4" />
          <AlertDescription>Failed to load analytics data.</AlertDescription>
        </Alert>
      </div>
    )

  /* ------------------------------- Render --------------------------------- */
  return (
    <div className="min-h-screen bg-gray-50 p-6">
      <div className="mx-auto max-w-7xl space-y-6">
        {/* ------------------------------------------------------------------ */}
        {/* Header                                                            */}
        {/* ------------------------------------------------------------------ */}
        <header className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold">Commission Analytics</h1>
            <p className="text-gray-600">Comprehensive insights into performance and trends.</p>
          </div>
          <div className="flex items-center gap-3">
            <Select value={selectedPeriod} onValueChange={(v) => setSelectedPeriod(v as any)}>
              <SelectTrigger className="w-40">
                <Calendar className="mr-2 h-4 w-4" />
                <SelectValue placeholder="Select period" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="daily">Daily</SelectItem>
                <SelectItem value="weekly">Weekly</SelectItem>
                <SelectItem value="monthly">Monthly</SelectItem>
                <SelectItem value="quarterly">Quarterly</SelectItem>
              </SelectContent>
            </Select>
            <Button variant="outline">
              <Download className="mr-2 h-4 w-4" />
              Export
            </Button>
          </div>
        </header>

        {/* ------------------------------------------------------------------ */}
        {/* Key Metrics                                                       */}
        {/* ------------------------------------------------------------------ */}
        <section className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
          {/* Total commissions */}
          <MetricCard
            title="Total Commissions"
            icon={<DollarSign className="h-4 w-4 text-muted-foreground" />}
            primary={fmtCurrency(analytics.summary.totalCommissions)}
            delta={analytics.summary.growthRate}
          />
          {/* Quota */}
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Quota Attainment</CardTitle>
              <Target className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <p className="text-2xl font-bold">{analytics.summary.quotaAttainment.toFixed(1)}%</p>
              <Progress value={analytics.summary.quotaAttainment} className="mt-2" />
              <p className="mt-1 text-xs text-muted-foreground">
                {analytics.summary.quotaAttainment >= 100 ? "Above" : "Below"} target
              </p>
            </CardContent>
          </Card>
          {/* Conversion */}
          <MetricCard
            title="Conversion Rate"
            icon={<Activity className="h-4 w-4 text-muted-foreground" />}
            primary={`${analytics.summary.conversionRate.toFixed(1)}%`}
            subtitle={`${analytics.summary.totalDeals} deals`}
          />
          {/* Avg deal */}
          <MetricCard
            title="Avg Deal Size"
            icon={<BarChart3 className="h-4 w-4 text-muted-foreground" />}
            primary={fmtCurrency(analytics.summary.averageDealSize)}
            subtitle={`Avg commission: ${fmtCurrency(analytics.summary.averageCommission)}`}
          />
        </section>

        {/* ------------------------------------------------------------------ */}
        {/* Tabs                                                              */}
        {/* ------------------------------------------------------------------ */}
        <Tabs defaultValue="trends" className="space-y-6">
          <TabsList className="grid w-full grid-cols-5">
            <TabsTrigger value="trends">Trends</TabsTrigger>
            <TabsTrigger value="breakdowns">Breakdowns</TabsTrigger>
            <TabsTrigger value="performers">Top Performers</TabsTrigger>
            <TabsTrigger value="insights">Insights</TabsTrigger>
            <TabsTrigger value="forecast">Forecast</TabsTrigger>
          </TabsList>

          {/* ----------------------------- Trends --------------------------- */}
          <TabsContent value="trends" className="space-y-6">
            <div className="grid gap-6 lg:grid-cols-2">
              <TrendCard
                title="Commission Trend"
                description="Earnings vs target"
                data={analytics.trends.commissionTrend}
                yTick={(v) => `$${((v as number) / 1_000).toFixed(0)}k`}
                lines={[
                  { dataKey: "value", stroke: "#2563eb", label: "Actual" },
                  { dataKey: "target", stroke: "#dc2626", label: "Target", dash: "5 5" },
                ]}
                type="line"
              />
              <TrendCard
                title="Deal Volume Trend"
                description="Deals closed over time"
                data={analytics.trends.dealVolumeTrend}
                type="area"
                area={{ dataKey: "value", stroke: "#10b981", fill: "#10b981" }}
              />
              <TrendCard
                title="Quota Attainment Trend"
                description="Quota performance over time"
                data={analytics.trends.quotaAttainmentTrend}
                yTick={(v) => `${v}%`}
                lines={[
                  { dataKey: "value", stroke: "#8b5cf6", label: "Actual" },
                  { dataKey: "target", stroke: "#dc2626", label: "Target", dash: "5 5" },
                ]}
                type="line"
              />
              <TrendCard
                title="Conversion Rate Trend"
                description="Win-rate over time"
                data={analytics.trends.conversionRateTrend}
                yTick={(v) => `${v}%`}
                type="area"
                area={{ dataKey: "value", stroke: "#f59e0b", fill: "#f59e0b" }}
              />
            </div>
          </TabsContent>

          {/* -------------------------- Breakdowns ------------------------- */}
          <TabsContent value="breakdowns" className="space-y-6">
            <div className="grid gap-6 lg:grid-cols-2">
              {/* By plan */}
              <PieBreakdownCard
                title="Commission by Plan"
                description="Breakdown of commissions by plan"
                data={analytics.breakdowns.byPlan}
                dataKey="totalCommissions"
                nameKey="planName"
              />
              {/* By deal type */}
              <PieBreakdownCard
                title="Commission by Deal Type"
                description="Breakdown by deal type"
                data={analytics.breakdowns.byDealType}
                dataKey="totalCommissions"
                nameKey="dealType"
              />
            </div>
          </TabsContent>

          {/* ----------------------- Top Performers ------------------------ */}
          <TabsContent value="performers" className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle>Top Sales Reps</CardTitle>
                <CardDescription>Ranked by total commission</CardDescription>
              </CardHeader>
              <CardContent className="space-y-2">
                {analytics.topPerformers.salesReps.map((rep) => (
                  <div key={rep.salesRepId} className="flex items-center justify-between rounded-md border p-2">
                    <span>
                      #{rep.rank} – {rep.name}
                    </span>
                    <span className="font-medium">{fmtCurrency(rep.totalCommissions)}</span>
                  </div>
                ))}
              </CardContent>
            </Card>
          </TabsContent>

          {/* --------------------------- Insights -------------------------- */}
          <TabsContent value="insights" className="space-y-6">
            {analytics.insights.map((insight) => (
              <Alert key={insight.id} className={`border ${insightBg(insight.type)}`}>
                {insightIcon(insight.type)}
                <AlertDescription>
                  <strong>{insight.title}: </strong>
                  {insight.description}
                </AlertDescription>
              </Alert>
            ))}
          </TabsContent>

          {/* --------------------------- Forecast ------------------------- */}
          <TabsContent value="forecast">
            <Card>
              <CardHeader>
                <CardTitle>Next Period Projection</CardTitle>
                <CardDescription>
                  Confidence interval: {fmtCurrency(analytics.forecasts.confidenceInterval[0])} –{" "}
                  {fmtCurrency(analytics.forecasts.confidenceInterval[1])}
                </CardDescription>
              </CardHeader>
              <CardContent>
                <p className="text-3xl font-bold">{fmtCurrency(analytics.forecasts.nextPeriodProjection)}</p>
                <ul className="mt-4 space-y-1 text-sm text-muted-foreground">
                  {analytics.forecasts.factors.map((f) => (
                    <li key={f.factor}>
                      • {f.factor} – impact {(f.impact * 100).toFixed(0)}%, confidence {(f.confidence * 100).toFixed(0)}
                      %
                    </li>
                  ))}
                </ul>
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </div>
    </div>
  )
}

/* -------------------------------------------------------------------------- */
/*                               Helper Cards                                 */
/* -------------------------------------------------------------------------- */

function MetricCard({
  title,
  icon,
  primary,
  delta,
  subtitle,
}: {
  title: string
  icon: React.ReactNode
  primary: string
  delta?: number
  subtitle?: string
}) {
  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <CardTitle className="text-sm font-medium">{title}</CardTitle>
        {icon}
      </CardHeader>
      <CardContent>
        <p className="text-2xl font-bold">{primary}</p>
        {delta !== undefined ? (
          <p className="flex items-center text-xs mt-1 text-green-600">
            <TrendingUp className="mr-1 h-3 w-3" />+{delta.toFixed(1)}% from last period
          </p>
        ) : null}
        {subtitle && <p className="text-xs text-muted-foreground mt-1">{subtitle}</p>}
      </CardContent>
    </Card>
  )
}

function TrendCard({
  title,
  description,
  data,
  type,
  lines,
  area,
  yTick,
}: {
  title: string
  description: string
  data: any[]
  type: "line" | "area"
  lines?: { dataKey: string; stroke: string; label?: string; dash?: string }[]
  area?: { dataKey: string; stroke: string; fill: string }
  yTick?: (v: any) => string
}) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>{title}</CardTitle>
        <CardDescription>{description}</CardDescription>
      </CardHeader>
      <CardContent>
        <ResponsiveContainer width="100%" height={300}>
          {type === "line" ? (
            <LineChart data={data}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="period" />
              <YAxis tickFormatter={yTick} />
              <Tooltip />
              {lines?.map((l) => (
                <Line
                  key={l.dataKey}
                  type="monotone"
                  dataKey={l.dataKey}
                  stroke={l.stroke}
                  strokeDasharray={l.dash}
                  name={l.label}
                  strokeWidth={2}
                />
              ))}
            </LineChart>
          ) : (
            <AreaChart data={data}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="period" />
              <YAxis tickFormatter={yTick} />
              <Tooltip />
              {area && (
                <Area type="monotone" dataKey={area.dataKey} stroke={area.stroke} fill={area.fill} fillOpacity={0.3} />
              )}
            </AreaChart>
          )}
        </ResponsiveContainer>
      </CardContent>
    </Card>
  )
}

function PieBreakdownCard({
  title,
  description,
  data,
  dataKey,
  nameKey,
}: {
  title: string
  description: string
  data: any[]
  dataKey: string
  nameKey: string
}) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>{title}</CardTitle>
        <CardDescription>{description}</CardDescription>
      </CardHeader>
      <CardContent>
        <ResponsiveContainer width="100%" height={300}>
          <PieChart>
            <Pie
              data={data}
              dataKey={dataKey}
              nameKey={nameKey}
              cx="50%"
              cy="50%"
              outerRadius={100}
              label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(0)}%`}
            >
              {data.map((_, i) => (
                <Cell key={i} fill={COLORS[i % COLORS.length]} />
              ))}
            </Pie>
            <Tooltip />
          </PieChart>
        </ResponsiveContainer>
      </CardContent>
    </Card>
  )
}
