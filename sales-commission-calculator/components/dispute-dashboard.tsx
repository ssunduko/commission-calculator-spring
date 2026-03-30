"use client"

import { useState, useEffect } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Plus, Clock, CheckCircle, TrendingUp, BarChart3, Calendar } from "lucide-react"
import { DisputeForm } from "./dispute-form"
import { DisputeList } from "./dispute-list"
import { DisputeDetail } from "./dispute-detail"
import type { Dispute } from "@/lib/dispute-workflow/types"
import { disputesApi, mapApiDisputeToLocal, type DisputeResponse } from "@/lib/api"

interface DisputeDashboardProps {
  userRole: "sales" | "admin" | "finance" | "manager"
  userId: string
  userName: string
}

export function DisputeDashboard({ userRole, userId, userName }: DisputeDashboardProps) {
  const [activeView, setActiveView] = useState<"list" | "create" | "detail">("list")
  const [selectedDispute, setSelectedDispute] = useState<Dispute | null>(null)
  const [apiDisputes, setApiDisputes] = useState<Dispute[]>([])

  const activeStatuses = ["initiated", "under_review", "pending_info", "escalated"]
  const resolvedStatuses = ["resolved", "rejected"]
  const metrics = {
    totalDisputes: apiDisputes.length,
    activeDisputes: apiDisputes.filter((d) => activeStatuses.includes(d.status)).length,
    resolvedDisputes: apiDisputes.filter((d) => resolvedStatuses.includes(d.status)).length,
    averageResolutionTime: 0,
    disputesByStatus: apiDisputes.reduce((acc, d) => { acc[d.status] = (acc[d.status] || 0) + 1; return acc }, {} as Record<string, number>),
    disputesByType: apiDisputes.reduce((acc, d) => { acc[d.type] = (acc[d.type] || 0) + 1; return acc }, {} as Record<string, number>),
    disputesByPriority: apiDisputes.reduce((acc, d) => { acc[d.priority] = (acc[d.priority] || 0) + 1; return acc }, {} as Record<string, number>),
  }

  const loadDisputes = () => {
    disputesApi.getAll().then((data) => {
      setApiDisputes(data.map(mapApiDisputeToLocal))
    }).catch((err) => {
      console.error("Failed to load disputes from API:", err)
    })
  }

  useEffect(() => {
    loadDisputes()
  }, [])

  const handleCreateDispute = () => {
    setActiveView("create")
  }

  const handleDisputeCreated = (disputeId: string) => {
    disputesApi.get(disputeId).then((data) => {
      const dispute = mapApiDisputeToLocal(data)
      setSelectedDispute(dispute)
      setActiveView("detail")
      loadDisputes()
    }).catch(() => {
      setActiveView("list")
      loadDisputes()
    })
  }

  const handleViewDispute = (dispute: Dispute) => {
    setSelectedDispute(dispute)
    setActiveView("detail")
  }

  const handleBackToList = () => {
    setActiveView("list")
    setSelectedDispute(null)
  }

  const handleCancelCreate = () => {
    setActiveView("list")
  }

  if (activeView === "create") {
    return (
      <div className="min-h-screen bg-gray-50 p-6">
        <div className="max-w-4xl mx-auto">
          <DisputeForm onSubmit={handleDisputeCreated} onCancel={handleCancelCreate} />
        </div>
      </div>
    )
  }

  if (activeView === "detail" && selectedDispute) {
    return (
      <div className="min-h-screen bg-gray-50 p-6">
        <div className="max-w-7xl mx-auto">
          <DisputeDetail
            dispute={selectedDispute}
            onBack={handleBackToList}
            userRole={userRole}
            userId={userId}
            userName={userName}
          />
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50 p-6">
      <div className="max-w-7xl mx-auto space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Dispute Management</h1>
            <p className="text-gray-600 mt-1">Track and manage commission disputes efficiently</p>
          </div>
          <Button onClick={handleCreateDispute}>
            <Plus className="w-4 h-4 mr-2" />
            Create Dispute
          </Button>
        </div>

        {/* Metrics Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium text-gray-600">Total Disputes</CardTitle>
              <BarChart3 className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-3xl font-bold">{metrics.totalDisputes}</div>
              <div className="text-xs text-muted-foreground mt-1">All time disputes</div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium text-gray-600">Active Disputes</CardTitle>
              <Clock className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-3xl font-bold text-orange-600">{metrics.activeDisputes}</div>
              <div className="text-xs text-muted-foreground mt-1">Pending resolution</div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium text-gray-600">Resolved</CardTitle>
              <CheckCircle className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-3xl font-bold text-green-600">{metrics.resolvedDisputes}</div>
              <div className="text-xs text-muted-foreground mt-1">Successfully resolved</div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium text-gray-600">Avg Resolution</CardTitle>
              <Calendar className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-3xl font-bold">{Math.round(metrics.averageResolutionTime)}</div>
              <div className="text-xs text-muted-foreground mt-1">Days to resolve</div>
            </CardContent>
          </Card>
        </div>

        {/* Main Content */}
        <Tabs defaultValue="disputes" className="space-y-6">
          <TabsList>
            <TabsTrigger value="disputes">All Disputes</TabsTrigger>
            <TabsTrigger value="analytics">Analytics</TabsTrigger>
          </TabsList>

          <TabsContent value="disputes">
            <DisputeList onViewDispute={handleViewDispute} userRole={userRole} userId={userId} />
          </TabsContent>

          <TabsContent value="analytics" className="space-y-6">
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              {/* Disputes by Status */}
              <Card>
                <CardHeader>
                  <CardTitle>Disputes by Status</CardTitle>
                  <CardDescription>Current distribution of dispute statuses</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    {Object.entries(metrics.disputesByStatus).map(([status, count]) => (
                      <div key={status} className="flex items-center justify-between">
                        <div className="flex items-center gap-2">
                          <Badge variant="outline" className="capitalize">
                            {status.replace("_", " ")}
                          </Badge>
                        </div>
                        <div className="flex items-center gap-2">
                          <div className="w-24 bg-gray-200 rounded-full h-2">
                            <div
                              className="bg-blue-600 h-2 rounded-full"
                              style={{ width: `${(count / metrics.totalDisputes) * 100}%` }}
                            />
                          </div>
                          <span className="text-sm font-medium w-8 text-right">{count}</span>
                        </div>
                      </div>
                    ))}
                  </div>
                </CardContent>
              </Card>

              {/* Disputes by Type */}
              <Card>
                <CardHeader>
                  <CardTitle>Disputes by Type</CardTitle>
                  <CardDescription>Most common dispute categories</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    {Object.entries(metrics.disputesByType).map(([type, count]) => (
                      <div key={type} className="flex items-center justify-between">
                        <div className="flex items-center gap-2">
                          <Badge variant="outline" className="capitalize">
                            {type.replace("_", " ")}
                          </Badge>
                        </div>
                        <div className="flex items-center gap-2">
                          <div className="w-24 bg-gray-200 rounded-full h-2">
                            <div
                              className="bg-green-600 h-2 rounded-full"
                              style={{ width: `${(count / metrics.totalDisputes) * 100}%` }}
                            />
                          </div>
                          <span className="text-sm font-medium w-8 text-right">{count}</span>
                        </div>
                      </div>
                    ))}
                  </div>
                </CardContent>
              </Card>

              {/* Disputes by Priority */}
              <Card>
                <CardHeader>
                  <CardTitle>Disputes by Priority</CardTitle>
                  <CardDescription>Priority level distribution</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    {Object.entries(metrics.disputesByPriority).map(([priority, count]) => (
                      <div key={priority} className="flex items-center justify-between">
                        <div className="flex items-center gap-2">
                          <Badge variant="outline" className="capitalize">
                            {priority}
                          </Badge>
                        </div>
                        <div className="flex items-center gap-2">
                          <div className="w-24 bg-gray-200 rounded-full h-2">
                            <div
                              className="bg-orange-600 h-2 rounded-full"
                              style={{ width: `${(count / metrics.totalDisputes) * 100}%` }}
                            />
                          </div>
                          <span className="text-sm font-medium w-8 text-right">{count}</span>
                        </div>
                      </div>
                    ))}
                  </div>
                </CardContent>
              </Card>

              {/* Quick Stats */}
              <Card>
                <CardHeader>
                  <CardTitle>Quick Statistics</CardTitle>
                  <CardDescription>Key performance indicators</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    <div className="flex items-center justify-between p-3 bg-blue-50 rounded-lg">
                      <div className="flex items-center gap-2">
                        <TrendingUp className="w-4 h-4 text-blue-600" />
                        <span className="text-sm font-medium">Resolution Rate</span>
                      </div>
                      <span className="text-lg font-bold text-blue-600">
                        {metrics.totalDisputes > 0
                          ? Math.round((metrics.resolvedDisputes / metrics.totalDisputes) * 100)
                          : 0}
                        %
                      </span>
                    </div>

                    <div className="flex items-center justify-between p-3 bg-green-50 rounded-lg">
                      <div className="flex items-center gap-2">
                        <CheckCircle className="w-4 h-4 text-green-600" />
                        <span className="text-sm font-medium">Success Rate</span>
                      </div>
                      <span className="text-lg font-bold text-green-600">
                        {metrics.resolvedDisputes > 0
                          ? Math.round(
                              (metrics.resolvedDisputes /
                                (metrics.resolvedDisputes + (metrics.disputesByStatus.rejected || 0))) *
                                100,
                            )
                          : 0}
                        %
                      </span>
                    </div>

                    <div className="flex items-center justify-between p-3 bg-orange-50 rounded-lg">
                      <div className="flex items-center gap-2">
                        <Clock className="w-4 h-4 text-orange-600" />
                        <span className="text-sm font-medium">Avg Resolution Time</span>
                      </div>
                      <span className="text-lg font-bold text-orange-600">
                        {Math.round(metrics.averageResolutionTime)} days
                      </span>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </div>
          </TabsContent>
        </Tabs>
      </div>
    </div>
  )
}
