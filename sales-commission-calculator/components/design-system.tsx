"use client"

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Progress } from "@/components/ui/progress"
import { Separator } from "@/components/ui/separator"
import {
  Palette,
  Type,
  Layout,
  Eye,
  Layers,
  Grid,
  Zap,
  Star,
  CheckCircle,
  AlertCircle,
  Info,
  XCircle,
} from "lucide-react"

export function DesignSystem() {
  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-white to-blue-50 p-8">
      <div className="max-w-7xl mx-auto space-y-12">
        {/* Header */}
        <div className="text-center space-y-4">
          <div className="inline-flex items-center gap-3 px-6 py-3 bg-white/80 backdrop-blur-sm rounded-full border border-slate-200 shadow-sm">
            <Palette className="w-5 h-5 text-blue-600" />
            <span className="text-sm font-medium text-slate-700">Design System</span>
          </div>
          <h1 className="text-4xl font-bold bg-gradient-to-r from-slate-900 via-blue-900 to-slate-900 bg-clip-text text-transparent">
            Sales Commission Calculator
          </h1>
          <p className="text-lg text-slate-600 max-w-2xl mx-auto">
            A comprehensive design system for modern sales commission management with enhanced visual hierarchy and
            professional aesthetics.
          </p>
        </div>

        {/* Color Palette */}
        <Card className="border-0 shadow-xl bg-white/70 backdrop-blur-sm">
          <CardHeader className="pb-8">
            <CardTitle className="flex items-center gap-3 text-2xl">
              <div className="p-2 bg-gradient-to-br from-blue-500 to-purple-600 rounded-lg">
                <Palette className="w-6 h-6 text-white" />
              </div>
              Color Palette
            </CardTitle>
            <CardDescription className="text-base">
              Carefully crafted color system for optimal contrast and accessibility
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-8">
            {/* Primary Colors */}
            <div className="space-y-4">
              <h3 className="text-lg font-semibold text-slate-800">Primary Colors</h3>
              <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-4">
                {[
                  { name: "Blue 50", color: "bg-blue-50", hex: "#eff6ff", text: "text-slate-700" },
                  { name: "Blue 100", color: "bg-blue-100", hex: "#dbeafe", text: "text-slate-700" },
                  { name: "Blue 500", color: "bg-blue-500", hex: "#3b82f6", text: "text-white" },
                  { name: "Blue 600", color: "bg-blue-600", hex: "#2563eb", text: "text-white" },
                  { name: "Blue 700", color: "bg-blue-700", hex: "#1d4ed8", text: "text-white" },
                  { name: "Blue 900", color: "bg-blue-900", hex: "#1e3a8a", text: "text-white" },
                ].map((color) => (
                  <div key={color.name} className="space-y-2">
                    <div className={`${color.color} h-16 rounded-xl border border-slate-200 shadow-sm`} />
                    <div className="text-center">
                      <div className="text-sm font-medium text-slate-700">{color.name}</div>
                      <div className="text-xs text-slate-500">{color.hex}</div>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Status Colors */}
            <div className="space-y-4">
              <h3 className="text-lg font-semibold text-slate-800">Status Colors</h3>
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                {[
                  { name: "Success", color: "bg-emerald-500", hex: "#10b981", icon: CheckCircle },
                  { name: "Warning", color: "bg-amber-500", hex: "#f59e0b", icon: AlertCircle },
                  { name: "Error", color: "bg-red-500", hex: "#ef4444", icon: XCircle },
                  { name: "Info", color: "bg-cyan-500", hex: "#06b6d4", icon: Info },
                ].map((status) => (
                  <div key={status.name} className="space-y-3">
                    <div className={`${status.color} h-16 rounded-xl shadow-sm flex items-center justify-center`}>
                      <status.icon className="w-6 h-6 text-white" />
                    </div>
                    <div className="text-center">
                      <div className="text-sm font-medium text-slate-700">{status.name}</div>
                      <div className="text-xs text-slate-500">{status.hex}</div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Typography */}
        <Card className="border-0 shadow-xl bg-white/70 backdrop-blur-sm">
          <CardHeader className="pb-8">
            <CardTitle className="flex items-center gap-3 text-2xl">
              <div className="p-2 bg-gradient-to-br from-purple-500 to-pink-600 rounded-lg">
                <Type className="w-6 h-6 text-white" />
              </div>
              Typography Scale
            </CardTitle>
            <CardDescription className="text-base">
              Harmonious type scale for clear information hierarchy
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-8">
            <div className="space-y-6">
              <div className="space-y-2">
                <h1 className="text-4xl font-bold text-slate-900">Heading 1 - Display</h1>
                <p className="text-sm text-slate-500">text-4xl font-bold</p>
              </div>
              <div className="space-y-2">
                <h2 className="text-3xl font-bold text-slate-800">Heading 2 - Page Title</h2>
                <p className="text-sm text-slate-500">text-3xl font-bold</p>
              </div>
              <div className="space-y-2">
                <h3 className="text-2xl font-semibold text-slate-800">Heading 3 - Section</h3>
                <p className="text-sm text-slate-500">text-2xl font-semibold</p>
              </div>
              <div className="space-y-2">
                <h4 className="text-xl font-semibold text-slate-700">Heading 4 - Subsection</h4>
                <p className="text-sm text-slate-500">text-xl font-semibold</p>
              </div>
              <div className="space-y-2">
                <h5 className="text-lg font-medium text-slate-700">Heading 5 - Component</h5>
                <p className="text-sm text-slate-500">text-lg font-medium</p>
              </div>
              <div className="space-y-2">
                <p className="text-base text-slate-600">
                  Body text - Regular paragraph content with optimal readability and comfortable line spacing for
                  extended reading.
                </p>
                <p className="text-sm text-slate-500">text-base text-slate-600</p>
              </div>
              <div className="space-y-2">
                <p className="text-sm text-slate-500">Small text - Secondary information, captions, and metadata</p>
                <p className="text-xs text-slate-400">text-sm text-slate-500</p>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Component Showcase */}
        <Card className="border-0 shadow-xl bg-white/70 backdrop-blur-sm">
          <CardHeader className="pb-8">
            <CardTitle className="flex items-center gap-3 text-2xl">
              <div className="p-2 bg-gradient-to-br from-emerald-500 to-teal-600 rounded-lg">
                <Layers className="w-6 h-6 text-white" />
              </div>
              Component Library
            </CardTitle>
            <CardDescription className="text-base">
              Polished UI components with consistent styling and interactions
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-12">
            {/* Buttons */}
            <div className="space-y-6">
              <h3 className="text-lg font-semibold text-slate-800">Buttons</h3>
              <div className="flex flex-wrap gap-4">
                <Button className="bg-gradient-to-r from-blue-600 to-blue-700 hover:from-blue-700 hover:to-blue-800 shadow-lg">
                  <Zap className="w-4 h-4 mr-2" />
                  Primary Action
                </Button>
                <Button variant="outline" className="border-2 hover:bg-slate-50 bg-transparent">
                  Secondary Action
                </Button>
                <Button variant="ghost" className="hover:bg-slate-100">
                  Ghost Button
                </Button>
                <Button variant="destructive" className="shadow-lg">
                  <XCircle className="w-4 h-4 mr-2" />
                  Destructive
                </Button>
              </div>
            </div>

            {/* Cards */}
            <div className="space-y-6">
              <h3 className="text-lg font-semibold text-slate-800">Cards & Metrics</h3>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                <Card className="border-0 shadow-lg bg-gradient-to-br from-blue-50 to-indigo-50 hover:shadow-xl transition-all duration-300">
                  <CardHeader className="pb-3">
                    <CardTitle className="flex items-center justify-between text-slate-700">
                      <span className="text-sm font-medium">Total Earnings</span>
                      <div className="p-2 bg-blue-100 rounded-lg">
                        <Star className="w-4 h-4 text-blue-600" />
                      </div>
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="text-3xl font-bold text-slate-900 mb-2">$127,450</div>
                    <div className="flex items-center gap-2">
                      <div className="flex items-center text-emerald-600 text-sm font-medium">
                        <CheckCircle className="w-3 h-3 mr-1" />
                        +12.5%
                      </div>
                      <span className="text-slate-500 text-sm">vs last month</span>
                    </div>
                  </CardContent>
                </Card>

                <Card className="border-0 shadow-lg bg-gradient-to-br from-emerald-50 to-teal-50 hover:shadow-xl transition-all duration-300">
                  <CardHeader className="pb-3">
                    <CardTitle className="flex items-center justify-between text-slate-700">
                      <span className="text-sm font-medium">Quota Progress</span>
                      <div className="p-2 bg-emerald-100 rounded-lg">
                        <CheckCircle className="w-4 h-4 text-emerald-600" />
                      </div>
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="text-3xl font-bold text-slate-900 mb-3">87.2%</div>
                    <Progress value={87.2} className="h-2 mb-2" />
                    <div className="text-slate-500 text-sm">$174,400 of $200,000</div>
                  </CardContent>
                </Card>

                <Card className="border-0 shadow-lg bg-gradient-to-br from-amber-50 to-orange-50 hover:shadow-xl transition-all duration-300">
                  <CardHeader className="pb-3">
                    <CardTitle className="flex items-center justify-between text-slate-700">
                      <span className="text-sm font-medium">Active Deals</span>
                      <div className="p-2 bg-amber-100 rounded-lg">
                        <Eye className="w-4 h-4 text-amber-600" />
                      </div>
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="text-3xl font-bold text-slate-900 mb-2">23</div>
                    <div className="text-slate-500 text-sm">$485,200 pipeline value</div>
                  </CardContent>
                </Card>
              </div>
            </div>

            {/* Status Badges */}
            <div className="space-y-6">
              <h3 className="text-lg font-semibold text-slate-800">Status Indicators</h3>
              <div className="flex flex-wrap gap-3">
                <Badge className="bg-emerald-100 text-emerald-800 border-emerald-200 px-3 py-1">
                  <CheckCircle className="w-3 h-3 mr-1" />
                  Approved
                </Badge>
                <Badge className="bg-blue-100 text-blue-800 border-blue-200 px-3 py-1">
                  <Info className="w-3 h-3 mr-1" />
                  In Review
                </Badge>
                <Badge className="bg-amber-100 text-amber-800 border-amber-200 px-3 py-1">
                  <AlertCircle className="w-3 h-3 mr-1" />
                  Pending
                </Badge>
                <Badge className="bg-red-100 text-red-800 border-red-200 px-3 py-1">
                  <XCircle className="w-3 h-3 mr-1" />
                  Rejected
                </Badge>
                <Badge className="bg-purple-100 text-purple-800 border-purple-200 px-3 py-1">
                  <Star className="w-3 h-3 mr-1" />
                  Priority
                </Badge>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Layout Principles */}
        <Card className="border-0 shadow-xl bg-white/70 backdrop-blur-sm">
          <CardHeader className="pb-8">
            <CardTitle className="flex items-center gap-3 text-2xl">
              <div className="p-2 bg-gradient-to-br from-orange-500 to-red-600 rounded-lg">
                <Layout className="w-6 h-6 text-white" />
              </div>
              Layout & Spacing
            </CardTitle>
            <CardDescription className="text-base">
              Consistent spacing system and layout principles for visual harmony
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-8">
            <div className="space-y-6">
              <h3 className="text-lg font-semibold text-slate-800">Spacing Scale</h3>
              <div className="space-y-4">
                {[
                  { size: "xs", value: "0.25rem", pixels: "4px" },
                  { size: "sm", value: "0.5rem", pixels: "8px" },
                  { size: "md", value: "1rem", pixels: "16px" },
                  { size: "lg", value: "1.5rem", pixels: "24px" },
                  { size: "xl", value: "2rem", pixels: "32px" },
                  { size: "2xl", value: "3rem", pixels: "48px" },
                ].map((space) => (
                  <div key={space.size} className="flex items-center gap-4">
                    <div className="w-16 text-sm font-medium text-slate-600">{space.size}</div>
                    <div className="bg-blue-500 rounded" style={{ width: space.value, height: "1rem" }} />
                    <div className="text-sm text-slate-500">
                      {space.value} ({space.pixels})
                    </div>
                  </div>
                ))}
              </div>
            </div>

            <Separator className="my-8" />

            <div className="space-y-6">
              <h3 className="text-lg font-semibold text-slate-800">Grid System</h3>
              <div className="grid grid-cols-12 gap-4 h-24">
                {Array.from({ length: 12 }, (_, i) => (
                  <div
                    key={i}
                    className="bg-gradient-to-br from-slate-100 to-slate-200 rounded-lg flex items-center justify-center text-xs font-medium text-slate-600"
                  >
                    {i + 1}
                  </div>
                ))}
              </div>
              <p className="text-sm text-slate-500">
                12-column responsive grid system with consistent gutters and breakpoints
              </p>
            </div>
          </CardContent>
        </Card>

        {/* Design Principles */}
        <Card className="border-0 shadow-xl bg-gradient-to-br from-slate-50 to-blue-50">
          <CardHeader className="pb-8">
            <CardTitle className="flex items-center gap-3 text-2xl">
              <div className="p-2 bg-gradient-to-br from-indigo-500 to-purple-600 rounded-lg">
                <Grid className="w-6 h-6 text-white" />
              </div>
              Design Principles
            </CardTitle>
            <CardDescription className="text-base">
              Core principles guiding the visual design and user experience
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
              {[
                {
                  title: "Clarity",
                  description: "Clear visual hierarchy and intuitive information architecture",
                  icon: Eye,
                  color: "from-blue-500 to-cyan-500",
                },
                {
                  title: "Consistency",
                  description: "Unified design language across all components and interactions",
                  icon: Grid,
                  color: "from-purple-500 to-pink-500",
                },
                {
                  title: "Efficiency",
                  description: "Streamlined workflows and optimized user journeys",
                  icon: Zap,
                  color: "from-emerald-500 to-teal-500",
                },
                {
                  title: "Accessibility",
                  description: "Inclusive design with proper contrast and keyboard navigation",
                  icon: CheckCircle,
                  color: "from-orange-500 to-red-500",
                },
                {
                  title: "Responsiveness",
                  description: "Seamless experience across all device sizes and orientations",
                  icon: Layout,
                  color: "from-indigo-500 to-blue-500",
                },
                {
                  title: "Performance",
                  description: "Fast loading times and smooth animations for better UX",
                  icon: Star,
                  color: "from-amber-500 to-orange-500",
                },
              ].map((principle) => (
                <div key={principle.title} className="space-y-4">
                  <div
                    className={`w-12 h-12 rounded-xl bg-gradient-to-br ${principle.color} flex items-center justify-center shadow-lg`}
                  >
                    <principle.icon className="w-6 h-6 text-white" />
                  </div>
                  <div className="space-y-2">
                    <h4 className="text-lg font-semibold text-slate-800">{principle.title}</h4>
                    <p className="text-slate-600 text-sm leading-relaxed">{principle.description}</p>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
