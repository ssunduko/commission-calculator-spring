"use client"

import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"
import { session } from "@/lib/api"

/**
 * Client-side auth gate. Renders nothing while the session check is in flight,
 * redirects unauthenticated visitors to /login, and reveals its children once
 * a token is present in local storage.
 *
 * The /login and /register pages opt out of this guard by simply not wrapping
 * themselves in it.
 */
export function AuthGuard({ children }: { children: React.ReactNode }) {
  const router = useRouter()
  const [authorized, setAuthorized] = useState(false)
  const [checked, setChecked] = useState(false)

  useEffect(() => {
    const token = session.getToken()
    if (!token) {
      router.replace("/login")
      setChecked(true)
      return
    }
    setAuthorized(true)
    setChecked(true)
  }, [router])

  if (!checked) {
    return null
  }

  if (!authorized) {
    return null
  }

  return <>{children}</>
}
