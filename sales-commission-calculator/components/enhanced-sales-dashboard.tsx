"use client"

import type React from "react"

import { useState, useEffect } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Progress } from "@/components/ui/progress"
import { Separator } from "@/components/ui/separator"
import { Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, AreaChart, Area } from "recharts"
import {
  Eye,
  RefreshCw,
  CheckCircle,
  TrendingUp,
  Target,
  Calendar,
  DollarSign,
  Building2,
  User,
  Star,
  Award,
  BarChart3,
  PieChart,
  Activity,
  ArrowUpRight,
  Filter,
  Download,
  Bell,
  Settings,
} from "lucide-react"
import { DealModal } from "./deal-modal"
import { dealsApi, calculationsApi, type DealResponse, type CommissionCalculationResponse } from "@/lib/api"

const MONTH_NAMES = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"]
const STATUS_COLORS: Record<string, string> = {
  OPEN: "#3b82f6",
  WON: "#10b981",
  LOST: "#ef4444",
  CANCELLED: "#6b7280",
}

function buildEarningsData(deals: DealResponse[], calcByDeal: Map<string, CommissionCalculationResponse>) {
  const byMonth = new Map<string, { earnings: number; deals: number }>()
  deals.filter((d) => d.status === "WON").forEach((d) => {
    const date = d.closeDate || d.createdDate
    const month = date ? MONTH_NAMES[new Date(date).getMonth()] : "Unknown"
    const entry = byMonth.get(month) || { earnings: 0, deals: 0 }
    const calc = calcByDeal.get(d.id)
    entry.earnings += calc?.netCommission ?? 0
    entry.deals += 1
    byMonth.set(month, entry)
  })
  const avgEarnings = byMonth.size > 0
    ? Array.from(byMonth.values()).reduce((s, e) => s + e.earnings, 0) / byMonth.size
    : 0
  return Array.from(byMonth.entries()).map(([month, data]) => ({
    month,
    earnings: data.earnings,
    target: Math.round(avgEarnings * 1.1),
    deals: data.deals,
  }))
}

function buildPipelineData(deals: DealResponse[]) {
  const byStatus = new Map<string, { value: number; count: number }>()
  deals.forEach((d) => {
    const entry = byStatus.get(d.status) || { value: 0, count: 0 }
    entry.value += d.value
    entry.count += 1
    byStatus.set(d.status, entry)
  })
  return Array.from(byStatus.entries()).map(([stage, data]) => ({
    stage,
    value: data.value,
    count: data.count,
    color: STATUS_COLORS[stage] || "#8b5cf6",
  }))
}

function mapDealToView(deal: DealResponse, calc?: CommissionCalculationResponse) {
  return {
    id: deal.id,
    title: deal.title,
    description: deal.title,
    company: deal.salesRepId,
    contactName: deal.salesRepId,
    contactEmail: "",
    contactPhone: "",
    stage: deal.status === "WON" ? "Closed Won" : deal.status,
    probability: deal.status === "WON" ? 100 : 0,
    value: deal.value,
    startDate: deal.createdDate || "",
    closeDate: deal.closeDate || "",
    createdDate: deal.createdDate || "",
    lastModified: deal.closeDate || deal.createdDate || "",
    source: "API",
    dealType: "New Business",
    products: [
      {
        id: `prod-${deal.id}`,
        name: deal.title,
        quantity: 1,
        unitPrice: deal.value,
        totalPrice: deal.value,
      },
    ],
    commissionDetails: {
      baseCommission: calc?.baseCommission ?? 0,
      acceleratorBonus: 0,
      spifBonus: 0,
      totalCommission: calc?.netCommission ?? 0,
      commissionRate: deal.value > 0 && calc ? ((calc.baseCommission / deal.value) * 100) : 0,
      planName: calc?.planId ?? "N/A",
    },
    documents: [],
    notes: "",
    termsAndConditions: "",
    status: deal.status === "WON" ? ("won" as const) : ("open" as const),
  }
}

