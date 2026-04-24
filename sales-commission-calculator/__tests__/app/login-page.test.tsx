import { render, screen, waitFor } from "@testing-library/react"
import userEvent from "@testing-library/user-event"
import LoginPage from "@/app/login/page"
import { authApi, session } from "@/lib/api"

jest.mock("@/lib/api", () => ({
  authApi: { login: jest.fn(), register: jest.fn() },
  session: { save: jest.fn(), clear: jest.fn(), getToken: jest.fn(), getUser: jest.fn() },
  subscriptionPackagesApi: { list: jest.fn() },
}))

const mockAuthApi = authApi as jest.Mocked<typeof authApi>
const mockSession = session as jest.Mocked<typeof session>

beforeEach(() => {
  jest.clearAllMocks()
})

describe("LoginPage", () => {
  test("renders login form fields", () => {
    render(<LoginPage />)
    expect(screen.getByText(/sign in to commission hub/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/username/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument()
    expect(screen.getByRole("button", { name: /sign in/i })).toBeInTheDocument()
  })

  test("does not call API when fields are blank (HTML5 required blocks submit)", () => {
    render(<LoginPage />)
    const usernameInput = screen.getByLabelText(/username/i) as HTMLInputElement
    const passwordInput = screen.getByLabelText(/password/i) as HTMLInputElement
    expect(usernameInput).toBeRequired()
    expect(passwordInput).toBeRequired()
    expect(mockAuthApi.login).not.toHaveBeenCalled()
  })

  test("submits credentials and stores session on success", async () => {
    const user = userEvent.setup()
    mockAuthApi.login.mockResolvedValue({
      token: "jwt-xyz",
      tokenType: "Bearer",
      expiresInSeconds: 3600,
      userId: "usr-001",
      username: "jsmith",
      email: "john@example.com",
      fullName: "John Smith",
    })

    render(<LoginPage />)
    await user.type(screen.getByLabelText(/username/i), "jsmith")
    await user.type(screen.getByLabelText(/password/i), "sales123")
    await user.click(screen.getByRole("button", { name: /sign in/i }))

    await waitFor(() => {
      expect(mockAuthApi.login).toHaveBeenCalledWith({ username: "jsmith", password: "sales123" })
    })
    expect(mockSession.save).toHaveBeenCalledWith("jwt-xyz", {
      userId: "usr-001",
      username: "jsmith",
      email: "john@example.com",
      fullName: "John Smith",
    })
  })

  test("surfaces 401 error to the user", async () => {
    const user = userEvent.setup()
    mockAuthApi.login.mockRejectedValue(new Error("API 401: unauthorized"))

    render(<LoginPage />)
    await user.type(screen.getByLabelText(/username/i), "jsmith")
    await user.type(screen.getByLabelText(/password/i), "wrong")
    await user.click(screen.getByRole("button", { name: /sign in/i }))

    expect(await screen.findByText(/invalid username or password/i)).toBeInTheDocument()
    expect(mockSession.save).not.toHaveBeenCalled()
  })

  test("declares the WebMCP signInToCommissionHub tool on the page", () => {
    render(<LoginPage />)
    const wrapper = document.querySelector("[data-webmcp-tool='signInToCommissionHub']")
    expect(wrapper).not.toBeNull()
    expect(wrapper?.getAttribute("data-webmcp-endpoint")).toBe("/auth/login")
    expect(wrapper?.getAttribute("data-webmcp-method")).toBe("POST")
  })
})
