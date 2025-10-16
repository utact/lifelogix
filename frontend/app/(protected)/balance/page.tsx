"use client"

import { useEffect, useState, useMemo } from "react"
import { useRouter } from "next/navigation"
import { DashboardHeader } from "@/components/dashboard-header"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { HexagonChart } from "@/components/hexagon-chart"
import { BarChart3, TrendingUp, Calendar } from "lucide-react"
import { BalanceSkeleton } from "@/components/balance-skeleton"
import { api, type TimeBlock } from "@/lib/api"
import { IDEAL_HOURS } from "@/lib/config"
import { useToast } from "@/hooks/use-toast"

export default function BalancePage() {
  const router = useRouter()
  const { toast } = useToast()
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [timeBlocks, setTimeBlocks] = useState<TimeBlock[]>([])
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const token = localStorage.getItem("accessToken")
    if (!token) {
      router.push("/login")
    } else {
      setIsAuthenticated(true)
      loadData(token)
    }
  }, [router])

  const loadData = async (token: string) => {
    try {
      const today = new Date().toISOString().split("T")[0]
      const timelineData = await api.getTimeline(token, today)
      setTimeBlocks(timelineData.timeBlocks)
    } catch (error) {
      toast({
        title: "데이터 로드 실패",
        description: error instanceof Error ? error.message : "다시 시도해주세요",
        variant: "destructive",
      })
    } finally {
      setIsLoading(false)
    }
  }

  const balanceChartData = useMemo(() => {
    const categoryMap: { [key: string]: string } = {
      "수면": "수면",
      "식사": "식사",
      "직장/학교": "업무",
      "학습": "학습",
      "자기계발": "학습",
      "운동": "운동",
      "취미/오락": "여가",
      "휴식": "여가",
      "사회 활동": "여가",
      "개인정비": "여가",
      "가사": "여가",
      "이동": "여가",
    };

    const aggregatedHours = new Map<string, number>();
    const categories = Object.keys(IDEAL_HOURS);
    categories.forEach(key => aggregatedHours.set(key, 0));

    timeBlocks.forEach((block) => {
      if (block.actual) {
        const frontCategory = categoryMap[block.actual.categoryName];
        if (frontCategory) {
          const existing = aggregatedHours.get(frontCategory) || 0;
          aggregatedHours.set(frontCategory, existing + 0.5);
        }
      }
    });

    const highestActualHour = Math.max(...Array.from(aggregatedHours.values()));
    const highestIdealHour = Math.max(...Object.values(IDEAL_HOURS));
    const dynamicMax = Math.ceil(Math.max(8, highestActualHour, highestIdealHour));

    const current = categories.map((category) => {
      const actualHours = aggregatedHours.get(category) || 0;
      return Math.min(Math.round((actualHours / dynamicMax) * 100), 100);
    });

    const ideal = categories.map((category) => {
      const idealHours = IDEAL_HOURS[category as keyof typeof IDEAL_HOURS];
      return Math.min(Math.round((idealHours / dynamicMax) * 100), 100);
    });

    return { current, ideal };
  }, [timeBlocks]);

  const calculateMatch = (ideal: number[], current: number[]) => {
    if (ideal.length === 0 || current.length === 0) return 0
    const totalDiff = ideal.reduce((sum, val, idx) => sum + Math.abs(val - current[idx]), 0)
    const maxDiff = ideal.reduce((sum, val) => sum + val, 0)
    return Math.round(((maxDiff - totalDiff) / maxDiff) * 100)
  }

  const dailyMatch = calculateMatch(balanceChartData.ideal, balanceChartData.current)

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
      <div className="container mx-auto max-w-7xl p-6">
        <div className="mb-6">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-bold flex items-center gap-2">
                <BarChart3 className="h-8 w-8 text-primary" />
                밸런스 분석
              </h1>
              <p className="text-muted-foreground">이상적인 삶의 균형과 현재 상태를 비교합니다</p>
            </div>
          </div>
        </div>

        {isLoading ? (
          <BalanceSkeleton />
        ) : (
          <Tabs defaultValue="daily" className="space-y-6">
            <TabsList className="grid w-full max-w-md grid-cols-2">
              <TabsTrigger value="daily">일간 분석</TabsTrigger>
              <TabsTrigger value="weekly" disabled>주간 분석</TabsTrigger>
            </TabsList>

            <TabsContent value="daily" className="space-y-6">
              <div className="grid gap-6 lg:grid-cols-2">
                <Card>
                  <CardHeader>
                    <CardTitle>이상적인 밸런스</CardTitle>
                    <CardDescription>목표로 하는 하루의 균형</CardDescription>
                  </CardHeader>
                  <CardContent className="flex items-center justify-center py-8">
                    <HexagonChart
                      data={balanceChartData.ideal}
                      labels={Object.keys(IDEAL_HOURS)}
                      color="#3b82f6"
                    />
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader>
                    <CardTitle>현재 밸런스</CardTitle>
                    <CardDescription>오늘의 실제 활동 균형</CardDescription>
                  </CardHeader>
                  <CardContent className="flex items-center justify-center py-8">
                    <HexagonChart
                      data={balanceChartData.current}
                      labels={Object.keys(IDEAL_HOURS)}
                      color="#10b981"
                    />
                  </CardContent>
                </Card>
              </div>

              <Card className="bg-gradient-to-br from-primary/5 to-primary/10 border-primary/20">
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <TrendingUp className="h-5 w-5 text-primary" />
                    정합도 분석
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="grid gap-4 md:grid-cols-3">
                    <div className="rounded-lg border border-border bg-card p-4 text-center">
                      <div className="text-3xl font-bold text-primary">{dailyMatch}%</div>
                      <div className="text-sm text-muted-foreground">현재 정합도</div>
                    </div>
                    <div className="rounded-lg border border-border bg-card p-4 text-center">
                       <div className="text-xl font-bold">-</div>
                       <div className="text-sm text-muted-foreground">전일 데이터 없음</div>
                    </div>
                    <div className="rounded-lg border border-border bg-card p-4 text-center">
                       <div className="text-3xl font-bold text-blue-600">100%</div>
                       <div className="text-sm text-muted-foreground">이번 주 목표</div>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </TabsContent>
          </Tabs>
        )}
      </div>
    </div>
  )
}