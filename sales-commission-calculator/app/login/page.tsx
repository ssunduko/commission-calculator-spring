"use client"

import type React from "react"
import { useEffect, useState } from "react"
import Link from "next/link"
import { useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { AlertCircle, LogIn, Zap } from "lucide-react"
import { WebMcpTool } from "@/components/webmcp-tool"
import { authApi, session } from "@/lib/api"

export default function LoginPage() {
  const router = useRouter()
  const [username, setUsername] = useState("")
  const [password, setPassword] = useState("")
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    if (session.getToken()) {
      router.replace("/")
    }
  }, [router])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)

    if (!username.trim() || !password.trim()) {
      setError("Please enter both your username and password.")
      return
    }

    setSubmitting(true)
    try {
      const response = await authApi.login({ username, password })
      session.save(response.token, {
        userId: response.userId,
        username: response.username,
        email: response.email,
        fullName: response.fullName,
      })
      router.push("/")
    } catch (err: any) {
      setError(err?.message?.includes("401")
        ? "Invalid username or password."
        : err?.message || "Login failed. Please try again.")
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-50 via-white to-blue-50/40 px-4 py-12">
      <WebMcpTool
        tool="signInToCommissionHub"
        description="Sign a user in to Commission Hub using their username and password. On success the WebMCP tool stores the returned JWT in browser local storage so subsequent backend calls are authenticated."
        endpoint="/auth/login"
        method="POST"
        params={[
          { name: "username", description: "The user's login name (e.g. jsmith, admin, demo)" },
          { name: "password", description: "The user's password (plain text; submitted over HTTPS/localhost)" },
        ]}
        className="w-full max-w-md"
      >
        <Card className="shadow-xl border-0 bg-white/95 backdrop-blur-sm">
          <CardHeader className="space-y-3 pb-2">
            <div className="flex items-center justify-center">
              <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-blue-600 to-purple-600 shadow-lg">
                <Zap className="h-6 w-6 text-white" />
              </div>
            </div>
            <CardTitle className="text-2xl text-center">Sign in to Commission Hub</CardTitle>
            <CardDescription className="text-center">
              Welcome back. Enter your credentials to continue.
            </CardDescription>
          </CardHeader>
          <form onSubmit={handleSubmit}>
            <CardContent className="space-y-4">
              {error && (
                <Alert variant="destructive">
                  <AlertCircle className="h-4 w-4" />
                  <AlertDescription>{error}</AlertDescription>
                </Alert>
              )}

              <div className="space-y-2">
                <Label htmlFor="username">Username</Label>
                <Input
                  id="username"
                  name="username"
                  placeholder="e.g. jsmith"
                  autoComplete="username"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  disabled={submitting}
                  required
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="password">Password</Label>
                <Input
                  id="password"
                  name="password"
                  type="password"
                  placeholder="••••••••"
                  autoComplete="current-password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  disabled={submitting}
                  required
                />
              </div>

              <div className="text-xs text-slate-500 border rounded-md p-3 bg-slate-50/60">
                <div className="font-medium text-slate-700 mb-1">Demo credentials</div>
                <div>jsmith / sales123 · admin / admin123 · demo / demo1234</div>
              </div>
            </CardContent>
            <CardFooter className="flex flex-col gap-3">
              <Button type="submit" className="w-full" disabled={submitting}>
                {submitting ? (
                  <>
                    <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin mr-2" />
                    Signing in...
                  </>
                ) : (
                  <>
                    <LogIn className="w-4 h-4 mr-2" />
                    Sign in
                  </>
                )}
              </Button>
              <div className="text-sm text-center text-slate-600">
                Need an account?{" "}
                <Link href="/register" className="text-blue-600 hover:underline font-medium">
                  Create one
                </Link>
              </div>
            </CardFooter>
          </form>
        </Card>
      </WebMcpTool>
    </div>
  )
}
