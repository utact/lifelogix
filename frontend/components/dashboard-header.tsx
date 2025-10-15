"use client"

import { Button } from "@/components/ui/button"
import { LogOut, Settings, User, Users, Home, CalendarDays, Scale } from "lucide-react"
import { ThemeToggle } from "@/components/theme-toggle"
import { useRouter, usePathname } from "next/navigation"

export function DashboardHeader() {
  const router = useRouter()
  const pathname = usePathname()

  const handleLogout = () => {
    localStorage.removeItem("accessToken")
    router.push("/login")
  }

  const navItems = [
    { path: "/dashboard", icon: Home, label: "타임라인" },
    { path: "/calendar", icon: CalendarDays, label: "캘린더" },
    { path: "/balance", icon: Scale, label: "밸런스" },
    { path: "/social", icon: Users, label: "소셜" },
  ]

  return (
    <header className="border-b border-border bg-card/50 backdrop-blur-sm sticky top-0 z-50">
      <div className="container mx-auto flex h-16 items-center justify-between px-4">
        <div className="flex items-center">
          <div className="flex items-center gap-3 cursor-pointer" onClick={() => router.push("/dashboard")}>
            <ThemeToggle />
            <div>
              <h1 className="text-xl font-bold">LifeLogix</h1>
              <span className="text-xs text-muted-foreground">의도적인 삶을 위한 운영체제</span>
            </div>
          </div>
        </div>

        <nav className="flex items-center gap-1">
          {navItems.map((item) => {
            const isActive = pathname === item.path
            const Icon = item.icon
            return (
              <Button
                key={item.path}
                variant={isActive ? "secondary" : "ghost"}
                size="sm"
                onClick={() => router.push(item.path)}
                className="transition-all duration-200 hover:scale-105"
              >
                <Icon className="h-4 w-4 mr-2" />
                {item.label}
              </Button>
            )
          })}
        </nav>

        <div className="flex items-center gap-2">
          <Button
            variant="ghost"
            size="icon"
            onClick={() => router.push("/mypage")}
            className="transition-all duration-200 hover:scale-110"
          >
            <User className="h-5 w-5" />
          </Button>
          <Button
            variant="ghost"
            size="icon"
            onClick={() => router.push("/settings")}
            className="transition-all duration-200 hover:scale-110"
          >
            <Settings className="h-5 w-5" />
          </Button>
          <Button
            variant="ghost"
            size="icon"
            onClick={handleLogout}
            className="transition-all duration-200 hover:scale-110"
          >
            <LogOut className="h-5 w-5" />
          </Button>
        </div>
      </div>
    </header>
  )
}
