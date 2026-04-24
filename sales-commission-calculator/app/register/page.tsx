"use client"

import type React from "react"
import { useEffect, useState } from "react"
import Link from "next/link"
import { useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Badge } from "@/components/ui/badge"
import { Progress } from "@/components/ui/progress"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { AlertCircle, CheckCircle, CreditCard, Lock, UserPlus, Zap } from "lucide-react"
import { WebMcpTool } from "@/components/webmcp-tool"
import {
  authApi,
  session,
  subscriptionPackagesApi,
  type SubscriptionPackageResponse,
} from "@/lib/api"

type StepKey = 1 | 2 | 3

const CURRENT_YEAR = new Date().getFullYear()
const EXPIRY_YEARS: string[] = Array.from({ length: 12 }, (_, i) => String(CURRENT_YEAR + i))
const EXPIRY_MONTHS: string[] = Array.from({ length: 12 }, (_, i) => String(i + 1).padStart(2, "0"))

function formatCardNumber(value: string): string {
  const digits = value.replace(/\D/g, "").slice(0, 19)
  return digits.replace(/(\d{4})(?=\d)/g, "$1 ")
}

function formatPrice(amount: number): string {
  return new Intl.NumberFormat("en-US", { style: "currency", currency: "USD" }).format(amount)
}

