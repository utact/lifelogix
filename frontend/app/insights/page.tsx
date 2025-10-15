"use client"

import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"
import { DashboardHeader } from "@/components/dashboard-header"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Lock, TrendingUp, Target, Zap, Users, Award } from "lucide-react"
import { Badge } from "@/components/ui/badge"
import { useToast } from "@/hooks/use-toast"

export default function InsightsPage() {
  const router = useRouter()
  const { toast } = useToast()
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [adminPassword, setAdminPassword] = useState("")
  const [isUnlocked, setIsUnlocked] = useState(false)

  useEffect(() => {
    const token = localStorage.getItem("accessToken")
    if (!token) {
      router.push("/login")
    } else {
      setIsAuthenticated(true)
    }
  }, [router])

  const handleUnlock = () => {
    if (adminPassword === "admin123") {
      setIsUnlocked(true)
      toast({
        title: "잠금 해제 완료",
        description: "베타 기능을 체험할 수 있습니다",
      })
    } else {
      toast({
        title: "비밀번호가 올바르지 않습니다",
        variant: "destructive",
      })
    }
  }

  if (!isAuthenticated) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="text-muted-foreground">Loading...</div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-background">
      <DashboardHeader />
      <div className="container mx-auto max-w-6xl p-6">
        <div className="mb-6">
          <div className="flex items-center gap-3 mb-2">
            <h1 className="text-3xl font-bold">인사이트 & 미래 기능</h1>
            <Badge variant="secondary">v1.0.0</Badge>
          </div>
          <p className="text-muted-foreground">
            LifeLogix의 미래 기능을 미리 확인하세요. 현재는 베타 버전으로 제공됩니다.
          </p>
        </div>

        {!isUnlocked ? (
          <Card className="max-w-md mx-auto">
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Lock className="h-5 w-5" />
                베타 기능 잠금
              </CardTitle>
              <CardDescription>관리자 비밀번호를 입력하여 미래 기능을 체험하세요</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="adminPassword">관리자 비밀번호</Label>
                <Input
                  id="adminPassword"
                  type="password"
                  placeholder="admin123"
                  value={adminPassword}
                  onChange={(e) => setAdminPassword(e.target.value)}
                  onKeyDown={(e) => e.key === "Enter" && handleUnlock()}
                />
              </div>
              <Button onClick={handleUnlock} className="w-full">
                잠금 해제
              </Button>
              <p className="text-xs text-muted-foreground text-center">힌트: 테스트용 비밀번호는 "admin123" 입니다</p>
            </CardContent>
          </Card>
        ) : (
          <div className="grid gap-6 md:grid-cols-2">
            <Card className="border-primary/50">
              <CardHeader>
                <div className="flex items-center justify-between">
                  <CardTitle className="flex items-center gap-2">
                    <TrendingUp className="h-5 w-5 text-primary" />
                    육각형 밸런스 그래프
                  </CardTitle>
                  <Badge>v1.0.0</Badge>
                </div>
                <CardDescription>주간 데이터 기반 삶의 균형 상태 시각화</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="aspect-square bg-muted rounded-lg flex items-center justify-center">
                  <p className="text-muted-foreground text-sm">그래프 미리보기 (개발 중)</p>
                </div>
                <p className="mt-4 text-sm text-muted-foreground">
                  수면, 업무, 학습, 운동, 여가, 관계 등 6가지 영역의 균형을 한눈에 확인할 수 있습니다.
                </p>
              </CardContent>
            </Card>

            <Card className="border-primary/50">
              <CardHeader>
                <div className="flex items-center justify-between">
                  <CardTitle className="flex items-center gap-2">
                    <Zap className="h-5 w-5 text-primary" />
                    OAuth 소셜 로그인
                  </CardTitle>
                  <Badge>v1.0.0</Badge>
                </div>
                <CardDescription>주요 소셜 계정을 통한 간편 로그인</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-2">
                  <Button variant="outline" className="w-full bg-transparent" disabled>
                    Google로 로그인
                  </Button>
                  <Button variant="outline" className="w-full bg-transparent" disabled>
                    Kakao로 로그인
                  </Button>
                  <Button variant="outline" className="w-full bg-transparent" disabled>
                    Naver로 로그인
                  </Button>
                </div>
                <p className="mt-4 text-sm text-muted-foreground">소셜 로그인으로 더 빠르고 편리하게 시작하세요.</p>
              </CardContent>
            </Card>

            <Card className="border-primary/50">
              <CardHeader>
                <div className="flex items-center justify-between">
                  <CardTitle className="flex items-center gap-2">
                    <Users className="h-5 w-5 text-primary" />
                    소셜 기능
                  </CardTitle>
                  <Badge>v1.0.0</Badge>
                </div>
                <CardDescription>팔로우, 하루 회고 및 동료 코멘트</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  <div className="p-3 bg-muted rounded-lg">
                    <p className="text-sm font-medium">팔로우 시스템</p>
                    <p className="text-xs text-muted-foreground">친구들의 성장 여정을 함께 응원하세요</p>
                  </div>
                  <div className="p-3 bg-muted rounded-lg">
                    <p className="text-sm font-medium">하루 회고</p>
                    <p className="text-xs text-muted-foreground">오늘 하루를 돌아보고 공유하세요</p>
                  </div>
                  <div className="p-3 bg-muted rounded-lg">
                    <p className="text-sm font-medium">동료 코멘트</p>
                    <p className="text-xs text-muted-foreground">서로 격려하고 피드백을 주고받으세요</p>
                  </div>
                </div>
              </CardContent>
            </Card>

            <Card className="border-primary/50">
              <CardHeader>
                <div className="flex items-center justify-between">
                  <CardTitle className="flex items-center gap-2">
                    <Award className="h-5 w-5 text-primary" />
                    게이미피케이션
                  </CardTitle>
                  <Badge>v1.0.0</Badge>
                </div>
                <CardDescription>히든 미션 및 칭호 시스템</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  <div className="p-3 bg-muted rounded-lg flex items-center gap-3">
                    <div className="h-10 w-10 rounded-full bg-primary/20 flex items-center justify-center">🏆</div>
                    <div>
                      <p className="text-sm font-medium">얼리버드</p>
                      <p className="text-xs text-muted-foreground">7일 연속 아침 6시 전 기상</p>
                    </div>
                  </div>
                  <div className="p-3 bg-muted rounded-lg flex items-center gap-3">
                    <div className="h-10 w-10 rounded-full bg-primary/20 flex items-center justify-center">💪</div>
                    <div>
                      <p className="text-sm font-medium">운동 마스터</p>
                      <p className="text-xs text-muted-foreground">한 달 동안 20회 이상 운동</p>
                    </div>
                  </div>
                  <div className="p-3 bg-muted rounded-lg flex items-center gap-3">
                    <div className="h-10 w-10 rounded-full bg-primary/20 flex items-center justify-center">📚</div>
                    <div>
                      <p className="text-sm font-medium">지식 탐험가</p>
                      <p className="text-xs text-muted-foreground">100시간 학습 달성</p>
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>

            <Card className="border-primary/50 md:col-span-2">
              <CardHeader>
                <div className="flex items-center justify-between">
                  <CardTitle className="flex items-center gap-2">
                    <Target className="h-5 w-5 text-primary" />
                    AI Goal Planner & Personalized Insights
                  </CardTitle>
                  <Badge>v1.0.0</Badge>
                </div>
                <CardDescription>AI와 함께 능동적으로 삶을 설계하는 파트너</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="grid md:grid-cols-2 gap-4">
                  <div className="p-4 bg-gradient-to-br from-primary/10 to-primary/5 rounded-lg">
                    <h4 className="font-semibold mb-2">AI 목표 플래너</h4>
                    <p className="text-sm text-muted-foreground">
                      과거 데이터 패턴을 분석하여 최적의 목표와 시간 분배를 추천합니다.
                    </p>
                  </div>
                  <div className="p-4 bg-gradient-to-br from-primary/10 to-primary/5 rounded-lg">
                    <h4 className="font-semibold mb-2">개인화된 인사이트</h4>
                    <p className="text-sm text-muted-foreground">
                      사용자의 고유한 습관을 발견하여 자동화된 인사이트를 제공합니다.
                    </p>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>
        )}
      </div>
    </div>
  )
}