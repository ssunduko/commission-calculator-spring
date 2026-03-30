"use client"

import { useState } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Badge } from "@/components/ui/badge"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Separator } from "@/components/ui/separator"
import { Calculator, DollarSign, TrendingUp, AlertCircle, CheckCircle, Loader2 } from "lucide-react"
import { dealsApi, calculationsApi, plansApi } from "@/lib/api"

interface CommissionBreakdown {
  baseCommission: number
  bonuses: number
  accelerators: number
  decelerators: number
  grossCommission: number
  taxes: number
  netCommission: number
  breakdown: Array<{
    type: string
    name: string
    amount: number
    description: string
  }>
}

export function CommissionCalculatorWidget() {
  const [dealValue, setDealValue] = useState<string>("")
  const [dealType, setDealType] = useState<string>("new_business")
  const [commissionPlan, setCommissionPlan] = useState<string>("standard")
  const [quotaAttainment, setQuotaAttainment] = useState<string>("100")
  const [isCalculating, setIsCalculating] = useState(false)
  const [result, setResult] = useState<CommissionBreakdown | null>(null)
  const [errors, setErrors] = useState<string[]>([])

  const handleCalculate = async () => {
    if (!dealValue || Number.parseFloat(dealValue) <= 0) {
      setErrors(["Please enter a valid deal value"])
      return
    }

    setIsCalculating(true)
    setErrors([])
    setResult(null)

    try {
      // Create a deal via API, then calculate commission
      const deal = await dealsApi.create({
        title: `Calculator - ${dealType} - $${dealValue}`,
        value: Number.parseFloat(dealValue),
        salesRepId: "current-user",
      })

      // Get first available plan or use the selected one
      const plans = await plansApi.getAll()
      const planId = plans.length > 0 ? plans[0].id : commissionPlan

      const calc = await calculationsApi.calculate({
        dealId: deal.id,
        planId,
      })

      setResult({
        baseCommission: calc.baseCommission,
        bonuses: 0,
        accelerators: 0,
        decelerators: 0,
        grossCommission: calc.grossCommission,
        taxes: calc.grossCommission - calc.netCommission,
        netCommission: calc.netCommission,
        breakdown: [
          { type: "base", name: "Base Commission", amount: calc.baseCommission, description: `Plan: ${calc.planId}` },
          { type: "gross", name: "Gross Commission", amount: calc.grossCommission, description: "Before taxes" },
          { type: "net", name: "Net Commission", amount: calc.netCommission, description: "Final payout" },
        ],
      })
    } catch (error) {
      setErrors([error instanceof Error ? error.message : "An unexpected error occurred"])
    } finally {
      setIsCalculating(false)
    }
  }

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat("en-US", {
      style: "currency",
      currency: "USD",
    }).format(amount)
  }

  const getBreakdownColor = (type: string) => {
    switch (type) {
      case "base":
        return "bg-blue-100 text-blue-800"
      case "bonus":
        return "bg-green-100 text-green-800"
      case "accelerator":
        return "bg-purple-100 text-purple-800"
      case "decelerator":
        return "bg-red-100 text-red-800"
      default:
        return "bg-gray-100 text-gray-800"
    }
  }

  return (
    <Card className="w-full max-w-2xl">
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Calculator className="w-5 h-5" />
          Commission Calculator
        </CardTitle>
        <CardDescription>Calculate commission earnings based on deal parameters and commission plans</CardDescription>
      </CardHeader>
      <CardContent className="space-y-6">
        {/* Input Form */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="space-y-2">
            <Label htmlFor="deal-value">Deal Value</Label>
            <div className="relative">
              <DollarSign className="absolute left-3 top-1/2 transform -translate-y-1/2 text-muted-foreground w-4 h-4" />
              <Input
                id="deal-value"
                type="number"
                placeholder="50000"
                value={dealValue}
                onChange={(e) => setDealValue(e.target.value)}
                className="pl-10"
              />
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="deal-type">Deal Type</Label>
            <Select value={dealType} onValueChange={setDealType}>
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="new_business">New Business</SelectItem>
                <SelectItem value="renewal">Renewal</SelectItem>
                <SelectItem value="upsell">Upsell</SelectItem>
                <SelectItem value="cross_sell">Cross-sell</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <div className="space-y-2">
            <Label htmlFor="commission-plan">Commission Plan</Label>
            <Select value={commissionPlan} onValueChange={setCommissionPlan}>
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="standard">Standard Sales Plan</SelectItem>
                <SelectItem value="enterprise">Enterprise Plan</SelectItem>
                <SelectItem value="new_hire">New Hire Plan</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <div className="space-y-2">
            <Label htmlFor="quota-attainment">Quota Attainment (%)</Label>
            <Input
              id="quota-attainment"
              type="number"
              placeholder="100"
              value={quotaAttainment}
              onChange={(e) => setQuotaAttainment(e.target.value)}
            />
          </div>
        </div>

        {/* Calculate Button */}
        <Button onClick={handleCalculate} disabled={isCalculating || !dealValue} className="w-full">
          {isCalculating ? (
            <>
              <Loader2 className="w-4 h-4 mr-2 animate-spin" />
              Calculating...
            </>
          ) : (
            <>
              <Calculator className="w-4 h-4 mr-2" />
              Calculate Commission
            </>
          )}
        </Button>

        {/* Errors */}
        {errors.length > 0 && (
          <Alert>
            <AlertCircle className="h-4 w-4" />
            <AlertDescription>
              <ul className="list-disc list-inside">
                {errors.map((error, index) => (
                  <li key={index}>{error}</li>
                ))}
              </ul>
            </AlertDescription>
          </Alert>
        )}

        {/* Results */}
        {result && (
          <div className="space-y-4">
            <Separator />

            {/* Summary */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <Card>
                <CardContent className="p-4">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-sm text-muted-foreground">Gross Commission</p>
                      <p className="text-2xl font-bold">{formatCurrency(result.grossCommission)}</p>
                    </div>
                    <TrendingUp className="w-8 h-8 text-blue-500" />
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardContent className="p-4">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-sm text-muted-foreground">Taxes</p>
                      <p className="text-2xl font-bold text-red-600">-{formatCurrency(result.taxes)}</p>
                    </div>
                    <AlertCircle className="w-8 h-8 text-red-500" />
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardContent className="p-4">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-sm text-muted-foreground">Net Commission</p>
                      <p className="text-2xl font-bold text-green-600">{formatCurrency(result.netCommission)}</p>
                    </div>
                    <CheckCircle className="w-8 h-8 text-green-500" />
                  </div>
                </CardContent>
              </Card>
            </div>

            {/* Detailed Breakdown */}
            <Card>
              <CardHeader>
                <CardTitle className="text-lg">Commission Breakdown</CardTitle>
                <CardDescription>Detailed calculation components</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  {result.breakdown.map((item, index) => (
                    <div key={index} className="flex items-center justify-between p-3 border rounded-lg">
                      <div className="flex items-center gap-3">
                        <Badge className={getBreakdownColor(item.type)}>
                          {item.type.charAt(0).toUpperCase() + item.type.slice(1)}
                        </Badge>
                        <div>
                          <p className="font-medium">{item.name}</p>
                          <p className="text-sm text-muted-foreground">{item.description}</p>
                        </div>
                      </div>
                      <div className="text-right">
                        <p className={`font-bold ${item.amount >= 0 ? "text-green-600" : "text-red-600"}`}>
                          {item.amount >= 0 ? "+" : ""}
                          {formatCurrency(item.amount)}
                        </p>
                      </div>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>

            {/* Calculation Summary */}
            <Card>
              <CardHeader>
                <CardTitle className="text-lg">Calculation Summary</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="grid grid-cols-2 gap-4 text-sm">
                  <div className="flex justify-between">
                    <span>Base Commission:</span>
                    <span className="font-medium">{formatCurrency(result.baseCommission)}</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Bonuses:</span>
                    <span className="font-medium text-green-600">+{formatCurrency(result.bonuses)}</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Accelerators:</span>
                    <span className="font-medium text-purple-600">+{formatCurrency(result.accelerators)}</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Decelerators:</span>
                    <span className="font-medium text-red-600">{formatCurrency(result.decelerators)}</span>
                  </div>
                </div>
                <Separator className="my-3" />
                <div className="flex justify-between font-bold">
                  <span>Net Commission:</span>
                  <span className="text-green-600">{formatCurrency(result.netCommission)}</span>
                </div>
              </CardContent>
            </Card>
          </div>
        )}
      </CardContent>
    </Card>
  )
}
