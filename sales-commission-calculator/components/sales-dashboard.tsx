"use client"

import { useState, useEffect } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Progress } from "@/components/ui/progress"
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from "recharts"
import {
  Eye,
  RefreshCw,
  AlertCircle,
  CheckCircle,
  Clock,
  TrendingUp,
  Target,
  Calendar,
  DollarSign,
  Building2,
  User,
  Plus,
} from "lucide-react"
import { DealModal } from "./deal-modal"
import { DisputeForm } from "./dispute-form"
import { DisputeList } from "./dispute-list"
import { DisputeDetail } from "./dispute-detail"
import type { Dispute } from "@/lib/dispute-workflow/types"
import { dealsApi, calculationsApi, disputesApi, mapApiDisputeToLocal, type DealResponse, type CommissionCalculationResponse } from "@/lib/api"

function mapDealForView(deal: DealResponse, calc?: CommissionCalculationResponse) {
  const isWon = deal.status === "WON"
  return {
    id: deal.id,
    title: deal.title,
    description: deal.title,
    company: deal.salesRepId,
    contactName: deal.salesRepId,
    contactEmail: "",
    contactPhone: "",
    stage: isWon ? "Closed Won" : deal.status,
    probability: isWon ? 100 : 50,
    value: deal.value,
    startDate: deal.createdDate || "",
    closeDate: deal.closeDate || "",
    createdDate: deal.createdDate || "",
    lastModified: deal.closeDate || deal.createdDate || "",
    source: "API",
    dealType: "New Business",
    products: [{ id: `prod-${deal.id}`, name: deal.title, quantity: 1, unitPrice: deal.value, totalPrice: deal.value }],
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
    status: isWon ? ("won" as const) : ("open" as const),
  }
}

