import { render, screen, waitFor } from "@testing-library/react"
import { AdminDashboard } from "@/components/admin-dashboard"
import { dealsApi, plansApi, calculationsApi } from "@/lib/api"

jest.mock("@/lib/api", () => ({
  dealsApi: { getAll: jest.fn(), update: jest.fn() },
  plansApi: { getAll: jest.fn() },
  calculationsApi: { getAll: jest.fn() },
}))

const mockDealsApi = dealsApi as jest.Mocked<typeof dealsApi>
const mockPlansApi = plansApi as jest.Mocked<typeof plansApi>
const mockCalcsApi = calculationsApi as jest.Mocked<typeof calculationsApi>

beforeEach(() => {
  jest.clearAllMocks()
})

describe("AdminDashboard", () => {
  const sampleDeals = [
    { id: "d1", title: "Enterprise License", value: 125000, status: "WON" as const, salesRepId: "rep-1", closeDate: "2024-01-15", createdDate: "2024-01-01" },
    { id: "d2", title: "Small Deal", value: 5000, status: "OPEN" as const, salesRepId: "rep-2", closeDate: null, createdDate: "2024-02-01" },
  ]

  const samplePlans = [
    { id: "p1", name: "Enterprise Plan", currency: "USD", status: "ACTIVE" as const, effectiveStartDate: "2024-01-01", effectiveEndDate: null, createdDate: "2024-01-01", rulesCount: 3, tiersCount: 2 },
    { id: "p2", name: "Draft Plan", currency: "USD", status: "DRAFT" as const, effectiveStartDate: null, effectiveEndDate: null, createdDate: "2024-02-01", rulesCount: 0, tiersCount: 0 },
  ]

  const sampleCalcs = [
    { id: "c1", dealId: "d1", salesRepId: "rep-1", baseCommission: 12500, grossCommission: 13000, netCommission: 12500, status: "CALCULATED", calculationDate: "2024-01-15", planId: "p1" },
  ]

  function setupMocks() {
    mockDealsApi.getAll.mockResolvedValue(sampleDeals)
    mockPlansApi.getAll.mockResolvedValue(samplePlans)
    mockCalcsApi.getAll.mockResolvedValue(sampleCalcs)
  }

  test("renders admin dashboard title", async () => {
    setupMocks()
    render(<AdminDashboard />)
    expect(screen.getByText("Admin Dashboard")).toBeInTheDocument()
    await waitFor(() => expect(mockDealsApi.getAll).toHaveBeenCalled())
  })

  test("loads and displays plans from API", async () => {
    setupMocks()
    render(<AdminDashboard />)
    await waitFor(() => expect(screen.getByText("Enterprise Plan")).toBeInTheDocument())
    expect(screen.getByText("Draft Plan")).toBeInTheDocument()
  })

  test("shows active plan count dynamically", async () => {
    setupMocks()
    render(<AdminDashboard />)
    await waitFor(() => expect(screen.getByText("Enterprise Plan")).toBeInTheDocument())
    expect(screen.getByText("Active Plans")).toBeInTheDocument()
  })

  test("loads deals from API", async () => {
    setupMocks()
    render(<AdminDashboard />)
    await waitFor(() => {
      expect(mockDealsApi.getAll).toHaveBeenCalled()
      expect(mockPlansApi.getAll).toHaveBeenCalled()
      expect(mockCalcsApi.getAll).toHaveBeenCalled()
    })
  })

  test("shows deal count in stats", async () => {
    setupMocks()
    render(<AdminDashboard />)
    await waitFor(() => expect(screen.getByText("2")).toBeInTheDocument()) // 2 deals
  })

  test("handles API error gracefully", async () => {
    mockDealsApi.getAll.mockRejectedValue(new Error("fail"))
    mockPlansApi.getAll.mockRejectedValue(new Error("fail"))
    mockCalcsApi.getAll.mockRejectedValue(new Error("fail"))
    const consoleSpy = jest.spyOn(console, "error").mockImplementation()
    render(<AdminDashboard />)
    await waitFor(() => expect(consoleSpy).toHaveBeenCalled())
    consoleSpy.mockRestore()
  })
})
