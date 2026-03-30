import { render, screen, waitFor } from "@testing-library/react"
import userEvent from "@testing-library/user-event"
import { EnhancedAppLayout } from "@/components/enhanced-app-layout"
import { dealsApi } from "@/lib/api"

jest.mock("@/lib/api", () => ({
  dealsApi: { getAll: jest.fn() },
}))

// Mock all child dashboard components
jest.mock("@/components/enhanced-sales-dashboard", () => ({
  EnhancedSalesDashboard: () => <div data-testid="sales-dashboard">Sales Dashboard View</div>,
}))
jest.mock("@/components/admin-dashboard", () => ({
  AdminDashboard: () => <div data-testid="admin-dashboard">Admin Dashboard View</div>,
}))
jest.mock("@/components/plan-builder", () => ({
  PlanBuilder: () => <div data-testid="plan-builder">Plan Builder View</div>,
}))
jest.mock("@/components/analytics-dashboard", () => ({
  AnalyticsDashboard: () => <div data-testid="analytics-dashboard">Analytics View</div>,
}))
jest.mock("@/components/design-system", () => ({
  DesignSystem: () => <div data-testid="design-system">Design System View</div>,
}))
jest.mock("@/components/dispute-dashboard", () => ({
  DisputeDashboard: () => <div data-testid="dispute-dashboard">Disputes View</div>,
}))

const mockDealsApi = dealsApi as jest.Mocked<typeof dealsApi>

beforeEach(() => {
  jest.clearAllMocks()
  mockDealsApi.getAll.mockResolvedValue([])
})

describe("EnhancedAppLayout", () => {
  test("renders the app shell with logo", async () => {
    render(<EnhancedAppLayout />)
    expect(screen.getByText("Commission Hub")).toBeInTheDocument()
    await waitFor(() => expect(mockDealsApi.getAll).toHaveBeenCalled())
  })

  test("renders navigation menu items", async () => {
    render(<EnhancedAppLayout />)
    await waitFor(() => expect(mockDealsApi.getAll).toHaveBeenCalled())
    expect(screen.getByText("Dashboard")).toBeInTheDocument()
    expect(screen.getByText("Analytics")).toBeInTheDocument()
    expect(screen.getByText("Plan Builder")).toBeInTheDocument()
    expect(screen.getByText("Disputes")).toBeInTheDocument()
    expect(screen.getByText("Admin Panel")).toBeInTheDocument()
  })

  test("shows sales dashboard by default", async () => {
    render(<EnhancedAppLayout />)
    await waitFor(() => expect(mockDealsApi.getAll).toHaveBeenCalled())
    expect(screen.getByTestId("sales-dashboard")).toBeInTheDocument()
  })

  test("fetches recent activity from API on mount", async () => {
    const deals = [
      { id: "d1", title: "Recent Deal", value: 50000, status: "WON" as const, salesRepId: "rep-1", closeDate: "2024-06-01", createdDate: "2024-05-01" },
    ]
    mockDealsApi.getAll.mockResolvedValue(deals)
    render(<EnhancedAppLayout />)
    await waitFor(() => expect(mockDealsApi.getAll).toHaveBeenCalled())
  })

  test("displays recent activity from API deals", async () => {
    const deals = [
      { id: "d1", title: "Enterprise License", value: 125000, status: "WON" as const, salesRepId: "rep-1", closeDate: "2024-06-01", createdDate: "2024-05-01" },
    ]
    mockDealsApi.getAll.mockResolvedValue(deals)
    render(<EnhancedAppLayout />)
    await waitFor(() => expect(screen.getByText("Enterprise License")).toBeInTheDocument())
  })

  test("renders user profile section", async () => {
    render(<EnhancedAppLayout />)
    await waitFor(() => expect(mockDealsApi.getAll).toHaveBeenCalled())
    expect(screen.getByText("Sarah Johnson")).toBeInTheDocument()
  })

  test("handles API error for recent activity gracefully", async () => {
    mockDealsApi.getAll.mockRejectedValue(new Error("fail"))
    render(<EnhancedAppLayout />)
    // Should still render the layout without crashing
    await waitFor(() => expect(screen.getByText("Commission Hub")).toBeInTheDocument())
  })
})