export function SalesDashboard() {
  const [selectedDeal, setSelectedDeal] = useState<any | null>(null)
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [activeTab, setActiveTab] = useState("overview")

  // Dispute workflow states
  const [disputeView, setDisputeView] = useState<"list" | "form" | "detail">("list")
  const [selectedDispute, setSelectedDispute] = useState<Dispute | null>(null)

  // API-driven state
  const [closedWonDeals, setClosedWonDeals] = useState<any[]>([])
  const [pipelineDeals, setPipelineDeals] = useState<any[]>([])
  const [earningsData, setEarningsData] = useState<{ month: string; earnings: number }[]>([])
  const [commissionHistory, setCommissionHistory] = useState<any[]>([])

  useEffect(() => {
    async function loadData() {
      try {
        const [allDeals, allCalcs] = await Promise.all([
          dealsApi.getAll(),
          calculationsApi.getAll(),
        ])

        const calcsByDealId = new Map<string, CommissionCalculationResponse>()
        for (const calc of allCalcs) {
          calcsByDealId.set(calc.dealId, calc)
        }

        const wonDeals = allDeals
          .filter((d: DealResponse) => d.status === "WON")
          .map((d: DealResponse) => mapDealForView(d, calcsByDealId.get(d.id)))

        const openDeals = allDeals
          .filter((d: DealResponse) => d.status === "OPEN")
          .map((d: DealResponse) => mapDealForView(d, calcsByDealId.get(d.id)))

        setClosedWonDeals(wonDeals)
        setPipelineDeals(openDeals)

        // Build earningsData grouped by month from won deals
        const monthMap = new Map<string, number>()
        for (const deal of wonDeals) {
          const dateStr = deal.closeDate || deal.createdDate
          if (dateStr) {
            const date = new Date(dateStr)
            const monthKey = date.toLocaleDateString("en-US", { year: "numeric", month: "short" })
            monthMap.set(monthKey, (monthMap.get(monthKey) || 0) + deal.commissionDetails.totalCommission)
          }
        }
        const earnings = Array.from(monthMap.entries()).map(([month, earnings]) => ({ month, earnings }))
        setEarningsData(earnings)

        // Build commissionHistory from calculations
        const history = allCalcs.map((calc: CommissionCalculationResponse) => {
          const deal = allDeals.find((d: DealResponse) => d.id === calc.dealId)
          return {
            id: calc.id,
            dealId: calc.dealId,
            dealName: deal?.title ?? calc.dealId,
            amount: calc.netCommission,
            status: calc.status,
            paidDate: null,
            dueDate: calc.calculationDate,
            quarter: "Q1",
          }
        })
        setCommissionHistory(history)
      } catch (error) {
        console.error("Failed to load dashboard data:", error)
      }
    }

    loadData()
  }, [])

  const formatCurrency = (amount: number) =>
    new Intl.NumberFormat("en-US", {
      style: "currency",
      currency: "USD",
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

  const handleViewDealFromHistory = (commission: any) => {
    // First try to find in closed deals
    let deal = closedWonDeals.find((d) => d.id === commission.dealId)

    // If not found in closed deals, try pipeline deals
    if (!deal) {
      deal = pipelineDeals.find((d) => d.id === commission.dealId)
    }

    if (deal) {
      handleViewDeal(deal)
    }
  }

  const handleCloseModal = () => {
    setIsModalOpen(false)
    setSelectedDeal(null)
  }

  // Dispute handlers
  const handleCreateDispute = () => {
    setDisputeView("form")
  }

  const handleDisputeCreated = (disputeId: string) => {
    disputesApi.get(disputeId).then((data) => {
      const dispute = mapApiDisputeToLocal(data)
      setSelectedDispute(dispute)
      setDisputeView("detail")
    })
  }

  const handleViewDispute = (dispute: Dispute) => {
    setSelectedDispute(dispute)
    setDisputeView("detail")
  }

  const handleBackToDisputeList = () => {
    setDisputeView("list")
    setSelectedDispute(null)
  }

  const handleCancelDisputeForm = () => {
    setDisputeView("list")
  }

  const getStageColor = (stage: string) => {
    switch (stage) {
      case "Discovery":
        return "bg-blue-100 text-blue-800"
      case "Proposal":
        return "bg-yellow-100 text-yellow-800"
      case "Negotiation":
        return "bg-orange-100 text-orange-800"
      case "Closed Won":
        return "bg-green-100 text-green-800"
      case "Closed Lost":
        return "bg-red-100 text-red-800"
      default:
        return "bg-gray-100 text-gray-800"
    }
  }

  const getStageIcon = (stage: string) => {
    switch (stage) {
      case "Closed Won":
        return <CheckCircle className="w-3 h-3" />
      case "Closed Lost":
      case "Negotiation":
        return <AlertCircle className="w-3 h-3" />
      default:
        return <Clock className="w-3 h-3" />
    }
  }

  const getCommissionStatusColor = (status: string) => {
    switch (status) {
      case "paid":
        return "bg-green-100 text-green-800"
      case "approved":
        return "bg-blue-100 text-blue-800"
      case "pending":
        return "bg-yellow-100 text-yellow-800"
      case "rejected":
        return "bg-red-100 text-red-800"
      default:
        return "bg-gray-100 text-gray-800"
    }
  }

  const getCommissionStatusIcon = (status: string) => {
    switch (status) {
      case "paid":
      case "approved":
        return <CheckCircle className="w-3 h-3" />
      case "pending":
        return <Clock className="w-3 h-3" />
      case "rejected":
        return <AlertCircle className="w-3 h-3" />
      default:
        return <Clock className="w-3 h-3" />
    }
  }

  // Calculate metrics
  const totalEarnings = closedWonDeals.reduce((sum, deal) => sum + deal.commissionDetails.totalCommission, 0)
  const quotaTarget = 200000
  const quotaAttainment = (totalEarnings / quotaTarget) * 100
  const pipelineProjection = pipelineDeals.reduce(
    (sum, deal) => sum + (deal.commissionDetails.totalCommission * deal.probability) / 100,
    0,
  )
  const nextPayoutAmount = commissionHistory
    .filter((c) => c.status === "approved" || c.status === "pending")
    .reduce((sum, c) => sum + c.amount, 0)

  return (
    <div className="min-h-screen bg-gray-50 p-6">
      <div className="max-w-7xl mx-auto space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Commission Dashboard</h1>
            <p className="text-gray-600 mt-1">Welcome back, Sarah! Here's your commission overview.</p>
          </div>
          <div className="flex items-center gap-3">
            <Badge variant="outline" className="text-green-600 border-green-200">
              <RefreshCw className="w-3 h-3 mr-1" />
              Synced 2 min ago
            </Badge>
            <Button variant="outline">
              <Eye className="w-4 h-4 mr-2" />
              View Details
            </Button>
          </div>
        </div>

        {/* Key Metrics */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium text-gray-600">Total Earnings</CardTitle>
              <DollarSign className="h-4 w-4 text-gray-400" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{formatCurrency(totalEarnings)}</div>
              <div className="flex items-center text-xs text-green-600 mt-1">
                <TrendingUp className="w-3 h-3 mr-1" />
                +12.5% from last month
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium text-gray-600">Quota Attainment</CardTitle>
              <Target className="h-4 w-4 text-gray-400" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{quotaAttainment.toFixed(2)}%</div>
              <Progress value={quotaAttainment} className="mt-2" />
              <div className="text-xs text-gray-500 mt-1">Quota Progress</div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium text-gray-600">Pipeline Projection</CardTitle>
              <TrendingUp className="h-4 w-4 text-gray-400" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{formatCurrency(pipelineProjection)}</div>
              <div className="text-xs text-gray-500 mt-1">Based on open opportunities</div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium text-gray-600">Next Payout</CardTitle>
              <Calendar className="h-4 w-4 text-gray-400" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">Jan 31</div>
              <div className="text-xs text-gray-500 mt-1">{formatCurrency(nextPayoutAmount)} estimated</div>
            </CardContent>
          </Card>
        </div>

        {/* Tabs */}
        <Tabs value={activeTab} onValueChange={setActiveTab} className="space-y-6">
          <TabsList className="grid w-full grid-cols-4">
            <TabsTrigger value="overview">Overview</TabsTrigger>
            <TabsTrigger value="pipeline">Pipeline</TabsTrigger>
            <TabsTrigger value="history">History</TabsTrigger>
            <TabsTrigger value="disputes">Disputes</TabsTrigger>
          </TabsList>

          <TabsContent value="overview" className="space-y-6">
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              {/* Earnings Trend Chart */}
              <Card>
                <CardHeader>
                  <CardTitle>Earnings Trend</CardTitle>
                  <CardDescription>Your commission earnings over the last 6 months</CardDescription>
                </CardHeader>
                <CardContent>
                  <ResponsiveContainer width="100%" height={300}>
                    <LineChart data={earningsData}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="month" />
                      <YAxis />
                      <Tooltip formatter={(value) => [formatCurrency(Number(value)), "Earnings"]} />
                      <Line
                        type="monotone"
                        dataKey="earnings"
                        stroke="#ef4444"
                        strokeWidth={2}
                        dot={{ fill: "#ef4444", strokeWidth: 2, r: 4 }}
                      />
                    </LineChart>
                  </ResponsiveContainer>
                </CardContent>
              </Card>

              {/* Annual Quota Progress */}
              <Card>
                <CardHeader>
                  <CardTitle>Annual Quota Progress</CardTitle>
                  <CardDescription>Quarterly progress towards annual targets</CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="space-y-3">
                    <div className="flex justify-between items-center">
                      <span className="text-sm font-medium">Q1</span>
                      <span className="text-sm text-gray-500">$119,500 / $135,000</span>
                    </div>
                    <Progress value={88.5} className="h-2" />
                    <div className="text-right text-xs text-gray-500">88%</div>
                  </div>

                  <div className="space-y-3">
                    <div className="flex justify-between items-center">
                      <span className="text-sm font-medium">Q2</span>
                      <span className="text-sm text-gray-500">$92,750 / $135,000</span>
                    </div>
                    <Progress value={68.7} className="h-2" />
                    <div className="text-right text-xs text-gray-500">69%</div>
                  </div>

                  <div className="space-y-3">
                    <div className="flex justify-between items-center">
                      <span className="text-sm font-medium">Q3</span>
                      <span className="text-sm text-gray-500">$0 / $135,000</span>
                    </div>
                    <Progress value={0} className="h-2" />
                    <div className="text-right text-xs text-gray-500">0%</div>
                  </div>

                  <div className="space-y-3">
                    <div className="flex justify-between items-center">
                      <span className="text-sm font-medium">Q4</span>
                      <span className="text-sm text-gray-500">$0 / $135,000</span>
                    </div>
                    <Progress value={0} className="h-2" />
                    <div className="text-right text-xs text-gray-500">0%</div>
                  </div>

                  <div className="pt-4 border-t">
                    <div className="space-y-3">
                      <div className="flex justify-between items-center">
                        <span className="text-sm font-medium">Annual Total</span>
                        <span className="text-sm text-gray-500">$212,250 / $540,000</span>
                      </div>
                      <Progress value={39.3} className="h-2" />
                      <div className="text-right text-xs text-gray-500">39%</div>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </div>

            {/* Recent Closed Won Deals */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <CheckCircle className="w-5 h-5 text-green-600" />
                  Recent Closed Won Deals
                </CardTitle>
                <CardDescription>Your recently closed deals and commission information</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {closedWonDeals.map((deal) => (
                    <div
                      key={deal.id}
                      className="flex items-center justify-between p-4 border rounded-lg hover:bg-gray-50"
                    >
                      <div className="flex items-center space-x-4">
                        <div className="flex-shrink-0">
                          <Building2 className="w-8 h-8 text-gray-400" />
                        </div>
                        <div>
                          <h4 className="font-medium text-gray-900">{deal.title}</h4>
                          <div className="flex items-center space-x-4 text-sm text-gray-500">
                            <span className="flex items-center">
                              <Building2 className="w-3 h-3 mr-1" />
                              {deal.company}
                            </span>
                            <span className="flex items-center">
                              <User className="w-3 h-3 mr-1" />
                              {deal.contactName}
                            </span>
                            <Badge className={`${getStageColor(deal.stage)} flex items-center gap-1`}>
                              {getStageIcon(deal.stage)}
                              {deal.stage}
                            </Badge>
                            <span className="flex items-center">
                              <Calendar className="w-3 h-3 mr-1" />
                              Closed: {formatDate(deal.closeDate)}
                            </span>
                          </div>
                        </div>
                      </div>
                      <div className="text-right">
                        <div className="font-semibold text-lg">{formatCurrency(deal.value)}</div>
                        <div className="text-sm text-green-600 font-medium">
                          {formatCurrency(deal.commissionDetails.totalCommission)} commission
                        </div>
                        <Button
                          variant="outline"
                          size="sm"
                          className="mt-2 bg-transparent"
                          onClick={() => handleViewDeal(deal)}
                        >
                          <Eye className="w-3 h-3 mr-1" />
                          View Details
                        </Button>
                      </div>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="pipeline" className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle>Pipeline Deals</CardTitle>
                <CardDescription>Your current pipeline and projected commissions</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {pipelineDeals.map((deal) => (
                    <div
                      key={deal.id}
                      className="flex items-center justify-between p-4 border rounded-lg hover:bg-gray-50"
                    >
                      <div className="flex items-center space-x-4">
                        <div className="flex-shrink-0">
                          <Building2 className="w-8 h-8 text-gray-400" />
                        </div>
                        <div>
                          <h4 className="font-medium text-gray-900">{deal.title}</h4>
                          <div className="flex items-center space-x-4 text-sm text-gray-500">
                            <span className="flex items-center">
                              <Building2 className="w-3 h-3 mr-1" />
                              {deal.company}
                            </span>
                            <span className="flex items-center">
                              <User className="w-3 h-3 mr-1" />
                              {deal.contactName}
                            </span>
                            <Badge className={`${getStageColor(deal.stage)} flex items-center gap-1`}>
                              {getStageIcon(deal.stage)}
                              {deal.stage}
                            </Badge>
                            <span className="flex items-center">
                              <Calendar className="w-3 h-3 mr-1" />
                              Expected: {formatDate(deal.closeDate)}
                            </span>
                            <span className="text-blue-600 font-medium">{deal.probability}% probability</span>
                          </div>
                        </div>
                      </div>
                      <div className="text-right">
                        <div className="font-semibold text-lg">{formatCurrency(deal.value)}</div>
                        <div className="text-sm text-blue-600 font-medium">
                          {formatCurrency(deal.commissionDetails.totalCommission)} expected commission
                        </div>
                        <Button
                          variant="outline"
                          size="sm"
                          className="mt-2 bg-transparent"
                          onClick={() => handleViewDeal(deal)}
                        >
                          <Eye className="w-3 h-3 mr-1" />
                          View Details
                        </Button>
                      </div>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="history" className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle>Commission History</CardTitle>
                <CardDescription>Your commission payments and status</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {commissionHistory.map((commission) => (
                    <div
                      key={commission.id}
                      className="flex items-center justify-between p-4 border rounded-lg hover:bg-gray-50"
                    >
                      <div className="flex items-center space-x-4">
                        <div className="flex-shrink-0">
                          <DollarSign className="w-8 h-8 text-gray-400" />
                        </div>
                        <div>
                          <h4 className="font-medium text-gray-900">{commission.dealName}</h4>
                          <div className="flex items-center space-x-4 text-sm text-gray-500">
                            <span>{commission.quarter}</span>
                            <Badge className={`${getCommissionStatusColor(commission.status)} flex items-center gap-1`}>
                              {getCommissionStatusIcon(commission.status)}
                              {commission.status.charAt(0).toUpperCase() + commission.status.slice(1)}
                            </Badge>
                            <span className="flex items-center">
                              <Calendar className="w-3 h-3 mr-1" />
                              Due: {formatDate(commission.dueDate)}
                            </span>
                            {commission.paidDate && (
                              <span className="flex items-center text-green-600">
                                <CheckCircle className="w-3 h-3 mr-1" />
                                Paid: {formatDate(commission.paidDate)}
                              </span>
                            )}
                          </div>
                        </div>
                      </div>
                      <div className="text-right">
                        <div className="font-semibold text-lg">{formatCurrency(commission.amount)}</div>
                        <Button
                          variant="outline"
                          size="sm"
                          className="mt-2 bg-transparent"
                          onClick={() => handleViewDealFromHistory(commission)}
                        >
                          <Eye className="w-3 h-3 mr-1" />
                          View Details
                        </Button>
                      </div>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="disputes" className="space-y-6">
            {disputeView === "form" && (
              <DisputeForm onSubmit={handleDisputeCreated} onCancel={handleCancelDisputeForm} />
            )}

            {disputeView === "detail" && selectedDispute && (
              <DisputeDetail
                dispute={selectedDispute}
                onBack={handleBackToDisputeList}
                userRole="sales"
                userId="rep-001"
                userName="Sarah Johnson"
              />
            )}

            {disputeView === "list" && (
              <>
                <div className="flex items-center justify-between">
                  <div>
                    <h2 className="text-2xl font-bold">Commission Disputes</h2>
                    <p className="text-muted-foreground">Manage and track commission disputes</p>
                  </div>
                  <Button onClick={handleCreateDispute}>
                    <Plus className="w-4 h-4 mr-2" />
                    File New Dispute
                  </Button>
                </div>

                <DisputeList onViewDispute={handleViewDispute} userRole="sales" userId="rep-001" />
              </>
            )}
          </TabsContent>
        </Tabs>
      </div>

      {/* Deal Details Modal */}
      <DealModal
        isOpen={isModalOpen}
        onClose={handleCloseModal}
        dealData={selectedDeal}
        userRole="sales"
        onViewInHubSpot={(dealId) => window.open(`https://app.hubspot.com/deals/${dealId}`, "_blank")}
      />
    </div>
  )
}
