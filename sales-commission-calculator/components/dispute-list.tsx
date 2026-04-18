"use client"

import { useState, useMemo, useEffect } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Input } from "@/components/ui/input"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import {
  Search,
  Eye,
  Clock,
  AlertCircle,
  CheckCircle,
  XCircle,
  TrendingUp,
  Calendar,
  DollarSign,
  User,
  FileText,
  MessageSquare,
} from "lucide-react"
import type { Dispute, DisputeStatus, DisputeType, DisputePriority } from "@/lib/dispute-workflow/types"
import { disputesApi, mapApiDisputeToLocal } from "@/lib/api"
import { WebMcpTool } from "@/components/webmcp-tool"
import { useWebMcp } from "@/hooks/use-webmcp"

interface DisputeListProps {
  onViewDispute: (dispute: Dispute) => void
  userRole: "sales" | "admin" | "finance" | "manager"
  userId: string
}

export function DisputeList({ onViewDispute, userRole, userId }: DisputeListProps) {
  const [searchTerm, setSearchTerm] = useState("")
  const [statusFilter, setStatusFilter] = useState<DisputeStatus | "all">("all")
  const [typeFilter, setTypeFilter] = useState<DisputeType | "all">("all")
  const [priorityFilter, setPriorityFilter] = useState<DisputePriority | "all">("all")
  const [activeTab, setActiveTab] = useState("all")
  const [allDisputes, setAllDisputes] = useState<Dispute[]>([])

  useEffect(() => {
    disputesApi.getAll().then((data) => {
      setAllDisputes(data.map(mapApiDisputeToLocal))
    }).catch(() => setAllDisputes([]))
  }, [])

  // Also expose createDispute from the list page so agents don't need to
  // navigate to the form view before invoking it.
  useWebMcp({
    tool: "createDispute",
    description:
      "Create a new commission dispute. calculationId + salesRepId must refer to existing records — use listCalculations (or inspect an existing dispute) if unknown.",
    endpoint: "/disputes",
    method: "POST",
    params: [
      { name: "calculationId", description: "Commission calculation UUID to dispute" },
      { name: "salesRepId", description: "Sales rep user ID (typically the calc's salesRepId)" },
      { name: "title", description: "Brief title describing the dispute" },
      { name: "description", description: "Detailed explanation of the dispute issue" },
      {
        name: "priority",
        description: "Priority: LOW, MEDIUM, HIGH, or URGENT. Defaults to MEDIUM if omitted.",
        required: false,
      },
    ],
  })

  // Discovery helper: lets an agent look up valid calculationId / salesRepId
  // pairs so it can construct a createDispute call without any UI help.
  useWebMcp({
    tool: "listCalculations",
    description:
      "List commission calculations (id, dealId, salesRepId, amounts). Use this to find valid calculationId and salesRepId values before calling createDispute.",
    endpoint: "/calculations",
    method: "GET",
    params: [
      { name: "dealId", description: "Filter by deal ID", required: false },
      { name: "salesRepId", description: "Filter by sales rep user ID", required: false },
    ],
  })

  const myDisputes = allDisputes.filter((d) => d.submittedBy === userId)

  // Filter disputes based on current tab
  const baseDisputes = activeTab === "my" ? myDisputes : allDisputes

  // Apply filters
  const filteredDisputes = useMemo(() => {
    return baseDisputes.filter((dispute) => {
      // Search filter
      if (searchTerm) {
        const searchLower = searchTerm.toLowerCase()
        const matchesSearch =
          dispute.title.toLowerCase().includes(searchLower) ||
          dispute.description.toLowerCase().includes(searchLower) ||
          dispute.id.toLowerCase().includes(searchLower) ||
          dispute.submittedByName.toLowerCase().includes(searchLower)

        if (!matchesSearch) return false
      }

      // Status filter
      if (statusFilter !== "all" && dispute.status !== statusFilter) {
        return false
      }

      // Type filter
      if (typeFilter !== "all" && dispute.type !== typeFilter) {
        return false
      }

      // Priority filter (case-insensitive: backend enums are uppercase, local type is lowercase)
      if (
        priorityFilter !== "all" &&
        String(dispute.priority).toLowerCase() !== String(priorityFilter).toLowerCase()
      ) {
        return false
      }

      return true
    })
  }, [baseDisputes, searchTerm, statusFilter, typeFilter, priorityFilter])

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat("en-US", {
      style: "currency",
      currency: "USD",
    }).format(amount)
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
    })
  }

  const getStatusColor = (status: DisputeStatus) => {
    switch (status) {
      case "initiated":
        return "bg-blue-100 text-blue-800"
      case "under_review":
        return "bg-yellow-100 text-yellow-800"
      case "pending_info":
        return "bg-orange-100 text-orange-800"
      case "escalated":
        return "bg-red-100 text-red-800"
      case "resolved":
        return "bg-green-100 text-green-800"
      case "rejected":
        return "bg-gray-100 text-gray-800"
      default:
        return "bg-gray-100 text-gray-800"
    }
  }

  const getStatusIcon = (status: DisputeStatus) => {
    switch (status) {
      case "resolved":
        return <CheckCircle className="w-3 h-3" />
      case "rejected":
        return <XCircle className="w-3 h-3" />
      case "escalated":
        return <TrendingUp className="w-3 h-3" />
      case "pending_info":
        return <AlertCircle className="w-3 h-3" />
      default:
        return <Clock className="w-3 h-3" />
    }
  }

  const getPriorityColor = (priority: DisputePriority) => {
    switch (priority) {
      case "low":
        return "bg-gray-100 text-gray-800"
      case "medium":
        return "bg-blue-100 text-blue-800"
      case "high":
        return "bg-orange-100 text-orange-800"
      case "urgent":
        return "bg-red-100 text-red-800"
      default:
        return "bg-gray-100 text-gray-800"
    }
  }

  const getTypeLabel = (type: DisputeType) => {
    switch (type) {
      case "commission_calculation":
        return "Commission Calculation"
      case "deal_attribution":
        return "Deal Attribution"
      case "payout_timing":
        return "Payout Timing"
      case "rate_discrepancy":
        return "Rate Discrepancy"
      case "bonus_eligibility":
        return "Bonus Eligibility"
      case "other":
        return "Other"
      default:
        return type
    }
  }

  const getDaysAgo = (dateString: string) => {
    const date = new Date(dateString)
    const now = new Date()
    const diffTime = Math.abs(now.getTime() - date.getTime())
    const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24))

    if (diffDays === 0) return "Today"
    if (diffDays === 1) return "1 day ago"
    return `${diffDays} days ago`
  }

  const isOverdue = (dispute: Dispute) => {
    if (!dispute.dueDate || dispute.status === "resolved" || dispute.status === "rejected") return false
    return new Date(dispute.dueDate) < new Date()
  }

  return (
    <WebMcpTool
      tool="listDisputes"
      description="List commission disputes, optionally filtered by sales rep, status, or priority"
      endpoint="/disputes"
      method="GET"
      params={[
        { name: "salesRepId", description: "Filter by sales rep identifier", required: false },
        {
          name: "status",
          description: "Filter by status (INITIATED, UNDER_REVIEW, PENDING_INFO, ESCALATED, RESOLVED, REJECTED)",
          required: false,
        },
        {
          name: "priority",
          description: "Filter by priority (LOW, MEDIUM, HIGH, URGENT)",
          required: false,
        },
      ]}
      className="space-y-6"
    >
      {/* Filters */}
      <Card>
        <CardHeader>
          <CardTitle>Disputes</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex flex-col lg:flex-row gap-4">
            <div className="flex-1">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
                <Input
                  placeholder="Search disputes..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="pl-10"
                />
              </div>
            </div>

            <div className="flex gap-2">
              <Select value={statusFilter} onValueChange={(value) => setStatusFilter(value as DisputeStatus | "all")}>
                <SelectTrigger className="w-[140px]">
                  <SelectValue placeholder="Status" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Status</SelectItem>
                  <SelectItem value="initiated">Initiated</SelectItem>
                  <SelectItem value="under_review">Under Review</SelectItem>
                  <SelectItem value="pending_info">Pending Info</SelectItem>
                  <SelectItem value="escalated">Escalated</SelectItem>
                  <SelectItem value="resolved">Resolved</SelectItem>
                  <SelectItem value="rejected">Rejected</SelectItem>
                </SelectContent>
              </Select>

              <Select value={typeFilter} onValueChange={(value) => setTypeFilter(value as DisputeType | "all")}>
                <SelectTrigger className="w-[160px]">
                  <SelectValue placeholder="Type" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Types</SelectItem>
                  <SelectItem value="commission_calculation">Commission Calc</SelectItem>
                  <SelectItem value="deal_attribution">Deal Attribution</SelectItem>
                  <SelectItem value="payout_timing">Payout Timing</SelectItem>
                  <SelectItem value="rate_discrepancy">Rate Discrepancy</SelectItem>
                  <SelectItem value="bonus_eligibility">Bonus Eligibility</SelectItem>
                  <SelectItem value="other">Other</SelectItem>
                </SelectContent>
              </Select>

              <Select
                value={priorityFilter}
                onValueChange={(value) => setPriorityFilter(value as DisputePriority | "all")}
              >
                <SelectTrigger className="w-[120px]">
                  <SelectValue placeholder="Priority" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Priority</SelectItem>
                  <SelectItem value="low">Low</SelectItem>
                  <SelectItem value="medium">Medium</SelectItem>
                  <SelectItem value="high">High</SelectItem>
                  <SelectItem value="urgent">Urgent</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Tabs */}
      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList>
          <TabsTrigger value="all">All Disputes ({allDisputes.length})</TabsTrigger>
          <TabsTrigger value="my">My Disputes ({myDisputes.length})</TabsTrigger>
        </TabsList>

        <TabsContent value={activeTab} className="space-y-4">
          {filteredDisputes.length === 0 ? (
            <Card>
              <CardContent className="text-center py-8">
                <AlertCircle className="w-12 h-12 text-gray-400 mx-auto mb-4" />
                <h3 className="text-lg font-medium mb-2">No disputes found</h3>
                <p className="text-muted-foreground">
                  {searchTerm || statusFilter !== "all" || typeFilter !== "all" || priorityFilter !== "all"
                    ? "Try adjusting your filters to see more results."
                    : "No disputes have been created yet."}
                </p>
              </CardContent>
            </Card>
          ) : (
            <div className="space-y-4">
              {filteredDisputes.map((dispute) => (
                <Card
                  key={dispute.id}
                  className={`hover:shadow-md transition-shadow ${isOverdue(dispute) ? "border-red-200 bg-red-50" : ""}`}
                >
                  <CardContent className="p-6">
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <div className="flex items-center gap-3 mb-2">
                          <h3 className="font-semibold text-lg">{dispute.title}</h3>
                          <Badge className={getStatusColor(dispute.status)}>
                            {getStatusIcon(dispute.status)}
                            <span className="ml-1 capitalize">{dispute.status.replace("_", " ")}</span>
                          </Badge>
                          <Badge className={getPriorityColor(dispute.priority)}>{dispute.priority.toUpperCase()}</Badge>
                          {isOverdue(dispute) && (
                            <Badge className="bg-red-100 text-red-800">
                              <AlertCircle className="w-3 h-3 mr-1" />
                              Overdue
                            </Badge>
                          )}
                        </div>

                        <p className="text-muted-foreground mb-4 line-clamp-2">{dispute.description}</p>

                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 text-sm">
                          <div className="flex items-center gap-2">
                            <User className="w-4 h-4 text-gray-400" />
                            <span className="text-muted-foreground">Submitted by:</span>
                            <span className="font-medium">{dispute.submittedByName}</span>
                          </div>

                          <div className="flex items-center gap-2">
                            <Calendar className="w-4 h-4 text-gray-400" />
                            <span className="text-muted-foreground">Created:</span>
                            <span>{getDaysAgo(dispute.createdAt)}</span>
                          </div>

                          <div className="flex items-center gap-2">
                            <FileText className="w-4 h-4 text-gray-400" />
                            <span className="text-muted-foreground">Type:</span>
                            <span>{getTypeLabel(dispute.type)}</span>
                          </div>

                          {(dispute.disputedAmount > 0 || dispute.expectedAmount > 0) && (
                            <div className="flex items-center gap-2">
                              <DollarSign className="w-4 h-4 text-gray-400" />
                              <span className="text-muted-foreground">Amount:</span>
                              <span className="font-medium">
                                {dispute.expectedAmount > 0
                                  ? formatCurrency(dispute.expectedAmount)
                                  : formatCurrency(dispute.disputedAmount)}
                              </span>
                            </div>
                          )}
                        </div>

                        {(dispute.dealId || dispute.commissionId) && (
                          <div className="mt-3 flex items-center gap-2 text-sm">
                            <span className="text-muted-foreground">Related:</span>
                            {dispute.dealId && <Badge variant="outline">Deal: {dispute.dealId}</Badge>}
                            {dispute.commissionId && (
                              <Badge variant="outline">Commission: {dispute.commissionId}</Badge>
                            )}
                          </div>
                        )}

                        <div className="mt-4 flex items-center gap-4 text-sm text-muted-foreground">
                          <span className="flex items-center gap-1">
                            <MessageSquare className="w-3 h-3" />
                            {dispute.comments.length} comments
                          </span>
                          <span className="flex items-center gap-1">
                            <FileText className="w-3 h-3" />
                            {dispute.documents.length} documents
                          </span>
                          {dispute.assignedToName && (
                            <span className="flex items-center gap-1">
                              <User className="w-3 h-3" />
                              Assigned to: {dispute.assignedToName}
                            </span>
                          )}
                          {dispute.dueDate && (
                            <span className="flex items-center gap-1">
                              <Calendar className="w-3 h-3" />
                              Due: {formatDate(dispute.dueDate)}
                            </span>
                          )}
                        </div>

                        {dispute.tags.length > 0 && (
                          <div className="mt-3 flex flex-wrap gap-1">
                            {dispute.tags.map((tag) => (
                              <Badge key={tag} variant="outline" className="text-xs">
                                {tag}
                              </Badge>
                            ))}
                          </div>
                        )}
                      </div>

                      <div className="flex flex-col items-end gap-2">
                        <Button variant="outline" size="sm" onClick={() => onViewDispute(dispute)}>
                          <Eye className="w-4 h-4 mr-2" />
                          View Details
                        </Button>

                        {dispute.dueDate && (
                          <div
                            className={`text-xs ${isOverdue(dispute) ? "text-red-600 font-medium" : "text-muted-foreground"}`}
                          >
                            Due: {formatDate(dispute.dueDate)}
                          </div>
                        )}
                      </div>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
        </TabsContent>
      </Tabs>
    </WebMcpTool>
  )
}
