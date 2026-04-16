"use client"

import { useState } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Textarea } from "@/components/ui/textarea"
import { Label } from "@/components/ui/label"
import { Input } from "@/components/ui/input"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Alert, AlertDescription } from "@/components/ui/alert"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import {
  ArrowLeft,
  Clock,
  AlertCircle,
  CheckCircle,
  XCircle,
  TrendingUp,
  Calendar,
  User,
  FileText,
  Download,
  MessageSquare,
  Send,
  Eye,
  ExternalLink,
  DollarSign,
} from "lucide-react"
import type { Dispute, DisputeStatus, DisputePriority } from "@/lib/dispute-workflow/types"
import { disputesApi, mapApiDisputeToLocal } from "@/lib/api"

interface DisputeDetailProps {
  dispute: Dispute
  onBack: () => void
  userRole: "sales" | "admin" | "finance" | "manager"
  userId: string
  userName: string
}

export function DisputeDetail({ dispute: initialDispute, onBack, userRole, userId, userName }: DisputeDetailProps) {
  const [dispute, setDispute] = useState<Dispute>(initialDispute)
  const [newComment, setNewComment] = useState("")
  const [isSubmittingComment, setIsSubmittingComment] = useState(false)
  const [isUpdatingStatus, setIsUpdatingStatus] = useState(false)
  const [statusUpdateReason, setStatusUpdateReason] = useState("")
  const [resolutionAmount, setResolutionAmount] = useState("")
  const [resolutionNotes, setResolutionNotes] = useState("")
  const [openActionId, setOpenActionId] = useState<string | null>(null)

  // Actions available based on dispute status and user role
  const availableActions = (() => {
    const actions: { id: string; label: string; toStatus: DisputeStatus }[] = []
    if (dispute.status === "initiated" || dispute.status === "under_review") {
      if (userRole === "admin" || userRole === "manager") {
        actions.push({ id: "resolve", label: "Resolve", toStatus: "resolved" })
        actions.push({ id: "escalate", label: "Escalate", toStatus: "escalated" })
        actions.push({ id: "reject", label: "Reject", toStatus: "rejected" })
      }
    }
    if (dispute.status === "escalated" && (userRole === "admin" || userRole === "manager")) {
      actions.push({ id: "resolve", label: "Resolve", toStatus: "resolved" })
    }
    return actions
  })()

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
      hour: "2-digit",
      minute: "2-digit",
    })
  }

  const formatFileSize = (bytes: number) => {
    if (bytes === 0) return "0 Bytes"
    const k = 1024
    const sizes = ["Bytes", "KB", "MB", "GB"]
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return Number.parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + " " + sizes[i]
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
        return <CheckCircle className="w-4 h-4" />
      case "rejected":
        return <XCircle className="w-4 h-4" />
      case "escalated":
        return <TrendingUp className="w-4 h-4" />
      case "pending_info":
        return <AlertCircle className="w-4 h-4" />
      default:
        return <Clock className="w-4 h-4" />
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

  const handleAddComment = async () => {
    if (!newComment.trim()) return

    setIsSubmittingComment(true)

    try {
      const updated = await disputesApi.addComment(dispute.id, {
        userId,
        userName,
        text: newComment,
      })
      const mapped = mapApiDisputeToLocal(updated)
      setDispute((prev) => ({ ...prev, comments: mapped.comments, documents: mapped.documents }))
      setNewComment("")
    } catch (error) {
      console.error("Error adding comment:", error)
    } finally {
      setIsSubmittingComment(false)
    }
  }

  const handleStatusUpdate = async (newStatus: DisputeStatus) => {
    setIsUpdatingStatus(true)

    try {
      let updated
      if (newStatus === "resolved") {
        updated = await disputesApi.resolve(dispute.id, {
          resolution: resolutionNotes || statusUpdateReason || "Resolved",
          resolvedBy: userId,
          approved: true,
        })
      } else if (newStatus === "escalated") {
        updated = await disputesApi.escalate(dispute.id)
      } else if (newStatus === "rejected") {
        updated = await disputesApi.resolve(dispute.id, {
          resolution: statusUpdateReason || "Rejected",
          resolvedBy: userId,
          approved: false,
        })
      }

      if (updated) {
        const mapped = mapApiDisputeToLocal(updated)
        setDispute((prev) => ({
          ...prev,
          status: mapped.status,
          resolution: mapped.resolution,
          resolvedAt: mapped.resolvedAt,
          comments: mapped.comments,
          documents: mapped.documents,
          ...(newStatus === "resolved" && resolutionAmount
            ? { resolutionAmount: Number.parseFloat(resolutionAmount) }
            : {}),
        }))
      }

      setStatusUpdateReason("")
      setResolutionAmount("")
      setResolutionNotes("")
      setOpenActionId(null)
    } catch (error) {
      console.error("Error updating status:", error)
    } finally {
      setIsUpdatingStatus(false)
    }
  }

  const isOverdue = (dispute: Dispute) => {
    if (!dispute.dueDate || dispute.status === "resolved" || dispute.status === "rejected") return false
    return new Date(dispute.dueDate) < new Date()
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="sm" onClick={onBack}>
          <ArrowLeft className="w-4 h-4 mr-2" />
          Back to Disputes
        </Button>
        <div className="flex-1">
          <div className="flex items-center gap-3">
            <h1 className="text-2xl font-bold">{dispute.title}</h1>
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
          <p className="text-muted-foreground">Dispute ID: {dispute.id}</p>
        </div>

        {availableActions.length > 0 && (
          <div className="flex gap-2">
            {availableActions.map((action) => (
              <Dialog
                key={action.id}
                open={openActionId === action.id}
                onOpenChange={(open) => setOpenActionId(open ? action.id : null)}
              >
                <DialogTrigger asChild>
                  <Button variant={action.toStatus === "resolved" ? "default" : "outline"} size="sm">
                    {action.label}
                  </Button>
                </DialogTrigger>
                <DialogContent>
                  <DialogHeader>
                    <DialogTitle>{action.label}</DialogTitle>
                    <DialogDescription>{action.description}</DialogDescription>
                  </DialogHeader>
                  <div className="space-y-4">
                    <div>
                      <Label htmlFor="reason">Reason *</Label>
                      <Textarea
                        id="reason"
                        value={statusUpdateReason}
                        onChange={(e) => setStatusUpdateReason(e.target.value)}
                        placeholder="Explain the reason for this action..."
                        rows={3}
                      />
                    </div>
                    {action.toStatus === "resolved" && (
                      <>
                        <div>
                          <Label htmlFor="resolution-amount">Resolution Amount</Label>
                          <div className="relative">
                            <DollarSign className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-muted-foreground" />
                            <Input
                              id="resolution-amount"
                              type="number"
                              step="0.01"
                              value={resolutionAmount}
                              onChange={(e) => setResolutionAmount(e.target.value)}
                              className="pl-10"
                              placeholder="0.00"
                            />
                          </div>
                        </div>
                        <div>
                          <Label htmlFor="resolution-notes">Resolution Notes</Label>
                          <Textarea
                            id="resolution-notes"
                            value={resolutionNotes}
                            onChange={(e) => setResolutionNotes(e.target.value)}
                            placeholder="Additional notes about the resolution..."
                            rows={3}
                          />
                        </div>
                      </>
                    )}
                  </div>
                  <DialogFooter>
                    <Button
                      onClick={() => handleStatusUpdate(action.toStatus)}
                      disabled={isUpdatingStatus || !statusUpdateReason.trim()}
                    >
                      {isUpdatingStatus ? "Updating..." : action.label}
                    </Button>
                  </DialogFooter>
                </DialogContent>
              </Dialog>
            ))}
          </div>
        )}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Main Content */}
        <div className="lg:col-span-2 space-y-6">
          <Tabs defaultValue="details" className="w-full">
            <TabsList className="grid w-full grid-cols-4">
              <TabsTrigger value="details">Details</TabsTrigger>
              <TabsTrigger value="comments">Comments ({dispute.comments.length})</TabsTrigger>
              <TabsTrigger value="documents">Documents ({dispute.documents.length})</TabsTrigger>
              <TabsTrigger value="history">History ({dispute.statusHistory.length})</TabsTrigger>
            </TabsList>

            <TabsContent value="details" className="space-y-4">
              <Card>
                <CardHeader>
                  <CardTitle>Dispute Description</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="whitespace-pre-wrap">{dispute.description}</p>
                </CardContent>
              </Card>

              {dispute.resolution && (
                <Card>
                  <CardHeader>
                    <CardTitle className="text-green-600">Resolution</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <p className="whitespace-pre-wrap">{dispute.resolution}</p>
                    {dispute.resolutionAmount && (
                      <div className="mt-4 p-4 bg-green-50 rounded-lg">
                        <div className="flex items-center justify-between">
                          <span className="font-medium">Resolution Amount:</span>
                          <span className="text-lg font-bold text-green-600">
                            {formatCurrency(dispute.resolutionAmount)}
                          </span>
                        </div>
                      </div>
                    )}
                  </CardContent>
                </Card>
              )}
            </TabsContent>

            <TabsContent value="comments" className="space-y-4">
              {/* Add Comment */}
              <Card>
                <CardHeader>
                  <CardTitle>Add Comment</CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div>
                    <Label htmlFor="comment">Your Comment</Label>
                    <Textarea
                      id="comment"
                      value={newComment}
                      onChange={(e) => setNewComment(e.target.value)}
                      placeholder="Add your comment or question..."
                      rows={3}
                    />
                  </div>
                  <Button onClick={handleAddComment} disabled={isSubmittingComment || !newComment.trim()}>
                    <Send className="w-4 h-4 mr-2" />
                    {isSubmittingComment ? "Adding..." : "Add Comment"}
                  </Button>
                </CardContent>
              </Card>

              {/* Comments List */}
              <div className="space-y-4">
                {dispute.comments.length === 0 ? (
                  <Card>
                    <CardContent className="text-center py-8">
                      <MessageSquare className="w-12 h-12 text-gray-400 mx-auto mb-4" />
                      <h3 className="text-lg font-medium mb-2">No comments yet</h3>
                      <p className="text-muted-foreground">
                        Be the first to add a comment or question about this dispute.
                      </p>
                    </CardContent>
                  </Card>
                ) : (
                  dispute.comments.map((comment) => (
                    <Card key={comment.id}>
                      <CardContent className="pt-6">
                        <div className="flex items-start gap-4">
                          <div className="w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center">
                            <User className="w-4 h-4 text-blue-600" />
                          </div>
                          <div className="flex-1">
                            <div className="flex items-center gap-2 mb-2">
                              <span className="font-medium">{comment.userName}</span>
                              <Badge variant="outline" className="text-xs">
                                {comment.userRole}
                              </Badge>
                              <span className="text-sm text-muted-foreground">{formatDate(comment.createdAt)}</span>
                              {comment.isInternal && (
                                <Badge variant="secondary" className="text-xs">
                                  Internal
                                </Badge>
                              )}
                            </div>
                            <p className="whitespace-pre-wrap">{comment.content}</p>
                          </div>
                        </div>
                      </CardContent>
                    </Card>
                  ))
                )}
              </div>
            </TabsContent>

            <TabsContent value="documents" className="space-y-4">
              {dispute.documents.length === 0 ? (
                <Card>
                  <CardContent className="text-center py-8">
                    <FileText className="w-12 h-12 text-gray-400 mx-auto mb-4" />
                    <h3 className="text-lg font-medium mb-2">No documents uploaded</h3>
                    <p className="text-muted-foreground">Supporting documents will appear here when uploaded.</p>
                  </CardContent>
                </Card>
              ) : (
                <div className="space-y-4">
                  {dispute.documents.map((document) => (
                    <Card key={document.id}>
                      <CardContent className="pt-6">
                        <div className="flex items-center justify-between">
                          <div className="flex items-center gap-3">
                            <FileText className="w-8 h-8 text-blue-600" />
                            <div>
                              <h4 className="font-medium">{document.name}</h4>
                              <div className="flex items-center gap-2 text-sm text-muted-foreground">
                                <span>{document.type}</span>
                                <span>•</span>
                                <span>{formatFileSize(document.size)}</span>
                                <span>•</span>
                                <span>Uploaded by {document.uploadedBy}</span>
                                <span>•</span>
                                <span>{formatDate(document.uploadedAt)}</span>
                              </div>
                            </div>
                          </div>
                          <div className="flex gap-2">
                            <Button variant="outline" size="sm" disabled title="File bytes are not stored in this build">
                              <Eye className="w-4 h-4 mr-2" />
                              View
                            </Button>
                            <Button variant="outline" size="sm" disabled title="File bytes are not stored in this build">
                              <Download className="w-4 h-4 mr-2" />
                              Download
                            </Button>
                          </div>
                        </div>
                      </CardContent>
                    </Card>
                  ))}
                </div>
              )}
            </TabsContent>

            <TabsContent value="history" className="space-y-4">
              <div className="space-y-4">
                {dispute.statusHistory.map((history, index) => (
                  <Card key={history.id}>
                    <CardContent className="pt-6">
                      <div className="flex items-start gap-4">
                        <div className="w-8 h-8 bg-gray-100 rounded-full flex items-center justify-center">
                          <Clock className="w-4 h-4 text-gray-600" />
                        </div>
                        <div className="flex-1">
                          <div className="flex items-center gap-2 mb-1">
                            <span className="font-medium">Status changed</span>
                            <Badge className={getStatusColor(history.fromStatus)}>
                              {history.fromStatus.replace("_", " ")}
                            </Badge>
                            <span>→</span>
                            <Badge className={getStatusColor(history.toStatus)}>
                              {history.toStatus.replace("_", " ")}
                            </Badge>
                          </div>
                          <div className="text-sm text-muted-foreground">
                            <span>
                              By {history.changedByName} on {formatDate(history.changedAt)}
                            </span>
                            {history.reason && (
                              <div className="mt-1">
                                <span className="font-medium">Reason: </span>
                                {history.reason}
                              </div>
                            )}
                            {history.notes && (
                              <div className="mt-1">
                                <span className="font-medium">Notes: </span>
                                {history.notes}
                              </div>
                            )}
                          </div>
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                ))}
              </div>
            </TabsContent>
          </Tabs>
        </div>

        {/* Sidebar */}
        <div className="space-y-6">
          {/* Dispute Info */}
          <Card>
            <CardHeader>
              <CardTitle>Dispute Information</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-1 gap-4 text-sm">
                <div>
                  <span className="text-muted-foreground">Created:</span>
                  <div className="font-medium">{formatDate(dispute.createdAt)}</div>
                </div>
                <div>
                  <span className="text-muted-foreground">Updated:</span>
                  <div className="font-medium">{formatDate(dispute.updatedAt)}</div>
                </div>
                <div>
                  <span className="text-muted-foreground">Submitted by:</span>
                  <div className="font-medium">{dispute.submittedByName}</div>
                </div>
                {dispute.assignedToName && (
                  <div>
                    <span className="text-muted-foreground">Assigned to:</span>
                    <div className="font-medium">{dispute.assignedToName}</div>
                  </div>
                )}
              </div>

              {dispute.dueDate && (
                <div className="pt-4 border-t">
                  <div className="flex items-center gap-2 text-sm">
                    <Calendar className="w-4 h-4 text-orange-500" />
                    <span className="text-muted-foreground">Due Date:</span>
                    <span className={`font-medium ${isOverdue(dispute) ? "text-red-600" : ""}`}>
                      {formatDate(dispute.dueDate)}
                    </span>
                  </div>
                  {isOverdue(dispute) && (
                    <Alert className="mt-2">
                      <AlertCircle className="h-4 w-4" />
                      <AlertDescription className="text-xs">
                        This dispute is overdue and requires immediate attention.
                      </AlertDescription>
                    </Alert>
                  )}
                </div>
              )}
            </CardContent>
          </Card>

          {/* Financial Details */}
          {(dispute.disputedAmount > 0 || dispute.expectedAmount > 0) && (
            <Card>
              <CardHeader>
                <CardTitle>Financial Details</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                {dispute.disputedAmount > 0 && (
                  <div className="flex items-center justify-between">
                    <span className="text-muted-foreground">Current Amount:</span>
                    <span className="font-bold">{formatCurrency(dispute.disputedAmount)}</span>
                  </div>
                )}
                {dispute.expectedAmount > 0 && (
                  <div className="flex items-center justify-between">
                    <span className="text-muted-foreground">Expected Amount:</span>
                    <span className="font-bold text-green-600">{formatCurrency(dispute.expectedAmount)}</span>
                  </div>
                )}
                {dispute.disputedAmount > 0 && dispute.expectedAmount > 0 && (
                  <div className="pt-4 border-t">
                    <div className="flex items-center justify-between">
                      <span className="text-muted-foreground">Difference:</span>
                      <span
                        className={`font-bold ${dispute.expectedAmount > dispute.disputedAmount ? "text-green-600" : "text-red-600"}`}
                      >
                        {formatCurrency(dispute.expectedAmount - dispute.disputedAmount)}
                      </span>
                    </div>
                  </div>
                )}
                {dispute.resolutionAmount && (
                  <div className="pt-4 border-t">
                    <div className="flex items-center justify-between">
                      <span className="text-muted-foreground">Resolution Amount:</span>
                      <span className="font-bold text-green-600">{formatCurrency(dispute.resolutionAmount)}</span>
                    </div>
                  </div>
                )}
              </CardContent>
            </Card>
          )}

          {/* Related Records */}
          {(dispute.dealId || dispute.commissionId) && (
            <Card>
              <CardHeader>
                <CardTitle>Related Records</CardTitle>
              </CardHeader>
              <CardContent className="space-y-3">
                {dispute.dealId && (
                  <div className="flex items-center justify-between">
                    <span className="text-muted-foreground">Deal:</span>
                    <div className="flex items-center gap-2">
                      <Badge variant="outline">{dispute.dealId}</Badge>
                      <Button variant="ghost" size="sm">
                        <ExternalLink className="w-3 h-3" />
                      </Button>
                    </div>
                  </div>
                )}
                {dispute.commissionId && (
                  <div className="flex items-center justify-between">
                    <span className="text-muted-foreground">Commission:</span>
                    <div className="flex items-center gap-2">
                      <Badge variant="outline">{dispute.commissionId}</Badge>
                      <Button variant="ghost" size="sm">
                        <ExternalLink className="w-3 h-3" />
                      </Button>
                    </div>
                  </div>
                )}
              </CardContent>
            </Card>
          )}

          {/* Tags */}
          {dispute.tags.length > 0 && (
            <Card>
              <CardHeader>
                <CardTitle>Tags</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="flex flex-wrap gap-2">
                  {dispute.tags.map((tag) => (
                    <Badge key={tag} variant="secondary">
                      {tag}
                    </Badge>
                  ))}
                </div>
              </CardContent>
            </Card>
          )}
        </div>
      </div>
    </div>
  )
}
