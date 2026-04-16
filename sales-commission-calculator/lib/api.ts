const API_BASE = "/api"
const AUTH_HEADER = "Basic " + btoa("admin:admin123")

async function apiFetch<T>(path: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      Authorization: AUTH_HEADER,
      ...options?.headers,
    },
  })

  if (!res.ok) {
    const text = await res.text().catch(() => res.statusText)
    throw new Error(`API ${res.status}: ${text}`)
  }

  if (res.status === 204) return undefined as T
  return res.json()
}

// ── Deal types ──────────────────────────────────────────────────────────────

export type DealStatus = "OPEN" | "WON" | "LOST" | "CANCELLED"

export interface DealResponse {
  id: string
  title: string
  value: number
  status: DealStatus
  salesRepId: string
  closeDate: string | null
  createdDate: string | null
}

export interface CreateDealRequest {
  title: string
  value: number
  salesRepId: string
}

export interface UpdateDealRequest {
  title?: string
  value?: number
  status?: DealStatus
  closeDate?: string
}

// ── Plan types ──────────────────────────────────────────────────────────────

export type PlanStatus = "DRAFT" | "ACTIVE" | "INACTIVE" | "ARCHIVED"

export interface CommissionPlanResponse {
  id: string
  name: string
  currency: string
  status: PlanStatus
  effectiveStartDate: string | null
  effectiveEndDate: string | null
  createdDate: string | null
  rulesCount: number
  tiersCount: number
}

export interface CreateCommissionPlanRequest {
  name: string
  currencyCode: string
  effectiveStartDate?: string
  effectiveEndDate?: string
}

export interface AddRuleToPlanRequest {
  name: string
  description: string
  rate: number
  ruleType: string
  priority: number
}

// ── Calculation types ───────────────────────────────────────────────────────

export interface CommissionCalculationResponse {
  id: string
  dealId: string
  salesRepId: string
  baseCommission: number
  grossCommission: number
  netCommission: number
  status: string
  calculationDate: string | null
  planId: string
}

export interface CalculateCommissionRequest {
  dealId: string
  planId: string
}

// ── Dispute types ───────────────────────────────────────────────────────────

export type DisputeStatusApi =
  | "INITIATED"
  | "UNDER_REVIEW"
  | "ADDITIONAL_INFO_REQUESTED"
  | "ESCALATED"
  | "APPROVED"
  | "REJECTED"
  | "RESOLVED"
  | "CANCELLED"

export interface DisputeDocumentResponse {
  id: string
  name: string
  contentType: string | null
  sizeBytes: number
  uploadedBy: string | null
  uploadedAt: string | null
}

export interface DisputeCommentResponse {
  id: string
  userId: string | null
  userName: string | null
  text: string
  timestamp: string | null
  isSystemComment: boolean
}

export interface DisputeResponse {
  id: string
  calculationId: string
  salesRepId: string
  title: string
  description: string
  status: DisputeStatusApi
  isEscalated: boolean
  createdDate: string | null
  resolvedDate: string | null
  resolution: string | null
  commentsCount: number
  documents: DisputeDocumentResponse[]
  comments: DisputeCommentResponse[]
}

export interface CreateDisputeRequest {
  calculationId: string
  salesRepId: string
  title: string
  description: string
}

export interface ResolveDisputeRequest {
  resolution: string
  resolvedBy: string
  approved: boolean
}

export interface AddCommentRequest {
  userId: string
  userName: string
  text: string
}

export interface AddDocumentRequest {
  name: string
  contentType: string | null
  sizeBytes: number
  uploadedBy: string | null
}

// ── Deal API ────────────────────────────────────────────────────────────────

export const dealsApi = {
  getAll: (params?: { salesRepId?: string; status?: DealStatus }) => {
    const query = new URLSearchParams()
    if (params?.salesRepId) query.set("salesRepId", params.salesRepId)
    if (params?.status) query.set("status", params.status)
    const qs = query.toString()
    return apiFetch<DealResponse[]>(`/deals${qs ? `?${qs}` : ""}`)
  },
  get: (id: string) => apiFetch<DealResponse>(`/deals/${id}`),
  create: (data: CreateDealRequest) =>
    apiFetch<DealResponse>("/deals", { method: "POST", body: JSON.stringify(data) }),
  update: (id: string, data: UpdateDealRequest) =>
    apiFetch<DealResponse>(`/deals/${id}`, { method: "PUT", body: JSON.stringify(data) }),
  delete: (id: string) => apiFetch<void>(`/deals/${id}`, { method: "DELETE" }),
}

// ── Plans API ───────────────────────────────────────────────────────────────

