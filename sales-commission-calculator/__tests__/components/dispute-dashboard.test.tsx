import { render, screen, waitFor } from "@testing-library/react"
import { DisputeDashboard } from "@/components/dispute-dashboard"
import { disputesApi } from "@/lib/api"

jest.mock("@/lib/api", () => ({
  disputesApi: {
    getAll: jest.fn(),
    get: jest.fn(),
    create: jest.fn(),
  },
  mapApiDisputeToLocal: jest.fn((d: any) => ({
    id: d.id,
    title: d.title,
    description: d.description,
    type: "commission_calculation",
    priority: "medium",
    status: d.status === "INITIATED" ? "initiated" : d.status === "RESOLVED" ? "resolved" : "initiated",
    dealId: d.calculationId,
    commissionId: d.calculationId,
    disputedAmount: 0,
    expectedAmount: 0,
    currency: "USD",
    submittedBy: d.salesRepId,
    submittedByName: d.salesRepId,
    submittedByRole: "sales",
    createdAt: d.createdDate || new Date().toISOString(),
    updatedAt: d.createdDate || new Date().toISOString(),
    documents: [],
    comments: [],
    statusHistory: [],
    tags: [],
  })),
}))

// Mock child components to isolate DisputeDashboard
jest.mock("@/components/dispute-form", () => ({
  DisputeForm: ({ onCancel }: any) => <div data-testid="dispute-form"><button onClick={onCancel}>Cancel</button></div>,
}))
jest.mock("@/components/dispute-list", () => ({
  DisputeList: ({ onViewDispute }: any) => <div data-testid="dispute-list">Dispute List</div>,
}))
jest.mock("@/components/dispute-detail", () => ({
  DisputeDetail: ({ onBack }: any) => <div data-testid="dispute-detail"><button onClick={onBack}>Back</button></div>,
}))

const mockDisputesApi = disputesApi as jest.Mocked<typeof disputesApi>

beforeEach(() => {
  jest.clearAllMocks()
})

describe("DisputeDashboard", () => {
  const sampleDisputes = [
    { id: "disp-1", calculationId: "c1", salesRepId: "rep-1", title: "Rate Issue", description: "Wrong rate", status: "INITIATED" as const, isEscalated: false, createdDate: "2024-01-20T10:00:00", resolvedDate: null, resolution: null, commentsCount: 0 },
    { id: "disp-2", calculationId: "c2", salesRepId: "rep-2", title: "Resolved One", description: "Fixed", status: "RESOLVED" as const, isEscalated: false, createdDate: "2024-01-15T10:00:00", resolvedDate: "2024-01-18T10:00:00", resolution: "Fixed", commentsCount: 1 },
  ]

  test("renders dispute management title", async () => {
    mockDisputesApi.getAll.mockResolvedValue(sampleDisputes)
    render(<DisputeDashboard userRole="admin" userId="admin-1" userName="Admin" />)
    expect(screen.getByText("Dispute Management")).toBeInTheDocument()
    await waitFor(() => expect(mockDisputesApi.getAll).toHaveBeenCalled())
  })

  test("shows total disputes count from API", async () => {
    mockDisputesApi.getAll.mockResolvedValue(sampleDisputes)
    render(<DisputeDashboard userRole="admin" userId="admin-1" userName="Admin" />)
    await waitFor(() => expect(screen.getByText("2")).toBeInTheDocument())
  })

  test("shows active disputes count", async () => {
    mockDisputesApi.getAll.mockResolvedValue(sampleDisputes)
    render(<DisputeDashboard userRole="admin" userId="admin-1" userName="Admin" />)
    await waitFor(() => {
      // Active disputes card shows count
      expect(screen.getByText("Active Disputes")).toBeInTheDocument()
      expect(screen.getByText("Pending resolution")).toBeInTheDocument()
    })
  })

  test("renders Create Dispute button", async () => {
    mockDisputesApi.getAll.mockResolvedValue([])
    render(<DisputeDashboard userRole="admin" userId="admin-1" userName="Admin" />)
    expect(screen.getByText("Create Dispute")).toBeInTheDocument()
    await waitFor(() => expect(mockDisputesApi.getAll).toHaveBeenCalled())
  })

  test("renders dispute list component", async () => {
    mockDisputesApi.getAll.mockResolvedValue([])
    render(<DisputeDashboard userRole="admin" userId="admin-1" userName="Admin" />)
    expect(screen.getByTestId("dispute-list")).toBeInTheDocument()
    await waitFor(() => expect(mockDisputesApi.getAll).toHaveBeenCalled())
  })

  test("fetches disputes on mount", async () => {
    mockDisputesApi.getAll.mockResolvedValue([])
    render(<DisputeDashboard userRole="sales" userId="rep-1" userName="Sarah" />)
    await waitFor(() => expect(mockDisputesApi.getAll).toHaveBeenCalled())
  })

  test("handles API error gracefully", async () => {
    mockDisputesApi.getAll.mockRejectedValue(new Error("fail"))
    const consoleSpy = jest.spyOn(console, "error").mockImplementation()
    render(<DisputeDashboard userRole="admin" userId="admin-1" userName="Admin" />)
    await waitFor(() => expect(consoleSpy).toHaveBeenCalled())
    consoleSpy.mockRestore()
    // Should still render the page
    expect(screen.getByText("Dispute Management")).toBeInTheDocument()
  })
})