export default function RegisterPage() {
  const router = useRouter()
  const [step, setStep] = useState<StepKey>(1)
  const [packages, setPackages] = useState<SubscriptionPackageResponse[]>([])
  const [packagesLoading, setPackagesLoading] = useState(true)
  const [packagesError, setPackagesError] = useState<string | null>(null)

  const [firstName, setFirstName] = useState("")
  const [lastName, setLastName] = useState("")
  const [username, setUsername] = useState("")
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [confirmPassword, setConfirmPassword] = useState("")
  const [selectedPackageCode, setSelectedPackageCode] = useState<string>("")

  const [cardHolderName, setCardHolderName] = useState("")
  const [cardNumber, setCardNumber] = useState("")
  const [expiryMonth, setExpiryMonth] = useState<string>(EXPIRY_MONTHS[0])
  const [expiryYear, setExpiryYear] = useState<string>(EXPIRY_YEARS[0])
  const [cvv, setCvv] = useState("")

  const [errors, setErrors] = useState<Record<string, string>>({})
  const [submitError, setSubmitError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    if (session.getToken()) {
      router.replace("/")
      return
    }
    subscriptionPackagesApi
      .list()
      .then((list) => {
        setPackages(list)
        if (list.length > 0 && !selectedPackageCode) {
          const pro = list.find((p) => p.tier === "PROFESSIONAL")
          setSelectedPackageCode(pro ? pro.code : list[0].code)
        }
      })
      .catch((err) => setPackagesError(err?.message || "Failed to load subscription packages"))
      .finally(() => setPackagesLoading(false))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const selectedPackage = packages.find((p) => p.code === selectedPackageCode)

  const validateStep = (target: StepKey): boolean => {
    const next: Record<string, string> = {}

    if (target >= 1) {
      if (!firstName.trim()) next.firstName = "First name is required"
      if (!lastName.trim()) next.lastName = "Last name is required"
      if (!username.trim()) next.username = "Username is required"
      else if (username.length < 3) next.username = "Username must be at least 3 characters"
      if (!email.trim()) next.email = "Email is required"
      else if (!email.includes("@")) next.email = "Enter a valid email"
      if (!password) next.password = "Password is required"
      else if (password.length < 8) next.password = "Password must be at least 8 characters"
      if (confirmPassword !== password) next.confirmPassword = "Passwords do not match"
    }

    if (target >= 2) {
      if (!selectedPackageCode) next.packageCode = "Please choose a subscription package"
    }

    if (target >= 3) {
      if (!cardHolderName.trim()) next.cardHolderName = "Card holder name is required"
      const digits = cardNumber.replace(/\s+/g, "")
      if (!/^\d{13,19}$/.test(digits)) next.cardNumber = "Enter a 13–19 digit card number"
      if (!/^\d{3,4}$/.test(cvv)) next.cvv = "CVV must be 3 or 4 digits"
      if (!expiryMonth) next.expiryMonth = "Required"
      if (!expiryYear) next.expiryYear = "Required"
    }

    setErrors(next)
    return Object.keys(next).length === 0
  }

  const handleNext = () => {
    if (!validateStep(step)) return
    setStep((prev) => (prev === 3 ? 3 : ((prev + 1) as StepKey)))
  }

  const handleBack = () => {
    setStep((prev) => (prev === 1 ? 1 : ((prev - 1) as StepKey)))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setSubmitError(null)
    if (!validateStep(3)) {
      return
    }
    setSubmitting(true)
    try {
      const response = await authApi.register({
        username: username.trim(),
        email: email.trim(),
        firstName: firstName.trim(),
        lastName: lastName.trim(),
        password,
        packageCode: selectedPackageCode,
        payment: {
          cardHolderName: cardHolderName.trim(),
          cardNumber: cardNumber.replace(/\s+/g, ""),
          expiryMonth,
          expiryYear,
          cvv,
        },
      })
      session.save(response.token, {
        userId: response.userId,
        username: response.username,
        email: response.email,
        fullName: response.fullName,
      })
      router.push("/?welcome=1")
    } catch (err: any) {
      setSubmitError(err?.message || "Registration failed. Please review your details and try again.")
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-white to-blue-50/40 py-10 px-4">
      <WebMcpTool
        tool="registerForCommissionHub"
        description="Create a new Commission Hub user account, subscribe them to a package (BASIC, PROFESSIONAL, or ENTERPRISE), and charge their credit card. Call listSubscriptionPackages first if you need to pick a package code. On success the tool stores the returned JWT in local storage so subsequent calls are authenticated."
        endpoint="/register"
        method="POST"
        params={[
          { name: "username", description: "Desired login name — must be unique" },
          { name: "email", description: "Contact email — must be unique" },
          { name: "firstName", description: "User's first name" },
          { name: "lastName", description: "User's last name" },
          { name: "password", description: "Account password — at least 8 characters" },
          { name: "packageCode", description: "Subscription package code: BASIC, PROFESSIONAL, or ENTERPRISE" },
          { name: "payment", description: "Object with cardHolderName, cardNumber, expiryMonth, expiryYear, and cvv — test card numbers ending in 0000 are declined" },
        ]}
        className="max-w-4xl mx-auto"
      >
        <div className="flex items-center gap-3 mb-6">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-blue-600 to-purple-600 shadow-lg">
            <Zap className="h-5 w-5 text-white" />
          </div>
          <div>
            <h1 className="text-2xl font-bold text-slate-900">Create your Commission Hub account</h1>
            <p className="text-sm text-slate-600">Step {step} of 3</p>
          </div>
          <div className="ml-auto w-40">
            <Progress value={(step / 3) * 100} />
          </div>
        </div>

        <form onSubmit={handleSubmit} className="space-y-6">
          {step === 1 && (
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <UserPlus className="w-5 h-5" /> Account details
                </CardTitle>
                <CardDescription>Tell us who you are. You'll use this username to sign in.</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div className="space-y-1">
                    <Label htmlFor="firstName">First name</Label>
                    <Input
                      id="firstName"
                      name="firstName"
                      value={firstName}
                      onChange={(e) => setFirstName(e.target.value)}
                      placeholder="Jane"
                    />
                    {errors.firstName && <p className="text-sm text-red-600">{errors.firstName}</p>}
                  </div>
                  <div className="space-y-1">
                    <Label htmlFor="lastName">Last name</Label>
                    <Input
                      id="lastName"
                      name="lastName"
                      value={lastName}
                      onChange={(e) => setLastName(e.target.value)}
                      placeholder="Doe"
                    />
                    {errors.lastName && <p className="text-sm text-red-600">{errors.lastName}</p>}
                  </div>
                </div>

                <div className="space-y-1">
                  <Label htmlFor="username">Username</Label>
                  <Input
                    id="username"
                    name="username"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    placeholder="janedoe"
                    autoComplete="username"
                  />
                  {errors.username && <p className="text-sm text-red-600">{errors.username}</p>}
                </div>

                <div className="space-y-1">
                  <Label htmlFor="email">Email</Label>
                  <Input
                    id="email"
                    name="email"
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="jane@example.com"
                    autoComplete="email"
                  />
                  {errors.email && <p className="text-sm text-red-600">{errors.email}</p>}
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div className="space-y-1">
                    <Label htmlFor="password">Password</Label>
                    <Input
                      id="password"
                      name="password"
                      type="password"
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      placeholder="At least 8 characters"
                      autoComplete="new-password"
                    />
                    {errors.password && <p className="text-sm text-red-600">{errors.password}</p>}
                  </div>
                  <div className="space-y-1">
                    <Label htmlFor="confirmPassword">Confirm password</Label>
                    <Input
                      id="confirmPassword"
                      name="confirmPassword"
                      type="password"
                      value={confirmPassword}
                      onChange={(e) => setConfirmPassword(e.target.value)}
                      placeholder="Re-enter password"
                      autoComplete="new-password"
                    />
                    {errors.confirmPassword && <p className="text-sm text-red-600">{errors.confirmPassword}</p>}
                  </div>
                </div>

                <div className="flex justify-between pt-2">
                  <Button type="button" variant="outline" asChild>
                    <Link href="/login">Already have an account?</Link>
                  </Button>
                  <Button type="button" onClick={handleNext}>
                    Next: Choose a package
                  </Button>
                </div>
              </CardContent>
            </Card>
          )}

          {step === 2 && (
            <Card>
              <CardHeader>
                <CardTitle>Choose a subscription</CardTitle>
                <CardDescription>
                  Packages scale from solo reps to full enterprise teams. You can change tiers later.
                </CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                {packagesError && (
                  <Alert variant="destructive">
                    <AlertCircle className="h-4 w-4" />
                    <AlertDescription>{packagesError}</AlertDescription>
                  </Alert>
                )}

                {packagesLoading && (
                  <div className="text-sm text-slate-600">Loading available packages…</div>
                )}

                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  {packages.map((pkg) => {
                    const isSelected = pkg.code === selectedPackageCode
                    return (
                      <button
                        type="button"
                        key={pkg.id}
                        onClick={() => setSelectedPackageCode(pkg.code)}
                        className={`text-left border rounded-xl p-4 transition-all ${
                          isSelected
                            ? "border-blue-600 ring-2 ring-blue-600/30 shadow-lg bg-blue-50/40"
                            : "border-slate-200 hover:border-blue-300 hover:bg-slate-50"
                        }`}
                      >
                        <div className="flex items-start justify-between mb-2">
                          <div>
                            <div className="text-sm font-medium text-slate-500">{pkg.tier}</div>
                            <div className="text-lg font-bold">{pkg.name}</div>
                          </div>
                          {isSelected && <CheckCircle className="w-5 h-5 text-blue-600" />}
                        </div>
                        <div className="text-3xl font-bold mb-2">
                          {formatPrice(pkg.monthlyPrice)}
                          <span className="text-sm font-normal text-slate-500">/mo</span>
                        </div>
                        <p className="text-sm text-slate-600 mb-3">{pkg.description}</p>
                        <div className="flex flex-wrap gap-2">
                          <Badge variant="secondary">Up to {pkg.maxUsers} users</Badge>
                          <Badge variant="secondary">{pkg.maxDealsPerMonth} deals/mo</Badge>
                        </div>
                      </button>
                    )
                  })}
                </div>

                {errors.packageCode && <p className="text-sm text-red-600">{errors.packageCode}</p>}

                <input type="hidden" name="packageCode" value={selectedPackageCode} />

                <div className="flex justify-between pt-2">
                  <Button type="button" variant="outline" onClick={handleBack}>
                    Back
                  </Button>
                  <Button type="button" onClick={handleNext} disabled={!selectedPackage}>
                    Next: Payment
                  </Button>
                </div>
              </CardContent>
            </Card>
          )}

          {step === 3 && (
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <CreditCard className="w-5 h-5" /> Payment details
                </CardTitle>
                <CardDescription>
                  {selectedPackage ? (
                    <>
                      You'll be charged <strong>{formatPrice(selectedPackage.monthlyPrice)}</strong> today for the{" "}
                      <strong>{selectedPackage.name}</strong> plan, and then monthly until you cancel.
                    </>
                  ) : (
                    <>Select a package to see pricing.</>
                  )}
                </CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="space-y-1">
                  <Label htmlFor="cardHolderName">Cardholder name</Label>
                  <Input
                    id="cardHolderName"
                    name="cardHolderName"
                    value={cardHolderName}
                    onChange={(e) => setCardHolderName(e.target.value)}
                    placeholder="Name on card"
                    autoComplete="cc-name"
                  />
                  {errors.cardHolderName && <p className="text-sm text-red-600">{errors.cardHolderName}</p>}
                </div>

                <div className="space-y-1">
                  <Label htmlFor="cardNumber">Card number</Label>
                  <Input
                    id="cardNumber"
                    name="cardNumber"
                    value={cardNumber}
                    onChange={(e) => setCardNumber(formatCardNumber(e.target.value))}
                    placeholder="4242 4242 4242 4242"
                    inputMode="numeric"
                    autoComplete="cc-number"
                  />
                  {errors.cardNumber && <p className="text-sm text-red-600">{errors.cardNumber}</p>}
                  <p className="text-xs text-slate-500">
                    Use any test card (e.g. 4242 4242 4242 4242). Cards ending in 0000 simulate a decline.
                  </p>
                </div>

                <div className="grid grid-cols-3 gap-4">
                  <div className="space-y-1">
                    <Label htmlFor="expiryMonth">Month</Label>
                    <select
                      id="expiryMonth"
                      name="expiryMonth"
                      value={expiryMonth}
                      onChange={(e) => setExpiryMonth(e.target.value)}
                      className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                    >
                      {EXPIRY_MONTHS.map((m) => (
                        <option key={m} value={m}>
                          {m}
                        </option>
                      ))}
                    </select>
                    {errors.expiryMonth && <p className="text-sm text-red-600">{errors.expiryMonth}</p>}
                  </div>
                  <div className="space-y-1">
                    <Label htmlFor="expiryYear">Year</Label>
                    <select
                      id="expiryYear"
                      name="expiryYear"
                      value={expiryYear}
                      onChange={(e) => setExpiryYear(e.target.value)}
                      className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                    >
                      {EXPIRY_YEARS.map((y) => (
                        <option key={y} value={y}>
                          {y}
                        </option>
                      ))}
                    </select>
                    {errors.expiryYear && <p className="text-sm text-red-600">{errors.expiryYear}</p>}
                  </div>
                  <div className="space-y-1">
                    <Label htmlFor="cvv">CVV</Label>
                    <Input
                      id="cvv"
                      name="cvv"
                      value={cvv}
                      onChange={(e) => setCvv(e.target.value.replace(/\D/g, "").slice(0, 4))}
                      placeholder="123"
                      inputMode="numeric"
                      autoComplete="cc-csc"
                    />
                    {errors.cvv && <p className="text-sm text-red-600">{errors.cvv}</p>}
                  </div>
                </div>

                <Alert className="bg-slate-50 border-slate-200">
                  <Lock className="h-4 w-4" />
                  <AlertDescription className="text-slate-700">
                    This is a demo payment form. Card numbers never leave your browser in full — only the last four digits are persisted with the subscription record.
                  </AlertDescription>
                </Alert>

                {submitError && (
                  <Alert variant="destructive">
                    <AlertCircle className="h-4 w-4" />
                    <AlertDescription>{submitError}</AlertDescription>
                  </Alert>
                )}

                <div className="flex justify-between pt-2">
                  <Button type="button" variant="outline" onClick={handleBack}>
                    Back
                  </Button>
                  <Button type="submit" disabled={submitting}>
                    {submitting ? (
                      <>
                        <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin mr-2" />
                        Creating account…
                      </>
                    ) : (
                      <>
                        <CheckCircle className="w-4 h-4 mr-2" />
                        {selectedPackage
                          ? `Pay ${formatPrice(selectedPackage.monthlyPrice)} & create account`
                          : "Create account"}
                      </>
                    )}
                  </Button>
                </div>
              </CardContent>
            </Card>
          )}
        </form>
      </WebMcpTool>
    </div>
  )
}
