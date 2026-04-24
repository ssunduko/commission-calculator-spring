"use client"

import { useEffect, useRef } from "react"

/**
 * Declarative WebMCP hook — React equivalent of the data-webmcp-* HTML pattern.
 *
 * Registers an MCP tool with `navigator.modelContext` (Chrome 146+ WebMCP spec)
 * when the component mounts, and unregisters it on unmount.
 *
 * The hook also scans a container ref for `input[name]`, `textarea[name]`,
 * and `select[name]` elements to auto-build the tool's inputSchema,
 * mirroring how the Thymeleaf templates derive parameters from form fields.
 */

export interface WebMcpToolConfig {
  /** MCP tool name (e.g. "createDispute") */
  tool: string
  /** Human-readable description of what the tool does */
  description: string
  /** API endpoint — path params use {paramName} syntax */
  endpoint: string
  /** HTTP method (default GET) */
  method?: "GET" | "POST" | "PUT" | "DELETE"
  /** Key to extract from the JSON response (optional — returns full JSON if omitted) */
  responseKey?: string
  /**
   * Explicit parameter definitions. When provided, these are used instead of
   * scanning the DOM for form inputs. Useful for React components that use
   * controlled inputs without native `name` attributes.
   */
  params?: WebMcpParam[]
}

export interface WebMcpParam {
  name: string
  type?: string
  description?: string
  required?: boolean
}

const SESSION_TOKEN_KEY = "ccalc.session.token"

function bearerHeader(): string | null {
  if (typeof window === "undefined") return null
  const token = window.localStorage?.getItem(SESSION_TOKEN_KEY)
  return token ? `Bearer ${token}` : null
}

async function mcpFetch(
  origin: string,
  endpoint: string,
  method: string,
  responseKey: string | undefined,
  args: Record<string, string>
) {
  let url = endpoint
  const pathParams = endpoint.match(/\{(\w+)\}/g) || []
  const pathKeys = new Set(pathParams.map((p) => p.replace(/[{}]/g, "")))
  pathParams.forEach((p) => {
    const k = p.replace(/[{}]/g, "")
    url = url.replace(p, encodeURIComponent(args[k] || ""))
  })

  const headers: Record<string, string> = {
    "ngrok-skip-browser-warning": "true",
  }
  const auth = bearerHeader()
  if (auth) {
    headers.Authorization = auth
  }
  const opts: RequestInit = { method, headers, mode: "cors", credentials: "omit" }

  if (method === "POST" || method === "PUT") {
    headers["Content-Type"] = "application/json"
    const body = { ...args }
    pathKeys.forEach((k) => delete body[k])
    opts.body = JSON.stringify(body)
  } else {
    const qs = new URLSearchParams()
    Object.entries(args).forEach(([k, v]) => {
      if (!pathKeys.has(k) && v !== undefined && v !== null && v !== "") qs.set(k, String(v))
    })
    const s = qs.toString()
    if (s) url += (url.includes("?") ? "&" : "?") + s
  }

  const fullUrl = `${origin}/api${url}`
  console.log(`[WebMCP] fetch ${method} ${fullUrl}`)
  try {
    const res = await fetch(fullUrl, opts)
    const data = await res.json()
    const text = responseKey
      ? data[responseKey] || data.error || JSON.stringify(data)
      : typeof data === "string"
        ? data
        : JSON.stringify(data, null, 2)
    return { content: [{ type: "text", text }] }
  } catch (e: any) {
    const msg = `${e?.name || "Error"}: ${e?.message || e} (url=${fullUrl})`
    console.error("[WebMCP] fetch failed:", msg, e)
    return { content: [{ type: "text", text: msg }] }
  }
}

export function useWebMcp(config: WebMcpToolConfig, containerRef?: React.RefObject<HTMLElement | null>) {
  const registeredRef = useRef(false)

  useEffect(() => {
    const nav = navigator as any
    const { tool, description, endpoint, method = "GET", responseKey, params } = config

    if (!nav.modelContext) {
      console.warn(
        `[WebMCP] ${tool}: navigator.modelContext is not available — the Claude extension / Chrome 146+ flag may not be active on this page.`
      )
      return
    }
    if (typeof nav.modelContext.registerTool !== "function") {
      console.warn(
        `[WebMCP] ${tool}: navigator.modelContext exists but has no registerTool(). Surfaces present:`,
        Object.keys(nav.modelContext)
      )
      return
    }

    // Build input schema — prefer explicit params, fall back to DOM scan
    const properties: Record<string, { type: string; description: string }> = {}
    const required: string[] = []

    if (params) {
      params.forEach((p) => {
        properties[p.name] = { type: p.type || "string", description: p.description || p.name }
        if (p.required !== false) required.push(p.name)
      })
    } else if (containerRef?.current) {
      containerRef.current
        .querySelectorAll<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>(
          "input[name], textarea[name], select[name]"
        )
        .forEach((inp) => {
          const placeholder =
            inp instanceof HTMLInputElement || inp instanceof HTMLTextAreaElement ? inp.placeholder : ""
          properties[inp.name] = {
            type: "string",
            description: placeholder || inp.name,
          }
          required.push(inp.name)
        })
    }

    // Capture page origin now — the execute callback may run in an isolated
    // context (Chrome's WebMCP sandbox) where window.location isn't the page's.
    const origin = typeof window !== "undefined" ? window.location.origin : ""

    try {
      nav.modelContext.registerTool({
        name: tool,
        description,
        inputSchema: { type: "object", properties, required },
        execute: (args: Record<string, string>) => mcpFetch(origin, endpoint, method, responseKey, args),
      })
      registeredRef.current = true
      console.log(`[WebMCP] Registered tool: ${tool}`, {
        description,
        inputSchema: { type: "object", properties, required },
      })
    } catch (e: any) {
      console.warn(`[WebMCP] ${tool} registerTool threw:`, e)
    }

    // No unregister API in current WebMCP spec — tools persist for page lifetime
  }, []) // eslint-disable-line react-hooks/exhaustive-deps
}
