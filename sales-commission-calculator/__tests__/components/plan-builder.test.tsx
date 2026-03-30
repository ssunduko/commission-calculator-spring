import { render, screen, fireEvent, waitFor } from "@testing-library/react"
import { PlanBuilder } from "@/components/plan-builder"
import { plansApi } from "@/lib/api"

jest.mock("@/lib/api", () => ({
  plansApi: {
    create: jest.fn(),
    addRule: jest.fn(),
  },
}))

const mockPlansApi = plansApi as jest.Mocked<typeof plansApi>

beforeEach(() => {
  jest.clearAllMocks()
})

describe("PlanBuilder", () => {
  test("renders plan builder title", () => {
    render(<PlanBuilder />)
    expect(screen.getByText("Advanced Plan Builder")).toBeInTheDocument()
  })

  test("renders save plan button", () => {
    render(<PlanBuilder />)
    expect(screen.getByText("Save Plan")).toBeInTheDocument()
  })

  test("renders preview mode toggle", () => {
    render(<PlanBuilder />)
    expect(screen.getByText("Preview Mode")).toBeInTheDocument()
  })

  test("saves plan via API when Save Plan is clicked", async () => {
    mockPlansApi.create.mockResolvedValue({
      id: "new-plan-1",
      name: "New Commission Plan",
      currency: "USD",
      status: "DRAFT",
      effectiveStartDate: null,
      effectiveEndDate: null,
      createdDate: "2024-01-01",
      rulesCount: 0,
      tiersCount: 0,
    })
    mockPlansApi.addRule.mockResolvedValue({
      id: "new-plan-1",
      name: "New Commission Plan",
      currency: "USD",
      status: "DRAFT",
      effectiveStartDate: null,
      effectiveEndDate: null,
      createdDate: "2024-01-01",
      rulesCount: 1,
      tiersCount: 0,
    })

    render(<PlanBuilder />)
    fireEvent.click(screen.getByText("Save Plan"))

    await waitFor(() => {
      expect(mockPlansApi.create).toHaveBeenCalledWith(
        expect.objectContaining({ name: "New Commission Plan", currencyCode: "USD" }),
      )
    })
  })

  test("shows success message after saving", async () => {
    mockPlansApi.create.mockResolvedValue({
      id: "p1", name: "My Plan", currency: "USD", status: "DRAFT",
      effectiveStartDate: null, effectiveEndDate: null, createdDate: "2024-01-01",
      rulesCount: 0, tiersCount: 0,
    })
    mockPlansApi.addRule.mockResolvedValue({
      id: "p1", name: "My Plan", currency: "USD", status: "DRAFT",
      effectiveStartDate: null, effectiveEndDate: null, createdDate: "2024-01-01",
      rulesCount: 1, tiersCount: 0,
    })

    render(<PlanBuilder />)
    fireEvent.click(screen.getByText("Save Plan"))

    await waitFor(() => {
      expect(screen.getByText(/saved successfully/i)).toBeInTheDocument()
    })
  })

  test("shows error message on API failure", async () => {
    mockPlansApi.create.mockRejectedValue(new Error("Server error"))

    render(<PlanBuilder />)
    fireEvent.click(screen.getByText("Save Plan"))

    await waitFor(() => {
      expect(screen.getByText(/Error saving plan/i)).toBeInTheDocument()
    })
  })

  test("renders basic settings tab", () => {
    render(<PlanBuilder />)
    expect(screen.getByText("Basic")).toBeInTheDocument()
    expect(screen.getByText("Tiers")).toBeInTheDocument()
    expect(screen.getByText("Accelerators")).toBeInTheDocument()
    expect(screen.getByText("Bonuses")).toBeInTheDocument()
  })
})
