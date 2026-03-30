import type {
  Dispute,
  DisputeFormData,
  DisputeFilter,
  DisputeMetrics,
  DisputeWorkflowAction,
  DisputeComment,
  DisputeDocument,
  DisputeNotification,
  DisputeStatus,
  DisputeStatusHistory,
} from "./types"

export class DisputeWorkflowEngine {
  private disputes: Map<string, Dispute> = new Map()
  private notifications: DisputeNotification[] = []
  private workflowActions: DisputeWorkflowAction[] = []

  constructor() {
    this.initializeWorkflowActions()
    this.loadMockData()
  }

  private initializeWorkflowActions() {
    this.workflowActions = [
      {
        id: "start_review",
        label: "Start Review",
        description: "Begin reviewing the dispute",
        fromStatus: ["initiated"],
        toStatus: "under_review",
        requiredRole: ["admin", "manager"],
        requiresComment: true,
        requiresApproval: false,
        notificationTargets: ["submitter", "assignee"],
      },
      {
        id: "request_info",
        label: "Request Information",
        description: "Request additional information from submitter",
        fromStatus: ["under_review"],
        toStatus: "pending_info",
        requiredRole: ["admin", "manager"],
        requiresComment: true,
        requiresApproval: false,
        notificationTargets: ["submitter"],
      },
      {
        id: "provide_info",
        label: "Provide Information",
        description: "Provide requested information",
        fromStatus: ["pending_info"],
        toStatus: "under_review",
        requiredRole: ["sales"],
        requiresComment: true,
        requiresApproval: false,
        notificationTargets: ["assignee"],
      },
      {
        id: "escalate",
        label: "Escalate",
        description: "Escalate to higher authority",
        fromStatus: ["under_review", "pending_info"],
        toStatus: "escalated",
        requiredRole: ["admin", "manager"],
        requiresComment: true,
        requiresApproval: false,
        notificationTargets: ["finance", "senior_management"],
      },
      {
        id: "resolve",
        label: "Resolve",
        description: "Mark dispute as resolved",
        fromStatus: ["under_review", "escalated"],
        toStatus: "resolved",
        requiredRole: ["admin", "manager", "finance"],
        requiresComment: true,
        requiresApproval: true,
        notificationTargets: ["submitter", "all_stakeholders"],
      },
      {
        id: "reject",
        label: "Reject",
        description: "Reject the dispute",
        fromStatus: ["under_review", "escalated"],
        toStatus: "rejected",
        requiredRole: ["admin", "manager", "finance"],
        requiresComment: true,
        requiresApproval: true,
        notificationTargets: ["submitter"],
      },
    ]
  }

