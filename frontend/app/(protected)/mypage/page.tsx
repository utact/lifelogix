"use client"

import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"
import { DashboardHeader } from "@/components/dashboard-header"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { useAuth } from "@/context/AuthContext"
import { useToast } from "@/hooks/use-toast"
import { api, type UserResponse } from "@/lib/api"
import { User, Mail, Calendar, Trophy, Sparkles, LogOut } from "lucide-react"
import { AIGoalPlanner } from "@/components/ai-goal-planner"
import { GamificationPanel } from "@/components/gamification-panel"

export default function MyPage() {
  const router = useRouter()
  const { toast } = useToast()
  const { logout, isAuthenticated } = useAuth()
  const [stats, setStats] = useState({ categories: 0, activities: 0 })
  const [userInfo, setUserInfo] = useState<UserResponse | null>(null)
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem("accessToken")
    if (isAuthenticated && token) {
      fetchData(token)
    }
  }, [isAuthenticated])

  const fetchData = async (token: string) => {
    setIsLoading(true);
    try {
      const [userData, categories, activityGroups] = await Promise.all([
        api.getMe(token),
        api.getCategories(token),
        api.getActivities(token),
      ]);
      
      setUserInfo(userData);

      const activityCount = activityGroups.reduce((acc, group) => acc + group.activities.length, 0)
      setStats({ categories: categories.length, activities: activityCount })

    } catch (error) {
      toast({ 
        title: "데이터 로드 실패", 
        description: error instanceof Error ? error.message : "사용자 정보를 불러오는데 실패했습니다.",
        variant: "destructive" 
      })
    } finally {
      setIsLoading(false);
    }
  }

  const handleLogout = () => {
    logout()
    toast({
      title: "로그아웃 완료",
      description: "다시 로그인해주세요",
    })
  }

  if (isLoading || !isAuthenticated) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="text-muted-foreground">Loading...</div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-background">
      <DashboardHeader />
      <div className="container mx-auto max-w-7xl p-6">
        <div className="mb-6">
          <h1 className="text-3xl font-bold">마이페이지</h1>
          <p className="text-muted-foreground">프로필 정보를 확인하고 관리하세요</p>
        </div>

        <div className="grid gap-6 lg:grid-cols-2">
          {/* Profile Info */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <User className="h-5 w-5" />
                프로필 정보
              </CardTitle>
              <CardDescription>회원 정보를 확인할 수 있습니다</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="username">사용자 이름</Label>
                <Input id="username" value={userInfo?.username || ""} disabled />
              </div>
              <div className="space-y-2">
                <Label htmlFor="email">이메일</Label>
                <div className="flex items-center gap-2">
                  <Mail className="h-4 w-4 text-muted-foreground" />
                  <Input id="email" value={userInfo?.email || ""} disabled />
                </div>
              </div>
              <div className="space-y-2">
                <Label htmlFor="joinDate">가입일</Label>
                <div className="flex items-center gap-2">
                  <Calendar className="h-4 w-4 text-muted-foreground" />
                  <Input id="joinDate" value={"YYYY년 MM월 DD일"} disabled />
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Statistics & Titles */}
          <Card className="h-full flex flex-col">
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Trophy className="h-5 w-5 text-primary" />
                통계
              </CardTitle>
              <CardDescription>나의 활동 통계와 칭호를 확인하세요</CardDescription>
            </CardHeader>
            <CardContent className="flex-grow flex flex-col gap-4">
              <div className="grid grid-cols-3 gap-4">
                <div className="rounded-lg border border-border p-4 text-center bg-card hover:bg-accent/50 transition-colors">
                  <div className="text-2xl font-bold text-foreground">{stats.categories}</div>
                  <div className="text-xs text-muted-foreground">총 카테고리</div>
                </div>
                <div className="rounded-lg border border-border p-4 text-center bg-card hover:bg-accent/50 transition-colors">
                  <div className="text-2xl font-bold text-foreground">{stats.activities}</div>
                  <div className="text-xs text-muted-foreground">총 활동</div>
                </div>
                <div className="rounded-lg border border-border p-4 text-center bg-card hover:bg-accent/50 transition-colors">
                  <div className="text-2xl font-bold text-foreground">-</div>
                  <div className="text-xs text-muted-foreground">기록된 블록</div>
                </div>
              </div>
              <div className="flex-grow flex flex-col mt-4">
                <h4 className="text-sm font-semibold flex items-center gap-2 mb-2">
                  <Sparkles className="h-4 w-4 text-yellow-500" />
                  획득한 칭호
                </h4>
                <div className="flex-grow rounded-lg border border-dashed flex items-center justify-center">
                  <p className="text-sm text-muted-foreground">획득한 칭호가 없습니다.</p>
                </div>
              </div>
            </CardContent>
          </Card>

          <GamificationPanel />

          <AIGoalPlanner />

          {/* Account Management */}
          <div className="lg:col-span-2">
            <Card>
              <CardHeader>
                <CardTitle>계정 관리</CardTitle>
                <CardDescription>계정 설정을 관리합니다</CardDescription>
              </CardHeader>
              <CardContent>
                <Button variant="destructive" onClick={handleLogout} className="w-full">
                  <LogOut className="mr-2 h-4 w-4" />
                  로그아웃
                </Button>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  )
}