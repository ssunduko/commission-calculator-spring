import {
  dealsApi,
  plansApi,
  calculationsApi,
  disputesApi,
  mapApiDisputeToLocal,
  type DealResponse,
  type CommissionPlanResponse,
  type CommissionCalculationResponse,
  type DisputeResponse,
} from "@/lib/api"

// Mock global fetch
const mockFetch = jest.fn()
global.fetch = mockFetch

beforeEach(() => {
  mockFetch.mockReset()
})

function jsonResponse(data: any, status = 200) {
  return Promise.resolve({
    ok: true,
    status,
    json: () => Promise.resolve(data),
    text: () => Promise.resolve(JSON.stringify(data)),
  })
}

function errorResponse(status: number, message: string) {
  return Promise.resolve({
    ok: false,
    status,
    statusText: message,
    text: () => Promise.resolve(message),
  })
}

// ── Deals API ───────────────────────────────────────────────────────────────

describe("dealsApi", () => {
  const sampleDeal: DealResponse = {
    id: "d1",
    title: "Test Deal",
    value: 50000,
    status: "OPEN",
    salesRepId: "rep-1",
    closeDate: null,
    createdDate: "2024-01-01",
  }

  test("getAll fetches /api/deals", async () => {
    mockFetch.mockReturnValueOnce(jsonResponse([sampleDeal]))
    const result = await dealsApi.getAll()
    expect(mockFetch).toHaveBeenCalledWith("/api/deals", expect.objectContaining({ headers: expect.any(Object) }))
    expect(result).toEqual([sampleDeal])
  })

  test("getAll with status filter appends query param", async () => {
    mockFetch.mockReturnValueOnce(jsonResponse([]))
    await dealsApi.getAll({ status: "WON" })
    expect(mockFetch).toHaveBeenCalledWith("/api/deals?status=WON", expect.any(Object))
  })

  test("getAll with salesRepId filter appends query param", async () => {
    mockFetch.mockReturnValueOnce(jsonResponse([]))
    await dealsApi.getAll({ salesRepId: "rep-1" })
    expect(mockFetch).toHaveBeenCalledWith("/api/deals?salesRepId=rep-1", expect.any(Object))
  })

  test("get fetches /api/deals/:id", async () => {
    mockFetch.mockReturnValueOnce(jsonResponse(sampleDeal))
    const result = await dealsApi.get("d1")
    expect(mockFetch).toHaveBeenCalledWith("/api/deals/d1", expect.any(Object))
    expect(result).toEqual(sampleDeal)
  })

  test("create posts to /api/deals", async () => {
    mockFetch.mockReturnValueOnce(jsonResponse(sampleDeal))
    await dealsApi.create({ title: "New", value: 1000, salesRepId: "rep-1" })
    expect(mockFetch).toHaveBeenCalledWith(
      "/api/deals",
      expect.objectContaining({ method: "POST", body: expect.any(String) }),
    )
  })

  test("update puts to /api/deals/:id", async () => {
    mockFetch.mockReturnValueOnce(jsonResponse(sampleDeal))
    await dealsApi.update("d1", { status: "WON" })
    expect(mockFetch).toHaveBeenCalledWith(
      "/api/deals/d1",
      expect.objectContaining({ method: "PUT" }),
    )
  })

  test("delete sends DELETE to /api/deals/:id", async () => {
    mockFetch.mockReturnValueOnce(Promise.resolve({ ok: true, status: 204, json: () => Promise.resolve(undefined), text: () => Promise.resolve("") }))
    await dealsApi.delete("d1")
    expect(mockFetch).toHaveBeenCalledWith(
      "/api/deals/d1",
      expect.objectContaining({ method: "DELETE" }),
    )
  })

  test("throws on error response", async () => {
    mockFetch.mockReturnValueOnce(errorResponse(404, "Not Found"))
    await expect(dealsApi.get("bad-id")).rejects.toThrow("API 404")
  })

  test("includes Authorization header", async () => {
    mockFetch.mockReturnValueOnce(jsonResponse([]))
    await dealsApi.getAll()
    const headers = mockFetch.mock.calls[0][1].headers
    expect(headers.Authorization).toMatch(/^Basic /)
  })
})