  private loadMockData() {
    const mockDisputes: Dispute[] = [
      {
        id: "DISP-001",
        title: "Commission Rate Discrepancy - Enterprise Deal",
        description:
          "The commission rate applied to my Enterprise Software License deal (DEAL-001) appears to be incorrect. I should have received the accelerator bonus rate of 15% instead of the standard 10%. According to the Q1 commission plan, deals over $100K qualify for the accelerator rate.",
        type: "rate_discrepancy",
        priority: "high",
        status: "under_review",
        dealId: "deal-001",
        commissionId: "comm-001",
        disputedAmount: 12500,
        expectedAmount: 18750,
        currency: "USD",
        submittedBy: "rep-001",
        submittedByName: "Sarah Johnson",
        submittedByRole: "sales",
        assignedTo: "admin-001",
        assignedToName: "Mike Chen",
        createdAt: "2024-01-20T10:00:00Z",
        updatedAt: "2024-01-22T14:30:00Z",
        dueDate: "2024-01-27T23:59:59Z",
        documents: [
          {
            id: "DOC-DISP-001",
            name: "Enterprise_Deal_Contract.pdf",
            type: "application/pdf",
            url: "/documents/Enterprise_Deal_Contract.pdf",
            uploadedAt: "2024-01-20T10:05:00Z",
            uploadedBy: "Sarah Johnson",
            size: 2048576,
          },
          {
            id: "DOC-DISP-002",
            name: "Commission_Plan_Q1_2024.pdf",
            type: "application/pdf",
            url: "/documents/Commission_Plan_Q1_2024.pdf",
            uploadedAt: "2024-01-20T10:10:00Z",
            uploadedBy: "Sarah Johnson",
            size: 1024000,
          },
        ],
        comments: [
          {
            id: "COMM-DISP-001",
            disputeId: "DISP-001",
            userId: "rep-001",
            userName: "Sarah Johnson",
            userRole: "sales",
            content:
              "I have attached the signed contract and the Q1 commission plan showing the accelerator bonus criteria. This deal clearly qualifies for the 15% rate as it's over $100K and closed in Q1.",
            createdAt: "2024-01-20T10:15:00Z",
            isInternal: false,
          },
          {
            id: "COMM-DISP-002",
            disputeId: "DISP-001",
            userId: "admin-001",
            userName: "Mike Chen",
            userRole: "admin",
            content:
              "Thank you for providing the documentation. I am reviewing the contract terms and commission plan criteria. I can see the deal amount is $125K which does qualify for the accelerator rate. Will update within 24 hours.",
            createdAt: "2024-01-22T14:30:00Z",
            isInternal: false,
          },
        ],
        statusHistory: [
          {
            id: "HIST-001",
            disputeId: "DISP-001",
            fromStatus: "initiated",
            toStatus: "under_review",
            changedBy: "admin-001",
            changedByName: "Mike Chen",
            changedAt: "2024-01-21T09:00:00Z",
            reason: "Starting review process",
            notes: "Assigned to commission specialist for detailed review",
          },
        ],
        tags: ["high-value", "enterprise", "accelerator-bonus", "Q1-2024"],
      },
      {
        id: "DISP-002",
        title: "Deal Attribution Question - Professional Services",
        description:
          "I believe I should be credited for the Professional Services Package deal (DEAL-002) as I was the primary contact and did most of the relationship building over 6 months, even though Tom Wilson technically closed it. I have extensive email communication and meeting notes to support this.",
        type: "deal_attribution",
        priority: "medium",
        status: "pending_info",
        dealId: "deal-002",
        disputedAmount: 0,
        expectedAmount: 9000,
        currency: "USD",
        submittedBy: "rep-002",
        submittedByName: "Alex Rodriguez",
        submittedByRole: "sales",
        assignedTo: "admin-001",
        assignedToName: "Mike Chen",
        createdAt: "2024-01-18T15:30:00Z",
        updatedAt: "2024-01-19T11:20:00Z",
        dueDate: "2024-01-25T23:59:59Z",
        documents: [
          {
            id: "DOC-DISP-003",
            name: "Email_Thread_Client_Communication.pdf",
            type: "application/pdf",
            url: "/documents/Email_Thread_Client_Communication.pdf",
            uploadedAt: "2024-01-18T15:35:00Z",
            uploadedBy: "Alex Rodriguez",
            size: 1536000,
          },
        ],
        comments: [
          {
            id: "COMM-DISP-003",
            disputeId: "DISP-002",
            userId: "rep-002",
            userName: "Alex Rodriguez",
            userRole: "sales",
            content:
              "I have been working with Innovation Corp for 6 months and built the entire relationship. The email thread shows my extensive communication with the client from initial contact through needs assessment.",
            createdAt: "2024-01-18T15:40:00Z",
            isInternal: false,
          },
          {
            id: "COMM-DISP-004",
            disputeId: "DISP-002",
            userId: "admin-001",
            userName: "Mike Chen",
            userRole: "admin",
            content:
              "I need additional documentation showing your role in the deal progression. Can you provide meeting notes, proposal drafts, or CRM activity logs that demonstrate your primary involvement?",
            createdAt: "2024-01-19T11:20:00Z",
            isInternal: false,
          },
        ],
        statusHistory: [
          {
            id: "HIST-002",
            disputeId: "DISP-002",
            fromStatus: "initiated",
            toStatus: "under_review",
            changedBy: "admin-001",
            changedByName: "Mike Chen",
            changedAt: "2024-01-18T16:00:00Z",
            reason: "Initial review started",
            notes: "Need to verify deal attribution claims",
          },
          {
            id: "HIST-003",
            disputeId: "DISP-002",
            fromStatus: "under_review",
            toStatus: "pending_info",
            changedBy: "admin-001",
            changedByName: "Mike Chen",
            changedAt: "2024-01-19T11:20:00Z",
            reason: "Requesting additional documentation",
            notes: "Need more evidence of primary involvement in deal",
          },
        ],
        tags: ["attribution", "relationship-building", "professional-services"],
      },
    ]

    mockDisputes.forEach((dispute) => {
      this.disputes.set(dispute.id, dispute)
    })
  }