export function EnhancedSalesDashboard() {
  const [selectedDeal, setSelectedDeal] = useState<any | null>(null)
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [activeTab, setActiveTab] = useState("overview")
  const [closedWonDeals, setClosedWonDeals] = useState<any[]>([])
  const [earningsData, setEarningsData] = useState<any[]>([])
  const [pipelineData, setPipelineData] = useState<any[]>([])
  const [apiLoading, setApiLoading] = useState(true)
  const [apiError, setApiError] = useState<string | null>(null)

  const loadData = async () => {
    try {
      setApiLoading(true)
      const [allDeals, allCalcs] = await Promise.all([
        dealsApi.getAll(),
        calculationsApi.getAll(),
      ])
      const calcByDeal = new Map(allCalcs.map((c) => [c.dealId, c]))
      const wonDeals = allDeals.filter((d) => d.status === "WON")
      setClosedWonDeals(wonDeals.map((d) => mapDealToView(d, calcByDeal.get(d.id))))
      setEarningsData(buildEarningsData(allDeals, calcByDeal))
      setPipelineData(buildPipelineData(allDeals))
      setApiError(null)
    } catch (err: any) {
      setApiError(err.message)
      setClosedWonDeals([])
      setEarningsData([])
      setPipelineData([])
    } finally {
      setApiLoading(false)
    }
  }

  useEffect(() => {
    loadData()
  }, [])

  const formatCurrency = (amount: number) =>
    new Intl.NumberFormat("en-US", {
      style: "currency",
      currency: "USD",
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(amount)

  const formatDate = (dateString: string) =>
    new Date(dateString).toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
    })

  const handleViewDeal = (deal: any) => {
    setSelectedDeal(deal)
    setIsModalOpen(true)
  }

  const handleCloseModal = () => {
    setIsModalOpen(false)
    setSelectedDeal(null)
  }

  // Calculate enhanced metrics
  const totalEarnings = closedWonDeals.reduce((sum, deal) => sum + deal.commissionDetails.totalCommission, 0)
  const quotaTarget = 200000
  const quotaAttainment = (totalEarnings / quotaTarget) * 100
  const pipelineValue = pipelineData.reduce((sum, stage) => sum + stage.value, 0)
  const avgDealSize = closedWonDeals.reduce((sum, deal) => sum + deal.value, 0) / closedWonDeals.length
  const totalDeals = closedWonDeals.length + pipelineData.reduce((s, p) => s + p.count, 0)
  const winRate = totalDeals > 0 ? (closedWonDeals.length / totalDeals) * 100 : 0
  const dealsThisMonth = closedWonDeals.length
  const monthlyGrowth = earningsData.length >= 2
    ? ((earningsData[earningsData.length - 1]?.earnings - earningsData[earningsData.length - 2]?.earnings) / (earningsData[earningsData.length - 2]?.earnings || 1)) * 100
    : 0

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-white to-blue-50/30 p-6">
      <div className="max-w-7xl mx-auto space-y-8">
        {/* Enhanced Header */}
        <div className="flex items-center justify-between">
          <div className="space-y-2">
            <div className="flex items-center gap-3">
              <div className="p-2 bg-gradient-to-br from-blue-600 to-purple-600 rounded-xl shadow-lg">
                <BarChart3 className="w-6 h-6 text-white" />
              </div>
              <div>
                <h1 className="text-3xl font-bold bg-gradient-to-r from-slate-900 via-blue-900 to-slate-900 bg-clip-text text-transparent">
                  Sales Dashboard
                </h1>
                <p className="text-slate-600">Welcome back, Sarah! Here's your performance overview.</p>
              </div>
            </div>
          </div>
          <div className="flex items-center gap-3">
            {apiLoading ? (
              <Badge className="bg-blue-100 text-blue-700 border-blue-200 px-3 py-1.5 shadow-sm">
                <RefreshCw className="w-3 h-3 mr-1.5 animate-spin" />
                Loading from API...
              </Badge>
            ) : apiError ? (
              <Badge className="bg-red-100 text-red-700 border-red-200 px-3 py-1.5 shadow-sm">
                API Error: {apiError}
              </Badge>
            ) : (
              <Badge className="bg-emerald-100 text-emerald-700 border-emerald-200 px-3 py-1.5 shadow-sm">
                <CheckCircle className="w-3 h-3 mr-1.5" />
                Connected to API
              </Badge>
            )}
            <Button
              variant="outline"
              className="shadow-sm hover:shadow-md transition-all bg-transparent"
              onClick={() => loadData()}
            >
              <RefreshCw className="w-4 h-4 mr-2" />
              Sync Data
            </Button>
            <Button variant="outline" size="icon" className="shadow-sm hover:shadow-md transition-all bg-transparent">
              <Bell className="w-4 h-4" />
            </Button>
            <Button variant="outline" size="icon" className="shadow-sm hover:shadow-md transition-all bg-transparent">
              <Settings className="w-4 h-4" />
            </Button>
          </div>
        </div>

        {/* Enhanced Key Metrics */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <Card className="border-0 shadow-xl bg-gradient-to-br from-blue-50 to-indigo-50 hover:shadow-2xl transition-all duration-300 group">
            <CardHeader className="pb-3">
              <CardTitle className="flex items-center justify-between text-slate-700">
                <span className="text-sm font-medium">Total Earnings</span>
                <div className="p-2 bg-blue-100 rounded-lg group-hover:bg-blue-200 transition-colors">
                  <DollarSign className="w-4 h-4 text-blue-600" />
                </div>
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-3xl font-bold text-slate-900 mb-2">{formatCurrency(totalEarnings)}</div>
              <div className="flex items-center gap-2">
                <div className="flex items-center text-emerald-600 text-sm font-medium">
                  <ArrowUpRight className="w-3 h-3 mr-1" />+{monthlyGrowth}%
                </div>
                <span className="text-slate-500 text-sm">vs last month</span>
              </div>
            </CardContent>
          </Card>

          <Card className="border-0 shadow-xl bg-gradient-to-br from-emerald-50 to-teal-50 hover:shadow-2xl transition-all duration-300 group">
            <CardHeader className="pb-3">
              <CardTitle className="flex items-center justify-between text-slate-700">
                <span className="text-sm font-medium">Quota Attainment</span>
                <div className="p-2 bg-emerald-100 rounded-lg group-hover:bg-emerald-200 transition-colors">
                  <Target className="w-4 h-4 text-emerald-600" />
                </div>
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-3xl font-bold text-slate-900 mb-3">{quotaAttainment.toFixed(1)}%</div>
              <Progress value={quotaAttainment} className="h-2 mb-2" />
              <div className="text-slate-500 text-sm">
                {formatCurrency(totalEarnings)} of {formatCurrency(quotaTarget)}
              </div>
            </CardContent>
          </Card>

          <Card className="border-0 shadow-xl bg-gradient-to-br from-purple-50 to-pink-50 hover:shadow-2xl transition-all duration-300 group">
            <CardHeader className="pb-3">
              <CardTitle className="flex items-center justify-between text-slate-700">
                <span className="text-sm font-medium">Pipeline Value</span>
                <div className="p-2 bg-purple-100 rounded-lg group-hover:bg-purple-200 transition-colors">
                  <PieChart className="w-4 h-4 text-purple-600" />
                </div>
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-3xl font-bold text-slate-900 mb-2">{formatCurrency(pipelineValue)}</div>
              <div className="flex items-center gap-2">
                <Badge className="bg-purple-100 text-purple-700 text-xs px-2 py-1">
                  {pipelineData.reduce((sum, stage) => sum + stage.count, 0)} deals
                </Badge>
                <span className="text-slate-500 text-sm">in pipeline</span>
              </div>
            </CardContent>
          </Card>

          <Card className="border-0 shadow-xl bg-gradient-to-br from-amber-50 to-orange-50 hover:shadow-2xl transition-all duration-300 group">
            <CardHeader className="pb-3">
              <CardTitle className="flex items-center justify-between text-slate-700">
                <span className="text-sm font-medium">Win Rate</span>
                <div className="p-2 bg-amber-100 rounded-lg group-hover:bg-amber-200 transition-colors">
                  <Award className="w-4 h-4 text-amber-600" />
                </div>
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-3xl font-bold text-slate-900 mb-2">{winRate}%</div>
              <div className="flex items-center gap-2">
                <span className="text-slate-500 text-sm">Avg deal: {formatCurrency(avgDealSize)}</span>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Enhanced Tabs */}
        <Tabs value={activeTab} onValueChange={setActiveTab} className="space-y-6">
          <div className="flex items-center justify-between">
            <TabsList className="bg-white/80 backdrop-blur-sm border shadow-lg">
              <TabsTrigger value="overview" className="data-[state=active]:bg-blue-600 data-[state=active]:text-white">
                <Activity className="w-4 h-4 mr-2" />
                Overview
              </TabsTrigger>
              <TabsTrigger value="pipeline" className="data-[state=active]:bg-blue-600 data-[state=active]:text-white">
                <Target className="w-4 h-4 mr-2" />
                Pipeline
              </TabsTrigger>
              <TabsTrigger
                value="performance"
                className="data-[state=active]:bg-blue-600 data-[state=active]:text-white"
              >
                <TrendingUp className="w-4 h-4 mr-2" />
                Performance
              </TabsTrigger>
              <TabsTrigger value="deals" className="data-[state=active]:bg-blue-600 data-[state=active]:text-white">
                <Star className="w-4 h-4 mr-2" />
                Recent Deals
              </TabsTrigger>
            </TabsList>
            <div className="flex items-center gap-2">
              <Button variant="outline" size="sm" className="shadow-sm bg-transparent">
                <Filter className="w-4 h-4 mr-2" />
                Filter
              </Button>
              <Button variant="outline" size="sm" className="shadow-sm bg-transparent">
                <Download className="w-4 h-4 mr-2" />
                Export
              </Button>
            </div>
          </div>

          <TabsContent value="overview" className="space-y-8">
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
              {/* Enhanced Earnings Trend Chart */}
              <Card className="border-0 shadow-xl bg-white/80 backdrop-blur-sm">
                <CardHeader className="pb-6">
                  <CardTitle className="flex items-center gap-3">
                    <div className="p-2 bg-gradient-to-br from-blue-500 to-purple-600 rounded-lg">
                      <TrendingUp className="w-5 h-5 text-white" />
                    </div>
                    Earnings Trend
                  </CardTitle>
                  <CardDescription className="text-base">
                    Commission earnings vs targets over the last 6 months
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <ResponsiveContainer width="100%" height={320}>
                    <AreaChart data={earningsData}>
                      <defs>
                        <linearGradient id="earningsGradient" x1="0" y1="0" x2="0" y2="1">
                          <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.3} />
                          <stop offset="95%" stopColor="#3b82f6" stopOpacity={0} />
                        </linearGradient>
                      </defs>
                      <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                      <XAxis dataKey="month" stroke="#64748b" fontSize={12} />
                      <YAxis
                        stroke="#64748b"
                        fontSize={12}
                        tickFormatter={(value) => `$${(value / 1000).toFixed(0)}k`}
                      />
                      <Tooltip
                        formatter={(value, name) => [
                          formatCurrency(Number(value)),
                          name === "earnings" ? "Actual" : "Target",
                        ]}
                        labelStyle={{ color: "#1e293b" }}
                        contentStyle={{
                          backgroundColor: "white",
                          border: "1px solid #e2e8f0",
                          borderRadius: "8px",
                          boxShadow: "0 10px 15px -3px rgba(0, 0, 0, 0.1)",
                        }}
                      />
                      <Area
                        type="monotone"
                        dataKey="earnings"
                        stroke="#3b82f6"
                        strokeWidth={3}
                        fill="url(#earningsGradient)"
                      />
                      <Line
                        type="monotone"
                        dataKey="target"
                        stroke="#ef4444"
                        strokeWidth={2}
                        strokeDasharray="5 5"
                        dot={{ fill: "#ef4444", strokeWidth: 2, r: 4 }}
                      />
                    </AreaChart>
                  </ResponsiveContainer>
                </CardContent>
              </Card>

              {/* Enhanced Pipeline Breakdown */}
              <Card className="border-0 shadow-xl bg-white/80 backdrop-blur-sm">
                <CardHeader className="pb-6">
                  <CardTitle className="flex items-center gap-3">
                    <div className="p-2 bg-gradient-to-br from-emerald-500 to-teal-600 rounded-lg">
                      <PieChart className="w-5 h-5 text-white" />
                    </div>
                    Pipeline by Stage
                  </CardTitle>
                  <CardDescription className="text-base">Current deal distribution across sales stages</CardDescription>
                </CardHeader>
                <CardContent className="space-y-6">
                  {pipelineData.map((stage, index) => (
                    <div key={stage.stage} className="space-y-3">
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-3">
                          <div className="w-3 h-3 rounded-full shadow-sm" style={{ backgroundColor: stage.color }} />
                          <span className="font-medium text-slate-700">{stage.stage}</span>
                          <Badge variant="outline" className="text-xs">
                            {stage.count} deals
                          </Badge>
                        </div>
                        <span className="font-bold text-slate-900">{formatCurrency(stage.value)}</span>
                      </div>
                      <Progress
                        value={(stage.value / pipelineValue) * 100}
                        className="h-2"
                        style={
                          {
                            "--progress-background": stage.color,
                          } as React.CSSProperties
                        }
                      />
                    </div>
                  ))}
                  <Separator />
                  <div className="flex items-center justify-between font-bold text-lg">
                    <span className="text-slate-700">Total Pipeline</span>
                    <span className="text-slate-900">{formatCurrency(pipelineValue)}</span>
                  </div>
                </CardContent>
              </Card>
            </div>

            {/* Enhanced Activity Feed */}
            <Card className="border-0 shadow-xl bg-white/80 backdrop-blur-sm">
              <CardHeader>
                <CardTitle className="flex items-center gap-3">
                  <div className="p-2 bg-gradient-to-br from-orange-500 to-red-600 rounded-lg">
                    <Activity className="w-5 h-5 text-white" />
                  </div>
                  Recent Activity
                </CardTitle>
                <CardDescription>Latest updates and milestones</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {[
                    {
                      type: "deal_won",
                      title: "Deal closed successfully",
                      description: "Enterprise Software License - Acme Corporation",
                      amount: "$125,000",
                      time: "2 hours ago",
                      icon: CheckCircle,
                      color: "text-emerald-600 bg-emerald-100",
                    },
                    {
                      type: "quota_milestone",
                      title: "Quota milestone reached",
                      description: "87% of quarterly quota achieved",
                      amount: "+12%",
                      time: "1 day ago",
                      icon: Target,
                      color: "text-blue-600 bg-blue-100",
                    },
                    {
                      type: "deal_progress",
                      title: "Deal moved to negotiation",
                      description: "Cloud Migration Project - Global Industries",
                      amount: "$180,000",
                      time: "2 days ago",
                      icon: ArrowUpRight,
                      color: "text-purple-600 bg-purple-100",
                    },
                  ].map((activity, index) => (
                    <div
                      key={index}
                      className="flex items-center gap-4 p-4 rounded-xl bg-slate-50/50 hover:bg-slate-100/50 transition-colors"
                    >
                      <div className={`p-2 rounded-lg ${activity.color}`}>
                        <activity.icon className="w-4 h-4" />
                      </div>
                      <div className="flex-1">
                        <div className="font-medium text-slate-900">{activity.title}</div>
                        <div className="text-sm text-slate-600">{activity.description}</div>
                      </div>
                      <div className="text-right">
                        <div className="font-bold text-slate-900">{activity.amount}</div>
                        <div className="text-xs text-slate-500">{activity.time}</div>
                      </div>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="deals" className="space-y-6">
            <Card className="border-0 shadow-xl bg-white/80 backdrop-blur-sm">
              <CardHeader>
                <CardTitle className="flex items-center gap-3">
                  <div className="p-2 bg-gradient-to-br from-green-500 to-emerald-600 rounded-lg">
                    <Star className="w-5 h-5 text-white" />
                  </div>
                  Recent Closed Won Deals
                </CardTitle>
                <CardDescription>Your successfully closed deals and commission details</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {closedWonDeals.map((deal) => (
                    <div
                      key={deal.id}
                      className="group p-6 border border-slate-200 rounded-xl hover:shadow-lg hover:border-blue-200 transition-all duration-300 bg-white/50 hover:bg-white/80"
                    >
                      <div className="flex items-start justify-between">
                        <div className="flex items-start gap-4 flex-1">
                          <div className="p-3 bg-gradient-to-br from-blue-100 to-indigo-100 rounded-xl group-hover:from-blue-200 group-hover:to-indigo-200 transition-colors">
                            <Building2 className="w-6 h-6 text-blue-600" />
                          </div>
                          <div className="flex-1 space-y-3">
                            <div>
                              <h4 className="font-semibold text-lg text-slate-900 group-hover:text-blue-900 transition-colors">
                                {deal.title}
                              </h4>
                              <p className="text-slate-600 text-sm mt-1 line-clamp-2">{deal.description}</p>
                            </div>
                            <div className="flex items-center flex-wrap gap-4 text-sm">
                              <div className="flex items-center gap-2">
                                <Building2 className="w-4 h-4 text-slate-400" />
                                <span className="font-medium text-slate-700">{deal.company}</span>
                              </div>
                              <div className="flex items-center gap-2">
                                <User className="w-4 h-4 text-slate-400" />
                                <span className="text-slate-600">{deal.contactName}</span>
                              </div>
                              <div className="flex items-center gap-2">
                                <Calendar className="w-4 h-4 text-slate-400" />
                                <span className="text-slate-600">Closed: {formatDate(deal.closeDate)}</span>
                              </div>
                              <Badge className="bg-emerald-100 text-emerald-800 border-emerald-200">
                                <CheckCircle className="w-3 h-3 mr-1" />
                                {deal.stage}
                              </Badge>
                            </div>
                          </div>
                        </div>
                        <div className="text-right space-y-2">
                          <div className="text-2xl font-bold text-slate-900">{formatCurrency(deal.value)}</div>
                          <div className="text-sm text-emerald-600 font-semibold bg-emerald-50 px-3 py-1 rounded-full">
                            {formatCurrency(deal.commissionDetails.totalCommission)} commission
                          </div>
                          <Button
                            variant="outline"
                            size="sm"
                            className="mt-3 shadow-sm hover:shadow-md transition-all group-hover:border-blue-300 group-hover:text-blue-700 bg-transparent"
                            onClick={() => handleViewDeal(deal)}
                          >
                            <Eye className="w-4 h-4 mr-2" />
                            View Details
                          </Button>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </div>

      {/* Deal Details Modal */}
      <DealModal
        isOpen={isModalOpen}
        onClose={handleCloseModal}
        dealData={selectedDeal}
        userRole="sales-rep"
        onViewInHubSpot={(dealId) => window.open(`https://app.hubspot.com/deals/${dealId}`, "_blank")}
      />
    </div>
  )
}
