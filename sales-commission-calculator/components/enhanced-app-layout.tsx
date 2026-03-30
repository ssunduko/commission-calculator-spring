"use client"

import { useState, useEffect } from "react"
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarHeader,
  SidebarInset,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarProvider,
  SidebarTrigger,
} from "@/components/ui/sidebar"
import { Button } from "@/components/ui/button"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { Badge } from "@/components/ui/badge"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import {
  LayoutDashboard,
  Settings,
  Users,
  DollarSign,
  HelpCircle,
  Bell,
  ChevronUp,
  User,
  LogOut,
  Zap,
  Target,
  TrendingUp,
  BarChart3,
  FileText,
  Palette,
  Activity,
} from "lucide-react"
import { EnhancedSalesDashboard } from "./enhanced-sales-dashboard"
import { AdminDashboard } from "./admin-dashboard"
import { PlanBuilder } from "./plan-builder"
import { AnalyticsDashboard } from "./analytics-dashboard"
import { DesignSystem } from "./design-system"
import { DisputeDashboard } from "./dispute-dashboard"
import { dealsApi, type DealResponse } from "@/lib/api"

const menuItems = [
  {
    title: "Dashboard",
    icon: LayoutDashboard,
    id: "dashboard",
    description: "Overview and key metrics",
  },
  {
    title: "Analytics",
    icon: BarChart3,
    id: "analytics",
    description: "Performance insights",
  },
  {
    title: "Plan Builder",
    icon: Settings,
    id: "plan-builder",
    description: "Commission plan management",
  },
  {
    title: "Disputes",
    icon: FileText,
    id: "disputes",
    description: "Dispute management",
  },
  {
    title: "Admin Panel",
    icon: Users,
    id: "admin",
    description: "System administration",
  },
  {
    title: "Design System",
    icon: Palette,
    id: "design-system",
    description: "UI components and guidelines",
  },
]

const quickActions = [
  {
    title: "View Earnings",
    icon: DollarSign,
    description: "Check current commission",
    color: "from-emerald-500 to-teal-500",
  },
  {
    title: "Pipeline Review",
    icon: Target,
    description: "Review open deals",
    color: "from-blue-500 to-indigo-500",
  },
  {
    title: "Performance",
    icon: TrendingUp,
    description: "Analytics insights",
    color: "from-purple-500 to-pink-500",
  },
  {
    title: "Reports",
    icon: FileText,
    description: "Generate reports",
    color: "from-orange-500 to-red-500",
  },
]

const fmtCurrency = (n: number) =>
  new Intl.NumberFormat("en-US", { style: "currency", currency: "USD", maximumFractionDigits: 0 }).format(n)

function formatTimeAgo(dateStr: string | null): string {
  if (!dateStr) return ""
  const diff = Date.now() - new Date(dateStr).getTime()
  const days = Math.floor(diff / 86400000)
  if (days > 30) return `${Math.floor(days / 30)}mo ago`
  if (days > 0) return `${days}d ago`
  const hours = Math.floor(diff / 3600000)
  if (hours > 0) return `${hours}h ago`
  return "just now"
}

