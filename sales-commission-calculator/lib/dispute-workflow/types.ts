export interface DisputeDocument {
  id: string
  name: string
  type: string
  url: string
  uploadedAt: string
  uploadedBy: string
  size: number
}

export interface DisputeComment {
  id: string
  disputeId: string
  userId: string
  userName: string
  userRole: "sales" | "admin" | "finance" | "manager"
  content: string
  createdAt: string
  isInternal: boolean
  attachments?: DisputeDocument[]
}

export interface DisputeStatusHistory {
  id: string
  disputeId: string
  fromStatus: DisputeStatus
  toStatus: DisputeStatus
  changedBy: string
  changedByName: string
  changedAt: string
  reason?: string
  notes?: string
}

export type DisputeStatus = "initiated" | "under_review" | "pending_info" | "escalated" | "resolved" | "rejected"

export type DisputeType =
  | "commission_calculation"
  | "deal_attribution"
  | "payout_timing"
  | "rate_discrepancy"
  | "bonus_eligibility"
  | "other"

export type DisputePriority = "low" | "medium" | "high" | "urgent"

export interface Dispute {
  id: string
  title: string
  description: string
  type: DisputeType
  priority: DisputePriority
  status: DisputeStatus

  // Related entities
  dealId?: string
  commissionId?: string
  payoutId?: string

  // Financial details
  disputedAmount: number
  expectedAmount: number
  currency: string

  // Parties involved
  submittedBy: string
  submittedByName: string
  submittedByRole: "sales" | "admin" | "finance" | "manager"
  assignedTo?: string
  assignedToName?: string

  // Timestamps
  createdAt: string
  updatedAt: string
  dueDate?: string
  resolvedAt?: string

  // Additional data
  documents: DisputeDocument[]
  comments: DisputeComment[]
  statusHistory: DisputeStatusHistory[]
  tags: string[]

  // Resolution
  resolution?: string
  resolutionAmount?: number
  resolutionNotes?: string
}

export interface DisputeWorkflowAction {
  id: string
  label: string
  description: string
  fromStatus: DisputeStatus[]
  toStatus: DisputeStatus
  requiredRole: string[]
  requiresComment: boolean
  requiresApproval: boolean
  notificationTargets: string[]
}

export interface DisputeNotification {
  id: string
  disputeId: string
  type: "status_change" | "comment_added" | "document_uploaded" | "assignment" | "escalation" | "resolution"
  title: string
  message: string
  recipients: string[]
  createdAt: string
  readBy: string[]
}

export interface DisputeMetrics {
  totalDisputes: number
  activeDisputes: number
  resolvedDisputes: number
  averageResolutionTime: number
  disputesByType: Record<DisputeType, number>
  disputesByStatus: Record<DisputeStatus, number>
  disputesByPriority: Record<DisputePriority, number>
}

export interface DisputeFilter {
  status?: DisputeStatus[]
  type?: DisputeType[]
  priority?: DisputePriority[]
  assignedTo?: string[]
  submittedBy?: string[]
  dateRange?: {
    start: string
    end: string
  }
  amountRange?: {
    min: number
    max: number
  }
  tags?: string[]
}

export interface DisputeFormData {
  title: string
  description: string
  type: DisputeType
  priority: DisputePriority
  dealId?: string
  commissionId?: string
  disputedAmount: number
  expectedAmount: number
  documents: File[]
  tags: string[]
}