// ── Plans API ───────────────────────────────────────────────────────────────

describe("plansApi", () => {
  const samplePlan: CommissionPlanResponse = {
    id: "p1",
    name: "Test Plan",
    currency: "USD",
    status: "ACTIVE",
    effectiveStartDate: "2024-01-01",
    effectiveEndDate: null,
    createdDate: "2024-01-01",
    rulesCount: 2,
    tiersCount: 1,
  }

  test("getAll fetches /api/plans", async () => {
    mockFetch.mockReturnValueOnce(jsonResponse([samplePlan]))
    const result = await plansApi.getAll()
    expect(result).toEqual([samplePlan])
  })

  test("getAll with status filter", async () => {
    mockFetch.mockReturnValueOnce(jsonResponse([]))
    await plansApi.getAll("ACTIVE")
    expect(mockFetch).toHaveBeenCalledWith("/api/plans?status=ACTIVE", expect.any(Object))
  })

  test("create posts to /api/plans", async () => {
    mockFetch.mockReturnValueOnce(jsonResponse(samplePlan))
    await plansApi.create({ name: "New Plan", currencyCode: "USD" })
    expect(mockFetch).toHaveBeenCalledWith("/api/plans", expect.objectContaining({ method: "POST" }))
  })

  test("activate posts to /api/plans/:id/activate", async () => {
    mockFetch.mockReturnValueOnce(jsonResponse(samplePlan))
    await plansApi.activate("p1")
    expect(mockFetch).toHaveBeenCalledWith("/api/plans/p1/activate", expect.objectContaining({ method: "POST" }))
  })

  test("addRule posts to /api/plans/:id/rules", async () => {
    mockFetch.mockReturnValueOnce(jsonResponse(samplePlan))
    await plansApi.addRule("p1", { name: "Rule 1", description: "test", rate: 10, ruleType: "base", priority: 1 })
    expect(mockFetch).toHaveBeenCalledWith("/api/plans/p1/rules", expect.objectContaining({ method: "POST" }))
  })

  test("delete sends DELETE", async () => {
    mockFetch.mockReturnValueOnce(Promise.resolve({ ok: true, status: 204, json: () => Promise.resolve(undefined), text: () => Promise.resolve("") }))
    await plansApi.delete("p1")
    expect(mockFetch).toHaveBeenCalledWith("/api/plans/p1", expect.objectContaining({ method: "DELETE" }))
  })
})

// ── Calculations API ────────────────────────────────────────────────────────

describe("calculationsApi", () => {
  const sampleCalc: CommissionCalculationResponse = {
    id: "c1",
    dealId: "d1",
    salesRepId: "rep-1",
    baseCommission: 5000,
    grossCommission: 5500,
    netCommission: 5000,
    status: "CALCULATED",
    calculationDate: "2024-01-15",
    planId: "p1",
  }

  test("getAll fetches /api/calculations", async () => {
    mockFetch.mockReturnValueOnce(jsonResponse([sampleCalc]))
    const result = await calculationsApi.getAll()
    expect(result).toEqual([sampleCalc])
  })

  test("getAll with dealId param", async () => {
    mockFetch.mockReturnValueOnce(jsonResponse([]))
    await calculationsApi.getAll({ dealId: "d1" })
    expect(mockFetch).toHaveBeenCalledWith("/api/calculations?dealId=d1", expect.any(Object))
  })

  test("calculate posts to /api/calculations", async () => {
    mockFetch.mockReturnValueOnce(jsonResponse(sampleCalc))
    await calculationsApi.calculate({ dealId: "d1", planId: "p1" })
    expect(mockFetch).toHaveBeenCalledWith("/api/calculations", expect.objectContaining({ method: "POST" }))
  })
})

// ── Disputes API ────────────────────────────────────────────────────────────