export const plansApi = {
  getAll: (status?: PlanStatus) => {
    const qs = status ? `?status=${status}` : ""
    return apiFetch<CommissionPlanResponse[]>(`/plans${qs}`)
  },
  get: (id: string) => apiFetch<CommissionPlanResponse>(`/plans/${id}`),
  create: (data: CreateCommissionPlanRequest) =>
    apiFetch<CommissionPlanResponse>("/plans", { method: "POST", body: JSON.stringify(data) }),
  activate: (id: string) =>
    apiFetch<CommissionPlanResponse>(`/plans/${id}/activate`, { method: "POST" }),
  addRule: (id: string, data: AddRuleToPlanRequest) =>
    apiFetch<CommissionPlanResponse>(`/plans/${id}/rules`, { method: "POST", body: JSON.stringify(data) }),
  delete: (id: string) => apiFetch<void>(`/plans/${id}`, { method: "DELETE" }),
}

// ── Calculations API ────────────────────────────────────────────────────────

export const calculationsApi = {
  getAll: (params?: { dealId?: string; salesRepId?: string }) => {
    const query = new URLSearchParams()
    if (params?.dealId) query.set("dealId", params.dealId)
    if (params?.salesRepId) query.set("salesRepId", params.salesRepId)
    const qs = query.toString()
    return apiFetch<CommissionCalculationResponse[]>(`/calculations${qs ? `?${qs}` : ""}`)
  },
  get: (id: string) => apiFetch<CommissionCalculationResponse>(`/calculations/${id}`),
  calculate: (data: CalculateCommissionRequest) =>
    apiFetch<CommissionCalculationResponse>("/calculations", { method: "POST", body: JSON.stringify(data) }),
}

// ── Disputes API ────────────────────────────────────────────────────────────

export const disputesApi = {
  getAll: (params?: { salesRepId?: string; status?: DisputeStatusApi }) => {
    const query = new URLSearchParams()
    if (params?.salesRepId) query.set("salesRepId", params.salesRepId)
    if (params?.status) query.set("status", params.status)
    const qs = query.toString()
    return apiFetch<DisputeResponse[]>(`/disputes${qs ? `?${qs}` : ""}`)
  },
  get: (id: string) => apiFetch<DisputeResponse>(`/disputes/${id}`),
  create: (data: CreateDisputeRequest) =>
    apiFetch<DisputeResponse>("/disputes", { method: "POST", body: JSON.stringify(data) }),
  resolve: (id: string, data: ResolveDisputeRequest) =>
    apiFetch<DisputeResponse>(`/disputes/${id}/resolve`, { method: "POST", body: JSON.stringify(data) }),
  escalate: (id: string) =>
    apiFetch<DisputeResponse>(`/disputes/${id}/escalate`, { method: "POST" }),
  addComment: (id: string, data: AddCommentRequest) =>
    apiFetch<DisputeResponse>(`/disputes/${id}/comments`, { method: "POST", body: JSON.stringify(data) }),
  addDocument: (id: string, data: AddDocumentRequest) =>
    apiFetch<DisputeResponse>(`/disputes/${id}/documents`, { method: "POST", body: JSON.stringify(data) }),
  delete: (id: string) => apiFetch<void>(`/disputes/${id}`, { method: "DELETE" }),
}

// ── Dispute mapping helper ──────────────────────────────────────────────────

export function mapApiDisputeToLocal(d: DisputeResponse) {
  return {
    id: d.id,
    title: d.title,
    description: d.description,
    type: "commission_calculation" as const,
    priority: "medium" as const,
    status: (d.status === "INITIATED" ? "initiated"
      : d.status === "UNDER_REVIEW" ? "under_review"
      : d.status === "ESCALATED" ? "escalated"
      : d.status === "RESOLVED" || d.status === "APPROVED" ? "resolved"
      : d.status === "REJECTED" ? "rejected"
      : "initiated") as "initiated" | "under_review" | "pending_info" | "escalated" | "resolved" | "rejected",
    dealId: d.calculationId,
    commissionId: d.calculationId,
    disputedAmount: 0,
    expectedAmount: 0,
    currency: "USD",
    submittedBy: d.salesRepId,
    submittedByName: d.salesRepId,
    submittedByRole: "sales" as const,
    createdAt: d.createdDate || new Date().toISOString(),
    updatedAt: d.createdDate || new Date().toISOString(),
    resolvedAt: d.resolvedDate || undefined,
    documents: (d.documents || []).map((doc) => ({
      id: doc.id,
      name: doc.name,
      type: doc.contentType || "application/octet-stream",
      url: "",
      uploadedAt: doc.uploadedAt || new Date().toISOString(),
      uploadedBy: doc.uploadedBy || "unknown",
      size: doc.sizeBytes,
    })),
    comments: (d.comments || []).map((c) => ({
      id: c.id,
      disputeId: d.id,
      userId: c.userId || "system",
      userName: c.isSystemComment ? "System" : (c.userName || c.userId || "Unknown"),
      userRole: "sales" as const,
      content: c.text,
      createdAt: c.timestamp || new Date().toISOString(),
      isInternal: c.isSystemComment,
    })),
    statusHistory: [] as any[],
    tags: [] as string[],
    resolution: d.resolution || undefined,
  }
}
