"use client"

import { useState, useEffect } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Input } from "@/components/ui/input"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { DealModal } from "./deal-modal"
import { Search, Filter, Eye, Building2, Calendar, DollarSign } from "lucide-react"
import { dealsApi, calculationsApi, type DealResponse, type CommissionCalculationResponse } from "@/lib/api"

function mapDealForList(deal: DealResponse, calc?: CommissionCalculationResponse) {
  return {
    id: deal.id,
    title: deal.title,
    description: deal.title,
    company: deal.salesRepId,
    contactName: deal.salesRepId,
    contactEmail: "",
    contactPhone: "",
    stage: deal.status === "WON" ? "Closed Won" : deal.status === "OPEN" ? "Open" : deal.status,
    probability: deal.status === "WON" ? 100 : 50,
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

export function DealListWithModal() {
  const [deals, setDeals] = useState<any[]>([])
  const [selectedDeal, setSelectedDeal] = useState<any | null>(null)
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [searchTerm, setSearchTerm] = useState("")
  const [stageFilter, setStageFilter] = useState("all")

  useEffect(() => {
    async function load() {
      try {
        const [allDeals, allCalcs] = await Promise.all([dealsApi.getAll(), calculationsApi.getAll()])
        const calcByDeal = new Map(allCalcs.map((c) => [c.dealId, c]))
        setDeals(allDeals.map((d) => mapDealForList(d, calcByDeal.get(d.id))))
      } catch (err) {
        console.error("Failed to load deals:", err)
      }
    }
    load()
  }, [])

  const filteredDeals = deals.filter((deal) => {
    const matchesSearch =
      deal.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
      deal.company.toLowerCase().includes(searchTerm.toLowerCase())
    const matchesStage = stageFilter === "all" || deal.stage === stageFilter
    return matchesSearch && matchesStage
  })

  const handleViewDeal = (deal: any) => {
    setSelectedDeal(deal)
    setIsModalOpen(true)
  }

  const handleCloseModal = () => {
    setIsModalOpen(false)
    setSelectedDeal(null)
  }

  const handleApplyDeal = async (dealId: string) => {
    try {
      await dealsApi.update(dealId, { status: "WON" })
      // Reload deals
      const [allDeals, allCalcs] = await Promise.all([dealsApi.getAll(), calculationsApi.getAll()])
      const calcByDeal = new Map(allCalcs.map((c) => [c.dealId, c]))
      setDeals(allDeals.map((d) => mapDealForList(d, calcByDeal.get(d.id))))
    } catch (err) {
      console.error("Failed to apply deal:", err)
    }
  }

  const handleEditDeal = (dealId: string) => {
    console.log("Edit deal:", dealId)
  }

  const handleViewInHubSpot = (dealId: string) => {
    window.open(`https://app.hubspot.com/deals/${dealId}`, "_blank")
  }

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat("en-US", {
      style: "currency",
      currency: "USD",
    }).format(amount)
  }

  const getStageColor = (stage: string) => {
    const stageColors: Record<string, string> = {
      Qualified: "bg-blue-100 text-blue-800",
      Discovery: "bg-purple-100 text-purple-800",
      Proposal: "bg-orange-100 text-orange-800",
      Negotiation: "bg-yellow-100 text-yellow-800",
      "Closed Won": "bg-green-100 text-green-800",
      "Closed Lost": "bg-red-100 text-red-800",
    }
    return stageColors[stage] || "bg-gray-100 text-gray-800"
  }

  return (
    <div className="space-y-6">
      {/* Filters */}
      <Card>
        <CardHeader>
          <CardTitle>Deal Pipeline</CardTitle>
          <CardDescription>View and manage your sales deals</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="flex flex-col sm:flex-row gap-4">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-muted-foreground w-4 h-4" />
              <Input
                placeholder="Search deals..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-10"
              />
            </div>
            <Select value={stageFilter} onValueChange={setStageFilter}>
              <SelectTrigger className="w-full sm:w-48">
                <Filter className="w-4 h-4 mr-2" />
                <SelectValue placeholder="Filter by stage" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Stages</SelectItem>
                <SelectItem value="Qualified">Qualified</SelectItem>
                <SelectItem value="Discovery">Discovery</SelectItem>
                <SelectItem value="Proposal">Proposal</SelectItem>
                <SelectItem value="Negotiation">Negotiation</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </CardContent>
      </Card>

      {/* Deal List */}
      <div className="space-y-4">
        {filteredDeals.map((deal) => (
          <Card key={deal.id} className="hover:shadow-md transition-shadow">
            <CardContent className="p-6">
              <div className="flex items-start justify-between">
                <div className="flex-1">
                  <div className="flex items-center gap-3 mb-2">
                    <h3 className="text-lg font-semibold">{deal.title}</h3>
                    <Badge className={getStageColor(deal.stage)}>{deal.stage}</Badge>
                  </div>

                  <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
                    <div className="flex items-center gap-2">
                      <Building2 className="w-4 h-4 text-muted-foreground" />
                      <span className="text-sm">{deal.company}</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <DollarSign className="w-4 h-4 text-muted-foreground" />
                      <span className="text-sm font-medium">{formatCurrency(deal.value)}</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <Calendar className="w-4 h-4 text-muted-foreground" />
                      <span className="text-sm">Close: {new Date(deal.closeDate).toLocaleDateString()}</span>
                    </div>
                  </div>

                  <p className="text-sm text-muted-foreground line-clamp-2">{deal.description}</p>
                </div>

                <div className="flex items-center gap-2 ml-4">
                  <div className="text-right mr-4">
                    <div className="text-sm text-muted-foreground">Projected Commission</div>
                    <div className="text-lg font-bold text-green-600">
                      {formatCurrency(deal.commissionDetails.totalCommission)}
                    </div>
                  </div>
                  <Button variant="outline" size="sm" onClick={() => handleViewDeal(deal)}>
                    <Eye className="w-4 h-4 mr-2" />
                    View Details
                  </Button>
                </div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      {/* Deal Modal */}
      <DealModal
        isOpen={isModalOpen}
        onClose={handleCloseModal}
        dealData={selectedDeal}
        userRole="sales-rep"
        onApplyDeal={handleApplyDeal}
        onEditDeal={handleEditDeal}
        onViewInHubSpot={handleViewInHubSpot}
      />
    </div>
  )
}