describe("disputesApi", () => {
  const sampleDispute: DisputeResponse = {
    id: "disp-1",
    calculationId: "c1",
    salesRepId: "rep-1",
    title: "Test Dispute",
    description: "Desc",
    status: "INITIATED",
    isEscalated: false,
    createdDate: "2024-01-20T10:00:00",
    resolvedDate: null,
    resolution: null,
    commentsCount: 0,
  }

  test("getAll fetches /api/disputes", async () => {
    mockFetch.mockReturnValueOnce(jsonResponse([sampleDispute]))
    const result = await disputesApi.getAll()
    expect(result).toEqual([sampleDispute])
  })

  test("create posts to /api/disputes", async () => {
    mockFetch.mockReturnValueOnce(jsonResponse(sampleDispute))
    await disputesApi.create({ calculationId: "c1", salesRepId: "rep-1", title: "T", description: "D" })
    expect(mockFetch).toHaveBeenCalledWith("/api/disputes", expect.objectContaining({ method: "POST" }))
  })

  test("resolve posts to /api/disputes/:id/resolve", async () => {
    mockFetch.mockReturnValueOnce(jsonResponse(sampleDispute))
    await disputesApi.resolve("disp-1", { resolution: "Fixed", resolvedBy: "admin", approved: true })
    expect(mockFetch).toHaveBeenCalledWith("/api/disputes/disp-1/resolve", expect.objectContaining({ method: "POST" }))
  })

  test("escalate posts to /api/disputes/:id/escalate", async () => {
    mockFetch.mockReturnValueOnce(jsonResponse(sampleDispute))
    await disputesApi.escalate("disp-1")
    expect(mockFetch).toHaveBeenCalledWith("/api/disputes/disp-1/escalate", expect.objectContaining({ method: "POST" }))
  })
})

// ── mapApiDisputeToLocal ────────────────────────────────────────────────────

describe("mapApiDisputeToLocal", () => {
  test("maps INITIATED to initiated", () => {
    const result = mapApiDisputeToLocal({
      id: "d1", calculationId: "c1", salesRepId: "rep-1",
      title: "T", description: "D", status: "INITIATED",
      isEscalated: false, createdDate: "2024-01-01T00:00:00",
      resolvedDate: null, resolution: null, commentsCount: 0,
    })
    expect(result.status).toBe("initiated")
    expect(result.id).toBe("d1")
    expect(result.submittedBy).toBe("rep-1")
  })

  test("maps ESCALATED to escalated", () => {
    const result = mapApiDisputeToLocal({
      id: "d2", calculationId: "c1", salesRepId: "rep-1",
      title: "T", description: "D", status: "ESCALATED",
      isEscalated: true, createdDate: "2024-01-01T00:00:00",
      resolvedDate: null, resolution: null, commentsCount: 0,
    })
    expect(result.status).toBe("escalated")
  })

  test("maps RESOLVED to resolved with resolution text", () => {
    const result = mapApiDisputeToLocal({
      id: "d3", calculationId: "c1", salesRepId: "rep-1",
      title: "T", description: "D", status: "RESOLVED",
      isEscalated: false, createdDate: "2024-01-01T00:00:00",
      resolvedDate: "2024-01-05T00:00:00", resolution: "Fixed it", commentsCount: 2,
    })
    expect(result.status).toBe("resolved")
    expect(result.resolution).toBe("Fixed it")
    expect(result.resolvedAt).toBe("2024-01-05T00:00:00")
  })

  test("maps REJECTED to rejected", () => {
    const result = mapApiDisputeToLocal({
      id: "d4", calculationId: "c1", salesRepId: "rep-1",
      title: "T", description: "D", status: "REJECTED",
      isEscalated: false, createdDate: null,
      resolvedDate: null, resolution: null, commentsCount: 0,
    })
    expect(result.status).toBe("rejected")
    // Should use current time when createdDate is null
    expect(result.createdAt).toBeTruthy()
  })
})
