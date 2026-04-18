"use client"

import type React from "react"
import { useRef } from "react"
import { useWebMcp, type WebMcpToolConfig } from "@/hooks/use-webmcp"

/**
 * Declarative WebMCP wrapper component for React.
 *
 * Mirrors the Thymeleaf `data-webmcp-*` pattern: wrap any form card in
 * <WebMcpTool> and the contained inputs are auto-registered as an MCP tool.
 *
 * @example
 * ```tsx
 * <WebMcpTool
 *   tool="createDispute"
 *   description="Create a new commission dispute"
 *   endpoint="/disputes"
 *   method="POST"
 *   params={[
 *     { name: "calculationId", description: "Commission calculation UUID" },
 *     { name: "salesRepId",    description: "Sales rep identifier" },
 *     { name: "title",         description: "Dispute title" },
 *     { name: "description",   description: "Detailed description of the issue" },
 *   ]}
 * >
 *   <Card>…form fields…</Card>
 * </WebMcpTool>
 * ```
 */
export function WebMcpTool({
  children,
  className,
  ...config
}: WebMcpToolConfig & { children: React.ReactNode; className?: string }) {
  const containerRef = useRef<HTMLDivElement>(null)
  useWebMcp(config, containerRef)

  return (
    <div
      ref={containerRef}
      className={className}
      data-webmcp-tool={config.tool}
      data-webmcp-description={config.description}
      data-webmcp-endpoint={config.endpoint}
      data-webmcp-method={config.method || "GET"}
      {...(config.responseKey ? { "data-webmcp-response-key": config.responseKey } : {})}
    >
      {children}
    </div>
  )
}
