"use client"

import { useState } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Badge } from "@/components/ui/badge"
import { Switch } from "@/components/ui/switch"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Separator } from "@/components/ui/separator"
import { Alert, AlertDescription } from "@/components/ui/alert"
import {
  Plus,
  Trash2,
  Save,
  Eye,
  Play,
  Percent,
  TrendingUp,
  TrendingDown,
  AlertTriangle,
  CheckCircle,
  Settings,
  Calculator,
  Layers,
  Zap,
  Award,
  Globe,
  RotateCcw,
  Shield,
  Edit,
  Loader2,
} from "lucide-react"
import { Slider } from "@/components/ui/slider"
import { plansApi } from "@/lib/api"

interface CommissionTier {
  id: string
  name: string
  minThreshold: number
  maxThreshold?: number
  rate: number
  rateType: "percentage" | "fixed"
}

interface AcceleratorRule {
  id: string
  name: string
  threshold: number
  multiplier: number
  maxMultiplier?: number
  conditions: string[]
}

interface DeceleratorRule {
  id: string
  name: string
  threshold: number
  multiplier: number
  minMultiplier?: number
  conditions: string[]
}

interface BonusRule {
  id: string
  name: string
  type: "spif" | "quota_achievement" | "product_bonus" | "team_bonus"
  amount: number
  amountType: "percentage" | "fixed"
  conditions: string[]
  startDate?: string
  endDate?: string
  maxPayout?: number
}