export function EnhancedAppLayout() {
  const [activeView, setActiveView] = useState("dashboard")
  const [recentActivity, setRecentActivity] = useState<
    { title: string; description: string; amount: string; time: string; type: string }[]
  >([])

  useEffect(() => {
    dealsApi.getAll().then((deals) => {
      const sorted = [...deals].sort((a, b) => {
        const dateA = a.closeDate || a.createdDate || ""
        const dateB = b.closeDate || b.createdDate || ""
        return dateB.localeCompare(dateA)
      })
      setRecentActivity(
        sorted.slice(0, 5).map((d) => ({
          title: d.status === "WON" ? "Deal Closed" : d.status === "OPEN" ? "Deal Opened" : `Deal ${d.status}`,
          description: d.title,
          amount: fmtCurrency(d.value),
          time: formatTimeAgo(d.closeDate || d.createdDate),
          type: d.status === "WON" ? "success" : d.status === "OPEN" ? "pending" : "info",
        })),
      )
    }).catch(() => {
      setRecentActivity([])
    })
  }, [])

  const renderContent = () => {
    switch (activeView) {
      case "dashboard":
        return <EnhancedSalesDashboard />
      case "analytics":
        return <AnalyticsDashboard />
      case "disputes":
        return <DisputeDashboard userRole="admin" userId="admin" userName="Admin User" />
      case "admin":
        return <AdminDashboard />
      case "plan-builder":
        return <PlanBuilder />
      case "design-system":
        return <DesignSystem />
      default:
        return <EnhancedSalesDashboard />
    }
  }

  return (
    <SidebarProvider>
      <div className="flex min-h-screen w-full bg-gradient-to-br from-slate-50 via-white to-blue-50/30">
        <Sidebar className="border-r-0 shadow-xl bg-white/80 backdrop-blur-sm">
          <SidebarHeader className="border-b border-slate-200/50 bg-gradient-to-r from-blue-50 to-indigo-50">
            <div className="flex items-center gap-3 px-4 py-4">
              <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-blue-600 to-purple-600 shadow-lg">
                <Zap className="h-5 w-5 text-white" />
              </div>
              <div className="grid flex-1 text-left text-sm leading-tight">
                <span className="truncate font-bold text-slate-900">Commission Hub</span>
                <span className="truncate text-xs text-slate-600">Sales Performance Platform</span>
              </div>
            </div>
          </SidebarHeader>

          <SidebarContent className="px-2 py-4">
            <SidebarGroup>
              <SidebarGroupLabel className="text-slate-600 font-semibold px-3 py-2">Navigation</SidebarGroupLabel>
              <SidebarGroupContent>
                <SidebarMenu className="space-y-1">
                  {menuItems.map((item) => (
                    <SidebarMenuItem key={item.id}>
                      <SidebarMenuButton
                        onClick={() => setActiveView(item.id)}
                        isActive={activeView === item.id}
                        className="group px-3 py-3 rounded-xl hover:bg-blue-50 hover:shadow-sm transition-all data-[active=true]:bg-gradient-to-r data-[active=true]:from-blue-600 data-[active=true]:to-purple-600 data-[active=true]:text-white data-[active=true]:shadow-lg"
                      >
                        <item.icon className="h-5 w-5 group-hover:scale-110 transition-transform" />
                        <div className="flex flex-col">
                          <span className="font-medium">{item.title}</span>
                          <span className="text-xs opacity-70">{item.description}</span>
                        </div>
                      </SidebarMenuButton>
                    </SidebarMenuItem>
                  ))}
                </SidebarMenu>
              </SidebarGroupContent>
            </SidebarGroup>

            <SidebarGroup>
              <SidebarGroupLabel className="text-slate-600 font-semibold px-3 py-2">Quick Actions</SidebarGroupLabel>
              <SidebarGroupContent>
                <div className="grid grid-cols-2 gap-2 px-3">
                  {quickActions.map((action) => (
                    <Button
                      key={action.title}
                      variant="outline"
                      size="sm"
                      className="h-auto p-3 flex flex-col items-center gap-2 hover:shadow-md transition-all border-slate-200 hover:border-blue-300 bg-transparent"
                    >
                      <div className={`p-2 rounded-lg bg-gradient-to-br ${action.color}`}>
                        <action.icon className="h-4 w-4 text-white" />
                      </div>
                      <div className="text-center">
                        <div className="text-xs font-medium text-slate-700">{action.title}</div>
                        <div className="text-xs text-slate-500">{action.description}</div>
                      </div>
                    </Button>
                  ))}
                </div>
              </SidebarGroupContent>
            </SidebarGroup>

            <SidebarGroup>
              <SidebarGroupLabel className="text-slate-600 font-semibold px-3 py-2">Recent Activity</SidebarGroupLabel>
              <SidebarGroupContent>
                <div className="space-y-2 px-3">
                  {recentActivity.map((activity, index) => (
                    <div key={index} className="p-3 rounded-lg bg-slate-50/50 hover:bg-slate-100/50 transition-colors">
                      <div className="flex items-center justify-between mb-1">
                        <span className="text-sm font-medium text-slate-700">{activity.title}</span>
                        <span className="text-xs text-slate-500">{activity.time}</span>
                      </div>
                      <div className="text-xs text-slate-600 mb-1">{activity.description}</div>
                      <div className="text-sm font-bold text-slate-900">{activity.amount}</div>
                    </div>
                  ))}
                </div>
              </SidebarGroupContent>
            </SidebarGroup>
          </SidebarContent>

          <SidebarFooter className="border-t border-slate-200/50 bg-gradient-to-r from-slate-50 to-blue-50/50">
            <SidebarMenu>
              <SidebarMenuItem>
                <DropdownMenu>
                  <DropdownMenuTrigger asChild>
                    <SidebarMenuButton
                      size="lg"
                      className="data-[state=open]:bg-blue-50 hover:bg-blue-50 transition-colors rounded-xl p-3"
                    >
                      <Avatar className="h-10 w-10 rounded-xl shadow-md">
                        <AvatarImage src="/placeholder.svg?height=40&width=40" alt="Sarah Johnson" />
                        <AvatarFallback className="rounded-xl bg-gradient-to-br from-blue-600 to-purple-600 text-white font-bold">
                          SJ
                        </AvatarFallback>
                      </Avatar>
                      <div className="grid flex-1 text-left text-sm leading-tight">
                        <span className="truncate font-semibold text-slate-900">Sarah Johnson</span>
                        <span className="truncate text-xs text-slate-600">Senior Sales Representative</span>
                      </div>
                      <ChevronUp className="ml-auto size-4 text-slate-600" />
                    </SidebarMenuButton>
                  </DropdownMenuTrigger>
                  <DropdownMenuContent
                    className="w-[--radix-dropdown-menu-trigger-width] min-w-56 rounded-xl shadow-xl border-0 bg-white/95 backdrop-blur-sm"
                    side="bottom"
                    align="end"
                    sideOffset={4}
                  >
                    <DropdownMenuLabel className="p-0 font-normal">
                      <div className="flex items-center gap-3 px-3 py-3 text-left text-sm">
                        <Avatar className="h-10 w-10 rounded-xl">
                          <AvatarImage src="/placeholder.svg?height=40&width=40" alt="Sarah Johnson" />
                          <AvatarFallback className="rounded-xl bg-gradient-to-br from-blue-600 to-purple-600 text-white">
                            SJ
                          </AvatarFallback>
                        </Avatar>
                        <div className="grid flex-1 text-left text-sm leading-tight">
                          <span className="truncate font-semibold text-slate-900">Sarah Johnson</span>
                          <span className="truncate text-xs text-slate-600">sarah@company.com</span>
                        </div>
                      </div>
                    </DropdownMenuLabel>
                    <DropdownMenuSeparator />
                    <DropdownMenuItem className="hover:bg-blue-50 rounded-lg mx-1">
                      <User className="mr-3 h-4 w-4 text-slate-600" />
                      <span className="text-slate-700">Account Settings</span>
                    </DropdownMenuItem>
                    <DropdownMenuItem className="hover:bg-blue-50 rounded-lg mx-1">
                      <Bell className="mr-3 h-4 w-4 text-slate-600" />
                      <span className="text-slate-700">Notifications</span>
                    </DropdownMenuItem>
                    <DropdownMenuItem className="hover:bg-blue-50 rounded-lg mx-1">
                      <HelpCircle className="mr-3 h-4 w-4 text-slate-600" />
                      <span className="text-slate-700">Help & Support</span>
                    </DropdownMenuItem>
                    <DropdownMenuSeparator />
                    <DropdownMenuItem className="hover:bg-red-50 text-red-700 rounded-lg mx-1">
                      <LogOut className="mr-3 h-4 w-4" />
                      <span>Sign Out</span>
                    </DropdownMenuItem>
                  </DropdownMenuContent>
                </DropdownMenu>
              </SidebarMenuItem>
            </SidebarMenu>
          </SidebarFooter>
        </Sidebar>

        <SidebarInset className="flex-1">
          <header className="flex h-16 shrink-0 items-center gap-2 border-b border-slate-200/50 px-6 bg-white/80 backdrop-blur-sm shadow-sm">
            <SidebarTrigger className="-ml-1 hover:bg-blue-50 rounded-lg transition-colors" />
            <div className="ml-auto flex items-center gap-3">
              <Badge className="bg-emerald-100 text-emerald-700 border-emerald-200 px-3 py-1.5 shadow-sm">
                <Activity className="w-3 h-3 mr-1.5" />
                All systems operational
              </Badge>
              <Button variant="ghost" size="sm" className="hover:bg-blue-50 rounded-lg transition-colors">
                <Bell className="h-4 w-4 text-slate-600" />
              </Button>
            </div>
          </header>
          <div className="flex-1 overflow-auto">{renderContent()}</div>
        </SidebarInset>
      </div>
    </SidebarProvider>
  )
}
