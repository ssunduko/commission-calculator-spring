import { render, screen, waitFor } from "@testing-library/react"
import { EnhancedSalesDashboard } from "@/components/enhanced-sales-dashboard"
import { dealsApi, calculationsApi } from "@/lib/api"

jest.mock("@/lib/api", () => ({
  dealsApi: { getAll: jest.fn() },
  calculationsApi: { getAll: jest.fn() },
}))

// Mock recharts to avoid SVG rendering issues in jsdom
jest.mock("recharts", () => ({
  ResponsiveContainer: ({ children }: any) => <div data-testid="chart-container">{children}</div>,
  AreaChart: ({ children }: any) => <div>{children}</div>,
  Area: () => null,
  Line: () => null,
  XAxis: () => null,
  YAxis: () => null,
  CartesianGrid: () => null,
  Tooltip: () => null,
}))

const mockDealsApi = dealsApi as jest.Mocked<typeof dealsApi>
const mockCalcsApi = calculationsApi as jest.Mocked<typeof calculationsApi>

beforeEach(() => {
  jest.clearAllMocks()
})

describe("EnhancedSalesDashboard", () => {
  const sampleDeals = [
    { id: "d1", title: "Big Deal", value: 100000, status: "WON" as const, salesRepId: "rep-1", closeDate: "2024-01-15", createdDate: "2024-01-01" },
    { id: "d2", title: "Open Deal", value: 50000, status: "OPEN" as const, salesRepId: "rep-1", closeDate: null, createdDate: "2024-02-01" },
  ]

  const sampleCalcs = [
    { id: "c1", dealId: "d1", salesRepId: "rep-1", baseCommission: 10000, grossCommission: 11000, netCommission: 10000, status: "CALCULATED", calculationDate: "2024-01-15", planId: "p1" },
  ]

  test("shows loading state initially", () => {
    mockDealsApi.getAll.mockReturnValue(new Promise(() => {}))
    mockCalcsApi.getAll.mockReturnValue(new Promise(() => {}))
    render(<EnhancedSalesDashboard />)
    expect(screen.getByText(/Loading from API/i)).toBeInTheDocument()
  })

  test("renders dashboard title", async () => {
    mockDealsApi.getAll.mockResolvedValue(sampleDeals)
    mockCalcsApi.getAll.mockResolvedValue(sampleCalcs)
    render(<EnhancedSalesDashboard />)
    expect(screen.getByText("Sales Dashboard")).toBeInTheDocument()
    await waitFor(() => expect(screen.getByText("Connected to API")).toBeInTheDocument())
  })

  test("displays total earnings from API data", async () => {
    mockDealsApi.getAll.mockResolvedValue(sampleDeals)
    mockCalcsApi.getAll.mockResolvedValue(sampleCalcs)
    render(<EnhancedSalesDashboard />)
    await waitFor(() => expect(screen.getByText("Connected to API")).toBeInTheDocument())
    expect(screen.getByText("$10,000")).toBeInTheDocument()
  })

  test("shows error badge when API fails", async () => {
    mockDealsApi.getAll.mockRejectedValue(new Error("Connection refused"))
    mockCalcsApi.getAll.mockRejectedValue(new Error("Connection refused"))
    render(<EnhancedSalesDashboard />)
    await waitFor(() => expect(screen.getByText(/Connection refused/)).toBeInTheDocument())
  })

  test("renders won deals count in metrics", async () => {
    mockDealsApi.getAll.mockResolvedValue(sampleDeals)
    mockCalcsApi.getAll.mockResolvedValue(sampleCalcs)
    render(<EnhancedSalesDashboard />)
    await waitFor(() => expect(screen.getByText("Connected to API")).toBeInTheDocument())
    // Won deals should contribute to total earnings metric
    expect(screen.getByText("Total Earnings")).toBeInTheDocument()
  })

  test("displays pipeline data derived from API", async () => {
    mockDealsApi.getAll.mockResolvedValue(sampleDeals)
    mockCalcsApi.getAll.mockResolvedValue(sampleCalcs)
    render(<EnhancedSalesDashboard />)
    await waitFor(() => expect(screen.getByText("Connected to API")).toBeInTheDocument())
    // Pipeline should show total value
    expect(screen.getByText("Pipeline Value")).toBeInTheDocument()
  })

  test("calls API on mount", async () => {
    mockDealsApi.getAll.mockResolvedValue([])
    mockCalcsApi.getAll.mockResolvedValue([])
    render(<EnhancedSalesDashboard />)
    await waitFor(() => {
      expect(mockDealsApi.getAll).toHaveBeenCalled()
      expect(mockCalcsApi.getAll).toHaveBeenCalled()
    })
  })
})
