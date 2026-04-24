import { Suspense } from "react"
import { EnhancedAppLayout } from "@/components/enhanced-app-layout"
import { AuthGuard } from "@/components/auth-guard"

export default function HomePage() {
  return (
    <Suspense fallback={null}>
      <AuthGuard>
        <EnhancedAppLayout />
      </AuthGuard>
    </Suspense>
  )
}
