import { render, screen, waitFor } from "@testing-library/react"
import userEvent from "@testing-library/user-event"
import RegisterPage from "@/app/register/page"
import { authApi, session, subscriptionPackagesApi } from "@/lib/api"

jest.mock("@/lib/api", () => ({
  authApi: { login: jest.fn(), register: jest.fn() },
  session: { save: jest.fn(), clear: jest.fn(), getToken: jest.fn(), getUser: jest.fn() },
  subscriptionPackagesApi: { list: jest.fn(), get: jest.fn() },
}))

const mockAuthApi = authApi as jest.Mocked<typeof authApi>
const mockSession = session as jest.Mocked<typeof session>
const mockPackagesApi = subscriptionPackagesApi as jest.Mocked<typeof subscriptionPackagesApi>

const samplePackages = [
  {
    id: "pkg-basic",
    code: "BASIC",
    name: "Starter",
    description: "Solo plan",
    monthlyPrice: 19,
    maxUsers: 1,
    maxDealsPerMonth: 50,
    tier: "BASIC" as const,
    active: true,
  },
  {
    id: "pkg-pro",
    code: "PROFESSIONAL",
    name: "Pro",
    description: "Team plan",
    monthlyPrice: 79,
    maxUsers: 10,
    maxDealsPerMonth: 500,
    tier: "PROFESSIONAL" as const,
    active: true,
  },
]

beforeEach(() => {
  jest.clearAllMocks()
  mockPackagesApi.list.mockResolvedValue(samplePackages)
})

async function fillStep1(user: ReturnType<typeof userEvent.setup>) {
  await user.type(screen.getByLabelText(/first name/i), "Jane")
  await user.type(screen.getByLabelText(/last name/i), "Doe")
  await user.type(screen.getByLabelText(/^username$/i), "janedoe")
  await user.type(screen.getByLabelText(/^email$/i), "jane@example.com")
  await user.type(screen.getByLabelText(/^password$/i), "Sup3rSecret!")
  await user.type(screen.getByLabelText(/confirm password/i), "Sup3rSecret!")
  await user.click(screen.getByRole("button", { name: /next: choose a package/i }))
}

describe("RegisterPage", () => {
  test("renders step 1 and loads packages", async () => {
    render(<RegisterPage />)
    expect(screen.getByText(/create your commission hub account/i)).toBeInTheDocument()
    expect(screen.getByText(/account details/i)).toBeInTheDocument()
    await waitFor(() => expect(mockPackagesApi.list).toHaveBeenCalled())
  })

  test("validates required fields on step 1", async () => {
    const user = userEvent.setup()
    render(<RegisterPage />)
    await user.click(screen.getByRole("button", { name: /next: choose a package/i }))

    expect(await screen.findByText(/first name is required/i)).toBeInTheDocument()
    expect(screen.getByText(/last name is required/i)).toBeInTheDocument()
    expect(screen.getByText(/username is required/i)).toBeInTheDocument()
    expect(screen.getByText(/email is required/i)).toBeInTheDocument()
    expect(screen.getByText(/password is required/i)).toBeInTheDocument()
  })

  test("walks through full registration and submits to API", async () => {
    const user = userEvent.setup()
    mockAuthApi.register.mockResolvedValue({
      userId: "usr-new",
      username: "janedoe",
      email: "jane@example.com",
      fullName: "Jane Doe",
      subscriptionId: "sub-new",
      packageCode: "PROFESSIONAL",
      packageName: "Pro",
      subscriptionStatus: "ACTIVE",
      paymentId: "pay-new",
      paymentStatus: "COMPLETED",
      amountCharged: 79,
      cardLastFour: "4242",
      token: "jwt-token",
      expiresInSeconds: 7200,
    })

    render(<RegisterPage />)
    // Let the packages list promise resolve and state update
    await waitFor(() => expect(mockPackagesApi.list).toHaveBeenCalled())
    await waitFor(() => expect(screen.getByText(/account details/i)).toBeInTheDocument())

    await fillStep1(user)
    // Step 2 visible — Pro card present means packages loaded
    await screen.findByText(/Team plan/i)
    await user.click(await screen.findByRole("button", { name: /next: payment/i }))

    // Step 3 — payment
    await user.type(await screen.findByLabelText(/cardholder name/i), "Jane Doe")
    await user.type(screen.getByLabelText(/card number/i), "4242424242424242")
    await user.clear(screen.getByLabelText(/cvv/i))
    await user.type(screen.getByLabelText(/cvv/i), "123")

    const submit = await screen.findByRole("button", { name: /create account/i })
    await user.click(submit)

    await waitFor(() => expect(mockAuthApi.register).toHaveBeenCalled(), { timeout: 8000 })
    const call = mockAuthApi.register.mock.calls[0][0]
    expect(call.username).toBe("janedoe")
    expect(call.packageCode).toBe("PROFESSIONAL")
    expect(call.payment.cardNumber).toBe("4242424242424242")
    expect(call.payment.cardHolderName).toBe("Jane Doe")
    expect(call.payment.cvv).toBe("123")

    await waitFor(() => expect(mockSession.save).toHaveBeenCalled())
    expect(mockSession.save).toHaveBeenCalledWith("jwt-token", {
      userId: "usr-new",
      username: "janedoe",
      email: "jane@example.com",
      fullName: "Jane Doe",
    })
  }, 30000)

  test("declares the WebMCP registerForCommissionHub tool on the page", async () => {
    render(<RegisterPage />)
    await waitFor(() => expect(mockPackagesApi.list).toHaveBeenCalled())

    const wrapper = document.querySelector("[data-webmcp-tool='registerForCommissionHub']")
    expect(wrapper).not.toBeNull()
    expect(wrapper?.getAttribute("data-webmcp-endpoint")).toBe("/register")
    expect(wrapper?.getAttribute("data-webmcp-method")).toBe("POST")
  })
})
