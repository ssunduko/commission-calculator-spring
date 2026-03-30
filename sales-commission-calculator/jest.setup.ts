import "@testing-library/jest-dom"

// Suppress known jsdom SVG warnings from recharts/lucide rendering
const originalConsoleError = console.error
console.error = (...args: any[]) => {
  const msg = typeof args[0] === "string" ? args[0] : ""
  if (
    msg.includes("is unrecognized in this browser") ||
    msg.includes("is using incorrect casing")
  ) {
    return
  }
  originalConsoleError(...args)
}

// Mock next/navigation
jest.mock("next/navigation", () => ({
  useRouter: () => ({ push: jest.fn(), replace: jest.fn(), back: jest.fn() }),
  usePathname: () => "/",
  useSearchParams: () => new URLSearchParams(),
}))

// Mock next/image
jest.mock("next/image", () => ({
  __esModule: true,
  default: Object.assign(
    function MockImage(props: any) {
      return props
    },
    { displayName: "MockImage" },
  ),
}))

// Mock ResizeObserver
global.ResizeObserver = class {
  observe() {}
  unobserve() {}
  disconnect() {}
}

// Mock window.matchMedia
Object.defineProperty(window, "matchMedia", {
  writable: true,
  value: jest.fn().mockImplementation((query) => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: jest.fn(),
    removeListener: jest.fn(),
    addEventListener: jest.fn(),
    removeEventListener: jest.fn(),
    dispatchEvent: jest.fn(),
  })),
})
