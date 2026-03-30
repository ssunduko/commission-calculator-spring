import { renderHook, waitFor, act } from "@testing-library/react"
import { useDeals, usePlans, useCalculations, useDisputes } from "@/hooks/use-api"
import { dealsApi, plansApi, calculationsApi, disputesApi } from "@/lib/api"

// Mock the API module
jest.mock("@/lib/api", () => ({
  dealsApi: { getAll: jest.fn(), get: jest.fn() },
  plansApi: { getAll: jest.fn(), get: jest.fn() },
  calculationsApi: { getAll: jest.fn() },
  disputesApi: { getAll: jest.fn(), get: jest.fn() },
}))

const mockDealsApi = dealsApi as jest.Mocked<typeof dealsApi>
const mockPlansApi = plansApi as jest.Mocked<typeof plansApi>
const mockCalcsApi = calculationsApi as jest.Mocked<typeof calculationsApi>
const mockDisputesApi = disputesApi as jest.Mocked<typeof disputesApi>

beforeEach(() => {
  jest.clearAllMocks()
})

describe("useDeals", () => {
  test("returns loading state initially", () => {
    mockDealsApi.getAll.mockReturnValue(new Promise(() => {})) // never resolves
    const { result } = renderHook(() => useDeals())
    expect(result.current.loading).toBe(true)
    expect(result.current.data).toBeNull()
    expect(result.current.error).toBeNull()
  })

  test("returns data on success", async () => {
    const deals = [{ id: "d1", title: "Deal 1", value: 1000, status: "OPEN" as const, salesRepId: "r1", closeDate: null, createdDate: "2024-01-01" }]
    mockDealsApi.getAll.mockResolvedValue(deals)

    const { result } = renderHook(() => useDeals())
    await waitFor(() => expect(result.current.loading).toBe(false))
    expect(result.current.data).toEqual(deals)
    expect(result.current.error).toBeNull()
  })

  test("returns error on failure", async () => {
    mockDealsApi.getAll.mockRejectedValue(new Error("Network error"))

    const { result } = renderHook(() => useDeals())
    await waitFor(() => expect(result.current.loading).toBe(false))
    expect(result.current.data).toBeNull()
    expect(result.current.error).toBe("Network error")
  })

  test("passes filter params to API", async () => {
    mockDealsApi.getAll.mockResolvedValue([])
    renderHook(() => useDeals({ status: "WON" }))
    await waitFor(() => expect(mockDealsApi.getAll).toHaveBeenCalledWith({ status: "WON" }))
  })

  test("refetch reloads data", async () => {
    mockDealsApi.getAll.mockResolvedValue([])
    const { result } = renderHook(() => useDeals())
    await waitFor(() => expect(result.current.loading).toBe(false))

    mockDealsApi.getAll.mockResolvedValue([{ id: "d2", title: "Deal 2", value: 2000, status: "WON" as const, salesRepId: "r1", closeDate: null, createdDate: null }])
    await act(async () => {
      result.current.refetch()
    })
    await waitFor(() => expect(result.current.data).toHaveLength(1))
  })
})

describe("usePlans", () => {
  test("fetches all plans", async () => {
    const plans = [{ id: "p1", name: "Plan", currency: "USD", status: "ACTIVE" as const, effectiveStartDate: null, effectiveEndDate: null, createdDate: null, rulesCount: 0, tiersCount: 0 }]
    mockPlansApi.getAll.mockResolvedValue(plans)

    const { result } = renderHook(() => usePlans())
    await waitFor(() => expect(result.current.loading).toBe(false))
    expect(result.current.data).toEqual(plans)
  })

  test("passes status filter", async () => {
    mockPlansApi.getAll.mockResolvedValue([])
    renderHook(() => usePlans("DRAFT"))
    await waitFor(() => expect(mockPlansApi.getAll).toHaveBeenCalledWith("DRAFT"))
  })
})

describe("useCalculations", () => {
  test("fetches calculations", async () => {
    const calcs = [{ id: "c1", dealId: "d1", salesRepId: "r1", baseCommission: 100, grossCommission: 110, netCommission: 100, status: "CALCULATED", calculationDate: null, planId: "p1" }]
    mockCalcsApi.getAll.mockResolvedValue(calcs)

    const { result } = renderHook(() => useCalculations())
    await waitFor(() => expect(result.current.loading).toBe(false))
    expect(result.current.data).toEqual(calcs)
  })
})

describe("useDisputes", () => {
  test("fetches disputes", async () => {
    const disputes = [{ id: "disp-1", calculationId: "c1", salesRepId: "r1", title: "D", description: "D", status: "INITIATED" as const, isEscalated: false, createdDate: null, resolvedDate: null, resolution: null, commentsCount: 0 }]
    mockDisputesApi.getAll.mockResolvedValue(disputes)

    const { result } = renderHook(() => useDisputes())
    await waitFor(() => expect(result.current.loading).toBe(false))
    expect(result.current.data).toEqual(disputes)
  })
})