  async createDispute(formData: DisputeFormData, userId: string, userName: string, userRole: string): Promise<Dispute> {
    const disputeId = `DISP-${Date.now().toString().slice(-6)}`
    const now = new Date().toISOString()

    const dispute: Dispute = {
      id: disputeId,
      title: formData.title,
      description: formData.description,
      type: formData.type,
      priority: formData.priority,
      status: "initiated",
      dealId: formData.dealId,
      commissionId: formData.commissionId,
      disputedAmount: formData.disputedAmount,
      expectedAmount: formData.expectedAmount,
      currency: "USD",
      submittedBy: userId,
      submittedByName: userName,
      submittedByRole: userRole as any,
      createdAt: now,
      updatedAt: now,
      dueDate: this.calculateDueDate(formData.priority),
      documents: [],
      comments: [],
      statusHistory: [],
      tags: formData.tags,
    }

    // Process uploaded documents
    for (const file of formData.documents) {
      const document: DisputeDocument = {
        id: `DOC-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
        name: file.name,
        type: file.type,
        url: `/documents/${file.name}`,
        uploadedAt: now,
        uploadedBy: userName,
        size: file.size,
      }
      dispute.documents.push(document)
    }

    // Add initial status history
    const initialHistory: DisputeStatusHistory = {
      id: `HIST-${Date.now()}`,
      disputeId: disputeId,
      fromStatus: "initiated",
      toStatus: "initiated",
      changedBy: userId,
      changedByName: userName,
      changedAt: now,
      reason: "Dispute created",
      notes: "Initial dispute submission",
    }
    dispute.statusHistory.push(initialHistory)

    // Store dispute
    this.disputes.set(disputeId, dispute)

    // Auto-assign based on type
    await this.autoAssignDispute(dispute)

    // Create notification
    this.createNotification(
      dispute,
      "status_change",
      "New Dispute Created",
      `Dispute ${disputeId} has been created by ${userName}`,
    )

    return dispute
  }

  async updateDisputeStatus(
    disputeId: string,
    newStatus: DisputeStatus,
    userId: string,
    userName: string,
    reason?: string,
    notes?: string,
  ): Promise<Dispute> {
    const dispute = this.disputes.get(disputeId)
    if (!dispute) {
      throw new Error(`Dispute not found: ${disputeId}`)
    }

    const oldStatus = dispute.status
    dispute.status = newStatus
    dispute.updatedAt = new Date().toISOString()

    if (newStatus === "resolved") {
      dispute.resolvedAt = new Date().toISOString()
    }

    // Add status history
    const historyEntry: DisputeStatusHistory = {
      id: `HIST-${Date.now()}`,
      disputeId: disputeId,
      fromStatus: oldStatus,
      toStatus: newStatus,
      changedBy: userId,
      changedByName: userName,
      changedAt: new Date().toISOString(),
      reason,
      notes,
    }
    dispute.statusHistory.push(historyEntry)

    // Create notification
    this.createNotification(
      dispute,
      "status_change",
      "Dispute Status Updated",
      `Dispute ${disputeId} status changed from ${oldStatus} to ${newStatus}`,
    )

    return dispute
  }

  async addComment(
    disputeId: string,
    userId: string,
    userName: string,
    userRole: string,
    content: string,
    isInternal = false,
  ): Promise<DisputeComment> {
    const dispute = this.disputes.get(disputeId)
    if (!dispute) {
      throw new Error(`Dispute not found: ${disputeId}`)
    }

    const comment: DisputeComment = {
      id: `COMM-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
      disputeId,
      userId,
      userName,
      userRole: userRole as any,
      content,
      createdAt: new Date().toISOString(),
      isInternal,
    }

    dispute.comments.push(comment)
    dispute.updatedAt = new Date().toISOString()

    // Create notification
    this.createNotification(
      dispute,
      "comment_added",
      "New Comment Added",
      `${userName} added a comment to dispute ${disputeId}`,
    )

    return comment
  }

  async addDocument(disputeId: string, file: File, userId: string, userName: string): Promise<DisputeDocument> {
    const dispute = this.disputes.get(disputeId)
    if (!dispute) {
      throw new Error(`Dispute not found: ${disputeId}`)
    }

    const document: DisputeDocument = {
      id: `DOC-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
      name: file.name,
      type: file.type,
      url: `/documents/${file.name}`,
      uploadedAt: new Date().toISOString(),
      uploadedBy: userName,
      size: file.size,
    }

    dispute.documents.push(document)
    dispute.updatedAt = new Date().toISOString()

    // Create notification
    this.createNotification(
      dispute,
      "document_uploaded",
      "Document Uploaded",
      `${userName} uploaded a document to dispute ${disputeId}`,
    )

    return document
  }

  getDisputes(filter?: DisputeFilter): Dispute[] {
    let disputes = Array.from(this.disputes.values())

    if (!filter) {
      return disputes.sort((a, b) => new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime())
    }

    // Apply filters
    if (filter.status?.length) {
      disputes = disputes.filter((d) => filter.status!.includes(d.status))
    }

    if (filter.type?.length) {
      disputes = disputes.filter((d) => filter.type!.includes(d.type))
    }

    if (filter.priority?.length) {
      disputes = disputes.filter((d) => filter.priority!.includes(d.priority))
    }

    if (filter.submittedBy?.length) {
      disputes = disputes.filter((d) => filter.submittedBy!.includes(d.submittedBy))
    }

    if (filter.assignedTo?.length) {
      disputes = disputes.filter((d) => d.assignedTo && filter.assignedTo!.includes(d.assignedTo))
    }

    if (filter.dateRange) {
      const start = new Date(filter.dateRange.start)
      const end = new Date(filter.dateRange.end)
      disputes = disputes.filter((d) => {
        const created = new Date(d.createdAt)
        return created >= start && created <= end
      })
    }

    if (filter.amountRange) {
      disputes = disputes.filter(
        (d) => d.disputedAmount >= filter.amountRange!.min && d.disputedAmount <= filter.amountRange!.max,
      )
    }

    if (filter.tags?.length) {
      disputes = disputes.filter((d) => filter.tags!.some((tag) => d.tags.includes(tag)))
    }

    return disputes.sort((a, b) => new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime())
  }

  getDispute(disputeId: string): Dispute | undefined {
    return this.disputes.get(disputeId)
  }

  getDisputesByUser(userId: string): Dispute[] {
    return Array.from(this.disputes.values()).filter((d) => d.submittedBy === userId || d.assignedTo === userId)
  }

  getAvailableActions(disputeId: string, userRole: string): DisputeWorkflowAction[] {
    const dispute = this.disputes.get(disputeId)
    if (!dispute) return []

    return this.workflowActions.filter(
      (action) => action.fromStatus.includes(dispute.status) && action.requiredRole.includes(userRole),
    )
  }

  getMetrics(): DisputeMetrics {
    const disputes = Array.from(this.disputes.values())

    const resolvedDisputes = disputes.filter((d) => d.status === "resolved")
    const averageResolutionTime =
      resolvedDisputes.length > 0
        ? resolvedDisputes.reduce((sum, d) => {
            if (d.resolvedAt) {
              const created = new Date(d.createdAt).getTime()
              const resolved = new Date(d.resolvedAt).getTime()
              return sum + (resolved - created)
            }
            return sum
          }, 0) /
          resolvedDisputes.length /
          (1000 * 60 * 60 * 24) // Convert to days
        : 0

    return {
      totalDisputes: disputes.length,
      activeDisputes: disputes.filter((d) => !["resolved", "rejected"].includes(d.status)).length,
      resolvedDisputes: resolvedDisputes.length,
      averageResolutionTime,
      disputesByType: disputes.reduce(
        (acc, d) => {
          acc[d.type] = (acc[d.type] || 0) + 1
          return acc
        },
        {} as Record<string, number>,
      ),
      disputesByStatus: disputes.reduce(
        (acc, d) => {
          acc[d.status] = (acc[d.status] || 0) + 1
          return acc
        },
        {} as Record<string, number>,
      ),
      disputesByPriority: disputes.reduce(
        (acc, d) => {
          acc[d.priority] = (acc[d.priority] || 0) + 1
          return acc
        },
        {} as Record<string, number>,
      ),
    }
  }

  getNotifications(userId: string): DisputeNotification[] {
    return this.notifications.filter((n) => n.recipients.includes(userId) || n.recipients.includes("all_stakeholders"))
  }

  private calculateDueDate(priority: string): string {
    const now = new Date()
    let daysToAdd = 7 // Default

    switch (priority) {
      case "urgent":
        daysToAdd = 1
        break
      case "high":
        daysToAdd = 3
        break
      case "medium":
        daysToAdd = 7
        break
      case "low":
        daysToAdd = 14
        break
    }

    now.setDate(now.getDate() + daysToAdd)
    return now.toISOString()
  }

  private async autoAssignDispute(dispute: Dispute): Promise<void> {
    // Auto-assignment logic based on type
    let assigneeId: string | undefined
    let assigneeName: string | undefined

    switch (dispute.type) {
      case "commission_calculation":
      case "rate_discrepancy":
        assigneeId = "admin-001"
        assigneeName = "Mike Chen"
        break
      case "deal_attribution":
        assigneeId = "admin-002"
        assigneeName = "Lisa Wang"
        break
      case "payout_timing":
        assigneeId = "finance-001"
        assigneeName = "David Kim"
        break
      default:
        assigneeId = "admin-001"
        assigneeName = "Mike Chen"
    }

    if (assigneeId && assigneeName) {
      dispute.assignedTo = assigneeId
      dispute.assignedToName = assigneeName

      // Add assignment to status history
      const assignmentHistory: DisputeStatusHistory = {
        id: `HIST-${Date.now()}`,
        disputeId: dispute.id,
        fromStatus: dispute.status,
        toStatus: dispute.status,
        changedBy: "system",
        changedByName: "System",
        changedAt: new Date().toISOString(),
        reason: "Auto-assignment",
        notes: `Automatically assigned to ${assigneeName} based on dispute type`,
      }
      dispute.statusHistory.push(assignmentHistory)

      // Create notification
      this.createNotification(
        dispute,
        "assignment",
        "Dispute Assigned",
        `Dispute ${dispute.id} has been assigned to ${assigneeName}`,
      )
    }
  }

  private createNotification(dispute: Dispute, type: DisputeNotification["type"], title: string, message: string) {
    const notification: DisputeNotification = {
      id: `NOTIF-${Date.now()}`,
      disputeId: dispute.id,
      type,
      title,
      message,
      recipients: this.getNotificationRecipients(dispute, type),
      createdAt: new Date().toISOString(),
      readBy: [],
    }

    this.notifications.push(notification)
  }

  private getNotificationRecipients(dispute: Dispute, type: DisputeNotification["type"]): string[] {
    const recipients = new Set<string>()

    // Always notify submitter and assignee
    recipients.add(dispute.submittedBy)
    if (dispute.assignedTo) recipients.add(dispute.assignedTo)

    // Add type-specific recipients
    switch (type) {
      case "escalation":
        recipients.add("finance-team")
        recipients.add("senior-management")
        break
      case "resolution":
        recipients.add("all-stakeholders")
        break
    }

    return Array.from(recipients)
  }
}

// Export singleton instance
export const disputeWorkflowEngine = new DisputeWorkflowEngine()
