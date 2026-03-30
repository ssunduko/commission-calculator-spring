"use client"

import { useState, useEffect } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Switch } from "@/components/ui/switch"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import { Alert, AlertDescription } from "@/components/ui/alert"
import {
  Settings,
  Plus,
  Edit,
  Play,
  Pause,
  Copy,
  Eye,
  Users,
  DollarSign,
  CheckCircle,
  RefreshCw,
  XCircle,
  AlertTriangle,
  Calendar,
  Building2,
  User,
  Check,
  X,
  Edit3,
} from "lucide-react"
import { DealModal } from "./deal-modal"
import {
  dealsApi,
  plansApi,
  calculationsApi,
  type DealResponse,
  type CommissionPlanResponse,
  type CommissionCalculationResponse,
} from "@/lib/api"

// Integration status derived from API connectivity
function useApiHealth() {
  const [status, setStatus] = useState({ connected: false, lastSync: "never", status: "unknown" })
  useEffect(() => {
    const start = Date.now()
    dealsApi.getAll().then(() => {
      const elapsed = Date.now() - start
      setStatus({ connected: true, lastSync: "just now", status: elapsed < 2000 ? "healthy" : "slow" })
    }).catch(() => {
      setStatus({ connected: false, lastSync: "failed", status: "error" })
    })
  }, [])
  return status
}

function mapApiDealToAdmin(deal: DealResponse, calc?: CommissionCalculationResponse) {
  const payoutStatuses = ["pending_approval", "approved", "paid"] as const
  return {
    id: deal.id,
    title: deal.title,
    description: deal.title,
    company: deal.salesRepId,
    contactName: deal.salesRepId,
    contactEmail: "",
    contactPhone: "",
    stage: "Closed Won",
    probability: 100,
    value: deal.value,
    closeDate: deal.closeDate || "",
    createdDate: deal.createdDate || "",
    lastModified: deal.closeDate || deal.createdDate || "",
    source: "API",
    dealType: "New Business",
    salesRep: deal.salesRepId,
    salesRepId: deal.salesRepId,
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
      commissionRate: deal.value > 0 && calc ? ((calc.baseCommission / deal.value) * 100) : 10,
      planName: calc?.planId ?? "N/A",
    },
    documents: [],
    notes: "",
    termsAndConditions: "",
    status: "won" as const,
    payoutStatus: calc?.status === "PAID" ? "paid"
      : calc?.status === "APPROVED" ? "approved"
      : calc?.status === "DISPUTED" ? "rejected"
      : "pending_approval",
    rejectionReason: undefined as string | undefined,
    lastActivity: deal.closeDate || deal.createdDate || "",
  }
}

