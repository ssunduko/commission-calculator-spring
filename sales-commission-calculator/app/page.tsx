import { Suspense } from "react"
import { EnhancedAppLayout } from "@/components/enhanced-app-layout"

export default function HomePage() {
  return (
    <Suspense fallback={null}>
      <EnhancedAppLayout />
    </Suspense>
  )
}
