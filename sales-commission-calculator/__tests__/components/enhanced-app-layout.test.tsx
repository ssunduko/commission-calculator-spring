import { render, screen } from "@testing-library/react"
import { EnhancedAppLayout } from "@/components/enhanced-app-layout"
import { dealsApi, session } from "@/lib/api"

jest.mock("@/lib/api", () => ({
  dealsApi: { getAll: jest.fn() },
  session: {
    save: jest.fn(),
    clear: jest.fn(),
    getToken: jest.fn().mockReturnValue(null),
    getUser: jest.fn().mockReturnValue(null),
  },
}))

// Mock all child dashboard components — the layout itself is just a shell, the
// children are responsible for any API calls. Replacing them keeps these tests
// focused on the shell's navigation, branding, and session integration.
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
const mockSession = session as jest.Mocked<typeof session>

beforeEach(() => {
  jest.clearAllMocks()
  mockDealsApi.getAll.mockResolvedValue([])
  mockSession.getUser.mockReturnValue(null)
  mockSession.getToken.mockReturnValue(null)
})

describe("EnhancedAppLayout", () => {
  test("renders the app shell with logo", () => {
    render(<EnhancedAppLayout />)
    expect(screen.getByText("Commission Hub")).toBeInTheDocument()
  })

  test("renders navigation menu items", () => {
    render(<EnhancedAppLayout />)
    expect(screen.getByText("Dashboard")).toBeInTheDocument()
    expect(screen.getByText("Analytics")).toBeInTheDocument()
    expect(screen.getByText("Plan Builder")).toBeInTheDocument()
    expect(screen.getByText("Disputes")).toBeInTheDocument()
    expect(screen.getByText("Admin Panel")).toBeInTheDocument()
  })

  test("shows sales dashboard by default", () => {
    render(<EnhancedAppLayout />)
    expect(screen.getByTestId("sales-dashboard")).toBeInTheDocument()
  })

  test("shows Guest profile when no session is present", () => {
    render(<EnhancedAppLayout />)
    // Sidebar profile pill renders the display name
    const guests = screen.getAllByText("Guest")
    expect(guests.length).toBeGreaterThan(0)
    expect(screen.getByText("Not signed in")).toBeInTheDocument()
  })

  test("shows the signed-in user from session", () => {
    mockSession.getUser.mockReturnValue({
      userId: "usr-001",
      username: "jsmith",
      email: "john@example.com",
      fullName: "John Smith",
    })

    render(<EnhancedAppLayout />)
    expect(screen.getAllByText("John Smith").length).toBeGreaterThan(0)
    expect(screen.getByText("@jsmith")).toBeInTheDocument()
  })

  test("renders without crashing when child dashboards are mounted", () => {
    render(<EnhancedAppLayout />)
    // The shell is the contract — children are mocked here so we don't assert on their data
    expect(screen.getByTestId("sales-dashboard")).toBeInTheDocument()
  })
})