export function AdminDashboard() {
  const [selectedPlan, setSelectedPlan] = useState(null)
  const [isCreatingPlan, setIsCreatingPlan] = useState(false)
  const [selectedDeal, setSelectedDeal] = useState(null)
  const [adjustmentAmount, setAdjustmentAmount] = useState("")
  const [adjustmentReason, setAdjustmentReason] = useState("")
  const [rejectionReason, setRejectionReason] = useState("")
  const [isProcessing, setIsProcessing] = useState(false)
  const [showAdjustmentDialog, setShowAdjustmentDialog] = useState(false)
  const [showRejectionDialog, setShowRejectionDialog] = useState(false)
  const [selectedDealForModal, setSelectedDealForModal] = useState(null)
  const [isDetailModalOpen, setIsDetailModalOpen] = useState(false)
  const [deals, setDeals] = useState<any[]>([])
  const [commissionPlans, setCommissionPlans] = useState<any[]>([])
  const [apiLoading, setApiLoading] = useState(true)
  const apiHealth = useApiHealth()

  useEffect(() => {
    async function loadData() {
      try {
        setApiLoading(true)
        const [apiDeals, apiPlans, apiCalcs] = await Promise.all([
          dealsApi.getAll(),
          plansApi.getAll(),
          calculationsApi.getAll(),
        ])
        const calcByDeal = new Map(apiCalcs.map((c) => [c.dealId, c]))
        setDeals(apiDeals.map((d) => mapApiDealToAdmin(d, calcByDeal.get(d.id))))
        setCommissionPlans(
          apiPlans.map((p) => ({
            id: p.id,
            name: p.name,
            status: p.status === "ACTIVE" ? "active" : p.status === "DRAFT" ? "draft" : p.status.toLowerCase(),
            users: 0,
            baseRate: 10,
            accelerator: 15,
            threshold: 100000,
            lastModified: p.createdDate || "",
            version: "1.0",
            rulesCount: p.rulesCount,
            tiersCount: p.tiersCount,
          })),
        )
      } catch (err: any) {
        console.error("Failed to load admin data:", err)
      } finally {
        setApiLoading(false)
      }
    }
    loadData()
  }, [])

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat("en-US", {
      style: "currency",
      currency: "USD",
    }).format(amount)
  }

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
    })
  }

  const getStatusColor = (status) => {
    switch (status) {
      case "pending_approval":
        return "bg-yellow-100 text-yellow-800 border-yellow-200"
      case "approved":
        return "bg-green-100 text-green-800 border-green-200"
      case "rejected":
        return "bg-red-100 text-red-800 border-red-200"
      case "paid":
        return "bg-blue-100 text-blue-800 border-blue-200"
      default:
        return "bg-gray-100 text-gray-800 border-gray-200"
    }
  }

  const getStatusIcon = (status) => {
    switch (status) {
      case "pending_approval":
        return <AlertTriangle className="w-3 h-3" />
      case "approved":
        return <CheckCircle className="w-3 h-3" />
      case "rejected":
        return <XCircle className="w-3 h-3" />
      case "paid":
        return <DollarSign className="w-3 h-3" />
      default:
        return <AlertTriangle className="w-3 h-3" />
    }
  }

  const handleApproveDeal = async (dealId: string) => {
    setIsProcessing(true)
    try {
      await dealsApi.update(dealId, { status: "WON" })
      setDeals((prevDeals) =>
        prevDeals.map((deal) =>
          deal.id === dealId
            ? { ...deal, payoutStatus: "approved", lastActivity: new Date().toISOString().split("T")[0] }
            : deal,
        ),
      )
    } catch (err: any) {
      console.error("Failed to approve deal:", err)
    } finally {
      setIsProcessing(false)
    }
  }

  const handleRejectDeal = async (dealId: string) => {
    if (!rejectionReason.trim()) {
      alert("Please provide a rejection reason")
      return
    }

    setIsProcessing(true)
    try {
      await dealsApi.update(dealId, { status: "CANCELLED" })
      setDeals((prevDeals) =>
        prevDeals.map((deal) =>
          deal.id === dealId
            ? {
                ...deal,
                payoutStatus: "rejected",
                rejectionReason: rejectionReason,
                lastActivity: new Date().toISOString().split("T")[0],
              }
            : deal,
        ),
      )
    } catch (err: any) {
      console.error("Failed to reject deal:", err)
    } finally {
      setRejectionReason("")
      setShowRejectionDialog(false)
      setSelectedDeal(null)
      setIsProcessing(false)
    }
  }

  const handleAdjustCommission = async (dealId: string) => {
    if (!adjustmentAmount || !adjustmentReason.trim()) {
      alert("Please provide adjustment amount and reason")
      return
    }

    setIsProcessing(true)
    try {
      await dealsApi.update(dealId, { value: Number.parseFloat(adjustmentAmount) })
      setDeals((prevDeals) =>
        prevDeals.map((deal) =>
          deal.id === dealId
            ? {
                ...deal,
                commissionDetails: {
                  ...deal.commissionDetails,
                  totalCommission: Number.parseFloat(adjustmentAmount),
                  adjustment: Number.parseFloat(adjustmentAmount) - deal.commissionDetails.totalCommission,
                  adjustmentReason: adjustmentReason,
                },
                lastActivity: new Date().toISOString().split("T")[0],
              }
            : deal,
        ),
      )
    } catch (err: any) {
      console.error("Failed to adjust commission:", err)
    } finally {
      setAdjustmentAmount("")
      setAdjustmentReason("")
      setShowAdjustmentDialog(false)
      setSelectedDeal(null)
      setIsProcessing(false)
    }
  }

  const getTotalCommission = (deal) => {
    const bonusTotal = (deal.commissionDetails.acceleratorBonus || 0) + (deal.commissionDetails.spifBonus || 0)
    const adjustmentTotal = deal.commissionDetails.adjustment || 0
    return deal.commissionDetails.baseCommission + bonusTotal + adjustmentTotal
  }

  const handleViewDealDetails = (deal) => {
    // Convert admin deal format to modal format
    const modalDeal = {
      ...deal,
      commissionDetails: {
        ...deal.commissionDetails,
        totalCommission: getTotalCommission(deal),
      },
    }

    setSelectedDealForModal(modalDeal)
    setIsDetailModalOpen(true)
  }

  return (
    <div className="min-h-screen bg-gray-50 p-6">
      <div className="max-w-7xl mx-auto space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Admin Dashboard</h1>
            <p className="text-gray-600 mt-1">Manage commission plans, integrations, and system settings.</p>
          </div>
          <div className="flex items-center gap-3">
            <Button variant="outline" size="sm">
              <RefreshCw className="w-4 h-4 mr-2" />
              Sync All
            </Button>
            <Button>
              <Plus className="w-4 h-4 mr-2" />
              New Plan
            </Button>
          </div>
        </div>

        {/* Quick Stats */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Active Plans</CardTitle>
              <Settings className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{commissionPlans.filter((p) => p.status === "active").length}</div>
              <div className="text-xs text-muted-foreground mt-1">{commissionPlans.filter((p) => p.status === "draft").length} draft plan(s)</div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Total Users</CardTitle>
              <Users className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{deals.length}</div>
              <div className="text-xs text-muted-foreground mt-1">Total deals loaded</div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Monthly Payouts</CardTitle>
              <DollarSign className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">$127K</div>
              <div className="text-xs text-green-600 mt-1">+8.2% from last month</div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">System Health</CardTitle>
              <CheckCircle className="h-4 w-4 text-green-500" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold text-green-600">Healthy</div>
              <div className="text-xs text-muted-foreground mt-1">All systems operational</div>
            </CardContent>
          </Card>
        </div>

        {/* Main Content */}
        <Tabs defaultValue="plans" className="space-y-6">
          <TabsList className="grid w-full grid-cols-5">
            <TabsTrigger value="plans">Commission Plans</TabsTrigger>
            <TabsTrigger value="payouts">Commission Payouts</TabsTrigger>
            <TabsTrigger value="integrations">Integrations</TabsTrigger>
            <TabsTrigger value="users">User Management</TabsTrigger>
            <TabsTrigger value="audit">Audit Logs</TabsTrigger>
          </TabsList>

          <TabsContent value="plans" className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle>Commission Plans</CardTitle>
                <CardDescription>Create and manage commission calculation plans</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {commissionPlans.map((plan) => (
                    <div key={plan.id} className="flex items-center justify-between p-4 border rounded-lg">
                      <div className="flex-1">
                        <div className="flex items-center gap-3">
                          <h4 className="font-medium">{plan.name}</h4>
                          <Badge variant={plan.status === "active" ? "default" : "secondary"}>{plan.status}</Badge>
                          <span className="text-sm text-muted-foreground">v{plan.version}</span>
                        </div>
                        <div className="text-sm text-muted-foreground mt-1">
                          {plan.rulesCount ?? plan.users} rules • {plan.tiersCount ?? 0} tiers • Base: {plan.baseRate}%
                        </div>
                        <div className="text-xs text-muted-foreground">Last modified: {plan.lastModified}</div>
                      </div>
                      <div className="flex items-center gap-2">
                        <Button variant="ghost" size="sm">
                          <Eye className="w-4 h-4" />
                        </Button>
                        <Button variant="ghost" size="sm">
                          <Edit className="w-4 h-4" />
                        </Button>
                        <Button variant="ghost" size="sm">
                          <Copy className="w-4 h-4" />
                        </Button>
                        {plan.status === "active" ? (
                          <Button variant="ghost" size="sm">
                            <Pause className="w-4 h-4" />
                          </Button>
                        ) : (
                          <Button variant="ghost" size="sm">
                            <Play className="w-4 h-4" />
                          </Button>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="payouts" className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle>Commission Payouts</CardTitle>
                <CardDescription>Review and approve commission payouts for closed-won deals</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {deals.map((deal) => (
                    <div key={deal.id} className="border rounded-lg p-4 space-y-4">
                      {/* Deal Header */}
                      <div className="flex items-start justify-between">
                        <div className="flex-1">
                          <div className="flex items-center gap-3 mb-2">
                            <h4 className="font-semibold text-lg">{deal.title}</h4>
                            <Badge className={getStatusColor(deal.payoutStatus)}>
                              {getStatusIcon(deal.payoutStatus)}
                              <span className="ml-1 capitalize">{deal.payoutStatus.replace("_", " ")}</span>
                            </Badge>
                          </div>
                          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm">
                            <div className="flex items-center gap-2">
                              <Building2 className="w-4 h-4 text-muted-foreground" />
                              <span>
                                <strong>Company:</strong> {deal.company}
                              </span>
                            </div>
                            <div className="flex items-center gap-2">
                              <User className="w-4 h-4 text-muted-foreground" />
                              <span>
                                <strong>Sales Rep:</strong> {deal.salesRep}
                              </span>
                            </div>
                            <div className="flex items-center gap-2">
                              <Calendar className="w-4 h-4 text-muted-foreground" />
                              <span>
                                <strong>Closed:</strong> {formatDate(deal.closeDate)}
                              </span>
                            </div>
                          </div>
                        </div>
                        <div className="text-right">
                          <div className="text-2xl font-bold">{formatCurrency(deal.value)}</div>
                          <div className="text-sm text-muted-foreground">Deal Value</div>
                        </div>
                      </div>

                      {/* Commission Breakdown */}
                      <div className="bg-gray-50 rounded-lg p-4">
                        <h5 className="font-medium mb-3">Commission Breakdown</h5>
                        <div className="grid grid-cols-1 md:grid-cols-4 gap-4 text-sm">
                          <div>
                            <div className="text-muted-foreground">Base Commission</div>
                            <div className="font-semibold">
                              {formatCurrency(deal.commissionDetails.baseCommission)} (
                              {deal.commissionDetails.commissionRate}%)
                            </div>
                          </div>
                          {deal.commissionDetails.acceleratorBonus && (
                            <div>
                              <div className="text-muted-foreground">Accelerator Bonus</div>
                              <div className="font-semibold text-green-600">
                                +{formatCurrency(deal.commissionDetails.acceleratorBonus)}
                              </div>
                            </div>
                          )}
                          {deal.commissionDetails.spifBonus && (
                            <div>
                              <div className="text-muted-foreground">SPIF Bonus</div>
                              <div className="font-semibold text-purple-600">
                                +{formatCurrency(deal.commissionDetails.spifBonus)}
                              </div>
                            </div>
                          )}
                          {deal.commissionDetails.adjustment && (
                            <div>
                              <div className="text-muted-foreground">Adjustment</div>
                              <div
                                className={`font-semibold ${deal.commissionDetails.adjustment > 0 ? "text-green-600" : "text-red-600"}`}
                              >
                                {deal.commissionDetails.adjustment > 0 ? "+" : ""}
                                {formatCurrency(deal.commissionDetails.adjustment)}
                              </div>
                            </div>
                          )}
                          <div>
                            <div className="text-muted-foreground">Total Commission</div>
                            <div className="font-bold text-lg text-green-600">
                              {formatCurrency(deal.commissionDetails.totalCommission)}
                            </div>
                          </div>
                        </div>
                      </div>

                      {/* Rejection Reason */}
                      {deal.payoutStatus === "rejected" && deal.rejectionReason && (
                        <Alert>
                          <XCircle className="h-4 w-4" />
                          <AlertDescription>
                            <strong>Rejection Reason:</strong> {deal.rejectionReason}
                          </AlertDescription>
                        </Alert>
                      )}

                      {/* Action Buttons */}
                      <div className="flex items-center justify-between pt-2 border-t">
                        <div className="text-xs text-muted-foreground">
                          Last Activity: {formatDate(deal.lastActivity)} • Plan: {deal.commissionDetails.planName}
                        </div>
                        <div className="flex items-center gap-2">
                          <Button variant="ghost" size="sm" onClick={() => handleViewDealDetails(deal)}>
                            <Eye className="w-4 h-4 mr-2" />
                            View Details
                          </Button>
                          {deal.payoutStatus === "pending_approval" && (
                            <>
                              <Dialog
                                open={showAdjustmentDialog && selectedDeal?.id === deal.id}
                                onOpenChange={setShowAdjustmentDialog}
                              >
                                <DialogTrigger asChild>
                                  <Button variant="outline" size="sm" onClick={() => setSelectedDeal(deal)}>
                                    <Edit3 className="w-4 h-4 mr-2" />
                                    Adjust
                                  </Button>
                                </DialogTrigger>
                                <DialogContent>
                                  <DialogHeader>
                                    <DialogTitle>Adjust Commission</DialogTitle>
                                    <DialogDescription>
                                      Make adjustments to the commission for {deal.title}
                                    </DialogDescription>
                                  </DialogHeader>
                                  <div className="space-y-4">
                                    <div>
                                      <Label htmlFor="adjustment-amount">Adjustment Amount</Label>
                                      <Input
                                        id="adjustment-amount"
                                        type="number"
                                        placeholder="Enter adjustment amount (positive or negative)"
                                        value={adjustmentAmount}
                                        onChange={(e) => setAdjustmentAmount(e.target.value)}
                                      />
                                    </div>
                                    <div>
                                      <Label htmlFor="adjustment-reason">Reason for Adjustment</Label>
                                      <Textarea
                                        id="adjustment-reason"
                                        placeholder="Explain the reason for this adjustment"
                                        value={adjustmentReason}
                                        onChange={(e) => setAdjustmentReason(e.target.value)}
                                      />
                                    </div>
                                    <div className="flex justify-end gap-2">
                                      <Button variant="outline" onClick={() => setShowAdjustmentDialog(false)}>
                                        Cancel
                                      </Button>
                                      <Button onClick={() => handleAdjustCommission(deal.id)} disabled={isProcessing}>
                                        {isProcessing ? "Processing..." : "Apply Adjustment"}
                                      </Button>
                                    </div>
                                  </div>
                                </DialogContent>
                              </Dialog>

                              <Dialog
                                open={showRejectionDialog && selectedDeal?.id === deal.id}
                                onOpenChange={setShowRejectionDialog}
                              >
                                <DialogTrigger asChild>
                                  <Button variant="outline" size="sm" onClick={() => setSelectedDeal(deal)}>
                                    <X className="w-4 h-4 mr-2" />
                                    Reject
                                  </Button>
                                </DialogTrigger>
                                <DialogContent>
                                  <DialogHeader>
                                    <DialogTitle>Reject Commission Payout</DialogTitle>
                                    <DialogDescription>
                                      Provide a reason for rejecting the commission payout for {deal.title}
                                    </DialogDescription>
                                  </DialogHeader>
                                  <div className="space-y-4">
                                    <div>
                                      <Label htmlFor="rejection-reason">Rejection Reason</Label>
                                      <Textarea
                                        id="rejection-reason"
                                        placeholder="Explain why this commission payout is being rejected"
                                        value={rejectionReason}
                                        onChange={(e) => setRejectionReason(e.target.value)}
                                      />
                                    </div>
                                    <div className="flex justify-end gap-2">
                                      <Button variant="outline" onClick={() => setShowRejectionDialog(false)}>
                                        Cancel
                                      </Button>
                                      <Button
                                        variant="destructive"
                                        onClick={() => handleRejectDeal(deal.id)}
                                        disabled={isProcessing}
                                      >
                                        {isProcessing ? "Processing..." : "Reject Payout"}
                                      </Button>
                                    </div>
                                  </div>
                                </DialogContent>
                              </Dialog>

                              <Button size="sm" onClick={() => handleApproveDeal(deal.id)} disabled={isProcessing}>
                                <Check className="w-4 h-4 mr-2" />
                                {isProcessing ? "Processing..." : "Approve"}
                              </Button>
                            </>
                          )}
                          {deal.payoutStatus === "approved" && (
                            <Badge variant="outline" className="text-green-600">
                              <CheckCircle className="w-3 h-3 mr-1" />
                              Approved - Ready for Payment
                            </Badge>
                          )}
                          {deal.payoutStatus === "paid" && (
                            <Badge variant="outline" className="text-blue-600">
                              <DollarSign className="w-3 h-3 mr-1" />
                              Payment Completed
                            </Badge>
                          )}
                          {deal.payoutStatus === "rejected" && (
                            <Badge variant="outline" className="text-red-600">
                              <XCircle className="w-3 h-3 mr-1" />
                              Payout Rejected
                            </Badge>
                          )}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="integrations" className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <div className="w-8 h-8 bg-blue-600 rounded flex items-center justify-center text-white font-bold text-sm">
                      API
                    </div>
                    Backend API (Spring Boot)
                  </CardTitle>
                  <CardDescription>Commission Calculator vertical slice API on port 8081</CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="flex items-center justify-between">
                    <span className="text-sm">Connection Status</span>
                    <Badge variant="default" className={apiHealth.connected ? "bg-green-100 text-green-800" : "bg-red-100 text-red-800"}>
                      {apiHealth.connected ? <CheckCircle className="w-3 h-3 mr-1" /> : <XCircle className="w-3 h-3 mr-1" />}
                      {apiHealth.connected ? "Connected" : "Disconnected"}
                    </Badge>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm">Last Check</span>
                    <span className="text-sm text-muted-foreground">{apiHealth.lastSync}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm">Health</span>
                    <span className="text-sm text-muted-foreground capitalize">{apiHealth.status}</span>
                  </div>
                  <div className="flex gap-2 pt-2">
                    <Button variant="outline" size="sm" className="flex-1 bg-transparent" onClick={() => window.location.reload()}>
                      <RefreshCw className="w-4 h-4 mr-2" />
                      Refresh
                    </Button>
                    <Button variant="outline" size="sm" className="flex-1 bg-transparent" onClick={() => window.open("/api/deals", "_blank")}>
                      <Settings className="w-4 h-4 mr-2" />
                      Test API
                    </Button>
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <div className="w-8 h-8 bg-purple-500 rounded flex items-center justify-center text-white font-bold text-sm">
                      DB
                    </div>
                    H2 Database
                  </CardTitle>
                  <CardDescription>In-memory database with Flyway migrations</CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="flex items-center justify-between">
                    <span className="text-sm">Status</span>
                    <Badge variant="default" className={apiHealth.connected ? "bg-green-100 text-green-800" : "bg-gray-100 text-gray-800"}>
                      {apiHealth.connected ? <CheckCircle className="w-3 h-3 mr-1" /> : <AlertTriangle className="w-3 h-3 mr-1" />}
                      {apiHealth.connected ? "Active" : "Unknown"}
                    </Badge>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm">Deals Loaded</span>
                    <span className="text-sm text-muted-foreground">{deals.length}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm">Plans Loaded</span>
                    <span className="text-sm text-muted-foreground">{commissionPlans.length}</span>
                  </div>
                  <div className="flex gap-2 pt-2">
                    <Button variant="outline" size="sm" className="flex-1 bg-transparent" onClick={() => window.open("http://localhost:8081/h2-console", "_blank")}>
                      <Settings className="w-4 h-4 mr-2" />
                      H2 Console
                    </Button>
                  </div>
                </CardContent>
              </Card>
            </div>
          </TabsContent>

          <TabsContent value="users" className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle>User Management</CardTitle>
                <CardDescription>Manage user access and plan assignments</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="text-center py-8">
                  <Users className="w-12 h-12 text-muted-foreground mx-auto mb-4" />
                  <h3 className="text-lg font-medium mb-2">User Management</h3>
                  <p className="text-muted-foreground mb-4">
                    Manage user roles, permissions, and commission plan assignments.
                  </p>
                  <Button>
                    <Plus className="w-4 h-4 mr-2" />
                    Add User
                  </Button>
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="audit" className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle>Audit Logs</CardTitle>
                <CardDescription>Track all system changes and calculations</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  <div className="flex items-center justify-between p-3 border rounded-lg">
                    <div className="flex items-center gap-3">
                      <div className="w-2 h-2 bg-blue-500 rounded-full"></div>
                      <div>
                        <div className="text-sm font-medium">Commission plan updated</div>
                        <div className="text-xs text-muted-foreground">
                          Standard Sales Plan v2.1 - Base rate changed from 8% to 10%
                        </div>
                      </div>
                    </div>
                    <div className="text-xs text-muted-foreground">2 hours ago</div>
                  </div>

                  <div className="flex items-center justify-between p-3 border rounded-lg">
                    <div className="flex items-center gap-3">
                      <div className="w-2 h-2 bg-green-500 rounded-full"></div>
                      <div>
                        <div className="text-sm font-medium">HubSpot sync completed</div>
                        <div className="text-xs text-muted-foreground">
                          142 deals synchronized, 3 new commissions calculated
                        </div>
                      </div>
                    </div>
                    <div className="text-xs text-muted-foreground">5 minutes ago</div>
                  </div>

                  <div className="flex items-center justify-between p-3 border rounded-lg">
                    <div className="flex items-center gap-3">
                      <div className="w-2 h-2 bg-purple-500 rounded-full"></div>
                      <div>
                        <div className="text-sm font-medium">Commission adjustment approved</div>
                        <div className="text-xs text-muted-foreground">
                          Sarah Johnson - Deal #1234 - Adjustment: +$500
                        </div>
                      </div>
                    </div>
                    <div className="text-xs text-muted-foreground">1 day ago</div>
                  </div>
                </div>
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </div>

      {/* Deal Details Modal */}
      <DealModal
        isOpen={isDetailModalOpen}
        onClose={() => setIsDetailModalOpen(false)}
        dealData={selectedDealForModal}
        userRole="admin"
        onViewInHubSpot={(dealId) => window.open(`https://app.hubspot.com/deals/${dealId}`, "_blank")}
      />
    </div>
  )
}