export function PlanBuilder() {
  const [planName, setPlanName] = useState("New Commission Plan")
  const [planDescription, setPlanDescription] = useState("")
  const [baseCurrency, setBaseCurrency] = useState("USD")
  const [effectiveDate, setEffectiveDate] = useState("")

  // Base Commission
  const [baseRate, setBaseRate] = useState([10])
  const [baseRateType, setBaseRateType] = useState("percentage")
  const [appliesTo, setAppliesTo] = useState("all-deals")

  // Tiers
  const [tiers, setTiers] = useState<CommissionTier[]>([])
  const [useTieredStructure, setUseTieredStructure] = useState(false)

  // Accelerators
  const [accelerators, setAccelerators] = useState<AcceleratorRule[]>([])

  // Decelerators
  const [decelerators, setDecelerators] = useState<DeceleratorRule[]>([])

  // Bonuses
  const [bonuses, setBonuses] = useState<BonusRule[]>([])

  // Advanced Settings
  const [currencyConversion, setCurrencyConversion] = useState(true)
  const [supportedCurrencies, setSupportedCurrencies] = useState(["USD", "EUR", "GBP"])
  const [roundingMethod, setRoundingMethod] = useState("round")
  const [roundingPrecision, setRoundingPrecision] = useState(2)
  const [taxEnabled, setTaxEnabled] = useState(false)
  const [taxRate, setTaxRate] = useState(0)
  const [taxJurisdiction, setTaxJurisdiction] = useState("US")
  const [retroactiveEnabled, setRetroactiveEnabled] = useState(true)
  const [adminOverrideEnabled, setAdminOverrideEnabled] = useState(true)

  const [isPreviewMode, setIsPreviewMode] = useState(false)
  const [isSaving, setIsSaving] = useState(false)
  const [saveMessage, setSaveMessage] = useState<string | null>(null)

  const handleSavePlan = async () => {
    setIsSaving(true)
    setSaveMessage(null)
    try {
      const plan = await plansApi.create({
        name: planName,
        currencyCode: baseCurrency,
        effectiveStartDate: effectiveDate || undefined,
      })
      // Add rules from tiers
      for (const tier of tiers) {
        await plansApi.addRule(plan.id, {
          name: tier.name,
          description: `${tier.rateType === "percentage" ? tier.rate + "%" : "$" + tier.rate} commission`,
          rate: tier.rate,
          ruleType: "tiered",
          priority: tiers.indexOf(tier) + 1,
        })
      }
      // Add base rate as a rule if no tiers
      if (tiers.length === 0) {
        await plansApi.addRule(plan.id, {
          name: "Base Rate",
          description: `${baseRateType === "percentage" ? baseRate[0] + "%" : "$" + baseRate[0]} base commission`,
          rate: baseRate[0],
          ruleType: "base_rate",
          priority: 1,
        })
      }
      setSaveMessage(`Plan "${plan.name}" saved successfully (ID: ${plan.id})`)
    } catch (err: any) {
      setSaveMessage(`Error saving plan: ${err.message}`)
    } finally {
      setIsSaving(false)
    }
  }

  const addTier = () => {
    const newTier: CommissionTier = {
      id: `tier-${Date.now()}`,
      name: `Tier ${tiers.length + 1}`,
      minThreshold: tiers.length > 0 ? tiers[tiers.length - 1].maxThreshold || 0 : 0,
      maxThreshold: undefined,
      rate: 5,
      rateType: "percentage",
    }
    setTiers([...tiers, newTier])
  }

  const removeTier = (id: string) => {
    setTiers(tiers.filter((tier) => tier.id !== id))
  }

  const updateTier = (id: string, updates: Partial<CommissionTier>) => {
    setTiers(tiers.map((tier) => (tier.id === id ? { ...tier, ...updates } : tier)))
  }

  const addAccelerator = () => {
    const newAccelerator: AcceleratorRule = {
      id: `acc-${Date.now()}`,
      name: `Accelerator ${accelerators.length + 1}`,
      threshold: 100,
      multiplier: 1.2,
      maxMultiplier: 2.0,
      conditions: [],
    }
    setAccelerators([...accelerators, newAccelerator])
  }

  const removeAccelerator = (id: string) => {
    setAccelerators(accelerators.filter((acc) => acc.id !== id))
  }

  const updateAccelerator = (id: string, updates: Partial<AcceleratorRule>) => {
    setAccelerators(accelerators.map((acc) => (acc.id === id ? { ...acc, ...updates } : acc)))
  }

  const addDecelerator = () => {
    const newDecelerator: DeceleratorRule = {
      id: `dec-${Date.now()}`,
      name: `Decelerator ${decelerators.length + 1}`,
      threshold: 80,
      multiplier: 0.8,
      minMultiplier: 0.5,
      conditions: [],
    }
    setDecelerators([...decelerators, newDecelerator])
  }

  const removeDecelerator = (id: string) => {
    setDecelerators(decelerators.filter((dec) => dec.id !== id))
  }

  const updateDecelerator = (id: string, updates: Partial<DeceleratorRule>) => {
    setDecelerators(decelerators.map((dec) => (dec.id === id ? { ...dec, ...updates } : dec)))
  }

  const addBonus = () => {
    const newBonus: BonusRule = {
      id: `bonus-${Date.now()}`,
      name: `Bonus ${bonuses.length + 1}`,
      type: "spif",
      amount: 1000,
      amountType: "fixed",
      conditions: [],
    }
    setBonuses([...bonuses, newBonus])
  }

  const removeBonus = (id: string) => {
    setBonuses(bonuses.filter((bonus) => bonus.id !== id))
  }

  const updateBonus = (id: string, updates: Partial<BonusRule>) => {
    setBonuses(bonuses.map((bonus) => (bonus.id === id ? { ...bonus, ...updates } : bonus)))
  }

  const calculateSampleCommission = (dealValue: number) => {
    let commission = 0

    if (useTieredStructure && tiers.length > 0) {
      // Tiered calculation
      let remainingValue = dealValue
      for (const tier of tiers.sort((a, b) => a.minThreshold - b.minThreshold)) {
        if (remainingValue <= 0) break

        const tierMin = tier.minThreshold
        const tierMax = tier.maxThreshold || Number.POSITIVE_INFINITY
        const tierValue = Math.min(remainingValue, Math.max(0, tierMax - Math.max(tierMin, dealValue - remainingValue)))

        if (tierValue > 0) {
          const tierCommission = tier.rateType === "fixed" ? tier.rate : (tierValue * tier.rate) / 100
          commission += tierCommission
          remainingValue -= tierValue
        }
      }
    } else {
      // Base rate calculation
      commission = baseRateType === "fixed" ? baseRate[0] : (dealValue * baseRate[0]) / 100
    }

    // Apply bonuses
    bonuses.forEach((bonus) => {
      const bonusAmount = bonus.amountType === "fixed" ? bonus.amount : (commission * bonus.amount) / 100
      commission += bonusAmount
    })

    // Apply accelerators (assuming 110% quota attainment)
    const quotaAttainment = 110
    accelerators.forEach((acc) => {
      if (quotaAttainment >= acc.threshold) {
        commission *= acc.multiplier
      }
    })

    return commission
  }

  return (
    <div className="min-h-screen bg-gray-50 p-6">
      <div className="max-w-7xl mx-auto space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Advanced Plan Builder</h1>
            <p className="text-gray-600 mt-1">
              Create sophisticated commission calculation plans with multi-tier structures and advanced features.
            </p>
          </div>
          <div className="flex items-center gap-3">
            <Button variant="outline" onClick={() => setIsPreviewMode(!isPreviewMode)}>
              <Eye className="w-4 h-4 mr-2" />
              {isPreviewMode ? "Edit Mode" : "Preview Mode"}
            </Button>
            <Button variant="outline">
              <Play className="w-4 h-4 mr-2" />
              Test Plan
            </Button>
            <Button onClick={handleSavePlan} disabled={isSaving}>
              {isSaving ? <Loader2 className="w-4 h-4 mr-2 animate-spin" /> : <Save className="w-4 h-4 mr-2" />}
              {isSaving ? "Saving..." : "Save Plan"}
            </Button>
          </div>
        </div>

        {saveMessage && (
          <Alert className={saveMessage.startsWith("Error") ? "border-red-200 bg-red-50" : "border-green-200 bg-green-50"}>
            <AlertDescription>{saveMessage}</AlertDescription>
          </Alert>
        )}

        <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
          {/* Plan Configuration */}
          <div className="lg:col-span-3">
            <Tabs defaultValue="basic" className="space-y-6">
              <TabsList className="grid w-full grid-cols-6">
                <TabsTrigger value="basic">Basic</TabsTrigger>
                <TabsTrigger value="tiers">Tiers</TabsTrigger>
                <TabsTrigger value="accelerators">Accelerators</TabsTrigger>
                <TabsTrigger value="bonuses">Bonuses</TabsTrigger>
                <TabsTrigger value="advanced">Advanced</TabsTrigger>
                <TabsTrigger value="preview">Preview</TabsTrigger>
              </TabsList>

              {/* Basic Configuration */}
              <TabsContent value="basic" className="space-y-6">
                <Card>
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                      <Settings className="w-5 h-5" />
                      Plan Information
                    </CardTitle>
                    <CardDescription>Configure basic plan details and base commission structure</CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-6">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <div className="space-y-2">
                        <Label htmlFor="plan-name">Plan Name</Label>
                        <Input
                          id="plan-name"
                          value={planName}
                          onChange={(e) => setPlanName(e.target.value)}
                          placeholder="Enter plan name"
                        />
                      </div>
                      <div className="space-y-2">
                        <Label htmlFor="effective-date">Effective Date</Label>
                        <Input
                          id="effective-date"
                          type="date"
                          value={effectiveDate}
                          onChange={(e) => setEffectiveDate(e.target.value)}
                        />
                      </div>
                    </div>

                    <div className="space-y-2">
                      <Label htmlFor="plan-description">Description</Label>
                      <Input
                        id="plan-description"
                        value={planDescription}
                        onChange={(e) => setPlanDescription(e.target.value)}
                        placeholder="Describe the commission plan"
                      />
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <div className="space-y-2">
                        <Label htmlFor="base-currency">Base Currency</Label>
                        <Select value={baseCurrency} onValueChange={setBaseCurrency}>
                          <SelectTrigger>
                            <SelectValue />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem value="USD">USD - US Dollar</SelectItem>
                            <SelectItem value="EUR">EUR - Euro</SelectItem>
                            <SelectItem value="GBP">GBP - British Pound</SelectItem>
                            <SelectItem value="CAD">CAD - Canadian Dollar</SelectItem>
                          </SelectContent>
                        </Select>
                      </div>
                    </div>

                    <Separator />

                    {/* Base Commission Configuration */}
                    <div className="space-y-4">
                      <div className="flex items-center justify-between">
                        <h3 className="text-lg font-semibold flex items-center gap-2">
                          <Percent className="w-5 h-5 text-blue-600" />
                          Base Commission Rate
                        </h3>
                        <div className="flex items-center space-x-2">
                          <Switch
                            id="tiered-structure"
                            checked={useTieredStructure}
                            onCheckedChange={setUseTieredStructure}
                          />
                          <Label htmlFor="tiered-structure">Use Tiered Structure</Label>
                        </div>
                      </div>

                      {!useTieredStructure && (
                        <Card className="border-blue-200 bg-blue-50">
                          <CardContent className="p-4 space-y-4">
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                              <div className="space-y-2">
                                <Label>
                                  Commission Rate: {baseRate[0]}
                                  {baseRateType === "percentage" ? "%" : " (Fixed)"}
                                </Label>
                                <Slider
                                  value={baseRate}
                                  onValueChange={setBaseRate}
                                  max={baseRateType === "percentage" ? 25 : 10000}
                                  min={baseRateType === "percentage" ? 1 : 100}
                                  step={baseRateType === "percentage" ? 0.5 : 100}
                                  className="w-full"
                                />
                              </div>
                              <div className="space-y-2">
                                <Label htmlFor="rate-type">Rate Type</Label>
                                <Select value={baseRateType} onValueChange={setBaseRateType}>
                                  <SelectTrigger>
                                    <SelectValue />
                                  </SelectTrigger>
                                  <SelectContent>
                                    <SelectItem value="percentage">Percentage</SelectItem>
                                    <SelectItem value="fixed">Fixed Amount</SelectItem>
                                  </SelectContent>
                                </Select>
                              </div>
                            </div>

                            <div className="space-y-2">
                              <Label htmlFor="applies-to">Applies To</Label>
                              <Select value={appliesTo} onValueChange={setAppliesTo}>
                                <SelectTrigger>
                                  <SelectValue />
                                </SelectTrigger>
                                <SelectContent>
                                  <SelectItem value="all-deals">All Deals</SelectItem>
                                  <SelectItem value="new-business">New Business Only</SelectItem>
                                  <SelectItem value="renewals">Renewals Only</SelectItem>
                                  <SelectItem value="upsells">Upsells Only</SelectItem>
                                </SelectContent>
                              </Select>
                            </div>
                          </CardContent>
                        </Card>
                      )}
                    </div>
                  </CardContent>
                </Card>
              </TabsContent>

              {/* Tiered Structure */}
              <TabsContent value="tiers" className="space-y-6">
                <Card>
                  <CardHeader>
                    <div className="flex items-center justify-between">
                      <div>
                        <CardTitle className="flex items-center gap-2">
                          <Layers className="w-5 h-5 text-purple-600" />
                          Multi-Tier Commission Structure
                        </CardTitle>
                        <CardDescription>
                          Configure progressive commission rates based on deal value thresholds
                        </CardDescription>
                      </div>
                      <Button onClick={addTier} disabled={!useTieredStructure}>
                        <Plus className="w-4 h-4 mr-2" />
                        Add Tier
                      </Button>
                    </div>
                  </CardHeader>
                  <CardContent>
                    {!useTieredStructure ? (
                      <Alert>
                        <AlertTriangle className="h-4 w-4" />
                        <AlertDescription>
                          Enable "Use Tiered Structure" in the Basic tab to configure commission tiers.
                        </AlertDescription>
                      </Alert>
                    ) : tiers.length === 0 ? (
                      <div className="text-center py-8">
                        <Layers className="w-12 h-12 text-muted-foreground mx-auto mb-4" />
                        <h3 className="text-lg font-medium mb-2">No Tiers Configured</h3>
                        <p className="text-muted-foreground mb-4">
                          Add commission tiers to create progressive rate structures.
                        </p>
                        <Button onClick={addTier}>
                          <Plus className="w-4 h-4 mr-2" />
                          Add First Tier
                        </Button>
                      </div>
                    ) : (
                      <div className="space-y-4">
                        {tiers.map((tier, index) => (
                          <Card key={tier.id} className="border-purple-200 bg-purple-50">
                            <CardContent className="p-4">
                              <div className="flex items-center justify-between mb-4">
                                <h4 className="font-medium flex items-center gap-2">
                                  <Badge variant="outline">Tier {index + 1}</Badge>
                                  {tier.name}
                                </h4>
                                <Button variant="ghost" size="sm" onClick={() => removeTier(tier.id)}>
                                  <Trash2 className="w-4 h-4" />
                                </Button>
                              </div>

                              <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                                <div className="space-y-2">
                                  <Label>Tier Name</Label>
                                  <Input
                                    value={tier.name}
                                    onChange={(e) => updateTier(tier.id, { name: e.target.value })}
                                    placeholder="Tier name"
                                  />
                                </div>
                                <div className="space-y-2">
                                  <Label>Min Threshold</Label>
                                  <Input
                                    type="number"
                                    value={tier.minThreshold}
                                    onChange={(e) => updateTier(tier.id, { minThreshold: Number(e.target.value) })}
                                    placeholder="0"
                                  />
                                </div>
                                <div className="space-y-2">
                                  <Label>Max Threshold</Label>
                                  <Input
                                    type="number"
                                    value={tier.maxThreshold || ""}
                                    onChange={(e) =>
                                      updateTier(tier.id, {
                                        maxThreshold: e.target.value ? Number(e.target.value) : undefined,
                                      })
                                    }
                                    placeholder="Unlimited"
                                  />
                                </div>
                                <div className="space-y-2">
                                  <Label>Rate ({tier.rateType === "percentage" ? "%" : "Fixed"})</Label>
                                  <div className="flex gap-2">
                                    <Input
                                      type="number"
                                      value={tier.rate}
                                      onChange={(e) => updateTier(tier.id, { rate: Number(e.target.value) })}
                                      placeholder="5"
                                    />
                                    <Select
                                      value={tier.rateType}
                                      onValueChange={(value: "percentage" | "fixed") =>
                                        updateTier(tier.id, { rateType: value })
                                      }
                                    >
                                      <SelectTrigger className="w-24">
                                        <SelectValue />
                                      </SelectTrigger>
                                      <SelectContent>
                                        <SelectItem value="percentage">%</SelectItem>
                                        <SelectItem value="fixed">$</SelectItem>
                                      </SelectContent>
                                    </Select>
                                  </div>
                                </div>
                              </div>
                            </CardContent>
                          </Card>
                        ))}
                      </div>
                    )}
                  </CardContent>
                </Card>
              </TabsContent>

              {/* Accelerators & Decelerators */}
              <TabsContent value="accelerators" className="space-y-6">
                {/* Accelerators */}
                <Card>
                  <CardHeader>
                    <div className="flex items-center justify-between">
                      <div>
                        <CardTitle className="flex items-center gap-2">
                          <TrendingUp className="w-5 h-5 text-green-600" />
                          Accelerators
                        </CardTitle>
                        <CardDescription>Multiplier bonuses for quota over-achievement</CardDescription>
                      </div>
                      <Button onClick={addAccelerator}>
                        <Plus className="w-4 h-4 mr-2" />
                        Add Accelerator
                      </Button>
                    </div>
                  </CardHeader>
                  <CardContent>
                    {accelerators.length === 0 ? (
                      <div className="text-center py-8">
                        <TrendingUp className="w-12 h-12 text-muted-foreground mx-auto mb-4" />
                        <h3 className="text-lg font-medium mb-2">No Accelerators Configured</h3>
                        <p className="text-muted-foreground mb-4">
                          Add accelerators to reward over-performance with commission multipliers.
                        </p>
                        <Button onClick={addAccelerator}>
                          <Plus className="w-4 h-4 mr-2" />
                          Add Accelerator
                        </Button>
                      </div>
                    ) : (
                      <div className="space-y-4">
                        {accelerators.map((acc) => (
                          <Card key={acc.id} className="border-green-200 bg-green-50">
                            <CardContent className="p-4">
                              <div className="flex items-center justify-between mb-4">
                                <h4 className="font-medium flex items-center gap-2">
                                  <Zap className="w-4 h-4 text-green-600" />
                                  {acc.name}
                                </h4>
                                <Button variant="ghost" size="sm" onClick={() => removeAccelerator(acc.id)}>
                                  <Trash2 className="w-4 h-4" />
                                </Button>
                              </div>

                              <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                                <div className="space-y-2">
                                  <Label>Name</Label>
                                  <Input
                                    value={acc.name}
                                    onChange={(e) => updateAccelerator(acc.id, { name: e.target.value })}
                                    placeholder="Accelerator name"
                                  />
                                </div>
                                <div className="space-y-2">
                                  <Label>Threshold (%)</Label>
                                  <Input
                                    type="number"
                                    value={acc.threshold}
                                    onChange={(e) => updateAccelerator(acc.id, { threshold: Number(e.target.value) })}
                                    placeholder="100"
                                  />
                                </div>
                                <div className="space-y-2">
                                  <Label>Multiplier</Label>
                                  <Input
                                    type="number"
                                    step="0.1"
                                    value={acc.multiplier}
                                    onChange={(e) => updateAccelerator(acc.id, { multiplier: Number(e.target.value) })}
                                    placeholder="1.2"
                                  />
                                </div>
                                <div className="space-y-2">
                                  <Label>Max Multiplier</Label>
                                  <Input
                                    type="number"
                                    step="0.1"
                                    value={acc.maxMultiplier || ""}
                                    onChange={(e) =>
                                      updateAccelerator(acc.id, {
                                        maxMultiplier: e.target.value ? Number(e.target.value) : undefined,
                                      })
                                    }
                                    placeholder="2.0"
                                  />
                                </div>
                              </div>
                            </CardContent>
                          </Card>
                        ))}
                      </div>
                    )}
                  </CardContent>
                </Card>

                {/* Decelerators */}
                <Card>
                  <CardHeader>
                    <div className="flex items-center justify-between">
                      <div>
                        <CardTitle className="flex items-center gap-2">
                          <TrendingDown className="w-5 h-5 text-red-600" />
                          Decelerators
                        </CardTitle>
                        <CardDescription>Reduced rates for under-performance</CardDescription>
                      </div>
                      <Button onClick={addDecelerator}>
                        <Plus className="w-4 h-4 mr-2" />
                        Add Decelerator
                      </Button>
                    </div>
                  </CardHeader>
                  <CardContent>
                    {decelerators.length === 0 ? (
                      <div className="text-center py-8">
                        <TrendingDown className="w-12 h-12 text-muted-foreground mx-auto mb-4" />
                        <h3 className="text-lg font-medium mb-2">No Decelerators Configured</h3>
                        <p className="text-muted-foreground mb-4">
                          Add decelerators to apply reduced commission rates for under-performance.
                        </p>
                        <Button onClick={addDecelerator}>
                          <Plus className="w-4 h-4 mr-2" />
                          Add Decelerator
                        </Button>
                      </div>
                    ) : (
                      <div className="space-y-4">
                        {decelerators.map((dec) => (
                          <Card key={dec.id} className="border-red-200 bg-red-50">
                            <CardContent className="p-4">
                              <div className="flex items-center justify-between mb-4">
                                <h4 className="font-medium flex items-center gap-2">
                                  <TrendingDown className="w-4 h-4 text-red-600" />
                                  {dec.name}
                                </h4>
                                <Button variant="ghost" size="sm" onClick={() => removeDecelerator(dec.id)}>
                                  <Trash2 className="w-4 h-4" />
                                </Button>
                              </div>

                              <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                                <div className="space-y-2">
                                  <Label>Name</Label>
                                  <Input
                                    value={dec.name}
                                    onChange={(e) => updateDecelerator(dec.id, { name: e.target.value })}
                                    placeholder="Decelerator name"
                                  />
                                </div>
                                <div className="space-y-2">
                                  <Label>Threshold (%)</Label>
                                  <Input
                                    type="number"
                                    value={dec.threshold}
                                    onChange={(e) => updateDecelerator(dec.id, { threshold: Number(e.target.value) })}
                                    placeholder="80"
                                  />
                                </div>
                                <div className="space-y-2">
                                  <Label>Multiplier</Label>
                                  <Input
                                    type="number"
                                    step="0.1"
                                    value={dec.multiplier}
                                    onChange={(e) => updateDecelerator(dec.id, { multiplier: Number(e.target.value) })}
                                    placeholder="0.8"
                                  />
                                </div>
                                <div className="space-y-2">
                                  <Label>Min Multiplier</Label>
                                  <Input
                                    type="number"
                                    step="0.1"
                                    value={dec.minMultiplier || ""}
                                    onChange={(e) =>
                                      updateDecelerator(dec.id, {
                                        minMultiplier: e.target.value ? Number(e.target.value) : undefined,
                                      })
                                    }
                                    placeholder="0.5"
                                  />
                                </div>
                              </div>
                            </CardContent>
                          </Card>
                        ))}
                      </div>
                    )}
                  </CardContent>
                </Card>
              </TabsContent>

              {/* Bonus Systems */}
              <TabsContent value="bonuses" className="space-y-6">
                <Card>
                  <CardHeader>
                    <div className="flex items-center justify-between">
                      <div>
                        <CardTitle className="flex items-center gap-2">
                          <Award className="w-5 h-5 text-yellow-600" />
                          Bonus Systems
                        </CardTitle>
                        <CardDescription>
                          SPIF bonuses, quota achievement bonuses, and product-specific bonuses
                        </CardDescription>
                      </div>
                      <Button onClick={addBonus}>
                        <Plus className="w-4 h-4 mr-2" />
                        Add Bonus
                      </Button>
                    </div>
                  </CardHeader>
                  <CardContent>
                    {bonuses.length === 0 ? (
                      <div className="text-center py-8">
                        <Award className="w-12 h-12 text-muted-foreground mx-auto mb-4" />
                        <h3 className="text-lg font-medium mb-2">No Bonuses Configured</h3>
                        <p className="text-muted-foreground mb-4">
                          Add bonus rules to reward specific achievements and behaviors.
                        </p>
                        <Button onClick={addBonus}>
                          <Plus className="w-4 h-4 mr-2" />
                          Add Bonus
                        </Button>
                      </div>
                    ) : (
                      <div className="space-y-4">
                        {bonuses.map((bonus) => (
                          <Card key={bonus.id} className="border-yellow-200 bg-yellow-50">
                            <CardContent className="p-4">
                              <div className="flex items-center justify-between mb-4">
                                <h4 className="font-medium flex items-center gap-2">
                                  <Award className="w-4 h-4 text-yellow-600" />
                                  {bonus.name}
                                  <Badge variant="outline">{bonus.type.replace("_", " ").toUpperCase()}</Badge>
                                </h4>
                                <Button variant="ghost" size="sm" onClick={() => removeBonus(bonus.id)}>
                                  <Trash2 className="w-4 h-4" />
                                </Button>
                              </div>

                              <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                                <div className="space-y-2">
                                  <Label>Bonus Name</Label>
                                  <Input
                                    value={bonus.name}
                                    onChange={(e) => updateBonus(bonus.id, { name: e.target.value })}
                                    placeholder="Bonus name"
                                  />
                                </div>
                                <div className="space-y-2">
                                  <Label>Bonus Type</Label>
                                  <Select
                                    value={bonus.type}
                                    onValueChange={(value: any) => updateBonus(bonus.id, { type: value })}
                                  >
                                    <SelectTrigger>
                                      <SelectValue />
                                    </SelectTrigger>
                                    <SelectContent>
                                      <SelectItem value="spif">SPIF Bonus</SelectItem>
                                      <SelectItem value="quota_achievement">Quota Achievement</SelectItem>
                                      <SelectItem value="product_bonus">Product Bonus</SelectItem>
                                      <SelectItem value="team_bonus">Team Bonus</SelectItem>
                                    </SelectContent>
                                  </Select>
                                </div>
                              </div>

                              <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
                                <div className="space-y-2">
                                  <Label>Amount</Label>
                                  <Input
                                    type="number"
                                    value={bonus.amount}
                                    onChange={(e) => updateBonus(bonus.id, { amount: Number(e.target.value) })}
                                    placeholder="1000"
                                  />
                                </div>
                                <div className="space-y-2">
                                  <Label>Amount Type</Label>
                                  <Select
                                    value={bonus.amountType}
                                    onValueChange={(value: any) => updateBonus(bonus.id, { amountType: value })}
                                  >
                                    <SelectTrigger>
                                      <SelectValue />
                                    </SelectTrigger>
                                    <SelectContent>
                                      <SelectItem value="fixed">Fixed Amount</SelectItem>
                                      <SelectItem value="percentage">Percentage</SelectItem>
                                    </SelectContent>
                                  </Select>
                                </div>
                                <div className="space-y-2">
                                  <Label>Max Payout</Label>
                                  <Input
                                    type="number"
                                    value={bonus.maxPayout || ""}
                                    onChange={(e) =>
                                      updateBonus(bonus.id, {
                                        maxPayout: e.target.value ? Number(e.target.value) : undefined,
                                      })
                                    }
                                    placeholder="Unlimited"
                                  />
                                </div>
                              </div>

                              {bonus.type === "spif" && (
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                  <div className="space-y-2">
                                    <Label>Start Date</Label>
                                    <Input
                                      type="date"
                                      value={bonus.startDate || ""}
                                      onChange={(e) => updateBonus(bonus.id, { startDate: e.target.value })}
                                    />
                                  </div>
                                  <div className="space-y-2">
                                    <Label>End Date</Label>
                                    <Input
                                      type="date"
                                      value={bonus.endDate || ""}
                                      onChange={(e) => updateBonus(bonus.id, { endDate: e.target.value })}
                                    />
                                  </div>
                                </div>
                              )}
                            </CardContent>
                          </Card>
                        ))}
                      </div>
                    )}
                  </CardContent>
                </Card>
              </TabsContent>

              {/* Advanced Settings */}
              <TabsContent value="advanced" className="space-y-6">
                {/* Currency Conversion */}
                <Card>
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                      <Globe className="w-5 h-5 text-blue-600" />
                      Currency Conversion
                    </CardTitle>
                    <CardDescription>Automatic conversion with configurable exchange rates</CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <div className="flex items-center space-x-2">
                      <Switch
                        id="currency-conversion"
                        checked={currencyConversion}
                        onCheckedChange={setCurrencyConversion}
                      />
                      <Label htmlFor="currency-conversion">Enable automatic currency conversion</Label>
                    </div>

                    {currencyConversion && (
                      <div className="space-y-4 p-4 border rounded-lg bg-blue-50">
                        <div className="space-y-2">
                          <Label>Supported Currencies</Label>
                          <div className="flex flex-wrap gap-2">
                            {["USD", "EUR", "GBP", "CAD", "AUD", "JPY"].map((currency) => (
                              <div key={currency} className="flex items-center space-x-2">
                                <input
                                  type="checkbox"
                                  id={currency}
                                  checked={supportedCurrencies.includes(currency)}
                                  onChange={(e) => {
                                    if (e.target.checked) {
                                      setSupportedCurrencies([...supportedCurrencies, currency])
                                    } else {
                                      setSupportedCurrencies(supportedCurrencies.filter((c) => c !== currency))
                                    }
                                  }}
                                />
                                <Label htmlFor={currency}>{currency}</Label>
                              </div>
                            ))}
                          </div>
                        </div>
                      </div>
                    )}
                  </CardContent>
                </Card>

                {/* Rounding Rules */}
                <Card>
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                      <Calculator className="w-5 h-5 text-purple-600" />
                      Rounding Rules
                    </CardTitle>
                    <CardDescription>Configurable precision and rounding methods</CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <div className="space-y-2">
                        <Label>Rounding Method</Label>
                        <Select value={roundingMethod} onValueChange={setRoundingMethod}>
                          <SelectTrigger>
                            <SelectValue />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem value="round">Round (Standard)</SelectItem>
                            <SelectItem value="floor">Floor (Round Down)</SelectItem>
                            <SelectItem value="ceil">Ceiling (Round Up)</SelectItem>
                          </SelectContent>
                        </Select>
                      </div>
                      <div className="space-y-2">
                        <Label>Decimal Precision</Label>
                        <Select
                          value={roundingPrecision.toString()}
                          onValueChange={(value) => setRoundingPrecision(Number(value))}
                        >
                          <SelectTrigger>
                            <SelectValue />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem value="0">0 (Whole numbers)</SelectItem>
                            <SelectItem value="1">1 decimal place</SelectItem>
                            <SelectItem value="2">2 decimal places</SelectItem>
                            <SelectItem value="3">3 decimal places</SelectItem>
                          </SelectContent>
                        </Select>
                      </div>
                    </div>
                  </CardContent>
                </Card>

                {/* Tax Calculations */}
                <Card>
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                      <Shield className="w-5 h-5 text-green-600" />
                      Tax Calculations
                    </CardTitle>
                    <CardDescription>Jurisdiction-based tax handling with exemptions</CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <div className="flex items-center space-x-2">
                      <Switch id="tax-enabled" checked={taxEnabled} onCheckedChange={setTaxEnabled} />
                      <Label htmlFor="tax-enabled">Enable tax calculations</Label>
                    </div>

                    {taxEnabled && (
                      <div className="space-y-4 p-4 border rounded-lg bg-green-50">
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                          <div className="space-y-2">
                            <Label>Tax Rate (%)</Label>
                            <Input
                              type="number"
                              step="0.1"
                              value={taxRate}
                              onChange={(e) => setTaxRate(Number(e.target.value))}
                              placeholder="0"
                            />
                          </div>
                          <div className="space-y-2">
                            <Label>Tax Jurisdiction</Label>
                            <Select value={taxJurisdiction} onValueChange={setTaxJurisdiction}>
                              <SelectTrigger>
                                <SelectValue />
                              </SelectTrigger>
                              <SelectContent>
                                <SelectItem value="US">United States</SelectItem>
                                <SelectItem value="CA">Canada</SelectItem>
                                <SelectItem value="UK">United Kingdom</SelectItem>
                                <SelectItem value="EU">European Union</SelectItem>
                              </SelectContent>
                            </Select>
                          </div>
                        </div>
                      </div>
                    )}
                  </CardContent>
                </Card>

                {/* Retroactive Adjustments */}
                <Card>
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                      <RotateCcw className="w-5 h-5 text-orange-600" />
                      Retroactive Adjustments
                    </CardTitle>
                    <CardDescription>Full recalculation support for deal changes</CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <div className="flex items-center space-x-2">
                      <Switch
                        id="retroactive-enabled"
                        checked={retroactiveEnabled}
                        onCheckedChange={setRetroactiveEnabled}
                      />
                      <Label htmlFor="retroactive-enabled">Enable retroactive adjustments</Label>
                    </div>

                    {retroactiveEnabled && (
                      <Alert>
                        <RotateCcw className="h-4 w-4" />
                        <AlertDescription>
                          When enabled, any changes to deals will trigger automatic recalculation of affected
                          commissions with full audit trails.
                        </AlertDescription>
                      </Alert>
                    )}
                  </CardContent>
                </Card>

                {/* Admin Overrides */}
                <Card>
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                      <Edit className="w-5 h-5 text-red-600" />
                      Admin Overrides
                    </CardTitle>
                    <CardDescription>Manual adjustments with full audit trails</CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <div className="flex items-center space-x-2">
                      <Switch
                        id="admin-override-enabled"
                        checked={adminOverrideEnabled}
                        onCheckedChange={setAdminOverrideEnabled}
                      />
                      <Label htmlFor="admin-override-enabled">Allow admin overrides</Label>
                    </div>

                    {adminOverrideEnabled && (
                      <Alert>
                        <Edit className="h-4 w-4" />
                        <AlertDescription>
                          Administrators can manually adjust commission amounts with required justification and complete
                          audit logging.
                        </AlertDescription>
                      </Alert>
                    )}
                  </CardContent>
                </Card>
              </TabsContent>

              {/* Preview */}
              <TabsContent value="preview" className="space-y-6">
                <Card>
                  <CardHeader>
                    <CardTitle>Plan Preview & Testing</CardTitle>
                    <CardDescription>
                      See how your commission plan will calculate for different scenarios
                    </CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-6">
                    {/* Sample Calculations */}
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                      {[10000, 50000, 100000].map((dealValue) => (
                        <Card key={dealValue} className="border-2">
                          <CardContent className="p-4">
                            <div className="text-center">
                              <div className="text-sm text-muted-foreground">Deal Value</div>
                              <div className="text-2xl font-bold">${dealValue.toLocaleString()}</div>
                              <Separator className="my-2" />
                              <div className="text-sm text-muted-foreground">Commission</div>
                              <div className="text-xl font-bold text-green-600">
                                ${calculateSampleCommission(dealValue).toLocaleString()}
                              </div>
                              <div className="text-xs text-muted-foreground mt-1">
                                {((calculateSampleCommission(dealValue) / dealValue) * 100).toFixed(2)}% rate
                              </div>
                            </div>
                          </CardContent>
                        </Card>
                      ))}
                    </div>

                    {/* Plan Summary */}
                    <Card className="bg-gray-50">
                      <CardHeader>
                        <CardTitle className="text-lg">Plan Summary</CardTitle>
                      </CardHeader>
                      <CardContent className="space-y-3">
                        <div className="grid grid-cols-2 gap-4 text-sm">
                          <div>
                            <span className="font-medium">Plan Name:</span> {planName}
                          </div>
                          <div>
                            <span className="font-medium">Base Currency:</span> {baseCurrency}
                          </div>
                          <div>
                            <span className="font-medium">Structure:</span>{" "}
                            {useTieredStructure ? `${tiers.length} Tiers` : "Flat Rate"}
                          </div>
                          <div>
                            <span className="font-medium">Accelerators:</span> {accelerators.length}
                          </div>
                          <div>
                            <span className="font-medium">Decelerators:</span> {decelerators.length}
                          </div>
                          <div>
                            <span className="font-medium">Bonuses:</span> {bonuses.length}
                          </div>
                          <div>
                            <span className="font-medium">Currency Conversion:</span>{" "}
                            {currencyConversion ? "Enabled" : "Disabled"}
                          </div>
                          <div>
                            <span className="font-medium">Tax Calculations:</span>{" "}
                            {taxEnabled ? `${taxRate}% (${taxJurisdiction})` : "Disabled"}
                          </div>
                        </div>
                      </CardContent>
                    </Card>
                  </CardContent>
                </Card>
              </TabsContent>
            </Tabs>
          </div>

          {/* Live Preview Panel */}
          <div className="lg:col-span-1">
            <Card className="sticky top-6">
              <CardHeader>
                <CardTitle className="text-lg">Live Preview</CardTitle>
                <CardDescription>Real-time calculation preview</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="p-4 bg-gray-50 rounded-lg">
                  <h4 className="font-medium mb-2">Sample Calculation</h4>
                  <div className="space-y-2 text-sm">
                    <div className="flex justify-between">
                      <span>Deal Value:</span>
                      <span>$50,000</span>
                    </div>
                    {useTieredStructure ? (
                      tiers.map((tier, index) => (
                        <div key={tier.id} className="flex justify-between text-xs text-muted-foreground">
                          <span>Tier {index + 1}:</span>
                          <span>${tier.rateType === "percentage" ? "Variable" : tier.rate}</span>
                        </div>
                      ))
                    ) : (
                      <div className="flex justify-between">
                        <span>
                          Base ({baseRate[0]}
                          {baseRateType === "percentage" ? "%" : ""}):
                        </span>
                        <span>
                          $
                          {baseRateType === "percentage"
                            ? ((50000 * baseRate[0]) / 100).toLocaleString()
                            : baseRate[0].toLocaleString()}
                        </span>
                      </div>
                    )}
                    {bonuses.map((bonus) => (
                      <div key={bonus.id} className="flex justify-between text-xs text-green-600">
                        <span>{bonus.name}:</span>
                        <span>+${bonus.amountType === "fixed" ? bonus.amount.toLocaleString() : "Variable"}</span>
                      </div>
                    ))}
                    <hr className="my-2" />
                    <div className="flex justify-between font-medium">
                      <span>Total Commission:</span>
                      <span>${calculateSampleCommission(50000).toLocaleString()}</span>
                    </div>
                  </div>
                </div>

                <div className="space-y-3">
                  <h4 className="font-medium">Plan Validation</h4>
                  <div className="flex items-center gap-2 text-sm">
                    <CheckCircle className="w-4 h-4 text-green-500" />
                    <span>Plan name provided</span>
                  </div>
                  <div className="flex items-center gap-2 text-sm">
                    <CheckCircle className="w-4 h-4 text-green-500" />
                    <span>Base rate configured</span>
                  </div>
                  {useTieredStructure && tiers.length === 0 && (
                    <div className="flex items-center gap-2 text-sm">
                      <AlertTriangle className="w-4 h-4 text-yellow-500" />
                      <span>Add tiers for tiered structure</span>
                    </div>
                  )}
                  {!currencyConversion && (
                    <div className="flex items-center gap-2 text-sm">
                      <AlertTriangle className="w-4 h-4 text-yellow-500" />
                      <span>Consider enabling currency conversion</span>
                    </div>
                  )}
                </div>

                <div className="space-y-2">
                  <h4 className="font-medium">Advanced Features</h4>
                  <div className="space-y-1 text-xs">
                    <div className="flex items-center justify-between">
                      <span>Multi-tier Structure:</span>
                      <Badge variant={useTieredStructure ? "default" : "secondary"}>
                        {useTieredStructure ? "Enabled" : "Disabled"}
                      </Badge>
                    </div>
                    <div className="flex items-center justify-between">
                      <span>Currency Conversion:</span>
                      <Badge variant={currencyConversion ? "default" : "secondary"}>
                        {currencyConversion ? "Enabled" : "Disabled"}
                      </Badge>
                    </div>
                    <div className="flex items-center justify-between">
                      <span>Tax Calculations:</span>
                      <Badge variant={taxEnabled ? "default" : "secondary"}>
                        {taxEnabled ? "Enabled" : "Disabled"}
                      </Badge>
                    </div>
                    <div className="flex items-center justify-between">
                      <span>Retroactive Adjustments:</span>
                      <Badge variant={retroactiveEnabled ? "default" : "secondary"}>
                        {retroactiveEnabled ? "Enabled" : "Disabled"}
                      </Badge>
                    </div>
                    <div className="flex items-center justify-between">
                      <span>Admin Overrides:</span>
                      <Badge variant={adminOverrideEnabled ? "default" : "secondary"}>
                        {adminOverrideEnabled ? "Enabled" : "Disabled"}
                      </Badge>
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  )
}
